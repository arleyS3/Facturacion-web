package com.facturacion.api.application.comprobante.ubl.mapper.factura;

/**
 * Datos de esquema de impuesto UBL.
 *
 * @param codigoTributo código del tributo
 * @param nombreTributo nombre del tributo
 * @param codigoTipoTributo código del tipo de tributo
 */
public record DatosEsquemaImpuestoFacturaUbl(
    String codigoTributo,
    String nombreTributo,
    String codigoTipoTributo
) {}
