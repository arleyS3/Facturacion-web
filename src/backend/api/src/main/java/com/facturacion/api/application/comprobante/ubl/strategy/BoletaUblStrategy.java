package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.boleta.BoletaUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.boleta.BoletaUblMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Estrategia UBL para boleta electrónica.
 */
@Component
@RequiredArgsConstructor
public class BoletaUblStrategy implements UblDocumentoStrategy {

    private final BoletaUblMapper mapper;
    private final BoletaUblBuilder builder;

    /**
     * {@inheritDoc}
     */
    @Override
    public String codigoSunat() {
        return "03";
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
