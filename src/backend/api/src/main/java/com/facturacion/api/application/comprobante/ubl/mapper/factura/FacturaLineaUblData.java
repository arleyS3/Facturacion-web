package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import java.math.BigDecimal;

/**
 * Línea de detalle para factura UBL 2.1.
 *
 * @param numero número de línea
 * @param codigoProducto código del producto
 * @param codigoProductoSunat código SUNAT del producto
 * @param descripcion descripción del ítem
 * @param cantidad cantidad
 * @param unidadMedida unidad de medida
 * @param precioUnitario precio unitario
 * @param precioUnitarioSinIGV precio unitario sin IGV
 * @param montoBaseIGV base imponible IGV
 * @param montoIGV monto de IGV
 * @param porcentajeIGV porcentaje de IGV
 * @param tipoAfectacionIGV tipo de afectación IGV
 * @param montoISC monto ISC
 * @param tipoSistemaISC tipo de sistema ISC
 * @param valorVenta valor de venta
 * @param valorVentaUnitario valor de venta unitario
 */
public record FacturaLineaUblData(
        Integer numero,
        String codigoProducto,
        String codigoProductoSunat,
        String descripcion,
        BigDecimal cantidad,
        String unidadMedida,
        BigDecimal precioUnitario,
        BigDecimal precioUnitarioSinIGV,
        BigDecimal montoBaseIGV,
        BigDecimal montoIGV,
        BigDecimal porcentajeIGV,
        String tipoAfectacionIGV,
        BigDecimal montoISC,
        String tipoSistemaISC,
        BigDecimal valorVenta,
        BigDecimal valorVentaUnitario
) {
}
