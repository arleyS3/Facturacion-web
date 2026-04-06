package com.facturacion.api.web.dto;

/**
 * Representa un elemento de catálogo con código, etiqueta y un campo extra opcional.
 * Usado por endpoints que exponen listas de valores (catálogos).
 *
 * @param code código del elemento
 * @param label etiqueta legible para UI
 * @param extra campo adicional opcional (puede contener información contextual)
 */
public record CatalogItem(String code, String label, String extra) {
    public static CatalogItem of(String code, String label) {
        return new CatalogItem(code, label, null);
    }

    public static CatalogItem of(String code, String label, String extra) {
        return new CatalogItem(code, label, extra);
    }
}
