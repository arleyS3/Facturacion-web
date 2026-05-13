package com.facturacion.api.application.comprobante.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Línea canónica del comprobante.
 * 
 * <p>
 * Acepta nombres de campo en formato snake_case (JSON):
 * </p>
 * <pre>
 * {
 *   "codigo_producto": "MED001",
 *   "descripcion": "Medicamento x 30",
 *   "cantidad": 30.00,
 *   "valor_unitario": 50.00,
 *   "igv": 270.00,
 *   "codigo_tipoIgv": "10",
 *   "unidad_medida": "NIU"
 * }
 * </pre>
 *
 * @param codigoProducto código del producto (SKU)
 * @param descripcion descripción del ítem
 * @param cantidad cantidad
 * @param valorUnitario valor unitario
 * @param igv monto de IGV
 * @param codigoTipoIgv código de afectación IGV
 * @param unidadMedida código de unidad de medida (catálogo SUNAT)
 */
public record DetalleCanonico(
        @Size(max = 50, message = "Código de producto máximo 50 caracteres")
        @JsonProperty("codigo_producto")
        String codigoProducto,

        @NotBlank(message = "Descripción del ítem es requerida")
        @Size(max = 500, message = "Descripción máximo 500 caracteres")
        String descripcion,

        @NotNull(message = "Cantidad es requerida")
        @DecimalMin(value = "0.01", message = "Cantidad debe ser mayor a 0")
        BigDecimal cantidad,

        @NotNull(message = "Valor unitario es requerido")
        @DecimalMin(value = "0.01", message = "Valor unitario debe ser mayor a 0")
        @JsonProperty("valor_unitario")
        BigDecimal valorUnitario,

        @NotNull(message = "Monto de IGV es requerido")
        @DecimalMin(value = "0.00", message = "IGV no puede ser negativo")
        BigDecimal igv,

        @NotBlank(message = "Código de afectación IGV es requerido")
        @Size(max = 2, message = "Código máximo 2 caracteres")
        @JsonProperty("codigo_tipoIgv")
        String codigoTipoIgv,

        /**
         * Código de unidad de medida según catálogo SUNAT.
         * Por defecto "NIU" (Unidad).
         */
        @Size(max = 3, message = "Unidad de medida máximo 3 caracteres")
        @JsonProperty("unidad_medida")
        String unidadMedida
) {
}