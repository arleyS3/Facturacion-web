package com.facturacion.api.core.catalogo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TipoAfectacionIGV {
    GRAVADO_OPERACION_ONEROSA("10", "Gravado - Operación Onerosa", "1000"),
    GRAVADO_RETIRO_PREMIO("11", "Gravado – Retiro por premio", "9996"),
    GRAVADO_RETIRO_DONACION("12", "Gravado – Retiro por donación", "9996"),
    GRAVADO_RETIRO("13", "Gravado – Retiro", "9996"),
    GRAVADO_RETIRO_PUBLICIDAD("14", "Gravado – Retiro por publicidad", "9996"),
    GRAVADO_BONIFICACIONES("15", "Gravado – Bonificaciones", "9996"),
    GRAVADO_RETIRO_ENTREGA_TRABAJADORES("16", "Gravado – Retiro por entrega a trabajadores", "9996"),
    GRAVADO_IVAP("17", "Gravado - IVAP", "1016 o 9996"),
    EXONERADO_OPERACION_ONEROSA("20", "Exonerado - Operación Onerosa", "9997"),
    EXONERADO_TRANSFERENCIA_GRATUITA("21", "Exonerado - Transferencia gratuita", "9996"),
    INAFECTO_OPERACION_ONEROSA("30", "Inafecto - Operación Onerosa", "9998"),
    INAFECTO_RETIRO_BONIFICACION("31", "Inafecto – Retiro por Bonificación", "9996"),
    INAFECTO_RETIRO("32", "Inafecto – Retiro", "9996"),
    INAFECTO_RETIRO_MUESTRAS_MEDICAS("33", "Inafecto – Retiro por Muestras Médicas", "9996"),
    INAFECTO_RETIRO_CONVENIO_COLECTIVO("34", "Inafecto - Retiro por Convenio Colectivo", "9996"),
    INAFECTO_RETIRO_PREMIO("35", "Inafecto – Retiro por premio", "9996"),
    INAFECTO_RETIRO_PUBLICIDAD("36", "Inafecto - Retiro por publicidad", "9996"),
    INAFECTO_TRANSFERENCIA_GRATUITA("37", "Inafecto - Transferencia gratuita", "9996"),
    EXPORTACION_BIENES_SERVICIOS("40", "Exportación de Bienes o Servicios", "9995 o 9996");

    private final String codigo;
    private final String descripcion;
    private final String codigoTributario;
}
