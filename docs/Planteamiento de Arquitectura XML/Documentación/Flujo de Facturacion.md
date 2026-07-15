---
title: Flujo de Facturación Electrónica (Perú)
date: 2026-04-25
tags:
  - sunat
  - facturacion-electronica
  - cpe
aliases:
  - Flujo de Facturacion
cssclasses:
  - documentation-note
related:
  - [[Arquitectura de Desarrollo]]
  - [[Plan de Implementacion por Ramas XML UBL]]
---

# Flujo de Facturación Electrónica en Perú

> **Documentación Técnica Oficial**
> Basado en normativa SUNAT vigente — Resolución N° 000003-2023/SUNAT y actualizaciones 2024–2025

---

## 1. Introducción y marco legal

La facturación electrónica en Perú la administra la **SUNAT** (Superintendencia Nacional de Aduanas y de Administración Tributaria). Desde 2022 el 100% de los contribuyentes está obligado a emitir Comprobantes de Pago Electrónicos (CPE) en formato digital.

El formato oficial es **XML bajo el esquema UBL 2.1** (Universal Business Language), adoptado por SUNAT. El envío y la validez tributaria dependen de que SUNAT (o un OSE autorizado) acepte el XML mediante una CDR.

<table>
  <thead>
    <tr><th>Campo</th><th>Detalle</th></tr>
  </thead>
  <tbody>
    <tr><td><strong>Estándar</strong></td><td>XML UBL 2.1</td></tr>
    <tr><td><strong>Ente regulador</strong></td><td>SUNAT</td></tr>
    <tr><td><strong>Obligatoriedad</strong></td><td>100% de contribuyentes desde 2022</td></tr>
    <tr><td><strong>Norma vigente</strong></td><td>Resolución N° 000003-2023/SUNAT</td></tr>
    <tr><td><strong>Plazo de envío</strong></td><td>Máximo 3 días calendario desde la fecha de emisión</td></tr>
    <tr><td><strong>Conservación</strong></td><td>Mínimo 5 años (XML, CDR, resúmenes, comunicaciones de baja)</td></tr>
  </tbody>
</table>

## 2. Tipos de Comprobantes Electrónicos

| Comprobante | Serie | Uso |
|---|---:|---|
| **Factura Electrónica** | F001, F002… | Operaciones B2B y B2G. Requiere RUC del receptor. |
| **Boleta de Venta** | B001, B002… | Operaciones B2C. Consumidores finales. Se envía por Resumen Diario. |
| **Nota de Crédito** | F… / B… | Anulaciones, descuentos, devoluciones. |
| **Nota de Débito** | F… / B… | Cargos adicionales. |
| **Guía de Remisión (GRE)** | V001… | Traslado de bienes; validada por SUNAT directamente. |
| **Comprobante de Retención** | R001… | Agentes de retención. |
| **Comprobante de Percepción** | P001… | Agentes de percepción. |

## 3. Flujo completo de facturación electrónica

> Este flujo aplica mayormente a Facturas, Notas de Crédito y Notas de Débito. Las boletas usan el flujo de Resumen Diario (sección 4).

### 3.1 Paso 1 — Generación del comprobante en el sistema

El ERP o software de facturación arma el comprobante con:

- Datos del emisor: RUC, razón social, dirección fiscal
- Datos del receptor: RUC o DNI, razón social o nombre
- Tipo de comprobante y serie-correlativo (ej. `F001-00000123`)
- Detalle de ítems (cantidad, valor unitario, descripción)
- Impuestos: IGV (18%), ISC, ICBPER, exoneraciones
- Totales desagregados y moneda
- Forma de pago

### 3.2 Paso 2 — Generación del XML UBL 2.1

El sistema convierte los datos a XML UBL 2.1; SUNAT exige ese formato para la validación. Ejemplo (acotado):

```xml
<Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2">
  <cbc:ID>F001-00000123</cbc:ID>
  <cbc:IssueDate>2025-04-25</cbc:IssueDate>
  <cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode>
  <cac:AccountingSupplierParty>
    <!-- Datos del emisor -->
  </cac:AccountingSupplierParty>
  <cac:AccountingCustomerParty>
    <!-- Datos del receptor -->
  </cac:AccountingCustomerParty>
  <cac:TaxTotal>
    <!-- Tributos -->
  </cac:TaxTotal>
  <cac:InvoiceLine>
    <!-- Detalle -->
  </cac:InvoiceLine>
</Invoice>
```

### 3.3 Paso 3 — Firma digital del XML

El XML firmado garantiza autenticidad, integridad y no repudio. Se usa XMLDSig/XAdES y el certificado digital del emisor debe estar registrado en SOL (SUNAT Operaciones en Línea).

Fragmento de la extensión de firma:

