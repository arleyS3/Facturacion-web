package com.facturacion.api.web.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Configuración de certificado digital para firma de comprobantes.
 * 
 * <p>
 * Almacena el certificado .pfx codificado en Base64 y la contraseña
 * encriptada para firmar los XML antes de enviar a SUNAT.
 * </p>
 * 
 * <p>
 * El campo password se debe encriptar con AES antes de guardar.
 * </p>
 */
@Entity
@Table(name = "configuracion_certificado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionCertificadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * RUC del emisor (identificador único).
     * Ej: 20123456789
     */
    @Column(name = "ruc_emisor", nullable = false, unique = true, length = 11)
    private String rucEmisor;

    /**
     * Alias del certificado en el keystore.
     * Ej: "certificado-empresa"
     */
    @Column(name = "alias_certificado", nullable = false, length = 100)
    private String aliasCertificado;

    /**
     * Certificado .pfx codificado en Base64.
     * Se guarda como bytea/texto para evitar manejo de archivos.
     */
    @Column(name = "certificado_base64", columnDefinition = "TEXT")
    private String certificadoBase64;

    /**
     * Contraseña del keystore, encriptada con AES.
     * Nunca guardar en texto plano.
     */
    @Column(name = "password_encriptada", columnDefinition = "TEXT")
    private String passwordEncriptada;

    /**
     * Fecha de vigencia del certificado.
     * Para alertas de renovación.
     */
    @Column(name = "fecha_vigencia")
    private LocalDateTime fechaVigencia;

    /**
     * Indicador de certificado activo.
     */
    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "creado_at")
    private LocalDateTime creadoAt;

    @Column(name = "actualizado_at")
    private LocalDateTime actualizadoAt;

    @PrePersist
    protected void onCreate() {
        creadoAt = LocalDateTime.now();
        actualizadoAt = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoAt = LocalDateTime.now();
    }
}