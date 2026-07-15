---
title: Estructura de Boleta Electrónica UBL 2.1 en XML
tags:
  - arquitectura/xml
  - boleta-electronica
  - sunat
aliases:
  - BoletaXML
  - Boleta UBL 2.1
related:
  - [[Arquitectura de Desarrollo]]
  - [[Plan de Implementacion por Ramas XML UBL]]
---

# Estructura de Boleta electrónica vs Formato XML

> [!abstract]
> Esta guía resume los **49 campos clave** de una boleta electrónica UBL 2.1 (SUNAT), con explicación breve y ejemplos XML plegables para lectura rápida en Obsidian.

## ¿Qué significa cada parte en un XML? (rápido)

```xml
<cbc:RegistrationName languageLocaleID="1000"><![CDATA[K&G Asociados S.A.]]></cbc:RegistrationName>
```

- `cbc:` → prefijo/namespace del estándar.
- `RegistrationName` → etiqueta (qué dato representa).
- `languageLocaleID="1000"` → atributo (metadato del dato).
- `<![CDATA[...]]>` → texto literal sin escapar caracteres.
- `</...>` → cierre de etiqueta.

> [!tip]
> Regla simple: **Etiqueta = dato**, **atributo = contexto**, **valor = contenido**.

---

## Campos de la boleta (1 al 49)

### 1. Firma Digital
Firma XMLDSIG para garantizar integridad y autoría.

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
> ```

### 2. Versión del UBL
Versión del estándar UBL usado en el documento.

> [!example]- Ver XML
> ```xml
> <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
> ```

### 3. Versión de la estructura del documento
Versión de personalización local (SUNAT).

> [!example]- Ver XML
> ```xml
> <cbc:CustomizationID>2.0</cbc:CustomizationID>
> ```

### 4. Código de tipo de operación
Identificador SUNAT del tipo de operación.

> [!example]- Ver XML
> ```xml
> <cbc:ProfileID
>   schemeName="SUNAT:Identificador de Tipo de Operación"
>   schemeAgencyName="PE:SUNAT"
>   schemeURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo17">0101</cbc:ProfileID>
> ```

### 5. Numeración (serie + correlativo)
Código visible único de la boleta.

> [!example]- Ver XML
> ```xml
> <cbc:ID>B002-10</cbc:ID>
> ```

### 6. Fecha de emisión
Fecha oficial de emisión del comprobante.

> [!example]- Ver XML
> ```xml
> <cbc:IssueDate>2017-04-28</cbc:IssueDate>
> ```

### 7. Hora de emisión
Hora exacta de emisión.

> [!example]- Ver XML
> ```xml
> <cbc:IssueTime>11:40:21</cbc:IssueTime>
> ```

### 8. Tipo de documento (Boleta)
Código SUNAT del tipo de comprobante boleta.

> [!example]- Ver XML
> ```xml
> <cbc:InvoiceTypeCode
>   listAgencyName="PE:SUNAT"
>   listName="SUNAT:Identificador de Tipo de Documento"
>   listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01">03</cbc:InvoiceTypeCode>
> ```

### 9. Código interno generado por el software de emisión
Código interno de trazabilidad del sistema emisor.

> [!example]- Ver XML
> ```xml
> <cbc:Note languageLocaleID="3000">05010020170428000005</cbc:Note>
> ```

### 10. Leyenda
Texto en letras del importe total u otra leyenda requerida.

> [!example]- Ver XML
> ```xml
> <cbc:Note languageLocaleID="1000">MIL OCHOCIENTOS CINCUENTA Y OCHO CON 59/100 Soles</cbc:Note>
> ```

### 11. Tipo de moneda
Moneda de emisión (ISO 4217).

> [!example]- Ver XML
> ```xml
> <cbc:DocumentCurrencyCode
>   listID="ISO 4217 Alpha"
>   listName="Currency"
>   listAgencyName="United Nations Economic Commission for Europe">PEN</cbc:DocumentCurrencyCode>
> ```

### 12. Tipo y número de guía de remisión relacionada
Referencia a guía vinculada con la operación.

> [!example]- Ver XML
> ```xml
> <cac:DespatchDocumentReference>
>   <cbc:ID>031-002020</cbc:ID>
>   <cbc:DocumentTypeCode
>     listAgencyName="PE:SUNAT"
>     listName="SUNAT:Identificador de guía relacionada"
>     listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01">09</cbc:DocumentTypeCode>
> </cac:DespatchDocumentReference>
> ```

### 13. Tipo y número de otro documento relacionado
Documento adicional asociado a la operación.

> [!example]- Ver XML
> ```xml
> <cac:AdditionalDocumentReference>
>   <cbc:ID>024099</cbc:ID>
>   <cbc:DocumentTypeCode
>     listAgencyName="PE:SUNAT"
>     listName="SUNAT:Identificador de documento relacionado"
>     listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo12">99</cbc:DocumentTypeCode>
> </cac:AdditionalDocumentReference>
> ```

### 14. Información adicional de la firma
Datos de signatario y referencia a firma digital.

> [!example]- Ver XML
> ```xml
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

