package com.facturacion.api.web.dto;

/**
 * Respuesta con información de una Resolución de Superintendencia SUNAT.
 *
 * @param numero    número correlativo de la resolución (ej. "000401-2025")
 * @param sumilla   descripción o asunto de la resolución
 * @param fecha     fecha de publicación en El Peruano (dd/MM/yyyy o vacío si no aplica)
 * @param urlPdf    enlace absoluto al PDF de la resolución
 * @param urlAnexo  enlace absoluto al PDF del anexo (null si no tiene)
 * @param categoria categoría normativa
 * @param stale     indica si los datos pueden estar desactualizados respecto a SUNAT
 */
public record ResolucionSunatResponse(
    String numero,
    String sumilla,
    String fecha,
    String urlPdf,
    String urlAnexo,
    String categoria,
    boolean stale
) {}
