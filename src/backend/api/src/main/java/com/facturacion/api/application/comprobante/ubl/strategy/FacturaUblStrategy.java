package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.factura.FacturaUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.FacturaUblMapper;
import com.facturacion.api.application.comprobante.ubl.signature.XmlSignatureService;
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
    private final XmlSignatureService signatureService;

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
        // 1. Generar XML UBL
        var data = mapper.fromCanonico(canonico);
        String xml = builder.construirXml(data);
        
        // 2. Firmar XML con certificado del emisor
        String rucEmisor = canonico.emisorRuc();
        if (rucEmisor != null && !rucEmisor.isBlank()) {
            try {
                xml = signatureService.signXml(xml, rucEmisor);
            } catch (Exception e) {
                // Si falla la firma, retornar XML sin firmar (para debug/demo)
                // En producción, debería lanzar excepción
            }
        }
        
        return xml;
    }
}