### 15. Nombre comercial del emisor
Nombre comercial o marca del emisor.

### 16. Razón social del emisor
Denominación legal del emisor.

### 17. Número de RUC del emisor
RUC del emisor.

### 18. Tipo de documento de identidad del emisor
Código del tipo de documento del emisor.

### 19. Código de domicilio fiscal o local anexo del emisor
Código de establecimiento (sucursal/local).

> [!example]- Ver XML (15 al 19)
> ```xml
> <cac:AccountingSupplierParty>
>   <cac:Party>
>     <cac:PartyName>
>       <cbc:Name><![CDATA[K&G Laboratorios]]></cbc:Name>
>     </cac:PartyName>
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
>       <cac:TaxScheme>
>         <cbc:ID>-</cbc:ID>
>       </cac:TaxScheme>
>     </cac:PartyTaxScheme>
>   </cac:Party>
> </cac:AccountingSupplierParty>
> ```

### 20. Tipo y número de documento del adquirente o usuario
Documento de identidad del cliente.

### 21. Nombres o razón social del adquirente o usuario
Nombre completo o razón social del cliente.

> [!example]- Ver XML (20 y 21)
> ```xml
> <cac:AccountingCustomerParty>
>   <cac:Party>
>     <cac:PartyTaxScheme>
>       <cbc:RegistrationName><![CDATA[PAZOS ATOCHE LUANA]]></cbc:RegistrationName>
>       <cbc:CompanyID
>         schemeID="1"
>         schemeName="SUNAT:Identificador de Documento de Identidad"
>         schemeAgencyName="PE:SUNAT"
>         schemeURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06">46237547</cbc:CompanyID>
>       <cac:TaxScheme>
>         <cbc:ID>-</cbc:ID>
>       </cac:TaxScheme>
>     </cac:PartyTaxScheme>
>   </cac:Party>
> </cac:AccountingCustomerParty>
> ```

### 22. Serie y número del comprobante de anticipo
Identificador del comprobante de anticipo relacionado.

### 23. Código de tipo de documento del anticipo
Tipo documental del anticipo (catálogo SUNAT).

### 24. Monto prepagado o anticipado
Importe reconocido como prepago.

### 25. Código de moneda del monto prepagado
Moneda del monto anticipado.

### 26. RUC del emisor del comprobante de anticipo
RUC de quien emitió el documento de anticipo.

> [!example]- Ver XML (22 al 26)
> ```xml
> <cac:PrepaidPayment>
>   <cbc:ID
>     schemeID="02"
>     schemeName="SUNAT:Identificador de Documentos Relacionados"
>     schemeAgencyName="PE:SUNAT"
>     schemeURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo12">BA01-2121</cbc:ID>
>   <cbc:PaidAmount currencyID="PEN">100.00</cbc:PaidAmount>
>   <cbc:InstructionID schemeID="6">20102030201</cbc:InstructionID>
> </cac:PrepaidPayment>
> ```

### 27. Descuento global
Descuento aplicado al total del comprobante.

