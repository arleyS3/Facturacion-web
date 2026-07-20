package com.facturacion.api.web.controller;

import com.facturacion.api.application.NormativaService;
import com.facturacion.api.web.dto.ResolucionSunatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/normativa")
@Validated
@Tag(name = "Normativa SUNAT", description = "Consulta de resoluciones de superintendencia del portal SUNAT")
public class NormativaController {

    private final NormativaService normativaService;

    public NormativaController(NormativaService normativaService) {
        this.normativaService = normativaService;
    }

    @Operation(summary = "Listar resoluciones SUNAT por año",
            description = "Retorna el índice correlativo de resoluciones de superintendencia "
                    + "del año indicado. Los datos se cachean por 6 horas con verificación "
                    + "asíncrona de vigencia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de resoluciones",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResolucionSunatResponse.class))),
            @ApiResponse(responseCode = "400", description = "Año inválido (debe ser 2000-2099)"),
            @ApiResponse(responseCode = "500", description = "Error al consultar SUNAT")
    })
    @GetMapping(value = "/resoluciones", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ResolucionSunatResponse> listarResoluciones(
            @Parameter(description = "Año (2000-2099)", required = true)
            @RequestParam @Min(2000) @Max(2099) int anio,

            @Parameter(description = "Búsqueda textual en número o sumilla (opcional)")
            @RequestParam(required = false) String q) {

        var todas = normativaService.obtenerResoluciones(anio);
        if (q == null || q.isBlank()) return todas;

        var busqueda = q.toLowerCase();
        return todas.stream()
                .filter(r -> r.numero().toLowerCase().contains(busqueda)
                        || r.sumilla().toLowerCase().contains(busqueda))
                .toList();
    }

    @Operation(summary = "Listar categorías disponibles",
            description = "Retorna las categorías de resoluciones disponibles para el año indicado.")
    @GetMapping(value = "/categorias", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> listarCategorias(
            @Parameter(description = "Año (2000-2099)", required = true)
            @RequestParam @Min(2000) @Max(2099) int anio) {

        return normativaService.obtenerResoluciones(anio).stream()
                .map(ResolucionSunatResponse::categoria)
                .distinct()
                .sorted()
                .toList();
    }
}
