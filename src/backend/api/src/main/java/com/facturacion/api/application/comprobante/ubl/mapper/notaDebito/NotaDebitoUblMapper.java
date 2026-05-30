package com.facturacion.api.application.comprobante.ubl.mapper.notaDebito;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import com.facturacion.api.application.comprobante.modelo.DocumentoRelacionadoCanonico;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        // 2. Extraer Documento de Referencia (Obligatorio en Notas de Débito)
        DocumentoRelacionadoCanonico docRef = canonico.documentoRelacionado();
        if (docRef == null) {
            throw new IllegalArgumentException("La Nota de Débito requiere un documento relacionado (Factura/Boleta).");
        }

        // Dividir el número del documento afectado (ej: "F001-0001" -> "F001" y "0001")
        String[] refNumeros = docRef.numeroDocumento() != null ? docRef.numeroDocumento().split("-") : new String[]{"", ""};
        String refSerie = refNumeros.length > 0 ? refNumeros[0] : "";
        String refCorrelativo = refNumeros.length > 1 ? refNumeros[1] : "";

        // 3. Cálculos y Mapeo de Detalles/Lineas
        List<NotaDebitoLineaUblData> lineas = new ArrayList<>();
        BigDecimal totalImpuestos = BigDecimal.ZERO;
        BigDecimal valorVentaTotal = BigDecimal.ZERO;

        if (canonico.detalles() != null) {
            int numeroLinea = 1;
            for (DetalleCanonico det : canonico.detalles()) {
                BigDecimal cantidad = det.cantidad() != null ? det.cantidad() : BigDecimal.ZERO;
                BigDecimal valorUnitario = det.valorUnitario() != null ? det.valorUnitario() : BigDecimal.ZERO;
                BigDecimal igv = det.igv() != null ? det.igv() : BigDecimal.ZERO;
                
                // Cálculo de Valor de Venta (Cantidad * Valor Unitario)
                BigDecimal valorVentaLinea = cantidad.multiply(valorUnitario).setScale(2, RoundingMode.HALF_UP);
                
                // Determinación del porcentaje de IGV (18% si hay IGV)
                BigDecimal porcentajeIgv = igv.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("18.00") : BigDecimal.ZERO;

                lineas.add(new NotaDebitoLineaUblData(
                        numeroLinea++,
                        det.codigoProducto(),
                        det.descripcion(),
                        cantidad,
                        det.unidadMedida() != null ? det.unidadMedida() : "NIU",
                        igv,
                        porcentajeIgv,
                        det.codigoTipoIgv(),
                        valorVentaLinea
                ));
                
                // Sumar a los totales globales
                totalImpuestos = totalImpuestos.add(igv);
                valorVentaTotal = valorVentaTotal.add(valorVentaLinea);
            }
        }

        // Importe Total = Valor de Venta Total + Total Impuestos
        BigDecimal importeTotal = valorVentaTotal.add(totalImpuestos);

        // 4. Retornar el objeto Data armado (Alineado con NotaDebitoUblData.java)
        return new NotaDebitoUblData(
                serie,
                correlativo,
                canonico.fechaEmision(),
                canonico.tipoDocumento() != null ? canonico.tipoDocumento() : "08", // 08 = Nota de Débito
                canonico.moneda() != null ? canonico.moneda() : "PEN",
                canonico.emisorRuc(),
                canonico.emisorRazonSocial(),
                canonico.receptorDocumento(),
                canonico.receptorRazonSocial(),
                docRef.tipoDocumento(),
                refSerie,
                refCorrelativo,
                docRef.motivoCodigo(),
                docRef.motivoDescripcion(),
                totalImpuestos,
                importeTotal,
                valorVentaTotal,
                lineas
        );
    }
}