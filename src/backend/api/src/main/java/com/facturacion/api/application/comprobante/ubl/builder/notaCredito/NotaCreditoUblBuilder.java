package com.facturacion.api.application.comprobante.ubl.builder.notaCredito;

import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.DocumentoAdicionalUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.NotaCreditoLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.NotaCreditoUblData;
import com.helger.ubl21.UBL21Marshaller;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Constructor de XML UBL 2.1 para Nota de Crédito Electrónica.
 * Sigue el mismo patrón que FacturaUblBuilder.
 *
 * Correcciones aplicadas:
 * - BillingReference usa setCreditNoteDocumentReference (no InvoiceDocumentReference)
 * - PricingReference con PriceTypeCode="01" por línea (obligatorio SUNAT)
 * - AddressTypeCode en emisor (campo 18)
 * - schemeID dinámico para receptor según tipo de documento
 * - DespatchDocumentReference para guía de remisión
 * - AdditionalDocumentReference para documentos adicionales
 */
@Component
public class NotaCreditoUblBuilder {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String VERSION_UBL        = "2.1";
    private static final String VERSION_ESTRUCTURA = "2.0";

    private static final String CODIGO_TRIBUTO_IGV       = "1000";
    private static final String CODIGO_TRIBUTO_EXONERADO = "9997";
    private static final String CODIGO_TRIBUTO_INAFECTO  = "9998";

    private static final String NOMBRE_IGV       = "IGV";
    private static final String NOMBRE_EXONERADO = "EXONERADO";
    private static final String NOMBRE_INAFECTO  = "INAFECTO";

    private static final String TIPO_TRIBUTO_VAT = "VAT";
    private static final String TIPO_TRIBUTO_FRE = "FRE";

    private static final String CATEGORIA_GRAVADO   = "S";
    private static final String CATEGORIA_EXONERADO = "E";
    private static final String CATEGORIA_INAFECTO  = "O";

    private static final BigDecimal PORCENTAJE_IGV = new BigDecimal("18.00");

    /**
     * Construye el objeto CreditNoteType UBL 2.1 a partir de los datos canónicos,
     * sin serializar a XML.
     *
     * @param data datos de la nota de crédito en formato canónico
     * @return CreditNoteType construido (no serializado)
     */
    public CreditNoteType buildCreditNote(NotaCreditoUblData data) {
        CreditNoteType nc = new CreditNoteType();

        configurarEncabezado(nc, data);
        configurarMotivo(nc, data);
        configurarDocumentoAfectado(nc, data);
        configurarGuiaRemisionSiExiste(nc, data);
        configurarDocumentosAdicionalesSiExisten(nc, data);
        configurarPartes(nc, data);
        configurarImpuestos(nc, data);
        configurarTotalesMonetarios(nc, data);
        configurarLineas(nc, data);

        return nc;
    }

    public String construirXml(NotaCreditoUblData data) throws Exception {
        CreditNoteType nc = buildCreditNote(data);
        return serializar(nc);
    }

    // =========================================================================
    // ENCABEZADO
    // =========================================================================

    private void configurarEncabezado(CreditNoteType nc, NotaCreditoUblData data) {
        nc.setUBLVersionID(VERSION_UBL);
        nc.setCustomizationID(VERSION_ESTRUCTURA);
        nc.setID(data.serie() + "-" + data.correlativo());

        if (data.fechaEmision() != null && !data.fechaEmision().isBlank()) {
            nc.setIssueDate(LocalDate.parse(data.fechaEmision(), FORMATO_FECHA));
        }

        DocumentCurrencyCodeType moneda = new DocumentCurrencyCodeType();
        moneda.setListID("ISO 4217 Alpha");
        moneda.setName("Currency");
        moneda.setListAgencyName("United Nations Economic Commission for Europe");
        moneda.setValue(nvl(data.moneda(), "PEN"));
        nc.setDocumentCurrencyCode(moneda);
    }

    // =========================================================================
    // MOTIVO — DiscrepancyResponse (catálogo 09)
    // =========================================================================

