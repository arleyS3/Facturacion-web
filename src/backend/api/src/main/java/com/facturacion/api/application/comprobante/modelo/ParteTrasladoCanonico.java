package com.facturacion.api.application.comprobante.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Datos canónicos de traslado para guía de remisión.
 * 
 * <p>
 * Ejemplo JSON con snake_case:
 * </p>
 * <pre>
 * {
 *   "tipo_traslado": "01",
 *   "modalidad_traslado": "01",
 *   "peso_bruto_total": 25.50,
 *   "und_peso_total": "KGM",
 *   "numero_bultos": 3
 * }
 * </pre>
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
        @JsonProperty("tipo_traslado")
        String tipoTraslado,
        
        @JsonProperty("modalidad_traslado")
        String modalidadTraslado,
        
        @JsonProperty("punto_partida")
        String puntoPartida,
        
        @JsonProperty("punto_llegada")
        String puntoLlegada,
        
        @JsonProperty("punto_partida_ubigeo")
        String puntoPartidaUbigeo,
        
        @JsonProperty("punto_partida_direccion")
        String puntoPartidaDireccion,
        
        @JsonProperty("punto_partida_urbanizacion")
        String puntoPartidaUrbanizacion,
        
        @JsonProperty("punto_llegada_ubigeo")
        String puntoLlegadaUbigeo,
        
        @JsonProperty("punto_llegada_direccion")
        String puntoLlegadaDireccion,
        
        @JsonProperty("punto_llegada_urbanizacion")
        String puntoLlegadaUrbanizacion,
        
        @JsonProperty("transportista_nro_documento")
        String transportistaNroDocumento,
        
        @JsonProperty("transportista_razonSocial")
        String transportistaRazonSocial,
        
        @JsonProperty("transportista_tipo_documento")
        String transportistaTipoDocumento,
        
        @JsonProperty("placa_vehiculo")
        String placaVehiculo,
        
        @JsonProperty("marca_vehiculo")
        String marcaVehiculo,
        
        @JsonProperty("conductor_nro_documento")
        String conductorNroDocumento,
        
        @JsonProperty("conductor_tipo_documento")
        String conductorTipoDocumento,
        
        @JsonProperty("peso_bruto_total")
        BigDecimal pesoBrutoTotal,
        
        @JsonProperty("und_peso_total")
        String undPesoTotal,
        
        @JsonProperty("numero_bultos")
        Integer numeroBultos,
        
        @JsonProperty("fecha_traslado")
        String fechaTraslado
) {
}