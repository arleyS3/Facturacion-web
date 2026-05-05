package com.facturacion.api.application.comprobante.ubl.mapper.factura;

/**
 * Datos del emisor para la factura UBL.
 *
 * @param nroDocumento número de documento del emisor
 * @param tipoDocumento tipo de documento del emisor
 * @param razonSocial razón social del emisor
 * @param nombreComercial nombre comercial del emisor
 * @param direccion dirección del emisor
 * @param ubigeo ubigeo del emisor
 * @param urbanizacion urbanización del emisor
 * @param departamento departamento del emisor
 * @param provincia provincia del emisor
 * @param distrito distrito del emisor
 * @param codigoPais código de país del emisor
 * @param codigoDomicilio código de domicilio fiscal
 */
public record DatosEmisorFacturaUbl(
    String nroDocumento,
    String tipoDocumento,
    String razonSocial,
    String nombreComercial,
    String direccion,
    String ubigeo,
    String urbanizacion,
    String departamento,
    String provincia,
    String distrito,
    String codigoPais,
    String codigoDomicilio
) {}
