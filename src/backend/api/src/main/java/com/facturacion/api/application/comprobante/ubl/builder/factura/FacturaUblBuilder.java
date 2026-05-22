package com.facturacion.api.application.comprobante.ubl.builder.factura;

import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosDescuentoGlobalFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosDescuentoLineaFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosEncabezadoFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosEmisorFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosEsquemaImpuestoFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosFirmaFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosCategoriaImpuestoFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosPrecioReferenciaFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosReferenciaOrdenFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosReceptorFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosImpuestoSubtotalFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosImpuestoTotalFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosTotalesFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosTotalesMonetariosFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.FacturaLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.FacturaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.LeyendaUblData;
import com.helger.ubl21.UBL21Marshaller;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Constructor de XML UBL 2.1 para Factura Electrónica.
 * Genera un documento XML válido según especificación UBL 2.1 y normativa SUNAT
 * Perú.
 *
 * <p>
 * Esta clase construye objetos InvoiceType de UBL 2.1 utilizando la librería
 * ph-ubl.
 * Soporta los campos requeridos por SUNAT para facturas electrónicas.
 * </p>
 *
 * @author Facturación API
 * @version 1.0
 * @see <a href="https://github.com/phax/ph-ubl">ph-ubl</a>
 * @see <a href="https://cpe.sunat.gob.pe">SUNAT CPE</a>
 */
@Component
public class FacturaUblBuilder {

    /** Formato de fecha para UBL. */
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Formato de hora para UBL. */
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String VERSION_UBL = "2.1";
    private static final String VERSION_ESTRUCTURA = "2.0";
    private static final String CODIGO_TIPO_DOCUMENTO_FACTURA = "01";
    private static final String CODIGO_DOMICILIO_POR_DEFECTO = "0001";

    /** Código de tributo IGV. */
    private static final String CODIGO_TRIBUTO_IGV = "1000";
    /** Código de tributo Exonerado. */
    private static final String CODIGO_TRIBUTO_EXONERADO = "9997";
    /** Código de tributo Inafecto. */
    private static final String CODIGO_TRIBUTO_INAFECTO = "9998";

    /** Nombre del impuesto IGV. */
    private static final String NOMBRE_IMPUESTO_IGV = "IGV";
    /** Nombre del impuesto Exonerado. */
    private static final String NOMBRE_IMPUESTO_EXONERADO = "EXONERADO";
    /** Nombre del impuesto Inafecto. */
    private static final String NOMBRE_IMPUESTO_INAFECTO = "INAFECTO";

    /** Código de tipo de tributo VAT. */
    private static final String TIPO_TRIBUTO_VAT = "VAT";
    /** Código de tipo de tributo FRE (Free/Exempt). */
    private static final String TIPO_TRIBUTO_FRE = "FRE";

    /** Categoría de impuesto Gravado. */
    private static final String CATEGORIA_GRAVADO = "S";
    /** Categoría de impuesto Exonerado. */
    private static final String CATEGORIA_EXONERADO = "E";
    /** Categoría de impuesto Inafecto. */
    private static final String CATEGORIA_INAFECTO = "O";

    private static final BigDecimal PORCENTAJE_IGV = new BigDecimal("18.00");

    private static final @Nullable String NO_HAY_CODIGO_DE_REGIMEN_ESPECIAL = "-";

    /**
     * Construye el XML de factura UBL 2.1 a partir de los datos canónicos.
     *
     * @param datosFactura datos de la factura en formato canónico
     * @return contenido XML generado
     * @throws Exception si hay error al generar el XML
     */
    public String construirXml(FacturaUblData datosFactura) throws Exception {
        InvoiceType facturaUbl = new InvoiceType();

        configurarEncabezado(facturaUbl, datosFactura.encabezado());
        configurarFirmaSiExiste(facturaUbl, datosFactura.firma());
        configurarReferenciaOrdenSiExiste(facturaUbl, datosFactura.referenciaOrden());
        configurarTotalLineasSiExiste(facturaUbl, datosFactura.totalLineas());
        configurarDescuentosGlobalesSiExisten(facturaUbl, datosFactura.descuentosGlobales(),
                datosFactura.encabezado());
        configurarLeyendasSiExisten(facturaUbl, datosFactura.leyendas());
        configurarGuiaRemisionSiExiste(facturaUbl, datosFactura);
        configurarDocumentosAdicionalesSiExisten(facturaUbl, datosFactura);
        configurarPartes(facturaUbl, datosFactura.emisor(), datosFactura.receptor());
        configurarImpuestosSiCorresponde(facturaUbl, datosFactura.totales(), datosFactura.encabezado());
        configurarImpuestosTotalesSiExisten(facturaUbl, datosFactura.impuestosTotales(),
                datosFactura.encabezado());
        configurarTotales(facturaUbl, datosFactura.totalesMonetarios(), datosFactura.totales(),
                datosFactura.encabezado());
        configurarLineas(facturaUbl, datosFactura.encabezado(), datosFactura.lineas());

        return serializarFactura(facturaUbl);
    }

    /**
     * Crea los totales monetarios de la factura.
     *
     * @param totalesMonetarios DatosTotalesMonetariosFacturaUbl
     * @param totalesImpuestos  DatosTotalesFacturaUbl
     * @param encabezado        datos de encabezado
     * @return MonetaryTotalType UBL
     */
    private void configurarEncabezado(InvoiceType facturaUbl, DatosEncabezadoFacturaUbl encabezado) {
        facturaUbl.setUBLVersionID(VERSION_UBL);
        facturaUbl.setCustomizationID(VERSION_ESTRUCTURA);
        facturaUbl.setProfileID(crearProfileID(encabezado.tipoDeOperacion()));
        facturaUbl.setID(encabezado.serie() + "-" + encabezado.correlativo());
        facturaUbl.setIssueDate(parsearFecha(encabezado.fechaEmision()));
        facturaUbl.setIssueTime(parsearHora(encabezado.horaEmision()));
        facturaUbl.setInvoiceTypeCode(crearCodigoTipoDocumento(CODIGO_TIPO_DOCUMENTO_FACTURA));

        String fechaVencimiento = encabezado.fechaVencimiento();
        if (fechaVencimiento != null) {
            facturaUbl.setDueDate(parsearFecha(fechaVencimiento));
        }

        facturaUbl.setDocumentCurrencyCode(crearCodigoMonedaDocumento(encabezado.moneda()));
    }

    /**
     * Parsea una fecha usando el formato UBL.
     *
     * @param fecha fecha en texto
     * @return fecha parseada
     */
    private LocalDate parsearFecha(String fecha) {
        return LocalDate.parse(fecha, FORMATO_FECHA);
    }

