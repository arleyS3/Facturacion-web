---
title: Rama XML UBL - Integracion Final
date: 2026-04-26
tags:
  - arquitectura/xml
  - ramas
  - integracion
  - ubl21
aliases:
  - Rama UBL Integracion
  - Integracion Final UBL
related:
  - [[Plan de Implementacion por Ramas XML UBL]]
  - [[Arquitectura de Desarrollo]]
---

# Rama `feature/xml-ubl-integration-hardening`

> [!abstract]
> Integra flujo XML completo, endpoint, validaciones y firma digital.

## Alcance (sí editar)
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/
  formato/XmlUblGeneradorComprobante.java
  ubl/validator/UblSchemaValidator.java
  ubl/validator/SunatCatalogValidator.java
  ubl/signature/XmlSignatureService.java
src/backend/api/src/main/java/com/facturacion/api/web/controller/
  ComprobanteController.java (o TramaController)
src/backend/api/src/main/java/com/facturacion/api/web/dto/
  GenerarComprobanteResponse.java
```

## Árbol de carpetas permitido
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/
  formato/XmlUblGeneradorComprobante.java
  ubl/validator/
    UblSchemaValidator.java
    SunatCatalogValidator.java
  ubl/signature/
    XmlSignatureService.java
src/backend/api/src/main/java/com/facturacion/api/web/controller/
  ComprobanteController.java (o TramaController)
src/backend/api/src/main/java/com/facturacion/api/web/dto/
  GenerarComprobanteResponse.java
```

## Fuera de alcance (no tocar)
- Builders/mappers/strategies de documentos
- Modelo canónico (salvo ajustes coordinados)

> [!warning]
> Si necesitás ajustar un builder/mapper, coordinar con la rama del documento.

## Checklist (DoD)
- [ ] Endpoint XML operativo
- [ ] Validación XSD + catálogos SUNAT
- [ ] Firma digital aislada en `XmlSignatureService`

> [!warning]
> Estado actual en backend:
> - `XmlSignatureService` es stub (sin firma real).
> - `UblSchemaValidator` y `SunatCatalogValidator` están en modo mínimo (TODO).
> - `ComprobanteController`/`GenerarComprobanteResponse` no existen aún.