    private void configurarMotivo(CreditNoteType nc, NotaCreditoUblData data) {
        ResponseType response = new ResponseType();

        ReferenceIDType refId = new ReferenceIDType();
        refId.setValue(nvl(data.documentoAfectadoSerie()) + "-" + nvl(data.documentoAfectadoNumero()));
        response.setReferenceID(refId);

        ResponseCodeType responseCode = new ResponseCodeType();
        responseCode.setListAgencyName("PE:SUNAT");
        responseCode.setListName("Tipo de nota de crédito");
        responseCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo09");
        responseCode.setValue(nvl(data.codigoMotivo(), "01"));
        response.setResponseCode(responseCode);

        DescriptionType desc = new DescriptionType();
        desc.setValue(nvl(data.descripcionMotivo()));
        response.addDescription(desc);

        nc.addDiscrepancyResponse(response);
    }

    // =========================================================================
    // DOCUMENTO AFECTADO — BillingReference
    // 🔴 CORRECCIÓN: setCreditNoteDocumentReference (no InvoiceDocumentReference)
    // =========================================================================

    private void configurarDocumentoAfectado(CreditNoteType nc, NotaCreditoUblData data) {
        BillingReferenceType billingRef = new BillingReferenceType();
        DocumentReferenceType docRef = new DocumentReferenceType();

        IDType docRefId = new IDType();
        docRefId.setValue(nvl(data.documentoAfectadoSerie()) + "-" + nvl(data.documentoAfectadoNumero()));
        docRef.setID(docRefId);

        DocumentTypeCodeType docTypeCode = new DocumentTypeCodeType();
        docTypeCode.setListAgencyName("PE:SUNAT");
        docTypeCode.setListName("SUNAT:Identificador de Tipo de Documento");
        docTypeCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01");
        docTypeCode.setValue(nvl(data.documentoAfectadoTipo(), "01"));
        docRef.setDocumentTypeCode(docTypeCode);

        // 🔴 CORRECCIÓN CRÍTICA: para NC se usa CreditNoteDocumentReference
        billingRef.setCreditNoteDocumentReference(docRef);
        nc.addBillingReference(billingRef);
    }

    // =========================================================================
    // GUÍA DE REMISIÓN — DespatchDocumentReference (campos 12-13)
    // =========================================================================

    private void configurarGuiaRemisionSiExiste(CreditNoteType nc, NotaCreditoUblData data) {
        if (data.guiaRemisionId() == null || data.guiaRemisionId().isBlank()) return;

        DocumentReferenceType ref = new DocumentReferenceType();

        IDType id = new IDType();
        id.setValue(data.guiaRemisionId());
        ref.setID(id);

        DocumentTypeCodeType typeCode = new DocumentTypeCodeType();
        typeCode.setValue(nvl(data.guiaRemisionCodigo(), "09"));
        typeCode.setListAgencyName("PE:SUNAT");
        typeCode.setListName("SUNAT:Identificador de guía relacionada");
        typeCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01");
        ref.setDocumentTypeCode(typeCode);

        nc.addDespatchDocumentReference(ref);
    }

    // =========================================================================
    // DOCUMENTOS ADICIONALES — AdditionalDocumentReference (campos 14-15)
    // =========================================================================

    private void configurarDocumentosAdicionalesSiExisten(CreditNoteType nc, NotaCreditoUblData data) {
        if (data.documentosAdicionales() == null || data.documentosAdicionales().isEmpty()) return;

        for (DocumentoAdicionalUblData doc : data.documentosAdicionales()) {
            DocumentReferenceType ref = new DocumentReferenceType();

            IDType id = new IDType();
            id.setValue(nvl(doc.id()));
            ref.setID(id);

            DocumentTypeCodeType typeCode = new DocumentTypeCodeType();
            typeCode.setValue(nvl(doc.tipoDocumento(), "99"));
            typeCode.setListAgencyName("PE:SUNAT");
            typeCode.setListName("SUNAT:Identificador de documento relacionado");
            typeCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo12");
            ref.setDocumentTypeCode(typeCode);

            nc.addAdditionalDocumentReference(ref);
        }
    }

    // =========================================================================
    // EMISOR Y RECEPTOR
    // =========================================================================

