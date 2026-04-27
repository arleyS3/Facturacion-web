package com.facturacion.api.application.comprobante.formato;

import com.facturacion.api.application.comprobante.FormatoSalidaComprobante;
import com.facturacion.api.application.comprobante.ResultadoComprobante;
import com.facturacion.api.web.dto.GenerarTramaRequest;

/**
 * Contrato para generadores de comprobantes por formato.
 */
public interface GeneradorComprobante {

    /**
     * Retorna el formato que soporta el generador.
     *
     * @return formato de salida
     */
    FormatoSalidaComprobante formato();

    /**
     * Genera el comprobante desde la solicitud.
     *
     * @param request datos de la solicitud
     * @return resultado generado
     * @throws Exception si ocurre un error en la generación
     */
    ResultadoComprobante generar(GenerarTramaRequest request) throws Exception;
}
