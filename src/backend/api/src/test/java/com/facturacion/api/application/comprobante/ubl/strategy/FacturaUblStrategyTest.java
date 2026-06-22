package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.dto.GenerarXmlResult;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.factura.FacturaUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.FacturaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.FacturaUblMapper;
import com.facturacion.api.application.comprobante.ubl.signature.XmlSignatureService;
import com.facturacion.api.application.comprobante.validation.ValidacionService;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link FacturaUblStrategy}.
 * <p>
 * Verifies that the strategy builds the UBL object, validates it,
 * serializes it, and returns a {@link GenerarXmlResult} with both
 * XML and validation errors.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class FacturaUblStrategyTest {

    @Mock
    private FacturaUblMapper mapper;

    @Mock
    private FacturaUblBuilder builder;

    @Mock
    private XmlSignatureService signatureService;

    @Mock
    private ValidacionService validationService;

    @Mock
    private ComprobanteCanonico canonico;

    private FacturaUblStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FacturaUblStrategy(mapper, builder, signatureService, validationService);
    }

    @Test
    void generarXml_returnsGenerarXmlResult_withXmlAndValidationErrors() throws Exception {
        FacturaUblData data = mock(FacturaUblData.class);
        InvoiceType invoiceType = new InvoiceType();
        String expectedXml = "<Invoice>test</Invoice>";
        List<String> expectedErrors = List.of("cvc-complex-type.2.4.a: Element ID missing");

        when(canonico.debeFirmar()).thenReturn(false);
        when(mapper.fromCanonico(canonico)).thenReturn(data);
        when(builder.buildInvoice(data)).thenReturn(invoiceType);
        when(validationService.validar(invoiceType)).thenReturn(expectedErrors);
        when(builder.serializarFactura(invoiceType)).thenReturn(expectedXml);

        GenerarXmlResult result = strategy.generarXml(canonico);

        assertNotNull(result);
        assertEquals(expectedXml, result.xml());
        assertEquals(expectedErrors, result.validationErrors());

        verify(mapper).fromCanonico(canonico);
        verify(builder).buildInvoice(data);
        verify(validationService).validar(invoiceType);
        verify(builder).serializarFactura(invoiceType);
        verifyNoInteractions(signatureService);
    }

    @Test
    void generarXml_returnsEmptyValidationErrors_whenXmlIsValid() throws Exception {
        FacturaUblData data = mock(FacturaUblData.class);
        InvoiceType invoiceType = new InvoiceType();
        String expectedXml = "<Invoice>valid</Invoice>";

        when(canonico.debeFirmar()).thenReturn(false);
        when(mapper.fromCanonico(canonico)).thenReturn(data);
        when(builder.buildInvoice(data)).thenReturn(invoiceType);
        when(validationService.validar(invoiceType)).thenReturn(List.of());
        when(builder.serializarFactura(invoiceType)).thenReturn(expectedXml);

        GenerarXmlResult result = strategy.generarXml(canonico);

        assertNotNull(result);
        assertEquals(expectedXml, result.xml());
        assertTrue(result.validationErrors().isEmpty());
    }

    @Test
    void generarXml_appliesDigitalSignature_whenDebeFirmarIsTrue() throws Exception {
        FacturaUblData data = mock(FacturaUblData.class);
        InvoiceType invoiceType = new InvoiceType();
        String unsignedXml = "<Invoice>unsigned</Invoice>";
        String signedXml = "<Invoice>signed</Invoice>";

        when(canonico.debeFirmar()).thenReturn(true);
        when(canonico.emisorRuc()).thenReturn("20123456789");
        when(mapper.fromCanonico(canonico)).thenReturn(data);
        when(builder.buildInvoice(data)).thenReturn(invoiceType);
        when(validationService.validar(invoiceType)).thenReturn(List.of());
        when(builder.serializarFactura(invoiceType)).thenReturn(unsignedXml);
        when(signatureService.signXml(unsignedXml, "20123456789")).thenReturn(signedXml);

        GenerarXmlResult result = strategy.generarXml(canonico);

        assertNotNull(result);
        assertEquals(signedXml, result.xml());
        verify(signatureService).signXml(unsignedXml, "20123456789");
    }

    @Test
    void codigoSunat_returns01() {
        assertEquals("01", strategy.codigoSunat());
    }
}
