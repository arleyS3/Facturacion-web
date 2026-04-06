package com.facturacion.api.core.catalogo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TipoOperacion {
    VENTA_INTERNA("0101", "Venta interna"),
    VENTA_INTERNA_GASTOS_DEDUCIBLES_PN("0112", "Venta Interna - Sustenta Gastos Deducibles Persona Natural"),
    VENTA_INTERNA_NRUS("0113", "Venta Interna-NRUS"),
    EXPORTACION_BIENES("0200", "Exportación de Bienes"),
    EXPORTACION_SERVICIOS_INTEGRAMENTE_PAIS("0201", "Exportación de Servicios – Prestación servicios realizados íntegramente en el país"),
    EXPORTACION_SERVICIOS_HOSPEDAJE_NO_DOM("0202", "Exportación de Servicios – Prestación de servicios de hospedaje No Domiciliado"),
    EXPORTACION_SERVICIOS_TRANSPORTE_NAVIERAS("0203", "Exportación de Servicios – Transporte de navieras"),
    EXPORTACION_SERVICIOS_NAVES_AERONAVES("0204", "Exportación de Servicios – Servicios a naves y aeronaves de bandera extranjera"),
    EXPORTACION_SERVICIOS_PAQUETE_TURISTICO("0205", "Exportación de Servicios - Servicios que conformen un Paquete Turístico"),
    EXPORTACION_SERVICIOS_COMPLEMENTARIOS_CARGA("0206", "Exportación de Servicios – Servicios complementarios al transporte de carga"),
    EXPORTACION_SERVICIOS_ENERGIA_ZED("0207", "Exportación de Servicios – Suministro de energía eléctrica a favor de sujetos domiciliados en ZED"),
    EXPORTACION_SERVICIOS_PARCIAL_EXTRANJERO("0208", "Exportación de Servicios – Prestación servicios realizados parcialmente en el extranjero"),
    CARTA_PORTE_AEREO_NACIONAL("0301", "Operaciones con Carta de porte aéreo (emitidas en el ámbito nacional)"),
    TRANSPORTE_FERROVIARIO_PASAJEROS("0302", "Operaciones de Transporte ferroviario de pasajeros"),
    VENTAS_NO_DOM_NO_EXPORTACION("0401", "Ventas no domiciliados que no califican como exportación"),
    SUJETA_DETRACCION("1001", "Operación Sujeta a Detracción"),
    SUJETA_DETRACCION_HIDROBIOLOGICOS("1002", "Operación Sujeta a Detracción- Recursos Hidrobiológicos"),
    SUJETA_DETRACCION_TRANSPORTE_PASAJEROS("1003", "Operación Sujeta a Detracción- Servicios de Transporte Pasajeros"),
    SUJETA_DETRACCION_TRANSPORTE_CARGA("1004", "Operación Sujeta a Detracción- Servicios de Transporte Carga"),
    SUJETA_PERCEPCION("2001", "Operación Sujeta a Percepción"),
    CREDITOS_EMPRESAS("2100", "Créditos a empresas"),
    CREDITOS_CONSUMO_REVOLVENTE("2101", "Créditos de consumo revolvente"),
    CREDITOS_CONSUMO_NO_REVOLVENTE("2102", "Créditos de consumo no revolvente"),
    OTRAS_NO_GRAVADAS_FINANCIERO_SEGUROS("2103", "Otras operaciones no gravadas - Empresas del sistema financiero y cooperativas de ahorro y crédito no autorizadas a captar recursos del público"),
    OTRAS_NO_GRAVADAS_SEGUROS("2104", "Otras operaciones no gravadas - Empresas del sistema de seguros");

    private final String codigo;
    private final String descripcion;

}
