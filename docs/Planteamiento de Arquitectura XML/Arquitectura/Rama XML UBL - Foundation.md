---
title: Rama XML UBL - Foundation
date: 2026-04-26
tags:
  - arquitectura/xml
  - ramas
  - foundation
  - ubl21
aliases:
  - Foundation XML UBL
  - Rama UBL Foundation
related:
  - [[Plan de Implementacion por Ramas XML UBL]]
  - [[Arquitectura de Desarrollo]]
---

# Rama `feature/xml-ubl-foundation`

> [!abstract]
> Base técnica compartida para TXT + XML. Define contratos, modelo canónico y validadores transversales.

## Objetivo
Dejar lista la columna vertebral para que las ramas por documento solo implementen su tipo.

## Alcance (sí editar)
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/
  ComprobanteService.java
  ResultadoComprobante.java
  FormatoSalidaComprobante.java
  formato/GeneradorComprobante.java
  formato/TxtGeneradorComprobante.java
  formato/XmlUblGeneradorComprobante.java
  modelo/ComprobanteCanonico.java
  modelo/DocumentoRelacionadoCanonico.java
  modelo/ParteTrasladoCanonico.java
  mapper/SeccionesToCanonicoMapper.java
  ubl/strategy/UblDocumentoStrategy.java
  ubl/strategy/UblStrategyRegistry.java
  ubl/validator/UblSchemaValidator.java
  ubl/validator/SunatCatalogValidator.java
  ubl/io/UblXmlWriter.java
```

## Árbol de carpetas permitido
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

## Fuera de alcance (no tocar)
- `ubl/builder/**`
- `ubl/mapper/**`
- `ubl/strategy/*UblStrategy.java` (por tipo)
- Tests/Golden files específicos por documento

## Entregables
- Contrato TXT|XML funcionando
- Modelo canónico listo
- Validadores base conectados

> [!warning]
> Estado actual en backend:
> - `UblSchemaValidator` solo valida XML vacío (TODO XSD real).
> - `SunatCatalogValidator` tiene validaciones mínimas (tipoDocumento/moneda).

> [!warning]
> Si necesitás tocar un builder/mapper de documento, **no es esta rama**.

## Checklist (DoD)
- [ ] Compila con `ph-ubl21`/`ph-ublpe`
- [ ] TXT no se rompe
- [ ] `XmlUblGeneradorComprobante` usa `UblDocumentoStrategy`
