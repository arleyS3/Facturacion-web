package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.notaDebito.NotaDebitoUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.notaDebito.NotaDebitoUblMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Estrategia UBL para nota de débito electrónica.
 */
@Component
@RequiredArgsConstructor
public class NotaDebitoUblStrategy implements UblDocumentoStrategy {

    private final NotaDebitoUblMapper mapper;
    private final NotaDebitoUblBuilder builder;

    /**
     * {@inheritDoc}
     */
    @Override
    public String codigoSunat() {
        return "08";
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
