package com.facturacion.api.application.comprobante.ubl.mapper.factura;

/**
 * Datos de encabezado para la factura UBL.
 *
 * @param serie serie del comprobante
 * @param correlativo correlativo del comprobante
 * @param horaEmision hora de emisión
 * @param fechaEmision fecha de emisión
 * @param fechaVencimiento fecha de vencimiento
 * @param tipoDeOperacion código de tipo de operación
 * @param moneda código de moneda
 * @param tipoDocumento código de tipo de documento
 */
public record DatosEncabezadoFacturaUbl(
    String serie,
    String correlativo,
    String horaEmision,
    String fechaEmision,
    String fechaVencimiento,
    String tipoDeOperacion,
    String moneda,
    String tipoDocumento
) {}
