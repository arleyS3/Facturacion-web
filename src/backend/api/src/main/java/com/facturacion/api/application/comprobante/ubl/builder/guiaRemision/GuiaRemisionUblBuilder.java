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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * Constructor de XML UBL 2.1 para Guía de Remisión Electrónica según la especificación de SUNAT.
 */
@Component
public class GuiaRemisionUblBuilder {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String VERSION_UBL = "2.1";
    private static final String VERSION_ESTRUCTURA = "2.0";

    public DespatchAdviceType buildDespatchAdvice(GuiaRemisionUblData data) {
        DespatchAdviceType guia = new DespatchAdviceType();
        
        // 1. Encabezado principal
        guia.setUBLVersionID(VERSION_UBL);
        guia.setCustomizationID(VERSION_ESTRUCTURA);
        guia.setID(data.serie() + "-" + data.correlativo());
        
        if (data.fechaEmision() != null && !data.fechaEmision().isBlank()) {
            guia.setIssueDate(LocalDate.parse(data.fechaEmision(), FECHA_FMT));
        }

        if (data.horaEmision() != null && !data.horaEmision().isBlank()) {
            try {
                guia.setIssueTime(LocalTime.parse(data.horaEmision(), HORA_FMT));
            } catch (Exception e) {
                guia.setIssueTime(LocalTime.now());
            }
        }
        
        // Código de tipo de documento (09)
        DespatchAdviceTypeCodeType typeCode = new DespatchAdviceTypeCodeType();
        typeCode.setValue(data.tipoDocumento() != null ? data.tipoDocumento() : "09");
        typeCode.setListAgencyName("PE:SUNAT");
        typeCode.setListName("Tipo de Documento");
        typeCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01");
        guia.setDespatchAdviceTypeCode(typeCode);

        // Documento relacionado al transporte / DAM (Catálogo 61)
        if (data.docRefTransporteId() != null && !data.docRefTransporteId().isBlank()) {
            DocumentReferenceType docRef = new DocumentReferenceType();
            IDType docId = new IDType();
            docId.setValue(data.docRefTransporteId());
            docRef.setID(docId);

            DocumentTypeCodeType docType = new DocumentTypeCodeType();
            docType.setValue(data.docRefTransporteTipo() != null ? data.docRefTransporteTipo() : "50");
            docType.setListAgencyName("PE:SUNAT");
            docType.setListName("Documento Relacionado al transporte");
            docType.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo61");
            docRef.setDocumentTypeCode(docType);

            if (data.docRefTransporteEmisor() != null && !data.docRefTransporteEmisor().isBlank()) {
                PartyType issuerParty = new PartyType();
                PartyIdentificationType partyId = new PartyIdentificationType();
                IDType partyDocId = new IDType();
                partyDocId.setSchemeID(data.docRefTransporteEmisor().length() == 11 ? "06" : "1");
                partyDocId.setSchemeName("Documento de Identidad");
                partyDocId.setSchemeAgencyName("PE:SUNAT");
                partyDocId.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
                partyDocId.setValue(data.docRefTransporteEmisor());
                partyId.setID(partyDocId);
                issuerParty.addPartyIdentification(partyId);
                docRef.setIssuerParty(issuerParty);
            }
            guia.addAdditionalDocumentReference(docRef);
        }

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

        return guia;
    }

    public String serializarGuiaRemision(DespatchAdviceType guia) throws Exception {
        StringWriter writer = new StringWriter();
        UBL21Marshaller.despatchAdvice()
            .setFormattedOutput(true)
            .write(guia, writer);
        
        return writer.toString();
    }

    public String construirXml(GuiaRemisionUblData data) throws Exception {
        DespatchAdviceType guia = buildDespatchAdvice(data);
        return serializarGuiaRemision(guia);
    }

