package com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos UBL 2.1 para guía de remisión.
 *
 * @param serie serie del comprobante
 * @param correlativo correlativo del comprobante
 * @param fechaEmision fecha de emisión
 * @param fechaTraslado fecha de traslado
 * @param tipoDocumento código de tipo de documento
 * @param motivoTraslado motivo del traslado
 * @param modalidadTraslado modalidad del traslado
 * @param emisorNroDocumento número de documento del emisor
 * @param emisorRazonSocial razón social del emisor
 * @param emisorTipoDocumento tipo de documento del emisor
 * @param receptorNroDocumento número de documento del receptor
 * @param receptorRazonSocial razón social del receptor
 * @param receptorTipoDocumento tipo de documento del receptor
 * @param destinatarioNroDocumento número de documento del destinatario
 * @param destinatarioRazonSocial razón social del destinatario
 * @param destinatarioTipoDocumento tipo de documento del destinatario
 * @param puntoPartidaUbigeo ubigeo de partida
 * @param puntoPartidaDireccion dirección de partida
 * @param puntoPartidaUrbanizacion urbanización de partida
 * @param puntoLlegadaUbigeo ubigeo de llegada
 * @param puntoLlegadaDireccion dirección de llegada
 * @param puntoLlegadaUrbanizacion urbanización de llegada
 * @param transportistaNroDocumento documento del transportista
 * @param transportistaRazonSocial razón social del transportista
 * @param transportistaTipoDocumento tipo de documento del transportista
 * @param transportistaPlacaVehiculo placa del vehículo
 * @param transportistaMarcaVehiculo marca del vehículo
 * @param conductorNroDocumento documento del conductor
 * @param conductorTipoDocumento tipo de documento del conductor
 * @param pesoBruto peso bruto
 * @param unidadMedidaPeso unidad de medida del peso
 * @param numeroBultos número de bultos
 * @param lineas líneas de detalle
 */
public record GuiaRemisionUblData(
        String serie,
        String correlativo,
        String fechaEmision,
        String horaEmision,
        String fechaTraslado,
        String tipoDocumento,
        String motivoTraslado,
        String modalidadTraslado,
        String emisorNroDocumento,
        String emisorRazonSocial,
        String emisorTipoDocumento,
        String receptorNroDocumento,
        String receptorRazonSocial,
        String receptorTipoDocumento,
        String destinatarioNroDocumento,
        String destinatarioRazonSocial,
        String destinatarioTipoDocumento,
        String puntoPartidaUbigeo,
        String puntoPartidaDireccion,
        String puntoPartidaUrbanizacion,
        String puntoLlegadaUbigeo,
        String puntoLlegadaDireccion,
        String puntoLlegadaUrbanizacion,
        String transportistaNroDocumento,
        String transportistaRazonSocial,
        String transportistaTipoDocumento,
        String transportistaPlacaVehiculo,
        String transportistaMarcaVehiculo,
        String conductorNroDocumento,
        String conductorTipoDocumento,
        String conductorNombres,
        String conductorApellidos,
        String conductorLicencia,
        BigDecimal pesoBruto,
        String unidadMedidaPeso,
        Integer numeroBultos,
        String puertoCodigo,
        String puertoNombre,
        String contenedorId,
        String precintoId,
        String docRefTransporteId,
        String docRefTransporteTipo,
        String docRefTransporteEmisor,
        List<String> indicadores,
        List<GuiaRemisionLineaUblData> lineas
) {
}
