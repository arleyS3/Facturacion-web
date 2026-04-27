package com.facturacion.api.application.comprobante.ubl.mapper.boleta;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper de comprobante canónico a datos UBL de boleta.
 */
@Component
public class BoletaUblMapper {

    /**
     * Convierte el comprobante canónico a {@link BoletaUblData}.
     *
     * @param canonico comprobante canónico
     * @return datos UBL de boleta
     */
    public BoletaUblData fromCanonico(ComprobanteCanonico canonico) {
        String[] numeros = canonico.numero() != null ? canonico.numero().split("-") : new String[]{"B001", "1"};
        String serie = numeros.length > 0 ? numeros[0] : "B001";
        String correlativo = numeros.length > 1 ? numeros[1] : "1";

        List<BoletaLineaUblData> lineas = canonico.detalles() != null
            ? canonico.detalles().stream()
                .map(this::mapLinea)
                .toList()
            : List.of();

        return new BoletaUblData(
            serie,
            correlativo,
            canonico.fechaEmision(),
            canonico.moneda(),
            canonico.emisorRuc(),
            "6",
            null,
            canonico.receptorDocumento(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null, lineas
        );
    }

    /**
     * Mapea una línea canónica a línea UBL de boleta.
     *
     * @param detalle detalle canónico
     * @return línea UBL
     */
    private BoletaLineaUblData mapLinea(DetalleCanonico detalle) {
        return new BoletaLineaUblData(
            null,
            null,
            detalle.descripcion(),
            detalle.cantidad(),
            "NIU",
            detalle.igv(),
            new BigDecimal("18.00"),
            detalle.codigoTipoIgv(),
            detalle.valorUnitario(),
            null
        );
    }
}
