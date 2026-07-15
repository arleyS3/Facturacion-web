package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import java.math.BigDecimal;

/**
 * Datos de percepción y detracción para la factura UBL.
 *
 * @param montoPercepcion monto de percepción
 * @param importePercepcion importe de percepción
 * @param montoDetraccion monto de detracción
 * @param codigoDetraccion código de detracción
 */
public record DatosPercepcionDetraccionFacturaUbl(
    BigDecimal montoPercepcion,
    BigDecimal importePercepcion,
    BigDecimal montoDetraccion,
    String codigoDetraccion
) {}
