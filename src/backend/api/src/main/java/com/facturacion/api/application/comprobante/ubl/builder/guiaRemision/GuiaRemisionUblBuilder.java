package com.facturacion.api.application.comprobante.ubl.builder.guiaRemision;

import com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision.GuiaRemisionLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision.GuiaRemisionUblData;
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
 */
@Component
public class GuiaRemisionUblBuilder {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String VERSION_UBL = "2.1";
    private static final String VERSION_ESTRUCTURA = "2.0";

    public String construirXml(GuiaRemisionUblData data) throws Exception {
        DespatchAdviceType guia = new DespatchAdviceType();
        
        // 1. Encabezado principal
        guia.setUBLVersionID(VERSION_UBL);
        guia.setCustomizationID(VERSION_ESTRUCTURA);
        guia.setID(data.serie() + "-" + data.correlativo());
        
        if (data.fechaEmision() != null) {
            guia.setIssueDate(LocalDate.parse(data.fechaEmision(), FECHA_FMT));
        }
        
        // Código de tipo de documento (09)
        DespatchAdviceTypeCodeType typeCode = new DespatchAdviceTypeCodeType();
        typeCode.setValue(data.tipoDocumento());
        typeCode.setListAgencyName("PE:SUNAT");
        typeCode.setListName("SUNAT:Identificador de Tipo de Documento");
        typeCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01");
        guia.setDespatchAdviceTypeCode(typeCode);

        // 2. Entidades (Remitente y Destinatario)
        guia.setDespatchSupplierParty(crearRemitente(data));
        guia.setDeliveryCustomerParty(crearDestinatario(data));
        
        // 3. Datos de Traslado (Shipment)
        guia.setShipment(crearShipment(data));
        
        // 4. Detalle de los bienes
        if (data.lineas() != null) {
            for (GuiaRemisionLineaUblData linea : data.lineas()) {
                guia.addDespatchLine(crearLinea(linea));
            }
        }
        
        // 5. Generar XML
        StringWriter writer = new StringWriter();
        UBL21Marshaller.despatchAdvice()
            .setFormattedOutput(true)
            .write(guia, writer);
        
        return writer.toString();
    }

