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
    private static final String CODIGO_IMPUESTO_IGV = "1000";
    private static final String NOMBRE_IMPUESTO_IGV = "IGV";
    private static final BigDecimal PORCENTAJE_IGV = new BigDecimal("18.00");

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
 * @param totalesImpuestos DatosTotalesFacturaUbl
 * @param encabezado datos de encabezado
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
     * Configura la firma digital si existe.
     *
     * @param facturaUbl factura UBL a completar
     * @param firma datos de firma
     */
    private void configurarFirmaSiExiste(InvoiceType facturaUbl, DatosFirmaFacturaUbl firma) {
        if (firma == null) {
            return;
        }
        SignatureType signature = new SignatureType();
        signature.setID(firma.id());

        PartyType signatoryParty = new PartyType();
        PartyIdentificationType identificacion = new PartyIdentificationType();
        IDType id = new IDType();
        id.setValue(firma.rucFirmante());
        identificacion.setID(id);
        signatoryParty.addPartyIdentification(identificacion);

        PartyNameType partyName = new PartyNameType();
        NameType nombre = new NameType();
        nombre.setValue(firma.nombreFirmante());
        partyName.setName(nombre);
        signatoryParty.addPartyName(partyName);

        signature.setSignatoryParty(signatoryParty);

        ExternalReferenceType externalReference = new ExternalReferenceType();
        URIType uri = new URIType();
        uri.setValue(firma.uriReferencia());
        externalReference.setURI(uri);

        /*DigitalSignatureAttachmentT firmaDigital = new DigitalSignatureAttachmentType();
        firmaDigital.setExternalReference(externalReference);
        signature.setDigitalSignatureAttachment(firmaDigital);*/

        facturaUbl.addSignature(signature);
    }

    /**
     * Configura la referencia de orden si existe.
     *
     * @param facturaUbl factura UBL a completar
     * @param referenciaOrden datos de referencia de orden
     */
    private void configurarReferenciaOrdenSiExiste(InvoiceType facturaUbl, DatosReferenciaOrdenFacturaUbl referenciaOrden) {
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
     * @param facturaUbl factura UBL a completar
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
     * @param leyendas leyendas registradas
     */
    private void configurarLeyendasSiExisten(InvoiceType facturaUbl, List<LeyendaUblData> leyendas) {
        if (leyendas == null || leyendas.isEmpty()) {
            return;
        }
        facturaUbl.setNote(crearNotasLeyenda(leyendas));
    }

    /**
     * Configura emisor y receptor de la factura.
     *
     * @param facturaUbl factura UBL a completar
     * @param emisor datos del emisor
     * @param receptor datos del receptor
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
 * Configura los impuestos si aplica IGV.
 *
 * @param facturaUbl factura UBL a completar
 * @param totales DatosTotalesFacturaUbl
 * @param encabezado datos de encabezado
 */
    private void configurarImpuestosSiCorresponde(
            InvoiceType facturaUbl,
            DatosTotalesFacturaUbl totales,
            DatosEncabezadoFacturaUbl encabezado) {
        if (totales == null) {
            return;
        }
        BigDecimal igv = totales.igv();
        if (igv != null && igv.compareTo(BigDecimal.ZERO) > 0) {
            facturaUbl.addTaxTotal(crearImpuestoTotal(igv, encabezado.moneda()));
        }
    }

    /**
     * Configura el total de impuestos con subtotales si existe.
     *
     * @param facturaUbl factura UBL a completar
     * @param impuestosTotales impuestos totales
     * @param encabezado datos de encabezado
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
 * @param facturaUbl factura UBL a completar
 * @param totalesMonetarios DatosTotalesMonetariosFacturaUbl
 * @param totalesImpuestos DatosTotalesFacturaUbl
 * @param encabezado datos de encabezado
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
     * @param lineas líneas de detalle
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

        TaxSchemeType esquemaImpuesto = new TaxSchemeType();
        IDType idEsquema = new IDType();
        idEsquema.setSchemeID("UN/ECE 5153");
        idEsquema.setSchemeAgencyID("6");
        idEsquema.setValue(CODIGO_IMPUESTO_IGV);
        esquemaImpuesto.setID(idEsquema);
        esquemaImpuesto.setName(NOMBRE_IMPUESTO_IGV);
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
            esquemaImpuesto.setID("-");
            esquemaTributario.setTaxScheme(esquemaImpuesto);
            parte.addPartyTaxScheme(esquemaTributario);
        }

        cliente.setParty(parte);
        return cliente;
    }

    /**
     * Crea el total de impuestos (IGV).
     *
     * @param igv monto del IGV
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
     * @param moneda código de moneda
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
     * @param moneda código de moneda
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
        idCategoria.setSchemeID("UNCL5305");
        idCategoria.setSchemeAgencyID("6");
        idCategoria.setValue(CODIGO_IMPUESTO_IGV);
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
     * @param totalesImpuestos datos de totales con impuestos
     * @param encabezado datos de encabezado
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
     * @param moneda código de moneda
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
     * @param linea datos de la línea
     * @param moneda código de moneda
     * @return línea UBL
     */
    private InvoiceLineType crearLineaFactura(FacturaLineaUblData linea, String moneda) {
        InvoiceLineType lineaUbl = new InvoiceLineType();

        int numeroLinea = linea.numero() != null ? linea.numero() : 1;
        lineaUbl.setID(String.valueOf(numeroLinea));

        BigDecimal valorVenta = valorOZero(linea.valorVenta());
        lineaUbl.setLineExtensionAmount(valorVenta).setCurrencyID(moneda);

        ItemType item = new ItemType();
        String descripcion = linea.descripcion();
        if (descripcion != null && !descripcion.isBlank()) {
            DescriptionType descripcionUbl = new DescriptionType();
            descripcionUbl.setValue(descripcion);
            item.setDescription(Collections.singletonList(descripcionUbl));
        }
        lineaUbl.setItem(item);

        DatosPrecioReferenciaFacturaUbl precioReferencia = linea.precioReferencia();
        if (precioReferencia != null) {
            lineaUbl.setPricingReference(crearPrecioReferencia(precioReferencia, moneda));
        }

        DatosDescuentoLineaFacturaUbl descuentoLinea = linea.descuentoLinea();
        if (descuentoLinea != null) {
            lineaUbl.addAllowanceCharge(crearDescuentoLinea(descuentoLinea, moneda));
        }

        BigDecimal precioUnitario = linea.precioUnitario();
        if (precioUnitario != null) {
            PriceType precio = new PriceType();
            precio.setPriceAmount(precioUnitario).setCurrencyID(moneda);
            lineaUbl.setPrice(precio);
        }

        return lineaUbl;
    }

    /**
     * Crea el precio de referencia para la línea.
     *
     * @param precioReferencia datos del precio de referencia
     * @param moneda código de moneda
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
     * @param moneda código de moneda
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
