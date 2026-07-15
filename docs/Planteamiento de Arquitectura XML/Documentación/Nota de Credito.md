---
title: Estructura de Nota de Crédito Electrónica UBL 2.1 en XML
tags:
  - arquitectura/xml
  - nota-de-credito-electronica
  - sunat
aliases:
  - Nota de Credito XML
  - Nota de Crédito UBL 2.1
related:
  - [[Arquitectura de Desarrollo]]
  - [[Plan de Implementacion por Ramas XML UBL]]
---

# Estructura de Nota de Crédito electrónica vs Formato XML

> [!abstract]
> Esta guía ordena los **40 campos clave** de una Nota de Crédito electrónica (UBL 2.1 / SUNAT), con explicación breve y ejemplos XML plegables para lectura técnica rápida en Obsidian.

## ¿Qué representa cada parte en XML? (resumen)

```xml
<cbc:RegistrationName languageLocaleID="1000"><![CDATA[K&G Asociados S.A.]]></cbc:RegistrationName>
```

- `cbc:` → namespace/prefijo del estándar.
- `RegistrationName` → etiqueta (qué dato es).
- `languageLocaleID="1000"` → atributo (contexto del dato).
- `<![CDATA[...]]>` → texto literal sin escapar caracteres especiales.
- `</...>` → cierre de etiqueta.

> [!tip]
> Regla mental: **Etiqueta = dato**, **atributo = metadata**, **valor = contenido**.

---

## Campos de la Nota de Crédito (1 al 40)

### 1. Firma digital
Firma XMLDSIG para garantizar integridad y autenticidad del comprobante.

> [!example]- Ver XML
> ```xml
> <ext:UBLExtensions>
>   <ext:UBLExtension>
>     <ext:ExtensionContent>
>       <ds:Signature Id="signatureKG">
>         <ds:SignedInfo>
>           <ds:CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n20010315#WithComments"/>
>           <ds:SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#dsa-sha1"/>
>           <ds:Reference URI="">
>             <ds:Transforms>
>               <ds:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#envelopedsignature"/>
>             </ds:Transforms>
>             <ds:DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
>             <ds:DigestValue>+pruib33lOapq6GSw58GgQLR8VGIGqANloj4EqB1cb4=</ds:DigestValue>
>           </ds:Reference>
>         </ds:SignedInfo>
>         <ds:SignatureValue>...</ds:SignatureValue>
>         <ds:KeyInfo>
>           <ds:X509Data>
>             <ds:X509Certificate>...</ds:X509Certificate>
>           </ds:X509Data>
>         </ds:KeyInfo>
>       </ds:Signature>
>     </ext:ExtensionContent>
>   </ext:UBLExtension>
> </ext:UBLExtensions>
>
> <cac:Signature>
>   <cbc:ID>IDSignKG</cbc:ID>
>   <cac:SignatoryParty>
>     <cac:PartyIdentification>
>       <cbc:ID>20100113612</cbc:ID>
>     </cac:PartyIdentification>
>     <cac:PartyName>
>       <cbc:Name><![CDATA[K&G Laboratorios]]></cbc:Name>
>     </cac:PartyName>
>   </cac:SignatoryParty>
>   <cac:DigitalSignatureAttachment>
>     <cac:ExternalReference>
>       <cbc:URI>#signatureKG</cbc:URI>
>     </cac:ExternalReference>
>   </cac:DigitalSignatureAttachment>
> </cac:Signature>
> ```

### 2. Versión del UBL
Versión del estándar UBL utilizada.

> [!example]- Ver XML
> ```xml
> <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
> ```

### 3. Versión de la estructura del documento
Versión de personalización/reglas SUNAT.

> [!example]- Ver XML
> ```xml
> <cbc:CustomizationID>2.0</cbc:CustomizationID>
> ```

### 4. Numeración (serie + correlativo)
Identificador único visible de la Nota de Crédito.

> [!example]- Ver XML
> ```xml
> <cbc:ID>FC02-10</cbc:ID>
> ```

### 5. Fecha de emisión
Fecha en que se emite el comprobante.

