package com.facturacion.api.application.comprobante.ubl.validator;

import com.facturacion.api.application.ValidationException;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validador básico de catálogos SUNAT para comprobantes canónicos.
 */
@Component
public class SunatCatalogValidator {

    /**
     * Valida campos mínimos según catálogos SUNAT.
     *
     * @param canonico comprobante canónico
     * @throws ValidationException si existen errores de validación
     */
    public void validar(ComprobanteCanonico canonico) {
        List<String> errores = new ArrayList<>();

        if (canonico == null) {
            throw new ValidationException("Validación SUNAT falló", List.of("Comprobante canónico nulo"));
        }

        if (canonico.tipoDocumento() == null || canonico.tipoDocumento().isBlank()) {
            errores.add("tipoDocumento es obligatorio");
        }

        if (canonico.moneda() == null || canonico.moneda().isBlank()) {
            errores.add("moneda es obligatoria");
        }

        if (!errores.isEmpty()) {
            throw new ValidationException("Validación SUNAT falló", errores);
        }
    }
}
