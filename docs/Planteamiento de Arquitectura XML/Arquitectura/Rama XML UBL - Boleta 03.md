---
title: Rama XML UBL - Boleta 03
date: 2026-04-26
tags:
  - arquitectura/xml
  - ramas
  - boleta
  - ubl21
aliases:
  - Rama UBL Boleta
  - Boleta 03 Rama
related:
  - [[Plan de Implementacion por Ramas XML UBL]]
  - [[Arquitectura de Desarrollo]]
  - [[BoletaXML]]
---

# Rama `feature/xml-ubl-03-boleta`

> [!abstract]
> Implementación del XML UBL 2.1 de Boleta (03) con reglas de identificación del receptor.

## Alcance (sí editar)
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/ubl/
  mapper/boleta/BoletaUblMapper.java
  mapper/boleta/BoletaUblData.java
  mapper/boleta/BoletaLineaUblData.java
  builder/boleta/BoletaUblBuilder.java
  strategy/BoletaUblStrategy.java
src/backend/api/src/test/** (tests de boleta, si se crean)
docs/** (golden/boleta-03.xml, si se crean)
```

## Árbol de carpetas permitido
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/ubl/
  mapper/boleta/
    BoletaUblMapper.java
    BoletaUblData.java
    BoletaLineaUblData.java
  builder/boleta/
    BoletaUblBuilder.java
  strategy/
    BoletaUblStrategy.java
src/backend/api/src/test/**
docs/**
```

## Fuera de alcance (no tocar)
- Builders/mappers/strategies de otros tipos
- Validadores transversales (foundation)

## Reglas clave
- Mapear desde `ComprobanteCanonico`
- Usar `InvoiceType` + `UBL21Marshaller`
- Aplicar reglas SUNAT de identificación del receptor

> [!info]
> Estado actual en backend:
> - Builder/mapper/strategy existen.
> - No hay tests UBL ni golden files aún.

## Checklist (DoD)
- [ ] XML pasa validación estructural
- [ ] Golden file actualizado
- [ ] Sin cambios transversales
