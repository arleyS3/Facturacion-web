package com.facturacion.api.application.comprobante.ubl.mapper.boleta;

import com.facturacion.api.application.comprobante.modelo.AnticipoCanonico;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DescuentoGlobalCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import com.facturacion.api.application.comprobante.modelo.DocumentoAdicionalCanonico;
import com.facturacion.api.application.comprobante.modelo.GuiaRemisionReferenciaCanonico;
import com.facturacion.api.application.comprobante.modelo.LeyendaCanonico;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DocumentoAdicionalUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.LeyendaUblData;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper de comprobante canónico a datos UBL de boleta.
 */
@Component
public class BoletaUblMapper {
    /**
     * Convierte el comprobante canónico a {@link BoletaUblData}.
     *
     * @param canonico comprobante canónico
     * @return datos UBL de boleta
     */

    private static final BigDecimal PORCENTAJE_IGV = new BigDecimal("0.18");

    public BoletaUblData fromCanonico(ComprobanteCanonico canonico) {
        String[] numeros = canonico.numero() != null ? canonico.numero().split("-") : new String[]{"B001", "1"};
        String serie = numeros.length > 0 ? numeros[0] : "B001";
        String correlativo = numeros.length > 1 ? numeros[1] : "1";

        String horaEmision = canonico.horaEmision() != null ? canonico.horaEmision() : "00:00:00";

        List<BoletaLineaUblData> lineas = canonico.detalles() != null 
                ? mapLineas(canonico.detalles()) 
                : List.of();

        BigDecimal gravadas = BigDecimal.ZERO;
        BigDecimal exoneradas = BigDecimal.ZERO;
        BigDecimal inafectadas = BigDecimal.ZERO;
        BigDecimal totalIgv = BigDecimal.ZERO;
        BigDecimal valorVenta = BigDecimal.ZERO;

        for (BoletaLineaUblData linea : lineas) {
            BigDecimal montoIgv = linea.montoIGV() != null ? linea.montoIGV() : BigDecimal.ZERO;
            BigDecimal vlVenta = linea.valorVenta() != null ? linea.valorVenta() : BigDecimal.ZERO;
            String tipoAfectacion = linea.tipoAfectacionIGV();

            valorVenta = valorVenta.add(vlVenta);

            if ("10".equals(tipoAfectacion)) {
                BigDecimal base = montoIgv.compareTo(BigDecimal.ZERO) > 0 
                        ? montoIgv.divide(PORCENTAJE_IGV, 2, RoundingMode.HALF_UP) 
                        : vlVenta;
                gravadas = gravadas.add(base);
                totalIgv = totalIgv.add(montoIgv);      
            } else if ("20".equals(tipoAfectacion)) {
                exoneradas = exoneradas.add(vlVenta);
            } else if ("30".equals(tipoAfectacion) || "31".equals(tipoAfectacion)) {
                inafectadas = inafectadas.add(vlVenta);
            }
        }

        BigDecimal totalImpuestos = totalIgv;
        BigDecimal importeTotal = valorVenta.add(totalImpuestos);

        String receptorNroDoc = canonico.receptorDocumento();
        String receptorTipoDoc = resolverTipoDocumentoReceptor(receptorNroDoc); 
        String receptorRazonSocial = canonico.receptorRazonSocial() != null 
                ? canonico.receptorRazonSocial() 
                : "-";

        if ("0".equals(receptorTipoDoc)) {
            receptorNroDoc = "-";
        }

        // Leer descuento global desde el canónico (soporta el primero si hay varios)
        DescuentoGlobalCanonico primerDescuento = extraerPrimerDescuentoGlobal(canonico.descuentosGlobales());

        // Leer guía de remisión, documentos adicionales y anticipo desde el canónico
        GuiaRemisionReferenciaCanonico guiaRemision = canonico.guiaRemision();
        AnticipoCanonico primerAnticipo = extraerPrimerAnticipo(canonico.anticipos());

        return new BoletaUblData(
            serie,
            correlativo,
            canonico.fechaEmision(),
            horaEmision,
            canonico.moneda(),
            canonico.emisorRuc(),
            "6",
            canonico.emisorRazonSocial(),
            receptorNroDoc,
            receptorTipoDoc,
            receptorRazonSocial,
            gravadas,
            exoneradas,
            inafectadas,
            totalIgv,
            totalImpuestos,
            importeTotal,
            valorVenta,
            lineas,
            mapLeyendas(canonico.leyendas()),
            primerDescuento != null,                                          // tieneDescuentoGlobal
            primerDescuento != null ? primerDescuento.monto() : BigDecimal.ZERO,
            primerDescuento != null ? primerDescuento.montoBase() : BigDecimal.ZERO,
            guiaRemision != null ? guiaRemision.id() : null,
            guiaRemision != null ? guiaRemision.codigoDocumento() : null,
            mapDocumentosAdicionales(canonico.documentosAdicionales()),
            primerAnticipo != null ? primerAnticipo.id() : null,
            primerAnticipo != null ? primerAnticipo.tipoDocumento() : null,
            primerAnticipo != null ? primerAnticipo.monto() : null,
            primerAnticipo != null ? primerAnticipo.moneda() : null,
            primerAnticipo != null ? primerAnticipo.rucEmisor() : null
        );
    }

