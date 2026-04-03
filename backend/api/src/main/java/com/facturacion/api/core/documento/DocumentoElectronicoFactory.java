package com.facturacion.api.core.documento;

/**
 * Factory simple para crear instancias de {@link DocumentoElectronico} según
 * el código SUNAT de tipo de documento.
 */
public final class DocumentoElectronicoFactory {
    private DocumentoElectronicoFactory() {
    }

    public static DocumentoElectronico crearDocumentoElectronico(String codigoTipoDoc) {
        return switch (codigoTipoDoc) {
            case "01" -> new Factura();
            case "03" -> new Boleta();
            case "07" -> new NotaDeCredito();
            case "08" -> new NotaDeDebito();
            case "09" -> new GuiaRemision();
            case "31" -> new GuiaRemisionTransportista();
            default -> throw new IllegalArgumentException("Tipo de documento no soportado: " + codigoTipoDoc);
        };
    }
}