    private SupplierPartyType crearRemitente(GuiaRemisionUblData data) {
        SupplierPartyType supplier = new SupplierPartyType();
        PartyType party = new PartyType();
        
        if (data.emisorNroDocumento() != null) {
            PartyIdentificationType id = new PartyIdentificationType();
            IDType docId = new IDType();
            docId.setSchemeID(data.emisorTipoDocumento() != null ? data.emisorTipoDocumento() : "6");
            docId.setSchemeName("Documento de Identidad");
            docId.setSchemeAgencyName("PE:SUNAT");
            docId.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
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
            docId.setSchemeName("Documento de Identidad");
            docId.setSchemeAgencyName("PE:SUNAT");
            docId.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
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
            handlingCode.setListAgencyName("PE:SUNAT");
            handlingCode.setListName("Motivo de traslado");
            handlingCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo20");
            shipment.setHandlingCode(handlingCode);
        }

        // Peso Bruto Total
        if (data.pesoBruto() != null) {
            GrossWeightMeasureType peso = new GrossWeightMeasureType();
            peso.setValue(data.pesoBruto());
            peso.setUnitCode(data.unidadMedidaPeso() != null ? data.unidadMedidaPeso() : "KGM");
            shipment.setGrossWeightMeasure(peso);
        }

        // Indicadores especiales de SUNAT
        if (data.indicadores() != null) {
            for (String ind : data.indicadores()) {
                if (ind != null && !ind.isBlank()) {
                    SpecialInstructionsType spec = new SpecialInstructionsType();
                    spec.setValue(ind);
                    shipment.addSpecialInstructions(spec);
                }
            }
        }

        ShipmentStageType stage = new ShipmentStageType();
        
        // Modalidad de Traslado (Catálogo 18: 01=Público, 02=Privado)
        if (data.modalidadTraslado() != null) {
            TransportModeCodeType transportMode = new TransportModeCodeType();
            transportMode.setValue(data.modalidadTraslado());
            transportMode.setListAgencyName("PE:SUNAT");
            transportMode.setListName("Modalidad de traslado");
            transportMode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo18");
            stage.setTransportModeCode(transportMode);
            
            // Lógica SUNAT: Si es Público (01) requiere Transportista
            if ("01".equals(data.modalidadTraslado()) && data.transportistaNroDocumento() != null) {
                PartyType carrierParty = new PartyType();
                PartyIdentificationType carrierId = new PartyIdentificationType();
                IDType id = new IDType();
                id.setSchemeID(data.transportistaTipoDocumento() != null ? data.transportistaTipoDocumento() : "6");
                id.setSchemeName("Documento de Identidad");
                id.setSchemeAgencyName("PE:SUNAT");
                id.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
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
                id.setSchemeName("Documento de Identidad");
                id.setSchemeAgencyName("PE:SUNAT");
                id.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06");
                id.setValue(data.conductorNroDocumento());
                driver.setID(id);

                if (data.conductorNombres() != null && !data.conductorNombres().isBlank()) {
                    FirstNameType fn = new FirstNameType();
                    fn.setValue(data.conductorNombres());
                    driver.setFirstName(fn);
                }
                if (data.conductorApellidos() != null && !data.conductorApellidos().isBlank()) {
                    FamilyNameType fn = new FamilyNameType();
                    fn.setValue(data.conductorApellidos());
                    driver.setFamilyName(fn);
                }

                DocumentReferenceType licRef = new DocumentReferenceType();
                IDType licId = new IDType();
                licId.setValue(data.conductorLicencia() != null && !data.conductorLicencia().isBlank()
                        ? data.conductorLicencia()
                        : "C" + data.conductorNroDocumento());
                licRef.setID(licId);
                driver.addIdentityDocumentReference(licRef);

                stage.addDriverPerson(driver);
            }
        }
        
        // Fecha de inicio de traslado
        if (data.fechaTraslado() != null && !data.fechaTraslado().isBlank()) {
            PeriodType period = new PeriodType();
            period.setStartDate(LocalDate.parse(data.fechaTraslado(), FECHA_FMT));
            stage.setTransitPeriod(period);
        }
        shipment.addShipmentStage(stage);

        // Estructura Delivery (Punto de Llegada y Despatch Address Punto de Partida)
        DeliveryType delivery = new DeliveryType();

        // Punto de Llegada
        AddressType addressLlegada = new AddressType();
        if (data.puntoLlegadaUbigeo() != null && !data.puntoLlegadaUbigeo().isBlank()) {
            IDType ubiId = new IDType();
            ubiId.setSchemeAgencyName("PE:INEI");
            ubiId.setSchemeName("Ubigeos");
            ubiId.setValue(data.puntoLlegadaUbigeo());
            addressLlegada.setID(ubiId);
        }
        AddressTypeCodeType typeCodeLlegada = new AddressTypeCodeType();
        typeCodeLlegada.setListAgencyName("PE:SUNAT");
        typeCodeLlegada.setListName("Establecimientos anexos");
        addressLlegada.setAddressTypeCode(typeCodeLlegada);

        if (data.puntoLlegadaDireccion() != null && !data.puntoLlegadaDireccion().isBlank()) {
            AddressLineType lineLlegada = new AddressLineType();
            lineLlegada.setLine(data.puntoLlegadaDireccion());
            addressLlegada.addAddressLine(lineLlegada);
        }
        delivery.setDeliveryAddress(addressLlegada);

        // Punto de Partida (cac:Delivery/cac:Despatch/cac:DespatchAddress)
        DespatchType despatch = new DespatchType();
        AddressType addressPartida = new AddressType();
        if (data.puntoPartidaUbigeo() != null && !data.puntoPartidaUbigeo().isBlank()) {
            IDType ubiId = new IDType();
            ubiId.setSchemeAgencyName("PE:INEI");
            ubiId.setSchemeName("Ubigeos");
            ubiId.setValue(data.puntoPartidaUbigeo());
            addressPartida.setID(ubiId);
        }
        AddressTypeCodeType typeCodePartida = new AddressTypeCodeType();
        typeCodePartida.setListAgencyName("PE:SUNAT");
        typeCodePartida.setListName("Establecimientos anexos");
        addressPartida.setAddressTypeCode(typeCodePartida);

        if (data.puntoPartidaDireccion() != null && !data.puntoPartidaDireccion().isBlank()) {
            AddressLineType linePartida = new AddressLineType();
            linePartida.setLine(data.puntoPartidaDireccion());
            addressPartida.addAddressLine(linePartida);
        }
        despatch.setDespatchAddress(addressPartida);
        delivery.setDespatch(despatch);

        shipment.setDelivery(delivery);

        // Contenedor / Bultos y Placa de Vehículo (cac:TransportHandlingUnit)
        TransportHandlingUnitType thu = new TransportHandlingUnitType();
        if ((data.contenedorId() != null && !data.contenedorId().isBlank()) || (data.precintoId() != null && !data.precintoId().isBlank())) {
            PackageType pkg = new PackageType();
            if (data.contenedorId() != null && !data.contenedorId().isBlank()) {
                IDType pkgId = new IDType();
                pkgId.setValue(data.contenedorId());
                pkg.setID(pkgId);
            }
            if (data.precintoId() != null && !data.precintoId().isBlank()) {
                TraceIDType traceId = new TraceIDType();
                traceId.setValue(data.precintoId());
                pkg.setTraceID(traceId);
            }
            thu.addPackage(pkg);
        }

        if ("02".equals(data.modalidadTraslado()) && data.transportistaPlacaVehiculo() != null && !data.transportistaPlacaVehiculo().isBlank()) {
            TransportEquipmentType equipment = new TransportEquipmentType();
            IDType eqId = new IDType();
            eqId.setValue(data.transportistaPlacaVehiculo());
            equipment.setID(eqId);
            thu.addTransportEquipment(equipment);
        }

        if (!thu.getPackage().isEmpty() || !thu.getTransportEquipment().isEmpty()) {
            shipment.addTransportHandlingUnit(thu);
        }

        // Puerto/Aeropuerto (Catálogo 63 - cac:FirstArrivalPortLocation)
        if (data.puertoCodigo() != null && !data.puertoCodigo().isBlank()) {
            oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.LocationType portLocation = new oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.LocationType();
            IDType portId = new IDType();
            portId.setSchemeAgencyName("PE:SUNAT");
            portId.setSchemeName("Ubigeos");
            portId.setSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo63");
            portId.setValue(data.puertoCodigo());
            portLocation.setID(portId);

            LocationTypeCodeType locTypeCode = new LocationTypeCodeType();
            locTypeCode.setValue("1");
            portLocation.setLocationTypeCode(locTypeCode);

            if (data.puertoNombre() != null && !data.puertoNombre().isBlank()) {
                NameType portName = new NameType();
                portName.setValue(data.puertoNombre());
                portLocation.setName(portName);
            }
            shipment.setFirstArrivalPortLocation(portLocation);
        }

        return shipment;
    }

