package com.facturacion.api.web.controller;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.strategy.UblDocumentoStrategy;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para generar comprobantes electrónicos en formato UBL.
 *
 * <p>
 * Utiliza el patrón Strategy para permitir generar diferentes tipos de documentos
 * electrónicos (factura, boleta, nota de crédito, etc.) mediante una única interfaz.
 * </p>
 *
 * <pre>{@code
 * POST /api/v1/comprobantes/generar-xml
 * {
 *   "numero": "F001-1",
 *   "fechaEmision": "2026-05-01",
 *   "moneda": "PEN",
 *   "tipoDocumento": "01",
 *   "emisorRuc": "20123456789",
 *   "receptorDocumento": "20456789001",
 *   "detalles": [...]
 * }
 * }</pre>
 */
@RestController
@RequestMapping("/api/v1/comprobantes")
@Slf4j
public class ComprobanteController {

    /**
     * Mapa de estrategias por código SUNAT.
     * Permite seleccionar dinámicamente la estrategia correcta según el tipo de documento.
     */
    private final Map<String, UblDocumentoStrategy> estrategias;

    /**
     * Constructor que injecta todas las estrategias disponibles.
     *
     * @param estrategias lista de estrategias disponibles en el contexto de Spring
     */
    public ComprobanteController(List<UblDocumentoStrategy> estrategias) {
        // Indexa estrategias por código SUNAT para búsquedas O(1)
        this.estrategias = estrategias.stream()
            .collect(Collectors.toMap(
                UblDocumentoStrategy::codigoSunat,
                Function.identity()
            ));
    }

    /**
     * Genera el XML UBL del comprobante electrónico.
     *
     * <p>
     * Endpoint dinámico que detecta automáticamente el tipo de documento
     * a partir del campo 'tipoDocumento' en el cuerpo de la solicitud.
     * </p>
     *
     * @param canonico Datos del comprobante canónico
     * @return Mapa con el XML generado y metadatos
     */
    @PostMapping("/generar-xml")
    public Map<String, Object> generarXml(@Valid @RequestBody ComprobanteCanonico canonico) {
        // Determina el código SUNAT a usar
        String codigoSunat = canonico.tipoDocumento() != null 
            ? canonico.tipoDocumento() 
            : "01"; // Por defecto Factura

        log.info("Generando XML para tipoDocumento={}", codigoSunat);

        // Obtiene la estrategia correspondiente
        UblDocumentoStrategy estrategia = estrategias.get(codigoSunat);
        if (estrategia == null) {
            throw new IllegalArgumentException(
                "Tipo de documento no soportado: " + codigoSunat);
        }

        try {
            String xml = estrategia.generarXml(canonico);
            return Map.of(
                "success", true,
                "tipoDocumento", codigoSunat,
                "xml", xml
            );
        } catch (Exception e) {
            log.error("Error al generar XML: {}", e.getMessage(), e);
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }
}