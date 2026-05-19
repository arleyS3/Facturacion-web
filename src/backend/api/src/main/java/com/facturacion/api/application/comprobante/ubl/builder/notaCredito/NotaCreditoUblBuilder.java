package com.facturacion.api.application.comprobante.ubl.builder.notaCredito;

import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.NotaCreditoLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.NotaCreditoUblData;
import com.helger.ubl21.UBL21Marshaller;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Constructor de XML UBL 2.1 para Nota de Crédito Electrónica.
 * Sigue el mismo patrón que {@link com.facturacion.api.application.comprobante.ubl.builder.factura.FacturaUblBuilder}.
 */
@Component
public class NotaCreditoUblBuilder {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_HORA  = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String VERSION_UBL       = "2.1";
    private static final String VERSION_ESTRUCTURA = "2.0";

    private static final String CODIGO_TRIBUTO_IGV      = "1000";
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
     * Construye el XML de nota de crédito UBL 2.1.
     *
     * @param data datos de la nota de crédito
     * @return XML generado
     * @throws Exception si hay error al generar
     */
    public String construirXml(NotaCreditoUblData data) throws Exception {
        CreditNoteType nc = new CreditNoteType();

        configurarEncabezado(nc, data);
        configurarMotivo(nc, data);
        configurarDocumentoAfectado(nc, data);
        configurarPartes(nc, data);
        configurarImpuestos(nc, data);
        configurarTotalesMonetarios(nc, data);
        configurarLineas(nc, data);

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
        moneda.setValue(data.moneda() != null ? data.moneda() : "PEN");
        nc.setDocumentCurrencyCode(moneda);
    }

    // =========================================================================
    // MOTIVO (DiscrepancyResponse — catálogo 09)
    // =========================================================================

