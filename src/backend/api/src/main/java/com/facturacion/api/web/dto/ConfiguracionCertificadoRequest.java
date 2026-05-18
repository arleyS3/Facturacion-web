package com.facturacion.api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request para guardar la configuración del certificado digital.
 * 
 * <p>
 * Este DTO recibe el archivo .pfx en Base64 y la contraseña en texto plano.
 * El backend se encarga de encriptar la contraseña antes de guardarla en la BD.
 * </p>
 */
@Getter
@Setter
@Schema(description = "Solicitud para guardar configuración de certificado digital")
public class ConfiguracionCertificadoRequest {

    @NotBlank(message = "El RUC del emisor es requerido")
    @Size(min = 11, max = 11, message = "El RUC debe tener exactamente 11 caracteres")
    @Schema(description = "RUC del emisor (11 dígitos)", example = "20123456789", required = true)
    private String rucEmisor;

    @NotBlank(message = "El certificado en Base64 es requerido")
    @Schema(description = "Certificado .pfx codificado en Base64", example = "MIIO...", required = true)
    private String certificadoBase64;

    @NotBlank(message = "La contraseña del certificado es requerida")
    @Schema(description = "Contraseña del archivo .pfx", example = "MiContraseña123", required = true)
    private String password;

    @Schema(description = "Alias del certificado (opcional)", example = "certificado-empresa")
    private String aliasCertificado;

    @Schema(description = "Fecha de vigencia del certificado (opcional)", example = "2027-12-31")
    private String fechaVigencia;
}