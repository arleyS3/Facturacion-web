package com.facturacion.api.application;

import java.util.regex.Pattern;

public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static boolean requerido(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    public static boolean numerico(String valor, int longitudExacta) {
        if (valor == null) return false;
        return Pattern.matches("\\d{" + longitudExacta + "}", valor);
    }

    public static boolean fechaIso(String valor) {
        return valor != null && valor.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    public static boolean hora(String valor) {
        return valor != null && valor.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]");
    }

    public static boolean ruc(String valor) {
        return valor != null && valor.matches("(10|20)\\d{9}");
    }

    public static boolean dni(String valor) {
        return valor != null && valor.matches("\\d{8}");
    }

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

    public static boolean correlativo8(String correlativo) {
        return correlativo != null && correlativo.matches("[0-9]{8}");
    }
}
