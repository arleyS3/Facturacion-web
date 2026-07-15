package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import java.math.BigDecimal;

/**
 * Datos de subtotal de impuesto de la factura UBL.
 *
 * @param montoBase base imponible
 * @param montoImpuesto monto del impuesto
 * @param categoria categoria del impuesto
 */
public record DatosImpuestoSubtotalFacturaUbl(
    BigDecimal montoBase,
    BigDecimal montoImpuesto,
    DatosCategoriaImpuestoFacturaUbl categoria
) {}
