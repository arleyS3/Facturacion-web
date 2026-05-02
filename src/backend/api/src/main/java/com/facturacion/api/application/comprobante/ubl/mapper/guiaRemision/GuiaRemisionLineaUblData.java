package com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision;

import java.math.BigDecimal;

/**
 * Línea de detalle para guía de remisión UBL 2.1.
 *
 * @param numero número de línea
 * @param codigoProducto código del producto
 * @param descripcion descripción del ítem
 * @param cantidad cantidad
 * @param unidadMedida unidad de medida
 */
public record GuiaRemisionLineaUblData(
        Integer numero,
        String codigoProducto,
        String descripcion,
        BigDecimal cantidad,
        String unidadMedida
) {
}
