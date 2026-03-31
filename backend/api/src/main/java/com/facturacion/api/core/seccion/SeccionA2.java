package com.facturacion.api.core.seccion;

import java.util.List;

public class SeccionA2 extends Seccion<Object> implements SeccionA2Constantes {
    @Override
    public List<Object> cargarCamposDesdeArchivo(String ruta) {
        throw new UnsupportedOperationException("Usa /api/v1/tramas/importar para cargar desde TXT");
    }
}
