package com.facturacion.api.application.comprobante.ubl.mapper.notaDebito;

import java.math.BigDecimal;

/**
 * Datos UBL 2.1 para nota de débito.
 *
 * @param serie serie del comprobante
 * @param correlativo correlativo del comprobante
 * @param fechaEmision fecha de emisión
 * @param tipoDocumento código de tipo de documento
 * @param moneda código de moneda
 * @param emisorNroDocumento número de documento del emisor
 * @param emisorRazonSocial razón social del emisor
 * @param receptorNroDocumento número de documento del receptor
 * @param receptorRazonSocial razón social del receptor
 * @param documentoAfectadoTipo tipo del documento afectado
 * @param documentoAfectadoSerie serie del documento afectado
 * @param documentoAfectadoNumero número del documento afectado
 * @param codigoMotivo código de motivo
 * @param descripcionMotivo descripción del motivo
 * @param totalImpuestos total de impuestos
 * @param importeTotal importe total
 * @param valorVenta valor de venta
 * @param lineas líneas de detalle
 */
public record NotaDebitoUblData(
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
        BigDecimal totalImpuestos,
        BigDecimal importeTotal,
        BigDecimal valorVenta,
        java.util.List<NotaDebitoLineaUblData> lineas
) {
}