    private String resolverTipoDocumentoReceptor(String nroDocumento) {
        if (nroDocumento == null || nroDocumento.isBlank() || "-".equals(nroDocumento)) {
            return "0"; 
        }

        return switch (nroDocumento.trim().length()) {
            case 8 -> "1";
            case 11 -> "6";
            default -> "0";
        }; 
    }

    private List<BoletaLineaUblData> mapLineas(List<DetalleCanonico> detalles) {
        List<BoletaLineaUblData> lineas = new ArrayList<>();
        int numeroLinea = 1;
        for (DetalleCanonico detalle : detalles) {
            lineas.add(mapLinea(detalle, numeroLinea));
            numeroLinea++;
        }

        return lineas;
    }

    /**
     * Mapea una línea canónica a línea UBL de boleta.
     *
     * @param detalle detalle canónico
     * @return línea UBL
     */
    private BoletaLineaUblData mapLinea(DetalleCanonico detalle, int numeroLinea) {

        BigDecimal cantidad = detalle.cantidad() != null ? detalle.cantidad() : BigDecimal.ZERO;
        BigDecimal valorUnitario = detalle.valorUnitario() != null ? detalle.valorUnitario() : BigDecimal.ZERO;
        BigDecimal valorVenta = cantidad.multiply(valorUnitario);

        return new BoletaLineaUblData(
            numeroLinea,
            detalle.codigoProducto(),
            detalle.descripcion(),
            cantidad, 
            detalle.unidadMedida() != null ? detalle.unidadMedida() : "NIU",
            detalle.igv(),
            new BigDecimal("18.00"),
            detalle.codigoTipoIgv(),
            detalle.valorUnitario(),
            valorVenta
        );
    }

    /**
     * Extrae el primer descuento global de la lista, si existe.
     * Boleta soporta un solo descuento global por ahora.
     *
     * @param descuentos lista de descuentos globales canónicos (puede ser null)
     * @return el primer descuento o null si no hay
     */
    private DescuentoGlobalCanonico extraerPrimerDescuentoGlobal(List<DescuentoGlobalCanonico> descuentos) {
        if (descuentos == null || descuentos.isEmpty()) {
            return null;
        }
        return descuentos.get(0);
    }

    /**
     * Extrae el primer anticipo de la lista, si existe.
     * Boleta soporta un solo anticipo por ahora.
     *
     * @param anticipos lista de anticipos canónicos (puede ser null)
     * @return el primer anticipo o null si no hay
     */
    private AnticipoCanonico extraerPrimerAnticipo(List<AnticipoCanonico> anticipos) {
        if (anticipos == null || anticipos.isEmpty()) {
            return null;
        }
        return anticipos.get(0);
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
}
