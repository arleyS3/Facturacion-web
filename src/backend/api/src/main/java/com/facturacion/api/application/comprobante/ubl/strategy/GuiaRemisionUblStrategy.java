package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.dto.GenerarXmlResult;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.guiaRemision.GuiaRemisionUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision.GuiaRemisionUblMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Estrategia UBL para guía de remisión electrónica.
 */
@Component
@RequiredArgsConstructor
public class GuiaRemisionUblStrategy implements UblDocumentoStrategy {

    private final GuiaRemisionUblMapper mapper;
    private final GuiaRemisionUblBuilder builder;

    /**
     * {@inheritDoc}
     */
    @Override
    public String codigoSunat() {
        return "09";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenerarXmlResult generarXml(ComprobanteCanonico canonico) throws Exception {
        var data = mapper.fromCanonico(canonico);
        if (data == null) {
            String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <DespatchAdvice xmlns="urn:oasis:names:specification:ubl:schema:xsd:DespatchAdvice-2">
                  <cbc:ID>%s</cbc:ID>
                </DespatchAdvice>
                """.formatted(canonico.numero());
            return new GenerarXmlResult(xml, java.util.List.of());
        }
        return new GenerarXmlResult(builder.construirXml(data), java.util.List.of());
    }
}
