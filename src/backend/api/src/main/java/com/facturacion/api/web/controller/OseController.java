package com.facturacion.api.web.controller;

import com.facturacion.api.application.ose.OseSoapClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador para enviar XML UBL 2.1 al OSE (Operador de Servicios Electr&oacute;nicos).
 * <p>
 * Recibe un archivo .xml y lo reenv&iacute;a al endpoint SOAP del OSE
 * (DBNet / eComprobantes) para su validaci&oacute;n y env&iacute;o a SUNAT.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/ose")
@Tag(name = "OSE", description = "Envío de XML al Operador de Servicios Electrónicos")
@Slf4j
public class OseController {

    private final OseSoapClient oseSoapClient;

    public OseController(OseSoapClient oseSoapClient) {
        this.oseSoapClient = oseSoapClient;
    }

    @Operation(summary = "Enviar XML al OSE",
            description = "Recibe un archivo XML UBL 2.1 y lo envía al OSE para su validación.")
    @PostMapping(value = "/enviar-xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> enviarXml(@RequestParam("archivo") MultipartFile archivo) {
        try {
            String xmlContent = new String(archivo.getBytes(), StandardCharsets.UTF_8);
            log.info("Enviando XML al OSE ({} bytes, nombre={})", xmlContent.length(), archivo.getOriginalFilename());

            String respuestaOse = oseSoapClient.enviarXml(xmlContent);

            return Map.of(
                    "success", true,
                    "resultado", respuestaOse);
        } catch (IOException e) {
            log.error("Error al leer el archivo XML: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", "Error al leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al enviar al OSE: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", "Error al enviar al OSE: " + e.getMessage());
        }
    }
}
