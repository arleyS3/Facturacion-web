package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import java.util.List;

/**
 * Datos UBL 2.1 para factura electrónica.
 *
 * @param encabezado datos de encabezado
 * @param emisor datos del emisor
 * @param receptor datos del receptor
 * @param firma datos de la firma digital
 * @param referenciaOrden datos de referencia de orden
 * @param totalLineas número total de líneas
 * @param descuentosGlobales descuentos globales
 * @param totales datos de totales e impuestos
 * @param impuestosTotales impuestos totales con subtotales
 * @param totalesMonetarios totales monetarios del comprobante
 * @param percepcionDetraccion datos de percepción y detracción
 * @param lineas líneas de detalle
 * @param leyendas leyendas asociadas
 * @param guiaRemisionId ID de guía de remisión (serie-numero)
 * @param guiaRemisionCodigo código de tipo de guía (catálogo 01)
 * @param documentosAdicionales documentos adicionales referenciados (catálogo 12)
 */
public record FacturaUblData(
    DatosEncabezadoFacturaUbl encabezado,
    DatosEmisorFacturaUbl emisor,
    DatosReceptorFacturaUbl receptor,
    DatosFirmaFacturaUbl firma,
    DatosReferenciaOrdenFacturaUbl referenciaOrden,
    Integer totalLineas,
    List<DatosDescuentoGlobalFacturaUbl> descuentosGlobales,
    DatosTotalesFacturaUbl totales,
    DatosImpuestoTotalFacturaUbl impuestosTotales,
    DatosTotalesMonetariosFacturaUbl totalesMonetarios,
    DatosPercepcionDetraccionFacturaUbl percepcionDetraccion,
    List<FacturaLineaUblData> lineas,
    List<LeyendaUblData> leyendas,
    String guiaRemisionId,
    String guiaRemisionCodigo,
    List<DocumentoAdicionalUblData> documentosAdicionales
) {}
