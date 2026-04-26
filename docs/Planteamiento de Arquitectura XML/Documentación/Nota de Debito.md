---
title: Estructura de Nota de Débito Electrónica UBL 2.1 en XML
tags:
  - arquitectura/xml
  - nota-de-debito-electronica
  - sunat
aliases:
  - Nota de Debito XML
  - Nota de Débito UBL 2.1
---

# Estructura de Nota de Débito electrónica vs Formato XML

> [!abstract]
> Esta guía organiza los **40 campos clave** de una Nota de Débito electrónica (UBL 2.1 / SUNAT), con explicación breve y ejemplos XML en bloques plegables para estudiar sin ruido.

## ¿Qué representa cada parte en XML? (resumen)

```xml
<cbc:RegistrationName languageLocaleID="1000"><![CDATA[K&G Asociados S.A.]]></cbc:RegistrationName>
```

- `cbc:` → namespace/prefijo del estándar.
- `RegistrationName` → etiqueta (qué dato es).
- `languageLocaleID="1000"` → atributo (contexto del dato).
- `<![CDATA[...]]>` → texto literal (sin escapar caracteres especiales).
- `</...>` → cierre de etiqueta.

> [!tip]
> Pensalo así: **Etiqueta = tipo de dato**, **atributo = metadata**, **valor = contenido real**.

---

## Campos de la Nota de Débito (1 al 40)

### 1. Firma Digital
Firma XMLDSIG para validar integridad y autoría del comprobante.

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
Versión del estándar UBL usado por el documento.

> [!example]- Ver XML
> ```xml
> <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
> ```

### 3. Versión de la estructura del documento
Versión de personalización/reglas locales SUNAT.

> [!example]- Ver XML
> ```xml
> <cbc:CustomizationID>2.0</cbc:CustomizationID>
> ```

### 4. Numeración (serie + correlativo)
Identificador visible único de la Nota de Débito.

> [!example]- Ver XML
> ```xml
> <cbc:ID>FC02-10</cbc:ID>
> ```

### 5. Fecha de emisión
Fecha de emisión del comprobante.

> [!example]- Ver XML
> ```xml
> <cbc:IssueDate>2017-06-28</cbc:IssueDate>
> ```

### 6. Leyenda
Texto adicional exigido/reglamentario en el comprobante.

> [!example]- Ver XML
> ```xml
> <cbc:Note languageLocaleID="3000">05010020170628000785</cbc:Note>
> ```

### 7. Tipo de moneda
Moneda de emisión del documento.

> [!example]- Ver XML
> ```xml
> <cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode>
> ```

### 8. Código del tipo de nota de débito
Código SUNAT del motivo/tipo de ajuste.

> [!example]- Ver XML
> ```xml
> <cac:DiscrepancyResponse>
>   <cbc:ReferenceID>F002-6</cbc:ReferenceID>
>   <cbc:ResponseCode>07</cbc:ResponseCode>
> </cac:DiscrepancyResponse>
> ```

### 9. Motivo o sustento
Descripción del motivo de emisión de la nota de débito.

> [!example]- Ver XML
> ```xml
> <cac:DiscrepancyResponse>
>   <cbc:Description><![CDATA[Devolución por ítem]]></cbc:Description>
> </cac:DiscrepancyResponse>
> ```

### 10. Serie y número del documento que modifica
Identificador del documento afectado.

### 11. Tipo de documento que modifica
Tipo de comprobante afectado (factura/boleta/etc.).

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
Documento relacionado usado como sustento adicional.

### 13. Tipo y número de guía de remisión relacionada
Guía vinculada con la operación original.

> [!example]- Ver XML (12 y 13)
> ```xml
> <cac:DespatchDocumentReference>
>   <cbc:ID>031-002020</cbc:ID>
>   <cbc:DocumentTypeCode>09</cbc:DocumentTypeCode>
> </cac:DespatchDocumentReference>
> ```

### 14. Tipo y número de otro documento relacionado
Documento adicional asociado a la operación.

### 15. Código del documento relacionado
Código de clasificación del documento adicional.

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
Identificador tributario del emisor.

### 18. Código de domicilio fiscal/local anexo del emisor
Código del establecimiento declarado.

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
Identificación del cliente receptor.

### 20. Número de RUC del adquirente
RUC del cliente (cuando aplique).

### 21. Razón social del adquirente
Nombre o razón social del cliente.

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
Bases por tipo de afectación tributaria.

### 24. Sumatoria de IGV
Total de IGV declarado.

### 25. Sumatoria de ISC
Total de ISC declarado.

### 26. Sumatoria de otros tributos
Total de tributos adicionales.

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
Suma de descuentos aplicados.

### 28. Importe total de la operación
Monto final de la operación ajustada.

### 29. Monto prepagado
Monto de anticipos/prepagos aplicados.

> [!example]- Ver XML (27 al 29)
> ```xml
> <cac:LegalMonetaryTotal>
>   <cbc:AllowanceTotalAmount currencyID="PEN">60.00</cbc:AllowanceTotalAmount>
>   <cbc:PrepaidAmount currencyID="PEN">100.00</cbc:PrepaidAmount>
>   <cbc:PayableAmount currencyID="PEN">1858.59</cbc:PayableAmount>
> </cac:LegalMonetaryTotal>
> ```

### 30. Número de orden del ítem
Correlativo de línea dentro del detalle.

### 31. Unidad y cantidad por ítem
Unidad de medida y cantidad de la línea.

### 32. Valor de venta del ítem
Valor neto por línea sin impuestos.

### 33. Precio de línea referencial/base
Valor de referencia usado en el cálculo de línea.

> [!example]- Ver XML (30 al 33)
> ```xml
> <cac:DebitNoteLine>
>   <cbc:ID>1</cbc:ID>
>   <cbc:DebitedQuantity
>     unitCode="CS"
>     unitCodeListID="UN/ECE rec 20"
>     unitCodeListAgencyName="United Nations Economic Commission for Europe">50</cbc:DebitedQuantity>
>   <cbc:LineExtensionAmount currencyID="PEN">1439.48</cbc:LineExtensionAmount>
> </cac:DebitNoteLine>
> ```

### 34. Precio de venta unitario por ítem que modifica y código
Precio unitario e indicador de tipo de precio.

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
Valor referencial cuando no hay cobro efectivo.

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
Descripción del concepto por el que se ajusta el monto.

> [!example]- Ver XML
> ```xml
> <cbc:Description><![CDATA[Por aplicación de intereses compensatorios y moratorios según contrato N° 9685112]]></cbc:Description>
> ```

### 38. Código de producto
Código interno del producto/servicio.

### 39. Código de producto SUNAT
Código de clasificación estandarizado.

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
Valor unitario base para cálculo del ajuste.

> [!example]- Ver XML
> ```xml
> <cbc:PriceAmount currencyID="PEN">785.20</cbc:PriceAmount>
> ```

---

> [!success]
> Documento optimizado para Obsidian: estructura numerada limpia, código XML resaltado y ejemplos plegables para revisión técnica rápida.
