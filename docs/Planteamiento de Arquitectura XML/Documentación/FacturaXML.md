---
title: Estructura de Factura Electrónica UBL 2.1 en XML
tags:
  - arquitectura/xml
  - factura-electronica
  - sunat
aliases:
  - FacturaXML
  - Factura UBL 2.1
---

# Estructura de Factura electrónica vs Formato XML

> [!abstract]
> Esta guía explica los **48 campos clave** de una factura electrónica en UBL 2.1 (SUNAT), con ejemplos XML en bloques plegables para facilitar lectura y revisión.

## ¿Qué significa cada cosa en un XML? (resumen rápido)

En XML, cada dato viaja dentro de una estructura con etiquetas y atributos.

```xml
<cbc:RegistrationName languageLocaleID="1000"><![CDATA[K&G Asociados S.A.]]></cbc:RegistrationName>
```

- `cbc:` → **namespace/prefijo** (indica el estándar del nodo).
- `RegistrationName` → **nombre de etiqueta** (qué dato es).
- `languageLocaleID="1000"` → **atributo** (metadato del nodo).
- `<![CDATA[...]]>` → **CDATA** (texto literal sin escapar caracteres especiales).
- `</cbc:RegistrationName>` → **cierre de etiqueta**.

> [!tip]
> Regla mental simple: **Etiqueta = tipo de dato**, **atributo = contexto del dato**, **valor = contenido real**.

---

## Campos de la factura (1 al 48)

### 1. Firma Digital
Define la firma XMLDSIG del comprobante para validar integridad y autoría.

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
Versión del estándar UBL usado por el documento.

> [!example]- Ver XML
> ```xml
> <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
> ```

### 3. Versión de la estructura del documento
Versión de personalización/reglas locales del comprobante.

> [!example]- Ver XML
> ```xml
> <cbc:CustomizationID>2.0</cbc:CustomizationID>
> ```

### 4. Código de tipo de operación
Identifica el tipo de operación SUNAT (ej. venta interna).

> [!example]- Ver XML
> ```xml
> <cbc:ProfileID
>   schemeName="SUNAT:Identificador de Tipo de Operación"
>   schemeAgencyName="PE:SUNAT"
>   schemeURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo17">0101</cbc:ProfileID>
> ```

### 5. Numeración (serie + correlativo)
Código único visible del comprobante.

> [!example]- Ver XML
> ```xml
> <cbc:ID>F002-10</cbc:ID>
> ```

### 6. Fecha de emisión
Fecha oficial de emisión del comprobante.

> [!example]- Ver XML
> ```xml
> <cbc:IssueDate>2017-04-28</cbc:IssueDate>
> ```

### 7. Hora de emisión
Hora de emisión del comprobante.

> [!example]- Ver XML
> ```xml
> <cbc:IssueTime>11:40:21</cbc:IssueTime>
> ```

### 8. Fecha de vencimiento
Fecha límite de pago (si aplica).

> [!example]- Ver XML
> ```xml
> <cbc:DueDate>2017-05-28</cbc:DueDate>
> ```

### 9. Tipo de documento (Factura)
Código del tipo de comprobante según catálogo SUNAT.

> [!example]- Ver XML
> ```xml
> <cbc:InvoiceTypeCode
>   listAgencyName="PE:SUNAT"
>   listName="SUNAT:Identificador de Tipo de Documento"
>   listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01">01</cbc:InvoiceTypeCode>
> ```

### 10. Leyendas
Textos adicionales: monto en letras y/o códigos internos.

> [!example]- Ver XML
> ```xml
> <cbc:Note languageLocaleID="1000">MIL OCHOCIENTOS CINCUENTA Y OCHO CON 59/100 Soles</cbc:Note>
> <cbc:Note languageLocaleID="3000">05010020170428000005</cbc:Note>
> ```

### 11. Moneda del comprobante
Moneda de emisión (ISO 4217).

> [!example]- Ver XML
> ```xml
> <cbc:DocumentCurrencyCode
>   listID="ISO 4217 Alpha"
>   listName="Currency"
>   listAgencyName="United Nations Economic Commission for Europe">PEN</cbc:DocumentCurrencyCode>
> ```

### 12. Guía de remisión relacionada
Referencia a guía vinculada a la operación facturada.

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

### 13. Otro documento relacionado
Documento adicional asociado a la operación.

