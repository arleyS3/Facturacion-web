package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.factura.FacturaUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.FacturaUblMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Estrategia UBL para factura electrónica.
 */
@Component
@RequiredArgsConstructor
public class FacturaUblStrategy implements UblDocumentoStrategy {

    private final FacturaUblMapper mapper;
    private final FacturaUblBuilder builder;

    /**
     * {@inheritDoc}
     */
    @Override
    public String codigoSunat() {
        return "01";
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
