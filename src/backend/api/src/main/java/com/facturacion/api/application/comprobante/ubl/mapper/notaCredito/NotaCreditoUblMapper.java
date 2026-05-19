package com.facturacion.api.application.comprobante.ubl.mapper.notaCredito;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
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

    /**
     * Convierte el comprobante canónico a {@link NotaCreditoUblData}.
     */
    public NotaCreditoUblData fromCanonico(ComprobanteCanonico canonico) {
        // Serie y correlativo desde numero (ej: "F001-00000001")
        String[] numeros = canonico.numero() != null
                ? canonico.numero().split("-", 2)
                : new String[]{"FC01", "1"};
        String serie      = numeros.length > 0 ? numeros[0] : "FC01";
        String correlativo = numeros.length > 1 ? numeros[1] : "1";

        // Líneas
        List<NotaCreditoLineaUblData> lineas = canonico.detalles() != null
                ? mapLineas(canonico.detalles())
                : List.of();

        // Totales calculados desde líneas (igual que FacturaUblMapper)
        BigDecimal gravadas   = BigDecimal.ZERO;
        BigDecimal exoneradas = BigDecimal.ZERO;
        BigDecimal inafectas  = BigDecimal.ZERO;
        BigDecimal igvTotal   = BigDecimal.ZERO;
        BigDecimal valorVenta = BigDecimal.ZERO;

        for (NotaCreditoLineaUblData linea : lineas) {
            BigDecimal vv = valorOZero(linea.valorVenta());
            BigDecimal igv = valorOZero(linea.montoIGV());
            String tipo = linea.tipoAfectacionIGV() != null ? linea.tipoAfectacionIGV() : "10";

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
        String tipoAfectado = null;
        String docAfectadoSerie = "";
        String docAfectadoNumero = "";

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

        return new NotaCreditoUblData(
                serie,
                correlativo,
                canonico.fechaEmision(),
                "07",
                canonico.moneda(),
                canonico.emisorRuc(),
                canonico.emisorRazonSocial(),
                canonico.receptorDocumento(),
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
                PORCENTAJE_IGV.multiply(new BigDecimal("100")), // 18.00
                d.codigoTipoIgv(),
                valorVenta
        );
    }

    private static BigDecimal valorOZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
