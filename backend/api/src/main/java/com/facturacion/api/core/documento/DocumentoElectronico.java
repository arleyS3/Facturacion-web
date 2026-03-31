package com.facturacion.api.core.documento;

import com.facturacion.api.core.seccion.Seccion;

import lombok.Getter;

@Getter
public abstract class DocumentoElectronico {
    protected Seccion<?> seccionA;
    protected Seccion<?> seccionA2;
    protected Seccion<?> seccionB;
    protected Seccion<?> seccionB2;

    // Secciones adicionales usadas en Factura/Boleta según TXT
    protected Seccion<?> seccionA5;
    protected Seccion<?> seccionM;

    protected Seccion<?> seccionG;
    protected Seccion<?> seccionG1;
    protected Seccion<?> seccionG11;
    protected Seccion<?> seccionG4;
    protected Seccion<?> seccionD;
    protected Seccion<?> seccionG3;
    protected Seccion<?> seccionG2;
    protected Seccion<?> seccionG5;
    protected Seccion<?> seccionE;

    public abstract StringBuilder generaTramaTXT();
}
