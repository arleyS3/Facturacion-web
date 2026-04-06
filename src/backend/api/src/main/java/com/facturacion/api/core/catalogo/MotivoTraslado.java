package com.facturacion.api.core.catalogo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MotivoTraslado {
    VENTA("01", "Venta"),
    COMPRA("02", "Compra"),
    VENTA_TERCEROS("03", "Venta con entrega a terceros"),
    TRASLADO_ESTABLECIMIENTOS("04", "Traslado entre establecimientos de la misma empresa"),
    CONSIGNACION("05", "Consignación"),
    DEVOLUCION("06", "Devolución"),
    RECOJO_BIENES_TRANSFORMADOS("07", "Recojo de bienes transformados"),
    IMPORTACION("08", "Importación"),
    EXPORTACION("09", "Exportación"),
    OTROS("13", "Otros"),
    VENTA_CONFIRMACION("14", "Venta sujeta a confirmación del comprador"),
    TRASLADO_TRANSFORMACION("17", "Traslado de bienes para transformación"),
    TRASLADO_EMISOR_ITINERANTE("18", "Traslado emisor itinerante CP");

    private final String codigo;
    private final String descripcion;
}
