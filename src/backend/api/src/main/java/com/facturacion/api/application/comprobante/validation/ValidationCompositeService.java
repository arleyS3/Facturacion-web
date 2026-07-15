package com.facturacion.api.application.comprobante.validation;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.debitnote_21.DebitNoteType;
import oasis.names.specification.ubl.schema.xsd.despatchadvice_21.DespatchAdviceType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Compone múltiples {@link ValidacionService} y mergea los errores.
 * <p>
 * Orden actual: XSD estructural → Schematron reglas de negocio.
 * Marcado {@link Primary} para que las estrategias lo inyecten sin cambios.
 * </p>
 */
@Component
@Primary
public class ValidationCompositeService implements ValidacionService {

    private final List<ValidacionService> validadores;

    public ValidationCompositeService(List<ValidacionService> validadores) {
        this.validadores = validadores;
    }

    @Override
    public List<String> validar(InvoiceType invoice) {
        return validadores.stream()
                .flatMap(v -> v.validar(invoice).stream())
                .toList();
    }

    @Override
    public List<String> validar(CreditNoteType creditNote) {
        return validadores.stream()
                .flatMap(v -> v.validar(creditNote).stream())
                .toList();
    }

    @Override
    public List<String> validar(DebitNoteType debitNote) {
        return validadores.stream()
                .flatMap(v -> v.validar(debitNote).stream())
                .toList();
    }

    @Override
    public List<String> validar(DespatchAdviceType despatchAdvice) {
        return validadores.stream()
                .flatMap(v -> v.validar(despatchAdvice).stream())
                .toList();
    }
}
