---
title: Plan de Implementación por Ramas XML UBL 2.1
date: 2026-04-25
tags:
  - arquitectura/xml
  - plan/implementacion
  - github/branches
  - sunat
aliases:
  - Roadmap XML UBL por ramas
  - Playbook ramas UBL
status: draft
---

# Plan detallado por ramas (con ejemplos de código)

> [!abstract]
> Este documento baja a implementación práctica lo definido en [[Arquitectura de Desarrollo]].
> Incluye **qué se hace en cada rama**, **qué funciones crear**, **qué archivos tocar** y **ejemplos de código guía**.

## 1) Reglas de trabajo para NO generar conflictos

> [!important]
> Rama madre para todo lo transversal: `feature/xml-ubl-foundation`.
> Las 5 ramas de documentos se crean desde ahí y solo tocan su vertical.

```text
develop
 └─ feature/xml-ubl-foundation
     ├─ feature/xml-ubl-01-factura
     ├─ feature/xml-ubl-03-boleta
     ├─ feature/xml-ubl-07-nota-credito
     ├─ feature/xml-ubl-08-nota-debito
     └─ feature/xml-ubl-09-guia-remision
```

Reglas:
- Cada rama de documento puede editar solo:
  - `application/comprobante/ubl/builder/<tipo>/**`
  - `application/comprobante/ubl/mapper/<tipo>/**`
  - tests de su tipo.
- Cambios transversales (interfaces, enums, validadores comunes) se hacen en `feature/xml-ubl-foundation` con PR pequeño.
- Antes de abrir PR: rebase/merge desde `feature/xml-ubl-foundation` para minimizar drift.

---

## 2) Plan semanal recomendado

### Semana 1 — Foundation (base técnica)
- Dependencias `ph-ubl21`, `ph-ublpe`.
- Contrato `FormatoSalidaComprobante` y estrategia `GeneradorComprobante`.
- `ComprobanteCanonico` + `SeccionesToCanonicoMapper`.
- Adapter para TXT (`TxtGeneradorComprobante`) reutilizando flujo actual.

### Semana 2 — Factura + Boleta
- Implementar 01 y 03 en paralelo.
- Tests golden XML por tipo.

### Semana 3 — NC + ND
- Implementar 07 y 08.
- Validaciones de documento relacionado/motivos.

### Semana 4 — Guía + Integración
- Implementar 09.
- Integración endpoint XML.
- Validación XSD + validaciones SUNAT + hardening.

---

## 3) Rama `feature/xml-ubl-foundation`

## Objetivo
Dejar lista la columna vertebral para que las demás ramas solo implementen su tipo documental.

## Archivos a crear/modificar

```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/
  ComprobanteService.java
  FormatoSalidaComprobante.java
  formato/GeneradorComprobante.java
  formato/TxtGeneradorComprobante.java
  formato/XmlUblGeneradorComprobante.java
  modelo/ComprobanteCanonico.java
  mapper/SeccionesToCanonicoMapper.java
  ubl/validator/UblSchemaValidator.java
  ubl/validator/SunatCatalogValidator.java
```

## Funciones clave (ejemplo)

```java
public enum FormatoSalidaComprobante {
    TXT,
    XML
}
```

```java
public interface GeneradorComprobante {
    FormatoSalidaComprobante formato();
    ResultadoComprobante generar(GenerarTramaRequest request);
}
```

```java
@Service
public class ComprobanteService {
    private final Map<FormatoSalidaComprobante, GeneradorComprobante> generadores;

    public ResultadoComprobante generar(GenerarTramaRequest request, FormatoSalidaComprobante formato) {
        var generador = Optional.ofNullable(generadores.get(formato))
            .orElseThrow(() -> new IllegalArgumentException("Formato no soportado: " + formato));
        return generador.generar(request);
    }
}
```

