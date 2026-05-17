# API — Generar Nota de Crédito UBL 2.1

## Endpoint

```
POST /api/v1/comprobantes/generar-xml
POST /api/v1/comprobantes/descargar-xml
```

- `generar-xml` → devuelve el XML como string dentro de un JSON (para previsualizar).
- `descargar-xml` → devuelve el XML como archivo `.xml` descargable.

---

## Estructura del Request

```json
{
  "tipoDocumento": "07",
  "secciones": {
    "campos": {
      "A": { ... },
      "A2": { ... }   // opcional, alternativa a los campos de referencia en A
    },
    "listas": {
      "B": [ ... ]
    }
  }
}
```

---

## Campos — Sección `A` (cabecera)

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `Serie` | string | ✅ | Serie del comprobante. Ej: `F001` |
| `Correlativo` | string | ✅ | Correlativo de 8 dígitos. Ej: `00000001` |
| `FchEmis` | string | ✅ | Fecha de emisión `YYYY-MM-DD` |
| `HoraEmision` | string | ✅ | Hora de emisión `HH:MM:SS` |
| `TipoMoneda` | string | ✅ | Código ISO 4217. Ej: `PEN`, `USD` |
| `RUTEmis` | string | ✅ | RUC del emisor (11 dígitos) |
| `TipoRucEmis` | string | ✅ | Tipo doc emisor. `6` = RUC |
| `RznSocEmis` | string | ✅ | Razón social del emisor |
| `RUTRecep` | string | ✅ | Nro documento del receptor |
| `TipoRutReceptor` | string | ✅ | Tipo doc receptor. `6`=RUC, `1`=DNI |
| `RznSocRecep` | string | ✅ | Razón social del receptor |
| `TipoDocRef` | string | ✅ | Tipo del doc que modifica. `01`=Factura, `03`=Boleta |
| `SerieRef` | string | ✅ | Serie del doc que modifica. Ej: `F001` |
| `CorrelativoRef` | string | ✅ | Correlativo del doc que modifica. Ej: `00000010` |
| `CodMotivo` | string | ✅ | Código motivo catálogo 09 (ver tabla abajo) |
| `DescMotivo` | string | ✅ | Descripción del motivo |
| `MtoOperGravadas` | string | — | Monto operaciones gravadas |
| `MtoOperExoneradas` | string | — | Monto operaciones exoneradas |
| `MtoOperInafectas` | string | — | Monto operaciones inafectas |
| `MtoIGV` | string | — | Monto total IGV |
| `TotalImpuestos` | string | — | Total impuestos |
| `ImporteTotal` | string | — | Importe total del comprobante |
| `ValorVenta` | string | — | Valor de venta (sin IGV) |
| `CODI_EMPR` | string | ✅ | Código de empresa (numérico 1-9 dígitos) |
| `CodigoLocalAnexo` | string | ✅ | Código de local/anexo. Ej: `0000` |

---

## Campos — Lista `B` (líneas de detalle)

Cada elemento del array representa una línea del comprobante.

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `NroLinDet` | string | ✅ | Número de línea. Ej: `1` |
| `NmbItem` | string | ✅ | Descripción del producto/servicio |
| `QtyItem` | string | ✅ | Cantidad |
| `UnmdItem` | string | ✅ | Unidad de medida. Ej: `NIU`, `ZZ` |
| `MontoItem` | string | ✅ | Valor de venta del ítem (sin IGV) |
| `CodigoTipoIgv` | string | ✅ | Código afectación IGV (ver tabla abajo) |
| `TasaIgv` | string | — | Tasa IGV. Ej: `18` |
| `ImpuestoIgv` | string | — | Monto IGV del ítem |

---

## Catálogo 09 — Motivos de Nota de Crédito

| Código | Descripción |
|---|---|
| `01` | Anulación de la operación |
| `02` | Anulación por error en el RUC |
| `03` | Corrección por error en la descripción |
| `04` | Descuento global |
| `05` | Descuento por ítem |
| `06` | Devolución por ítem |
| `07` | Bonificación |
| `08` | Disminución en el valor |
| `09` | Otros conceptos |

---

## Catálogo 07 — Tipo de Afectación IGV

| Código | Descripción | Tributo |
|---|---|---|
| `10` | Gravado — Operación Onerosa | IGV (1000) |
| `20` | Exonerado — Operación Onerosa | EXO (9997) |
| `30` | Inafecto — Operación Onerosa | INA (9998) |

---

## Ejemplo completo de Request

