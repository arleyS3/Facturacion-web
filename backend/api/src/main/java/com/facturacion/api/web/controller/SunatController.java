package com.facturacion.api.web.controller;

import com.facturacion.api.application.SunatRucService;
import com.facturacion.api.web.dto.RucResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "SUNAT", description = "Consulta de contribuyente por RUC")
public class SunatController {

    private final SunatRucService sunatRucService;

    public SunatController(SunatRucService sunatRucService) {
        this.sunatRucService = sunatRucService;
    }

    @Operation(summary = "Consultar RUC en SUNAT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta exitosa (puede retornar null si el servicio externo no devuelve 200)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RucResponse.class))),
            @ApiResponse(responseCode = "400", description = "RUC inválido")
    })
    @GetMapping(value = "/sunat", produces = MediaType.APPLICATION_JSON_VALUE)
    /** Consulta información de contribuyente por RUC (vía servicio SUNAT externo). */
    public RucResponse consultarRuc(
            @RequestParam("ruc")
            @Pattern(regexp = "^[0-9]{11}$", message = "El RUC debe tener 11 dígitos")
            String ruc
    ) {
        return sunatRucService.consultarPorRuc(ruc);
    }
}
