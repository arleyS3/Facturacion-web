package com.facturacion.api.application.comprobante.ubl.validation;

import com.facturacion.api.application.comprobante.validation.ValidacionService;
import com.helger.schematron.pure.SchematronResourcePure;
import com.helger.schematron.svrl.SVRLHelper;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import com.helger.ubl21.UBL21Marshaller;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.debitnote_21.DebitNoteType;
import oasis.names.specification.ubl.schema.xsd.despatchadvice_21.DespatchAdviceType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import java.util.List;
import java.util.function.Supplier;

/**
 * Validación Schematron contra reglas de negocio SUNAT.
 * <p>
 * Las reglas están en {@code schematron/sunat.sch} y cubren validaciones
 * que el XSD no puede expresar: montos mínimos para RUC, coherencia de
 * impuestos, documentos de referencia obligatorios, etc.
 * </p>
 * <p>
 * Al igual que {@link XsdValidationService}, es <strong>no bloqueante</strong>:
 * cualquier error carga la lista sin detener el flujo de generación.
 * </p>
 */
@Component
public class SchematronValidationService implements ValidacionService {

    private static final Logger log = LoggerFactory.getLogger(SchematronValidationService.class);
    private static final String SCHEMATRON_PATH = "schematron/sunat.sch";

    private final SchematronResourcePure schematron;

    public SchematronValidationService() {
        this.schematron = SchematronResourcePure.fromClassPath(SCHEMATRON_PATH);
        if (!schematron.isValidSchematron()) {
            log.warn("Schematron rules file '{}' is not valid — validations will be skipped", SCHEMATRON_PATH);
        }
    }

    @Override
    public List<String> validar(InvoiceType invoice) {
        return validarDocumento(() -> UBL21Marshaller.invoice().getAsDocument(invoice));
    }

    @Override
    public List<String> validar(CreditNoteType creditNote) {
        return validarDocumento(() -> UBL21Marshaller.creditNote().getAsDocument(creditNote));
    }

    @Override
    public List<String> validar(DebitNoteType debitNote) {
        return validarDocumento(() -> UBL21Marshaller.debitNote().getAsDocument(debitNote));
    }

    @Override
    public List<String> validar(DespatchAdviceType despatchAdvice) {
        return validarDocumento(() -> UBL21Marshaller.despatchAdvice().getAsDocument(despatchAdvice));
    }

    private List<String> validarDocumento(Supplier<Document> supplier) {
        try {
            Document doc = supplier.get();
            if (doc == null) return List.of();
            return aplicarSchematron(doc);
        } catch (Exception e) {
            log.warn("Error en validación Schematron: {}", e.getMessage());
            return List.of();
        }
    }

    private List<String> aplicarSchematron(Document doc) throws Exception {
        SchematronOutputType svrl = schematron.applySchematronValidationToSVRL(doc, null);
        return SVRLHelper.getAllFailedAssertions(svrl).stream()
                .map(fa -> {
                    String loc = fa.getLocation();
                    String txt = fa.getText();
                    if (loc != null && !loc.isBlank()) {
                        return "[Schematron] " + loc + ": " + txt;
                    }
                    return "[Schematron] " + txt;
                })
                .toList();
    }
}
