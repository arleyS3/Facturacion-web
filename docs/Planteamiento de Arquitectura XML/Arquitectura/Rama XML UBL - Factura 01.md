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

## Progreso de Campos UBL

> [!info] Estructura
> Cada checkbox corresponde a un campo en [[FacturaXML]]. Marca según avanzas.

### Encabezado y Metadatos (1–11)
- [ ] [[FacturaXML#1-firma-digital|Firma Digital (1)]] (pendiente: XmlSignatureService)
- [x] [[FacturaXML#2-versión-del-ubl|Versión UBL (2)]]
- [x] [[FacturaXML#3-versión-de-la-estructura-del-documento|Versión estructura (3)]]
- [x] [[FacturaXML#4-código-de-tipo-de-operaciónTipo operación (4)]]
- [x] [[FacturaXML#5-numeración-serie--correlativo|Numeración (5)]]
- [x] [[FacturaXML#6-fecha-de-emisión|Fecha emisión (6)]]
- [x] [[FacturaXML#7-hora-de-emisión|Hora emisión (7)]]
- [x] [[FacturaXML#8-fecha-de-vencimiento|Fecha vencimiento (8)]]
- [x] [[FacturaXML#9-tipo-de-documento-factura|Tipo documento (9)]]
- [x] [[FacturaXML#10-leyendas|Leyendas (10)]]
- [x] [[FacturaXML#11-moneda-del-comprobante|Moneda (11)]]

### Referencias y Documentos Relacionados (12–13)
- [ ] [[FacturaXML#12-guía-de-remisión-relacionada|Guía remisión (12)]]
- [ ] [[FacturaXML#13-otro-documento-relacionado|Doc. relacionado (13)]]

### Datos del Emisor (14–17)
- [x] [[FacturaXML#14-nombre-comercial-del-emisor|Emisor: Nombre comercial (14)]]
- [x] [[FacturaXML#15-razón-social-del-emisor|Emisor: Razón social (15)]]
- [x] [[FacturaXML#16-tipo-y-número-de-ruc-del-emisor|Emisor: RUC (16)]]
- [x] [[FacturaXML#17-código-de-domicilio-fiscale-local-anexo-del-emisor|Emisor: Domicilio fiscal (17)]]

### Datos del Adquirente (18–20)
- [x] [[FacturaXML#18-tipo-y-número-de-documento-del-adquirente|Adquirente: Documento (18)]]
- [x] [[FacturaXML#19-razón-social-del-adquirente|Adquirente: Razón social (19)]]
- [ ] [[FacturaXML#20-dirección-de-entrega-del-bien|Dirección entrega (20)]]

### Descuentos Globales (21)
- [x] [[FacturaXML#21-descuentos-globales|Descuentos globales (21)]]

### Totales y Tributos (22–34)
- [x] [[FacturaXML#22-monto-total-de-impuestos|Total impuestos (22)]]
- [x] [[FacturaXML#23-monto-de-operaciones-gravadas|Gravadas (23)]]
- [ ] [[FacturaXML#24-monto-de-operaciones-exoneradas|Exoneradas (24)]]
- [ ] [[FacturaXML#25-monto-de-operaciones-inafectas|Inafectas (25)]]
- [ ] [[FacturaXML#26-monto-de-operaciones-gratuitas|Gratuitas (26)]]
- [x] [[FacturaXML#27-sumatoria-de-igv|Sumatoria IGV (27)]]
- [ ] [[FacturaXML#28-sumatoria-de-isc|Sumatoria ISC (28)]]
- [ ] [[FacturaXML#29-sumatoria-de-otros-tributos|Otros tributos (29)]]
- [x] [[FacturaXML#30-total-valor-de-venta|Total valor venta (30)]]
- [x] [[FacturaXML#31-total-precio-de-venta-incluye-impuestos|Total c/impuestos (31)]]
- [x] [[FacturaXML#32-total-de-descuentos-del-comprobante|Total descuentos (32)]]
- [ ] [[FacturaXML#33-total-de-otros-cargos-del-comprobante|Total cargos (33)]]
- [x] [[FacturaXML#34-importe-total-por-pagar|Importe total (34)]]

### Líneas de Ítem (35–48)
- [x] [[FacturaXML#35-número-de-orden-del-ítem|N° ítem (35)]]
- [ ] [[FacturaXML#36-cantidad-y-unidad-de-medida-por-ítem|Cantidad/unidad (36)]]
- [x] [[FacturaXML#37-valor-de-venta-del-ítem|Valor ítem (37)]]
- [x] [[FacturaXML#38-precio-de-venta-unitario-por-ítem-y-código|Precio unitario (38)]]
- [x] [[FacturaXML#39-valor-referencial-unitario-en-operaciones-no-onerosas|Valor referencial (39)]]
- [x] [[FacturaXML#40-descuentos-por-ítem|Descuento ítem (40)]]
- [ ] [[FacturaXML#41-cargos-por-ítem|Cargo ítem (41)]]
- [ ] [[FacturaXML#42-afectación-al-igv-por-ítem|IGV ítem (42)]]
- [ ] [[FacturaXML#43-afectación-al-isc-por-ítem|ISC ítem (43)]]
- [x] [[FacturaXML#44-descripción-detallada-del-bien-o-servicio|Descripción (44)]]
- [ ] [[FacturaXML#45-código-de-producto|Código producto (45)]]
- [ ] [[FacturaXML#46-código-de-producto-sunat|Código SUNAT (46)]]
- [ ] [[FacturaXML#47-propiedades-adicionales-del-ítem|Propiedades ítem (47)]]
- [ ] [[FacturaXML#48-valor-unitario-del-ítem|Valor unitario (48)]]

> [!tip]
> Usa `Ctrl+F` y busca `FacturaXML#` para filtrar por campo. Cada link apunta directamente a la sección correspondiente en la documentación.
