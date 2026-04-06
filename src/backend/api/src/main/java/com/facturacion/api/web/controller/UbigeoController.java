package com.facturacion.api.web.controller;

import com.facturacion.api.application.UbigeoService;
import com.facturacion.api.web.dto.CatalogItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos/ubigeo")
public class    UbigeoController {

    private final UbigeoService ubigeoService;

    public UbigeoController(UbigeoService ubigeoService) {
        this.ubigeoService = ubigeoService;
    }

    @GetMapping("/departamentos")
    /** Devuelve la lista de departamentos (label/value). */
    public List<CatalogItem> departamentos() {
        return ubigeoService.listarDepartamentos().stream()
                .map(d -> CatalogItem.of(d, d))
                .toList();
    }

    @GetMapping("/provincias")
    /** Devuelve provincias para el departamento indicado. */
    public List<CatalogItem> provincias(@RequestParam String departamento) {
        return ubigeoService.listarProvincias(departamento).stream()
                .map(p -> CatalogItem.of(p, p))
                .toList();
    }

    @GetMapping("/distritos")
    /** Devuelve distritos (con código ubigeo) para departamento y provincia. */
    public List<CatalogItem> distritos(@RequestParam String departamento, @RequestParam String provincia) {
        return ubigeoService.listarDistritos(departamento, provincia).stream()
                .map(d -> CatalogItem.of(d.ubigeo(), d.distrito()))
                .toList();
    }

    @GetMapping("/buscar")
    /** Busca un distrito por su código ubigeo. */
    public UbigeoService.UbigeoDistrito buscar(@RequestParam String ubigeo) {
        return ubigeoService.buscarPorUbigeo(ubigeo)
                .orElseThrow(() -> new IllegalArgumentException("ubigeo no encontrado"));
    }
}
