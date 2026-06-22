package com.facturacion.api.application.comprobante.ubl.validation;

import com.facturacion.api.application.comprobante.validation.ValidacionService;
import com.helger.diagnostics.error.list.IErrorList;
import com.helger.ubl21.UBL21Marshaller;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.debitnote_21.DebitNoteType;
import oasis.names.specification.ubl.schema.xsd.despatchadvice_21.DespatchAdviceType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * Implementación de {@link ValidacionService} que valida documentos UBL contra
 * los esquemas XSD de UBL 2.1 usando {@link UBL21Marshaller}.
 * <p>
 * La validación es <strong>no bloqueante</strong>: si ocurre cualquier
 * excepción (ej. esquema XSD no disponible, error de JAXB), se retorna una
 * lista vacía sin propagar la excepción.
 * </p>
 * <p>
 * Esta validación es independiente de la serialización XML. Los builders
 * existentes usan {@code .setUseSchema(false)} para la escritura — esta clase
 * no modifica esa configuración.
 * </p>
 */
@Component
public class XsdValidationService implements ValidacionService {

    @Override
    public List<String> validar(InvoiceType invoice) {
        return validarSeguro(() -> UBL21Marshaller.invoice().validate(invoice));
    }

    @Override
    public List<String> validar(CreditNoteType creditNote) {
        return validarSeguro(() -> UBL21Marshaller.creditNote().validate(creditNote));
    }

    @Override
    public List<String> validar(DebitNoteType debitNote) {
        return validarSeguro(() -> UBL21Marshaller.debitNote().validate(debitNote));
    }

    @Override
    public List<String> validar(DespatchAdviceType despatchAdvice) {
        return validarSeguro(() -> UBL21Marshaller.despatchAdvice().validate(despatchAdvice));
    }

    /**
     * Ejecuta la validación capturando cualquier excepción y convirtiendo
     * el resultado a una lista de mensajes legibles.
     *
     * @param supplier operación de validación a ejecutar
     * @return lista de errores de validación, vacía si no hay errores o si falla
     */
    private List<String> validarSeguro(Supplier<IErrorList> supplier) {
        try {
            IErrorList errorList = supplier.get();
            if (errorList == null || errorList.isEmpty()) {
                return List.of();
            }
            return errorList.stream()
                    .map(error -> error.getAsStringLocaleIndepdent())
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
