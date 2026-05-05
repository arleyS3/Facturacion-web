package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import java.math.BigDecimal;

/**
 * Totales monetarios del comprobante.
 *
 * @param totalValorVenta total valor de venta (sin impuestos)
 * @param totalPrecioVenta total precio de venta (con impuestos)
 * @param totalDescuentos total de descuentos globales
 * @param importeTotalPagar importe total por pagar
 */
public record DatosTotalesMonetariosFacturaUbl(
    BigDecimal totalValorVenta,
    BigDecimal totalPrecioVenta,
    BigDecimal totalDescuentos,
    BigDecimal importeTotalPagar
) {}