> [!example]- Ver XML
> ```xml
> <cbc:IssueDate>2017-06-28</cbc:IssueDate>
> ```

### 6. Leyenda
Texto complementario exigido o informativo.

> [!example]- Ver XML
> ```xml
> <cbc:Note languageLocaleID="3000">05010020170628000785</cbc:Note>
> ```

### 7. Tipo de moneda
Moneda en la que se emite la Nota de Crédito.

> [!example]- Ver XML
> ```xml
> <cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode>
> ```

### 8. Código del tipo de nota de crédito
Código SUNAT del motivo de ajuste.

> [!example]- Ver XML
> ```xml
> <cac:DiscrepancyResponse>
>   <cbc:ReferenceID>F002-6</cbc:ReferenceID>
>   <cbc:ResponseCode>07</cbc:ResponseCode>
> </cac:DiscrepancyResponse>
> ```

### 9. Motivo o sustento
Descripción del porqué de la Nota de Crédito.

> [!example]- Ver XML
> ```xml
> <cac:DiscrepancyResponse>
>   <cbc:Description><![CDATA[Devolución por ítem]]></cbc:Description>
> </cac:DiscrepancyResponse>
> ```

### 10. Serie y número del documento que modifica
Documento original afectado.

### 11. Tipo de documento del documento que modifica
Tipo de comprobante del documento afectado.

> [!example]- Ver XML (10 y 11)
> ```xml
> <cac:BillingReference>
>   <cac:CreditNoteDocumentReference>
>     <cbc:ID>F002-6</cbc:ID>
>     <cbc:DocumentTypeCode>01</cbc:DocumentTypeCode>
>   </cac:CreditNoteDocumentReference>
> </cac:BillingReference>
> ```

### 12. Documento de referencia
Documento adicional de sustento.

### 13. Tipo y número de guía de remisión relacionada
Guía asociada con la operación.

> [!example]- Ver XML (12 y 13)
> ```xml
> <cac:DespatchDocumentReference>
>   <cbc:ID>031-002020</cbc:ID>
>   <cbc:DocumentTypeCode>09</cbc:DocumentTypeCode>
> </cac:DespatchDocumentReference>
> ```

### 14. Tipo y número de otro documento relacionado
Documento complementario vinculado.

### 15. Código del documento relacionado
Código de clasificación de ese documento.

> [!example]- Ver XML (14 y 15)
> ```xml
> <cac:AdditionalDocumentReference>
>   <cbc:ID>10000120094</cbc:ID>
>   <cbc:DocumentTypeCode>05</cbc:DocumentTypeCode>
> </cac:AdditionalDocumentReference>
> ```

### 16. Razón social del emisor
Denominación legal del emisor.

### 17. Número y tipo de documento del emisor
Identificación tributaria del emisor.

### 18. Código del domicilio fiscal o local anexo del emisor
Código de establecimiento del emisor.

> [!example]- Ver XML (16 al 18)
> ```xml
> <cac:AccountingSupplierParty>
>   <cac:Party>
>     <cac:PartyTaxScheme>
>       <cbc:RegistrationName><![CDATA[K&G Asociados S. A.]]></cbc:RegistrationName>
>       <cbc:CompanyID
>         schemeID="6"
>         schemeName="SUNAT:Identificador de Documento de Identidad"
>         schemeAgencyName="PE:SUNAT"
>         schemeURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06">20100113612</cbc:CompanyID>
>       <cac:RegistrationAddress>
>         <cbc:AddressTypeCode>0001</cbc:AddressTypeCode>
>       </cac:RegistrationAddress>
>     </cac:PartyTaxScheme>
>   </cac:Party>
> </cac:AccountingSupplierParty>
> ```

### 19. Tipo y número de documento de identidad del adquirente
Identificación del cliente.

### 20. Número de RUC del adquirente
RUC del adquirente (si aplica).

### 21. Razón social del adquirente
Nombre o razón social del receptor.