    /**
     * Parsea una hora usando el formato UBL.
     *
     * @param hora hora en texto
     * @return hora parseada
     */
    private LocalTime parsearHora(String hora) {
        return LocalTime.parse(hora, FORMATO_HORA);
    }

    /**
     * Configura la firma digital del comprobante.
     *
     * <p>
     * La firma digital es obligatoria para SUNAT. Contiene:
     * - ID: identificador de la firma
     * - SignatoryParty: identificación del firmante (RUC y razón social)
     * </p>
     *
     * <p>
     * Nota: El DigitalSignatureAttachment requiere firma criptográfica real
     * que se implementaría con una librería externa (ej: DSS, xades4j).
     * Por ahora se incluye la estructura básica con SignatoryParty.
     * </p>
     *
     * @param facturaUbl factura UBL a completar
     * @param firma      datos de firma
     */
    private void configurarFirmaSiExiste(InvoiceType facturaUbl, DatosFirmaFacturaUbl firma) {
        if (firma == null) {
            return;
        }
        SignatureType signature = new SignatureType();

        // CAMPO: ID de la firma
        signature.setID(firma.id() != null ? firma.id() : "IDSignSP");

        // CAMPO: SignatoryParty - Información de quién firma
        PartyType signatoryParty = new PartyType();

        // PartyIdentification - RUC del firmante
        PartyIdentificationType identificacion = new PartyIdentificationType();
        IDType id = new IDType();
        id.setValue(firma.rucFirmante() != null ? firma.rucFirmante() : "");
        identificacion.setID(id);
        signatoryParty.addPartyIdentification(identificacion);

        // PartyName - Razón social del firmante
        PartyNameType partyName = new PartyNameType();
        NameType nombre = new NameType();
        nombre.setValue(firma.nombreFirmante() != null ? firma.nombreFirmante() : "");
        partyName.setName(nombre);
        signatoryParty.addPartyName(partyName);

        signature.setSignatoryParty(signatoryParty);

        /*
         * Nota: DigitalSignatureAttachment no está disponible en ph-ubl21 10.1.0
         * como método directo. En producción, la firma criptográfica real se
         * implementaría con una librería externa como:
         * - DSS (Digital Signature Services)
         * - xades4j
         *
         * Estructura esperada cuando se implemente:
         * <cac:DigitalSignatureAttachment>
         * <cac:ExternalReference>
         * <cbc:URI>#SignatureSP</cbc:URI>
         * </cac:ExternalReference>
         * </cac:DigitalSignatureAttachment>
         */

        facturaUbl.addSignature(signature);
    }

    /**
     * Configura la referencia de orden si existe.
     *
     * @param facturaUbl      factura UBL a completar
     * @param referenciaOrden datos de referencia de orden
     */
    private void configurarReferenciaOrdenSiExiste(InvoiceType facturaUbl,
            DatosReferenciaOrdenFacturaUbl referenciaOrden) {
        if (referenciaOrden == null || referenciaOrden.idOrden() == null) {
            return;
        }
        OrderReferenceType orderReference = new OrderReferenceType();
        orderReference.setID(referenciaOrden.idOrden());
        facturaUbl.setOrderReference(orderReference);
    }

    /**
     * Configura el total de líneas si existe.
     *
     * @param facturaUbl  factura UBL a completar
     * @param totalLineas total de líneas
     */
    private void configurarTotalLineasSiExiste(InvoiceType facturaUbl, Integer totalLineas) {
        if (totalLineas == null) {
            return;
        }
        LineCountNumericType lineasCount = new LineCountNumericType();
        lineasCount.setValue(BigDecimal.valueOf(totalLineas));
        facturaUbl.setLineCountNumeric(lineasCount);
    }

    /**
     * Configura los descuentos globales si existen.
     *
     * @param facturaUbl factura UBL a completar
     * @param descuentos lista de descuentos
     * @param encabezado datos de encabezado
     */
    private void configurarDescuentosGlobalesSiExisten(
            InvoiceType facturaUbl,
            List<DatosDescuentoGlobalFacturaUbl> descuentos,
            DatosEncabezadoFacturaUbl encabezado) {
        if (descuentos == null || descuentos.isEmpty()) {
            return;
        }
        for (DatosDescuentoGlobalFacturaUbl descuento : descuentos) {
            facturaUbl.addAllowanceCharge(crearDescuentoGlobal(descuento, encabezado.moneda()));
        }
    }

    /**
     * Crea el ProfileID requerido por SUNAT (catálogo 17).
     *
     * @param tipoDeOperacion código del tipo de operación
     * @return ProfileID configurado
     */
    private ProfileIDType crearProfileID(String tipoDeOperacion) {
        ProfileIDType profileID = new ProfileIDType();
        profileID.setSchemeName("SUNAT:Identificador de Tipo de Operación");
        profileID.setSchemeAgencyName("PE:SUNAT");
        profileID.setSchemeURI(
                "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo17");
        profileID.setValue(tipoDeOperacion);
        return profileID;
    }

    /**
     * Crea el tipo de código de InvoiceTypeCode con atributos SUNAT (catálogo 01).
     *
     * @param codigo código del tipo de documento
     * @return InvoiceTypeCodeType con atributos
     */
    private InvoiceTypeCodeType crearCodigoTipoDocumento(String codigo) {
        InvoiceTypeCodeType codigoTipoDocumento = new InvoiceTypeCodeType();
        codigoTipoDocumento.setValue(codigo);
        codigoTipoDocumento.setListAgencyName("PE:SUNAT");
        codigoTipoDocumento.setListName("SUNAT:Identificador de Tipo de Documento");
        codigoTipoDocumento.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01");
        return codigoTipoDocumento;
    }

    /**
     * Crea el código de moneda del documento (ISO 4217).
     *
     * @param codigo código de moneda
     * @return DocumentCurrencyCodeType con atributos
     */
    private DocumentCurrencyCodeType crearCodigoMonedaDocumento(String codigo) {
        DocumentCurrencyCodeType codigoMonedaDocumento = new DocumentCurrencyCodeType();
        codigoMonedaDocumento.setListID("ISO 4217 Alpha");
        codigoMonedaDocumento.setName("Currency");
        codigoMonedaDocumento.setListAgencyName(
                "United Nations Economic Commission for Europe");
        codigoMonedaDocumento.setValue(codigo);
        return codigoMonedaDocumento;
    }

    /**
     * Configura las leyendas cuando existen.
     *
     * @param facturaUbl factura UBL a completar
     * @param leyendas   leyendas registradas
     */
    private void configurarLeyendasSiExisten(InvoiceType facturaUbl, List<LeyendaUblData> leyendas) {
        if (leyendas == null || leyendas.isEmpty()) {
            return;
        }
        facturaUbl.setNote(crearNotasLeyenda(leyendas));
    }

