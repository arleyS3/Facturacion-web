package com.facturacion.api.application.comprobante.ubl.mapper.notaCredito;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos UBL 2.1 para nota de crédito.
 */
public record NotaCreditoUblData(
        String serie,
        String correlativo,
        String fechaEmision,
        String tipoDocumento,
        String moneda,
        // Emisor
        String emisorNroDocumento,
        String emisorRazonSocial,
        String emisorCodigoDomicilio,
        // Receptor
        String receptorNroDocumento,
        String receptorTipoDocumento,
        String receptorRazonSocial,
        // Documento afectado
        String documentoAfectadoTipo,
        String documentoAfectadoSerie,
        String documentoAfectadoNumero,
        String codigoMotivo,
        String descripcionMotivo,
        // Totales desglosados
        BigDecimal gravadas,
        BigDecimal exoneradas,
        BigDecimal inafectas,
        BigDecimal igv,
        BigDecimal totalImpuestos,
        BigDecimal importeTotal,
        BigDecimal valorVenta,
        // Referencias opcionales
        String guiaRemisionId,
        String guiaRemisionCodigo,
        List<DocumentoAdicionalUblData> documentosAdicionales,
        // Líneas
        List<NotaCreditoLineaUblData> lineas
) {
}
