
package com.facturacion.api.core.catalogo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CodigoTipTributo {
    IGV("1000", "IGV"),
    ISC("2000", "ISC"),
    OTROS("9996", "Otros");

    private final String codigo;
    private final String descripcion;

}
