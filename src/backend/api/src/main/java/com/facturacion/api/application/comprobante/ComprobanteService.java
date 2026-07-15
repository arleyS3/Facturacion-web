package com.facturacion.api.application.comprobante;

import com.facturacion.api.application.comprobante.formato.GeneradorComprobante;
import com.facturacion.api.web.dto.GenerarTramaRequest;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de aplicación para generar comprobantes en distintos formatos.
 */
@Service
public class ComprobanteService {

    private final Map<FormatoSalidaComprobante, GeneradorComprobante> generadores;

    /**
     * Crea el servicio con los generadores disponibles.
     *
     * @param generadorList lista de generadores registrados
     */
    public ComprobanteService(List<GeneradorComprobante> generadorList) {
        this.generadores = new EnumMap<>(FormatoSalidaComprobante.class);
        for (GeneradorComprobante generador : generadorList) {
            this.generadores.put(generador.formato(), generador);
        }
    }

    /**
     * Genera un comprobante en el formato solicitado.
     *
     * @param request solicitud de generación
     * @param formato formato de salida
     * @return resultado generado
     * @throws IllegalArgumentException si el formato no está soportado
     * @throws Exception si ocurre un error en la generación
     */
    public ResultadoComprobante generar(GenerarTramaRequest request, FormatoSalidaComprobante formato) throws Exception {
        GeneradorComprobante generador = generadores.get(formato);
        if (generador == null) {
            throw new IllegalArgumentException("Formato no soportado: " + formato);
        }
        return generador.generar(request);
    }
}