    private DespatchLineType crearLinea(GuiaRemisionLineaUblData linea) {
        DespatchLineType line = new DespatchLineType();
        int numLinea = linea.numero() != null ? linea.numero() : 1;
        line.setID(String.valueOf(numLinea));
        
        if (linea.cantidad() != null) {
            DeliveredQuantityType qty = new DeliveredQuantityType();
            qty.setValue(linea.cantidad());
            qty.setUnitCode(linea.unidadMedida() != null ? linea.unidadMedida() : "NIU");
            qty.setUnitCodeListID("UN/ECE rec 20");
            qty.setUnitCodeListAgencyName("United Nations Economic Commission for Europe");
            line.setDeliveredQuantity(qty);
        }

        OrderLineReferenceType orderLineRef = new OrderLineReferenceType();
        LineIDType lineId = new LineIDType();
        lineId.setValue(String.valueOf(numLinea));
        orderLineRef.setLineID(lineId);
        line.addOrderLineReference(orderLineRef);
        
        ItemType item = new ItemType();
        if (linea.descripcion() != null) {
            DescriptionType desc = new DescriptionType();
            desc.setValue(linea.descripcion());
            item.setDescription(Collections.singletonList(desc));
        }
        
        if (linea.codigoProducto() != null && !linea.codigoProducto().isBlank()) {
            ItemIdentificationType itemId = new ItemIdentificationType();
            IDType id = new IDType();
            id.setValue(linea.codigoProducto());
            itemId.setID(id);
            item.setSellersItemIdentification(itemId);
        }

        if (linea.damNumero() != null && !linea.damNumero().isBlank()) {
            ItemPropertyType prop = new ItemPropertyType();
            NameType propName = new NameType();
            propName.setValue("Numero de declaracion aduanera (DAM)");
            prop.setName(propName);

            NameCodeType nameCode = new NameCodeType();
            nameCode.setValue("7021");
            nameCode.setListName("Propiedad del item");
            nameCode.setListAgencyName("PE:SUNAT");
            prop.setNameCode(nameCode);

            ValueType val = new ValueType();
            val.setValue(linea.damNumero());
            prop.setValue(val);

            item.addAdditionalItemProperty(prop);
        }
        
        line.setItem(item);
        return line;
    }
}