> [!example]- Ver XML (19 al 21)
> ```xml
> <cac:AccountingCustomerParty>
>   <cac:Party>
>     <cac:PartyTaxScheme>
>       <cbc:RegistrationName><![CDATA[CECI FARMA IMPORT S.R.L.]]></cbc:RegistrationName>
>       <cbc:CompanyID
>         schemeID="6"
>         schemeName="SUNAT:Identificador de Documento de Identidad"
>         schemeAgencyName="PE:SUNAT"
>         schemeURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06">20102420706</cbc:CompanyID>
>     </cac:PartyTaxScheme>
>   </cac:Party>
> </cac:AccountingCustomerParty>
> ```

### 22. Monto total de impuestos
Total de tributos del documento.

### 23. Monto de operaciones gravadas/exoneradas/inafectas
Bases imponibles por tipo de afectación.

### 24. Sumatoria de IGV
Total IGV declarado.

### 25. Sumatoria de ISC
Total ISC declarado.

### 26. Sumatoria de otros tributos
Total de otros impuestos aplicados.

> [!example]- Ver XML (22 al 26)
> ```xml
> <cac:TaxTotal>
>   <cbc:TaxAmount currencyID="PEN">2124.00</cbc:TaxAmount>
>
>   <cac:TaxSubtotal>
>     <cbc:TaxableAmount currencyID="PEN">11800.00</cbc:TaxableAmount>
>     <cbc:TaxAmount currencyID="PEN">2124.00</cbc:TaxAmount>
>     <cac:TaxCategory>
>       <cbc:ID>S</cbc:ID>
>       <cac:TaxScheme>
>         <cbc:ID>1000</cbc:ID>
>         <cbc:Name>IGV</cbc:Name>
>         <cbc:TaxTypeCode>VAT</cbc:TaxTypeCode>
>       </cac:TaxScheme>
>     </cac:TaxCategory>
>   </cac:TaxSubtotal>
>
>   <cac:TaxSubtotal>
>     <cbc:TaxableAmount currencyID="PEN">31250.00</cbc:TaxableAmount>
>     <cbc:TaxAmount currencyID="PEN">1250.00</cbc:TaxAmount>
>     <cac:TaxCategory>
>       <cbc:ID>S</cbc:ID>
>       <cac:TaxScheme>
>         <cbc:ID>9999</cbc:ID>
>         <cbc:Name>OTROS</cbc:Name>
>         <cbc:TaxTypeCode>OTH</cbc:TaxTypeCode>
>       </cac:TaxScheme>
>     </cac:TaxCategory>
>   </cac:TaxSubtotal>
> </cac:TaxTotal>
> ```

### 27. Monto total de descuentos del comprobante
Suma total de descuentos aplicados.

### 28. Importe total de la operación
Monto final de la operación ajustada.

### 29. Monto prepagado
Anticipos/prepagos aplicados al total.

> [!example]- Ver XML (27 al 29)
> ```xml
> <cac:LegalMonetaryTotal>
>   <cbc:AllowanceTotalAmount currencyID="PEN">60.00</cbc:AllowanceTotalAmount>
>   <cbc:PrepaidAmount currencyID="PEN">100.00</cbc:PrepaidAmount>
>   <cbc:PayableAmount currencyID="PEN">1858.59</cbc:PayableAmount>
> </cac:LegalMonetaryTotal>
> ```

### 30. Número de orden del ítem
Correlativo de línea en el detalle.

### 31. Unidad de medida y cantidad por ítem
Unidad y cantidad registrada por línea.

### 32. Valor de venta del ítem
Importe neto del ítem sin impuestos.

### 33. Precio de línea referencial/base
Valor de referencia usado en el cálculo de la línea.

> [!example]- Ver XML (30 al 33)
> ```xml
> <cac:CreditNoteLine>
>   <cbc:ID>1</cbc:ID>
>   <cbc:CreditedQuantity
>     unitCode="CS"
>     unitCodeListID="UN/ECE rec 20"
>     unitCodeListAgencyName="United Nations Economic Commission for Europe">50</cbc:CreditedQuantity>
>   <cbc:LineExtensionAmount currencyID="PEN">1439.48</cbc:LineExtensionAmount>
> </cac:CreditNoteLine>
> ```

### 34. Precio de venta unitario por ítem que modifica y código
Precio unitario con indicador de tipo de precio.

