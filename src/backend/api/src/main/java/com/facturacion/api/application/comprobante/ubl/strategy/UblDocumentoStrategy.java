package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.dto.GenerarXmlResult;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;

/**
 * Estrategia para generar XML UBL por tipo de documento.
 */
public interface UblDocumentoStrategy {

    /**
     * Retorna el código SUNAT del documento.
     *
     * @return código SUNAT
     */
    String codigoSunat();

    /**
     * Genera el XML UBL a partir del comprobante canónico, incluyendo
     * validación XSD post-construcción. La validación es informativa
     * y no bloquea la generación del XML.
     *
     * @param canonico comprobante canónico
     * @return resultado con XML generado y errores de validación
     * @throws Exception si ocurre un error en la generación
     */
    GenerarXmlResult generarXml(ComprobanteCanonico canonico) throws Exception;
}
