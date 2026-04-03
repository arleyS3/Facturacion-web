package com.facturacion.api.application;

import java.util.regex.Pattern;

/**
 * Utilidades de validación pequeñas y reutilizables usadas por los servicios de
 * validación de tramas.
 */
public final class ValidationUtils {
    private ValidationUtils() {
    }

    /**
     * Indica si un valor es requerido (no nulo ni vacío tras trim).
     */
    public static boolean requerido(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    /**
     * Valida que la cadena sea numérica y tenga la longitud exacta indicada.
     */
    public static boolean numerico(String valor, int longitudExacta) {
        if (valor == null) return false;
        return Pattern.matches("\\d{" + longitudExacta + "}", valor);
    }

    /** Valida formato ISO date YYYY-MM-DD. */
    public static boolean fechaIso(String valor) {
        return valor != null && valor.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    /** Valida formato de hora HH:MM:SS. */
    public static boolean hora(String valor) {
        return valor != null && valor.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]");
    }

    /** Valida RUC (empieza con 10 o 20 y 11 dígitos en total). */
    public static boolean ruc(String valor) {
        return valor != null && valor.matches("(10|20)\\d{9}");
    }

    /** Valida DNI (8 dígitos). */
    public static boolean dni(String valor) {
        return valor != null && valor.matches("\\d{8}");
    }

    /**
     * Valida la serie según el tipo de documento SUNAT.
     *
     * @param serie            Serie a validar.
     * @param tipoDocumentoSunat Código SUNAT del documento ("01","03",...)
     */
    public static boolean serieSegunTipo(String serie, String tipoDocumentoSunat) {
        if (serie == null || serie.isBlank()) return false;
        return switch (tipoDocumentoSunat) {
            case "01" -> serie.matches("F[0-9]{3}");
            case "03" -> serie.matches("B[0-9]{3}");
            case "07", "08" -> serie.matches("F[0-9]{3}|B[0-9]{3}");
            case "09" -> serie.matches("T[0-9]{3}");
            case "31" -> serie.matches("V[0-9]{3}");
            default -> false;
        };
    }

    /** Valida que el correlativo tenga 8 dígitos. */
    public static boolean correlativo8(String correlativo) {
        return correlativo != null && correlativo.matches("[0-9]{8}");
    }
}
