package com.facturacion.api.core.seccion;

import java.util.List;


public class SeccionSimple extends Seccion<Object> {
    @Override
    public List<Object> cargarCamposDesdeArchivo(String ruta) {
        throw new UnsupportedOperationException("Importación desde archivo no implementada en API aún");
    }
}
