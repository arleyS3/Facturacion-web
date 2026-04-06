package com.facturacion.api.web.dto;

/**
 * Resultado de la importación de una trama TXT.
 *
 * @param tipoDocumento tipo de documento detectado (código SUNAT o alias)
 * @param secciones payload por secciones reconstruido a partir del TXT (campos + listas)
 */
public record ImportarTramaResponse(String tipoDocumento, SeccionesPayload secciones) {
}
