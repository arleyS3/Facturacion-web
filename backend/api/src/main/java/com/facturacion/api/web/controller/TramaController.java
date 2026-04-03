package com.facturacion.api.web.controller;

import com.facturacion.api.application.TramaImportService;
import com.facturacion.api.application.TramaService;
import com.facturacion.api.application.TipoDocumentoMapper;
import com.facturacion.api.web.dto.GenerarTramaRequest;
import com.facturacion.api.web.dto.GenerarTramaResponse;
import com.facturacion.api.web.dto.ImportarTramaResponse;
import com.facturacion.api.web.error.ErrorResponse;
import com.facturacion.api.web.Examples.TramaExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST para operaciones relacionadas con tramas TXT.
 *
 * Exposa endpoints para:
 * - generar una trama a partir de un payload por secciones (JSON),
 * - descargar la trama como archivo .txt,
 * - importar un archivo TXT y convertirlo al payload por secciones.
 */
@RestController
@RequestMapping("/api/v1/tramas")
@Tag(name = "Tramas", description = "Generación/descarga/importación de tramas TXT por secciones")
public class TramaController {

    private final TramaService tramaService;
    private final TramaImportService tramaImportService;

    public TramaController(TramaService tramaService, TramaImportService tramaImportService) {
        this.tramaService = tramaService;
        this.tramaImportService = tramaImportService;
    }

        @Operation(
            summary = "Generar trama TXT (en JSON)",
                description = "Genera la trama TXT a partir del payload por secciones (campos + listas). " +
                    "Devuelve la trama como string dentro de un JSON (útil para previsualizar en frontend). " +
                    "\n\nNotas: " +
                    "\n- tipoDocumento acepta códigos SUNAT (01 Factura, 03 Boleta, 07 Nota de Crédito, 08 Nota de Débito, 09 Guía de Remisión) y algunos alias (por ejemplo 'Factura', 'Boleta')." +
                    "\n- En secciones.listas.B cada item debe tener un NroLinDet (el índice se respeta en la exportación)."
        )
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trama generada"),
            @ApiResponse(
                responseCode = "400",
                description = "Request inválido o validación de negocio fallida",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
            )
        })
    @PostMapping(value = "/generar", produces = MediaType.APPLICATION_JSON_VALUE)
        public GenerarTramaResponse generar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                required = true,
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = {
                            @ExampleObject(
                                name = "Factura (01) - exonerada simple",
                                description = "Factura con 1 item exonerado (CodigoTipoIgv=9998).",
                                value = TramaExample.FACTURA_EXONERADA_SIMPLE
                            ),
                            @ExampleObject(
                                name = "Boleta (03) - alias tipoDocumento",
                                description = "tipoDocumento puede enviarse como alias 'Boleta'.",
                                value = TramaExample.BOLETA_ALIAS
                            ),
                            @ExampleObject(
                                name = "Nota de Crédito (07) - esquema base",
                                description = "Mismo payload base por secciones; reglas específicas de 07 se irán ajustando conforme a necesidad.",
                                value = TramaExample.NOTA_CREDITO_BASE
                            ),
                            @ExampleObject(
                                name = "Nota de Débito (08) - esquema base",
                                value = TramaExample.NOTA_DEBITO_BASE
                            ),
                            @ExampleObject(
                                name = "Guía de Remisión (09) - mínima",
                                description = "Mínimo para GR: sección A común + sección G con motivo/modalidad. Secciones adicionales se envían según el caso.",
                                value = TramaExample.GUIA_REMISION_MINIMA
                            )
                        }
                )
            )
            @Valid @RequestBody GenerarTramaRequest request
        ) {
        String trama = tramaService.generarTrama(request);
        return new GenerarTramaResponse(request.tipoDocumento(), trama);
    }

        @Operation(
            summary = "Descargar trama TXT (.txt)",
            description = "Genera la trama y la devuelve como archivo de texto (Content-Disposition: attachment). Ideal para botón 'Descargar'."
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "Archivo TXT generado",
                content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Request inválido o validación de negocio fallida",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
            )
        })
    @PostMapping(value = "/descargar", produces = MediaType.TEXT_PLAIN_VALUE)
        public ResponseEntity<String> descargar(@Valid @RequestBody GenerarTramaRequest request) {
        String trama = tramaService.generarTrama(request);

        var camposA = request.secciones() != null && request.secciones().campos() != null
            ? request.secciones().campos().get("A")
            : null;
        String rucEmisor = sanitizeFilenamePart(camposA != null ? camposA.get("RUTEmis") : null, "sin-ruc");
        String tipoDoc = sanitizeFilenamePart(TipoDocumentoMapper.toCodigoSunat(request.tipoDocumento()), "00");
        String serie = sanitizeFilenamePart(camposA != null ? camposA.get("Serie") : null, "S000");
        String correlativo = sanitizeFilenamePart(camposA != null ? camposA.get("Correlativo") : null, "00000000");

        String filename = rucEmisor + "-" + tipoDoc + "-" + serie + "-" + correlativo + ".txt";
        var disposition = ContentDisposition.attachment().filename(filename).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.TEXT_PLAIN)
                .body(trama);
    }

    private static String sanitizeFilenamePart(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim().replaceAll("[^A-Za-z0-9_-]", "-");
    }

            @Operation(
                summary = "Importar trama TXT (.txt) y convertir a JSON por secciones",
                description = "Recibe un archivo TXT (multipart/form-data) y devuelve el tipo de documento detectado y el payload por secciones (campos + listas)."
            )
            @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Trama importada"),
                @ApiResponse(
                    responseCode = "400",
                    description = "Archivo inválido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
                )
            })
    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ImportarTramaResponse importar(@RequestPart("file") MultipartFile file) throws Exception {
        var result = tramaImportService.importar(file.getInputStream());
        return new ImportarTramaResponse(result.tipoDocumento(), result.secciones());
    }
}
