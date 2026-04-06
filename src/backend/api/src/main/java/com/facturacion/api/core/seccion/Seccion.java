package com.facturacion.api.core.seccion;

import com.facturacion.api.core.campo.CampoSeccion;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Seccion<T> {
    protected List<CampoSeccion> campos;

    protected Seccion() {
        this.campos = new ArrayList<>();
    }

    /** Agrega un campo con un único valor (valor2 vacío). */
    public void agregarCampoConValor(String nombre, String valor) {
        campos.add(new CampoSeccion(nombre, "", valor));
    }

    /** Agrega un campo proporcionando valor y valor2 (útil para listas con índice). */
    public void agregarCampoConValor(String nombre, String valor, String valor2) {
        campos.add(new CampoSeccion(nombre, valor, valor2));
    }

    /**
     * Construye la representación TXT de una sección usando el prefijo indicado.
     *
     * @param campos Lista de campos a serializar.
     * @param prefijo Prefijo de sección (ej. "A", "B", "G1").
     */
    public StringBuilder getSeccion(List<CampoSeccion> campos, String prefijo) {
        StringBuilder sb = new StringBuilder();
        for (CampoSeccion campo : campos) {
            sb.append(prefijo).append(';')
                    .append(campo.getNombre()).append(';')
                    .append(campo.getValor()).append(';')
                    .append(campo.getValor2()).append('\n');
        }
        return sb;
    }

    /**
     * Carga campos desde un recurso externo (p. ej. archivo) para inicializar la sección.
     * Implementaciones concretas pueden usar esta firma para parseo de plantillas.
     */
    public abstract List<T> cargarCamposDesdeArchivo(String ruta);
}
