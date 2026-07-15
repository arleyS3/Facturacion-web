package com.facturacion.api.application.ose;

import java.util.List;

public record OseComponentesSucursalResponse(
        String codigo,
        String mensaje,
        List<String> listaErrores,
        List<String> listaDuplicados
) {}
