package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.dto.GenerarXmlResult;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.factura.FacturaUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.FacturaUblMapper;
import com.facturacion.api.application.comprobante.validation.ValidacionService;
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
    private final ValidacionService validacionService;

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
    public GenerarXmlResult generarXml(ComprobanteCanonico canonico) throws Exception {
        // 1. Mapear datos canónicos a datos UBL
        var data = mapper.fromCanonico(canonico);

        // 2. Construir objeto InvoiceType UBL
        var invoiceType = builder.buildInvoice(data);

        // 3. Validar contra esquema XSD (no bloqueante)
        var validationErrors = validacionService.validar(invoiceType);

        // 4. Serializar a XML
        String xml = builder.serializarFactura(invoiceType);

        return new GenerarXmlResult(xml, validationErrors);
    }
}
