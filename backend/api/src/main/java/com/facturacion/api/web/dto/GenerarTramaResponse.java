package com.facturacion.api.web.dto;

/**
 * Respuesta de la operación de generación de trama.
 *
 * @param tipoDocumento código o alias del tipo de documento solicitado
 * @param trama contenido de la trama en formato TXT (como String)
 */
public record GenerarTramaResponse(String tipoDocumento, String trama) {
}
