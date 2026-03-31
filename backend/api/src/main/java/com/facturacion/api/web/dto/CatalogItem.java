package com.facturacion.api.web.dto;

public record CatalogItem(String code, String label, String extra) {
    public static CatalogItem of(String code, String label) {
        return new CatalogItem(code, label, null);
    }

    public static CatalogItem of(String code, String label, String extra) {
        return new CatalogItem(code, label, extra);
    }
}
