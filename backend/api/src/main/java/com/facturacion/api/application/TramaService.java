package com.facturacion.api.application;

import com.facturacion.api.web.dto.GenerarTramaRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Servicio responsable de la generación de tramas a partir de un request.
 *
 * Encapsula la validación del request y delega la construcción de la trama
 * al {@link TramaAssembler}.
 */
@Service
@RequiredArgsConstructor
public class TramaService {

    /** Ensamblador responsable de construir la trama. */
    private final TramaAssembler tramaAssembler;

    /** Servicio que valida las solicitudes antes de generar la trama. */
    private final RequestValidationService requestValidationService;

    /**
     * Genera la trama a partir de la información provista en el request.
     * Primero valida el request y luego delega la generación al ensamblador.
     *
     * @param request Datos necesarios para generar la trama.
     * @return La trama generada como cadena.
     * @throws IllegalArgumentException Si la validación del request falla.
     */
    public String generarTrama(GenerarTramaRequest request) {
        requestValidationService.validar(request);
        return tramaAssembler.generar(request);
    }
}