    /**
     * Configura la guía de remisión relacionada si existe.
     * <p>
     * Genera el tag {@code cac:DespatchDocumentReference} con:
     * <ul>
     * <li>{@code cbc:ID}: serie-numero de la guía</li>
     * <li>{@code cbc:DocumentTypeCode}: código del catálogo 01 (09=remitente,
     * 31=transportista)</li>
     * </ul>
     * </p>
     *
     * @param facturaUbl factura UBL a completar
     * @param data       datos de factura
     */
    private void configurarGuiaRemisionSiExiste(InvoiceType facturaUbl, FacturaUblData data) {
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

        facturaUbl.addDespatchDocumentReference(ref);
    }

    /**
     * Configura los documentos adicionales relacionados si existen.
     * <p>
     * Genera los tags {@code cac:AdditionalDocumentReference} con:
     * <ul>
     * <li>{@code cbc:ID}: identificador del documento</li>
     * <li>{@code cbc:DocumentTypeCode}: código del catálogo 12</li>
     * </ul>
     * </p>
     *
     * @param facturaUbl factura UBL a completar
     * @param data       datos de factura
     */
    private void configurarDocumentosAdicionalesSiExisten(InvoiceType facturaUbl, FacturaUblData data) {
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

            facturaUbl.addAdditionalDocumentReference(ref);
        }
    }

    /**
     * Configura emisor y receptor de la factura.
     *
     * @param facturaUbl factura UBL a completar
     * @param emisor     datos del emisor
     * @param receptor   datos del receptor
     */
    private void configurarPartes(
            InvoiceType facturaUbl,
            DatosEmisorFacturaUbl emisor,
            DatosReceptorFacturaUbl receptor) {
        facturaUbl.setAccountingSupplierParty(crearProveedor(emisor));

        if (receptor != null && receptor.nroDocumento() != null) {
            facturaUbl.setAccountingCustomerParty(crearCliente(receptor));
        }
    }

    /**
     * Configura los impuestos por categoría (gravadas, exoneradas, inafectas).
     * Genera un TaxTotal con tres subtotales según especificación SUNAT.
     *
     * @param facturaUbl factura UBL a completar
     * @param totales    DatosTotalesFacturaUbl con totales por categoría
     * @param encabezado datos de encabezado
     */
    private void configurarImpuestosSiCorresponde(
            InvoiceType facturaUbl,
            DatosTotalesFacturaUbl totales,
            DatosEncabezadoFacturaUbl encabezado) {
        if (totales == null) {
            return;
        }
        String moneda = encabezado.moneda();

        // Calcular total de impuestos
        BigDecimal totalImpuestos = valorOZero(totales.totalImpuestos());
        if (totalImpuestos.compareTo(BigDecimal.ZERO) <= 0
                && valorOZero(totales.gravadas()).compareTo(BigDecimal.ZERO) <= 0
                && valorOZero(totales.exoneradas()).compareTo(BigDecimal.ZERO) <= 0
                && valorOZero(totales.inafectas()).compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Crear TaxTotal con los tres subtotales
        TaxTotalType taxTotal = new TaxTotalType();
        taxTotal.setTaxAmount(totalImpuestos).setCurrencyID(moneda);

        // Subtotal operaciones gravadas
        BigDecimal gravadas = valorOZero(totales.gravadas());
        BigDecimal igv = valorOZero(totales.igv());
        if (gravadas.compareTo(BigDecimal.ZERO) > 0) {
            taxTotal.addTaxSubtotal(crearSubtotalImpuestoGravado(gravadas, igv, moneda));
        }

        // Subtotal operaciones exoneradas
        BigDecimal exoneradas = valorOZero(totales.exoneradas());
        if (exoneradas.compareTo(BigDecimal.ZERO) > 0) {
            taxTotal.addTaxSubtotal(crearSubtotalImpuestoExonerado(exoneradas, moneda));
        }

        // Subtotal operaciones inafectas
        BigDecimal inafectas = valorOZero(totales.inafectas());
        if (inafectas.compareTo(BigDecimal.ZERO) > 0) {
            taxTotal.addTaxSubtotal(crearSubtotalImpuestoInafecto(inafectas, moneda));
        }

        facturaUbl.addTaxTotal(taxTotal);
    }

    /**
     * Crea el subtotal de impuesto para operaciones gravadas (IGV).
     *
     * @param montoBase     base imponible gravada
     * @param montoImpuesto monto del IGV
     * @param moneda        código de moneda
     * @return TaxSubtotalType configurado
     */
    private TaxSubtotalType crearSubtotalImpuestoGravado(
            BigDecimal montoBase,
            BigDecimal montoImpuesto,
            String moneda) {
        TaxSubtotalType subtotal = new TaxSubtotalType();
        subtotal.setTaxableAmount(montoBase).setCurrencyID(moneda);
        subtotal.setTaxAmount(montoImpuesto).setCurrencyID(moneda);
        subtotal.setTaxCategory(crearCategoriaGravado());
        return subtotal;
    }

    /**
     * Crea el subtotal de impuesto para operaciones exoneradas.
     *
     * @param montoBase base imponible exonerada
     * @param moneda    código de moneda
     * @return TaxSubtotalType configurado
     */
    private TaxSubtotalType crearSubtotalImpuestoExonerado(
            BigDecimal montoBase,
            String moneda) {
        TaxSubtotalType subtotal = new TaxSubtotalType();
        subtotal.setTaxableAmount(montoBase).setCurrencyID(moneda);
        subtotal.setTaxAmount(BigDecimal.ZERO).setCurrencyID(moneda);
        subtotal.setTaxCategory(crearCategoriaExonerado());
        return subtotal;
    }

    /**
     * Crea el subtotal de impuesto para operaciones inafectas.
     *
     * @param montoBase base imponible inafecta
     * @param moneda    código de moneda
     * @return TaxSubtotalType configurado
     */
    private TaxSubtotalType crearSubtotalImpuestoInafecto(
            BigDecimal montoBase,
            String moneda) {
        TaxSubtotalType subtotal = new TaxSubtotalType();
        subtotal.setTaxableAmount(montoBase).setCurrencyID(moneda);
        subtotal.setTaxAmount(BigDecimal.ZERO).setCurrencyID(moneda);
        subtotal.setTaxCategory(crearCategoriaInafecto());
        return subtotal;
    }

    /**
     * Crea la categoría de impuesto para operaciones gravadas (category S).
     *
     * @return TaxCategoryType configurado
     */
    private TaxCategoryType crearCategoriaGravado() {
        TaxCategoryType categoria = new TaxCategoryType();
        IDType idCategoria = new IDType();
        idCategoria.setSchemeID("UN/ECE 5305");
        idCategoria.setSchemeName("Tax Category Identifier");
        idCategoria.setSchemeAgencyName("United Nations Economic Commission for Europe");
        idCategoria.setValue(CATEGORIA_GRAVADO);
        categoria.setID(idCategoria);
        categoria.setPercent(PORCENTAJE_IGV);
        categoria.setTaxScheme(crearEsquemaImpuestoIgv());
        return categoria;
    }

    /**
     * Crea la categoría de impuesto para operaciones exoneradas (category E).
     *
     * @return TaxCategoryType configurado
     */
    private TaxCategoryType crearCategoriaExonerado() {
        TaxCategoryType categoria = new TaxCategoryType();
        IDType idCategoria = new IDType();
        idCategoria.setSchemeID("UN/ECE 5305");
        idCategoria.setSchemeName("Tax Category Identifier");
        idCategoria.setSchemeAgencyName("United Nations Economic Commission for Europe");
        idCategoria.setValue(CATEGORIA_EXONERADO);
        categoria.setID(idCategoria);
        categoria.setTaxScheme(crearEsquemaImpuestoExonerado());
        return categoria;
    }

    /**
     * Crea la categoría de impuesto para operaciones inafectas (category O).
     *
     * @return TaxCategoryType configurado
     */
    private TaxCategoryType crearCategoriaInafecto() {
        TaxCategoryType categoria = new TaxCategoryType();
        IDType idCategoria = new IDType();
        idCategoria.setSchemeID("UN/ECE 5305");
        idCategoria.setSchemeName("Tax Category Identifier");
        idCategoria.setSchemeAgencyName("United Nations Economic Commission for Europe");
        idCategoria.setValue(CATEGORIA_INAFECTO);
        categoria.setID(idCategoria);
        categoria.setTaxScheme(crearEsquemaImpuestoInafecto());
        return categoria;
    }

    /**
     * Crea el esquema de impuesto IGV.
     *
     * @return TaxSchemeType configurado
     */
    private TaxSchemeType crearEsquemaImpuestoIgv() {
        TaxSchemeType esquema = new TaxSchemeType();
        IDType idEsquema = new IDType();
        idEsquema.setSchemeID("UN/ECE 5153");
        idEsquema.setSchemeAgencyID("6");
        idEsquema.setValue(CODIGO_TRIBUTO_IGV);
        esquema.setID(idEsquema);
        esquema.setName(NOMBRE_IMPUESTO_IGV);
        esquema.setTaxTypeCode(TIPO_TRIBUTO_VAT);
        return esquema;
    }

    /**
     * Crea el esquema de impuesto Exonerado.
     *
     * @return TaxSchemeType configurado
     */
    private TaxSchemeType crearEsquemaImpuestoExonerado() {
        TaxSchemeType esquema = new TaxSchemeType();
        IDType idEsquema = new IDType();
        idEsquema.setSchemeID("UN/ECE 5153");
        idEsquema.setSchemeAgencyID("6");
        idEsquema.setValue(CODIGO_TRIBUTO_EXONERADO);
        esquema.setID(idEsquema);
        esquema.setName(NOMBRE_IMPUESTO_EXONERADO);
        esquema.setTaxTypeCode(TIPO_TRIBUTO_VAT);
        return esquema;
    }

    /**
     * Crea el esquema de impuesto Inafecto.
     *
     * @return TaxSchemeType configurado
     */
    private TaxSchemeType crearEsquemaImpuestoInafecto() {
        TaxSchemeType esquema = new TaxSchemeType();
        IDType idEsquema = new IDType();
        idEsquema.setSchemeID("UN/ECE 5153");
        idEsquema.setSchemeAgencyID("6");
        idEsquema.setValue(CODIGO_TRIBUTO_INAFECTO);
        esquema.setID(idEsquema);
        esquema.setName(NOMBRE_IMPUESTO_INAFECTO);
        esquema.setTaxTypeCode(TIPO_TRIBUTO_FRE);
        return esquema;
    }

    /**
     * Configura el total de impuestos con subtotales si existe.
     *
     * @param facturaUbl       factura UBL a completar
     * @param impuestosTotales impuestos totales
     * @param encabezado       datos de encabezado
     */
    private void configurarImpuestosTotalesSiExisten(
            InvoiceType facturaUbl,
            DatosImpuestoTotalFacturaUbl impuestosTotales,
            DatosEncabezadoFacturaUbl encabezado) {
        if (impuestosTotales == null) {
            return;
        }
        facturaUbl.addTaxTotal(crearImpuestoTotal(impuestosTotales, encabezado.moneda()));
    }

    /**
     * Configura los totales monetarios de la factura.
     *
     * @param facturaUbl        factura UBL a completar
     * @param totalesMonetarios DatosTotalesMonetariosFacturaUbl
     * @param totalesImpuestos  DatosTotalesFacturaUbl
     * @param encabezado        datos de encabezado
     */
    private void configurarTotales(
            InvoiceType facturaUbl,
            DatosTotalesMonetariosFacturaUbl totalesMonetarios,
            DatosTotalesFacturaUbl totalesImpuestos,
            DatosEncabezadoFacturaUbl encabezado) {
        facturaUbl.setLegalMonetaryTotal(crearTotalesMonetarios(totalesMonetarios, totalesImpuestos,
                encabezado));
    }

    /**
     * Configura las líneas de detalle cuando existen.
     *
     * @param facturaUbl factura UBL a completar
     * @param encabezado datos de encabezado
     * @param lineas     líneas de detalle
     */
    private void configurarLineas(
            InvoiceType facturaUbl,
            DatosEncabezadoFacturaUbl encabezado,
            List<FacturaLineaUblData> lineas) {
        if (lineas == null || lineas.isEmpty()) {
            return;
        }

        String moneda = encabezado.moneda();
        for (FacturaLineaUblData linea : lineas) {
            facturaUbl.addInvoiceLine(crearLineaFactura(linea, moneda));
        }
    }

    /**
     * Serializa la factura UBL a XML.
     *
     * @param facturaUbl factura UBL a serializar
     * @return XML serializado
     * @throws Exception si hay error al serializar
     */
    private String serializarFactura(InvoiceType facturaUbl) throws Exception {
        StringWriter escritorXml = new StringWriter();
        UBL21Marshaller.invoice()
                .setFormattedOutput(true)
                .setUseSchema(false)
                .write(facturaUbl, escritorXml);

        return escritorXml.toString();
    }

    /**
     * Crea el proveedor/emisor de la factura.
     *
     * @param emisor datos del emisor
     * @return datos del emisor
     */
    private SupplierPartyType crearProveedor(DatosEmisorFacturaUbl emisor) {
        SupplierPartyType proveedor = new SupplierPartyType();
        PartyType parte = new PartyType();

        if (emisor.razonSocial() != null) {
            PartyLegalEntityType entidadLegal = new PartyLegalEntityType();
            entidadLegal.setRegistrationName(emisor.razonSocial());

            AddressType direccionRegistro = new AddressType();
            AddressTypeCodeType codigoDireccion = new AddressTypeCodeType();
            String codigoDomicilio = emisor.codigoDomicilio();
            codigoDireccion.setValue(codigoDomicilio != null
                    ? codigoDomicilio
                    : CODIGO_DOMICILIO_POR_DEFECTO);
            direccionRegistro.setAddressTypeCode(codigoDireccion);
            entidadLegal.setRegistrationAddress(direccionRegistro);

            parte.addPartyLegalEntity(entidadLegal);
        }

        if (emisor.nombreComercial() != null) {
            PartyNameType nombreComercial = new PartyNameType();
            NameType nombre = new NameType();
            nombre.setValue(emisor.nombreComercial());
            nombreComercial.setName(nombre);
            parte.addPartyName(nombreComercial);
        }

        if (emisor.nroDocumento() != null) {
            parte.addPartyTaxScheme(crearEsquemaTributarioEmisor(emisor));
        }

        proveedor.setParty(parte);
        return proveedor;
    }

    /**
     * Crea el esquema tributario del emisor.
     *
     * @param emisor datos del emisor
     * @return esquema tributario configurado
     */
    private @NonNull PartyTaxSchemeType crearEsquemaTributarioEmisor(DatosEmisorFacturaUbl emisor) {
        PartyTaxSchemeType esquemaTributario = new PartyTaxSchemeType();
        esquemaTributario.setRegistrationName(emisor.razonSocial());
        esquemaTributario.setCompanyID(crearIdentificadorEmpresa(emisor.nroDocumento()));
        esquemaTributario.setRegistrationAddress(
                crearDireccionRegistro(emisor.codigoDomicilio()));
        // TaxScheme del Emisor
        /*
         * <cac:TaxScheme>
         * <cbc:ID>-</cbc:ID>
         * </cac:TaxScheme>
         */
        TaxSchemeType esquemaImpuesto = new TaxSchemeType();
        esquemaImpuesto.setID(NO_HAY_CODIGO_DE_REGIMEN_ESPECIAL);
        esquemaTributario.setTaxScheme(esquemaImpuesto);

        return esquemaTributario;
    }

    /**
     * Crea la dirección de registro cuando existe código de domicilio.
     *
     * @param codigoDomicilio código de domicilio
     * @return dirección UBL o null si no existe
     */
    private @Nullable AddressType crearDireccionRegistro(String codigoDomicilio) {
        if (codigoDomicilio == null) {
            return null;
        }
        AddressType direccion = new AddressType();
        AddressTypeCodeType codigoDireccion = new AddressTypeCodeType();
        codigoDireccion.setValue(codigoDomicilio);
        direccion.setAddressTypeCode(codigoDireccion);
        return direccion;
    }

    /**
     * Crea el identificador de empresa (RUC) del emisor o receptor.
     *
     * @param numeroDocumento número de documento
     * @return identificador UBL
     */
    private CompanyIDType crearIdentificadorEmpresa(String numeroDocumento) {
        CompanyIDType identificadorEmpresa = new CompanyIDType();
        identificadorEmpresa.setSchemeID("6");
        identificadorEmpresa.setSchemeName(
                "SUNAT:Identificador de Documento de Identidad");
        identificadorEmpresa.setSchemeAgencyName("PE:SUNAT");
        identificadorEmpresa.setSchemeURI(
                "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
        identificadorEmpresa.setValue(numeroDocumento);
        return identificadorEmpresa;
    }

    /**
     * Crea el cliente/receptor de la factura.
     *
     * @param receptor datos del receptor
     * @return datos del receptor
     */
    private CustomerPartyType crearCliente(DatosReceptorFacturaUbl receptor) {
        CustomerPartyType cliente = new CustomerPartyType();
        PartyType parte = new PartyType();
        PartyTaxSchemeType esquemaTributario = new PartyTaxSchemeType();
        if (receptor.razonSocial() != null) {
            esquemaTributario.setRegistrationName(receptor.razonSocial());
            esquemaTributario.setCompanyID(crearIdentificadorEmpresa(receptor.nroDocumento()));
            TaxSchemeType esquemaImpuesto = new TaxSchemeType();
            esquemaImpuesto.setID(NO_HAY_CODIGO_DE_REGIMEN_ESPECIAL);
            esquemaTributario.setTaxScheme(esquemaImpuesto);
            parte.addPartyTaxScheme(esquemaTributario);
        }

        cliente.setParty(parte);
        return cliente;
    }

    /**
     * Crea el total de impuestos (IGV).
     *
     * @param igv    monto del IGV
     * @param moneda código de moneda
     * @return detalle del impuesto
     */
    private TaxTotalType crearImpuestoTotal(BigDecimal igv, String moneda) {
        TaxTotalType totalImpuesto = new TaxTotalType();

        totalImpuesto.setTaxAmount(igv).setCurrencyID(moneda);

        TaxSubtotalType subtotalImpuesto = new TaxSubtotalType();
        subtotalImpuesto.setTaxableAmount(igv).setCurrencyID(moneda);
        subtotalImpuesto.setTaxAmount(igv).setCurrencyID(moneda);
        subtotalImpuesto.setTaxCategory(crearCategoriaImpuestoIgv());
        totalImpuesto.addTaxSubtotal(subtotalImpuesto);

        return totalImpuesto;
    }

    /**
     * Crea el total de impuestos con subtotales.
     *
     * @param impuestosTotales impuestos totales
     * @param moneda           código de moneda
     * @return impuesto total UBL
     */
    private TaxTotalType crearImpuestoTotal(
            DatosImpuestoTotalFacturaUbl impuestosTotales,
            String moneda) {
        TaxTotalType totalImpuesto = new TaxTotalType();
        totalImpuesto.setTaxAmount(valorOZero(impuestosTotales.montoTotal())).setCurrencyID(moneda);

        List<DatosImpuestoSubtotalFacturaUbl> subtotales = impuestosTotales.subtotales();
        if (subtotales == null) {
            return totalImpuesto;
        }

        for (DatosImpuestoSubtotalFacturaUbl subtotal : subtotales) {
            totalImpuesto.addTaxSubtotal(crearImpuestoSubtotal(subtotal, moneda));
        }

        return totalImpuesto;
    }

    /**
     * Crea un subtotal de impuesto.
     *
     * @param subtotal datos del subtotal
     * @param moneda   código de moneda
     * @return subtotal UBL
     */
    private TaxSubtotalType crearImpuestoSubtotal(
            DatosImpuestoSubtotalFacturaUbl subtotal,
            String moneda) {
        TaxSubtotalType subtotalUbl = new TaxSubtotalType();
        subtotalUbl.setTaxableAmount(valorOZero(subtotal.montoBase())).setCurrencyID(moneda);
        subtotalUbl.setTaxAmount(valorOZero(subtotal.montoImpuesto())).setCurrencyID(moneda);
        subtotalUbl.setTaxCategory(crearCategoriaImpuesto(subtotal.categoria()));
        return subtotalUbl;
    }

    /**
     * Crea la categoría de impuesto desde datos.
     *
     * @param categoria datos de categoría
     * @return categoría UBL
     */
    private TaxCategoryType crearCategoriaImpuesto(DatosCategoriaImpuestoFacturaUbl categoria) {
        TaxCategoryType categoriaUbl = new TaxCategoryType();
        if (categoria == null) {
            return categoriaUbl;
        }
        if (categoria.codigoCategoria() != null) {
            IDType idCategoria = new IDType();
            idCategoria.setValue(categoria.codigoCategoria());
            idCategoria.setSchemeID("UN/ECE 5305");
            idCategoria.setSchemeName("Tax Category Identifier");
            idCategoria.setSchemeAgencyName("United Nations Economic Commission for Europe");
            categoriaUbl.setID(idCategoria);
        }
        if (categoria.porcentaje() != null) {
            categoriaUbl.setPercent(categoria.porcentaje());
        }
        if (categoria.codigoAfectacionIgv() != null) {
            TaxExemptionReasonCodeType codigoAfectacion = new TaxExemptionReasonCodeType();
            codigoAfectacion.setValue(categoria.codigoAfectacionIgv());
            codigoAfectacion.setListAgencyName("PE:SUNAT");
            codigoAfectacion.setListName("SUNAT:Codigo de Tipo de Afectación del IGV");
            codigoAfectacion.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07");
            categoriaUbl.setTaxExemptionReasonCode(codigoAfectacion);
        }
        categoriaUbl.setTaxScheme(crearEsquemaImpuesto(categoria.esquema()));
        return categoriaUbl;
    }

    /**
     * Crea el esquema de impuesto UBL.
     *
     * @param esquema datos del esquema
     * @return esquema UBL
     */
    private TaxSchemeType crearEsquemaImpuesto(DatosEsquemaImpuestoFacturaUbl esquema) {
        TaxSchemeType esquemaUbl = new TaxSchemeType();
        if (esquema == null) {
            return esquemaUbl;
        }
        if (esquema.codigoTributo() != null) {
            IDType id = new IDType();
            id.setSchemeID("UN/ECE 5153");
            id.setSchemeAgencyID("6");
            id.setValue(esquema.codigoTributo());
            esquemaUbl.setID(id);
        }
        esquemaUbl.setName(esquema.nombreTributo());
        esquemaUbl.setTaxTypeCode(esquema.codigoTipoTributo());
        return esquemaUbl;
    }

    /**
     * Crea la categoría de impuesto IGV.
     *
     * @return categoría de impuesto configurada
     */
    private static TaxCategoryType crearCategoriaImpuestoIgv() {
        TaxCategoryType categoriaImpuesto = new TaxCategoryType();
        IDType idCategoria = new IDType();
        idCategoria.setSchemeID("UN/ECE 5305");
        idCategoria.setSchemeName("Tax Category Identifier");
        idCategoria.setSchemeAgencyName("United Nations Economic Commission for Europe");
        idCategoria.setValue(CATEGORIA_GRAVADO);
        categoriaImpuesto.setID(idCategoria);
        categoriaImpuesto.setPercent(PORCENTAJE_IGV);

        TaxSchemeType esquema = new TaxSchemeType();
        IDType idEsquema = new IDType();
        idEsquema.setSchemeID("UNCL5305");
        idEsquema.setSchemeAgencyID("6");
        idEsquema.setValue("VAT");
        esquema.setID(idEsquema);
        esquema.setName(NOMBRE_IMPUESTO_IGV);
        categoriaImpuesto.setTaxScheme(esquema);
        return categoriaImpuesto;
    }

    /**
     * Crea los totales monetarios de la factura.
     *
     * @param totalesMonetarios datos monetarios
     * @param totalesImpuestos  datos de totales con impuestos
     * @param encabezado        datos de encabezado
     * @return totales monetarios
     */
    private MonetaryTotalType crearTotalesMonetarios(
            DatosTotalesMonetariosFacturaUbl totalesMonetarios,
            DatosTotalesFacturaUbl totalesImpuestos,
            DatosEncabezadoFacturaUbl encabezado) {
        MonetaryTotalType totalesUbl = new MonetaryTotalType();

        if (totalesMonetarios != null) {
            BigDecimal totalValorVenta = valorOZero(totalesMonetarios.totalValorVenta());
            BigDecimal totalPrecioVenta = valorOZero(totalesMonetarios.totalPrecioVenta());
            BigDecimal totalDescuentos = valorOZero(totalesMonetarios.totalDescuentos());
            BigDecimal importePagar = valorOZero(totalesMonetarios.importeTotalPagar());

            totalesUbl.setLineExtensionAmount(totalValorVenta).setCurrencyID(encabezado.moneda());
            totalesUbl.setTaxInclusiveAmount(totalPrecioVenta).setCurrencyID(encabezado.moneda());
            totalesUbl.setAllowanceTotalAmount(totalDescuentos).setCurrencyID(encabezado.moneda());
            totalesUbl.setPayableAmount(importePagar).setCurrencyID(encabezado.moneda());
            return totalesUbl;
        }

        if (totalesImpuestos == null) {
            return totalesUbl;
        }

        BigDecimal subtotal = valorOZero(totalesImpuestos.valorVenta());
        BigDecimal impuestos = valorOZero(totalesImpuestos.totalImpuestos());
        BigDecimal total = totalesImpuestos.importeTotal() != null
                ? totalesImpuestos.importeTotal()
                : subtotal.add(impuestos);

        totalesUbl.setLineExtensionAmount(subtotal).setCurrencyID(encabezado.moneda());
        totalesUbl.setTaxExclusiveAmount(subtotal).setCurrencyID(encabezado.moneda());
        totalesUbl.setTaxInclusiveAmount(total).setCurrencyID(encabezado.moneda());
        totalesUbl.setPayableAmount(total).setCurrencyID(encabezado.moneda());

        return totalesUbl;
    }

    /**
     * Crea un descuento global en UBL.
     *
     * @param descuento datos del descuento
     * @param moneda    código de moneda
     * @return descuento UBL
     */
    private AllowanceChargeType crearDescuentoGlobal(
            DatosDescuentoGlobalFacturaUbl descuento,
            String moneda) {
        AllowanceChargeType allowance = new AllowanceChargeType();
        allowance.setChargeIndicator(descuento.esCargo());
        allowance.setAllowanceChargeReasonCode(descuento.codigoMotivo());
        allowance.setMultiplierFactorNumeric(descuento.porcentaje());
        allowance.setAmount(descuento.monto()).setCurrencyID(moneda);
        allowance.setBaseAmount(descuento.montoBase()).setCurrencyID(moneda);
        return allowance;
    }

    /**
     * Crea una línea de detalle de la factura.
     *
     * @param linea  datos de la línea
     * @param moneda código de moneda
     * @return línea UBL
     */
    private InvoiceLineType crearLineaFactura(FacturaLineaUblData linea, String moneda) {
        InvoiceLineType lineaUbl = new InvoiceLineType();

        // CAMPO 35: Número de orden del ítem
        int numeroLinea = linea.numero() != null ? linea.numero() : 1;
        lineaUbl.setID(String.valueOf(numeroLinea));

        // CAMPO 36: Cantidad y unidad de medida (InvoicedQuantity)
        BigDecimal cantidad = linea.cantidad();
        String unidadMedida = linea.unidadMedida();
        if (cantidad != null) {
            lineaUbl.setInvoicedQuantity(crearCantidad(cantidad, unidadMedida));
        }

        // CAMPO 37: Valor de venta del ítem (sin impuestos)
        BigDecimal valorVenta = valorOZero(linea.valorVenta());
        lineaUbl.setLineExtensionAmount(valorVenta).setCurrencyID(moneda);

        // CAMPO 38: Precio de venta unitario (PricingReference)
        DatosPrecioReferenciaFacturaUbl precioReferencia = linea.precioReferencia();
        if (precioReferencia != null) {
            lineaUbl.setPricingReference(crearPrecioReferencia(precioReferencia, moneda));
        }

        // CAMPO 40: Descuentos por ítem
        DatosDescuentoLineaFacturaUbl descuentoLinea = linea.descuentoLinea();
        if (descuentoLinea != null) {
            lineaUbl.addAllowanceCharge(crearDescuentoLinea(descuentoLinea, moneda));
        }

        // CAMPO 42: Afectación al IGV por ítem (TaxTotal por línea)
        lineaUbl.addTaxTotal(crearImpuestoLinea(linea, moneda));

        // CAMPO 44-46: Descripción y códigos del producto
        ItemType item = crearItem(linea);
        lineaUbl.setItem(item);

        // CAMPO 48: Valor unitario del ítem (Price)
        BigDecimal precioUnitario = linea.precioUnitario();
        if (precioUnitario != null) {
            PriceType precio = new PriceType();
            precio.setPriceAmount(precioUnitario).setCurrencyID(moneda);
            lineaUbl.setPrice(precio);
        }

        return lineaUbl;
    }

    /**
     * Crea la cantidad con unidad de medida para la línea.
     *
     * @param cantidad     cantidad del ítem
     * @param unidadMedida código de unidad (NIU, KG, etc.)
     * @return InvoicedQuantityType configurado
     */
    private InvoicedQuantityType crearCantidad(BigDecimal cantidad, String unidadMedida) {
        InvoicedQuantityType cantidadUbl = new InvoicedQuantityType();
        cantidadUbl.setValue(cantidad);

        String unidad = unidadMedida != null ? unidadMedida : "NIU";
        cantidadUbl.setUnitCode(unidad);
        cantidadUbl.setUnitCodeListID("UN/ECE rec 20");
        cantidadUbl.setUnitCodeListAgencyName("United Nations Economic Commission for Europe");

        return cantidadUbl;
    }

    /**
     * Crea el impuesto (TaxTotal) para una línea de detalle.
     * Incluye el código de afectación IGV según el tipo de operación.
     *
     * @param linea  datos de la línea
     * @param moneda código de moneda
     * @return TaxTotalType configurado para la línea
     */
    private TaxTotalType crearImpuestoLinea(FacturaLineaUblData linea, String moneda) {
        BigDecimal montoIGV = valorOZero(linea.montoIGV());
        BigDecimal montoBaseIGV = valorOZero(linea.montoBaseIGV());
        BigDecimal porcentajeIGV = linea.porcentajeIGV();
        String tipoAfectacion = linea.tipoAfectacionIGV();

        TaxTotalType taxTotal = new TaxTotalType();
        taxTotal.setTaxAmount(montoIGV).setCurrencyID(moneda);

        // Crear el TaxSubtotal según el tipo de afectación IGV
        TaxSubtotalType subtotal = new TaxSubtotalType();
        subtotal.setTaxableAmount(montoBaseIGV).setCurrencyID(moneda);
        subtotal.setTaxAmount(montoIGV).setCurrencyID(moneda);

        // Crear la categoría según tipo de afectación
        TaxCategoryType categoria = crearCategoriaImpuestoLinea(
                tipoAfectacion,
                porcentajeIGV != null ? porcentajeIGV : PORCENTAJE_IGV);
        subtotal.setTaxCategory(categoria);

        taxTotal.addTaxSubtotal(subtotal);
        return taxTotal;
    }

    /**
     * Crea la categoría de impuesto para una línea según el tipo de afectación.
     *
     * @param tipoAfectacion código de afectación IGV (10=Gravado, 20=Exonerado,
     *                       30/31=Inafecto)
     * @param porcentajeIGV  porcentaje del IGV
     * @return TaxCategoryType configurado
     */
    private TaxCategoryType crearCategoriaImpuestoLinea(String tipoAfectacion, BigDecimal porcentajeIGV) {
        TaxCategoryType categoria = new TaxCategoryType();

        // Código de categoría (S=Gravado, E=Exonerado, O=Inafecto)
        String codigoCategoria;
        String codigoTributo;
        String nombreTributo;
        String tipoTributo;

        if ("10".equals(tipoAfectacion)) {
            // Gravado
            codigoCategoria = CATEGORIA_GRAVADO;
            codigoTributo = CODIGO_TRIBUTO_IGV;
            nombreTributo = NOMBRE_IMPUESTO_IGV;
            tipoTributo = TIPO_TRIBUTO_VAT;
        } else if ("20".equals(tipoAfectacion)) {
            // Exonerado
            codigoCategoria = CATEGORIA_EXONERADO;
            codigoTributo = CODIGO_TRIBUTO_EXONERADO;
            nombreTributo = NOMBRE_IMPUESTO_EXONERADO;
            tipoTributo = TIPO_TRIBUTO_VAT;
        } else if ("30".equals(tipoAfectacion) || "31".equals(tipoAfectacion)) {
            // Inafecto (30=onerosa, 31=gratuita)
            codigoCategoria = CATEGORIA_INAFECTO;
            codigoTributo = CODIGO_TRIBUTO_INAFECTO;
            nombreTributo = NOMBRE_IMPUESTO_INAFECTO;
            tipoTributo = TIPO_TRIBUTO_FRE;
        } else {
            // Por defecto gravado
            codigoCategoria = CATEGORIA_GRAVADO;
            codigoTributo = CODIGO_TRIBUTO_IGV;
            nombreTributo = NOMBRE_IMPUESTO_IGV;
            tipoTributo = TIPO_TRIBUTO_VAT;
        }

        // ID de categoría
        IDType idCategoria = new IDType();
        idCategoria.setSchemeID("UN/ECE 5305");
        idCategoria.setSchemeName("Tax Category Identifier");
        idCategoria.setSchemeAgencyName("United Nations Economic Commission for Europe");
        idCategoria.setValue(codigoCategoria);
        categoria.setID(idCategoria);

        // Porcentaje (solo para gravado)
        if (CATEGORIA_GRAVADO.equals(codigoCategoria)) {
            categoria.setPercent(porcentajeIGV);
        }

        // Código de afectación IGV (TaxExemptionReasonCode)
        if (tipoAfectacion != null) {
            TaxExemptionReasonCodeType codigoAfectacion = new TaxExemptionReasonCodeType();
            codigoAfectacion.setValue(tipoAfectacion);
            codigoAfectacion.setListAgencyName("PE:SUNAT");
            codigoAfectacion.setListName("SUNAT:Codigo de Tipo de Afectación del IGV");
            codigoAfectacion.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07");
            categoria.setTaxExemptionReasonCode(codigoAfectacion);
        }

        // Esquema de impuesto (TaxScheme)
        TaxSchemeType esquema = new TaxSchemeType();
        IDType idEsquema = new IDType();
        idEsquema.setSchemeID("UN/ECE 5153");
        idEsquema.setSchemeAgencyID("6");
        idEsquema.setValue(codigoTributo);
        esquema.setID(idEsquema);
        esquema.setName(nombreTributo);
        esquema.setTaxTypeCode(tipoTributo);
        categoria.setTaxScheme(esquema);

        return categoria;
    }

    /**
     * Crea el ítem de la línea de detalle con descripción y código de producto.
     *
     * @param linea datos de la línea
     * @return ItemType configurado
     */
    private ItemType crearItem(FacturaLineaUblData linea) {
        ItemType item = new ItemType();

        // Agregar descripción del producto
        String descripcion = linea.descripcion();
        if (descripcion != null && !descripcion.isBlank()) {
            DescriptionType descripcionUbl = new DescriptionType();
            descripcionUbl.setValue(descripcion);
            item.setDescription(Collections.singletonList(descripcionUbl));
        }

        // Agregar código de producto del vendedor ( SellersItemIdentification )
        String codigoProducto = linea.codigoProducto();
        if (codigoProducto != null && !codigoProducto.isBlank()) {
            ItemIdentificationType sellersItemIdentification = new ItemIdentificationType();
            IDType idProducto = new IDType();
            idProducto.setValue(codigoProducto);
            sellersItemIdentification.setID(idProducto);
            item.setSellersItemIdentification(sellersItemIdentification);
        }

        // Agregar código de producto SUNAT/UNSPSC si existe
        String codigoProductoSunat = linea.codigoProductoSunat();
        if (codigoProductoSunat != null && !codigoProductoSunat.isBlank()) {
            ItemIdentificationType standardItemIdentification = new ItemIdentificationType();
            IDType idSunat = new IDType();
            idSunat.setValue(codigoProductoSunat);
            standardItemIdentification.setID(idSunat);
            item.setStandardItemIdentification(standardItemIdentification);
        }

        return item;
    }

    /**
     * Crea el precio de referencia para la línea.
     *
     * @param precioReferencia datos del precio de referencia
     * @param moneda           código de moneda
     * @return pricing reference UBL
     */
    private PricingReferenceType crearPrecioReferencia(
            DatosPrecioReferenciaFacturaUbl precioReferencia,
            String moneda) {
        PricingReferenceType pricingReference = new PricingReferenceType();
        PriceType precio = new PriceType();
        precio.setPriceAmount(precioReferencia.precio()).setCurrencyID(moneda);

        PriceTypeCodeType codigoTipoPrecio = new PriceTypeCodeType();
        codigoTipoPrecio.setValue(precioReferencia.codigoTipoPrecio());
        codigoTipoPrecio.setListName("SUNAT:Indicador de Tipo de Precio");
        codigoTipoPrecio.setListAgencyName("PE:SUNAT");
        codigoTipoPrecio.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16");

        PriceType alternativa = new PriceType();
        alternativa.setPriceAmount(precio.getPriceAmount());
        alternativa.setPriceTypeCode(codigoTipoPrecio);

        pricingReference.addAlternativeConditionPrice(alternativa);
        return pricingReference;
    }

    /**
     * Crea el descuento por línea.
     *
     * @param descuento datos del descuento
     * @param moneda    código de moneda
     * @return descuento UBL
     */
    private AllowanceChargeType crearDescuentoLinea(
            DatosDescuentoLineaFacturaUbl descuento,
            String moneda) {
        AllowanceChargeType allowance = new AllowanceChargeType();
        allowance.setChargeIndicator(descuento.esCargo());
        allowance.setAllowanceChargeReasonCode(descuento.codigoMotivo());
        allowance.setMultiplierFactorNumeric(descuento.porcentaje());
        allowance.setAmount(descuento.monto()).setCurrencyID(moneda);
        allowance.setBaseAmount(descuento.montoBase()).setCurrencyID(moneda);
        return allowance;
    }

    /**
     * Crea una lista de notas (leyendas) a partir de una lista de descripciones.
     *
     * @param leyendas lista de descripciones de leyendas
     * @return lista de notas UBL
     */
    private List<NoteType> crearNotasLeyenda(List<LeyendaUblData> leyendas) {
        return leyendas
                .stream()
                .map(leyenda -> {
                    NoteType nota = new NoteType();
                    nota.setLanguageLocaleID(leyenda.codigoLocal());
                    nota.setValue(leyenda.leyenda());
                    return nota;
                })
                .toList();
    }

    /**
     * Devuelve el valor o cero cuando es nulo.
     *
     * @param valor monto opcional
     * @return monto o cero
     */
    private BigDecimal valorOZero(@Nullable BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}