```java
@Component
public class TxtGeneradorComprobante implements GeneradorComprobante {
    private final TramaService tramaService;

    @Override
    public FormatoSalidaComprobante formato() { return FormatoSalidaComprobante.TXT; }

    @Override
    public ResultadoComprobante generar(GenerarTramaRequest request) {
        String txt = tramaService.generarTrama(request);
        return ResultadoComprobante.txt(txt);
    }
}
```

## Criterio de salida (DoD)
- [ ] Compila con dependencias UBL.
- [ ] TXT actual no se rompe (regresión verde).
- [ ] Existe punto de entrada único para `TXT|XML`.

---

## 4) Rama `feature/xml-ubl-01-factura`

## Objetivo
Generar `Invoice` UBL 2.1 para Factura (01).

## Archivos de la rama

```text
application/comprobante/ubl/mapper/factura/FacturaUblMapper.java
application/comprobante/ubl/builder/factura/FacturaUblBuilder.java
test/.../FacturaUblMapperTest.java
test/.../FacturaUblBuilderTest.java
test/.../golden/factura-01.xml
```

## Funciones clave (ejemplo)

```java
public interface FacturaUblMapper {
    FacturaUblData map(ComprobanteCanonico canonico);
}
```

```java
public class FacturaUblBuilder {
    public String buildXml(FacturaUblData data) {
        // Pseudocódigo: ajustar imports exactos según versión ph-ubl usada.
        var invoice = new InvoiceType();

        invoice.setID(id(data.serie(), data.correlativo()));
        invoice.setIssueDate(fecha(data.fechaEmision()));
        invoice.setDocumentCurrencyCode(code(data.moneda()));

        // Supplier / Customer / TaxTotal / LegalMonetaryTotal / InvoiceLine ...
        // marshal a XML
        return marshalToXml(invoice);
    }
}
```

## Qué validar sí o sí
- [ ] Totales (`TaxInclusiveAmount`, `PayableAmount`) coherentes con detalle.
- [ ] Códigos de IGV y afectación tributaria según catálogo SUNAT.

---

## 5) Rama `feature/xml-ubl-03-boleta`

## Objetivo
Generar `Invoice` UBL para Boleta (03) con reglas de identificación de receptor según corresponda.

## Archivos de la rama

```text
application/comprobante/ubl/mapper/boleta/BoletaUblMapper.java
application/comprobante/ubl/builder/boleta/BoletaUblBuilder.java
test/.../BoletaUblMapperTest.java
test/.../BoletaUblBuilderTest.java
test/.../golden/boleta-03.xml
```

## Funciones clave (ejemplo)

```java
public class BoletaUblBuilder {
    public String buildXml(BoletaUblData data) {
        var invoice = new InvoiceType();
        invoice.setID(id(data.serie(), data.correlativo()));
        invoice.setIssueDate(fecha(data.fechaEmision()));
        invoice.setDocumentCurrencyCode(code(data.moneda()));

        // Diferencias de reglas respecto a Factura:
        // - tipo y nivel de identificación adquirente
        // - límites y validaciones comerciales SUNAT
        return marshalToXml(invoice);
    }
}
```

## Qué validar sí o sí
- [ ] Reglas de documento de identidad según monto/escenario.
- [ ] Serie/correlativo para tipo 03.

---

## 6) Rama `feature/xml-ubl-07-nota-credito`

## Objetivo
Generar `CreditNote` UBL para Nota de Crédito (07) referenciando comprobante afectado.

## Archivos de la rama

```text
application/comprobante/ubl/mapper/notaCredito/NotaCreditoUblMapper.java
application/comprobante/ubl/builder/notaCredito/NotaCreditoUblBuilder.java
test/.../NotaCreditoUblMapperTest.java
test/.../NotaCreditoUblBuilderTest.java
test/.../golden/nota-credito-07.xml
```

## Funciones clave (ejemplo)