    private void configurarPartes(CreditNoteType nc, NotaCreditoUblData data) {
        nc.setAccountingSupplierParty(crearEmisor(data));
        nc.setAccountingCustomerParty(crearReceptor(data));
    }

    private SupplierPartyType crearEmisor(NotaCreditoUblData data) {
        SupplierPartyType supplier = new SupplierPartyType();
        PartyType party = new PartyType();

        if (data.emisorNroDocumento() != null) {
            PartyTaxSchemeType taxScheme = new PartyTaxSchemeType();
            taxScheme.setRegistrationName(nvl(data.emisorRazonSocial()));
            taxScheme.setCompanyID(crearCompanyID(data.emisorNroDocumento(), "6"));

            // Campo 18 — AddressTypeCode (código domicilio fiscal)
            AddressType address = new AddressType();
            AddressTypeCodeType addressCode = new AddressTypeCodeType();
            addressCode.setValue(nvl(data.emisorCodigoDomicilio(), "0001"));
            address.setAddressTypeCode(addressCode);
            taxScheme.setRegistrationAddress(address);

            TaxSchemeType scheme = new TaxSchemeType();
            IDType schemeId = new IDType();
            schemeId.setSchemeID("UN/ECE 5153");
            schemeId.setSchemeAgencyID("6");
            schemeId.setValue(CODIGO_TRIBUTO_IGV);
            scheme.setID(schemeId);
            scheme.setName(NOMBRE_IGV);
            taxScheme.setTaxScheme(scheme);

            party.addPartyTaxScheme(taxScheme);
        }

        if (data.emisorRazonSocial() != null) {
            PartyLegalEntityType ple = new PartyLegalEntityType();
            ple.setRegistrationName(data.emisorRazonSocial());

            AddressType regAddress = new AddressType();
            AddressTypeCodeType addressCode = new AddressTypeCodeType();
            addressCode.setValue(nvl(data.emisorCodigoDomicilio(), "0001"));
            regAddress.setAddressTypeCode(addressCode);
            ple.setRegistrationAddress(regAddress);

            party.addPartyLegalEntity(ple);
        }

        supplier.setParty(party);
        return supplier;
    }

    private CustomerPartyType crearReceptor(NotaCreditoUblData data) {
        CustomerPartyType customer = new CustomerPartyType();
        PartyType party = new PartyType();

        if (data.receptorNroDocumento() != null) {
            PartyTaxSchemeType taxScheme = new PartyTaxSchemeType();
            taxScheme.setRegistrationName(nvl(data.receptorRazonSocial()));

            // Campo 19 — schemeID dinámico según tipo de documento del receptor
            String tipoDocReceptor = nvl(data.receptorTipoDocumento(), "6");
            taxScheme.setCompanyID(crearCompanyID(data.receptorNroDocumento(), tipoDocReceptor));

            TaxSchemeType scheme = new TaxSchemeType();
            scheme.setID("-");
            taxScheme.setTaxScheme(scheme);

            party.addPartyTaxScheme(taxScheme);
        }

        customer.setParty(party);
        return customer;
    }

    private CompanyIDType crearCompanyID(String nroDocumento, String schemeId) {
        CompanyIDType companyId = new CompanyIDType();
        companyId.setSchemeID(schemeId);
        companyId.setSchemeName("SUNAT:Identificador de Documento de Identidad");
        companyId.setSchemeAgencyName("PE:SUNAT");
        companyId.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
        companyId.setValue(nroDocumento);
        return companyId;
    }

    // =========================================================================
    // IMPUESTOS — TaxTotal
    // =========================================================================

