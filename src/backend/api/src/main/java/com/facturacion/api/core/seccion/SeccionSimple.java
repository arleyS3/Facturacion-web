package com.facturacion.api.core.seccion;

import java.util.List;


/**
 * Implementación simple de {@link Seccion} que no modela campos estructurados.
 * Usada para secciones donde sólo se requieren pares nombre/valor sin estructura adicional.
 */
public class SeccionSimple extends Seccion<Object> {
    @Override
    public List<Object> cargarCamposDesdeArchivo(String ruta) {
        throw new UnsupportedOperationException("Importación desde archivo no implementada en API aún");
    }
}
