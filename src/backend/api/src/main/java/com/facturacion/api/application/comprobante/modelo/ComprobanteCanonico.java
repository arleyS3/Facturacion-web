package com.facturacion.api.application.comprobante.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Modelo canónico de comprobante para generación UBL.
 * 
 * <p>
 * Acepta nombres de campo en formato snake_case (JSON) además de camelCase.
 * Ejemplo de request:
 * </p>
 * <pre>
 * {
 *   "tipo_documento": "01",
 *   "numero": "F001-00000001",
 *   "fecha_emision": "2026-05-01",
 *   "emisor_ruc": "20123456789",
 *   "emisor_razonSocial": "Empresa SAC",
 *   ...
 * }
 * </pre>
 *
 * @param tipoDocumento código de documento (SUNAT)
 * @param numero número del comprobante
 * @param fechaEmision fecha de emisión
 * @param horaEmision hora de emisión (opcional)
 * @param fechaVencimiento fecha de vencimiento (opcional)
 * @param tipoDeOperacion código de tipo de operación (catálogo 17)
 * @param moneda código de moneda
 * @param emisorRuc RUC del emisor
 * @param emisorRazonSocial razón social del emisor
 * @param emisorNombreComercial nombre comercial del emisor (opcional)
 * @param emisorDireccion dirección del emisor (opcional)
 * @param emisorUrbanizacion urbanización del emisor (opcional)
 * @param emisorDepartamento departamento del emisor (opcional)
 * @param emisorProvincia provincia del emisor (opcional)
 * @param emisorDistrito distrito del emisor (opcional)
 * @param emisorUbigeo ubigeo del emisor (opcional)
 * @param emisorCodigoDomicilio código de domicilio fiscal (opcional)
 * @param receptorDocumento documento del receptor
 * @param receptorRazonSocial razón social del receptor (opcional)
 * @param receptorDireccion dirección del receptor (opcional)
 * @param receptorUbigeo ubigeo del receptor (opcional)
 * @param detalles líneas del comprobante
 * @param documentoRelacionado documento relacionado (si aplica)
 * @param parteTraslado datos de traslado (si aplica)
 * @param leyendas leyendas del comprobante (catálogo 52)
 * @param descuentosGlobales descuentos o cargos globales (opcional)
 * @param anticipos lista de anticipos aplicados (opcional)
 * @param documentoAdicional documento adicional referenciado (opcional)
 * @param guiaRemision guía de remisión relacionada (opcional)
 */
public record ComprobanteCanonico(
        @NotBlank(message = "Tipo de documento es requerido")
        @Size(max = 2, message = "Tipo de documento máximo 2 caracteres")
        @JsonProperty("tipo_documento")
        String tipoDocumento,

        @NotBlank(message = "Número de comprobante es requerido")
        @Size(max = 20, message = "Número máximo 20 caracteres")
        String numero,

        @NotBlank(message = "Fecha de emisión es requerida")
        @Size(max = 10, message = "Fecha formato YYYY-MM-DD")
        @JsonProperty("fecha_emision")
        String fechaEmision,

        @Size(max = 8, message = "Hora formato HH:mm:ss")
        @JsonProperty("hora_emision")
        String horaEmision,

        @Size(max = 10, message = "Fecha formato YYYY-MM-DD")
        @JsonProperty("fecha_vencimiento")
        String fechaVencimiento,

        @Size(max = 4, message = "Tipo de operación máximo 4 caracteres")
        @JsonProperty("tipo_de_operacion")
        String tipoDeOperacion,

        @NotBlank(message = "Moneda es requerida")
        @Size(max = 3, message = "Código de moneda máximo 3 caracteres")
        String moneda,

        @NotBlank(message = "RUC del emisor es requerido")
        @Size(min = 11, max = 11, message = "RUC debe tener 11 dígitos")
        @JsonProperty("emisor_ruc")
        String emisorRuc,

        @NotBlank(message = "Razón social del emisor es requerida")
        @Size(max = 200, message = "Razón social máximo 200 caracteres")
        @JsonProperty("emisor_razonSocial")
        String emisorRazonSocial,

        @Size(max = 200, message = "Nombre comercial máximo 200 caracteres")
        @JsonProperty("emisor_nombreComercial")
        String emisorNombreComercial,

        @Size(max = 500, message = "Dirección máximo 500 caracteres")
        @JsonProperty("emisor_direccion")
        String emisorDireccion,

        @Size(max = 200, message = "Urbanización máximo 200 caracteres")
        @JsonProperty("emisor_urbanizacion")
        String emisorUrbanizacion,

        @Size(max = 100, message = "Departamento máximo 100 caracteres")
        @JsonProperty("emisor_departamento")
        String emisorDepartamento,

        @Size(max = 100, message = "Provincia máximo 100 caracteres")
        @JsonProperty("emisor_provincia")
        String emisorProvincia,

        @Size(max = 100, message = "Distrito máximo 100 caracteres")
        @JsonProperty("emisor_distrito")
        String emisorDistrito,

        @Size(max = 6, message = "Ubigeo máximo 6 caracteres")
        @JsonProperty("emisor_ubigeo")
        String emisorUbigeo,

        @Size(max = 4, message = "Código de domicilio máximo 4 caracteres")
        @JsonProperty("emisor_codigoDomicilio")
        String emisorCodigoDomicilio,

        @NotBlank(message = "Documento del receptor es requerido")
        @Size(min = 4, max = 20, message = "Documento receptor debe tener entre 4 y 20 caracteres")
        @JsonProperty("receptor_documento")
        String receptorDocumento,

        @Size(max = 200, message = "Razón social receptor máximo 200 caracteres")
        @JsonProperty("receptor_razonSocial")
        String receptorRazonSocial,

        @Size(max = 500, message = "Dirección receptor máximo 500 caracteres")
        @JsonProperty("receptor_direccion")
        String receptorDireccion,

        @Size(max = 6, message = "Ubigeo receptor máximo 6 caracteres")
        @JsonProperty("receptor_ubigeo")
        String receptorUbigeo,

        @NotEmpty(message = "Debe incluir al menos un detalle")
        List<DetalleCanonico> detalles,

        @JsonProperty("documento_relacionado")
        DocumentoRelacionadoCanonico documentoRelacionado,

        @JsonProperty("parte_traslado")
        ParteTrasladoCanonico parteTraslado,

        List<LeyendaCanonico> leyendas,

        @JsonProperty("descuentos_globales")
        List<DescuentoGlobalCanonico> descuentosGlobales,

        List<AnticipoCanonico> anticipos,

        @JsonProperty("documentos_adicionales")
        List<DocumentoAdicionalCanonico> documentosAdicionales,

        @JsonProperty("guia_remision")
        GuiaRemisionReferenciaCanonico guiaRemision,

        /**
         * Indica si se debe firmar digitalmente el XML.
         * Por defecto es true. Si es false, retorna el XML sin firma.
         */
        @JsonProperty("firmar")
        Boolean firmar
) {
    /**
     * Indica si el XML debe ser firmado.
     * Por defecto true (comportamiento anterior).
     */
    public boolean debeFirmar() {
        return firmar == null || firmar;
    }
}