```xml
<ext:UBLExtensions>
  <ext:UBLExtension>
    <ext:ExtensionContent>
      <ds:Signature Id="SignatureSP"> ... </ds:Signature>
    </ext:ExtensionContent>
  </ext:UBLExtension>
</ext:UBLExtensions>
```

> [!note] Registro del certificado
> El certificado digital del emisor debe estar cargado y vigente en SOL para que SUNAT lo reconozca.

### 3.4 Paso 4 — Compresión en ZIP

El XML firmado se comprime en un ZIP con la nomenclatura: `RUC-TIPO-SERIE-CORRELATIVO.zip`.

Ejemplos:

- `20600099991-01-F001-00000123.zip` (Factura)
- `20600099991-03-B001-00001200.zip` (Boleta)

### 3.5 Paso 5 — Envío para validación (dentro de 3 días)

Hay dos modalidades:

- Envío directo a SUNAT (SEE — del contribuyente)
- Envío mediante OSE (Operador de Servicios Electrónicos) autorizado — el OSE actúa de intermediario y reporta a SUNAT

Desde 2024 los contribuyentes PRICOS con ingresos ≥ 300 UIT deben operar vía OSE.

Ejemplos de OSE: [[Nubefact]], [[Digiflow]], [[Efact]], [[EDICOM]]

### 3.6 Paso 6 — Respuesta de validación (CDR)

SUNAT/OSE devuelven un XML CDR firmado con el resultado:

| Estado CDR | Significado |
|---|---|
| Aceptado ✅ | El comprobante tiene validez tributaria |
| Rechazado ❌ | El comprobante tiene errores; corregir y reenviar |

> Nota: El estado "Observado" ya no forma parte del flujo vigente; hoy la CDR es Aceptada o Rechazada.

### 3.7 Paso 7 — Representación impresa (PDF)

Una vez aceptado, se genera la representación visual (PDF) que se entrega al receptor. La validez tributaria reside en el XML aceptado, no en el PDF.

<details>
  <summary>Consideraciones sobre el PDF y la consulta</summary>
  <p>El emisor debe mantener acceso a los comprobantes (XML y PDF) para consulta por al menos 1 año y conservarlos 5 años según SUNAT.</p>
</details>

## 4. Flujo especial — Boletas de Venta (Resumen Diario)

Las boletas no se envían individualmente: se generan y entregan al cliente, y al cierre del día se agrupan en un <strong>Resumen Diario</strong> (XML) que se envía a SUNAT/OSE.

Pasos principales:

1. Generación de boleta en XML y entrega al cliente
2. Al cierre del día, creación del Resumen Diario que agrupa boletas emitidas/anuladas
3. Envío del Resumen Diario (plazo máximo: 7 días calendario)
4. Recepción de la CDR del Resumen (Aceptado/Rechazado)

Las bajas de numeración y anulaciones también se reportan vía Resumen Diario.

## 5. Guía de Remisión Electrónica (GRE)

La GRE sustenta el traslado de bienes y se envía DIRECTO a SUNAT — no pasa por OSE. No hay entorno de pruebas para GRE.

## 6. Sobre el archivo TXT

El TXT es un uso interno o intermedio en algunos ERP; NO es el formato oficial de SUNAT. Solo el XML UBL 2.1 con CDR de Aceptación tiene validez tributaria.

## 7. Almacenamiento y conservación

| Campo | Detalle |
|---|---|
| Plazo mínimo | 5 años |
| Documentos | XML, CDR, resúmenes diarios, comunicaciones de baja |
| Acceso | Emisor debe permitir consulta al receptor por mínimo 1 año |

## 8. Resumen del flujo completo

```text
[1] Venta en el sistema
    ↓
[2] Generación XML UBL 2.1
    ↓
[3] Firma digital (XMLDSig / XAdES)
    ↓
[4] Compresión ZIP → RUC-TIPO-SERIE-CORRELATIVO.zip
    ↓
[5] Envío (dentro de 3 días) → OSE o SUNAT
    ↓
[6] Validación tributaria
    ↓
[7] CDR: Aceptado ✅ o Rechazado ❌
    ↓
[8] Generación PDF y entrega al cliente
```

> [!tip] Consejo técnico
> Implementá siempre validaciones locales (esquema XML, cálculos de impuestos, unicidad de serie-correlativo) antes de firmar y enviar; así evitás reenvíos por errores fáciles de detectar.

---

## Referencias y fuentes

- SUNAT — cpe.sunat.gob.pe
- RS N° 097-2012/SUNAT
- RS N° 000003-2023/SUNAT
- RS N° 114-2019/SUNAT

<!-- Wikilinks útiles -->
- [[SUNAT]]
- [[Operadores de Servicios Electrónicos | OSE]]
