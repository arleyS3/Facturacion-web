package com.facturacion.api.application.comprobante.ubl.mapper.notaDebito;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper de comprobante canónico a datos UBL de nota de débito.
 */
@Component
public class NotaDebitoUblMapper {

    /**
     * Convierte el comprobante canónico a {@link NotaDebitoUblData}.
     *
     * @param canonico comprobante canónico
     * @return datos UBL de nota de débito
     */
    public NotaDebitoUblData fromCanonico(ComprobanteCanonico canonico) {
        String[] numeros = canonico.numero() != null ? canonico.numero().split("-") : new String[]{"BD01", "1"};
        String serie = numeros.length > 0 ? numeros[0] : "BD01";
        String correlativo = numeros.length > 1 ? numeros[1] : "1";

        List<NotaDebitoLineaUblData> lineas = canonico.detalles() != null
            ? canonico.detalles().stream()
                .map(this::mapLinea)
                .toList()
            : List.of();

        String tipoAfectado = canonico.documentoRelacionado() != null 
            ? canonico.documentoRelacionado().tipoDocumento() 
            : null;
        String documentoAfectadoNumero = canonico.documentoRelacionado() != null 
            ? canonico.documentoRelacionado().numeroDocumento() 
            : null;
        
        // Splitear numeroDocumento (ej: "F001-00000001") en serie y correlativo
        String documentoAfectadoSerie = "";
        String documentoAfectadoCorrelativo = documentoAfectadoNumero;
        if (documentoAfectadoNumero != null && documentoAfectadoNumero.contains("-")) {
            String[] partes = documentoAfectadoNumero.split("-", 2);
            documentoAfectadoSerie = partes[0];
            documentoAfectadoCorrelativo = partes.length > 1 ? partes[1] : "";
        }

        return new NotaDebitoUblData(
            serie,
            correlativo,
            canonico.fechaEmision(),
            "08",
            canonico.moneda(),
            canonico.emisorRuc(),
            null,
            canonico.receptorDocumento(),
            null,
            tipoAfectado,
            documentoAfectadoSerie,
            documentoAfectadoCorrelativo,
            canonico.documentoRelacionado() != null ? canonico.documentoRelacionado().motivoCodigo() : null,
            canonico.documentoRelacionado() != null ? canonico.documentoRelacionado().motivoDescripcion() : null,
            null,
            null,
            null,
            lineas
        );
    }

    /**
     * Mapea una línea canónica a línea UBL de nota de débito.
     *
     * @param detalle detalle canónico
     * @return línea UBL
     */
    private NotaDebitoLineaUblData mapLinea(DetalleCanonico detalle) {
        return new NotaDebitoLineaUblData(
            null,
            null,
            detalle.descripcion(),
            detalle.cantidad(),
            "NIU",
            detalle.igv(),
            new java.math.BigDecimal("18.00"),
            detalle.codigoTipoIgv(),
            null
        );
    }
}