    private void configurarImpuestos(CreditNoteType nc, NotaCreditoUblData data) {
        String moneda = nvl(data.moneda(), "PEN");
        BigDecimal totalImpuestos = valorOZero(data.totalImpuestos());

        TaxTotalType taxTotal = new TaxTotalType();
        taxTotal.setTaxAmount(totalImpuestos).setCurrencyID(moneda);

        BigDecimal gravadas   = valorOZero(data.gravadas());
        BigDecimal igv        = valorOZero(data.igv());
        BigDecimal exoneradas = valorOZero(data.exoneradas());
        BigDecimal inafectas  = valorOZero(data.inafectas());

        if (gravadas.compareTo(BigDecimal.ZERO) > 0) {
            taxTotal.addTaxSubtotal(crearSubtotalGravado(gravadas, igv, moneda));
        }
        if (exoneradas.compareTo(BigDecimal.ZERO) > 0) {
            taxTotal.addTaxSubtotal(crearSubtotalExonerado(exoneradas, moneda));
        }
        if (inafectas.compareTo(BigDecimal.ZERO) > 0) {
            taxTotal.addTaxSubtotal(crearSubtotalInafecto(inafectas, moneda));
        }

        // Fallback: si no hay subtotales pero hay impuesto total
        if (gravadas.compareTo(BigDecimal.ZERO) <= 0
                && exoneradas.compareTo(BigDecimal.ZERO) <= 0
                && inafectas.compareTo(BigDecimal.ZERO) <= 0
                && totalImpuestos.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal base = totalImpuestos.divide(new BigDecimal("0.18"), 2, RoundingMode.HALF_UP);
            taxTotal.addTaxSubtotal(crearSubtotalGravado(base, totalImpuestos, moneda));
        }

        nc.addTaxTotal(taxTotal);
    }

    private TaxSubtotalType crearSubtotalGravado(BigDecimal base, BigDecimal igv, String moneda) {
        TaxSubtotalType sub = new TaxSubtotalType();
        sub.setTaxableAmount(base).setCurrencyID(moneda);
        sub.setTaxAmount(igv).setCurrencyID(moneda);
        sub.setTaxCategory(crearCategoria(CATEGORIA_GRAVADO, PORCENTAJE_IGV,
                CODIGO_TRIBUTO_IGV, NOMBRE_IGV, TIPO_TRIBUTO_VAT));
        return sub;
    }

    private TaxSubtotalType crearSubtotalExonerado(BigDecimal base, String moneda) {
        TaxSubtotalType sub = new TaxSubtotalType();
        sub.setTaxableAmount(base).setCurrencyID(moneda);
        sub.setTaxAmount(BigDecimal.ZERO).setCurrencyID(moneda);
        sub.setTaxCategory(crearCategoria(CATEGORIA_EXONERADO, null,
                CODIGO_TRIBUTO_EXONERADO, NOMBRE_EXONERADO, TIPO_TRIBUTO_VAT));
        return sub;
    }

    private TaxSubtotalType crearSubtotalInafecto(BigDecimal base, String moneda) {
        TaxSubtotalType sub = new TaxSubtotalType();
        sub.setTaxableAmount(base).setCurrencyID(moneda);
        sub.setTaxAmount(BigDecimal.ZERO).setCurrencyID(moneda);
        sub.setTaxCategory(crearCategoria(CATEGORIA_INAFECTO, null,
                CODIGO_TRIBUTO_INAFECTO, NOMBRE_INAFECTO, TIPO_TRIBUTO_FRE));
        return sub;
    }

    private TaxCategoryType crearCategoria(String codigoCategoria, @Nullable BigDecimal porcentaje,
            String codigoTributo, String nombreTributo, String tipoTributo) {
        TaxCategoryType cat = new TaxCategoryType();

        IDType idCat = new IDType();
        idCat.setSchemeID("UN/ECE 5305");
        idCat.setSchemeName("Tax Category Identifier");
        idCat.setSchemeAgencyName("United Nations Economic Commission for Europe");
        idCat.setValue(codigoCategoria);
        cat.setID(idCat);

        if (porcentaje != null) cat.setPercent(porcentaje);

        TaxSchemeType scheme = new TaxSchemeType();
        IDType schemeId = new IDType();
        schemeId.setSchemeID("UN/ECE 5153");
        schemeId.setSchemeAgencyID("6");
        schemeId.setValue(codigoTributo);
        scheme.setID(schemeId);
        scheme.setName(nombreTributo);
        scheme.setTaxTypeCode(tipoTributo);
        cat.setTaxScheme(scheme);

        return cat;
    }

    // =========================================================================
    // TOTALES MONETARIOS — LegalMonetaryTotal
    // =========================================================================

