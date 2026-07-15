package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.dto.GenerarXmlResult;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.notaDebito.NotaDebitoUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.notaDebito.NotaDebitoUblMapper;
import com.facturacion.api.application.comprobante.validation.ValidacionService;
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
    private final ValidacionService validacionService;

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
    public GenerarXmlResult generarXml(ComprobanteCanonico canonico) throws Exception {
        // 1. Mapear datos canónicos a datos UBL
        var data = mapper.fromCanonico(canonico);

        // 2. Construir objeto DebitNoteType UBL
        var debitNote = builder.buildDebitNote(data);

        // 3. Validar contra esquema XSD (no bloqueante)
        var validationErrors = validacionService.validar(debitNote);

        // 4. Serializar a XML
        String xml = builder.serializar(debitNote);

        return new GenerarXmlResult(xml, validationErrors);
    }
}
