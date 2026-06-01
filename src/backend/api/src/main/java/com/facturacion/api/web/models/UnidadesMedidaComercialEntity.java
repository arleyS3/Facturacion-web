package com.facturacion.api.web.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unidades_medida_comercial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadesMedidaComercialEntity {
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
