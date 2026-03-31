package com.facturacion.api.core.documento;

import com.facturacion.api.core.seccion.SeccionA;
import com.facturacion.api.core.seccion.SeccionA2;
import com.facturacion.api.core.seccion.SeccionA5;
import com.facturacion.api.core.seccion.SeccionB;
import com.facturacion.api.core.seccion.SeccionE;
import com.facturacion.api.core.seccion.SeccionM;

public class Factura extends DocumentoElectronico {
    public Factura() {
        this.seccionA = new SeccionA();
        this.seccionB = new SeccionB();
        this.seccionA2 = new SeccionA2();
        this.seccionA5 = new SeccionA5();
        this.seccionE = new SeccionE();
        this.seccionM = new SeccionM();
    }

    @Override
    public StringBuilder generaTramaTXT() {
        StringBuilder trama = new StringBuilder();
        trama.append(seccionA.getSeccion(seccionA.getCampos(), "A"));
        trama.append(seccionA5.getSeccion(seccionA5.getCampos(), "A5"));
        trama.append(seccionB.getSeccion(seccionB.getCampos(), "B"));
        trama.append(seccionE.getSeccion(seccionE.getCampos(), "E"));
        trama.append(seccionM.getSeccion(seccionM.getCampos(), "M"));
        trama.append(seccionA2.getSeccion(seccionA2.getCampos(), "A2"));
        return trama;
    }
}
