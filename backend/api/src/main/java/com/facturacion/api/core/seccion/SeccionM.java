package com.facturacion.api.core.seccion;

import java.util.List;


/** Sección M: datos de envío por email (Mails). */
public class SeccionM extends Seccion<Object> {
    @Override
    public List<Object> cargarCamposDesdeArchivo(String ruta) {
        throw new UnsupportedOperationException("Usa /api/v1/tramas/importar para cargar desde TXT");
    }
}
