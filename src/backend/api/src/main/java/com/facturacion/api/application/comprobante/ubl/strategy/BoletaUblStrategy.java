package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.dto.GenerarXmlResult;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.boleta.BoletaUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.boleta.BoletaUblMapper;
import com.facturacion.api.application.comprobante.validation.ValidacionService;

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
    private final ValidacionService validacionService;

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
    public GenerarXmlResult generarXml(ComprobanteCanonico canonico) throws Exception {
        // 1. Mapear datos canónicos a datos UBL
        var data = mapper.fromCanonico(canonico);

        // 2. Construir objeto InvoiceType UBL
        var invoiceType = builder.buildInvoice(data);

        // 3. Validar contra esquema XSD (no bloqueante)
        var validationErrors = validacionService.validar(invoiceType);

        // 4. Serializar a XML (requiere data para post-procesamiento de leyendas)
        String xml = builder.serializar(invoiceType, data);

        return new GenerarXmlResult(xml, validationErrors);
    }
}
