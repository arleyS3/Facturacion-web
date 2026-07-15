package com.facturacion.api.application.comprobante.ubl.mapper.boleta;

import java.math.BigDecimal;

/**
 * Línea de detalle para boleta UBL 2.1.
 *
 * @param numero número de línea
 * @param codigoProducto código del producto
 * @param descripcion descripción del ítem
 * @param cantidad cantidad
 * @param unidadMedida unidad de medida
 * @param montoIGV monto de IGV
 * @param porcentajeIGV porcentaje de IGV
 * @param tipoAfectacionIGV tipo de afectación IGV
 * @param precioUnitario precio unitario
 * @param valorVenta valor de venta
 */
public record BoletaLineaUblData(
        Integer numero,
        String codigoProducto,
        String descripcion,
        BigDecimal cantidad,
        String unidadMedida,
        BigDecimal montoIGV,
        BigDecimal porcentajeIGV,
        String tipoAfectacionIGV,
        BigDecimal precioUnitario,
        BigDecimal valorVenta
) {
}
