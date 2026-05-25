package com.facturacion.api.application.comprobante.ubl.mapper.notaCredito;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import com.facturacion.api.application.comprobante.modelo.DocumentoAdicionalCanonico;
import com.facturacion.api.application.comprobante.modelo.GuiaRemisionReferenciaCanonico;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper de comprobante canónico a datos UBL de nota de crédito.
 * Sigue el mismo patrón que FacturaUblMapper.
 */
@Component
public class NotaCreditoUblMapper {

    private static final BigDecimal PORCENTAJE_IGV = new BigDecimal("0.18");

    public NotaCreditoUblData fromCanonico(ComprobanteCanonico canonico) {
        String[] numeros = canonico.numero() != null
                ? canonico.numero().split("-", 2)
                : new String[]{"FC01", "1"};
        String serie      = numeros.length > 0 ? numeros[0] : "FC01";
        String correlativo = numeros.length > 1 ? numeros[1] : "1";

        List<NotaCreditoLineaUblData> lineas = canonico.detalles() != null
                ? mapLineas(canonico.detalles())
                : List.of();

        // Totales por tipo de afectación IGV
        BigDecimal gravadas   = BigDecimal.ZERO;
        BigDecimal exoneradas = BigDecimal.ZERO;
        BigDecimal inafectas  = BigDecimal.ZERO;
        BigDecimal igvTotal   = BigDecimal.ZERO;
        BigDecimal valorVenta = BigDecimal.ZERO;

        for (NotaCreditoLineaUblData linea : lineas) {
            BigDecimal vv  = valorOZero(linea.valorVenta());
            BigDecimal igv = valorOZero(linea.montoIGV());
            String tipo    = linea.tipoAfectacionIGV() != null ? linea.tipoAfectacionIGV() : "10";

            valorVenta = valorVenta.add(vv);

            switch (tipo) {
                case "10" -> {
                    BigDecimal base = igv.compareTo(BigDecimal.ZERO) > 0
                            ? igv.divide(PORCENTAJE_IGV, 2, RoundingMode.HALF_UP)
                            : vv;
                    gravadas = gravadas.add(base);
                    igvTotal = igvTotal.add(igv);
                }
                case "20" -> exoneradas = exoneradas.add(vv);
                case "30", "31" -> inafectas = inafectas.add(vv);
            }
        }

        BigDecimal totalImpuestos = igvTotal;
        BigDecimal importeTotal   = valorVenta.add(totalImpuestos);

        // Documento afectado
        String tipoAfectado       = null;
        String docAfectadoSerie   = "";
        String docAfectadoNumero  = "";

        if (canonico.documentoRelacionado() != null) {
            tipoAfectado = canonico.documentoRelacionado().tipoDocumento();
            String numDoc = canonico.documentoRelacionado().numeroDocumento();
            if (numDoc != null && numDoc.contains("-")) {
                String[] partes = numDoc.split("-", 2);
                docAfectadoSerie  = partes[0];
                docAfectadoNumero = partes.length > 1 ? partes[1] : "";
            } else {
                docAfectadoNumero = numDoc != null ? numDoc : "";
            }
        }

        // Guía de remisión
        GuiaRemisionReferenciaCanonico guia = canonico.guiaRemision();
        String guiaId     = guia != null ? guia.id() : null;
        String guiaCodigo = guia != null ? guia.codigoDocumento() : null;

        // Documentos adicionales
        List<DocumentoAdicionalUblData> docsAdicionales = mapDocumentosAdicionales(canonico.documentosAdicionales());

        return new NotaCreditoUblData(
                serie,
                correlativo,
                canonico.fechaEmision(),
                "07",
                canonico.moneda(),
                canonico.emisorRuc(),
                canonico.emisorRazonSocial(),
                canonico.emisorCodigoDomicilio() != null ? canonico.emisorCodigoDomicilio() : "0001",
                canonico.receptorDocumento(),
                "6", // por defecto RUC; se puede extender con tipo desde canónico
                canonico.receptorRazonSocial(),
                tipoAfectado,
                docAfectadoSerie,
                docAfectadoNumero,
                canonico.documentoRelacionado() != null ? canonico.documentoRelacionado().motivoCodigo() : null,
                canonico.documentoRelacionado() != null ? canonico.documentoRelacionado().motivoDescripcion() : null,
                gravadas,
                exoneradas,
                inafectas,
                igvTotal,
                totalImpuestos,
                importeTotal,
                valorVenta,
                guiaId,
                guiaCodigo,
                docsAdicionales,
                lineas
        );
    }

    private List<NotaCreditoLineaUblData> mapLineas(List<DetalleCanonico> detalles) {
        List<NotaCreditoLineaUblData> lineas = new ArrayList<>();
        int num = 1;
        for (DetalleCanonico d : detalles) {
            lineas.add(mapLinea(d, num++));
        }
        return lineas;
    }

    private NotaCreditoLineaUblData mapLinea(DetalleCanonico d, int numero) {
        BigDecimal cantidad      = d.cantidad() != null ? d.cantidad() : BigDecimal.ONE;
        BigDecimal valorUnitario = d.valorUnitario() != null ? d.valorUnitario() : BigDecimal.ZERO;
        BigDecimal valorVenta    = cantidad.multiply(valorUnitario);
        BigDecimal montoIgv      = d.igv() != null ? d.igv() : BigDecimal.ZERO;

        return new NotaCreditoLineaUblData(
                numero,
                d.codigoProducto(),
                d.descripcion(),
                cantidad,
                d.unidadMedida() != null ? d.unidadMedida() : "NIU",
                montoIgv,
                PORCENTAJE_IGV.multiply(new BigDecimal("100")),
                d.codigoTipoIgv(),
                valorVenta
        );
    }

    private List<DocumentoAdicionalUblData> mapDocumentosAdicionales(List<DocumentoAdicionalCanonico> docs) {
        if (docs == null || docs.isEmpty()) return List.of();
        return docs.stream()
                .map(d -> new DocumentoAdicionalUblData(d.id(), d.tipoDocumento()))
                .toList();
    }

    private static BigDecimal valorOZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
