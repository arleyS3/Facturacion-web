package com.facturacion.api.application.comprobante.modelo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Leyenda del comprobante electrónico.
 *
 * <p>
 * Las leyendas son textos complementarios que aparecen en el comprobante.
 * Cada leyenda tiene un código local que indica el tipo de leyenda (catálogo 52).
 * Ejemplos: monto en letras, información adicional, etc.
 * </p>
 *
 * @param codigoLocal código de leyenda según catálogo SUNAT 52
 * @param leyenda texto de la leyenda
 * @see <a href="https://cpe.sunat.gob.pe/estandares/catalogo-52">Catálogo 52 - Leyendas</a>
 */
public record LeyendaCanonico(
        @NotBlank(message = "Código de leyenda es requerido")
        @Size(max = 4, message = "Código de leyenda máximo 4 caracteres")
        String codigoLocal,

        @NotBlank(message = "Leyenda es requerida")
        @Size(max = 500, message = "Leyenda máximo 500 caracteres")
        String leyenda
) {}