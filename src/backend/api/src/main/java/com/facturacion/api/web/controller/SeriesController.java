package com.facturacion.api.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SeriesController {

    @GetMapping("/series/{tipoDocumento}")
    /**
     * Devuelve una lista de series de ejemplo según el tipo de documento.
     * Este endpoint es una implementación simple usada en UI para seleccionar series.
     *
     * @param tipoDocumento nombre o código del tipo de documento (por ejemplo "Factura", "Boleta", "09")
     * @return lista de series (ej.: F001, B001, etc.)
     * @throws IllegalStateException si no hay series definidas para el valor dado
     */
    public List<String> series(@PathVariable String tipoDocumento) {
        return switch (tipoDocumento) {
            case "Factura", "Nota de débito", "Nota de Débito", "Nota de crédito", "Nota de Crédito" -> List.of("F001", "F002", "F003", "F004", "F005");
            case "Boleta" -> List.of("B001", "B002", "B003", "B004", "B005");
            case "Guía Remitente","09" -> List.of("T001", "T002", "T003", "T004", "T005");
            case "Guía Transportista","31" -> List.of("G001", "G002", "G003", "G004", "G005");
            default -> throw new IllegalStateException("No hay series para dicho valor: " + tipoDocumento);
        };
    }
}