    private void configurarTotalesMonetarios(CreditNoteType nc, NotaCreditoUblData data) {
        String moneda = nvl(data.moneda(), "PEN");
        MonetaryTotalType total = new MonetaryTotalType();

        total.setLineExtensionAmount(valorOZero(data.valorVenta())).setCurrencyID(moneda);
        total.setTaxInclusiveAmount(valorOZero(data.importeTotal())).setCurrencyID(moneda);
        total.setPayableAmount(valorOZero(data.importeTotal())).setCurrencyID(moneda);

        nc.setLegalMonetaryTotal(total);
    }

    // =========================================================================
    // LÍNEAS — CreditNoteLine
    // =========================================================================

    private void configurarLineas(CreditNoteType nc, NotaCreditoUblData data) {
        if (data.lineas() == null || data.lineas().isEmpty()) return;

        String moneda = nvl(data.moneda(), "PEN");
        int idx = 1;
        for (NotaCreditoLineaUblData linea : data.lineas()) {
            nc.addCreditNoteLine(crearLinea(linea, idx++, moneda));
        }
    }

    private CreditNoteLineType crearLinea(NotaCreditoLineaUblData l, int idx, String moneda) {
        CreditNoteLineType line = new CreditNoteLineType();

        line.setID(l.numero() != null ? String.valueOf(l.numero()) : String.valueOf(idx));

        // Cantidad
        CreditedQuantityType qty = new CreditedQuantityType();
        qty.setValue(valorOZero(l.cantidad()).compareTo(BigDecimal.ZERO) > 0 ? l.cantidad() : BigDecimal.ONE);
        qty.setUnitCode(nvl(l.unidadMedida(), "NIU"));
        qty.setUnitCodeListID("UN/ECE rec 20");
        qty.setUnitCodeListAgencyName("United Nations Economic Commission for Europe");
        line.setCreditedQuantity(qty);

        // Valor de venta
        line.setLineExtensionAmount(valorOZero(l.valorVenta())).setCurrencyID(moneda);

        // Campo 33-34 — PricingReference con PriceTypeCode="01" (obligatorio SUNAT)
        line.setPricingReference(crearPricingReference(l, moneda));

        // TaxTotal por línea
        line.addTaxTotal(crearImpuestoLinea(l, moneda));

        // Item
        ItemType item = new ItemType();
        if (l.descripcion() != null && !l.descripcion().isBlank()) {
            DescriptionType desc = new DescriptionType();
            desc.setValue(l.descripcion());
            item.setDescription(Collections.singletonList(desc));
        }
        if (l.codigoProducto() != null && !l.codigoProducto().isBlank()) {
            ItemIdentificationType sellerId = new ItemIdentificationType();
            IDType pid = new IDType();
            pid.setValue(l.codigoProducto());
            sellerId.setID(pid);
            item.setSellersItemIdentification(sellerId);
        }
        line.setItem(item);

        // Precio unitario (Price)
        BigDecimal cantidad = valorOZero(l.cantidad());
        if (cantidad.compareTo(BigDecimal.ZERO) > 0) {
            PriceType price = new PriceType();
            price.setPriceAmount(valorOZero(l.valorVenta())
                    .divide(cantidad, 2, RoundingMode.HALF_UP))
                    .setCurrencyID(moneda);
            line.setPrice(price);
        }

        return line;
    }

