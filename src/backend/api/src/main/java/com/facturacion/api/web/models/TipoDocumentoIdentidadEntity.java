package com.facturacion.api.web.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tipos_documento_identidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumentoIdentidadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String descripcion;

    private Boolean activo;

    @Column(name = "creado_at")
    private LocalDateTime creadoAt;

    @Column(name = "actualizado_at")
    private LocalDateTime actualizadoAt;
}
