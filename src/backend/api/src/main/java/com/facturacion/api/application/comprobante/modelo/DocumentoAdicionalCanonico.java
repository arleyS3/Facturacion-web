package com.facturacion.api.application.comprobante.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Documento adicional referenciado en el comprobante.
 * <p>
 * Corresponde a AdditionalDocumentReference de UBL 2.1.
 * Opcional — solo se envía cuando el frontend provee el dato.
 * </p>
 *
 * @param id identificador del documento adicional
 * @param tipoDocumento tipo de documento (catálogo 12)
 */
public record DocumentoAdicionalCanonico(
        String id,

        @JsonProperty("tipo_documento")
        String tipoDocumento
) {}
