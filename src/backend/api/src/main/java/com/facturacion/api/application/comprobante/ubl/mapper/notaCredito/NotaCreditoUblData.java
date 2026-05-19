package com.facturacion.api.application.comprobante.ubl.mapper.notaCredito;

import java.math.BigDecimal;

/**
 * Datos UBL 2.1 para nota de crédito.
 */
public record NotaCreditoUblData(
        String serie,
        String correlativo,
        String fechaEmision,
        String tipoDocumento,
        String moneda,
        String emisorNroDocumento,
        String emisorRazonSocial,
        String receptorNroDocumento,
        String receptorRazonSocial,
        String documentoAfectadoTipo,
        String documentoAfectadoSerie,
        String documentoAfectadoNumero,
        String codigoMotivo,
        String descripcionMotivo,
        // Totales desglosados (igual que FacturaUblData)
        BigDecimal gravadas,
        BigDecimal exoneradas,
        BigDecimal inafectas,
        BigDecimal igv,
        BigDecimal totalImpuestos,
        BigDecimal importeTotal,
        BigDecimal valorVenta,
        java.util.List<NotaCreditoLineaUblData> lineas
) {
}
