package com.facturacion.api.core.documento;

import com.facturacion.api.core.seccion.SeccionSimple;

/**
 * Representa una Guía de Remisión. Las secciones se implementan como {@link com.facturacion.api.core.seccion.SeccionSimple}
 * para permitir una representación flexible de campos sin estructura compleja.
 */
public class GuiaRemision extends DocumentoElectronico {
    public GuiaRemision() {
        this.seccionA = new SeccionSimple();
        this.seccionB2 = new SeccionSimple();
        this.seccionG = new SeccionSimple();
        this.seccionG1 = new SeccionSimple();
        this.seccionG11 = new SeccionSimple();
        this.seccionG4 = new SeccionSimple();
        this.seccionD = new SeccionSimple();
        this.seccionG3 = new SeccionSimple();
        this.seccionG2 = new SeccionSimple();
        this.seccionG5 = new SeccionSimple();
        this.seccionE = new SeccionSimple();
        this.seccionB = new SeccionSimple();
    }

    @Override
    public StringBuilder generaTramaTXT() {
        StringBuilder trama = new StringBuilder();
        // Orden de alineado
        trama.append(seccionA.getSeccion(seccionA.getCampos(), "A"));
        trama.append(seccionB.getSeccion(seccionB.getCampos(), "B"));
        // B2 se imprime con prefijo "B" en el desktop
        trama.append(seccionB2.getSeccion(seccionB2.getCampos(), "B"));
        trama.append(seccionG.getSeccion(seccionG.getCampos(), "G"));
        trama.append(seccionG1.getSeccion(seccionG1.getCampos(), "G1"));
        trama.append(seccionG11.getSeccion(seccionG11.getCampos(), "G11"));
        trama.append(seccionG4.getSeccion(seccionG4.getCampos(), "G4"));
        trama.append(seccionD.getSeccion(seccionD.getCampos(), "D"));
        trama.append(seccionG3.getSeccion(seccionG3.getCampos(), "G3"));
        trama.append(seccionG2.getSeccion(seccionG2.getCampos(), "G2"));
        trama.append(seccionG5.getSeccion(seccionG5.getCampos(), "G5"));
        trama.append(seccionE.getSeccion(seccionE.getCampos(), "E"));
        return trama;
    }
}
