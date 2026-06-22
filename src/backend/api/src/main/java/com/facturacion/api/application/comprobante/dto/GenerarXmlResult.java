package com.facturacion.api.application.comprobante.dto;

import java.util.List;

/**
 * Resultado de la generación de XML UBL.
 * <p>
 * Contiene el XML generado y una lista de errores de validación (vacía si no hay errores).
 * La validación es informativa y no bloquea la generación del XML.
 * </p>
 *
 * @param xml              contenido XML generado (puede ser {@code null} si ocurrió un error grave)
 * @param validationErrors lista de errores de validación XSD, nunca {@code null}
 */
public record GenerarXmlResult(
        String xml,
        List<String> validationErrors
) {

    /**
     * Constructor canónico con defensa contra {@code null} en {@code validationErrors}.
     * Crea una copia inmutable de la lista para preservar la inmutabilidad del record.
     */
    public GenerarXmlResult {
        validationErrors = validationErrors != null ? List.copyOf(validationErrors) : List.of();
    }

    /**
     * Retorna una representación compacta del resultado.
     *
     * @return representación en cadena
     */
    @Override
    public String toString() {
        return "GenerarXmlResult{xml='" + xml + "', validationErrors=" + validationErrors + '}';
    }
}
