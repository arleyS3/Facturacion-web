package com.facturacion.api.application.comprobante.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Datos del documento relacionado (nota de crédito o débito).
 * 
 * <p>
 * Ejemplo JSON con snake_case:
 * </p>
 * <pre>
 * {
 *   "tipo_documento": "01",
 *   "numero_documento": "F001-00000001",
 *   "codigo_motivo": "01"
 * }
 * </pre>
 *
 * @param tipoDocumento tipo de documento afectado
 * @param serie serie del documento afectado
 * @param correlativo correlativo del documento afectado
 * @param motivoCodigo código del motivo
 * @param motivoDescripcion descripción del motivo
 */
public record DocumentoRelacionadoCanonico(
        @JsonProperty("tipo_documento")
        String tipoDocumento,
        
        @JsonProperty("numero_documento")
        String numeroDocumento,
        
        @JsonProperty("codigo_motivo")
        String motivoCodigo,
        
        @JsonProperty("descripcion_motivo")
        String motivoDescripcion
) {
}
