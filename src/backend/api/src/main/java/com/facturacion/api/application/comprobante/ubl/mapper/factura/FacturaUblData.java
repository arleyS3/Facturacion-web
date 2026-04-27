package com.facturacion.api.application.comprobante.ubl.mapper.factura;

/**
 * Datos UBL 2.1 para factura electrónica.
 *
 * @param serie serie del comprobante
 * @param correlativo correlativo del comprobante
 * @param fechaEmision fecha de emisión
 * @param fechaVencimiento fecha de vencimiento
 * @param moneda código de moneda
 * @param tipoDocumento código de tipo de documento
 * @param emisorNroDocumento número de documento del emisor
 * @param emisorTipoDocumento tipo de documento del emisor
 * @param emisorRazonSocial razón social del emisor
 * @param emisorNombreComercial nombre comercial del emisor
 * @param emisorDireccion dirección del emisor
 * @param emisorUbigeo ubigeo del emisor
 * @param emisorUrbanizacion urbanización del emisor
 * @param emisorDepartamento departamento del emisor
 * @param emisorProvincia provincia del emisor
 * @param emisorDistrito distrito del emisor
 * @param emisorCodigoPais código de país del emisor
 * @param receptorNroDocumento número de documento del receptor
 * @param receptorTipoDocumento tipo de documento del receptor
 * @param receptorRazonSocial razón social del receptor
 * @param receptorDireccion dirección del receptor
 * @param receptorUbigeo ubigeo del receptor
 * @param receptorCodigoPais código de país del receptor
 * @param gravadas monto de operaciones gravadas
 * @param exoneradas monto de operaciones exoneradas
 * @param inafectas monto de operaciones inafectas
 * @param ISC monto del ISC
 * @param IGV monto del IGV
 * @param IVAP monto del IVAP
 * @param totalImpuestos total de impuestos
 * @param importeTotal importe total
 * @param montoPercepcion monto de percepción
 * @param importePercepcion importe de percepción
 * @param montoDetraccion monto de detracción
 * @param codigoDetraccion código de detracción
 * @param valorVenta valor de venta
 * @param lineas líneas de detalle
 */
public record FacturaUblData(
        String serie,
        String correlativo,
        String fechaEmision,
        String fechaVencimiento,
        String moneda,
        String tipoDocumento,
        String emisorNroDocumento,
        String emisorTipoDocumento,
        String emisorRazonSocial,
        String emisorNombreComercial,
        String emisorDireccion,
        String emisorUbigeo,
        String emisorUrbanizacion,
        String emisorDepartamento,
        String emisorProvincia,
        String emisorDistrito,
        String emisorCodigoPais,
        String receptorNroDocumento,
        String receptorTipoDocumento,
        String receptorRazonSocial,
        String receptorDireccion,
        String receptorUbigeo,
        String receptorCodigoPais,
        java.math.BigDecimal gravadas,
        java.math.BigDecimal exoneradas,
        java.math.BigDecimal inafectas,
        java.math.BigDecimal ISC,
        java.math.BigDecimal IGV,
        java.math.BigDecimal IVAP,
        java.math.BigDecimal totalImpuestos,
        java.math.BigDecimal importeTotal,
        java.math.BigDecimal montoPercepcion,
        java.math.BigDecimal importePercepcion,
        java.math.BigDecimal montoDetraccion,
        String codigoDetraccion,
        java.math.BigDecimal valorVenta,
        java.util.List<FacturaLineaUblData> lineas
) {
}
