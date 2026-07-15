package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import java.math.BigDecimal;

/**
 * Totales e impuestos resumidos de la factura UBL.
 *
 * @param gravadas monto de operaciones gravadas
 * @param exoneradas monto de operaciones exoneradas
 * @param inafectas monto de operaciones inafectas
 * @param isc monto del ISC
 * @param igv monto del IGV
 * @param ivap monto del IVAP
 * @param totalImpuestos total de impuestos
 * @param importeTotal importe total
 * @param valorVenta valor de venta
 */
public record DatosTotalesFacturaUbl(
    BigDecimal gravadas,
    BigDecimal exoneradas,
    BigDecimal inafectas,
    BigDecimal isc,
    BigDecimal igv,
    BigDecimal ivap,
    BigDecimal totalImpuestos,
    BigDecimal importeTotal,
    BigDecimal valorVenta
) {}
