---
title: Arquitectura de Desarrollo TXT + XML UBL 2.1
date: 2026-04-25
tags:
  - arquitectura/xml
  - backend/api
  - sunat
  - ubl21
aliases:
  - Arquitectura XML Backend
  - Plan de Desarrollo UBL
status: draft
---

# Arquitectura de Desarrollo: coexistencia TXT + XML (SUNAT)

> [!abstract]
> Objetivo: extender `src/backend/api/` para generar XML UBL 2.1 con `ph-ubl`/`ph-ublpe` para **Factura (01), Boleta (03), Nota de Crédito (07), Nota de Débito (08) y Guía de Remisión (09)**, manteniendo intacto el flujo TXT actual.

## 1) Estado actual (flujo TXT)

```text
TramaController
  -> TramaService
      -> RequestValidationService
      -> TramaAssembler
          -> DocumentoElectronicoFactory
              -> Factura/Boleta/NC/ND/GR
                  -> Secciones (A, B, A2, G...)
                      -> generaTramaTXT()
```

- Entrada: `GenerarTramaRequest(tipoDocumento, secciones)` con `campos` y `listas`.
- Mapeo tipo doc: `TipoDocumentoMapper`.
- Validación: `RequestValidationService` (reglas mínimas comunes + comprobante + guía).
- Serialización TXT: `Seccion#getSeccion(...)` (`PREFIJO;CAMPO;idx;valor`).
- Importación TXT: `TramaImportService`.

> [!note]
> Punto de extensión natural: agregar una capa de **salida por formato** (TXT/XML) sin tocar la estructura de request ni romper endpoints existentes.

## 2) Arquitectura objetivo

> [!important]
> Decisión: usar un **modelo intermedio canónico** (`ComprobanteCanonico`) para desacoplar reglas de negocio SUNAT de la librería `ph-ubl`.

### Componentes propuestos

1. **Orquestador de generación**
   - `application/comprobante/ComprobanteService`
   - Selecciona estrategia por formato: TXT o XML.

2. **Estrategias de salida**
   - `application/comprobante/formato/GeneradorComprobante` (interface)
   - `TxtGeneradorComprobante` (adaptador al flujo actual)
   - `XmlUblGeneradorComprobante` (nuevo)

3. **Modelo canónico + mapeadores**
   - `application/comprobante/modelo/ComprobanteCanonico`
   - `application/comprobante/mapper/SeccionesToCanonicoMapper`
   - Un mapper por tipo UBL (`FacturaUblMapper`, `BoletaUblMapper`, etc.).

4. **Builders UBL por documento**
   - `application/comprobante/ubl/builder/{factura|boleta|notaCredito|notaDebito|guiaRemision}`
   - Encapsulan creación de objetos `ph-ubl`/`ph-ublpe`.

5. **Validación XML/SUNAT**
   - `application/comprobante/ubl/validator/UblSchemaValidator`
   - `.../SunatCatalogValidator`

6. **Firma digital (fase posterior/integración)**
   - `application/comprobante/ubl/signature/XmlSignatureService`

## 3) Estructura sugerida en `src/backend/api/`

```text
src/main/java/com/facturacion/api/application/comprobante/
  ComprobanteService.java
  FormatoSalidaComprobante.java (TXT|XML)
  formato/
    GeneradorComprobante.java
    TxtGeneradorComprobante.java
    XmlUblGeneradorComprobante.java
  modelo/
    ComprobanteCanonico.java
    DetalleCanonico.java
  mapper/
    SeccionesToCanonicoMapper.java
  ubl/
    builder/
      factura/FacturaUblBuilder.java
      boleta/BoletaUblBuilder.java
      notaCredito/NotaCreditoUblBuilder.java
      notaDebito/NotaDebitoUblBuilder.java
      guiaRemision/GuiaRemisionUblBuilder.java
    mapper/
      FacturaUblMapper.java
      BoletaUblMapper.java
      NotaCreditoUblMapper.java
      NotaDebitoUblMapper.java
      GuiaRemisionUblMapper.java
    validator/
      UblSchemaValidator.java
      SunatCatalogValidator.java
    signature/
      XmlSignatureService.java
```

## 4) Estrategia de ramas GitHub (paralelo sin pisadas)

- **Base compartida**: `feature/xml-ubl-foundation`
  - agrega dependencias `ph-ubl*`, interfaces, modelo canónico, contrato de formato y tests base.
- Ramas por tipo (siempre desde `feature/xml-ubl-foundation`):
  - `feature/xml-ubl-01-factura`
  - `feature/xml-ubl-03-boleta`
  - `feature/xml-ubl-07-nota-credito`
  - `feature/xml-ubl-08-nota-debito`
  - `feature/xml-ubl-09-guia-remision`

Orden de merge recomendado:
1) foundation → 2) factura → 3) boleta → 4) nota crédito → 5) nota débito → 6) guía → 7) integración final.

Reglas anti-conflicto:
- cada rama toca SOLO su paquete `ubl/builder/<tipo>` y `ubl/mapper/<tipo>`;
- cambios transversales van por PR chico contra `feature/xml-ubl-foundation`;
- prohibido editar el mismo test snapshot entre ramas sin coordinación.

## 5) Fases y checklist

- [ ] **Fase 0 Foundation**: dependencias, `FormatoSalidaComprobante`, `GeneradorComprobante`, `ComprobanteCanonico`, adapter TXT.
- [ ] **Fase 1-5 Documento**: implementar builder+mapper+validator por cada tipo SUNAT.
- [ ] **Fase 6 Integración**: endpoint para XML (`/api/v1/tramas/generar-xml` o `formato=XML`), nombre archivo `.xml`, compatibilidad backward.
- [ ] **Fase 7 Pruebas**: unit (mappers/builders), integración (endpoint), validación XSD, golden files XML por tipo.

## 6) Riesgos y mitigaciones

> [!warning]
> Riesgo: catálogos SUNAT desactualizados.  
> Mitigación: centralizar catálogo versionado + tests por código catálogo.

> [!warning]
> Riesgo: XML válido XSD pero rechazado por reglas SUNAT.  
> Mitigación: validar doble capa (`XSD + reglas de negocio SUNAT`) antes de firma/envío.

> [!warning]
> Riesgo: firma digital rompe canonicalización.  
> Mitigación: servicio de firma aislado, pruebas con certificados reales de homologación.

> [!warning]
> Riesgo: alto acoplamiento a `ph-ubl`.  
> Mitigación: mapear desde `ComprobanteCanonico`; `ph-ubl` solo dentro de `ubl/*`.

## 7) Referencias internas

- [[Plan de Implementacion por Ramas XML UBL]]
- [[Ph-ubl]]
- [[FacturaXML]]
- [[BoletaXML]]
- [[Nota de Credito]]
- [[Nota de Debito]]
- [[Flujo de Facturacion]]
