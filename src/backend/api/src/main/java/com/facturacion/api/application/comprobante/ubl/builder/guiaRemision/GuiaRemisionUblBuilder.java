package com.facturacion.api.application.comprobante.ubl.builder.guiaRemision;

import com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision.GuiaRemisionUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision.GuiaRemisionLineaUblData;
import com.helger.ubl21.UBL21Marshaller;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.despatchadvice_21.DespatchAdviceType;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * Constructor de XML UBL 2.1 para Guía de Remisión Electrónica.
 * Genera un documento XML válido según especificación UBL 2.1 y normativa SUNAT Perú.
 * 
 * <p>Esta clase construye objetos DespatchAdviceType de UBL 2.1 utilizando la librería ph-ubl.
 * Soporta los campos requeridos por SUNAT para guías de remisión electrónicas.</p>
 * 
 * @author Facturación API
 * @version 1.0
 * @see <a href="https://github.com/phax/ph-ubl">ph-ubl</a>
 * @see <a href="https://cpe.sunat.gob.pe">SUNAT CPE</a>
 */
@Component
public class GuiaRemisionUblBuilder {

    /** Formato de fecha para parsing de fechas. */
    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Construye el XML de guía de remisión UBL 2.1.
     *
     * @param data datos de la guía en formato canónico
     * @return contenido XML generado
     * @throws Exception si hay error al generar el XML
     */
    public String construirXml(GuiaRemisionUblData data) throws Exception {
        DespatchAdviceType guia = new DespatchAdviceType();
        
        guia.setID(data.serie() + "-" + data.correlativo());
        
        LocalDate fecha = LocalDate.parse(data.fechaEmision(), FECHA_FMT);
        guia.setIssueDate(fecha);
        
        guia.setDespatchAdviceTypeCode("09");
        
        guia.setDespatchSupplierParty(crearRemitente(data));
        
        if (data.destinatarioNroDocumento() != null) {
            guia.setDeliveryCustomerParty(crearDestinatario(data));
        }
        
        guia.setShipment(crearShipment(data));
        
        if (data.lineas() != null) {
            for (GuiaRemisionLineaUblData linea : data.lineas()) {
                guia.addDespatchLine(crearLinea(linea));
            }
        }
        
        StringWriter writer = new StringWriter();
        UBL21Marshaller.despatchAdvice()
            .setFormattedOutput(true)
            .write(guia, writer);
        
        return writer.toString();
    }

    /**
     * Crea el remitente de la guía de remisión.
     *
     * @param data datos de la guía
     * @return datos del remitente
     */
    private SupplierPartyType crearRemitente(GuiaRemisionUblData data) {
        SupplierPartyType supplier = new SupplierPartyType();
        PartyType party = new PartyType();
        
        if (data.emisorRazonSocial() != null) {
            PartyNameType name = new PartyNameType();
            name.setName(data.emisorRazonSocial());
            party.addPartyName(name);
        }
        
        supplier.setParty(party);
        return supplier;
    }

    /**
     * Crea el destinatario de la guía de remisión.
     *
     * @param data datos de la guía
     * @return datos del destinatario
     */
    private CustomerPartyType crearDestinatario(GuiaRemisionUblData data) {
        CustomerPartyType customer = new CustomerPartyType();
        PartyType party = new PartyType();
        
        if (data.destinatarioRazonSocial() != null) {
            PartyLegalEntityType ple = new PartyLegalEntityType();
            ple.setRegistrationName(data.destinatarioRazonSocial());
            party.addPartyLegalEntity(ple);
        }
        
        customer.setParty(party);
        return customer;
    }

    /**
     * Crea el contenido del shipment (datos de traslado).
     *
     * @param data datos de la guía de remisión
     * @return datos de traslado
     */
    private ShipmentType crearShipment(GuiaRemisionUblData data) {
        ShipmentType shipment = new ShipmentType();
        
        if (data.pesoBruto() != null) {
            GrossWeightMeasureType peso = new GrossWeightMeasureType();
            peso.setValue(data.pesoBruto());
            peso.setUnitCode(data.unidadMedidaPeso() != null ? data.unidadMedidaPeso() : "KGM");
            shipment.setGrossWeightMeasure(peso);
        }
        
        return shipment;
    }

    /**
     * Crea una línea de detalle de la guía de remisión.
     *
     * @param linea datos de la línea
     * @return línea UBL
     */
    private DespatchLineType crearLinea(GuiaRemisionLineaUblData linea) {
        DespatchLineType line = new DespatchLineType();
        
        line.setID(String.valueOf(linea.numero() != null ? linea.numero() : 1));
        
        ItemType item = new ItemType();
        DescriptionType desc = new DescriptionType();
        desc.setValue(linea.descripcion());
        item.setDescription(Collections.singletonList(desc));
        line.setItem(item);
        
        return line;
    }
}
