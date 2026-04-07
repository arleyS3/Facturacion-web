package com.facturacion.api.web.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ubigeo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UbigeoEntity {
    @Id
    @Column(name = "ubigeo")
    private String ubigeo;

    @Column(nullable = false)
    private String distrito;

    @Column(nullable = false)
    private String provincia;

    @Column(nullable = false)
    private String departamento;
}
