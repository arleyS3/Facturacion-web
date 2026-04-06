package com.facturacion.api.application;

import java.util.List;

/**
 * Excepción lanzada cuando la validación de un request falla.
 * Contiene una lista de mensajes detallando los errores detectados.
 */
public class ValidationException extends RuntimeException {
    private final List<String> details;

    public ValidationException(String message, List<String> details) {
        super(message);
        this.details = details;
    }

    /** Devuelve los detalles (mensajes) de la validación fallida. */
    public List<String> getDetails() {
        return details;
    }
}
