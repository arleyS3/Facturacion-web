package com.facturacion.api.application.comprobante.modelo;

import java.math.BigDecimal;

/**
 * Datos canónicos de traslado para guía de remisión.
 *
 * @param motivoTraslado motivo del traslado
 * @param modalidadTraslado modalidad de traslado
 * @param puntoPartida punto de partida
 * @param puntoLlegada punto de llegada
 * @param puntoPartidaUbigeo ubigeo de partida
 * @param puntoPartidaDireccion dirección de partida
 * @param puntoPartidaUrbanizacion urbanización de partida
 * @param puntoLlegadaUbigeo ubigeo de llegada
 * @param puntoLlegadaDireccion dirección de llegada
 * @param puntoLlegadaUrbanizacion urbanización de llegada
 * @param transportistaNroDocumento documento del transportista
 * @param transportistaRazonSocial razón social del transportista
 * @param transportistaTipoDocumento tipo de documento del transportista
 * @param placaVehiculo placa del vehículo
 * @param marcaVehiculo marca del vehículo
 * @param conductorNroDocumento documento del conductor
 * @param conductorTipoDocumento tipo de documento del conductor
 * @param pesoBruto peso bruto
 * @param unidadMedidaPeso unidad de peso
 * @param numeroBultos número de bultos
 * @param fechaTraslado fecha de traslado
 */
public record ParteTrasladoCanonico(
        String motivoTraslado,
        String modalidadTraslado,
        String puntoPartida,
        String puntoLlegada,
        String puntoPartidaUbigeo,
        String puntoPartidaDireccion,
        String puntoPartidaUrbanizacion,
        String puntoLlegadaUbigeo,
        String puntoLlegadaDireccion,
        String puntoLlegadaUrbanizacion,
        String transportistaNroDocumento,
        String transportistaRazonSocial,
        String transportistaTipoDocumento,
        String placaVehiculo,
        String marcaVehiculo,
        String conductorNroDocumento,
        String conductorTipoDocumento,
        BigDecimal pesoBruto,
        String unidadMedidaPeso,
        Integer numeroBultos,
        String fechaTraslado
) {
}
