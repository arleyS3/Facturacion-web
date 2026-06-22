package com.facturacion.api.application.comprobante.validation;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.debitnote_21.DebitNoteType;
import oasis.names.specification.ubl.schema.xsd.despatchadvice_21.DespatchAdviceType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

import java.util.List;

/**
 * Servicio de validación de documentos UBL contra esquemas XSD.
 * <p>
 * Cada método valida un tipo de documento UBL y retorna una lista de errores
 * de validación. Si el documento es válido o si ocurre una excepción durante
 * la validación, retorna una lista vacía.
 * </p>
 * <p>
 * La validación es <strong>no bloqueante</strong> — el XML se genera siempre,
 * los errores son informativos y no interrumpen el flujo de generación.
 * </p>
 */
public interface ValidacionService {

    /**
     * Valida una factura (Invoice) contra el esquema XSD de UBL 2.1.
     *
     * @param invoice factura a validar
     * @return lista de errores de validación, vacía si es válida o si falla la validación
     */
    List<String> validar(InvoiceType invoice);

    /**
     * Valida una nota de crédito (CreditNote) contra el esquema XSD de UBL 2.1.
     *
     * @param creditNote nota de crédito a validar
     * @return lista de errores de validación, vacía si es válida o si falla la validación
     */
    List<String> validar(CreditNoteType creditNote);

    /**
     * Valida una nota de débito (DebitNote) contra el esquema XSD de UBL 2.1.
     *
     * @param debitNote nota de débito a validar
     * @return lista de errores de validación, vacía si es válida o si falla la validación
     */
    List<String> validar(DebitNoteType debitNote);

    /**
     * Valida una guía de remisión (DespatchAdvice) contra el esquema XSD de UBL 2.1.
     *
     * @param despatchAdvice guía de remisión a validar
     * @return lista de errores de validación, vacía si es válida o si falla la validación
     */
    List<String> validar(DespatchAdviceType despatchAdvice);
}
