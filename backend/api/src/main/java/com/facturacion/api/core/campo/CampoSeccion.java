package com.facturacion.api.core.campo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CampoSeccion {
    private String nombre;
    private String valor;
    private String valor2;


    public CampoSeccion(String nombre, String valor, String valor2) {
        this.nombre = nombre;
        this.valor = valor;
        this.valor2 = valor2;
    }

}