    private void configurarMotivo(CreditNoteType nc, NotaCreditoUblData data) {
        ResponseType response = new ResponseType();

        ReferenceIDType refId = new ReferenceIDType();
        String docAfectado = nvl(data.documentoAfectadoSerie()) + "-" + nvl(data.documentoAfectadoNumero());
        refId.setValue(docAfectado);
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
    // DOCUMENTO AFECTADO (BillingReference)
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

        billingRef.setInvoiceDocumentReference(docRef);
        nc.addBillingReference(billingRef);
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

        // PartyTaxScheme con RUC
        if (data.emisorNroDocumento() != null) {
            PartyTaxSchemeType taxScheme = new PartyTaxSchemeType();
            taxScheme.setRegistrationName(nvl(data.emisorRazonSocial()));
            taxScheme.setCompanyID(crearCompanyID(data.emisorNroDocumento()));

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

        // PartyLegalEntity
        if (data.emisorRazonSocial() != null) {
            PartyLegalEntityType ple = new PartyLegalEntityType();
            ple.setRegistrationName(data.emisorRazonSocial());
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
            taxScheme.setCompanyID(crearCompanyID(data.receptorNroDocumento()));

            TaxSchemeType scheme = new TaxSchemeType();
            scheme.setID("-");
            taxScheme.setTaxScheme(scheme);

            party.addPartyTaxScheme(taxScheme);
        }

        customer.setParty(party);
        return customer;
    }

    private CompanyIDType crearCompanyID(String nroDocumento) {
        CompanyIDType companyId = new CompanyIDType();
        companyId.setSchemeID("6");
        companyId.setSchemeName("SUNAT:Identificador de Documento de Identidad");
        companyId.setSchemeAgencyName("PE:SUNAT");
        companyId.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
        companyId.setValue(nroDocumento);
        return companyId;
    }

    // =========================================================================
    // IMPUESTOS (TaxTotal)
    // =========================================================================

    private void configurarImpuestos(CreditNoteType nc, NotaCreditoUblData data) {
        String moneda = nvl(data.moneda(), "PEN");
        BigDecimal totalImpuestos = valorOZero(data.totalImpuestos());

        TaxTotalType taxTotal = new TaxTotalType();
        taxTotal.setTaxAmount(totalImpuestos).setCurrencyID(moneda);

        // Subtotal gravadas
        BigDecimal gravadas = valorOZero(data.gravadas());
        BigDecimal igv = valorOZero(data.igv());
        if (gravadas.compareTo(BigDecimal.ZERO) > 0) {
            taxTotal.addTaxSubtotal(crearSubtotalGravado(gravadas, igv, moneda));
        }

        // Subtotal exoneradas
        BigDecimal exoneradas = valorOZero(data.exoneradas());
        if (exoneradas.compareTo(BigDecimal.ZERO) > 0) {
            taxTotal.addTaxSubtotal(crearSubtotalExonerado(exoneradas, moneda));
        }

        // Subtotal inafectas
        BigDecimal inafectas = valorOZero(data.inafectas());
        if (inafectas.compareTo(BigDecimal.ZERO) > 0) {
            taxTotal.addTaxSubtotal(crearSubtotalInafecto(inafectas, moneda));
        }

        // Si no hay subtotales específicos, agregar uno genérico con el total
        if (gravadas.compareTo(BigDecimal.ZERO) <= 0
                && exoneradas.compareTo(BigDecimal.ZERO) <= 0
                && inafectas.compareTo(BigDecimal.ZERO) <= 0
                && totalImpuestos.compareTo(BigDecimal.ZERO) > 0) {
            taxTotal.addTaxSubtotal(crearSubtotalGravado(
                totalImpuestos.divide(new BigDecimal("0.18"), 2, RoundingMode.HALF_UP),
                totalImpuestos, moneda));
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

        if (porcentaje != null) {
            cat.setPercent(porcentaje);
        }

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
    // TOTALES MONETARIOS (LegalMonetaryTotal)
    // =========================================================================

    private void configurarTotalesMonetarios(CreditNoteType nc, NotaCreditoUblData data) {
        String moneda = nvl(data.moneda(), "PEN");
        MonetaryTotalType total = new MonetaryTotalType();

        BigDecimal valorVenta   = valorOZero(data.valorVenta());
        BigDecimal importeTotal = valorOZero(data.importeTotal());

        total.setLineExtensionAmount(valorVenta).setCurrencyID(moneda);
        total.setTaxInclusiveAmount(importeTotal).setCurrencyID(moneda);
        total.setPayableAmount(importeTotal).setCurrencyID(moneda);

        nc.setLegalMonetaryTotal(total);
    }

    // =========================================================================
    // LÍNEAS (CreditNoteLine)
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

        // ID
        line.setID(l.numero() != null ? String.valueOf(l.numero()) : String.valueOf(idx));

        // Cantidad
        CreditedQuantityType qty = new CreditedQuantityType();
        qty.setValue(l.cantidad() != null ? l.cantidad() : BigDecimal.ONE);
        qty.setUnitCode(nvl(l.unidadMedida(), "NIU"));
        qty.setUnitCodeListID("UN/ECE rec 20");
        qty.setUnitCodeListAgencyName("United Nations Economic Commission for Europe");
        line.setCreditedQuantity(qty);

        // Valor de venta de la línea
        BigDecimal valorVenta = valorOZero(l.valorVenta());
        line.setLineExtensionAmount(valorVenta).setCurrencyID(moneda);

        // TaxTotal de la línea
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

        // Precio unitario (valorUnitario)
        if (l.porcentajeIGV() != null) {
            PriceType price = new PriceType();
            price.setPriceAmount(valorOZero(l.valorVenta())
                .divide(l.cantidad() != null && l.cantidad().compareTo(BigDecimal.ZERO) > 0
                    ? l.cantidad() : BigDecimal.ONE, 2, RoundingMode.HALF_UP))
                .setCurrencyID(moneda);
            line.setPrice(price);
        }

        return line;
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

        // Categoría según tipo de afectación
        TaxCategoryType cat = new TaxCategoryType();

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

        IDType idCat = new IDType();
        idCat.setSchemeID("UN/ECE 5305");
        idCat.setSchemeName("Tax Category Identifier");
        idCat.setSchemeAgencyName("United Nations Economic Commission for Europe");
        idCat.setValue(codigoCategoria);
        cat.setID(idCat);

        if (CATEGORIA_GRAVADO.equals(codigoCategoria)) {
            cat.setPercent(PORCENTAJE_IGV);
        }

        // TaxExemptionReasonCode (catálogo 07)
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

    private String serializar(CreditNoteType nc) throws Exception {
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
