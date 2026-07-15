package com.facturacion.api.application.comprobante.ubl.validator;

import com.facturacion.api.application.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validador de esquema UBL.
 */
@Component
public class UblSchemaValidator {

    /**
     * Valida el XML UBL contra reglas básicas.
     *
     * @param xml xml a validar
     * @throws ValidationException si el xml es inválido
     */
    public void validar(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new ValidationException("Validación UBL falló", List.of("XML vacío o nulo"));
        }
    }
}
