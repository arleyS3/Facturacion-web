package com.facturacion.api.application.comprobante.ubl.builder.boleta;

import com.facturacion.api.application.comprobante.ubl.mapper.boleta.BoletaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.boleta.BoletaLineaUblData;
import com.facturacion.api.application.comprobante.ubl.util.MontoEnLetrasUtil;
import com.helger.ubl21.UBL21Marshaller;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Constructor de XML UBL 2.1 para Boleta Electrónica.
 * Genera un documento XML válido según especificación UBL 2.1 y normativa SUNAT
 * Perú.
 *
 * <p>
 * Esta clase construye objetos InvoiceType de UBL 2.1 utilizando la librería
 * ph-ubl.
 * Soporta los campos requeridos por SUNAT para boletas electrónicas.
 * </p>
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

    private static final @Nullable String NO_HAY_CODIGO_DE_REGIMEN_ESPECIAL = "-";

    /**
     * Construye el objeto InvoiceType UBL 2.1 a partir de los datos canónicos,
     * sin serializar a XML.
     *
     * @param data datos de la boleta en formato canónico
     * @return InvoiceType construido (no serializado)
     */
    public InvoiceType buildInvoice(BoletaUblData data) {
        InvoiceType invoice = new InvoiceType();

        invoice.setUBLVersionID("2.1");
        invoice.setCustomizationID("2.0");

        ProfileIDType profileID = new ProfileIDType();
        profileID.setSchemeName("SUNAT:Identificador de Tipo de Operación");
        profileID.setSchemeAgencyName("PE:SUNAT");
        profileID.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo17");
        profileID.setValue("0101");
        invoice.setProfileID(profileID);

        invoice.setID(data.serie() + "-" + data.correlativo());
        invoice.setIssueDate(LocalDate.parse(data.fechaEmision(), FECHA_FMT));
        invoice.setIssueTime(LocalTime.parse(data.horaEmision(),
                DateTimeFormatter.ofPattern("HH:mm:ss")));

        InvoiceTypeCodeType typeCode = new InvoiceTypeCodeType();
        typeCode.setValue("03");
        typeCode.setListAgencyName("PE:SUNAT");
        typeCode.setListName("SUNAT:Identificador de Tipo de Documento");
        typeCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01");
        invoice.setInvoiceTypeCode(typeCode);

        DocumentCurrencyCodeType currency = new DocumentCurrencyCodeType();
        currency.setListID("ISO 4217 Alpha");
        currency.setName("Currency");
        currency.setListAgencyName("United Nations Economic Commission for Europe");
        currency.setValue(data.moneda());
        invoice.setDocumentCurrencyCode(currency);
        configurarLeyendas(invoice, data);
        configurarGuiaRemision(invoice, data);
        configurarDocumentoAdicional(invoice, data);
        configurarAnticipo(invoice, data);

        invoice.setAccountingSupplierParty(crearProveedor(data));
        invoice.setAccountingCustomerParty(crearCliente(data));

        configurarDescuentoGlobal(invoice, data);
        crearImpuestoTotal(invoice, data);

        invoice.setLegalMonetaryTotal(crearTotales(data));

        if (data.lineas() != null) {
            for (BoletaLineaUblData linea : data.lineas()) {
                invoice.addInvoiceLine(crearLinea(linea, data.moneda()));
            }
        }

        return invoice;
    }

    /**
     * Serializa un InvoiceType UBL a XML, aplicando el reemplazo de leyendas
     * post-serialización que requiere SUNAT.
     *
     * @param invoice InvoiceType a serializar
     * @param data    datos originales de la boleta (para reemplazo de leyendas)
     * @return XML serializado
     * @throws Exception si hay error al serializar
     */
    public String serializar(InvoiceType invoice, BoletaUblData data) throws Exception {
        StringWriter writer = new StringWriter();
        UBL21Marshaller.invoice()
                .setFormattedOutput(true)
                .write(invoice, writer);

        String xmlFinal = writer.toString();
        if (data.leyendas() != null) {
            for (var leyenda : data.leyendas()) {
                xmlFinal = xmlFinal.replace(
                        "<cbc:Note>" + leyenda.leyenda() + "</cbc:Note>",
                        "<cbc:Note languageLocaleID=\"" + leyenda.codigoLocal() + "\">" + leyenda.leyenda()
                                + "</cbc:Note>");
            }
        }

        return xmlFinal;
    }

    /**
     * Construye el XML de boleta UBL 2.1.
     *
     * @param data datos de la boleta en formato canónico
     * @return contenido XML generado
     * @throws Exception si hay error al generar el XML
     */
    public String construirXml(BoletaUblData data) throws Exception {
        InvoiceType invoice = buildInvoice(data);
        return serializar(invoice, data);
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
            PartyNameType partyName = new PartyNameType();
            NameType nombre = new NameType();
            nombre.setValue(data.emisorRazonSocial());
            partyName.setName(nombre);
            party.addPartyName(partyName);
        }

        PartyTaxSchemeType taxScheme = new PartyTaxSchemeType();
        taxScheme.setRegistrationName(data.emisorRazonSocial());

        CompanyIDType companyId = new CompanyIDType();
        companyId.setSchemeID("6");
        companyId.setSchemeName("SUNAT:Identificador de Documento de Identidad");
        companyId.setSchemeAgencyName("PE:SUNAT");
        companyId.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
        companyId.setValue(data.emisorNroDocumento());
        taxScheme.setCompanyID(companyId);

        // TaxScheme del Emisor
        /*
         * <cac:TaxScheme>
         * <cbc:ID>-</cbc:ID>
         * </cac:TaxScheme>
         */
        TaxSchemeType esquema = new TaxSchemeType();
        esquema.setID(NO_HAY_CODIGO_DE_REGIMEN_ESPECIAL);
        taxScheme.setTaxScheme(esquema);
        party.addPartyTaxScheme(taxScheme);

        PartyLegalEntityType legalEntity = new PartyLegalEntityType();
        legalEntity.setRegistrationName(data.emisorRazonSocial());
        party.addPartyLegalEntity(legalEntity);

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

        PartyTaxSchemeType taxScheme = new PartyTaxSchemeType();
        taxScheme.setRegistrationName(data.receptorRazonSocial());

        CompanyIDType companyId = new CompanyIDType();
        companyId.setSchemeID(data.receptorTipoDocumento());
        companyId.setSchemeName("SUNAT:Identificador de Documento de Identidad");
        companyId.setSchemeAgencyName("PE:SUNAT");
        companyId.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
        companyId.setValue(data.receptorNroDocumento());
        taxScheme.setCompanyID(companyId);

        TaxSchemeType esquema = new TaxSchemeType();
        esquema.setID(NO_HAY_CODIGO_DE_REGIMEN_ESPECIAL);
        taxScheme.setTaxScheme(esquema);
        party.addPartyTaxScheme(taxScheme);

        PartyLegalEntityType legalEntity = new PartyLegalEntityType();
        legalEntity.setRegistrationName(data.receptorRazonSocial());
        party.addPartyLegalEntity(legalEntity);

        customer.setParty(party);
        return customer;
    }

    /**
     * Crea el total de impuestos (IGV).
     *
     * @param igv monto del IGV
     * @return detalle del impuesto
     */
    private void crearImpuestoTotal(InvoiceType invoice, BoletaUblData data) {
        BigDecimal totalImpuestos = data.totalImpuestos() != null
                ? data.totalImpuestos()
                : BigDecimal.ZERO;

        TaxTotalType taxTotal = new TaxTotalType();
        taxTotal.setTaxAmount(totalImpuestos).setCurrencyID(data.moneda());

        if (data.gravadas() != null && data.gravadas().compareTo(BigDecimal.ZERO) > 0) {
            TaxSubtotalType sub = new TaxSubtotalType();
            sub.setTaxableAmount(data.gravadas()).setCurrencyID(data.moneda());
            sub.setTaxAmount(data.IGV() != null ? data.IGV() : BigDecimal.ZERO)
                    .setCurrencyID(data.moneda());
            sub.setTaxCategory(crearCategoriaGravado());
            taxTotal.addTaxSubtotal(sub);
        }

        if (data.exoneradas() != null && data.exoneradas().compareTo(BigDecimal.ZERO) > 0) {
            TaxSubtotalType sub = new TaxSubtotalType();
            sub.setTaxableAmount(data.exoneradas()).setCurrencyID(data.moneda());
            sub.setTaxAmount(BigDecimal.ZERO).setCurrencyID(data.moneda());
            sub.setTaxCategory(crearCategoriaExonerado());
            taxTotal.addTaxSubtotal(sub);
        }

        if (data.inafectas() != null && data.inafectas().compareTo(BigDecimal.ZERO) > 0) {
            TaxSubtotalType sub = new TaxSubtotalType();
            sub.setTaxableAmount(data.inafectas()).setCurrencyID(data.moneda());
            sub.setTaxAmount(BigDecimal.ZERO).setCurrencyID(data.moneda());
            sub.setTaxCategory(crearCategoriaInafecto());
            taxTotal.addTaxSubtotal(sub);
        }

        invoice.addTaxTotal(taxTotal);
    }

    private TaxCategoryType crearCategoriaGravado() {
        TaxCategoryType cat = new TaxCategoryType();
        IDType id = new IDType();
        id.setSchemeID("UN/ECE 5305");
        id.setSchemeName("Tax Category Identifier");
        id.setSchemeAgencyName("United Nations Economic Commission for Europe");
        id.setValue("S");
        cat.setID(id);
        cat.setPercent(new BigDecimal("18.00"));
        cat.setTaxScheme(crearEsquemaIgv());
        return cat;
    }

    private TaxCategoryType crearCategoriaExonerado() {
        TaxCategoryType cat = new TaxCategoryType();
        IDType id = new IDType();
        id.setSchemeID("UN/ECE 5305");
        id.setSchemeName("Tax Category Identifier");
        id.setSchemeAgencyName("United Nations Economic Commission for Europe");
        id.setValue("E");
        cat.setID(id);
        cat.setTaxScheme(crearEsquemaExonerado());
        return cat;
    }

    private TaxCategoryType crearCategoriaInafecto() {
        TaxCategoryType cat = new TaxCategoryType();
        IDType id = new IDType();
        id.setSchemeID("UN/ECE 5305");
        id.setSchemeName("Tax Category Identifier");
        id.setSchemeAgencyName("United Nations Economic Commission for Europe");
        id.setValue("O");
        cat.setID(id);
        cat.setTaxScheme(crearEsquemaInafecto());
        return cat;
    }

    private TaxSchemeType crearEsquemaIgv() {
        IDType id = new IDType();
        TaxSchemeType scheme = new TaxSchemeType();
        id.setSchemeAgencyID("6");
        id.setSchemeID("UN/ECE 5153");
        id.setValue("1000");
        scheme.setID(id);
        scheme.setTaxTypeCode("VAT");
        scheme.setName("IGV");
        return scheme;
    }

    private TaxSchemeType crearEsquemaExonerado() {
        TaxSchemeType scheme = new TaxSchemeType();
        IDType id = new IDType();
        id.setSchemeID("UN/ECE 5153");
        id.setSchemeAgencyID("6");
        id.setValue("9997");
        scheme.setID(id);
        scheme.setName("EXONERADO");
        scheme.setTaxTypeCode("VAT");
        return scheme;
    }

    private TaxSchemeType crearEsquemaInafecto() {
        TaxSchemeType scheme = new TaxSchemeType();
        IDType id = new IDType();
        id.setSchemeID("UN/ECE 5153");
        id.setSchemeAgencyID("6");
        id.setValue("9998");
        scheme.setID(id);
        scheme.setName("INAFECTO");
        scheme.setTaxTypeCode("FRE");
        return scheme;
    }

    /**
     * Crea los totales monetarios de la boleta.
     *
     * @param data datos de la boleta
     * @return totales monetarios
     */
    private MonetaryTotalType crearTotales(BoletaUblData data) {
        MonetaryTotalType mt = new MonetaryTotalType();
        String moneda = data.moneda();

        BigDecimal subtotal = data.valorVenta() != null ? data.valorVenta() : BigDecimal.ZERO;
        BigDecimal impuestos = data.totalImpuestos() != null ? data.totalImpuestos() : BigDecimal.ZERO;
        BigDecimal total = data.importeTotal() != null ? data.importeTotal() : subtotal.add(impuestos);

        mt.setLineExtensionAmount(subtotal).setCurrencyID(moneda);
        mt.setTaxExclusiveAmount(subtotal).setCurrencyID(moneda);
        mt.setTaxInclusiveAmount(total).setCurrencyID(moneda);
        mt.setPayableAmount(total).setCurrencyID(moneda);

        return mt;
    }

    /**
     * Crea una línea de detalle de la boleta.
     *
     * @param linea datos de la línea
     * @return línea UBL
     */
    private InvoiceLineType crearLinea(BoletaLineaUblData linea, String moneda) {
        InvoiceLineType line = new InvoiceLineType();

        line.setID(String.valueOf(linea.numero() != null ? linea.numero() : 1));

        if (linea.cantidad() != null) {
            InvoicedQuantityType qty = new InvoicedQuantityType();
            qty.setValue(linea.cantidad());
            qty.setUnitCode(linea.unidadMedida() != null ? linea.unidadMedida() : "NIU");
            qty.setUnitCodeListID("UN/ECE rec 20");
            qty.setUnitCodeListAgencyName("United Nations Economic Commission for Europe");
            line.setInvoicedQuantity(qty);
        }

        BigDecimal valorVenta = linea.valorVenta() != null ? linea.valorVenta() : BigDecimal.ZERO;
        line.setLineExtensionAmount(valorVenta).setCurrencyID(moneda);

        if (linea.precioUnitario() != null) {
            PricingReferenceType pricingRef = new PricingReferenceType();
            PriceType precioRef = new PriceType();
            precioRef.setPriceAmount(linea.precioUnitario()).setCurrencyID(moneda);
            PriceTypeCodeType tipoPrecio = new PriceTypeCodeType();
            tipoPrecio.setValue("01");
            tipoPrecio.setListName("SUNAT:Indicador de Tipo de Precio");
            tipoPrecio.setListAgencyName("PE:SUNAT");
            tipoPrecio.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16");
            precioRef.setPriceTypeCode(tipoPrecio);
            pricingRef.addAlternativeConditionPrice(precioRef);
            line.setPricingReference(pricingRef);
        }

        line.addTaxTotal(crearImpuestoLinea(linea, moneda));

        ItemType item = new ItemType();
        if (linea.descripcion() != null) {
            DescriptionType desc = new DescriptionType();
            desc.setValue(linea.descripcion());
            item.setDescription(Collections.singletonList(desc));
        }
        if (linea.codigoProducto() != null && !linea.codigoProducto().isBlank()) {
            ItemIdentificationType sellerId = new ItemIdentificationType();
            IDType idProducto = new IDType();
            idProducto.setValue(linea.codigoProducto());
            sellerId.setID(idProducto);
            item.setSellersItemIdentification(sellerId);
        }
        line.setItem(item);

        if (linea.precioUnitario() != null) {
            PriceType price = new PriceType();
            price.setPriceAmount(linea.precioUnitario()).setCurrencyID(moneda);
            ;
            line.setPrice(price);
        }

        return line;
    }

    private TaxTotalType crearImpuestoLinea(BoletaLineaUblData linea, String moneda) {
        BigDecimal montoIgv = linea.montoIGV() != null ? linea.montoIGV() : BigDecimal.ZERO;
        BigDecimal valorVenta = linea.valorVenta() != null ? linea.valorVenta() : BigDecimal.ZERO;
        String tipoAfectacion = linea.tipoAfectacionIGV() != null
                ? linea.tipoAfectacionIGV()
                : "10";

        TaxTotalType taxTotal = new TaxTotalType();
        taxTotal.setTaxAmount(montoIgv).setCurrencyID(moneda);

        TaxSubtotalType sub = new TaxSubtotalType();
        sub.setTaxableAmount(valorVenta).setCurrencyID(moneda);
        sub.setTaxAmount(montoIgv).setCurrencyID(moneda);
        sub.setTaxCategory(crearCategoriaImpuestoLinea(tipoAfectacion));
        taxTotal.addTaxSubtotal(sub);

        return taxTotal;
    }

    private TaxCategoryType crearCategoriaImpuestoLinea(String tipoAfectacion) {
        TaxCategoryType cat = new TaxCategoryType();

        String codigoCategoria;
        TaxSchemeType esquema;

        if ("10".equals(tipoAfectacion)) {
            codigoCategoria = "S";
            esquema = crearEsquemaIgv();
            cat.setPercent(new BigDecimal("18.00"));
        } else if ("20".equals(tipoAfectacion)) {
            codigoCategoria = "E";
            esquema = crearEsquemaExonerado();
        } else {
            codigoCategoria = "O";
            esquema = crearEsquemaInafecto();
        }

        IDType id = new IDType();
        id.setSchemeID("UN/ECE 5305");
        id.setSchemeName("Tax Category Identifier");
        id.setSchemeAgencyName("United Nations Economic Commission for Europe");
        id.setValue(codigoCategoria);
        cat.setID(id);

        TaxExemptionReasonCodeType exemption = new TaxExemptionReasonCodeType();
        exemption.setValue(tipoAfectacion);
        exemption.setListAgencyName("PE:SUNAT");
        exemption.setListName("SUNAT:Codigo de Tipo de Afectación del IGV");
        exemption.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07");
        cat.setTaxExemptionReasonCode(exemption);

        cat.setTaxScheme(esquema);
        return cat;
    }

    private void configurarLeyendas(InvoiceType invoice, BoletaUblData data) {
        if (data.leyendas() == null || data.leyendas().isEmpty()) {
            return;
        }
        final BigDecimal importeTotal = data.importeTotal();
        final String moneda = data.moneda();

        List<NoteType> notas = data.leyendas().stream()
                .map(leyenda -> {
                    NoteType nota = new NoteType();
                    nota.setLanguageLocaleID(leyenda.codigoLocal());
                    if ("1000".equals(leyenda.codigoLocal()) && importeTotal != null) {
                        nota.setValue(MontoEnLetrasUtil.convertir(importeTotal, moneda));
                    } else {
                        nota.setValue(leyenda.leyenda());
                    }
                    return nota;
                })
                .toList();
        invoice.setNote(notas);
    }

    private void configurarDescuentoGlobal(InvoiceType invoice, BoletaUblData data) {
        if (data.tieneDescuentoGlobal() == null || !data.tieneDescuentoGlobal()) {
            return;
        }
        if (data.descuentoMonto() == null || data.descuentoMonto().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        AllowanceChargeType descuento = new AllowanceChargeType();

        descuento.setChargeIndicator(false);
        descuento.setAllowanceChargeReasonCode("00");
        descuento.setAmount(data.descuentoMonto()).setCurrencyID(data.moneda());
        descuento.setBaseAmount(data.descuentoBase()).setCurrencyID(data.moneda());

        invoice.addAllowanceCharge(descuento);
    }

    private void configurarGuiaRemision(InvoiceType invoice, BoletaUblData data) {
        if (data.guiaRemisionId() == null || data.guiaRemisionId().isBlank()) {
            return;
        }
        DocumentReferenceType ref = new DocumentReferenceType();

        IDType id = new IDType();
        id.setValue(data.guiaRemisionId());
        ref.setID(id);

        DocumentTypeCodeType typeCode = new DocumentTypeCodeType();
        typeCode.setValue(data.guiaRemisionCodigo() != null ? data.guiaRemisionCodigo() : "09");
        typeCode.setListAgencyName("PE:SUNAT");
        typeCode.setListName("SUNAT:Identificador de guía relacionada");
        typeCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01");
        ref.setDocumentTypeCode(typeCode);

        invoice.addDespatchDocumentReference(ref);
    }

    private void configurarDocumentoAdicional(InvoiceType invoice, BoletaUblData data) {
        if (data.documentosAdicionales() == null || data.documentosAdicionales().isEmpty()) {
            return;
        }
        for (var doc : data.documentosAdicionales()) {
            DocumentReferenceType ref = new DocumentReferenceType();

            IDType id = new IDType();
            id.setValue(doc.id() != null ? doc.id() : "");
            ref.setID(id);

            DocumentTypeCodeType typeCode = new DocumentTypeCodeType();
            typeCode.setValue(doc.tipoDocumento() != null ? doc.tipoDocumento() : "99");
            typeCode.setListAgencyName("PE:SUNAT");
            typeCode.setListName("SUNAT:Identificador de documento relacionado");
            typeCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo12");
            ref.setDocumentTypeCode(typeCode);

            invoice.addAdditionalDocumentReference(ref);
        }
    }

    private void configurarAnticipo(InvoiceType invoice, BoletaUblData data) {
        if (data.anticipoId() == null || data.anticipoId().isBlank()) {
            return;
        }

        PaymentType anticipo = new PaymentType();

        IDType id = new IDType();
        id.setValue(data.anticipoId());
        id.setSchemeID(data.anticipoTipoDoc() != null ? data.anticipoTipoDoc() : "02");
        id.setSchemeName("SUNAT:Identificador de Documentos Relacionados");
        id.setSchemeAgencyName("PE:SUNAT");
        id.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo12");
        anticipo.setID(id);

        if (data.anticipoMonto() != null) {
            anticipo.setPaidAmount(data.anticipoMonto())
                    .setCurrencyID(data.anticipoMoneda() != null
                            ? data.anticipoMoneda()
                            : data.moneda());
        }

        if (data.anticipoRucEmisor() != null) {
            InstructionIDType instructionId = new InstructionIDType();
            instructionId.setValue(data.anticipoRucEmisor());
            instructionId.setSchemeID("6");
            anticipo.setInstructionID(instructionId);
        }

        invoice.addPrepaidPayment(anticipo);
    }
}
