package com.facturacion.api.application.comprobante.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Anticipo recibido aplicado al comprobante.
 * <p>
 * Corresponde al elemento PrepaidPayment de UBL 2.1.
 * Opcional — solo se envía cuando el frontend provee el dato.
 * </p>
 *
 * @param id identificador del anticipo
 * @param tipoDocumento tipo de documento del anticipo (catálogo 12)
 * @param monto monto del anticipo
 * @param moneda código de moneda del anticipo
 * @param rucEmisor RUC del emisor del anticipo
 */
public record AnticipoCanonico(
        String id,

        @JsonProperty("tipo_documento")
        String tipoDocumento,

        BigDecimal monto,

        String moneda,

        @JsonProperty("ruc_emisor")
        String rucEmisor
) {}
