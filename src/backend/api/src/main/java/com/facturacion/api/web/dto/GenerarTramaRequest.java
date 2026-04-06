package com.facturacion.api.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerarTramaRequest(
        @NotBlank String tipoDocumento,
        @NotNull @Valid SeccionesPayload secciones
) {
}
