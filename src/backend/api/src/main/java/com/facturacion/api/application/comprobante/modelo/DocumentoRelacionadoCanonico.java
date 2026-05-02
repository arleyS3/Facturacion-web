package com.facturacion.api.application.comprobante.modelo;

/**
 * Datos del documento relacionado (nota de crédito o débito).
 *
 * @param tipoDocumento tipo de documento afectado
 * @param serie serie del documento afectado
 * @param correlativo correlativo del documento afectado
 * @param motivoCodigo código del motivo
 * @param motivoDescripcion descripción del motivo
 */
public record DocumentoRelacionadoCanonico(
        String tipoDocumento,
        String serie,
        String correlativo,
        String motivoCodigo,
        String motivoDescripcion
) {
}
