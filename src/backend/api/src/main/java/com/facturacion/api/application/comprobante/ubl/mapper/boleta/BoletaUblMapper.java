package com.facturacion.api.application.comprobante.ubl.mapper.boleta;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;

import lombok.val;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper de comprobante canónico a datos UBL de boleta.
 */
@Component
public class BoletaUblMapper {
    /**
     * Convierte el comprobante canónico a {@link BoletaUblData}.
     *
     * @param canonico comprobante canónico
     * @return datos UBL de boleta
     */

    private static final BigDecimal PORCENTAJE_IGV = new BigDecimal("0.18");

    public BoletaUblData fromCanonico(ComprobanteCanonico canonico) {
        String[] numeros = canonico.numero() != null ? canonico.numero().split("-") : new String[]{"B001", "1"};
        String serie = numeros.length > 0 ? numeros[0] : "B001";
        String correlativo = numeros.length > 1 ? numeros[1] : "1";

        String horaEmision = canonico.horaEmision() != null ? canonico.horaEmision() : "00:00:00";

        List<BoletaLineaUblData> lineas = canonico.detalles() != null 
                ? mapLineas(canonico.detalles()) 
                : List.of();

        BigDecimal gravadas = BigDecimal.ZERO;
        BigDecimal exoneradas = BigDecimal.ZERO;
        BigDecimal inafectadas = BigDecimal.ZERO;
        BigDecimal totalIgv = BigDecimal.ZERO;
        BigDecimal valorVenta = BigDecimal.ZERO;

        for (BoletaLineaUblData linea : lineas) {
            BigDecimal montoIgv = linea.montoIGV() != null ? linea.montoIGV() : BigDecimal.ZERO;
            BigDecimal vlVenta = linea.valorVenta() != null ? linea.valorVenta() : BigDecimal.ZERO;
            String tipoAfectacion = linea.tipoAfectacionIGV();

            valorVenta = valorVenta.add(vlVenta);

            if ("10".equals(tipoAfectacion)) {
                BigDecimal base = montoIgv.compareTo(BigDecimal.ZERO) > 0 
                        ? montoIgv.divide(PORCENTAJE_IGV, 2, RoundingMode.HALF_UP) 
                        : vlVenta;
                gravadas = gravadas.add(base);
                totalIgv = totalIgv.add(montoIgv);      
            } else if ("20".equals(tipoAfectacion)) {
                exoneradas = exoneradas.add(vlVenta);
            } else if ("30".equals(tipoAfectacion) || "31".equals(tipoAfectacion)) {
                inafectadas = inafectadas.add(vlVenta);
            }
        }

        BigDecimal totalImpuestos = totalIgv;
        BigDecimal importeTotal = valorVenta.add(totalImpuestos);

        String receptorNroDoc = canonico.receptorDocumento();
        String receptorTipoDoc = resolverTipoDocumentoReceptor(receptorNroDoc); 
        String receptorRazonSocial = canonico.receptorRazonSocial() != null 
                ? canonico.receptorRazonSocial() 
                : "-";

        if ("0".equals(receptorTipoDoc)) {
            receptorNroDoc = "-";
        }

        return new BoletaUblData(
            serie,
            correlativo,
            canonico.fechaEmision(),
            horaEmision,
            canonico.moneda(),
            canonico.emisorRuc(),
            "6",
            canonico.emisorRazonSocial(),
            receptorNroDoc,
            receptorTipoDoc,
            receptorRazonSocial,
            gravadas,
            exoneradas,
            inafectadas,
            totalIgv,
            totalImpuestos,
            importeTotal,
            valorVenta,
            lineas
        );
    }

    private String resolverTipoDocumentoReceptor(String nroDocumento) {
        if (nroDocumento == null || nroDocumento.isBlank() || "-".equals(nroDocumento)) {
            return "0"; 
        }

        return switch (nroDocumento.trim().length()) {
            case 8 -> "1";
            case 11 -> "6";
            default -> "0";
        }; 
    }

    private List<BoletaLineaUblData> mapLineas(List<DetalleCanonico> detalles) {
        List<BoletaLineaUblData> lineas = new ArrayList<>();
        int numeroLinea = 1;
        for (DetalleCanonico detalle : detalles) {
            lineas.add(mapLinea(detalle, numeroLinea));
            numeroLinea++;
        }

        return lineas;
    }

    /**
     * Mapea una línea canónica a línea UBL de boleta.
     *
     * @param detalle detalle canónico
     * @return línea UBL
     */
    private BoletaLineaUblData mapLinea(DetalleCanonico detalle, int numeroLinea) {

        BigDecimal cantidad = detalle.cantidad() != null ? detalle.cantidad() : BigDecimal.ZERO;
        BigDecimal valorUnitario = detalle.valorUnitario() != null ? detalle.valorUnitario() : BigDecimal.ZERO;
        BigDecimal valorVenta = cantidad.multiply(valorUnitario);

        return new BoletaLineaUblData(
            numeroLinea,
            detalle.codigoProducto(),
            detalle.descripcion(),
            cantidad, 
            detalle.unidadMedida() != null ? detalle.unidadMedida() : "NIU",
            detalle.igv(),
            new BigDecimal("18.00"),
            detalle.codigoTipoIgv(),
            detalle.valorUnitario(),
            valorVenta
        );
    }
}