> [!example]- Ver XML
> ```xml
> <cac:AllowanceCharge>
>   <cbc:ChargeIndicator>false</cbc:ChargeIndicator>
>   <cbc:AllowanceChargeReasonCode>00</cbc:AllowanceChargeReasonCode>
>   <cbc:Amount currencyID="PEN">60.00</cbc:Amount>
>   <cbc:BaseAmount currencyID="PEN">1439.48</cbc:BaseAmount>
> </cac:AllowanceCharge>
> ```

### 28. Monto total de impuestos
Suma total de tributos del documento.

### 29. Monto de operaciones gravadas/exoneradas/inafectas
Bases imponibles por tipo de afectación.

### 30. Sumatoria de IGV
Total IGV declarado.

### 31. Sumatoria de ISC
Total ISC declarado.

### 32. Sumatoria de otros tributos
Total de otros impuestos/cargos tributarios.

> [!example]- Ver XML (28 al 32)
> ```xml
> <cac:TaxTotal>
>   <cbc:TaxAmount currencyID="PEN">259.11</cbc:TaxAmount>
>
>   <cac:TaxSubtotal>
>     <cbc:TaxableAmount currencyID="PEN">1439.48</cbc:TaxableAmount>
>     <cbc:TaxAmount currencyID="PEN">259.11</cbc:TaxAmount>
>     <cac:TaxCategory>
>       <cbc:ID schemeID="UN/ECE 5305">S</cbc:ID>
>       <cac:TaxScheme>
>         <cbc:ID schemeID="UN/ECE 5305" schemeAgencyID="6">1000</cbc:ID>
>         <cbc:Name>IGV</cbc:Name>
>         <cbc:TaxTypeCode>VAT</cbc:TaxTypeCode>
>       </cac:TaxScheme>
>     </cac:TaxCategory>
>   </cac:TaxSubtotal>
>
>   <cac:TaxSubtotal>
>     <cbc:TaxableAmount currencyID="PEN">320.00</cbc:TaxableAmount>
>     <cbc:TaxAmount currencyID="PEN">0.00</cbc:TaxAmount>
>     <cac:TaxCategory>
>       <cbc:ID schemeID="UN/ECE 5305">S</cbc:ID>
>       <cac:TaxScheme>
>         <cbc:ID schemeID="UN/ECE 5305" schemeAgencyID="6">9999</cbc:ID>
>         <cbc:Name>OTROS</cbc:Name>
>         <cbc:TaxTypeCode>OTH</cbc:TaxTypeCode>
>       </cac:TaxScheme>
>     </cac:TaxCategory>
>   </cac:TaxSubtotal>
> </cac:TaxTotal>
> ```

### 33. Total valor de venta
Total de líneas sin impuestos.

### 34. Total precio de venta (incluye impuestos)
Total de venta con impuestos.

### 35. Monto total de descuentos
Suma de descuentos aplicados.

### 36. Monto total de otros cargos
Suma de cargos adicionales.

### 37. Importe total de la venta
Monto final por pagar.

> [!example]- Ver XML (33 al 37)
> ```xml
> <cac:LegalMonetaryTotal>
>   <cbc:LineExtensionAmount currencyID="PEN">1439.48</cbc:LineExtensionAmount>
>   <cbc:TaxInclusiveAmount currencyID="PEN">1698.59</cbc:TaxInclusiveAmount>
>   <cbc:AllowanceTotalAmount currencyID="PEN">60.00</cbc:AllowanceTotalAmount>
>   <cbc:ChargeTotalAmount currencyID="PEN">320.00</cbc:ChargeTotalAmount>
>   <cbc:PrepaidAmount currencyID="PEN">100.00</cbc:PrepaidAmount>
>   <cbc:PayableAmount currencyID="PEN">1858.59</cbc:PayableAmount>
> </cac:LegalMonetaryTotal>
> ```

### 38. Número de orden del ítem
Correlativo de línea dentro de la boleta.

### 39. Unidad de medida por ítem
Código de unidad del producto/servicio.

### 40. Cantidad de unidades por ítem
Cantidad vendida por línea.

