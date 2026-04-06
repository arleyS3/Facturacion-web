package com.facturacion.api.web.dto;

/**
 * Respuesta con información pública del RUC consultado en SUNAT.
 * Todos los campos pueden ser nulos si la información no está disponible.
 *
 * @param nombre razón social o nombre del contribuyente
 * @param tipoDocumento tipo de documento registrado
 * @param numeroDocumento número de documento (RUC)
 * @param estado estado del contribuyente
 * @param condicion condición del contribuyente
 * @param direccion dirección completa
 * @param ubigeo código UBIGEO asociado a la dirección
 * @param viaTipo tipo de vía (Av, Jr, etc.)
 * @param viaNombre nombre de la vía
 * @param zonaCodigo código de la zona
 * @param zonaTipo tipo de zona (Urbanización, etc.)
 * @param numero número de la dirección
 * @param interior interior (departamento/apt)
 * @param lote lote
 * @param dpto departamento interno (si aplica)
 * @param manzana manzana (si aplica)
 * @param kilometro referencia por kilómetro (si aplica)
 * @param distrito distrito
 * @param provincia provincia
 * @param departamento departamento
 */
public record RucResponse(
        String nombre,
        String tipoDocumento,
        String numeroDocumento,
        String estado,
        String condicion,
        String direccion,
        String ubigeo,
        String viaTipo,
        String viaNombre,
        String zonaCodigo,
        String zonaTipo,
        String numero,
        String interior,
        String lote,
        String dpto,
        String manzana,
        String kilometro,
        String distrito,
        String provincia,
        String departamento
) {
}
