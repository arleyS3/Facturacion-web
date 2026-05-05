package com.facturacion.api.application.comprobante.modelo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Modelo canónico de comprobante para generación UBL.
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
 */
public record ComprobanteCanonico(
        @NotBlank(message = "Tipo de documento es requerido")
        @Size(max = 2, message = "Tipo de documento máximo 2 caracteres")
        String tipoDocumento,

        @NotBlank(message = "Número de comprobante es requerido")
        @Size(max = 20, message = "Número máximo 20 caracteres")
        String numero,

        @NotBlank(message = "Fecha de emisión es requerida")
        @Size(max = 10, message = "Fecha formato YYYY-MM-DD")
        String fechaEmision,

        @Size(max = 8, message = "Hora formato HH:mm:ss")
        String horaEmision,

        @Size(max = 10, message = "Fecha formato YYYY-MM-DD")
        String fechaVencimiento,

        @Size(max = 4, message = "Tipo de operación máximo 4 caracteres")
        String tipoDeOperacion,

        @NotBlank(message = "Moneda es requerida")
        @Size(max = 3, message = "Código de moneda máximo 3 caracteres")
        String moneda,

        @NotBlank(message = "RUC del emisor es requerido")
        @Size(min = 11, max = 11, message = "RUC debe tener 11 dígitos")
        String emisorRuc,

        @NotBlank(message = "Razón social del emisor es requerida")
        @Size(max = 200, message = "Razón social máximo 200 caracteres")
        String emisorRazonSocial,

        @Size(max = 200, message = "Nombre comercial máximo 200 caracteres")
        String emisorNombreComercial,

        @Size(max = 500, message = "Dirección máximo 500 caracteres")
        String emisorDireccion,

        @Size(max = 200, message = "Urbanización máximo 200 caracteres")
        String emisorUrbanizacion,

        @Size(max = 100, message = "Departamento máximo 100 caracteres")
        String emisorDepartamento,

        @Size(max = 100, message = "Provincia máximo 100 caracteres")
        String emisorProvincia,

        @Size(max = 100, message = "Distrito máximo 100 caracteres")
        String emisorDistrito,

        @Size(max = 6, message = "Ubigeo máximo 6 caracteres")
        String emisorUbigeo,

        @Size(max = 4, message = "Código de domicilio máximo 4 caracteres")
        String emisorCodigoDomicilio,

        @NotBlank(message = "Documento del receptor es requerido")
        @Size(min = 4, max = 20, message = "Documento receptor debe tener entre 4 y 20 caracteres")
        String receptorDocumento,

        @Size(max = 200, message = "Razón social receptor máximo 200 caracteres")
        String receptorRazonSocial,

        @Size(max = 500, message = "Dirección receptor máximo 500 caracteres")
        String receptorDireccion,

        @Size(max = 6, message = "Ubigeo receptor máximo 6 caracteres")
        String receptorUbigeo,

        @NotEmpty(message = "Debe incluir al menos un detalle")
        List<DetalleCanonico> detalles,

        DocumentoRelacionadoCanonico documentoRelacionado,

        ParteTrasladoCanonico parteTraslado
) {
}
