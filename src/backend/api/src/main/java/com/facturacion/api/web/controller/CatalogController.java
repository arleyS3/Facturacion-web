package com.facturacion.api.web.controller;

import com.facturacion.api.web.dto.CatalogItem;
import com.facturacion.api.web.repositories.AeropuertosPeruRepository;
import com.facturacion.api.web.repositories.PuertosPeruRepository;
import com.facturacion.api.web.repositories.MonedaRepository;
import com.facturacion.api.web.repositories.UnidadesMedidaRepository;
import com.facturacion.api.web.repositories.CodigoTipoTributoRepository;
import com.facturacion.api.web.repositories.MotivoTrasladoRepository;
import com.facturacion.api.web.repositories.TipoNotaCreditoRepository;
import com.facturacion.api.web.repositories.TipoNotaDebitoRepository;
import com.facturacion.api.web.repositories.TipoOperacionRepository;
import com.facturacion.api.web.repositories.UnidadesMedidaComercialRepository;
import com.facturacion.api.web.repositories.TipoDocumentoIdentidadRepository;
import com.facturacion.api.web.repositories.TipoAfectacionIgvRepository;
import com.facturacion.api.web.repositories.DocumentosRelacionadosTransporteRepository;
import com.facturacion.api.web.repositories.TipoSistemaIscRepository;
import lombok.RequiredArgsConstructor;


import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/catalogos")
@RequiredArgsConstructor
public class CatalogController {

  private final AeropuertosPeruRepository aeropuertosPeruRepository;
  private final PuertosPeruRepository puertosPeruRepository;
  private final MonedaRepository monedaRepository;
  private final UnidadesMedidaRepository unidadesMedidaRepository;
  private final UnidadesMedidaComercialRepository unidadesMedidaComercialRepository;
  private final CodigoTipoTributoRepository codigoTipoTributoRepository;
  private final MotivoTrasladoRepository motivoTrasladoRepository;
  private final TipoNotaCreditoRepository tipoNotaCreditoRepository;
  private final TipoNotaDebitoRepository tipoNotaDebitoRepository;
  private final TipoOperacionRepository tipoOperacionRepository;
  private final TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;
  private final TipoAfectacionIgvRepository tipoAfectacionIgvRepository;
  private final DocumentosRelacionadosTransporteRepository documentosRelacionadosTransporteRepository;
  private final TipoSistemaIscRepository tipoSistemaIscRepository;

  @GetMapping("/unidades-medida")
  /** Devuelve las unidades de medida disponibles. */
  public List<CatalogItem> unidadesDeMedida() {
    return unidadesMedidaRepository.findAll().stream()
        .map(u -> CatalogItem.of(u.getCodigo(), u.getDescripcion()))
        .toList();
  }

  @GetMapping("/unidades-medida-comercial")
  /** Devuelve las unidades de medida comercial disponibles. */
  public List<CatalogItem> unidadesDeMedidaComercial() {
    return unidadesMedidaComercialRepository.findAll().stream()
        .map(u -> CatalogItem.of(u.getCodigo(), u.getDescripcion()))
        .toList();
  }
  @GetMapping("/motivos-traslado")
  /** Devuelve los motivos de traslado (catalogo). */
  public List<CatalogItem> motivosTraslado() {
    return motivoTrasladoRepository.findAll().stream()
        .map(m -> CatalogItem.of(m.getCodigo(), m.getDescripcion()))
        .toList();
  }

  @GetMapping("/puertos")
  /** Lista puertos peruanos del catálogo. */
  public List<CatalogItem> puertos() {
    return puertosPeruRepository.findAll().stream()
        .map(p -> CatalogItem.of(p.getCodigo(), p.getNombre(), p.getUbigeo()))
        .toList();
  }

  @GetMapping("/aeropuertos")
  /** Lista aeropuertos del catálogo. */
  public List<CatalogItem> aeropuertos() {
    return aeropuertosPeruRepository.findAll().stream()
        .map(a -> CatalogItem.of(a.getCodigo(), a.getNombre(), a.getUbigeo()))
        .toList();
  }

