package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DescuentoGlobalCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import com.facturacion.api.application.comprobante.modelo.DocumentoAdicionalCanonico;
import com.facturacion.api.application.comprobante.modelo.GuiaRemisionReferenciaCanonico;
import com.facturacion.api.application.comprobante.modelo.LeyendaCanonico;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Mapper de comprobante canónico a datos UBL de factura.
 */
@Component
public class FacturaUblMapper {

    /** Porcentaje de IGV estándar. */
    private static final BigDecimal PORCENTAJE_IGV = new BigDecimal("0.18");

    /**
     * Convierte el comprobante canónico a {@link FacturaUblData}.
     *
     * @param canonico comprobante canónico
     * @return datos UBL de factura
     */
    public FacturaUblData fromCanonico(ComprobanteCanonico canonico) {
        String[] numeros =
            canonico.numero() != null
                ? canonico.numero().split("-")
                : new String[] { "F001", "1" };
        String serie = numeros.length > 0 ? numeros[0] : "F001";
        String correlativo = numeros.length > 1 ? numeros[1] : "1";

        // Hora de emisión: usa la del canonico o asume una por defecto
        String horaEmision = canonico.horaEmision() != null 
            ? canonico.horaEmision() 
            : "10:00:00";

        List<FacturaLineaUblData> lineas =
            canonico.detalles() != null
                ? mapLineas(canonico.detalles())
                : List.of();

        // Mapear descuentos globales
        List<DatosDescuentoGlobalFacturaUbl> descuentosGlobales =
            mapDescuentosGlobales(canonico.descuentosGlobales());

        // Calcular totales desde las líneas
        DatosTotalesFacturaUbl totales = calcularTotales(lineas, canonico.moneda());

        // Sumar montos de descuentos (esCargo == false) para restar de importe total
        BigDecimal totalDescuentos = descuentosGlobales.stream()
            .filter(d -> !d.esCargo())
            .map(DatosDescuentoGlobalFacturaUbl::monto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        DatosTotalesMonetariosFacturaUbl totalesMonetarios = calcularTotalesMonetarios(
            totales, canonico.moneda(), totalDescuentos);

        DatosEncabezadoFacturaUbl encabezado = new DatosEncabezadoFacturaUbl(
            serie,
            correlativo,
            horaEmision,
            canonico.fechaEmision(),
            canonico.fechaVencimiento(),
            canonico.tipoDeOperacion() != null ? canonico.tipoDeOperacion() : "0101",
            canonico.moneda(),
            canonico.tipoDocumento()
        );

        DatosEmisorFacturaUbl emisor = new DatosEmisorFacturaUbl(
            canonico.emisorRuc(),
            "6", // Tipo de documento: RUC
            canonico.emisorRazonSocial(),
            canonico.emisorNombreComercial(),
            canonico.emisorDireccion(),
            canonico.emisorUbigeo(),
            canonico.emisorUrbanizacion(),
            canonico.emisorDepartamento(),
            canonico.emisorProvincia(),
            canonico.emisorDistrito(),
            "PE", // Código de país
            canonico.emisorCodigoDomicilio() != null ? canonico.emisorCodigoDomicilio() : "0001"
        );

        DatosReceptorFacturaUbl receptor = new DatosReceptorFacturaUbl(
            canonico.receptorDocumento(),
            null, // Tipo de documento del receptor (opcional)
            canonico.receptorRazonSocial(),
            canonico.receptorDireccion(),
            canonico.receptorUbigeo(),
            "PE" // Código de país por defecto
        );

        GuiaRemisionReferenciaCanonico guiaRemision = canonico.guiaRemision();

        return new FacturaUblData(
            encabezado,
            emisor,
            receptor,
            null, // firma
            null, // referenciaOrden
            lineas.isEmpty() ? null : lineas.size(),
            descuentosGlobales,
            totales,
            null, // impuestosTotales (opcional)
            totalesMonetarios,
            null, // percepcionDetraccion
            lineas,
            mapLeyendas(canonico.leyendas()),
            guiaRemision != null ? guiaRemision.id() : null,
            guiaRemision != null ? guiaRemision.codigoDocumento() : null,
            mapDocumentosAdicionales(canonico.documentosAdicionales())
        );
    }

    /**
     * Calcula los totales de la factura desde las líneas.
     *
     * @param lineas lista de líneas UBL
     * @param moneda código de moneda
     * @return totales calculados
     */
    private DatosTotalesFacturaUbl calcularTotales(List<FacturaLineaUblData> lineas, String moneda) {
        BigDecimal gravadas = BigDecimal.ZERO;
        BigDecimal exoneradas = BigDecimal.ZERO;
        BigDecimal inafectas = BigDecimal.ZERO;
        BigDecimal totalIgv = BigDecimal.ZERO;
        BigDecimal totalIsc = BigDecimal.ZERO;
        BigDecimal valorVenta = BigDecimal.ZERO;

        for (FacturaLineaUblData linea : lineas) {
            BigDecimal cantidad = linea.cantidad() != null ? linea.cantidad() : BigDecimal.ZERO;
            BigDecimal valorUnitario = linea.precioUnitario() != null 
                ? linea.precioUnitario() 
                : BigDecimal.ZERO;
            BigDecimal montoIgv = linea.montoIGV() != null ? linea.montoIGV() : BigDecimal.ZERO;
            BigDecimal montoIsc = linea.montoISC() != null ? linea.montoISC() : BigDecimal.ZERO;
            String tipoAfectacion = linea.tipoAfectacionIGV();

            // Calcular valor de venta de la línea
            BigDecimal valorLinea = cantidad.multiply(valorUnitario);
            valorVenta = valorVenta.add(valorLinea);

            // Clasificar según tipo de afectación IGV
            // 10 = Gravado, 20 = Exonerado, 30/31 = Inafecto
            if ("10".equals(tipoAfectacion)) {
                // Gravado: el montoBaseIGV es el valor sin IGV
                BigDecimal montoBase = montoIgv.compareTo(BigDecimal.ZERO) > 0
                    ? montoIgv.divide(PORCENTAJE_IGV, 2, RoundingMode.HALF_UP)
                    : valorLinea;
                gravadas = gravadas.add(montoBase);
                totalIgv = totalIgv.add(montoIgv);
            } else if ("20".equals(tipoAfectacion)) {
                // Exonerado
                exoneradas = exoneradas.add(valorLinea);
            } else if ("30".equals(tipoAfectacion) || "31".equals(tipoAfectacion)) {
                // Inafecto
                inafectas = inafectas.add(valorLinea);
            }

            // Acumular ISC
            totalIsc = totalIsc.add(montoIsc);
        }

        BigDecimal totalImpuestos = totalIgv.add(totalIsc);
        BigDecimal importeTotal = valorVenta.add(totalImpuestos);

        return new DatosTotalesFacturaUbl(
            gravadas,
            exoneradas,
            inafectas,
            totalIsc, // ISC
            totalIgv,
            null, // IVAP
            totalImpuestos,
            importeTotal,
            valorVenta
        );
    }

    /**
     * Calcula los totales monetarios.
     *
     * @param totales totales de impuestos
     * @param moneda código de moneda
     * @param totalDescuentos suma de montos de descuentos globales (esCargo == false)
     * @return totales monetarios
     */
    DatosTotalesMonetariosFacturaUbl calcularTotalesMonetarios(
            DatosTotalesFacturaUbl totales, String moneda, BigDecimal totalDescuentos) {
        BigDecimal descuentos = totalDescuentos != null ? totalDescuentos : BigDecimal.ZERO;
        BigDecimal importeTotalPagar = totales.importeTotal().subtract(descuentos);
        return new DatosTotalesMonetariosFacturaUbl(
            totales.valorVenta(),
            totales.importeTotal(),
            descuentos,
            importeTotalPagar
        );
    }

    /**
     * Mapea la lista de descuentos globales canónicos a datos UBL.
     *
     * @param descuentos lista de descuentos canónicos (puede ser null)
     * @return lista de descuentos UBL, nunca null
     */
    private List<DatosDescuentoGlobalFacturaUbl> mapDescuentosGlobales(
            List<DescuentoGlobalCanonico> descuentos) {
        if (descuentos == null) {
            return List.of();
        }
        return descuentos.stream()
                .map(this::toDatosDescuentoGlobal)
                .toList();
    }

    /**
     * Convierte un {@link DescuentoGlobalCanonico} a {@link DatosDescuentoGlobalFacturaUbl}.
     *
     * @param d descuento global canónico
     * @return datos UBL de descuento global
     */
    public DatosDescuentoGlobalFacturaUbl toDatosDescuentoGlobal(DescuentoGlobalCanonico d) {
        return new DatosDescuentoGlobalFacturaUbl(
            d.esCargo() != null && d.esCargo(),
            d.codigoMotivo(),
            d.porcentaje(),
            d.monto(),
            d.montoBase()
        );
    }

    /**
     * Mapea leyendas canónicas a leyendas UBL.
     *
     * @param leyendas lista de leyendas canónicas
     * @return lista de leyendas UBL
     */
    private List<LeyendaUblData> mapLeyendas(List<LeyendaCanonico> leyendas) {
        if (leyendas == null || leyendas.isEmpty()) {
            return List.of();
        }
        return leyendas.stream()
                .map(l -> new LeyendaUblData(l.codigoLocal(), l.leyenda()))
                .toList();
    }

    /**
     * Mapea documentos adicionales canónicos a datos UBL.
     *
     * @param docs lista de documentos adicionales canónicos (puede ser null)
     * @return lista de datos UBL, nunca null
     */
    private List<DocumentoAdicionalUblData> mapDocumentosAdicionales(
            List<DocumentoAdicionalCanonico> docs) {
        if (docs == null || docs.isEmpty()) {
            return List.of();
        }
        return docs.stream()
                .map(d -> new DocumentoAdicionalUblData(d.id(), d.tipoDocumento()))
                .toList();
    }

    /**
     * Mapea las líneas canónicas a líneas UBL de factura con número secuencial.
     *
     * @param detalles lista de detalles canónicos
     * @return lista de líneas UBL con número secuencial
     */
    private List<FacturaLineaUblData> mapLineas(List<DetalleCanonico> detalles) {
        List<FacturaLineaUblData> lineas = new ArrayList<>();
        int numeroLinea = 1;
        for (DetalleCanonico detalle : detalles) {
            lineas.add(mapLinea(detalle, numeroLinea));
            numeroLinea++;
        }
        return lineas;
    }

    /**
     * Mapea una línea canónica a línea UBL de factura.
     *
     * @param detalle detalle canónico
     * @return línea UBL
     */
    private FacturaLineaUblData mapLinea(DetalleCanonico detalle) {
        return mapLinea(detalle, null);
    }

    /**
     * Mapea una línea canónica a línea UBL de factura con número de línea.
     *
     * @param detalle detalle canónico
     * @param numeroLinea número de línea (1, 2, 3...)
     * @return línea UBL
     */
    private FacturaLineaUblData mapLinea(DetalleCanonico detalle, Integer numeroLinea) {
        BigDecimal cantidad = detalle.cantidad();
        BigDecimal valorUnitario = detalle.valorUnitario();
        BigDecimal valorVenta = cantidad.multiply(valorUnitario);
        BigDecimal montoIgv = detalle.igv();
        BigDecimal montoIsc = detalle.iscMonto() != null ? detalle.iscMonto() : BigDecimal.ZERO;
        String iscTipoSistema = detalle.iscTipoSistema();
        
        // Calcular monto base IGV (valor sin impuesto)
        BigDecimal montoBaseIGV = montoIgv.compareTo(BigDecimal.ZERO) > 0
            ? montoIgv.divide(PORCENTAJE_IGV, 2, RoundingMode.HALF_UP)
            : valorVenta;

        return new FacturaLineaUblData(
            numeroLinea,                    // numero
            detalle.codigoProducto(),    // codigoProducto
            null,                         // codigoProductoSUNAT
            detalle.descripcion(),        // descripcion
            cantidad,                    // cantidad
            detalle.unidadMedida() != null ? detalle.unidadMedida() : "NIU", // unidadMedida
            valorUnitario,               // precioUnitario
            null,                        // precioReferencia
            null,                        // descuentoLinea
            null,                        // precioUnitarioSinIGV
            montoBaseIGV,                 // montoBaseIGV
            montoIgv,                    // montoIGV
            PORCENTAJE_IGV.multiply(new BigDecimal("100")), // porcentajeIGV (18.00)
            detalle.codigoTipoIgv(),     // tipoAfectacionIGV
            montoIsc.compareTo(BigDecimal.ZERO) > 0 ? montoIsc : null,  // montoISC
            iscTipoSistema,              // tipoSistemaISC
            valorVenta,                  // valorVenta
            null                         // valorVentaUnitario
        );
    }
}
