package com.facturacion.api.application.comprobante.ubl.builder.notaDebito;

import com.facturacion.api.application.comprobante.ubl.mapper.notaDebito.NotaDebitoLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.notaDebito.NotaDebitoUblData;
import com.helger.ubl21.UBL21Marshaller;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.debitnote_21.DebitNoteType;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
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

    public String construirXml(NotaDebitoUblData data) throws Exception {
        DebitNoteType notaDebito = new DebitNoteType();
        
        // 1. Encabezado principal
        notaDebito.setUBLVersionID(VERSION_UBL);
        notaDebito.setCustomizationID(VERSION_ESTRUCTURA);
        notaDebito.setID(data.serie() + "-" + data.correlativo());
        
        if (data.fechaEmision() != null) {
            notaDebito.setIssueDate(LocalDate.parse(data.fechaEmision(), FECHA_FMT));
        }
        
        notaDebito.setDocumentCurrencyCode(data.moneda() != null ? data.moneda() : "PEN");

        // 2. Motivo de la Nota de Débito (Catálogo 10: DiscrepancyResponse)
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

        // 3. Documento de Referencia (BillingReference)
        BillingReferenceType billingRef = new BillingReferenceType();
        DocumentReferenceType docRef = new DocumentReferenceType();
        docRef.setID(data.documentoAfectadoSerie() + "-" + data.documentoAfectadoNumero());
        DocumentTypeCodeType docTypeCode = new DocumentTypeCodeType();
        docTypeCode.setValue(data.documentoAfectadoTipo());
        docRef.setDocumentTypeCode(docTypeCode);
        billingRef.setInvoiceDocumentReference(docRef);
        notaDebito.addBillingReference(billingRef);

        // 4. Entidades (Emisor y Receptor)
        notaDebito.setAccountingSupplierParty(crearEmisor(data));
        notaDebito.setAccountingCustomerParty(crearReceptor(data));
        
        // 5. Totales Monetarios 
        MonetaryTotalType totales = new MonetaryTotalType();
        PayableAmountType importeTotal = new PayableAmountType();
        importeTotal.setCurrencyID(data.moneda() != null ? data.moneda() : "PEN");
        importeTotal.setValue(data.importeTotal());
        totales.setPayableAmount(importeTotal);
        notaDebito.setRequestedMonetaryTotal(totales);

        // 6. Detalle de los bienes/cargos (DebitNoteLine)
        if (data.lineas() != null) {
            for (NotaDebitoLineaUblData linea : data.lineas()) {
                notaDebito.addDebitNoteLine(crearLinea(linea, data.moneda()));
            }
        }
        
        // 7. Generar XML String
        StringWriter writer = new StringWriter();
        UBL21Marshaller.debitNote()
            .setFormattedOutput(true)
            .write(notaDebito, writer);
        
        return writer.toString();
    }

    private SupplierPartyType crearEmisor(NotaDebitoUblData data) {
        SupplierPartyType supplier = new SupplierPartyType();
        PartyType party = new PartyType();
        
        if (data.emisorNroDocumento() != null) {
            PartyIdentificationType id = new PartyIdentificationType();
            IDType docId = new IDType();
            docId.setSchemeID("6"); // RUC
            docId.setValue(data.emisorNroDocumento());
            id.setID(docId);
            party.addPartyIdentification(id);
        }

        if (data.emisorRazonSocial() != null) {
            PartyLegalEntityType legalEntity = new PartyLegalEntityType();
            legalEntity.setRegistrationName(data.emisorRazonSocial());
            party.addPartyLegalEntity(legalEntity);
        }
        
        supplier.setParty(party);
        return supplier;
    }

    private CustomerPartyType crearReceptor(NotaDebitoUblData data) {
        CustomerPartyType customer = new CustomerPartyType();
        PartyType party = new PartyType();
        
        if (data.receptorNroDocumento() != null) {
            PartyIdentificationType id = new PartyIdentificationType();
            IDType docId = new IDType();
            docId.setSchemeID("6"); // Asumimos RUC
            docId.setValue(data.receptorNroDocumento());
            id.setID(docId);
            party.addPartyIdentification(id);
        }

        if (data.receptorRazonSocial() != null) {
            PartyLegalEntityType legalEntity = new PartyLegalEntityType();
            legalEntity.setRegistrationName(data.receptorRazonSocial());
            party.addPartyLegalEntity(legalEntity);
        }
        
        customer.setParty(party);
        return customer;
    }

    private DebitNoteLineType crearLinea(NotaDebitoLineaUblData linea, String moneda) {
        DebitNoteLineType line = new DebitNoteLineType();
        line.setID(String.valueOf(linea.numero() != null ? linea.numero() : 1));
        
        if (linea.cantidad() != null) {
            DebitedQuantityType qty = new DebitedQuantityType();
            qty.setValue(linea.cantidad());
            qty.setUnitCode(linea.unidadMedida() != null ? linea.unidadMedida() : "NIU");
            line.setDebitedQuantity(qty);
        }
        
        if (linea.valorVenta() != null) {
            LineExtensionAmountType extAmount = new LineExtensionAmountType();
            extAmount.setCurrencyID(moneda != null ? moneda : "PEN");
            extAmount.setValue(linea.valorVenta());
            line.setLineExtensionAmount(extAmount);
        }
        
        ItemType item = new ItemType();
        if (linea.descripcion() != null) {
            DescriptionType desc = new DescriptionType();
            desc.setValue(linea.descripcion());
            item.setDescription(Collections.singletonList(desc));
        }
        line.setItem(item);

        PriceType price = new PriceType();
        PriceAmountType priceAmount = new PriceAmountType();
        priceAmount.setCurrencyID(moneda != null ? moneda : "PEN");
        // Calcular el precio unitario (Valor Venta / Cantidad)
        if (linea.cantidad() != null && linea.valorVenta() != null && linea.cantidad().compareTo(java.math.BigDecimal.ZERO) > 0) {
            priceAmount.setValue(linea.valorVenta().divide(linea.cantidad(), 2, RoundingMode.HALF_UP));
        } else {
            priceAmount.setValue(java.math.BigDecimal.ZERO);
        }
        price.setPriceAmount(priceAmount);
        line.setPrice(price);
        
        return line;
    }
}