package com.facturacion.api.core.seccion;

import java.util.List;


public class SeccionA extends Seccion<Object> {
    @Override
    public List<Object> cargarCamposDesdeArchivo(String ruta) {
        throw new UnsupportedOperationException("Usa /api/v1/tramas/importar para cargar desde TXT");
    }
}
