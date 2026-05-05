package com.facturacion.api.application.comprobante.ubl.mapper.factura;

/**
 * Datos del receptor para la factura UBL.
 *
 * @param nroDocumento número de documento del receptor
 * @param tipoDocumento tipo de documento del receptor
 * @param razonSocial razón social del receptor
 * @param direccion dirección del receptor
 * @param ubigeo ubigeo del receptor
 * @param codigoPais código de país del receptor
 */
public record DatosReceptorFacturaUbl(
    String nroDocumento,
    String tipoDocumento,
    String razonSocial,
    String direccion,
    String ubigeo,
    String codigoPais
) {}
