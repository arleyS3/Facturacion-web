package com.facturacion.api.web.controller;

import com.facturacion.api.web.dto.SeccionesPayload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.facturacion.api.application.PlantillaService;

@RestController
@RequestMapping("/api/v1/plantillas")
public class PlantillaController {

  private final PlantillaService plantillaService;

  public PlantillaController(PlantillaService plantillaService) {
    this.plantillaService = plantillaService;
  }

  @GetMapping("/{slug}")
  /** Devuelve una plantilla por slug (ej. factura, boleta). */
  public SeccionesPayload get(@PathVariable String slug) {
    return plantillaService.plantillaPorSlug(slug);
  }
}