### 41. Valor de venta del ítem
Valor de línea sin impuestos.

> [!example]- Ver XML (38 al 41)
> ```xml
> <cac:InvoiceLine>
>   <cbc:ID>1</cbc:ID>
>   <cbc:InvoicedQuantity
>     unitCode="CS"
>     unitCodeListID="UN/ECE rec 20"
>     unitCodeListAgencyName="United Nations Economic Commission for Europe">50</cbc:InvoicedQuantity>
>   <cbc:LineExtensionAmount currencyID="PEN">1439.48</cbc:LineExtensionAmount>
> </cac:InvoiceLine>
> ```

### 42. Precio de venta unitario por ítem y código
Precio unitario con su tipo de precio.

### 43. Valor referencial unitario en operaciones no onerosas
Valor referencial cuando no existe cobro.

### 44. Descuentos por ítem
Descuentos aplicados a una línea específica.

> [!example]- Ver XML (42 al 44)
> ```xml
> <cac:PricingReference>
>   <cac:AlternativeConditionPrice>
>     <cbc:PriceAmount currencyID="PEN">34.99</cbc:PriceAmount>
>     <cbc:PriceTypeCode
>       listName="SUNAT:Indicador de Tipo de Precio"
>       listAgencyName="PE:SUNAT"
>       listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16">01</cbc:PriceTypeCode>
>   </cac:AlternativeConditionPrice>
> </cac:PricingReference>
>
> <cac:AllowanceCharge>
>   <cbc:ChargeIndicator>false</cbc:ChargeIndicator>
>   <cbc:Amount currencyID="PEN">60.00</cbc:Amount>
> </cac:AllowanceCharge>
> ```

### 45. Monto de tributo del ítem
Tributo calculado a nivel de línea.

> [!example]- Ver XML
> ```xml
> <cac:TaxTotal>
>   <cbc:TaxAmount currencyID="PEN">259.11</cbc:TaxAmount>
>   <cac:TaxSubtotal>
>     <cbc:TaxAmount currencyID="PEN">259.11</cbc:TaxAmount>
>     <cac:TaxCategory>
>       <cbc:ID
>         schemeID="UN/ECE 5305"
>         schemeName="Tax Category Identifier"
>         schemeAgencyName="United Nations Economic Commission for Europe">S</cbc:ID>
>       <cbc:Percent>18.00</cbc:Percent>
>       <cbc:TaxExemptionReasonCode
>         listAgencyName="PE:SUNAT"
>         listName="SUNAT:Codigo de Tipo de Afectación del IGV"
>         listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07">10</cbc:TaxExemptionReasonCode>
>       <cac:TaxScheme>
>         <cbc:ID schemeID="UN/ECE 5153" schemeName="Tax Scheme Identifier" schemeAgencyName="United Nations Economic Commission for Europe">1000</cbc:ID>
>         <cbc:Name>IGV</cbc:Name>
>         <cbc:TaxTypeCode>VAT</cbc:TaxTypeCode>
>       </cac:TaxScheme>
>     </cac:TaxCategory>
>   </cac:TaxSubtotal>
> </cac:TaxTotal>
> ```

### 46. Descripción detallada del bien o servicio
Descripción comercial del ítem.

> [!example]- Ver XML
> ```xml
> <cac:Item>
>   <cbc:Description><![CDATA[CAPTOPRIL 1000mg X 30]]></cbc:Description>
> </cac:Item>
> ```

### 47. Código de producto
Código interno del emisor.

### 48. Código de producto SUNAT
Clasificación estandarizada (UNSPSC u otra).

> [!example]- Ver XML (47 y 48)
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

### 49. Valor unitario del ítem
Valor unitario usado para el cálculo de la línea.

> [!example]- Ver XML
> ```xml
> <cac:Price>
>   <cbc:PriceAmount currencyID="PEN">785.20</cbc:PriceAmount>
> </cac:Price>
> ```

---

> [!success]
> Documento optimizado para Obsidian: numeración clara, XML resaltado y secciones plegables para estudiar rápido sin perder detalle técnico.
