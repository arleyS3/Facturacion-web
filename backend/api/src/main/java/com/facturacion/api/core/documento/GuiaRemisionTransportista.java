package com.facturacion.api.core.documento;

import com.facturacion.api.core.seccion.SeccionSimple;

/**
 * Guía de Remisión (Transportista). Reutiliza la serialización por secciones
 * para construir la trama TXT.
 */
public class GuiaRemisionTransportista extends DocumentoElectronico {
    public GuiaRemisionTransportista() {
        this.seccionA = new SeccionSimple();
        this.seccionG = new SeccionSimple();
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
