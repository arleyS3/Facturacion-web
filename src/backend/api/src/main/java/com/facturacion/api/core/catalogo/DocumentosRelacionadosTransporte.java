package com.facturacion.api.core.catalogo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DocumentosRelacionadosTransporte {
    
    FACTURA("01", "Factura"),
    BOLETA_VENTA("03", "Boleta de Venta"),
    LIQUIDACION_COMPRA("04", "Liquidación de Compra"),
    GUIA_REMISION_REMITENTE("09", "Guía de Remisión - Remitente"),
    TICKET_CINTA_MAQUINA_REGISTRADORA("12", "Ticket o cinta emitido por máquina registradora"),
    GUIA_REMISION_TRANSPORTISTA("31", "Guía de Remisión - Transportista"),
    COMPROBANTE_OPERACIONES_LEY_29972("48", "Comprobante de Operaciones - Ley N° 29972"),
    CONSTANCIA_DEPOSITO_IVAP_LEY_28211("49", "Constancia de Depósito - IVAP (Ley 28211)"),
    DECLARACION_ADUANERA_MERCANCIAS("50", "Declaración Aduanera de Mercancías"),
    DECLARACION_SIMPLIFICADA_DS("52", "Declaración Simplificada (DS)"),
    AUTORIZACION_CIRCULACION_MATPEL_CALLAO("65", "Autorización de Circulación para transportar MATPEL - Callao"),
    AUTORIZACION_CIRCULACION_CARGA_MERCANCIAS_LIMA_METROPOLITANA("66", "Autorización de Circulación para transporte de carga y mercancías en Lima Metropolitana"),
    PERMISO_OPERACION_ESPECIAL_MATPEL_MTC("67", "Permiso de Operación Especial para el servicio de transporte de MATPEL - MTC"),
    HABILITACION_SANITARIA_TRANSPORTE_TERRESTRE_PRODUCTOS_PESQUEROS_ACUICOLAS("68", "Habilitación Sanitaria de Transporte Terrestre de Productos Pesqueros y Acuícolas"),
    PERMISO_AUTORIZACION_OPERACION_TRANSPORTE_MERCANCIAS("69", "Permiso / Autorización de operación de transporte de mercancías"),
    RESOLUCION_ADJUDICACION_BIENES_SUNAT("71", "Resolución de Adjudicación de bienes - SUNAT"),
    RESOLUCION_COMISO_BIENES_SUNAT("72", "Resolución de Comiso de bienes - SUNAT"),
    GUIA_TRANSPORTE_FORESTAL_FAUNA_SERFOR("73", "Guía de Transporte Forestal o de Fauna - SERFOR"),
    GUIA_TRANSITO_SUCAMEC("74", "Guía de Tránsito - SUCAMEC"),
    AUTORIZACION_OPERACION_SANEAMIENTO_AMBIENTAL_MINSA("75", "Autorización para operar como empresa de Saneamiento Ambiental - MINSA -"),
    AUTORIZACION_MANEJO_RECOJO_RESIDUOS_SOLIDA_PELIGROASOS_NO_PELIGROASOS("76", "Autorización para manejo y recojo de residuos sólidos peligrosos y no peligrosos"),
    CERTIFICADO_FITOSANITARIO_MOVILIZACION_PLANTAS_PRODUCTOS_VEGETALES_ARTICULOS_REGLEMENTADOS("77", "Certificado fitosanitario la movilización de plantas, productos vegetales, y otros artículos reglamentados"),
    REGISTRO_UNICO_USUARIOS_TRANSPORTISTAS_ALCOHOL_ETILICO("78", "Registro Único de Usuarios y Transportistas de Alcohol Etílico"),
    CONSTANCIA_DEPOSITO_DETRACCION("80", "Constancia de Depósito - Detracción"),
    CODIGO_AUTORIZACION_SCOP("81", "Código de autorización emitida por el SCOP"),
    DECLARACION_JURADA_MUDANZA("82", "Declaración jurada de mudanza");

    private final String codigo;
    private final String descripcion;
    
    @Override
    public String toString() {
        return descripcion;
    }
}
