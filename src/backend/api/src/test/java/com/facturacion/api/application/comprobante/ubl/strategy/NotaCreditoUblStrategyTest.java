package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.dto.GenerarXmlResult;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.notaCredito.NotaCreditoUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.NotaCreditoUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.NotaCreditoUblMapper;
import com.facturacion.api.application.comprobante.validation.ValidacionService;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link NotaCreditoUblStrategy}.
 * <p>
 * Verifies that the strategy builds the CreditNote UBL object, validates it,
 * serializes it, and returns a {@link GenerarXmlResult}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class NotaCreditoUblStrategyTest {

    @Mock
    private NotaCreditoUblMapper mapper;

    @Mock
    private NotaCreditoUblBuilder builder;

    @Mock
    private ValidacionService validationService;

    private NotaCreditoUblStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new NotaCreditoUblStrategy(mapper, builder, validationService);
    }

    @Test
    void generarXml_returnsGenerarXmlResult_withXmlAndValidationErrors() throws Exception {
        NotaCreditoUblData data = mock(NotaCreditoUblData.class);
        CreditNoteType creditNote = new CreditNoteType();
        String expectedXml = "<CreditNote>test</CreditNote>";
        List<String> expectedErrors = List.of("cvc-complex-type: Element missing");

        when(mapper.fromCanonico(canonico)).thenReturn(data);
        when(builder.buildCreditNote(data)).thenReturn(creditNote);
        when(validationService.validar(creditNote)).thenReturn(expectedErrors);
        when(builder.serializar(creditNote)).thenReturn(expectedXml);

        GenerarXmlResult result = strategy.generarXml(canonico);

        assertNotNull(result);
        assertEquals(expectedXml, result.xml());
        assertEquals(expectedErrors, result.validationErrors());

        verify(mapper).fromCanonico(canonico);
        verify(builder).buildCreditNote(data);
        verify(validationService).validar(creditNote);
        verify(builder).serializar(creditNote);
    }

    @Test
    void generarXml_returnsEmptyValidationErrors_whenXmlIsValid() throws Exception {
        NotaCreditoUblData data = mock(NotaCreditoUblData.class);
        CreditNoteType creditNote = new CreditNoteType();
        String expectedXml = "<CreditNote>valida</CreditNote>";

        when(mapper.fromCanonico(canonico)).thenReturn(data);
        when(builder.buildCreditNote(data)).thenReturn(creditNote);
        when(validationService.validar(creditNote)).thenReturn(List.of());
        when(builder.serializar(creditNote)).thenReturn(expectedXml);

        GenerarXmlResult result = strategy.generarXml(canonico);

        assertNotNull(result);
        assertEquals(expectedXml, result.xml());
        assertTrue(result.validationErrors().isEmpty());
    }

    @Test
    void codigoSunat_returns07() {
        assertEquals("07", strategy.codigoSunat());
    }

    @Mock
    private ComprobanteCanonico canonico;
}
