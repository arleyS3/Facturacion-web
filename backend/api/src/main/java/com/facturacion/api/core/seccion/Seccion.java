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

    public void agregarCampoConValor(String nombre, String valor) {
        campos.add(new CampoSeccion(nombre, "", valor));
    }

    public void agregarCampoConValor(String nombre, String valor, String valor2) {
        campos.add(new CampoSeccion(nombre, valor, valor2));
    }

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

    public abstract List<T> cargarCamposDesdeArchivo(String ruta);
}