    private SupplierPartyType crearRemitente(GuiaRemisionUblData data) {
        SupplierPartyType supplier = new SupplierPartyType();
        PartyType party = new PartyType();
        
        if (data.emisorNroDocumento() != null) {
            PartyIdentificationType id = new PartyIdentificationType();
            IDType docId = new IDType();
            docId.setSchemeID(data.emisorTipoDocumento() != null ? data.emisorTipoDocumento() : "6");
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

    private CustomerPartyType crearDestinatario(GuiaRemisionUblData data) {
        CustomerPartyType customer = new CustomerPartyType();
        PartyType party = new PartyType();
        
        if (data.destinatarioNroDocumento() != null) {
            PartyIdentificationType id = new PartyIdentificationType();
            IDType docId = new IDType();
            docId.setSchemeID(data.destinatarioTipoDocumento() != null ? data.destinatarioTipoDocumento() : "6");
            docId.setValue(data.destinatarioNroDocumento());
            id.setID(docId);
            party.addPartyIdentification(id);
        }

        if (data.destinatarioRazonSocial() != null) {
            PartyLegalEntityType legalEntity = new PartyLegalEntityType();
            legalEntity.setRegistrationName(data.destinatarioRazonSocial());
            party.addPartyLegalEntity(legalEntity);
        }
        
        customer.setParty(party);
        return customer;
    }

    private ShipmentType crearShipment(GuiaRemisionUblData data) {
        ShipmentType shipment = new ShipmentType();
        shipment.setID("SUNAT_Envio");
        
        // Motivo de traslado (Catálogo 20)
        if (data.motivoTraslado() != null) {
            HandlingCodeType handlingCode = new HandlingCodeType();
            handlingCode.setValue(data.motivoTraslado());
            shipment.setHandlingCode(handlingCode);
        }

        // Peso Bruto Total
        if (data.pesoBruto() != null) {
            GrossWeightMeasureType peso = new GrossWeightMeasureType();
            peso.setValue(data.pesoBruto());
            peso.setUnitCode(data.unidadMedidaPeso() != null ? data.unidadMedidaPeso() : "KGM");
            shipment.setGrossWeightMeasure(peso);
        }

        ShipmentStageType stage = new ShipmentStageType();
        
        // Modalidad de Traslado (Catálogo 18: 01=Público, 02=Privado)
        if (data.modalidadTraslado() != null) {
            TransportModeCodeType transportMode = new TransportModeCodeType();
            transportMode.setValue(data.modalidadTraslado());
            stage.setTransportModeCode(transportMode);
            
            // Lógica SUNAT: Si es Público (01) requiere Transportista
            if ("01".equals(data.modalidadTraslado()) && data.transportistaNroDocumento() != null) {
                PartyType carrierParty = new PartyType();
                PartyIdentificationType carrierId = new PartyIdentificationType();
                IDType id = new IDType();
                id.setSchemeID(data.transportistaTipoDocumento() != null ? data.transportistaTipoDocumento() : "6");
                id.setValue(data.transportistaNroDocumento());
                carrierId.setID(id);
                carrierParty.addPartyIdentification(carrierId);
                
                if (data.transportistaRazonSocial() != null) {
                    PartyLegalEntityType ple = new PartyLegalEntityType();
                    ple.setRegistrationName(data.transportistaRazonSocial());
                    carrierParty.addPartyLegalEntity(ple);
                }
                stage.addCarrierParty(carrierParty);
            }
            // Lógica SUNAT: Si es Privado (02) requiere Conductor
            else if ("02".equals(data.modalidadTraslado()) && data.conductorNroDocumento() != null) {
                PersonType driver = new PersonType();
                IDType id = new IDType();
                id.setSchemeID(data.conductorTipoDocumento() != null ? data.conductorTipoDocumento() : "1"); // 1=DNI
                id.setValue(data.conductorNroDocumento());
                driver.setID(id);
                stage.addDriverPerson(driver);
            }
        }
        
        // Fecha de inicio de traslado
        if(data.fechaTraslado() != null) {
            PeriodType period = new PeriodType();
            period.setStartDate(LocalDate.parse(data.fechaTraslado(), FECHA_FMT));
            stage.setTransitPeriod(period);
        }
        shipment.addShipmentStage(stage);

        // Lógica SUNAT: Vehículo (solo en transporte privado 02)
        if ("02".equals(data.modalidadTraslado()) && data.transportistaPlacaVehiculo() != null) {
            TransportHandlingUnitType thu = new TransportHandlingUnitType();
            TransportEquipmentType equipment = new TransportEquipmentType();
            equipment.setID(data.transportistaPlacaVehiculo());
            thu.addTransportEquipment(equipment);
            shipment.addTransportHandlingUnit(thu);
        }

        // Punto de Llegada
        DeliveryType delivery = new DeliveryType();
        AddressType addressLlegada = new AddressType();
        if (data.puntoLlegadaUbigeo() != null) addressLlegada.setID(data.puntoLlegadaUbigeo());
        if (data.puntoLlegadaDireccion() != null) {
            AddressLineType lineLlegada = new AddressLineType();
            lineLlegada.setLine(data.puntoLlegadaDireccion());
            addressLlegada.addAddressLine(lineLlegada);
        }
        delivery.setDeliveryAddress(addressLlegada);
        shipment.setDelivery(delivery);
        
        // Punto de Partida
        AddressType addressPartida = new AddressType();
        if (data.puntoPartidaUbigeo() != null) addressPartida.setID(data.puntoPartidaUbigeo());
        if (data.puntoPartidaDireccion() != null) {
            AddressLineType linePartida = new AddressLineType();
            linePartida.setLine(data.puntoPartidaDireccion());
            addressPartida.addAddressLine(linePartida);
        }
        shipment.setOriginAddress(addressPartida);

        return shipment;
    }

    private DespatchLineType crearLinea(GuiaRemisionLineaUblData linea) {
        DespatchLineType line = new DespatchLineType();
        line.setID(String.valueOf(linea.numero() != null ? linea.numero() : 1));
        
        if (linea.cantidad() != null) {
            DeliveredQuantityType qty = new DeliveredQuantityType();
            qty.setValue(linea.cantidad());
            qty.setUnitCode(linea.unidadMedida() != null ? linea.unidadMedida() : "NIU");
            line.setDeliveredQuantity(qty);
        }
        
        ItemType item = new ItemType();
        if (linea.descripcion() != null) {
            DescriptionType desc = new DescriptionType();
            desc.setValue(linea.descripcion());
            item.setDescription(Collections.singletonList(desc));
        }
        
        if (linea.codigoProducto() != null) {
            ItemIdentificationType itemId = new ItemIdentificationType();
            IDType id = new IDType();
            id.setValue(linea.codigoProducto());
            itemId.setID(id);
            item.setSellersItemIdentification(itemId);
        }
        
        line.setItem(item);
        return line;
    }
}
