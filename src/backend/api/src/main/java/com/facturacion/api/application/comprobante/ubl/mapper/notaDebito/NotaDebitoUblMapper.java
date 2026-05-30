package com.facturacion.api.application.comprobante.ubl.mapper.notaDebito;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import com.facturacion.api.application.comprobante.modelo.DocumentoRelacionadoCanonico;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper de comprobante canónico a datos UBL de Nota de Débito (DebitNote).
 */
@Component
public class NotaDebitoUblMapper {

    public NotaDebitoUblData fromCanonico(ComprobanteCanonico canonico) {
        if (canonico == null) {
            return null;
        }

        // 1. Serie y Correlativo (Ej: FD01-000001)
        String[] numeros = canonico.numero() != null ? canonico.numero().split("-") : new String[]{"FD01", "1"};
        String serie = numeros.length > 0 ? numeros[0] : "FD01";
        String correlativo = numeros.length > 1 ? numeros[1] : "1";

        // 2. Extraer Documento de Referencia (Obligatorio en Notas de Débito/Crédito)
        DocumentoRelacionadoCanonico docReferencia = null;
        if (canonico.documentosRelacionados() != null && !canonico.documentosRelacionados().isEmpty()) {
            docReferencia = canonico.documentosRelacionados().get(0);
        }

        if (docReferencia == null) {
            throw new IllegalArgumentException("La Nota de Débito requiere un documento de referencia (Factura/Boleta).");
        }

        String[] refNumeros = docReferencia.numero() != null ? docReferencia.numero().split("-") : new String[]{"", ""};
        String refSerie = refNumeros.length > 0 ? refNumeros[0] : "";
        String refCorrelativo = refNumeros.length > 1 ? refNumeros[1] : "";

        // 3. Mapeo de Detalles/Lineas
        List<NotaDebitoLineaUblData> lineas = new ArrayList<>();
        if (canonico.detalles() != null) {
            int numeroLinea = 1;
            for (DetalleCanonico det : canonico.detalles()) {
                lineas.add(new NotaDebitoLineaUblData(
                        numeroLinea++,
                        det.codigoProducto(),
                        det.descripcion(),
                        det.cantidad(),
                        det.unidadMedida() != null ? det.unidadMedida() : "NIU",
                        det.precioUnitario() != null ? det.precioUnitario() : BigDecimal.ZERO,
                        det.valorTotal() != null ? det.valorTotal() : BigDecimal.ZERO
                ));
            }
        }

        // 4. Retornar el objeto Data armado
        return new NotaDebitoUblData(
                serie,
                correlativo,
                canonico.fechaEmision(),
                canonico.horaEmision() != null ? canonico.horaEmision() : "00:00:00",
                canonico.moneda() != null ? canonico.moneda() : "PEN",
                canonico.emisorRuc(),
                "6", // RUC Emisor
                canonico.emisorRazonSocial(),
                canonico.receptorDocumento(),
                canonico.receptorTipoDocumento() != null ? canonico.receptorTipoDocumento() : "6",
                canonico.receptorRazonSocial(),
                docReferencia.tipoDocumento(), // 01 (Factura) o 03 (Boleta)
                refSerie,
                refCorrelativo,
                docReferencia.codigoMotivo(), // Catálogo 10
                docReferencia.descripcionMotivo(),
                canonico.totalCargos() != null ? canonico.totalCargos() : BigDecimal.ZERO,
                BigDecimal.ZERO, 
                canonico.totalIgv() != null ? canonico.totalIgv() : BigDecimal.ZERO,
                canonico.totalImporte() != null ? canonico.totalImporte() : BigDecimal.ZERO,
                lineas
        );
    }
}