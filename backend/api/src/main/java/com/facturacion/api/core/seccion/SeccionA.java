package com.facturacion.api.core.seccion;

import java.util.List;


/**
 * Sección A (cabecera) del documento. Contiene campos generales como Serie, Correlativo,
 * Fecha, Emisor y Receptor.
 */
public class SeccionA extends Seccion<Object> {
    @Override
    public List<Object> cargarCamposDesdeArchivo(String ruta) {
        throw new UnsupportedOperationException("Usa /api/v1/tramas/importar para cargar desde TXT");
    }
}
