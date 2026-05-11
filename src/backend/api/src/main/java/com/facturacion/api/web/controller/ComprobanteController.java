package com.facturacion.api.web.controller;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.strategy.UblDocumentoStrategy;
import com.facturacion.api.web.Examples.ComprobanteExample;
import com.facturacion.api.web.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para generar comprobantes electrónicos en formato UBL 2.1.
 *
 * <p>
 * Utiliza el patrón Strategy para permitir generar diferentes tipos de
 * documentos electrónicos (factura, boleta, nota de crédito, etc.) mediante una
 * única interfaz.
 * </p>
 *
 * <pre>{@code
 * POST /api/v1/comprobantes/generar-xml
 * {
 *   "tipoDocumento": "01",
 *   "numero": "F001-1",
 *   "fechaEmision": "2026-05-01",
 *   "moneda": "PEN",
 *   "emisorRuc": "20123456789",
 *   "receptorDocumento": "20456789001",
 *   "detalles": [...]
 * }
 * }</pre>
 */
@RestController
@RequestMapping("/api/v1/comprobantes")
@Tag(name = "Comprobantes UBL", description = "Generación de XML UBL 2.1 para comprobantes electrónicos")
@Slf4j
public class ComprobanteController {

    /**
     * Mapa de estrategias por código SUNAT.
     * Permite seleccionar dinámicamente la estrategia correcta según el tipo de
     * documento.
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
                        Function.identity()));
    }

    /**
     * Genera el XML UBL 2.1 del comprobante electrónico.
     *
     * <p>
     * Endpoint dinámico que detecta automáticamente el tipo de documento a
     * partir del campo 'tipoDocumento' en el cuerpo de la solicitud.
     * </p>
     *
     * <p>
     * Códigos SUNAT soportados:
     * <ul>
     * <li>01 - Factura Electrónica</li>
     * <li>03 - Boleta de Venta Electrónica</li>
     * <li>07 - Nota de Crédito</li>
     * <li>08 - Nota de Débito</li>
     * <li>09 - Guía de Remisión Electrónica</li>
     * </ul>
     * </p>
     *
     * @param canonico Datos del comprobante canónico
     * @return Mapa con el XML generado y metadatos
     */
    @Operation(summary = "Generar XML UBL 2.1",
            description = "Genera el XML UBL 2.1 del comprobante electrónico según el tipo de documento especificado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "XML generado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class),
                            examples = {
                                    @ExampleObject(name = "Factura generada",
                                            value = "{\"success\":true,\"tipoDocumento\":\"01\",\"xml\":\"<Invoice>...\"}"),
                                    @ExampleObject(name = "Boleta generada",
                                            value = "{\"success\":true,\"tipoDocumento\":\"03\",\"xml\":\"<Invoice>...\"}")})),
            @ApiResponse(responseCode = "400", description = "Request inválido o tipo de documento no soportado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno al generar XML",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/generar-xml", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> generarXml(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos del comprobante canónico",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "Factura (01)", description = "Factura Electrónica",
                                            value = ComprobanteExample.FACTURA_REQUEST),
                                    @ExampleObject(name = "Boleta (03)", description = "Boleta de Venta",
                                            value = ComprobanteExample.BOLETA_REQUEST),
                                    @ExampleObject(name = "Nota de Crédito (07)",
                                            value = ComprobanteExample.NOTA_CREDITO_REQUEST),
                                    @ExampleObject(name = "Nota de Débito (08)",
                                            value = ComprobanteExample.NOTA_DEBITO_REQUEST),
                                    @ExampleObject(name = "Guía de Remisión (09)",
                                            value = ComprobanteExample.GUIA_REMISION_REQUEST)
                            }))
            @Valid @RequestBody ComprobanteCanonico canonico) {
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
                    "xml", xml);
        } catch (Exception e) {
            log.error("Error al generar XML: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage());
        }
    }
}
