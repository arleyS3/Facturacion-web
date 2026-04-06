package com.facturacion.api.web.error;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO para representar errores devueltos por la API.
 *
 * @param message   resumen del error
 * @param details   lista de detalles o mensajes específicos
 * @param timestamp instante en que se produjo la respuesta de error
 */
public record ErrorResponse(
        String message,
        List<String> details,
        OffsetDateTime timestamp
) {
    /**
     * Crea una instancia de ErrorResponse con timestamp actual.
     *
     * @param message resumen del error
     * @param details detalles del error
     * @return instancia de ErrorResponse
     */
    public static ErrorResponse of(String message, List<String> details) {
        return new ErrorResponse(message, details, OffsetDateTime.now());
    }
}
