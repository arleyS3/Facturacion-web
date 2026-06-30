<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
            xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
            xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
>

  <sch:title>Reglas de negocio SUNAT para comprobantes electrónicos UBL 2.1</sch:title>

  <!-- ========================================================== -->
  <!-- PATTERN: Receptor                                          -->
  <!-- ========================================================== -->
  <sch:pattern id="receptor" name="Validaciones del receptor">
    <sch:rule context="//cac:AccountingCustomerParty">

      <!-- Si el total > 700, el receptor debe ser RUC (schemeID=6) -->
      <sch:let name="total" value="//cac:LegalMonetaryTotal/cbc:PayableAmount"/>
      <sch:let name="receptorId" value="cac:Party/cac:PartyIdentification/cbc:ID"/>
      <sch:let name="schemeId" value="cac:Party/cac:PartyIdentification/cbc:ID/@schemeID"/>

      <sch:report test="number($total) > 700 and $schemeId != '6' and $schemeId != ''"
                 diagnostics="diag-ruc-obligatorio">
        RUC obligatorio: El monto total S/. <sch:value-of select="$total"/>
        supera los S/. 700. El receptor debe identificarse con RUC (schemeID=6),
        pero se encontró schemeID=<sch:value-of select="$schemeId"/>.
      </sch:report>

      <!-- Si el receptor es RUC, el nombre del receptor es obligatorio -->
      <sch:report test="$schemeId = '6' and not(cac:Party/cac:PartyName/cbc:Name)"
                 diagnostics="diag-nombre-receptor">
        El nombre del receptor es obligatorio cuando el tipo de documento es RUC.
      </sch:report>

    </sch:rule>
  </sch:pattern>

  <!-- ========================================================== -->
  <!-- PATTERN: Impuestos / IGV                                   -->
  <!-- ========================================================== -->
  <sch:pattern id="impuestos" name="Validaciones de impuestos">

    <!-- Regla: Si hay operación gravada, debe existir TaxTotal con IGV -->
    <sch:rule context="/cac:Invoice|cac:CreditNote|cac:DebitNote">

      <!-- Si el InvoiceTypeCode indica Factura (01) o Boleta (03) y hay
           operación gravada, debe haber TaxTotal con IGV -->
      <sch:let name="tipoDoc" value="cbc:InvoiceTypeCode"/>
      <sch:let name="taxAmounts" value="cac:TaxTotal/cbc:TaxAmount"/>

      <sch:report test="($tipoDoc = '01' or $tipoDoc = '03') and not(cac:TaxTotal[1]/cbc:TaxAmount > 0)">
        La Factura/Boleta debe incluir al menos un TaxTotal con IGV cuando hay operaciones gravadas.
      </sch:report>

    </sch:rule>

    <!-- Regla: TaxTotal.SUBTOTAL (cbc:TaxSubtotal) no debe tener monto negativo -->
    <sch:rule context="cac:TaxTotal/cac:TaxSubtotal">
      <sch:assert test="cbc:TaxAmount >= 0 or cbc:TaxAmount = ''"
                 diagnostics="diag-tax-monto-negativo">
        El monto del subtotal de impuesto no puede ser negativo:
        <sch:value-of select="cbc:TaxAmount"/>
      </sch:assert>
    </sch:rule>

  </sch:pattern>

  <!-- ========================================================== -->
  <!-- PATTERN: Totales                                           -->
  <!-- ========================================================== -->
  <sch:pattern id="totales" name="Validaciones de totales">

    <!-- El PayableAmount debe coincidir con la suma lógica -->
    <sch:rule context="/cac:Invoice">
      <sch:let name="lineExt" value="cac:LegalMonetaryTotal/cbc:LineExtensionAmount"/>
      <sch:let name="payable" value="cac:LegalMonetaryTotal/cbc:PayableAmount"/>

      <sch:report test="exists($lineExt) and exists($payable) and number($payable) > number($lineExt)">
        El monto total a pagar (S/. <sch:value-of select="$payable"/>)
        no puede ser mayor que el subtotal de línea
        (S/. <sch:value-of select="$lineExt"/>) sin considerar descuentos globales.
        Revise los cálculos de impuestos y descuentos.
      </sch:report>
    </sch:rule>

  </sch:pattern>

  <!-- ========================================================== -->
  <!-- PATTERN: Documentos de referencia                           -->
  <!-- ========================================================== -->
  <sch:pattern id="referencias" name="Validaciones de documentos de referencia">

    <!-- Nota de Crédito debe tener documento de referencia -->
    <sch:rule context="/cac:CreditNote">
      <sch:assert test="cac:BillingReference/cac:InvoiceDocumentReference/cbc:ID"
                 diagnostics="diag-ref-credito">
        La Nota de Crédito debe incluir un documento de referencia (InvoiceDocumentReference).
      </sch:assert>
    </sch:rule>

    <!-- Nota de Débito debe tener documento de referencia -->
    <sch:rule context="/cac:DebitNote">
      <sch:assert test="cac:BillingReference/cac:InvoiceDocumentReference/cbc:ID"
                 diagnostics="diag-ref-debito">
        La Nota de Débito debe incluir un documento de referencia (InvoiceDocumentReference).
      </sch:assert>
    </sch:rule>

  </sch:pattern>

  <!-- ========================================================== -->
  <!-- PATTERN: ISC                                               -->
  <!-- ========================================================== -->
  <sch:pattern id="isc" name="Validaciones ISC">

    <!-- El monto ISC total debe corresponder a la categoría ISC -->
    <sch:rule context="cac:TaxTotal[cac:TaxSubtotal/cac:TaxCategory/cac:TaxScheme/cbc:ID='2000']">
      <sch:report test="cbc:TaxAmount &lt; 0"
                 diagnostics="diag-isc-negativo">
        El monto ISC total no puede ser negativo.
      </sch:report>
    </sch:rule>

  </sch:pattern>

  <!-- ========================================================== -->
  <!-- DIAGNÓSTICOS                                                -->
  <!-- ========================================================== -->
  <sch:diagnostics>
    <sch:diagnostic id="diag-ruc-obligatorio">
      SUNAT: Monto &gt; 700 → RUC obligatorio (Art. 4° R.S. 007-99/SUNAT)
    </sch:diagnostic>
    <sch:diagnostic id="diag-nombre-receptor">
      SUNAT: El receptor RUC debe tener nombre registrado
    </sch:diagnostic>
    <sch:diagnostic id="diag-tax-monto-negativo">
      El subtotal de impuesto tiene valor negativo inesperado
    </sch:diagnostic>
    <sch:diagnostic id="diag-ref-credito">
      SUNAT: NC sin documento de referencia será rechazada
    </sch:diagnostic>
    <sch:diagnostic id="diag-ref-debito">
      SUNAT: ND sin documento de referencia será rechazada
    </sch:diagnostic>
    <sch:diagnostic id="diag-isc-negativo">
      ISC negativo no es válido en SUNAT
    </sch:diagnostic>
  </sch:diagnostics>

</sch:schema>
