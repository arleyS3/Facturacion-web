package com.facturacion.api.core.catalogo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TipoNotaCredito {
    ANULACION_OPERACION("01", "Anulación de la operación"),
    ANULACION_ERROR_RUC("02", "Anulación por error en el RUC"),
    CORRECCION_ERROR_DESCRIPCION("03", "Corrección por error en la descripción"),
    DESCUENTO_GLOBAL("04", "Descuento global"),
    DESCUENTO_ITEM("05", "Descuento por ítem"),
    DEVOLUCION_TOTAL("06", "Devolución total"),
    DEVOLUCION_ITEM("07", "Devolución por ítem"),
    BONIFICACION("08", "Bonificación"),
    DISMINUCION_VALOR("09", "Disminución en el valor"),
    OTROS_CONCEPTOS("10", "Otros Conceptos"),
    AJUSTES_EXPORTACION("11", "Ajustes de operaciones de exportación"),
    AJUSTES_IVAP("12", "Ajustes afectos al IVAP"),
    AJUSTES_MONTOS_FECHAS_PAGO("13", "Ajustes – montos y/o fechas de pago");

    private final String codigo;
    private final String descripcion;
}
