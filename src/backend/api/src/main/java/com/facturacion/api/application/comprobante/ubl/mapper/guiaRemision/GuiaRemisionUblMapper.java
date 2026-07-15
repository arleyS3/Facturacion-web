package com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import com.facturacion.api.application.comprobante.modelo.ParteTrasladoCanonico;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper de comprobante canónico a datos UBL de guía de remisión.
 */
@Component
public class GuiaRemisionUblMapper {

    /**
     * Convierte el comprobante canónico a {@link GuiaRemisionUblData}.
     */
    public GuiaRemisionUblData fromCanonico(ComprobanteCanonico canonico) {
        if (canonico == null) {
            return null;
        }

        // 1. Manejo de Serie y Correlativo (Ej: T001-000001)
        String[] numeros = canonico.numero() != null ? canonico.numero().split("-") : new String[]{"T001", "1"};
        String serie = numeros.length > 0 ? numeros[0] : "T001";
        String correlativo = numeros.length > 1 ? numeros[1] : "1";

        // 2. Extraer datos de traslado (Es obligatorio para la Guía)
        ParteTrasladoCanonico traslado = canonico.parteTraslado();
        if (traslado == null) {
            throw new IllegalArgumentException("La sección parte_traslado es obligatoria para generar la Guía de Remisión");
        }

        // 3. Mapeo de Detalles/Lineas
        List<GuiaRemisionLineaUblData> lineas = new ArrayList<>();
        if (canonico.detalles() != null) {
            int numeroLinea = 1;
            for (DetalleCanonico det : canonico.detalles()) {
                lineas.add(new GuiaRemisionLineaUblData(
                        numeroLinea++,
                        det.codigoProducto(),
                        det.descripcion(),
                        det.cantidad(),
                        det.unidadMedida() != null ? det.unidadMedida() : "NIU"
                ));
            }
        }

        // 4. Construir y retornar el Data Record
        return new GuiaRemisionUblData(
                serie,
                correlativo,
                canonico.fechaEmision(),
                traslado.fechaTraslado(),
                canonico.tipoDocumento() != null ? canonico.tipoDocumento() : "09", // 09 = Guía de Remisión Remitente
                traslado.tipoTraslado(), // Catálogo 20
                traslado.modalidadTraslado(), // Catálogo 18 (01=Público, 02=Privado)
                canonico.emisorRuc(),
                canonico.emisorRazonSocial(),
                "6", // RUC (Catálogo 06)
                canonico.receptorDocumento(),
                canonico.receptorRazonSocial(),
                "6", // RUC Receptor
                canonico.receptorDocumento(), // Destinatario (mismo que receptor en el modelo canónico)
                canonico.receptorRazonSocial(),
                "6",
                traslado.puntoPartidaUbigeo(),
                traslado.puntoPartidaDireccion(),
                traslado.puntoPartidaUrbanizacion(),
                traslado.puntoLlegadaUbigeo(),
                traslado.puntoLlegadaDireccion(),
                traslado.puntoLlegadaUrbanizacion(),
                traslado.transportistaNroDocumento(),
                traslado.transportistaRazonSocial(),
                traslado.transportistaTipoDocumento(),
                traslado.placaVehiculo(),
                traslado.marcaVehiculo(),
                traslado.conductorNroDocumento(),
                traslado.conductorTipoDocumento(),
                traslado.pesoBrutoTotal(),
                traslado.undPesoTotal() != null ? traslado.undPesoTotal() : "KGM",
                traslado.numeroBultos(),
                lineas
        );
    }
}
