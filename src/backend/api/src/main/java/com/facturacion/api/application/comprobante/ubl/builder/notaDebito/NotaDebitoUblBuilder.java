package com.facturacion.api.application.comprobante.ubl.builder.notaDebito;

import com.facturacion.api.application.comprobante.ubl.mapper.notaDebito.NotaDebitoLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.notaDebito.NotaDebitoUblData;
import com.helger.ubl21.UBL21Marshaller;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.debitnote_21.DebitNoteType;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * Constructor de XML UBL 2.1 para Nota de Débito Electrónica.
 */
@Component
public class NotaDebitoUblBuilder {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String VERSION_UBL = "2.1";
    private static final String VERSION_ESTRUCTURA = "2.0";

    /**
     * Construye el objeto DebitNoteType UBL 2.1 a partir de los datos canónicos,
     * sin serializar a XML.
     *
     * @param data datos de la nota de débito en formato canónico
     * @return DebitNoteType construido (no serializado)
     */
    public DebitNoteType buildDebitNote(NotaDebitoUblData data) {
        DebitNoteType notaDebito = new DebitNoteType();
        
        // 1. Encabezado principal
        notaDebito.setUBLVersionID(VERSION_UBL);
        notaDebito.setCustomizationID(VERSION_ESTRUCTURA);
        notaDebito.setID(data.serie() + "-" + data.correlativo());
        
        if (data.fechaEmision() != null) {
            notaDebito.setIssueDate(LocalDate.parse(data.fechaEmision(), FECHA_FMT));
        }
        
        String moneda = data.moneda() != null ? data.moneda() : "PEN";
        notaDebito.setDocumentCurrencyCode(moneda);

        // 2. DiscrepancyResponse (Motivo de la Nota de Débito)
        ResponseType discrepancy = new ResponseType();
        discrepancy.setReferenceID(data.documentoAfectadoSerie() + "-" + data.documentoAfectadoNumero());
        if (data.codigoMotivo() != null) {
            ResponseCodeType respCode = new ResponseCodeType();
            respCode.setValue(data.codigoMotivo());
            discrepancy.setResponseCode(respCode);
        }
        if (data.descripcionMotivo() != null) {
            DescriptionType desc = new DescriptionType();
            desc.setValue(data.descripcionMotivo());
            discrepancy.addDescription(desc);
        }
        notaDebito.addDiscrepancyResponse(discrepancy);

        // 3. BillingReference (Documento original modificado)
        BillingReferenceType billingRef = new BillingReferenceType();
        DocumentReferenceType docRef = new DocumentReferenceType();
        docRef.setID(data.documentoAfectadoSerie() + "-" + data.documentoAfectadoNumero());
        if (data.documentoAfectadoTipo() != null) {
            DocumentTypeCodeType docTypeCode = new DocumentTypeCodeType();
            docTypeCode.setValue(data.documentoAfectadoTipo());
            docRef.setDocumentTypeCode(docTypeCode);
        }
        billingRef.setInvoiceDocumentReference(docRef);
        notaDebito.addBillingReference(billingRef);

        // 4. Entidades (PartyTaxScheme)
        notaDebito.setAccountingSupplierParty(crearEmisor(data));
        notaDebito.setAccountingCustomerParty(crearReceptor(data));
        
        // 5. Impuestos Globales (TaxTotal global)
        if (data.totalImpuestos() != null) {
            notaDebito.addTaxTotal(crearTaxTotalGlobal(data, moneda));
        }

        // 6. Totales Monetarios 
        notaDebito.setRequestedMonetaryTotal(crearTotalesMonetarios(data, moneda));

        // 7. Líneas de Detalle
        if (data.lineas() != null) {
            for (NotaDebitoLineaUblData linea : data.lineas()) {
                notaDebito.addDebitNoteLine(crearLinea(linea, moneda));
            }
        }

        return notaDebito;
    }

    /**
     * Serializa un DebitNoteType UBL a XML.
     *
     * @param notaDebito DebitNoteType a serializar
     * @return XML serializado
     * @throws Exception si hay error al serializar
     */
    public String serializar(DebitNoteType notaDebito) throws Exception {
        StringWriter writer = new StringWriter();
        UBL21Marshaller.debitNote()
            .setFormattedOutput(true)
            .write(notaDebito, writer);
        
        return writer.toString();
    }

    public String construirXml(NotaDebitoUblData data) throws Exception {
        DebitNoteType notaDebito = buildDebitNote(data);
        return serializar(notaDebito);
    }

