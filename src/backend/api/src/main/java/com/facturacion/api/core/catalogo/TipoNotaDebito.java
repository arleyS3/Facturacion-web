package com.facturacion.api.core.catalogo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TipoNotaDebito {
    INTERESES_MORA("01", "Intereses por mora"),
    AUMENTO_VALOR("02", "Aumento en el valor"),
    PENALIDADES_OTROS("03", "Penalidades/ otros conceptos"),
    AJUSTES_EXPORTACION("11", "Ajustes de operaciones de exportación"),
    AJUSTES_IVAP("12", "Ajustes afectos al IVAP");

    private final String codigo;
    private final String descripcion;

}
