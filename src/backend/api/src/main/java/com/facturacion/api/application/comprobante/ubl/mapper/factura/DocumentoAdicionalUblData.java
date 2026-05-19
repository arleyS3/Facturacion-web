package com.facturacion.api.application.comprobante.ubl.mapper.factura;

/**
 * Documento adicional referenciado en el comprobante UBL.
 * <p>
 * Corresponde a {@code cac:AdditionalDocumentReference} de UBL 2.1.
 * </p>
 *
 * @param id identificador del documento
 * @param tipoDocumento código del tipo de documento (catálogo 12)
 */
public record DocumentoAdicionalUblData(
        String id,
        String tipoDocumento
) {}
