package com.facturacion.api.application.comprobante.validation;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.debitnote_21.DebitNoteType;
import oasis.names.specification.ubl.schema.xsd.despatchadvice_21.DespatchAdviceType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ValidacionService} interface contract.
 * <p>
 * Verifies that the interface accepts all UBL document types and returns
 * {@link List}{@code <String>} as specified.
 * </p>
 */
class ValidacionServiceTest {

    @Test
    void validarInvoice_returnsMutableList() {
        ValidacionService service = new ValidacionService() {
            @Override public List<String> validar(InvoiceType invoice) { return List.of(); }
            @Override public List<String> validar(CreditNoteType creditNote) { return List.of(); }
            @Override public List<String> validar(DebitNoteType debitNote) { return List.of(); }
            @Override public List<String> validar(DespatchAdviceType despatchAdvice) { return List.of(); }
        };

        List<String> result = service.validar(new InvoiceType());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void validarCreditNote_returnsEmptyList_whenNoErrors() {
        ValidacionService service = new ValidacionService() {
            @Override public List<String> validar(InvoiceType invoice) { return List.of(); }
            @Override public List<String> validar(CreditNoteType creditNote) { return List.of(); }
            @Override public List<String> validar(DebitNoteType debitNote) { return List.of(); }
            @Override public List<String> validar(DespatchAdviceType despatchAdvice) { return List.of(); }
        };

        List<String> result = service.validar(new CreditNoteType());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void validarDebitNote_returnsEmptyList_whenNoErrors() {
        ValidacionService service = new ValidacionService() {
            @Override public List<String> validar(InvoiceType invoice) { return List.of(); }
            @Override public List<String> validar(CreditNoteType creditNote) { return List.of(); }
            @Override public List<String> validar(DebitNoteType debitNote) { return List.of(); }
            @Override public List<String> validar(DespatchAdviceType despatchAdvice) { return List.of(); }
        };

        List<String> result = service.validar(new DebitNoteType());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void validarDespatchAdvice_returnsEmptyList_whenNoErrors() {
        ValidacionService service = new ValidacionService() {
            @Override public List<String> validar(InvoiceType invoice) { return List.of(); }
            @Override public List<String> validar(CreditNoteType creditNote) { return List.of(); }
            @Override public List<String> validar(DebitNoteType debitNote) { return List.of(); }
            @Override public List<String> validar(DespatchAdviceType despatchAdvice) { return List.of(); }
        };

        List<String> result = service.validar(new DespatchAdviceType());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void validarWithErrors_returnsNonEmptyList() {
        ValidacionService service = new ValidacionService() {
            @Override public List<String> validar(InvoiceType invoice) { return List.of("error 1", "error 2"); }
            @Override public List<String> validar(CreditNoteType creditNote) { return List.of("error"); }
            @Override public List<String> validar(DebitNoteType debitNote) { return List.of("error"); }
            @Override public List<String> validar(DespatchAdviceType despatchAdvice) { return List.of("error"); }
        };

        List<String> errors = service.validar(new InvoiceType());
        assertEquals(2, errors.size());
        assertEquals("error 1", errors.get(0));
        assertEquals("error 2", errors.get(1));
    }
}
