package com.facturacion.api.application.comprobante.ubl.builder.notaCredito;

import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.NotaCreditoUblData;
import com.helger.ubl21.UBL21Marshaller;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyLegalEntityType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.SupplierPartyType;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Constructor de XML UBL 2.1 para Nota de Crédito Electrónica.
 * Genera un documento XML válido según especificación UBL 2.1 y normativa SUNAT Perú.
 * 
 * @author Facturación API
 * @version 1.0
 * @see <a href="https://github.com/phax/ph-ubl">ph-ubl</a>
 * @see <a href="https://cpe.sunat.gob.pe">SUNAT CPE</a>
 */
@Component
public class NotaCreditoUblBuilder {

    /** Formato de fecha para parsing de fechas. */
    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /** Código de moneda por defecto para Perú. */
    private static final String MONEDA_DEFAULT = "PEN";

    /**
     * Construye el XML de nota de crédito UBL 2.1.
     *
     * @param data datos de la nota de crédito en formato canónico
     * @return contenido XML
     * @throws Exception si hay error al generar el XML
     */
    public String construirXml(NotaCreditoUblData data) throws Exception {
        CreditNoteType notaCredito = new CreditNoteType();
        
        notaCredito.setID(data.serie() + "-" + data.correlativo());
        
        LocalDate fecha = LocalDate.parse(data.fechaEmision(), FECHA_FMT);
        notaCredito.setIssueDate(fecha);
        
        notaCredito.setDocumentCurrencyCode(MONEDA_DEFAULT);
        
        notaCredito.setAccountingSupplierParty(crearProveedor(data));
        
        StringWriter writer = new StringWriter();
        UBL21Marshaller.creditNote()
            .setFormattedOutput(true)
            .write(notaCredito, writer);
        
        return writer.toString();
    }

    /**
     * Crea el proveedor/emisor de la nota de crédito.
     *
     * @param data datos de la nota de crédito
     * @return datos del emisor
     */
    private SupplierPartyType crearProveedor(NotaCreditoUblData data) {
        SupplierPartyType supplier = new SupplierPartyType();
        PartyType party = new PartyType();
        
        if (data.emisorRazonSocial() != null) {
            PartyLegalEntityType ple = new PartyLegalEntityType();
            ple.setRegistrationName(data.emisorRazonSocial());
            party.addPartyLegalEntity(ple);
        }
        
        supplier.setParty(party);
        return supplier;
    }
}
