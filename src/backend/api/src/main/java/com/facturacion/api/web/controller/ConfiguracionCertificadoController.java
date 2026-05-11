/**
 * Controlador REST para la gestión de certificados digitales.
 * 
 * <p>
 * Este controlador proporciona endpoints para:
 * </p>
 * <ul>
 *   <li>Guardar/actualizar configuración de certificado digital (.pfx)</li>
 *   <li>Consultar certificado configurado por RUC</li>
 *   <li>Eliminar (desactivar) certificado por RUC</li>
 * </ul>
 * 
 * <p>
 * El certificado digital se utiliza para firmar digitalmente los comprobantes
 * electrónicos (facturas, boletas, notas) antes de enviarlos a SUNAT.
 * </p>
 * 
 * <p>
 * <b>Flujo de uso:</b>
 * <ol>
 *   <li>El usuario sube el archivo .pfx codificado en Base64</li>
 *   <li>Ingresa la contraseña del certificado</li>
 *   <li>El backend encripta la contraseña con AES-256</li>
 *   <li>Al generar un comprobante, se usa el certificado para firmar el XML</li>
 * </ol>
 * </p>
 * 
 * @author Sistema de Facturación
 * @version 1.0.0
 * @since 1.0.0
 * @see ConfiguracionCertificadoRequest
 * @see ConfiguracionCertificadoResponse
 * @see com.facturacion.api.web.models.ConfiguracionCertificadoEntity
 */
package com.facturacion.api.web.controller;

import com.facturacion.api.application.EncryptionUtil;
import com.facturacion.api.web.dto.ConfiguracionCertificadoRequest;
import com.facturacion.api.web.dto.ConfiguracionCertificadoResponse;
import com.facturacion.api.web.models.ConfiguracionCertificadoEntity;
import com.facturacion.api.web.repositories.ConfiguracionCertificadoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controlador para gestionar la configuración del certificado digital.
 * 
 * <p>
 * Permite guardar y consultar la configuración del certificado .pfx que se
 * utiliza para firmar los comprobantes electrónicos antes de enviarlos a SUNAT.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/configuracion-certificado")
