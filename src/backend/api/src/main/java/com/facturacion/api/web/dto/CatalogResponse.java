package com.facturacion.api.web.dto;

public record CatalogResponse(
        Long id,
        String codigo,
        String descripcion,
        Boolean activo,
        String codigoTributario
) {}
