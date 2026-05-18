package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.boleta.BoletaUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.boleta.BoletaUblMapper;
import com.facturacion.api.application.comprobante.ubl.signature.XmlSignatureService;

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
    private final XmlSignatureService signatureService;

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
        String xml = builder.construirXml(data);

        if (canonico.debeFirmar()) {
            String rucEmisor = canonico.emisorRuc();
            if (rucEmisor != null && !rucEmisor.isBlank()) {
                try {
                    xml = signatureService.signXml(xml, rucEmisor);
                } catch (Exception e) {
                    // TODO: handle exception
                    throw new RuntimeException("Error al firmar XML: " + e.getMessage(), e);
                }
            }
        }

        return xml;
    }
}
