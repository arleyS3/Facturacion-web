package com.facturacion.api.application.comprobante.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Referencia a una guía de remisión relacionada con el comprobante.
 * <p>
 * Corresponde a DespatchDocumentReference de UBL 2.1.
 * Opcional — solo se envía cuando el frontend provee el dato.
 * </p>
 * <p>
 * El ID final se forma como serie + "-" + numero (ej: "0001-002020").
 * El código de documento puede ser "09" (guía remitente) o "31" (guía transportista)
 * según catálogo 01 de SUNAT.
 * </p>
 *
 * @param serie serie del documento de guía
 * @param numero número del documento de guía
 * @param codigoDocumento código del tipo de documento según catálogo 01
 */
public record GuiaRemisionReferenciaCanonico(
        String serie,

        String numero,

        @JsonProperty("codigo_documento")
        String codigoDocumento
) {
    /**
     * Construye el ID completo en formato "serie-numero".
     *
     * @return ID formateado para el XML
     */
    public String id() {
        return (serie != null ? serie : "") + "-" + (numero != null ? numero : "");
    }
}