    private SupplierPartyType crearEmisor(NotaDebitoUblData data) {
        SupplierPartyType supplier = new SupplierPartyType();
        PartyType party = new PartyType();
        
        PartyLegalEntityType ple = new PartyLegalEntityType();
        if (data.emisorRazonSocial() != null) ple.setRegistrationName(data.emisorRazonSocial());
        
        AddressType address = new AddressType();
        AddressTypeCodeType addressTypeCode = new AddressTypeCodeType();
        addressTypeCode.setValue("0001");
        address.setAddressTypeCode(addressTypeCode);
        ple.setRegistrationAddress(address);
        party.addPartyLegalEntity(ple);

        if (data.emisorNroDocumento() != null) {
            PartyTaxSchemeType pts = new PartyTaxSchemeType();
            if (data.emisorRazonSocial() != null) pts.setRegistrationName(data.emisorRazonSocial());
            
            CompanyIDType companyId = new CompanyIDType();
            companyId.setSchemeID("6"); 
            companyId.setSchemeName("SUNAT:Identificador de Documento de Identidad");
            companyId.setSchemeAgencyName("PE:SUNAT");
            companyId.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
            companyId.setValue(data.emisorNroDocumento());
            pts.setCompanyID(companyId);
            pts.setRegistrationAddress(address);
            
            TaxSchemeType ts = new TaxSchemeType();
            IDType tsId = new IDType();
            tsId.setValue("-");
            ts.setID(tsId);
            pts.setTaxScheme(ts);
            
            party.addPartyTaxScheme(pts);
        }

        supplier.setParty(party);
        return supplier;
    }

    private CustomerPartyType crearReceptor(NotaDebitoUblData data) {
        CustomerPartyType customer = new CustomerPartyType();
        PartyType party = new PartyType();
        
        PartyLegalEntityType ple = new PartyLegalEntityType();
        if (data.receptorRazonSocial() != null) ple.setRegistrationName(data.receptorRazonSocial());
        party.addPartyLegalEntity(ple);

        if (data.receptorNroDocumento() != null) {
            PartyTaxSchemeType pts = new PartyTaxSchemeType();
            if (data.receptorRazonSocial() != null) pts.setRegistrationName(data.receptorRazonSocial());
            
            CompanyIDType companyId = new CompanyIDType();
            companyId.setSchemeID("6"); 
            companyId.setSchemeName("SUNAT:Identificador de Documento de Identidad");
            companyId.setSchemeAgencyName("PE:SUNAT");
            companyId.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
            companyId.setValue(data.receptorNroDocumento());
            pts.setCompanyID(companyId);
            
            TaxSchemeType ts = new TaxSchemeType();
            IDType tsId = new IDType();
            tsId.setValue("-");
            ts.setID(tsId);
            pts.setTaxScheme(ts);
            
            party.addPartyTaxScheme(pts);
        }

        customer.setParty(party);
        return customer;
    }

    private TaxTotalType crearTaxTotalGlobal(NotaDebitoUblData data, String moneda) {
        TaxTotalType taxTotal = new TaxTotalType();
        taxTotal.setTaxAmount(data.totalImpuestos()).setCurrencyID(moneda);

        TaxSubtotalType subtotal = new TaxSubtotalType();
        BigDecimal valorVenta = data.valorVenta() != null ? data.valorVenta() : BigDecimal.ZERO;
        subtotal.setTaxableAmount(valorVenta).setCurrencyID(moneda);
        subtotal.setTaxAmount(data.totalImpuestos()).setCurrencyID(moneda);

        TaxCategoryType category = new TaxCategoryType();
        IDType catId = new IDType();
        catId.setSchemeID("UN/ECE 5305");
        catId.setValue("S"); 
        category.setID(catId);
        
        TaxSchemeType ts = new TaxSchemeType();
        IDType tsId = new IDType();
        tsId.setSchemeID("UN/ECE 5153");
        tsId.setSchemeAgencyID("6");
        tsId.setValue("1000");
        ts.setID(tsId);
        ts.setName("IGV");
        ts.setTaxTypeCode("VAT");
        category.setTaxScheme(ts);

        subtotal.setTaxCategory(category);
        taxTotal.addTaxSubtotal(subtotal);

        return taxTotal;
    }

    private MonetaryTotalType crearTotalesMonetarios(NotaDebitoUblData data, String moneda) {
        MonetaryTotalType totales = new MonetaryTotalType();
        BigDecimal valorVenta = data.valorVenta() != null ? data.valorVenta() : BigDecimal.ZERO;
        BigDecimal importeTotal = data.importeTotal() != null ? data.importeTotal() : BigDecimal.ZERO;

        totales.setLineExtensionAmount(valorVenta).setCurrencyID(moneda);
        totales.setTaxInclusiveAmount(importeTotal).setCurrencyID(moneda);
        totales.setPayableAmount(importeTotal).setCurrencyID(moneda);

        return totales;
    }

