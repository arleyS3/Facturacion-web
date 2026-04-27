---
title: Rama XML UBL - Factura 01
date: 2026-04-26
tags:
  - arquitectura/xml
  - ramas
  - factura
  - ubl21
aliases:
  - Rama UBL Factura
  - Factura 01 Rama
related:
  - [[Plan de Implementacion por Ramas XML UBL]]
  - [[Arquitectura de Desarrollo]]
  - [[FacturaXML]]
---

# Rama `feature/xml-ubl-01-factura`

> [!abstract]
> Implementación del XML UBL 2.1 de Factura (01) usando `ph-ubl`.

## Alcance (sí editar)
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/ubl/
  mapper/factura/FacturaUblMapper.java
  mapper/factura/FacturaUblData.java
  mapper/factura/FacturaLineaUblData.java
  builder/factura/FacturaUblBuilder.java
  strategy/FacturaUblStrategy.java
src/backend/api/src/test/** (tests de factura, si se crean)
docs/** (golden/factura-01.xml, si se crean)
```

## Árbol de carpetas permitido
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/ubl/
  mapper/factura/
    FacturaUblMapper.java
    FacturaUblData.java
    FacturaLineaUblData.java
  builder/factura/
    FacturaUblBuilder.java
  strategy/
    FacturaUblStrategy.java
src/backend/api/src/test/**
docs/**
```

## Fuera de alcance (no tocar)
- Builders/mappers/strategies de otros tipos
- Validadores transversales (foundation)

## Reglas clave
- Mapear desde `ComprobanteCanonico`
- Usar `InvoiceType` + `UBL21Marshaller`
- Mantener `currencyID` en montos

> [!info]
> Estado actual en backend:
> - Builder/mapper/strategy existen.
> - No hay tests UBL ni golden files aún.

## Checklist (DoD)
- [ ] XML pasa validación estructural
- [ ] Golden file actualizado
- [ ] Sin cambios transversales
