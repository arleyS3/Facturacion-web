package com.facturacion.api.application.comprobante;

import java.util.List;

/**
 * Resultado de la generación de un comprobante.
 *
 * @param formato formato de salida
 * @param contenido contenido del comprobante
 * @param nombreArchivo nombre sugerido de archivo
 * @param warnings advertencias no bloqueantes
 */
public record ResultadoComprobante(
        FormatoSalidaComprobante formato,
        String contenido,
        String nombreArchivo,
        List<String> warnings
) {

    /**
     * Crea un resultado en formato texto.
     *
     * @param contenido contenido generado
     * @return resultado con formato TXT
     */
    public static ResultadoComprobante txt(String contenido) {
        return new ResultadoComprobante(FormatoSalidaComprobante.TXT, contenido, null, List.of());
    }

    /**
     * Crea un resultado en formato XML.
     *
     * @param contenido contenido generado
     * @param nombreArchivo nombre sugerido
     * @return resultado con formato XML
     */
    public static ResultadoComprobante xml(String contenido, String nombreArchivo) {
        return new ResultadoComprobante(FormatoSalidaComprobante.XML, contenido, nombreArchivo, List.of());
    }
}
