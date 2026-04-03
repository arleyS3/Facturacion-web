package com.facturacion.api.core.seccion;

import java.util.List;

/** Sección B: detalle de items del documento. */
public class SeccionB extends Seccion<Object> {
    @Override
    public List<Object> cargarCamposDesdeArchivo(String ruta) {
        throw new UnsupportedOperationException("Usa /api/v1/tramas/importar para cargar desde TXT");
    }
}
