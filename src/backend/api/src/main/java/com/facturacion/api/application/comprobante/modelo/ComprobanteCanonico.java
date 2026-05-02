package com.facturacion.api.application.comprobante.modelo;

import java.util.List;

/**
 * Modelo canónico de comprobante para generación UBL.
 *
 * @param tipoDocumento código de documento
 * @param numero número del comprobante
 * @param fechaEmision fecha de emisión
 * @param moneda código de moneda
 * @param emisorRuc ruc del emisor
 * @param receptorDocumento documento del receptor
 * @param detalles líneas del comprobante
 * @param documentoRelacionado documento relacionado (si aplica)
 * @param parteTraslado datos de traslado (si aplica)
 */
public record ComprobanteCanonico(
        String tipoDocumento,
        String numero,
        String fechaEmision,
        String moneda,
        String emisorRuc,
        String receptorDocumento,
        List<DetalleCanonico> detalles,
        DocumentoRelacionadoCanonico documentoRelacionado,
        ParteTrasladoCanonico parteTraslado
) {
}
