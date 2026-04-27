package com.facturacion.api.application.comprobante.ubl.strategy;

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
     * Genera el XML UBL a partir del comprobante canónico.
     *
     * @param canonico comprobante canónico
     * @return XML generado
     * @throws Exception si ocurre un error en la generación
     */
    String generarXml(ComprobanteCanonico canonico) throws Exception;
}
