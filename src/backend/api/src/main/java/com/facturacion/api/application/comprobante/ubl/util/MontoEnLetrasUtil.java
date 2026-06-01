package com.facturacion.api.application.comprobante.ubl.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidad para convertir montos numéricos a su representación en letras,
 * en el formato requerido por SUNAT para comprobantes electrónicos.
 *
 * <p>
 * Formato de salida: {@code SON [entero] CON [decimales/100] [moneda]}
 * </p>
 *
 * <p>
 * Ejemplos:
 * </p>
 * 
 * <pre>
 * MontoEnLetrasUtil.convertir(new BigDecimal("1234.56"), "PEN")
 * // → "SON MIL DOSCIENTOS TREINTA Y CUATRO CON 56/100 SOLES"
 *
 * MontoEnLetrasUtil.convertir(new BigDecimal("0.50"), "PEN")
 * // → "SON 50/100 SOLES"
 *
 * MontoEnLetrasUtil.convertir(new BigDecimal("100.00"), "PEN")
 * // → "SON CIEN CON 00/100 SOLES"
 * </pre>
 */
public final class MontoEnLetrasUtil {

    private MontoEnLetrasUtil() {
        // Utility class, no instanciable
    }

    private static final String[] UNIDADES = {
            "CERO", "UN", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE"
    };

    private static final String[] DIEZ_A_QUINCE = {
            "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE"
    };

    private static final String[] DECENAS = {
            "", "", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA",
            "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"
    };

    private static final String[] CENTENAS = {
            "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
            "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"
    };

    private static final String CIEN = "CIEN";
    private static final String MIL = "MIL";
    private static final String MILLON = "MILLON";
    private static final String MILLONES = "MILLONES";
    private static final String SON = "SON";
    private static final String CON = "CON";

    /**
     * Convierte un monto a su representación en letras para comprobantes
     * electrónicos SUNAT.
     *
     * <p>
     * Formato: {@code SON [entero en palabras] CON [dd/100] [moneda]}
     * </p>
     *
     * @param monto  monto a convertir (no nulo, escala 2)
     * @param moneda código de moneda (PEN, USD, etc.)
     * @return monto en letras en formato SUNAT
     */
    public static String convertir(BigDecimal monto, String moneda) {
        if (monto == null) {
            throw new IllegalArgumentException("El monto no puede ser nulo");
        }

        // Redondear a 2 decimales
        BigDecimal montoNormalizado = monto.setScale(2, RoundingMode.HALF_UP);

        long parteEntera = montoNormalizado.longValue();
        int parteDecimal = montoNormalizado
                .remainder(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        String textoMoneda = obtenerTextoMoneda(moneda);

        if (parteEntera == 0 && parteDecimal == 0) {
            return SON + " CERO CON 00/100 " + textoMoneda;
        }

        StringBuilder sb = new StringBuilder(SON);

        if (parteEntera > 0) {
            sb.append(' ').append(convertirEntero(parteEntera));
        }

        sb.append(" ").append(CON).append(" ")
                .append(String.format("%02d", parteDecimal))
                .append("/100 ").append(textoMoneda);

        return sb.toString();
    }

    /**
     * Convierte un número entero positivo a palabras en español (mayúsculas).
     * Maneja desde 1 hasta 999,999,999.
     */
    private static String convertirEntero(long n) {
        if (n == 0) {
            return "CERO";
        }
        if (n < 0) {
            throw new IllegalArgumentException("No se soportan números negativos: " + n);
        }

        StringBuilder resultado = new StringBuilder();

        // Millones
        long millones = n / 1_000_000;
        if (millones > 0) {
            if (millones == 1) {
                resultado.append(MILLON);
            } else {
                resultado.append(convertirGrupo((int) millones)).append(' ').append(MILLONES);
            }
            n %= 1_000_000;
        }

        // Miles
        long miles = n / 1_000;
        if (miles > 0) {
            if (resultado.length() > 0) {
                resultado.append(' ');
            }
            if (miles == 1) {
                resultado.append(MIL);
            } else {
                resultado.append(convertirGrupo((int) miles)).append(' ').append(MIL);
            }
            n %= 1_000;
        }

        // Resto (1-999)
        if (n > 0) {
            if (resultado.length() > 0) {
                resultado.append(' ');
            }
            resultado.append(convertirGrupo((int) n));
        }

        return resultado.toString();
    }

    /**
     * Convierte un número de 1 a 999 a palabras en español.
     */
    private static String convertirGrupo(int n) {
        int centenas = n / 100;
        int resto = n % 100;

        if (centenas == 0) {
            return convertirDecenas(resto);
        }

        StringBuilder sb = new StringBuilder();
        if (centenas == 1) {
            if (resto == 0) {
                sb.append(CIEN);
            } else {
                sb.append("CIENTO");
            }
        } else {
            sb.append(CENTENAS[centenas]);
        }

        if (resto > 0) {
            if (centenas == 1 && resto > 0) {
                sb.append(' ');
            } else {
                sb.append(' ');
            }
            sb.append(convertirDecenas(resto));
        }

        return sb.toString();
    }

    /**
     * Convierte un número de 1 a 99 a palabras en español.
     */
    private static String convertirDecenas(int n) {
        if (n == 0) {
            return "";
        }
        if (n < 10) {
            return UNIDADES[n];
        }

        if (n <= 15) {
            if (n == 10) return DIEZ_A_QUINCE[0];
            return DIEZ_A_QUINCE[n - 10];
        }

        if (n <= 19) {
            return "DIECI" + UNIDADES[n - 10].toLowerCase();
        }

        if (n < 30) {
            if (n == 20) {
                return DECENAS[2]; // VEINTE
            }
            String unidad = UNIDADES[n - 20].toLowerCase();
            return "VEINTI" + unidad;
        }

        int decena = n / 10;
        int unidad = n % 10;

        if (unidad == 0) {
            return DECENAS[decena];
        }

        return DECENAS[decena] + " Y " + (unidad == 1 ? "UN" : UNIDADES[unidad].toLowerCase());
    }

    /**
     * Obtiene el texto de la moneda en plural para el formato SUNAT.
     */
    private static String obtenerTextoMoneda(String moneda) {
        if (moneda == null) {
            return "";
        }
        return switch (moneda.toUpperCase()) {
            case "PEN" -> "SOLES";
            case "USD" -> "DOLARES AMERICANOS";
            case "EUR" -> "EUROS";
            default -> moneda.toUpperCase();
        };
    }
}