```json
{
  "tipoDocumento": "07",
  "secciones": {
    "campos": {
      "A": {
        "CODI_EMPR": "1",
        "Serie": "F001",
        "Correlativo": "00000001",
        "FchEmis": "2026-05-17",
        "HoraEmision": "10:30:00",
        "TipoMoneda": "PEN",
        "RUTEmis": "20612468886",
        "TipoRucEmis": "6",
        "RznSocEmis": "EMPRESA SAC",
        "RUTRecep": "20100162076",
        "TipoRutReceptor": "6",
        "RznSocRecep": "CLIENTE SAC",
        "CodigoLocalAnexo": "0000",
        "TipoDocRef": "01",
        "SerieRef": "F001",
        "CorrelativoRef": "00000010",
        "CodMotivo": "03",
        "DescMotivo": "Corrección por error en la descripción",
        "MtoOperGravadas": "100.00",
        "MtoIGV": "18.00",
        "TotalImpuestos": "18.00",
        "ImporteTotal": "118.00",
        "ValorVenta": "100.00"
      }
    },
    "listas": {
      "B": [
        {
          "NroLinDet": "1",
          "NmbItem": "Producto corregido",
          "QtyItem": "1",
          "UnmdItem": "NIU",
          "MontoItem": "100.00",
          "CodigoTipoIgv": "10",
          "TasaIgv": "18",
          "ImpuestoIgv": "18.00"
        }
      ]
    }
  }
}
```

---

## Respuesta — `generar-xml`

```json
{
  "tipoDocumento": "07",
  "xml": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<CreditNote xmlns=..."
}
```

## Respuesta — `descargar-xml`

Archivo `.xml` con nombre: `{RUC}-07-{Serie}-{Correlativo}.xml`

Headers:
```
Content-Type: application/xml
Content-Disposition: attachment; filename="20612468886-07-F001-00000001.xml"
```

---

## Estructura XML generada (UBL 2.1)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<CreditNote xmlns="urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2"
            xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
            xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
            xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2">

  <!-- Firma digital (placeholder) -->
  <ext:UBLExtensions>
    <ext:UBLExtension>
      <ext:ExtensionContent/>
    </ext:UBLExtension>
  </ext:UBLExtensions>

  <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
  <cbc:CustomizationID>2.0</cbc:CustomizationID>
  <cbc:ID>F001-00000001</cbc:ID>
  <cbc:IssueDate>2026-05-17</cbc:IssueDate>
  <cbc:IssueTime>10:30:00</cbc:IssueTime>

  <!-- Motivo (catálogo 09) -->
  <cac:DiscrepancyResponse>
    <cbc:ReferenceID>F001-00000010</cbc:ReferenceID>
    <cbc:ResponseCode>03</cbc:ResponseCode>
    <cbc:Description>Corrección por error en la descripción</cbc:Description>
  </cac:DiscrepancyResponse>

  <!-- Documento que modifica -->
  <cac:BillingReference>
    <cac:InvoiceDocumentReference>
      <cbc:ID>F001-00000010</cbc:ID>
      <cbc:DocumentTypeCode
        listAgencyName="PE:SUNAT"
        listName="Tipo de Documento"
        listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01">01</cbc:DocumentTypeCode>
    </cac:InvoiceDocumentReference>
  </cac:BillingReference>

  <cbc:DocumentCurrencyCode
    listID="ISO 4217 Alpha"
    listName="Currency"
    listAgencyName="United Nations Economic Commission for Europe">PEN</cbc:DocumentCurrencyCode>

  <!-- Emisor -->
  <cac:AccountingSupplierParty>...</cac:AccountingSupplierParty>

  <!-- Receptor -->
  <cac:AccountingCustomerParty>...</cac:AccountingCustomerParty>

  <!-- Totales de impuestos -->
  <cac:TaxTotal>...</cac:TaxTotal>

  <!-- Totales monetarios -->
  <cac:LegalMonetaryTotal>
    <cbc:LineExtensionAmount currencyID="PEN">100.00</cbc:LineExtensionAmount>
    <cbc:PayableAmount currencyID="PEN">118.00</cbc:PayableAmount>
  </cac:LegalMonetaryTotal>

  <!-- Líneas -->
  <cac:CreditNoteLine>...</cac:CreditNoteLine>

</CreditNote>
```

---

## Notas importantes

1. **Referencia obligatoria** — `SerieRef` y `CorrelativoRef` son obligatorios para que el XML sea válido ante SUNAT. Sin ellos el `BillingReference` queda vacío y SUNAT rechaza el comprobante.

2. **Serie hereda del documento que modifica** — Si el documento referenciado es una Factura (`TipoDocRef=01`), la serie debe empezar con `F`. Si es Boleta (`TipoDocRef=03`), puede empezar con `F` o `B`.

3. **Firma digital** — El XML generado incluye el placeholder `ext:UBLExtensions` pero **no está firmado**. Para envío real a SUNAT se debe firmar con certificado digital antes de enviar.

4. **Autenticación** — El endpoint requiere JWT Bearer token en el header `Authorization`.

5. **Mismo payload que `/tramas/generar`** — Se reutiliza exactamente el mismo formato de request que el endpoint de tramas TXT, facilitando la integración.
