package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.dto.GenerarXmlResult;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.notaDebito.NotaDebitoUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.notaDebito.NotaDebitoUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.notaDebito.NotaDebitoUblMapper;
import com.facturacion.api.application.comprobante.validation.ValidacionService;
import oasis.names.specification.ubl.schema.xsd.debitnote_21.DebitNoteType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link NotaDebitoUblStrategy}.
 * <p>
 * Verifies that the strategy builds the DebitNote UBL object, validates it,
 * serializes it, and returns a {@link GenerarXmlResult}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class NotaDebitoUblStrategyTest {

    @Mock
    private NotaDebitoUblMapper mapper;

    @Mock
    private NotaDebitoUblBuilder builder;

    @Mock
    private ValidacionService validationService;

    @Mock
    private ComprobanteCanonico canonico;

    private NotaDebitoUblStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new NotaDebitoUblStrategy(mapper, builder, validationService);
    }

    @Test
    void generarXml_returnsGenerarXmlResult_withXmlAndValidationErrors() throws Exception {
        NotaDebitoUblData data = mock(NotaDebitoUblData.class);
        DebitNoteType debitNote = new DebitNoteType();
        String expectedXml = "<DebitNote>test</DebitNote>";
        List<String> expectedErrors = List.of("cvc-datatype: invalid date format");

        when(mapper.fromCanonico(canonico)).thenReturn(data);
        when(builder.buildDebitNote(data)).thenReturn(debitNote);
        when(validationService.validar(debitNote)).thenReturn(expectedErrors);
        when(builder.serializar(debitNote)).thenReturn(expectedXml);

        GenerarXmlResult result = strategy.generarXml(canonico);

        assertNotNull(result);
        assertEquals(expectedXml, result.xml());
        assertEquals(expectedErrors, result.validationErrors());

        verify(mapper).fromCanonico(canonico);
        verify(builder).buildDebitNote(data);
        verify(validationService).validar(debitNote);
        verify(builder).serializar(debitNote);
    }

    @Test
    void generarXml_returnsEmptyValidationErrors_whenXmlIsValid() throws Exception {
        NotaDebitoUblData data = mock(NotaDebitoUblData.class);
        DebitNoteType debitNote = new DebitNoteType();
        String expectedXml = "<DebitNote>valida</DebitNote>";

        when(mapper.fromCanonico(canonico)).thenReturn(data);
        when(builder.buildDebitNote(data)).thenReturn(debitNote);
        when(validationService.validar(debitNote)).thenReturn(List.of());
        when(builder.serializar(debitNote)).thenReturn(expectedXml);

        GenerarXmlResult result = strategy.generarXml(canonico);

        assertNotNull(result);
        assertEquals(expectedXml, result.xml());
        assertTrue(result.validationErrors().isEmpty());
    }

    @Test
    void codigoSunat_returns08() {
        assertEquals("08", strategy.codigoSunat());
    }
}
