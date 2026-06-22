package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.dto.GenerarXmlResult;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.notaCredito.NotaCreditoUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.NotaCreditoUblMapper;
import com.facturacion.api.application.comprobante.validation.ValidacionService;
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
    private final ValidacionService validacionService;

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
    public GenerarXmlResult generarXml(ComprobanteCanonico canonico) throws Exception {
        // 1. Mapear datos canónicos a datos UBL
        var data = mapper.fromCanonico(canonico);

        // 2. Construir objeto CreditNoteType UBL
        var creditNote = builder.buildCreditNote(data);

        // 3. Validar contra esquema XSD (no bloqueante)
        var validationErrors = validacionService.validar(creditNote);

        // 4. Serializar a XML
        String xml = builder.serializar(creditNote);

        return new GenerarXmlResult(xml, validationErrors);
    }
}
