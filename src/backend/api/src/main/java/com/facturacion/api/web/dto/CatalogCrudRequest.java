package com.facturacion.api.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CatalogCrudRequest {
    @NotBlank
    @Size(max = 10)
    private String codigo;

    @NotBlank
    @Size(max = 250)
    private String descripcion;

    @Size(max = 2)
    private String codigoTributario;
}
