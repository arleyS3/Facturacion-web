package com.facturacion.api.web.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "monedas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonedaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entidad;

    private String divisa;

    @Column(name = "codigo_alfabetico")
    private String codigoAlfabetico;

    @Column(name = "codigo_numerico")
    private Short codigoNumerico;

    @Column(name = "unidad_menor")
    private String unidadMenor;

    @Column(name = "fecha_retiro")
    private String fechaRetiro;
}
