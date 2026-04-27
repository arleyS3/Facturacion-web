package com.facturacion.api.application.comprobante.ubl.builder.boleta;

import com.facturacion.api.application.comprobante.ubl.mapper.boleta.BoletaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.boleta.BoletaLineaUblData;
import com.helger.ubl21.UBL21Marshaller;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * Constructor de XML UBL 2.1 para Boleta Electrónica.
 * Genera un documento XML válido según especificación UBL 2.1 y normativa SUNAT Perú.
 * 
 * <p>Esta clase construye objetos InvoiceType de UBL 2.1 utilizando la librería ph-ubl.
 * Soporta los campos requeridos por SUNAT para boletas electrónicas.</p>
 * 
 * @author Facturación API
 * @version 1.0
 * @see <a href="https://github.com/phax/ph-ubl">ph-ubl</a>
 * @see <a href="https://cpe.sunat.gob.pe">SUNAT CPE</a>
 */
@Component
public class BoletaUblBuilder {

    /** Formato de fecha para parsing de fechas. */
    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /** Código de moneda por defecto para Perú. */
    private static final String MONEDA_DEFAULT = "PEN";

    /**
     * Construye el XML de boleta UBL 2.1.
     *
     * @param data datos de la boleta en formato canónico
     * @return contenido XML generado
     * @throws Exception si hay error al generar el XML
     */
    public String construirXml(BoletaUblData data) throws Exception {
        InvoiceType invoice = new InvoiceType();
        
        invoice.setID(data.serie() + "-" + data.correlativo());
        
        LocalDate fecha = LocalDate.parse(data.fechaEmision(), FECHA_FMT);
        invoice.setIssueDate(fecha);
        
        invoice.setInvoiceTypeCode("03");
        
        invoice.setDocumentCurrencyCode(MONEDA_DEFAULT);
        
        invoice.setAccountingSupplierParty(crearProveedor(data));
        
        if (data.receptorNroDocumento() != null && !data.receptorNroDocumento().isEmpty()) {
            invoice.setAccountingCustomerParty(crearCliente(data));
        }
        
        if (data.IGV() != null && data.IGV().compareTo(BigDecimal.ZERO) > 0) {
            invoice.addTaxTotal(crearImpuestoTotal(data.IGV()));
        }
        
        invoice.setLegalMonetaryTotal(crearTotales(data));
        
        if (data.lineas() != null) {
            for (BoletaLineaUblData linea : data.lineas()) {
                invoice.addInvoiceLine(crearLinea(linea));
            }
        }
        
        StringWriter writer = new StringWriter();
        UBL21Marshaller.invoice()
            .setFormattedOutput(true)
            .write(invoice, writer);
        
        return writer.toString();
    }

    /**
     * Crea el proveedor/emisor de la boleta.
     *
     * @param data datos de la boleta
     * @return datos del emisor
     */
    private SupplierPartyType crearProveedor(BoletaUblData data) {
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

    /**
     * Crea el cliente/receptor de la boleta.
     *
     * @param data datos de la boleta
     * @return datos del receptor
     */
    private CustomerPartyType crearCliente(BoletaUblData data) {
        CustomerPartyType customer = new CustomerPartyType();
        PartyType party = new PartyType();
        
        if (data.receptorRazonSocial() != null) {
            PartyLegalEntityType ple = new PartyLegalEntityType();
            ple.setRegistrationName(data.receptorRazonSocial());
            party.addPartyLegalEntity(ple);
        }
        
        customer.setParty(party);
        return customer;
    }

    /**
     * Crea el total de impuestos (IGV).
     *
     * @param igv monto del IGV
     * @return detalle del impuesto
     */
    private TaxTotalType crearImpuestoTotal(BigDecimal igv) {
        TaxTotalType total = new TaxTotalType();
        total.setTaxAmount(igv);
        
        TaxSubtotalType sub = new TaxSubtotalType();
        sub.setTaxableAmount(igv);
        sub.setTaxAmount(igv);
        
        TaxCategoryType cat = new TaxCategoryType();
        IDType id = new IDType();
        id.setValue("1000");
        cat.setID(id);
        cat.setPercent(new BigDecimal("18.00"));
        
        TaxSchemeType scheme = new TaxSchemeType();
        IDType sid = new IDType();
        sid.setValue("1000");
        scheme.setID(sid);
        scheme.setName("IGV");
        scheme.setTaxTypeCode("VAT");
        cat.setTaxScheme(scheme);
        
        sub.setTaxCategory(cat);
        total.addTaxSubtotal(sub);
        
        return total;
    }

    /**
     * Crea los totales monetarios de la boleta.
     *
     * @param data datos de la boleta
     * @return totales monetarios
     */
    private MonetaryTotalType crearTotales(BoletaUblData data) {
        MonetaryTotalType mt = new MonetaryTotalType();
        
        BigDecimal subtotal = data.valorVenta() != null ? data.valorVenta() : BigDecimal.ZERO;
        BigDecimal impuestos = data.totalImpuestos() != null ? data.totalImpuestos() : BigDecimal.ZERO;
        BigDecimal total = data.importeTotal() != null ? data.importeTotal() : subtotal.add(impuestos);
        
        mt.setLineExtensionAmount(subtotal);
        mt.setTaxExclusiveAmount(subtotal);
        mt.setTaxInclusiveAmount(total);
        mt.setPayableAmount(total);
        
        return mt;
    }

    /**
     * Crea una línea de detalle de la boleta.
     *
     * @param linea datos de la línea
     * @return línea UBL
     */
    private InvoiceLineType crearLinea(BoletaLineaUblData linea) {
        InvoiceLineType line = new InvoiceLineType();
        
        line.setID(String.valueOf(linea.numero() != null ? linea.numero() : 1));
        
        BigDecimal valor = linea.valorVenta() != null ? linea.valorVenta() : BigDecimal.ZERO;
        line.setLineExtensionAmount(valor);
        
        ItemType item = new ItemType();
        DescriptionType desc = new DescriptionType();
        desc.setValue(linea.descripcion());
        item.setDescription(Collections.singletonList(desc));
        line.setItem(item);
        
        if (linea.precioUnitario() != null) {
            PriceType price = new PriceType();
            price.setPriceAmount(linea.precioUnitario());
            line.setPrice(price);
        }
        
        return line;
    }
}
