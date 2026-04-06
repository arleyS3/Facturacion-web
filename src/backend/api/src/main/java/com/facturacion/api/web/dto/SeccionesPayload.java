package com.facturacion.api.web.dto;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public record SeccionesPayload(
        @Valid Map<String, Map<String, String>> campos,
        @Valid Map<String, List<Map<String, String>>> listas
) {
}
