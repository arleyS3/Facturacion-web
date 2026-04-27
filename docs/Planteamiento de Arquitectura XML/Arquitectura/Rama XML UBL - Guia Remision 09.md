---
title: Rama XML UBL - Guia Remision 09
date: 2026-04-26
tags:
  - arquitectura/xml
  - ramas
  - guia-remision
  - ubl21
aliases:
  - Rama UBL Guia Remision
  - Guia Remision 09 Rama
related:
  - [[Plan de Implementacion por Ramas XML UBL]]
  - [[Arquitectura de Desarrollo]]
---

# Rama `feature/xml-ubl-09-guia-remision`

> [!abstract]
> Implementación del XML UBL 2.1 de Guía de Remisión (09) con `DespatchAdvice`.

## Alcance (sí editar)
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/ubl/
  mapper/guiaRemision/GuiaRemisionUblMapper.java
  mapper/guiaRemision/GuiaRemisionUblData.java
  mapper/guiaRemision/GuiaRemisionLineaUblData.java
  builder/guiaRemision/GuiaRemisionUblBuilder.java
  strategy/GuiaRemisionUblStrategy.java
src/backend/api/src/test/** (tests de guía, si se crean)
docs/** (golden/guia-remision-09.xml, si se crean)
```

## Árbol de carpetas permitido
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/ubl/
  mapper/guiaRemision/
    GuiaRemisionUblMapper.java
    GuiaRemisionUblData.java
    GuiaRemisionLineaUblData.java
  builder/guiaRemision/
    GuiaRemisionUblBuilder.java
  strategy/
    GuiaRemisionUblStrategy.java
src/backend/api/src/test/**
docs/**
```

## Fuera de alcance (no tocar)
- Builders/mappers/strategies de otros tipos
- Validadores transversales (foundation)

## Reglas clave
- Usar `DespatchAdviceType`
- Definir `Shipment` con motivo/modalidad de traslado
- Validar ubigeos y actores de transporte

> [!warning]
> Estado actual en backend:
> - `GuiaRemisionUblMapper` retorna `null` (pendiente).
> - `GuiaRemisionUblStrategy` devuelve XML mínimo si no hay data.
> - No hay tests UBL ni golden files aún.

## Checklist (DoD)
- [ ] XML pasa validación estructural
- [ ] Golden file actualizado
- [ ] Sin cambios transversales