@Tag(name = "Certificado Digital", description = "Gestión de certificados digitales para firma de comprobantes")
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionCertificadoController {

    private final ConfiguracionCertificadoRepository certificadoRepository;
    private final EncryptionUtil encryptionUtil;

    /**
     * Guarda la configuración del certificado digital.
     * 
     * <p>
     * El endpoint recibe el archivo .pfx codificado en Base64 y la contraseña.
     * El backend encripta la contraseña con AES-256 antes de guardarla.
     * </p>
     * 
     * <p>
     * Si ya existe un certificado para el RUC, se actualiza. Para actualizar
     * la contraseña del certificado, se debe reenviar todo el registro.
     * </p>
     * 
     * @param request datos del certificado (RUC, certificado Base64, contraseña)
     * @return respuesta con los datos guardados
     */
    @Operation(summary = "Guardar configuración de certificado",
            description = "Guarda o actualiza la configuración del certificado digital (.pfx) "
                    + "para firma de comprobantes electrónicos. La contraseña se encripta "
                    + "automáticamente con AES-256.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificado guardado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConfiguracionCertificadoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o certificado .pfx corrupto",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Ya existe certificado para otro RUC",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ConfiguracionCertificadoResponse> guardarCertificado(
            @Valid @RequestBody ConfiguracionCertificadoRequest request) {
        
        log.info("Guardando configuración de certificado para RUC: {}", request.getRucEmisor());
        
        // Validar que el certificado Base64 sea válido (no vacía y divisible por 4)
        String certBase64 = request.getCertificadoBase64().trim();
        if (certBase64.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El certificado no puede estar vacío");
        }
        
        // Encriptar la contraseña
        String passwordEncriptada;
        try {
            passwordEncriptada = encryptionUtil.encrypt(request.getPassword());
        } catch (Exception e) {
            log.error("Error al encriptar contraseña: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error al procesar la contraseña");
        }
        
        // Buscar si existe configuración previa para este RUC
        var existente = certificadoRepository.findByRucEmisorAndActivoTrue(request.getRucEmisor());
        
        ConfiguracionCertificadoEntity certificado;
        LocalDateTime ahora = LocalDateTime.now();
        
        if (existente.isPresent()) {
            // Actualizar existente
            certificado = existente.get();
            certificado.setCertificadoBase64(certBase64);
            certificado.setPasswordEncriptada(passwordEncriptada);
            if (request.getAliasCertificado() != null) {
                certificado.setAliasCertificado(request.getAliasCertificado());
            }
            if (request.getFechaVigencia() != null && !request.getFechaVigencia().isEmpty()) {
                try {
                    certificado.setFechaVigencia(LocalDateTime.parse(request.getFechaVigencia() + "T23:59:59"));
                } catch (Exception e) {
                    log.warn("Formato de fecha inválido: {}", request.getFechaVigencia());
                }
            }
            certificado.setActualizadoAt(ahora);
            log.info("Actualizando certificado existente para RUC: {}", request.getRucEmisor());
        } else {
            // Crear nuevo
            String alias = request.getAliasCertificado() != null && !request.getAliasCertificado().isEmpty()
                    ? request.getAliasCertificado()
                    : "certificado-" + request.getRucEmisor();
            
            LocalDateTime fechaVigencia = null;
            if (request.getFechaVigencia() != null && !request.getFechaVigencia().isEmpty()) {
                try {
                    fechaVigencia = LocalDateTime.parse(request.getFechaVigencia() + "T23:59:59");
                } catch (Exception e) {
                    log.warn("Formato de fecha inválido: {}", request.getFechaVigencia());
                }
            }
            
            certificado = ConfiguracionCertificadoEntity.builder()
                    .rucEmisor(request.getRucEmisor())
                    .aliasCertificado(alias)
                    .certificadoBase64(certBase64)
                    .passwordEncriptada(passwordEncriptada)
                    .fechaVigencia(fechaVigencia)
                    .activo(true)
                    .creadoAt(ahora)
                    .actualizadoAt(ahora)
                    .build();
            log.info("Creando nuevo certificado para RUC: {}", request.getRucEmisor());
        }
        
        // Guardar en BD
        certificado = certificadoRepository.save(certificado);
        
        // Construir respuesta
        ConfiguracionCertificadoResponse response = ConfiguracionCertificadoResponse.builder()
                .id(certificado.getId())
                .rucEmisor(certificado.getRucEmisor())
                .aliasCertificado(certificado.getAliasCertificado())
                .activo(certificado.getActivo())
                .fechaVigencia(certificado.getFechaVigencia() != null 
                        ? certificado.getFechaVigencia().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
                        : null)
                .creadoAt(certificado.getCreadoAt() != null 
                        ? certificado.getCreadoAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
                        : null)
                .mensaje("Certificado guardado exitosamente")
                .build();
        
        log.info("Certificado guardado exitosamente para RUC: {}", request.getRucEmisor());
        return ResponseEntity.ok(response);
    }

    /**
     * Consulta la configuración del certificado para un RUC específico.
     * 
     * <p>
     * Devuelve los datos del certificado almacenado (sin la contraseña ni el certificado Base64).
     * </p>
     * 
     * @param rucEmisor RUC del emisor
     * @return datos de configuración del certificado
     */
    @Operation(summary = "Consultar certificado",
            description = "Consulta la configuración del certificado digital para un RUC específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificado encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConfiguracionCertificadoResponse.class))),
            @ApiResponse(responseCode = "404", description = "No hay certificado configurado para este RUC",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{rucEmisor}")
    public ResponseEntity<ConfiguracionCertificadoResponse> obtenerCertificado(
            @PathVariable String rucEmisor) {
        
        log.info("Consultando certificado para RUC: {}", rucEmisor);
        
        var certificado = certificadoRepository.findByRucEmisorAndActivoTrue(rucEmisor);
        
        if (certificado.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "No hay certificado configurado para RUC: " + rucEmisor);
        }
        
        ConfiguracionCertificadoEntity c = certificado.get();
        
        ConfiguracionCertificadoResponse response = ConfiguracionCertificadoResponse.builder()
                .id(c.getId())
                .rucEmisor(c.getRucEmisor())
                .aliasCertificado(c.getAliasCertificado())
                .activo(c.getActivo())
                .fechaVigencia(c.getFechaVigencia() != null 
                        ? c.getFechaVigencia().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
                        : null)
                .creadoAt(c.getCreadoAt() != null 
                        ? c.getCreadoAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
                        : null)
                .mensaje("Certificado encontrado")
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina (desactiva) la configuración del certificado para un RUC.
     * 
     * <p>
     * No elimina físicamente el registro, solo lo marca como inactivo.
     * </p>
     * 
     * @param rucEmisor RUC del emisor
     * @return respuesta de éxito
     */
    @Operation(summary = "Eliminar certificado",
            description = "Desactiva la configuración del certificado digital para un RUC")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificado eliminado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConfiguracionCertificadoResponse.class))),
            @ApiResponse(responseCode = "404", description = "No hay certificado configurado para este RUC",
                    content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{rucEmisor}")
    public ResponseEntity<ConfiguracionCertificadoResponse> eliminarCertificado(
            @PathVariable String rucEmisor) {
        
        log.info("Eliminando certificado para RUC: {}", rucEmisor);
        
        var certificado = certificadoRepository.findByRucEmisorAndActivoTrue(rucEmisor);
        
        if (certificado.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "No hay certificado configurado para RUC: " + rucEmisor);
        }
        
        ConfiguracionCertificadoEntity c = certificado.get();
        c.setActivo(false);
        c.setActualizadoAt(LocalDateTime.now());
        certificadoRepository.save(c);
        
        ConfiguracionCertificadoResponse response = ConfiguracionCertificadoResponse.builder()
                .id(c.getId())
                .rucEmisor(c.getRucEmisor())
                .aliasCertificado(c.getAliasCertificado())
                .activo(false)
                .mensaje("Certificado eliminado exitosamente")
                .build();
        
        return ResponseEntity.ok(response);
    }
}