    /**
     * Campo 33-34: PricingReference con AlternativeConditionPrice.
     * PriceTypeCode="01" = precio unitario con IGV (obligatorio SUNAT).
     * PriceTypeCode="02" = precio unitario sin IGV (para operaciones gratuitas).
     */
    private PricingReferenceType crearPricingReference(NotaCreditoLineaUblData l, String moneda) {
        PricingReferenceType pricingRef = new PricingReferenceType();

        BigDecimal cantidad = valorOZero(l.cantidad());
        BigDecimal valorVenta = valorOZero(l.valorVenta());
        BigDecimal montoIgv   = valorOZero(l.montoIGV());

        // Precio con IGV = (valorVenta + IGV) / cantidad
        BigDecimal precioConIgv = cantidad.compareTo(BigDecimal.ZERO) > 0
                ? valorVenta.add(montoIgv).divide(cantidad, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        PriceType precio = new PriceType();
        precio.setPriceAmount(precioConIgv).setCurrencyID(moneda);

        PriceTypeCodeType priceTypeCode = new PriceTypeCodeType();
        priceTypeCode.setValue("01"); // 01 = precio unitario con IGV
        priceTypeCode.setListName("SUNAT:Indicador de Tipo de Precio");
        priceTypeCode.setListAgencyName("PE:SUNAT");
        priceTypeCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16");
        precio.setPriceTypeCode(priceTypeCode);

        pricingRef.addAlternativeConditionPrice(precio);
        return pricingRef;
    }

    private TaxTotalType crearImpuestoLinea(NotaCreditoLineaUblData l, String moneda) {
        BigDecimal montoIgv  = valorOZero(l.montoIGV());
        BigDecimal montoBase = valorOZero(l.valorVenta());
        String tipoAfectacion = l.tipoAfectacionIGV() != null ? l.tipoAfectacionIGV() : "10";

        TaxTotalType taxTotal = new TaxTotalType();
        taxTotal.setTaxAmount(montoIgv).setCurrencyID(moneda);

        TaxSubtotalType sub = new TaxSubtotalType();
        sub.setTaxableAmount(montoBase).setCurrencyID(moneda);
        sub.setTaxAmount(montoIgv).setCurrencyID(moneda);

        String codigoCategoria;
        String codigoTributo;
        String nombreTributo;
        String tipoTributo;

        switch (tipoAfectacion) {
            case "20" -> { codigoCategoria = CATEGORIA_EXONERADO; codigoTributo = CODIGO_TRIBUTO_EXONERADO;
                           nombreTributo = NOMBRE_EXONERADO; tipoTributo = TIPO_TRIBUTO_VAT; }
            case "30", "31" -> { codigoCategoria = CATEGORIA_INAFECTO; codigoTributo = CODIGO_TRIBUTO_INAFECTO;
                                 nombreTributo = NOMBRE_INAFECTO; tipoTributo = TIPO_TRIBUTO_FRE; }
            default -> { codigoCategoria = CATEGORIA_GRAVADO; codigoTributo = CODIGO_TRIBUTO_IGV;
                         nombreTributo = NOMBRE_IGV; tipoTributo = TIPO_TRIBUTO_VAT; }
        }

        TaxCategoryType cat = new TaxCategoryType();

        IDType idCat = new IDType();
        idCat.setSchemeID("UN/ECE 5305");
        idCat.setSchemeName("Tax Category Identifier");
        idCat.setSchemeAgencyName("United Nations Economic Commission for Europe");
        idCat.setValue(codigoCategoria);
        cat.setID(idCat);

        if (CATEGORIA_GRAVADO.equals(codigoCategoria)) cat.setPercent(PORCENTAJE_IGV);

        TaxExemptionReasonCodeType exemption = new TaxExemptionReasonCodeType();
        exemption.setValue(tipoAfectacion);
        exemption.setListAgencyName("PE:SUNAT");
        exemption.setListName("SUNAT:Codigo de Tipo de Afectación del IGV");
        exemption.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07");
        cat.setTaxExemptionReasonCode(exemption);

        TaxSchemeType scheme = new TaxSchemeType();
        IDType schemeId = new IDType();
        schemeId.setSchemeID("UN/ECE 5153");
        schemeId.setSchemeAgencyID("6");
        schemeId.setValue(codigoTributo);
        scheme.setID(schemeId);
        scheme.setName(nombreTributo);
        scheme.setTaxTypeCode(tipoTributo);
        cat.setTaxScheme(scheme);

        sub.setTaxCategory(cat);
        taxTotal.addTaxSubtotal(sub);
        return taxTotal;
    }

    // =========================================================================
    // SERIALIZACIÓN
    // =========================================================================

    public String serializar(CreditNoteType nc) throws Exception {
        StringWriter writer = new StringWriter();
        UBL21Marshaller.creditNote()
                .setFormattedOutput(true)
                .setUseSchema(false)
                .write(nc, writer);
        return writer.toString();
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static String nvl(String val) {
        return val != null ? val : "";
    }

    private static String nvl(String val, String fallback) {
        return (val != null && !val.isBlank()) ? val : fallback;
    }

    private static BigDecimal valorOZero(@Nullable BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
