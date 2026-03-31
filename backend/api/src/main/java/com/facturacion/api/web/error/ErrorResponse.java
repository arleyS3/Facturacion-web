package com.facturacion.api.web.error;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        String message,
        List<String> details,
        OffsetDateTime timestamp
) {
    public static ErrorResponse of(String message, List<String> details) {
        return new ErrorResponse(message, details, OffsetDateTime.now());
    }
}