> [!example]- Ver XML
> ```xml
> <cac:AdditionalDocumentReference>
>   <cbc:ID>024099</cbc:ID>
>   <cbc:DocumentTypeCode
>     listAgencyName="PE:SUNAT"
>     listName="SUNAT:Identificador de documento relacionado"
>     listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo12">...</cbc:DocumentTypeCode>
> </cac:AdditionalDocumentReference>
> ```

### 14. Nombre comercial del emisor
Nombre comercial (marca) del emisor.

> [!example]- Ver XML
> ```xml
> <cac:PartyName>
>   <cbc:Name><![CDATA[K&G Laboratorios]]></cbc:Name>
> </cac:PartyName>
> ```

### 15. Razón social del emisor
Denominación legal registrada.

> [!example]- Ver XML
> ```xml
> <cbc:RegistrationName><![CDATA[K&G Asociados S.A.]]></cbc:RegistrationName>
> ```

### 16. Tipo y número de RUC del emisor
Identificación tributaria del emisor.

> [!example]- Ver XML
> ```xml
> <cbc:CompanyID
>   schemeID="6"
>   schemeName="SUNAT:Identificador de Documento de Identidad"
>   schemeAgencyName="PE:SUNAT"
>   schemeURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06">20100113612</cbc:CompanyID>
> ```

### 17. Código de domicilio fiscal/local anexo del emisor
Código del establecimiento del emisor.

> [!example]- Ver XML
> ```xml
> <cac:RegistrationAddress>
>   <cbc:AddressTypeCode>0001</cbc:AddressTypeCode>
> </cac:RegistrationAddress>
> ```

### 18. Tipo y número de documento del adquirente
Documento de identidad del cliente.

> [!example]- Ver XML
> ```xml
> <cbc:CompanyID
>   schemeID="6"
>   schemeName="SUNAT:Identificador de Documento de Identidad"
>   schemeAgencyName="PE:SUNAT"
>   schemeURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06">20102420706</cbc:CompanyID>
> ```

### 19. Razón social del adquirente
Nombre o razón social del cliente.

> [!example]- Ver XML
> ```xml
> <cbc:RegistrationName><![CDATA[CECI FARMA IMPORT S.R.L.]]></cbc:RegistrationName>
> ```

### 20. Dirección de entrega del bien
Ubicación donde se entrega el producto.

> [!example]- Ver XML
> ```xml
> <cac:DeliveryTerms>
>   <cac:DeliveryLocation>
>     <cac:Address>
>       <cbc:StreetName>CALLE NEGOCIOS # 420</cbc:StreetName>
>       <cbc:CityName>LIMA</cbc:CityName>
>       <cbc:CountrySubentity>LIMA</cbc:CountrySubentity>
>       <cbc:CountrySubentityCode>150141</cbc:CountrySubentityCode>
>       <cbc:District>SURQUILLO</cbc:District>
>       <cac:Country>
>         <cbc:IdentificationCode
>           listID="ISO 3166-1"
>           listAgencyName="United Nations Economic Commission for Europe"
>           listName="Country">PE</cbc:IdentificationCode>
>       </cac:Country>
>     </cac:Address>
>   </cac:DeliveryLocation>
> </cac:DeliveryTerms>
> ```

### 21. Descuentos globales
Descuentos aplicados a nivel total del comprobante.

> [!example]- Ver XML
> ```xml
> <cac:AllowanceCharge>
>   <cbc:ChargeIndicator>false</cbc:ChargeIndicator>
>   <cbc:AllowanceChargeReasonCode>00</cbc:AllowanceChargeReasonCode>
>   <cbc:Amount currencyID="PEN">60.00</cbc:Amount>
>   <cbc:BaseAmount currencyID="PEN">1439.48</cbc:BaseAmount>
> </cac:AllowanceCharge>
> ```

### 22. Monto total de impuestos
Total de tributos del comprobante.

### 23. Monto de operaciones gravadas
Base imponible gravada con IGV.

### 24. Monto de operaciones exoneradas
Monto sin IGV por exoneración.

### 25. Monto de operaciones inafectas
Monto no afecto al impuesto.

### 26. Monto de operaciones gratuitas
Valor referencial en operaciones no onerosas.

### 27. Sumatoria de IGV
Total IGV del comprobante.

### 28. Sumatoria de ISC
Total ISC del comprobante.

