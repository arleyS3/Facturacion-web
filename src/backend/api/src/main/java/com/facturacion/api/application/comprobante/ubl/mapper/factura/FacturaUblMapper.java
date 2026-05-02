package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper de comprobante canónico a datos UBL de factura.
 */
@Component
public class FacturaUblMapper {

    /**
     * Convierte el comprobante canónico a {@link FacturaUblData}.
     *
     * @param canonico comprobante canónico
     * @return datos UBL de factura
     */
    public FacturaUblData fromCanonico(ComprobanteCanonico canonico) {
        String[] numeros = canonico.numero() != null ? canonico.numero().split("-") : new String[]{"F001", "1"};
        String serie = numeros.length > 0 ? numeros[0] : "F001";
        String correlativo = numeros.length > 1 ? numeros[1] : "1";

        List<FacturaLineaUblData> lineas = canonico.detalles() != null
            ? canonico.detalles().stream()
                .map(this::mapLinea)
                .toList()
            : List.of();

        return new FacturaUblData(
            serie,
            correlativo,
            canonico.fechaEmision(),
            null,
            canonico.moneda(),
            canonico.tipoDocumento(),
            canonico.emisorRuc(),
            "6",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "PE",
            canonico.receptorDocumento(),
            null,
            null,
            null,
            null,
            "PE",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            lineas
        );
    }

    /**
     * Mapea una línea canónica a línea UBL de factura.
     *
     * @param detalle detalle canónico
     * @return línea UBL
     */
    private FacturaLineaUblData mapLinea(DetalleCanonico detalle) {
        return new FacturaLineaUblData(
            null,
            null,
            null,
            detalle.descripcion(),
            detalle.cantidad(),
            "NIU",
            detalle.valorUnitario(),
            null,
            null,
            detalle.igv(),
            new java.math.BigDecimal("18.00"),
            detalle.codigoTipoIgv(),
            null,
            null,
            null,
            null
        );
    }
}
