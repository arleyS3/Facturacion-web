package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import java.math.BigDecimal;

/**
 * Datos de precio de referencia para la línea de factura.
 *
 * @param precio precio de referencia
 * @param codigoTipoPrecio código del tipo de precio (catálogo 16)
 */
public record DatosPrecioReferenciaFacturaUbl(
    BigDecimal precio,
    String codigoTipoPrecio
) {}