### 29. Sumatoria de otros tributos
Otros impuestos aplicados.

> [!example]- Ver XML (22 al 29)
> ```xml
> <cac:TaxTotal>
>   <cbc:TaxAmount currencyID="PEN">259.11</cbc:TaxAmount>
>
>   <!-- Gravadas / IGV -->
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
>   <!-- Exoneradas -->
>   <cac:TaxSubtotal>
>     <cbc:TaxableAmount currencyID="PEN">320.00</cbc:TaxableAmount>
>     <cbc:TaxAmount currencyID="PEN">0.00</cbc:TaxAmount>
>     <cac:TaxCategory>
>       <cbc:ID schemeID="UN/ECE 5305">E</cbc:ID>
>       <cac:TaxScheme>
>         <cbc:ID schemeID="UN/ECE 5305" schemeAgencyID="6">9997</cbc:ID>
>         <cbc:Name>EXONERADO</cbc:Name>
>         <cbc:TaxTypeCode>VAT</cbc:TaxTypeCode>
>       </cac:TaxScheme>
>     </cac:TaxCategory>
>   </cac:TaxSubtotal>
> </cac:TaxTotal>
> ```

### 30. Total valor de venta
Total sin impuestos.

### 31. Total precio de venta (incluye impuestos)
Total con impuestos incluidos.

### 32. Total de descuentos del comprobante
Suma total de descuentos aplicados.

### 33. Total de otros cargos del comprobante
Suma total de recargos.

### 34. Importe total por pagar
Monto final a pagar por el cliente.

> [!example]- Ver XML (30 al 34)
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

### 35. Número de orden del ítem
Secuencia del detalle dentro de la factura.

### 36. Cantidad y unidad de medida por ítem
Cantidad vendida y código de unidad.

### 37. Valor de venta del ítem
Valor del ítem sin impuestos.

> [!example]- Ver XML (35 al 37)
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

### 38. Precio de venta unitario por ítem y código
Precio unitario con indicador de tipo de precio.

