package com.facturacion.api.core.documento;

import com.facturacion.api.core.seccion.Seccion;

import lombok.Getter;

@Getter
/**
 * Representa un documento electrónico genérico compuesto por múltiples secciones.
 *
 * <p>Las implementaciones concretas (Factura, Boleta, NC, ND, Guía...) contienen
 * instancias de {@link com.facturacion.api.core.seccion.Seccion} para cada parte
 * de la trama. El método {@link #generaTramaTXT()} debe devolver la representación
 * textual (TXT) concatenada del documento.</p>
 */
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

    /**
     * Genera la trama TXT correspondiente al documento.
     *
     * @return StringBuilder con la trama lista para escritura.
     */
    public abstract StringBuilder generaTramaTXT();
}
