package com.facturacion.api.application.comprobante.ubl.mapper;

import java.util.List;
import java.util.Map;

/**
 * Representación canónica del comprobante extraída del request de secciones.
 * Agrupa los campos de las secciones A, B y listas B para uso en los mappers UBL.
 */
public record ComprobanteCanonico(
        String tipoDocumento,
        Map<String, String> camposA,
        Map<String, String> camposA2,
        List<Map<String, String>> lineasB
) {
}