> [!example]- Ver XML
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
> ```

### 39. Valor referencial unitario en operaciones no onerosas
Valor referencial cuando no hay cobro efectivo.

> [!example]- Ver XML
> ```xml
> <cac:PricingReference>
>   <cac:AlternativeConditionPrice>
>     <cbc:PriceAmount currencyID="PEN">250.00</cbc:PriceAmount>
>     <cbc:PriceTypeCode
>       listName="SUNAT:Indicador de Tipo de Precio"
>       listAgencyName="PE:SUNAT"
>       listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16">02</cbc:PriceTypeCode>
>   </cac:AlternativeConditionPrice>
> </cac:PricingReference>
> ```

### 40. Descuentos por ítem
Descuento aplicado en una línea específica.

> [!example]- Ver XML
> ```xml
> <cac:AllowanceCharge>
>   <cbc:ChargeIndicator>false</cbc:ChargeIndicator>
>   <cbc:AllowanceChargeReasonCode>00</cbc:AllowanceChargeReasonCode>
>   <cbc:Amount currencyID="PEN">143.95</cbc:Amount>
>   <cbc:BaseAmount currencyID="PEN">1439.48</cbc:BaseAmount>
> </cac:AllowanceCharge>
> ```

### 41. Cargos por ítem
Recargo aplicado en una línea específica.

> [!example]- Ver XML
> ```xml
> <cac:AllowanceCharge>
>   <cbc:ChargeIndicator>true</cbc:ChargeIndicator>
>   <cbc:AllowanceChargeReasonCode>50</cbc:AllowanceChargeReasonCode>
>   <cbc:MultiplierFactorNumeric>0.10</cbc:MultiplierFactorNumeric>
>   <cbc:Amount currencyID="PEN">44.82</cbc:Amount>
>   <cbc:BaseAmount currencyID="PEN">448.20</cbc:BaseAmount>
> </cac:AllowanceCharge>
> ```

### 42. Afectación al IGV por ítem
Detalle tributario IGV por línea.

> [!example]- Ver XML
> ```xml
> <cac:TaxTotal>
>   <cbc:TaxAmount currencyID="PEN">259.11</cbc:TaxAmount>
>   <cac:TaxSubtotal>
>     <cbc:TaxableAmount currencyID="PEN">1439.48</cbc:TaxableAmount>
>     <cbc:TaxAmount currencyID="PEN">259.11</cbc:TaxAmount>
>     <cac:TaxCategory>
>       <cbc:ID schemeID="UN/ECE 5305">S</cbc:ID>
>       <cbc:Percent>18.00</cbc:Percent>
>       <cbc:TaxExemptionReasonCode
>         listAgencyName="PE:SUNAT"
>         listName="SUNAT:Codigo de Tipo de Afectación del IGV"
>         listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07">10</cbc:TaxExemptionReasonCode>
>       <cac:TaxScheme>
>         <cbc:ID schemeID="UN/ECE 5153" schemeName="Tax Scheme Identifier">1000</cbc:ID>
>         <cbc:Name>IGV</cbc:Name>
>         <cbc:TaxTypeCode>VAT</cbc:TaxTypeCode>
>       </cac:TaxScheme>
>     </cac:TaxCategory>
>   </cac:TaxSubtotal>
> </cac:TaxTotal>
> ```

### 43. Afectación al ISC por ítem
Detalle tributario ISC por línea.

> [!example]- Ver XML
> ```xml
> <cac:TaxTotal>
>   <cbc:TaxAmount currencyID="PEN">1750.52</cbc:TaxAmount>
>   <cac:TaxSubtotal>
>     <cbc:TaxableAmount currencyID="PEN">8752.60</cbc:TaxableAmount>
>     <cbc:TaxAmount currencyID="PEN">1750.52</cbc:TaxAmount>
>     <cac:TaxCategory>
>       <cbc:ID schemeID="UN/ECE 5305">S</cbc:ID>
>       <cbc:Percent>20.00</cbc:Percent>
>       <cbc:TaxExemptionReasonCode
>         listAgencyName="PE:SUNAT"
>         listName="SUNAT:Codigo de Tipo de Afectación del IGV"
>         listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07">10</cbc:TaxExemptionReasonCode>
>       <cac:TierRange>01</cac:TierRange>
>       <cac:TaxScheme>
>         <cbc:ID schemeID="UN/ECE 5153" schemeName="Tax Scheme Identifier">2000</cbc:ID>
>         <cbc:Name>ISC</cbc:Name>
>         <cbc:TaxTypeCode>EXC</cbc:TaxTypeCode>
>       </cac:TaxScheme>
>     </cac:TaxCategory>
>   </cac:TaxSubtotal>
> </cac:TaxTotal>
> ```

### 44. Descripción detallada del bien o servicio
Detalle comercial del producto/servicio.

> [!example]- Ver XML
> ```xml
> <cac:Item>
>   <cbc:Description><![CDATA[CAPTOPRIL 1000mg X 30]]></cbc:Description>
> </cac:Item>
> ```

### 45. Código de producto
Código interno del emisor.

> [!example]- Ver XML
> ```xml
> <cbc:SellersItemIdentification>
>   <cbc:ID>Cap-258963</cbc:ID>
> </cbc:SellersItemIdentification>
> ```

### 46. Código de producto SUNAT
Clasificación estándar (ej. UNSPSC).

> [!example]- Ver XML
> ```xml
> <cac:CommodityClassification>
>   <cbc:ItemClassificationCode
>     listID="UNSPSC"
>     listAgencyName="GS1 US"
>     listName="Item Classification">51121703</cbc:ItemClassificationCode>
> </cac:CommodityClassification>
> ```

### 47. Propiedades adicionales del ítem
Información complementaria (ej. placa para gasto art. 37).

> [!example]- Ver XML
> ```xml
> <cac:AdditionalItemProperty>
>   <cbc:Name>Gastos Art. 37 Renta: Número de Placa</cbc:Name>
>   <cbc:NameCode
>     listName="SUNAT:Identificador de la propiedad del ítem"
>     listAgencyName="PE:SUNAT">7000</cbc:NameCode>
>   <cbc:Value>B6F-045</cbc:Value>
> </cac:AdditionalItemProperty>
> ```

### 48. Valor unitario del ítem
Valor unitario para cálculo de línea.

> [!example]- Ver XML
> ```xml
> <cac:Price>
>   <cbc:PriceAmount currencyID="PEN">785.20</cbc:PriceAmount>
> </cac:Price>
> ```

---

> [!success]
> Esta versión prioriza lectura en Obsidian: **estructura clara**, **bloques XML resaltados** y **ejemplos plegables** para que no te ahogues en un solo bloque gigante.
