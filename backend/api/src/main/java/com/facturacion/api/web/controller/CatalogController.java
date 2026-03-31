package com.facturacion.api.web.controller;

import com.facturacion.api.web.dto.CatalogItem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.facturacion.api.core.catalogo.*;


@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogController {

  @GetMapping("/unidades-medida")
  public List<CatalogItem> unidadesDeMedida() {
    return Arrays.stream(UnidadDeMedida.values())
        .map(u -> CatalogItem.of(u.getCodigo(), u.getDescripcion()))
        .toList();
  }

  @GetMapping("/motivos-traslado")
  public List<CatalogItem> motivosTraslado() {
    return Arrays.stream(MotivoTraslado.values())
        .map(m -> CatalogItem.of(m.getCodigo(), m.getDescripcion()))
        .toList();
  }

  @GetMapping("/puertos")
  public List<CatalogItem> puertos() {
    return Arrays.stream(PuertosPeru.values())
        .map(p -> CatalogItem.of(p.getCodigo(), p.getDescripcion(), p.getCodigoSunat()))
        .toList();
  }

  @GetMapping("/aeropuertos")
  public List<CatalogItem> aeropuertos() {
    return Arrays.stream(AeropuertosPeru.values())
        .map(a -> CatalogItem.of(a.getCodigo(), a.getDescripcion(), a.getCodigoSunat()))
        .toList();
  }

  @GetMapping("/tipos-documento")
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
  public List<CatalogItem> tiposOperacion() {
    return Arrays.stream(TipoOperacion.values())
        .map(t -> CatalogItem.of(t.getCodigo(), t.getDescripcion()))
        .toList();
  }

  @GetMapping("/tipos-afectacion-igv")
  public List<CatalogItem> tiposAfectacionIGV() {
    return Arrays.stream(TipoAfectacionIGV.values())
        .map(t -> CatalogItem.of(t.getCodigo(), t.getDescripcion(), t.getCodigoTributario()))
        .toList();
  }

  @GetMapping("/tipos-documento-identidad")
  public List<CatalogItem> tiposDocumentoIdentidad() {
    return Arrays.stream(TipoDocumentoIdentidad.values())
        .map(t -> CatalogItem.of(t.getCodigo(), t.getDescripcion()))
        .toList();
  }

  @GetMapping("/codigos-tipo-tributo")
  public List<CatalogItem> codigosTipoTributo() {
    return Arrays.stream(CodigoTipTributo.values())
        .map(c -> CatalogItem.of(c.getCodigo(), c.getDescripcion()))
        .toList();
  }

  @GetMapping("/tipos-nota-credito")
  public List<CatalogItem> tiposNotaCredito() {
    return Arrays.stream(TipoNotaCredito.values())
        .map(t -> CatalogItem.of(t.getCodigo(), t.getDescripcion()))
        .toList();
  }

  @GetMapping("/tipos-nota-debito")
  public List<CatalogItem> tiposNotaDebito() {
    return Arrays.stream(TipoNotaDebito.values())
        .map(t -> CatalogItem.of(t.getCodigo(), t.getDescripcion()))
        .toList();
  }

  @GetMapping("/documentos-relacionados-transporte")
  public List<CatalogItem> documentosRelacionadosTransporte() {
    return Arrays.stream(DocumentosRelacionadosTransporte.values())
        .map(d -> CatalogItem.of(d.getCodigo(), d.getDescripcion()))
        .toList();
  }

  @GetMapping("/monedas")
  public List<CatalogItem> monedas() {
    // Lee el archivo ISO-4217.txt desde resources
    InputStream is = getClass().getClassLoader().getResourceAsStream("BD/ISO-4217.txt");
    if (is == null)
      return List.of();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      return reader.lines()
          .skip(1) // salta encabezado
          .map(line -> line.split(",", -1))
          .filter(cols -> cols.length >= 3 && !cols[2].isBlank())
          .map(cols -> CatalogItem.of(
              cols[2].trim(), // CodigoAlfabetico
              cols[1].trim() + (cols[0].isBlank() ? "" : " (" + cols[0].trim() + ")"), // Divisa (Entidad)
              cols[3].trim() // CodigoNumerico
          ))
          .sorted(Comparator.comparing(CatalogItem::label))
          .collect(Collectors.toList());
    } catch (Exception e) {
      return List.of();
    }
  }

  @GetMapping("/series/{tipoDocumento}")
  public List<String> series(@PathVariable String tipoDocumento) {
    return switch (tipoDocumento) {
      case "Factura", "Nota de débito", "Nota de crédito" -> List.of("F001", "F002", "F003", "F004", "F005");
      case "Boleta" -> List.of("B001", "B002", "B003", "B004", "B005");
      case "Guía Remitente","09" -> List.of("T001", "T002", "T003", "T004", "T005");
      case "Guía Transportista","31" -> List.of("V001", "V002", "V003", "V004", "V005");
      default -> throw new IllegalStateException("No hay series para dicho valor: " + tipoDocumento);
    };
  }
}