```java
public class NotaCreditoUblBuilder {
    public String buildXml(NotaCreditoUblData data) {
        var creditNote = new CreditNoteType();
        creditNote.setID(id(data.serie(), data.correlativo()));
        creditNote.setIssueDate(fecha(data.fechaEmision()));

        // BillingReference al documento afectado
        creditNote.addBillingReference(crearReferencia(data.docAfectadoTipo(), data.docAfectadoSerie(), data.docAfectadoNumero()));

        // DiscrepancyResponse: motivo NC
        creditNote.addDiscrepancyResponse(crearMotivo(data.codigoMotivo(), data.descripcionMotivo()));

        return marshalToXml(creditNote);
    }
}
```

## Qué validar sí o sí
- [ ] Documento afectado obligatorio y existente en payload.
- [ ] Código de motivo NC válido para SUNAT.

---

## 7) Rama `feature/xml-ubl-08-nota-debito`

## Objetivo
Generar `DebitNote` UBL para Nota de Débito (08) con referencia al documento base.

## Archivos de la rama

```text
application/comprobante/ubl/mapper/notaDebito/NotaDebitoUblMapper.java
application/comprobante/ubl/builder/notaDebito/NotaDebitoUblBuilder.java
test/.../NotaDebitoUblMapperTest.java
test/.../NotaDebitoUblBuilderTest.java
test/.../golden/nota-debito-08.xml
```

## Funciones clave (ejemplo)

```java
public class NotaDebitoUblBuilder {
    public String buildXml(NotaDebitoUblData data) {
        var debitNote = new DebitNoteType();
        debitNote.setID(id(data.serie(), data.correlativo()));
        debitNote.setIssueDate(fecha(data.fechaEmision()));

        debitNote.addBillingReference(crearReferencia(data.docAfectadoTipo(), data.docAfectadoSerie(), data.docAfectadoNumero()));
        debitNote.addDiscrepancyResponse(crearMotivo(data.codigoMotivo(), data.descripcionMotivo()));

        return marshalToXml(debitNote);
    }
}
```

## Qué validar sí o sí
- [ ] Motivos ND válidos.
- [ ] Totales incrementales coherentes con sustento de débito.

---

## 8) Rama `feature/xml-ubl-09-guia-remision`

## Objetivo
Generar `DespatchAdvice` UBL para Guía de Remisión (09).

## Archivos de la rama

```text
application/comprobante/ubl/mapper/guiaRemision/GuiaRemisionUblMapper.java
application/comprobante/ubl/builder/guiaRemision/GuiaRemisionUblBuilder.java
test/.../GuiaRemisionUblMapperTest.java
test/.../GuiaRemisionUblBuilderTest.java
test/.../golden/guia-remision-09.xml
```

## Funciones clave (ejemplo)

```java
public class GuiaRemisionUblBuilder {
    public String buildXml(GuiaRemisionUblData data) {
        var despatch = new DespatchAdviceType();
        despatch.setID(id(data.serie(), data.correlativo()));
        despatch.setIssueDate(fecha(data.fechaEmision()));

        // Datos de traslado
        despatch.setShipment(crearShipment(data.motivoTraslado(), data.modalidadTraslado(), data.puntoPartida(), data.puntoLlegada()));

        // Líneas de guía
        despatch.setDespatchLine(crearLineas(data.items()));
        return marshalToXml(despatch);
    }
}
```

## Qué validar sí o sí
- [ ] Motivo/modalidad de traslado.
- [ ] Ubigeos, direcciones y actor transportista/remitente según caso.

---

## 9) Rama de integración final

Nombre sugerido: `feature/xml-ubl-integration-hardening`

## Objetivo
Unificar todo, exponer endpoint XML y cerrar validación transversal.

## Ejemplo de endpoint (opción con query param)

```java
@PostMapping("/generar")
public ResponseEntity<?> generar(
        @RequestBody @Valid GenerarTramaRequest request,
        @RequestParam(defaultValue = "TXT") FormatoSalidaComprobante formato) {
    var resultado = comprobanteService.generar(request, formato);
    return ResponseEntity.ok(resultado);
}
```

