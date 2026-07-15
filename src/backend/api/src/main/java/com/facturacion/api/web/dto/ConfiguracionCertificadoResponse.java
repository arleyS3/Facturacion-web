package com.facturacion.api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response con la configuración del certificado digital.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta con la configuración del certificado")
public class ConfiguracionCertificadoResponse {

    @Schema(description = "ID del registro en la BD", example = "1")
    private Long id;

    @Schema(description = "RUC del emisor", example = "20123456789")
    private String rucEmisor;

    @Schema(description = "Alias del certificado", example = "certificado-empresa")
    private String aliasCertificado;

    @Schema(description = "Indica si el certificado está activo", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha de vigencia del certificado", example = "2027-12-31T23:59:59")
    private String fechaVigencia;

    @Schema(description = "Fecha de creación del registro", example = "2026-05-11T10:30:00")
    private String creadoAt;

    @Schema(description = "Mensaje de éxito", example = "Certificado guardado exitosamente")
    private String mensaje;
}