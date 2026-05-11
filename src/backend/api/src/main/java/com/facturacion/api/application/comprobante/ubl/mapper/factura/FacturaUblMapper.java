package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Mapper de comprobante canónico a datos UBL de factura.
 */
@Component
public class FacturaUblMapper {

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
                ? canonico.detalles().stream().map(this::mapLinea).toList()
                : List.of();

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

        return new FacturaUblData(
            encabezado,
            emisor,
            receptor,
            null, // firma
            null, // referenciaOrden
            lineas.isEmpty() ? null : lineas.size(),
            List.of(), // descuentos globales
            null, // totales (calculable en builder)
            null, // impuestosTotales
            null, // totalesMonetarios (calculable en builder)
            null, // percepcionDetraccion
            lineas,
            null  // leyendas
        );
    }

    /**
     * Mapea una línea canónica a línea UBL de factura.
     *
     * @param detalle detalle canónico
     * @return línea UBL
     */
    private FacturaLineaUblData mapLinea(DetalleCanonico detalle) {
        return new FacturaLineaUblData(
            null,                         // numero
            detalle.codigoProducto(),    // codigoProducto
            null,                         // codigoProductoSUNAT
            detalle.descripcion(),        // descripcion
            detalle.cantidad(),          // cantidad
            "NIU",                       // unidadMedida
            detalle.valorUnitario(),      // precioUnitario
            null,                        // precioReferencia
            null,                        // descuentoLinea
            null,                        // precioUnitarioSinIGV
            null,                        // montoBaseIGV (calculable)
            detalle.igv(),              // montoIGV
            new java.math.BigDecimal("18.00"),  // porcentajeIGV (asumido)
            detalle.codigoTipoIgv(),     // tipoAfectacionIGV
            null,                        // montoISC
            null,                        // tipoSistemaISC
            null,                        // valorVenta (calculable)
            null                         // valorVentaUnitario
        );
    }
}
