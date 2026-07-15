---
title: Rama XML UBL - Nota de Credito 07
date: 2026-04-26
tags:
  - arquitectura/xml
  - ramas
  - nota-credito
  - ubl21
aliases:
  - Rama UBL Nota Credito
  - Nota de Credito 07 Rama
related:
  - [[Plan de Implementacion por Ramas XML UBL]]
  - [[Arquitectura de Desarrollo]]
  - [[Nota de Credito]]
---

# Rama `feature/xml-ubl-07-nota-credito`

> [!abstract]
> Implementación del XML UBL 2.1 de Nota de Crédito (07) con documento afectado y motivo.

## Alcance (sí editar)
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/ubl/
  mapper/notaCredito/NotaCreditoUblMapper.java
  mapper/notaCredito/NotaCreditoUblData.java
  mapper/notaCredito/NotaCreditoLineaUblData.java
  builder/notaCredito/NotaCreditoUblBuilder.java
  strategy/NotaCreditoUblStrategy.java
src/backend/api/src/test/** (tests de nota de crédito, si se crean)
docs/** (golden/nota-credito-07.xml, si se crean)
```

## Árbol de carpetas permitido
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/ubl/
  mapper/notaCredito/
    NotaCreditoUblMapper.java
    NotaCreditoUblData.java
    NotaCreditoLineaUblData.java
  builder/notaCredito/
    NotaCreditoUblBuilder.java
  strategy/
    NotaCreditoUblStrategy.java
src/backend/api/src/test/**
docs/**
```

## Fuera de alcance (no tocar)
- Builders/mappers/strategies de otros tipos
- Validadores transversales (foundation)

## Reglas clave
- Usar `CreditNoteType`
- Incluir `BillingReference` y `DiscrepancyResponse`
- Verificar códigos de motivo SUNAT

> [!warning]
> Estado actual en backend:
> - Builder actual **no** incluye `BillingReference` ni `DiscrepancyResponse` (pendiente).
> - No hay tests UBL ni golden files aún.

## Checklist (DoD)
- [ ] XML pasa validación estructural
- [ ] Golden file actualizado
- [ ] Sin cambios transversales
