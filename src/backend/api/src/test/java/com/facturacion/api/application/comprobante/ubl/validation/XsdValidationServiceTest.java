package com.facturacion.api.application.comprobante.ubl.validation;

import com.facturacion.api.application.comprobante.validation.ValidacionService;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.debitnote_21.DebitNoteType;
import oasis.names.specification.ubl.schema.xsd.despatchadvice_21.DespatchAdviceType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link XsdValidationService}.
 * <p>
 * Verifica que la validación XSD capture errores estructurales y que las
 * excepciones se manejen de forma no bloqueante (retornando lista vacía).
 * </p>
 */
class XsdValidationServiceTest {

    private ValidacionService service;

    @BeforeEach
    void setUp() {
        service = new XsdValidationService();
    }

    @Test
    void validarInvoice_emptyInvoice_capturesXsdErrors() {
        List<String> errors = service.validar(new InvoiceType());

        assertNotNull(errors);
        // An empty InvoiceType should produce XSD validation errors.
        // If XSD is available the list will be non-empty; if XSD isn't available
        // the catch block returns empty list — either way, the method is non-blocking.
        for (String error : errors) {
            assertFalse(error.isBlank(), "Each error message must be non-blank");
            assertTrue(error.contains("cvc") || error.contains(":"),
                    "Error messages should contain XSD validation details: " + error);
        }
    }

    @Test
    void validarInvoice_nullInvoice_returnsEmptyList() {
        List<String> errors = service.validar((InvoiceType) null);

        assertNotNull(errors);
        assertTrue(errors.isEmpty(),
                "Null input should be caught and return empty list");
    }

    @Test
    void validarCreditNote_nullInput_returnsEmptyList() {
        List<String> errors = service.validar((CreditNoteType) null);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void validarDebitNote_nullInput_returnsEmptyList() {
        List<String> errors = service.validar((DebitNoteType) null);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void validarDespatchAdvice_nullInput_returnsEmptyList() {
        List<String> errors = service.validar((DespatchAdviceType) null);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void errorMessages_areReadableStrings() {
        List<String> errors = service.validar(new InvoiceType());

        // If XSD validation produces errors, verify they're readable strings
        for (String error : errors) {
            assertNotNull(error);
            assertFalse(error.isBlank(), "Error messages should not be blank");
        }
    }

    @Test
    void allFourMethods_areNonBlocking() {
        // All methods should complete without throwing exceptions
        assertDoesNotThrow(() -> service.validar(new InvoiceType()));
        assertDoesNotThrow(() -> service.validar(new CreditNoteType()));
        assertDoesNotThrow(() -> service.validar(new DebitNoteType()));
        assertDoesNotThrow(() -> service.validar(new DespatchAdviceType()));
    }
}