## Validaciones integradas
- `UblSchemaValidator` antes de responder XML.
- `SunatCatalogValidator` antes de firma/envío.
- Firma digital en fase final (`XmlSignatureService`) sin mezclar con mappers/builders.

---

## 10) Plantilla mínima de PR por rama

```markdown
## Objetivo
Implementar <tipo-doc> XML UBL 2.1.

## Cambios
- Mapper:
- Builder:
- Tests unitarios:
- Golden file XML:

## No incluido
- Firma digital
- Envío a SUNAT

## Checklist
- [ ] No toqué paquetes de otros tipos
- [ ] Tests del tipo en verde
- [ ] XML pasa validación estructural
```

---

## 11) Nombres exactos de paquetes y clases (copiar/pegar)

> [!tip]
> Estos nombres están alineados al package real del proyecto: `com.facturacion.api`.

### 11.1 Foundation (`feature/xml-ubl-foundation`)

```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/
  ComprobanteService.java
  ResultadoComprobante.java
  FormatoSalidaComprobante.java
  formato/
    GeneradorComprobante.java
    TxtGeneradorComprobante.java
    XmlUblGeneradorComprobante.java
  modelo/
    ComprobanteCanonico.java
    DocumentoRelacionadoCanonico.java
    ParteTrasladoCanonico.java
  mapper/
    SeccionesToCanonicoMapper.java
  ubl/
    strategy/
      UblDocumentoStrategy.java
      UblStrategyRegistry.java
    validator/
      UblSchemaValidator.java
      SunatCatalogValidator.java
    io/
      UblXmlWriter.java
```

### 11.2 Factura (`feature/xml-ubl-01-factura`)

```text
.../application/comprobante/ubl/mapper/factura/
  FacturaUblMapper.java
  FacturaUblData.java

.../application/comprobante/ubl/builder/factura/
  FacturaUblBuilder.java

.../application/comprobante/ubl/strategy/
  FacturaUblStrategy.java
```

### 11.3 Boleta (`feature/xml-ubl-03-boleta`)

```text
.../application/comprobante/ubl/mapper/boleta/
  BoletaUblMapper.java
  BoletaUblData.java

.../application/comprobante/ubl/builder/boleta/
  BoletaUblBuilder.java

.../application/comprobante/ubl/strategy/
  BoletaUblStrategy.java
```

### 11.4 Nota de Crédito (`feature/xml-ubl-07-nota-credito`)

```text
.../application/comprobante/ubl/mapper/notaCredito/
  NotaCreditoUblMapper.java
  NotaCreditoUblData.java

.../application/comprobante/ubl/builder/notaCredito/
  NotaCreditoUblBuilder.java

.../application/comprobante/ubl/strategy/
  NotaCreditoUblStrategy.java
```

### 11.5 Nota de Débito (`feature/xml-ubl-08-nota-debito`)

```text
.../application/comprobante/ubl/mapper/notaDebito/
  NotaDebitoUblMapper.java
  NotaDebitoUblData.java

.../application/comprobante/ubl/builder/notaDebito/
  NotaDebitoUblBuilder.java

.../application/comprobante/ubl/strategy/
  NotaDebitoUblStrategy.java
```

### 11.6 Guía de Remisión (`feature/xml-ubl-09-guia-remision`)

```text
.../application/comprobante/ubl/mapper/guiaRemision/
  GuiaRemisionUblMapper.java
  GuiaRemisionUblData.java

.../application/comprobante/ubl/builder/guiaRemision/
  GuiaRemisionUblBuilder.java

.../application/comprobante/ubl/strategy/
  GuiaRemisionUblStrategy.java
```

### 11.7 Integración final (`feature/xml-ubl-integration-hardening`)

