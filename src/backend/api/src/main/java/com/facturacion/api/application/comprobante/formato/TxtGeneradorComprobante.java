package com.facturacion.api.application.comprobante.formato;

import com.facturacion.api.application.TramaService;
import com.facturacion.api.application.comprobante.FormatoSalidaComprobante;
import com.facturacion.api.application.comprobante.ResultadoComprobante;
import com.facturacion.api.web.dto.GenerarTramaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Generador de comprobantes en formato de texto (trama).
 */
@Component
@RequiredArgsConstructor
public class TxtGeneradorComprobante implements GeneradorComprobante {

    private final TramaService tramaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public FormatoSalidaComprobante formato() {
        return FormatoSalidaComprobante.TXT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultadoComprobante generar(GenerarTramaRequest request) {
        String txt = tramaService.generarTrama(request);
        return ResultadoComprobante.txt(txt);
    }
}
