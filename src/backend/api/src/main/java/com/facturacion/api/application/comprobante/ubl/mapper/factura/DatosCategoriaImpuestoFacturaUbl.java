package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import java.math.BigDecimal;

/**
 * Datos de categoría de impuesto UBL.
 *
 * @param codigoCategoria código de categoría (S, E, O, etc.)
 * @param porcentaje porcentaje de impuesto
 * @param codigoAfectacionIgv código de afectación del IGV
 * @param esquema datos del esquema de impuesto
 */
public record DatosCategoriaImpuestoFacturaUbl(
    String codigoCategoria,
    BigDecimal porcentaje,
    String codigoAfectacionIgv,
    DatosEsquemaImpuestoFacturaUbl esquema
) {}
