package com.facturacion.api.application.comprobante.ubl.mapper.boleta;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos UBL 2.1 para boleta electrónica.
 *
 * @param serie serie del comprobante
 * @param correlativo correlativo del comprobante
 * @param fechaEmision fecha de emisión
 * @param moneda código de moneda
 * @param emisorNroDocumento número de documento del emisor
 * @param emisorTipoDocumento tipo de documento del emisor
 * @param emisorRazonSocial razón social del emisor
 * @param receptorNroDocumento número de documento del receptor
 * @param receptorTipoDocumento tipo de documento del receptor
 * @param receptorRazonSocial razón social del receptor
 * @param gravadas monto de operaciones gravadas
 * @param exoneradas monto de operaciones exoneradas
 * @param inafectas monto de operaciones inafectas
 * @param IGV monto de IGV
 * @param totalImpuestos total de impuestos
 * @param importeTotal importe total
 * @param valorVenta valor de venta
 * @param lineas líneas de detalle
 */
public record BoletaUblData(
        String serie,
        String correlativo,
        String fechaEmision,
        String horaEmision,
        String moneda,
        String emisorNroDocumento,
        String emisorTipoDocumento,
        String emisorRazonSocial,
        String receptorNroDocumento,
        String receptorTipoDocumento,
        String receptorRazonSocial,
        BigDecimal gravadas,
        BigDecimal exoneradas,
        BigDecimal inafectas,
        BigDecimal IGV,
        BigDecimal totalImpuestos,
        BigDecimal importeTotal,
        BigDecimal valorVenta,
        List<BoletaLineaUblData> lineas
) {
}
