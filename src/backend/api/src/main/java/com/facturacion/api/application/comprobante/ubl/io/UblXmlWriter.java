package com.facturacion.api.application.comprobante.ubl.io;

import org.springframework.stereotype.Component;

/**
 * Utilitario para normalizar y escapar XML UBL.
 */
@Component
public class UblXmlWriter {

    /**
     * Normaliza un XML crudo para persistencia o validación.
     *
     * @param xmlRaw xml sin normalizar
     * @return xml normalizado
     */
    public String normalize(String xmlRaw) {
        return xmlRaw == null ? "" : xmlRaw.trim();
    }

    /**
     * Escapa caracteres XML especiales.
     *
     * @param value valor original
     * @return texto escapado
     */
    public String escape(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
