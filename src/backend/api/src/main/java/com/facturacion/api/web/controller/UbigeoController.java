package com.facturacion.api.web.controller;

import com.facturacion.api.web.dto.CatalogItem;
import com.facturacion.api.web.models.UbigeoEntity;
import com.facturacion.api.web.repositories.UbigeoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos/ubigeo")
@RequiredArgsConstructor
public class    UbigeoController {

    private final UbigeoRepository ubigeoRepository;

    @GetMapping("/departamentos")
    /** Devuelve la lista de departamentos (label/value). */
    public List<CatalogItem> departamentos() {
        return ubigeoRepository.findDistinctDepartamentos().stream()
                .map(d -> CatalogItem.of(d, d))
                .toList();
    }

    @GetMapping("/provincias")
    /** Devuelve provincias para el departamento indicado. */
    public List<CatalogItem> provincias(@RequestParam String departamento) {
        return ubigeoRepository.findProvinciasByDepartamento(departamento).stream()
                .map(p -> CatalogItem.of(p, p))
                .toList();
    }

    @GetMapping("/distritos")
    /** Devuelve distritos (con código ubigeo) para departamento y provincia. */
    public List<CatalogItem> distritos(@RequestParam String departamento, @RequestParam String provincia) {
        return ubigeoRepository.findUbigeoByDepartamentoAndProvincia(departamento, provincia).stream()
                .map(d -> CatalogItem.of(d.getUbigeo(), d.getDistrito()))
                .toList();
    }

    @GetMapping("/buscar")
    /** Busca un distrito por su código ubigeo. */
    public UbigeoEntity buscar(@RequestParam String ubigeo) {
        return ubigeoRepository.findById(ubigeo)
                .orElseThrow(() -> new IllegalArgumentException("ubigeo no encontrado"));
    }
}
