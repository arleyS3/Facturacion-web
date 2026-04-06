package com.facturacion.api.application;

import java.util.Locale;

/**
 * Mapea nombres legibles de tipo de documento a su código SUNAT de 2 dígitos.
 */
public final class TipoDocumentoMapper {
    private TipoDocumentoMapper() {
    }

    /**
     * Convierte una representación legible ("Factura", "Boleta", etc.) o un código
     * de 2 dígitos a su código SUNAT correspondiente.
     *
     * @throws IllegalArgumentException si el tipo no es soportado o es nulo/vacío.
     */
    public static String toCodigoSunat(String tipoDocumento) {
        if (tipoDocumento == null || tipoDocumento.isBlank()) {
            throw new IllegalArgumentException("tipoDocumento es requerido");
        }
        String t = tipoDocumento.trim();

        // Si ya viene como código SUNAT
        if (t.matches("\\d{2}")) {
            return t;
        }

        String norm = normalize(t);
        return switch (norm) {
            case "factura" -> "01";
            case "boleta" -> "03";
            case "nota de credito", "nota de crédito" -> "07";
            case "nota de debito", "nota de débito" -> "08";
            case "guia de remision", "guía de remisión" -> "09";
            case "guia de remision transportista", "guía de remisión transportista" -> "31";
            default -> throw new IllegalArgumentException("tipoDocumento no soportado aún: " + tipoDocumento);
        };
    }

    public static String normalize(String tipoDocumento) {
        return tipoDocumento == null ? "" : tipoDocumento.trim().toLowerCase(Locale.ROOT);
    }
}
