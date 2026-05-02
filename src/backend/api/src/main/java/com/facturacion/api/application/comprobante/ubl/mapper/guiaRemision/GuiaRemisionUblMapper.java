package com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import org.springframework.stereotype.Component;

/**
 * Mapper de comprobante canónico a datos UBL de guía de remisión.
 *
 * <p>La implementación completa depende de la definición final de campos.</p>
 */
@Component
public class GuiaRemisionUblMapper {

    /**
     * Convierte el comprobante canónico a {@link GuiaRemisionUblData}.
     *
     * @param canonico comprobante canónico
     * @return datos UBL o null si el mapeo no está implementado
     */
    public GuiaRemisionUblData fromCanonico(ComprobanteCanonico canonico) {
        return null;
    }
}