    private DebitNoteLineType crearLinea(NotaDebitoLineaUblData linea, String moneda) {
        DebitNoteLineType lineType = new DebitNoteLineType();
        lineType.setID(String.valueOf(linea.numero() != null ? linea.numero() : 1));

        DebitedQuantityType qty = new DebitedQuantityType();
        BigDecimal cantidad = linea.cantidad() != null ? linea.cantidad() : BigDecimal.ZERO;
        qty.setValue(cantidad);
        qty.setUnitCode(linea.unidadMedida() != null ? linea.unidadMedida() : "NIU");
        lineType.setDebitedQuantity(qty);

        BigDecimal valorVenta = linea.valorVenta() != null ? linea.valorVenta() : BigDecimal.ZERO;
        lineType.setLineExtensionAmount(valorVenta).setCurrencyID(moneda);

        PricingReferenceType pricingRef = new PricingReferenceType();
        PriceType altPrice = new PriceType();
        BigDecimal montoIgv = linea.montoIGV() != null ? linea.montoIGV() : BigDecimal.ZERO;
        
        BigDecimal precioConIgv = BigDecimal.ZERO;
        if (cantidad.compareTo(BigDecimal.ZERO) > 0) {
            precioConIgv = valorVenta.add(montoIgv).divide(cantidad, 2, RoundingMode.HALF_UP);
        }
        altPrice.setPriceAmount(precioConIgv).setCurrencyID(moneda);
        
        PriceTypeCodeType priceTypeCode = new PriceTypeCodeType();
        priceTypeCode.setValue("01"); 
        priceTypeCode.setListName("SUNAT:Indicador de Tipo de Precio");
        priceTypeCode.setListAgencyName("PE:SUNAT");
        priceTypeCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16");
        altPrice.setPriceTypeCode(priceTypeCode);
        
        pricingRef.addAlternativeConditionPrice(altPrice);
        lineType.setPricingReference(pricingRef);

        TaxTotalType taxTotal = new TaxTotalType();
        taxTotal.setTaxAmount(montoIgv).setCurrencyID(moneda);
        
        TaxSubtotalType subtotal = new TaxSubtotalType();
        subtotal.setTaxableAmount(valorVenta).setCurrencyID(moneda);
        subtotal.setTaxAmount(montoIgv).setCurrencyID(moneda);
        
        TaxCategoryType category = new TaxCategoryType();
        IDType catId = new IDType();
        catId.setSchemeID("UN/ECE 5305");
        
        String tipoAfectacion = linea.tipoAfectacionIGV() != null ? linea.tipoAfectacionIGV() : "10";
        if (tipoAfectacion.startsWith("1")) {
            catId.setValue("S");
            category.setPercent(linea.porcentajeIGV() != null ? linea.porcentajeIGV() : new BigDecimal("18.00"));
        } else if (tipoAfectacion.startsWith("2")) {
            catId.setValue("E");
        } else {
            catId.setValue("O");
        }
        category.setID(catId);
        
        TaxExemptionReasonCodeType exemptionReason = new TaxExemptionReasonCodeType();
        exemptionReason.setValue(tipoAfectacion);
        exemptionReason.setListAgencyName("PE:SUNAT");
        exemptionReason.setListName("SUNAT:Codigo de Tipo de Afectación del IGV");
        exemptionReason.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07");
        category.setTaxExemptionReasonCode(exemptionReason);
        
        TaxSchemeType ts = new TaxSchemeType();
        IDType tsId = new IDType();
        tsId.setSchemeID("UN/ECE 5153");
        tsId.setSchemeAgencyID("6");
        if (tipoAfectacion.startsWith("1")) {
            tsId.setValue("1000");
            ts.setName("IGV");
            ts.setTaxTypeCode("VAT");
        } else if (tipoAfectacion.startsWith("2")) {
            tsId.setValue("9997");
            ts.setName("EXONERADO");
            ts.setTaxTypeCode("VAT");
        } else {
            tsId.setValue("9998");
            ts.setName("INAFECTO");
            ts.setTaxTypeCode("FRE");
        }
        ts.setID(tsId);
        category.setTaxScheme(ts);
        
        subtotal.setTaxCategory(category);
        taxTotal.addTaxSubtotal(subtotal);
        lineType.addTaxTotal(taxTotal);

        ItemType item = new ItemType();
        if (linea.descripcion() != null) {
            DescriptionType desc = new DescriptionType();
            desc.setValue(linea.descripcion());
            item.addDescription(desc);
        }
        if (linea.codigoProducto() != null) {
            ItemIdentificationType itemId = new ItemIdentificationType();
            IDType id = new IDType();
            id.setValue(linea.codigoProducto());
            itemId.setID(id);
            item.setSellersItemIdentification(itemId);
        }
        lineType.setItem(item);

        PriceType price = new PriceType();
        BigDecimal precioSinIgv = BigDecimal.ZERO;
        if (cantidad.compareTo(BigDecimal.ZERO) > 0) {
            precioSinIgv = valorVenta.divide(cantidad, 2, RoundingMode.HALF_UP);
        }
        price.setPriceAmount(precioSinIgv).setCurrencyID(moneda);
        lineType.setPrice(price);

        return lineType;
    }
}