package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos de impuesto total de la factura UBL.
 *
 * @param montoTotal monto total del impuesto
 * @param subtotales detalle de subtotales del impuesto
 */
public record DatosImpuestoTotalFacturaUbl(
    BigDecimal montoTotal,
    List<DatosImpuestoSubtotalFacturaUbl> subtotales
) {}
