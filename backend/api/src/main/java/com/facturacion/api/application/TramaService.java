package com.facturacion.api.application;

import com.facturacion.api.web.dto.GenerarTramaRequest;
import org.springframework.stereotype.Service;

@Service
public class TramaService {

    private final TramaAssembler tramaAssembler;
    private final RequestValidationService requestValidationService;

    public TramaService(TramaAssembler tramaAssembler, RequestValidationService requestValidationService) {
        this.tramaAssembler = tramaAssembler;
        this.requestValidationService = requestValidationService;
    }

    public String generarTrama(GenerarTramaRequest request) {
        requestValidationService.validar(request);
        return tramaAssembler.generar(request);
    }
}
