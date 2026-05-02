package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.notaCredito.NotaCreditoUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.NotaCreditoUblMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Estrategia UBL para nota de crédito electrónica.
 */
@Component
@RequiredArgsConstructor
public class NotaCreditoUblStrategy implements UblDocumentoStrategy {

    private final NotaCreditoUblMapper mapper;
    private final NotaCreditoUblBuilder builder;

    /**
     * {@inheritDoc}
     */
    @Override
    public String codigoSunat() {
        return "07";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generarXml(ComprobanteCanonico canonico) throws Exception {
        var data = mapper.fromCanonico(canonico);
        return builder.construirXml(data);
    }
}
