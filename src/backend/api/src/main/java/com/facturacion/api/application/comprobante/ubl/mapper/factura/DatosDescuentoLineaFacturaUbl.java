package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import java.math.BigDecimal;

/**
 * Datos de descuento por línea de factura.
 *
 * @param esCargo indica si es cargo (true) o descuento (false)
 * @param codigoMotivo código del motivo (catálogo 53)
 * @param porcentaje porcentaje del ajuste
 * @param monto monto del ajuste
 * @param montoBase monto base sobre el que aplica
 */
public record DatosDescuentoLineaFacturaUbl(
    boolean esCargo,
    String codigoMotivo,
    BigDecimal porcentaje,
    BigDecimal monto,
    BigDecimal montoBase
) {}
