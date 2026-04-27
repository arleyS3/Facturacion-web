package com.facturacion.api.application.comprobante.modelo;

import java.math.BigDecimal;

/**
 * Línea canónica del comprobante.
 *
 * @param descripcion descripción del ítem
 * @param cantidad cantidad
 * @param valorUnitario valor unitario
 * @param igv monto de IGV
 * @param codigoTipoIgv código de afectación IGV
 */
public record DetalleCanonico(
        String descripcion,
        BigDecimal cantidad,
        BigDecimal valorUnitario,
        BigDecimal igv,
        String codigoTipoIgv
) {
}