```text
src/backend/api/src/main/java/com/facturacion/api/web/controller/
  ComprobanteController.java   (nuevo)  o TramaController.java (extensión)

src/backend/api/src/main/java/com/facturacion/api/web/dto/
  GenerarComprobanteResponse.java
```

---

## 12) Firmas de funciones recomendadas por rama

### 12.1 Foundation

```java
package com.facturacion.api.application.comprobante;

public enum FormatoSalidaComprobante { TXT, XML }
```

```java
package com.facturacion.api.application.comprobante.formato;

public interface GeneradorComprobante {
    FormatoSalidaComprobante formato();
    ResultadoComprobante generar(GenerarTramaRequest request);
}
```

```java
package com.facturacion.api.application.comprobante.ubl.strategy;

public interface UblDocumentoStrategy {
    String codigoSunat(); // 01, 03, 07, 08, 09
    String generarXml(ComprobanteCanonico canonico);
}
```

### 12.2 Factura (01)

```java
package com.facturacion.api.application.comprobante.ubl.mapper.factura;

public interface FacturaUblMapper {
    FacturaUblData fromCanonico(ComprobanteCanonico canonico);
}
```

```java
package com.facturacion.api.application.comprobante.ubl.builder.factura;

public interface FacturaUblBuilder {
    InvoiceType build(FacturaUblData data);
}
```

### 12.3 Boleta (03)

```java
package com.facturacion.api.application.comprobante.ubl.mapper.boleta;

public interface BoletaUblMapper {
    BoletaUblData fromCanonico(ComprobanteCanonico canonico);
}
```

```java
package com.facturacion.api.application.comprobante.ubl.builder.boleta;

public interface BoletaUblBuilder {
    InvoiceType build(BoletaUblData data);
}
```

### 12.4 Nota de Crédito (07)

```java
package com.facturacion.api.application.comprobante.ubl.mapper.notaCredito;

public interface NotaCreditoUblMapper {
    NotaCreditoUblData fromCanonico(ComprobanteCanonico canonico);
}
```

```java
package com.facturacion.api.application.comprobante.ubl.builder.notaCredito;

public interface NotaCreditoUblBuilder {
    CreditNoteType build(NotaCreditoUblData data);
}
```

### 12.5 Nota de Débito (08)

```java
package com.facturacion.api.application.comprobante.ubl.mapper.notaDebito;

public interface NotaDebitoUblMapper {
    NotaDebitoUblData fromCanonico(ComprobanteCanonico canonico);
}
```

```java
package com.facturacion.api.application.comprobante.ubl.builder.notaDebito;

public interface NotaDebitoUblBuilder {
    DebitNoteType build(NotaDebitoUblData data);
}
```

### 12.6 Guía de Remisión (09)

```java
package com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision;

public interface GuiaRemisionUblMapper {
    GuiaRemisionUblData fromCanonico(ComprobanteCanonico canonico);
}
```

```java
package com.facturacion.api.application.comprobante.ubl.builder.guiaRemision;

public interface GuiaRemisionUblBuilder {
    DespatchAdviceType build(GuiaRemisionUblData data);
}
```

### 12.7 Integración API

```java
package com.facturacion.api.web.controller;

@PostMapping("/api/v1/comprobantes/generar")
public GenerarComprobanteResponse generar(
        @Valid @RequestBody GenerarTramaRequest request,
        @RequestParam(defaultValue = "TXT") FormatoSalidaComprobante formato) {
    return GenerarComprobanteResponse.from(comprobanteService.generar(request, formato));
}
```

> [!note]
> Si preferís no crear nuevo controller, podés agregar este endpoint en `TramaController`; pero para evitar mezcla de responsabilidades, se recomienda `ComprobanteController`.

---

## 13) Referencias

- [[Arquitectura de Desarrollo]]
- [[Ph-ubl]]
- [[FacturaXML]]
- [[BoletaXML]]
- [[Nota de Credito]]
- [[Nota de Debito]]
- [[Flujo de Facturacion]]