> [!example]- Ver XML
> ```xml
> <cac:PricingReference>
>   <cac:AlternativeConditionPrice>
>     <cbc:PriceAmount currencyID="PEN">34.99</cbc:PriceAmount>
>     <cbc:PriceTypeCode>01</cbc:PriceTypeCode>
>   </cac:AlternativeConditionPrice>
> </cac:PricingReference>
> ```

### 35. Valor referencial unitario en operaciones no onerosas
Valor referencial cuando no existe cobro efectivo.

> [!example]- Ver XML
> ```xml
> <cac:PricingReference>
>   <cac:AlternativeConditionPrice>
>     <cbc:PriceAmount currencyID="PEN">250.00</cbc:PriceAmount>
>     <cbc:PriceTypeCode>02</cbc:PriceTypeCode>
>   </cac:AlternativeConditionPrice>
> </cac:PricingReference>
> ```

### 36. Monto de tributo del ítem (IGV/ISC)
Tributos calculados a nivel de línea.

> [!example]- Ver XML
> ```xml
> <cac:TaxTotal>
>   <cbc:TaxAmount currencyID="PEN">259.11</cbc:TaxAmount>
>   <cac:TaxSubtotal>
>     <cbc:TaxableAmount currencyID="PEN">1439.50</cbc:TaxableAmount>
>     <cbc:TaxAmount currencyID="PEN">259.11</cbc:TaxAmount>
>     <cac:TaxCategory>
>       <cbc:ID>S</cbc:ID>
>       <cbc:TaxExemptionReasonCode>10</cbc:TaxExemptionReasonCode>
>       <cac:TaxScheme>
>         <cbc:ID>1000</cbc:ID>
>         <cbc:Name>IGV</cbc:Name>
>         <cbc:TaxTypeCode>VAT</cbc:TaxTypeCode>
>       </cac:TaxScheme>
>     </cac:TaxCategory>
>   </cac:TaxSubtotal>
> </cac:TaxTotal>
>
> <cac:TaxTotal>
>   <cbc:TaxAmount currencyID="PEN">400.00</cbc:TaxAmount>
>   <cac:TaxSubtotal>
>     <cbc:TaxableAmount currencyID="PEN">3333.33</cbc:TaxableAmount>
>     <cbc:TaxAmount currencyID="PEN">400.00</cbc:TaxAmount>
>     <cac:TaxCategory>
>       <cbc:ID>S</cbc:ID>
>       <cbc:TaxExemptionReasonCode>10</cbc:TaxExemptionReasonCode>
>       <cac:TaxScheme>
>         <cbc:ID>2000</cbc:ID>
>         <cbc:Name>ISC</cbc:Name>
>         <cbc:TaxTypeCode>EXC</cbc:TaxTypeCode>
>       </cac:TaxScheme>
>     </cac:TaxCategory>
>   </cac:TaxSubtotal>
> </cac:TaxTotal>
> ```

### 37. Descripción detallada del bien o servicio
Descripción comercial de la línea que se acredita.

> [!example]- Ver XML
> ```xml
> <cbc:Description><![CDATA[CAPTOPRIL 1000mg X 30]]></cbc:Description>
> ```

### 38. Código de producto
Código interno del ítem.

### 39. Código de producto SUNAT
Código de clasificación estandarizada (ej. UNSPSC).

> [!example]- Ver XML (38 y 39)
> ```xml
> <cbc:SellersItemIdentification>
>   <cbc:ID>Cap-258963</cbc:ID>
> </cbc:SellersItemIdentification>
>
> <cac:CommodityClassification>
>   <cbc:ItemClassificationCode
>     listID="UNSPSC"
>     listAgencyName="GS1 US"
>     listName="Item Classification">51121703</cbc:ItemClassificationCode>
> </cac:CommodityClassification>
> ```

### 40. Valor unitario del ítem
Valor unitario base del ítem para cálculo.

> [!example]- Ver XML
> ```xml
> <cbc:PriceAmount currencyID="PEN">785.20</cbc:PriceAmount>
> ```

---

> [!success]
> Documento optimizado para Obsidian: numeración ordenada, XML legible y ejemplos plegables para revisión técnica más rápida.