  @GetMapping("/tipos-documento")
  /** Tipos de documento soportados por la API. */
  public List<CatalogItem> tiposDocumento() {
    return List.of(
        CatalogItem.of("01", "Factura"),
        CatalogItem.of("03", "Boleta"),
        CatalogItem.of("07", "Nota de Crédito"),
        CatalogItem.of("08", "Nota de Débito"),
        CatalogItem.of("09", "Guia de Remision"),
        CatalogItem.of("31", "Guia de Remision Transportista"));
  }

  @GetMapping("/tipos-operacion")
  /** Tipos de operación (catalogo). */
  public List<CatalogItem> tiposOperacion() {
    return tipoOperacionRepository.findAll().stream()
        .map(t -> CatalogItem.of(t.getCodigo(), t.getDescripcion()))
        .toList();
  }

  @GetMapping("/tipos-afectacion-igv")
  /** Tipos de afectación de IGV (catalogo). */
  public List<CatalogItem> tiposAfectacionIGV() {
    return tipoAfectacionIgvRepository.findAll().stream()
        .map(t -> CatalogItem.of(t.getCodigo(), t.getDescripcion(), t.getCodigoTributario()))
        .toList();
  }

  @GetMapping("/tipos-documento-identidad")
  /** Tipos de documento de identidad del catálogo. */
  public List<CatalogItem> tiposDocumentoIdentidad() {
    return tipoDocumentoIdentidadRepository.findAll().stream()
        .map(t -> CatalogItem.of(t.getCodigo(), t.getDescripcion()))
        .toList();
  }

  @GetMapping("/codigos-tipo-tributo")
  /** Códigos de tipo de tributo (catalogo). */
  public List<CatalogItem> codigosTipoTributo() {
    return codigoTipoTributoRepository.findAll().stream()
        .map(c -> CatalogItem.of(c.getCodigo(), c.getDescripcion()))
        .toList();
  }

  @GetMapping("/tipos-nota-credito")
  /** Tipos de nota de crédito. */
  public List<CatalogItem> tiposNotaCredito() {
    return tipoNotaCreditoRepository.findAll().stream()
        .map(t -> CatalogItem.of(t.getCodigo(), t.getDescripcion()))
        .toList();
  }

  @GetMapping("/tipos-nota-debito")
  /** Tipos de nota de débito. */
  public List<CatalogItem> tiposNotaDebito() {
    return tipoNotaDebitoRepository.findAll().stream()
        .map(t -> CatalogItem.of(t.getCodigo(), t.getDescripcion()))
        .toList();
  }

  @GetMapping("/documentos-relacionados-transporte")
  /** Documentos relacionados a transporte (catalogo). */
  public List<CatalogItem> documentosRelacionadosTransporte() {
    return documentosRelacionadosTransporteRepository.findAll().stream()
        .map(d -> CatalogItem.of(d.getCodigo(), d.getNombre()))
        .toList();
  }

  @GetMapping("/sistemas-isc")
  /** Sistemas de ISC (catálogo 08 SUNAT). */
  public List<CatalogItem> sistemasIsc() {
    return tipoSistemaIscRepository.findAll().stream()
        .map(s -> CatalogItem.of(s.getCodigo(), s.getDescripcion()))
        .toList();
  }

  @GetMapping("/monedas")
  public List<CatalogItem> monedas() {
    return monedaRepository.findAll().stream()
        .map(m -> CatalogItem.of(m.getCodigoAlfabetico(), m.getDivisa()))
        .toList();
  }

  @GetMapping("/series/{tipoDocumento}")
  /** Devuelve series de ejemplo para el tipo de documento. */
  public List<String> series(@PathVariable String tipoDocumento) {
    return switch (tipoDocumento) {
      case "Factura", "Nota de débito", "Nota de Débito", "Nota de crédito", "Nota de Crédito" -> List.of("F001", "F002", "F003", "F004", "F005");
      case "Boleta" -> List.of("B001", "B002", "B003", "B004", "B005");
      case "Guía Remitente", "Guia de Remision", "09" -> List.of("T001", "T002", "T003", "T004", "T005");
      case "Guía Transportista", "Guia de Remision Transportista", "31" -> List.of("V001", "V002", "V003", "V004", "V005");
      default -> List.of();
    };
  }
}
