package com.facturacion.api.application.comprobante.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Descuento o cargo global a nivel de comprobante (no por línea).
 * <p>
 * Corresponde al elemento AllowanceCharge de la factura UBL 2.1,
 * catálogo SUNAT 53.
 * </p>
 *
 * @param esCargo indica si es cargo (true) o descuento (false)
 * @param codigoMotivo código del motivo según catálogo 53
 * @param porcentaje porcentaje del descuento/cargo
 * @param monto monto del descuento/cargo
 * @param montoBase monto base sobre el que se aplica
 */
public record DescuentoGlobalCanonico(
        @JsonProperty("es_cargo")
        Boolean esCargo,

        @JsonProperty("codigo_motivo")
        String codigoMotivo,

        BigDecimal porcentaje,

        BigDecimal monto,

        @JsonProperty("monto_base")
        BigDecimal montoBase
) {}
