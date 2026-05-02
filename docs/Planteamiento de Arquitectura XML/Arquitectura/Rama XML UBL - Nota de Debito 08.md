---
title: Rama XML UBL - Nota de Debito 08
date: 2026-04-26
tags:
  - arquitectura/xml
  - ramas
  - nota-debito
  - ubl21
aliases:
  - Rama UBL Nota Debito
  - Nota de Debito 08 Rama
related:
  - [[Plan de Implementacion por Ramas XML UBL]]
  - [[Arquitectura de Desarrollo]]
  - [[Nota de Debito]]
---

# Rama `feature/xml-ubl-08-nota-debito`

> [!abstract]
> Implementación del XML UBL 2.1 de Nota de Débito (08) con documento base y motivo.

## Alcance (sí editar)
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/ubl/
  mapper/notaDebito/NotaDebitoUblMapper.java
  mapper/notaDebito/NotaDebitoUblData.java
  mapper/notaDebito/NotaDebitoLineaUblData.java
  builder/notaDebito/NotaDebitoUblBuilder.java
  strategy/NotaDebitoUblStrategy.java
src/backend/api/src/test/** (tests de nota de débito, si se crean)
docs/** (golden/nota-debito-08.xml, si se crean)
```

## Árbol de carpetas permitido
```text
src/backend/api/src/main/java/com/facturacion/api/application/comprobante/ubl/
  mapper/notaDebito/
    NotaDebitoUblMapper.java
    NotaDebitoUblData.java
    NotaDebitoLineaUblData.java
  builder/notaDebito/
    NotaDebitoUblBuilder.java
  strategy/
    NotaDebitoUblStrategy.java
src/backend/api/src/test/**
docs/**
```

## Fuera de alcance (no tocar)
- Builders/mappers/strategies de otros tipos
- Validadores transversales (foundation)

## Reglas clave
- Usar `DebitNoteType`
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
