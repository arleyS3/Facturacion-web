package com.facturacion.api.application.comprobante.ubl.mapper.factura;

/**
 * Datos de firma digital para la factura UBL.
 *
 * @param id identificador de la firma
 * @param rucFirmante RUC del firmante
 * @param nombreFirmante nombre del firmante
 * @param uriReferencia URI de referencia de la firma (ej. #SignatureSP)
 */
public record DatosFirmaFacturaUbl(
    String id,
    String rucFirmante,
    String nombreFirmante,
    String uriReferencia
) {}
