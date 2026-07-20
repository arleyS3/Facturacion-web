package com.facturacion.api.application.comprobante.mapper;

import com.facturacion.api.application.TipoDocumentoMapper;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import com.facturacion.api.application.comprobante.modelo.DocumentoRelacionadoCanonico;
import com.facturacion.api.application.comprobante.modelo.LeyendaCanonico;
import com.facturacion.api.application.comprobante.modelo.ParteTrasladoCanonico;
import com.facturacion.api.web.dto.GenerarTramaRequest;
import com.facturacion.api.web.dto.SeccionesPayload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapper de secciones de entrada a modelo canónico de comprobante.
 */
@Component
public class SeccionesToCanonicoMapper {

    /**
     * Mapea la solicitud a un comprobante canónico.
     *
     * @param request solicitud con secciones
     * @return comprobante canónico
     */
    public ComprobanteCanonico map(GenerarTramaRequest request) {
        SeccionesPayload secciones = request.secciones();
        Map<String, Map<String, String>> campos = secciones != null && secciones.campos() != null
                ? secciones.campos()
                : Map.of();

        Map<String, String> a = campos.getOrDefault("A", Map.of());

        return new ComprobanteCanonico(
                TipoDocumentoMapper.toCodigoSunat(request.tipoDocumento()),
                a.getOrDefault("Serie", "") + "-" + a.getOrDefault("Correlativo", ""),
                a.getOrDefault("FchEmis", ""),
                a.getOrDefault("HorEmis", "10:00:00"),
                a.getOrDefault("FecVencimiento", null),
                a.getOrDefault("TipOperacion", "0101"),
                a.getOrDefault("CodMoneda", "PEN"),
                a.getOrDefault("RUTEmis", ""),
                a.getOrDefault("RznSocEmisor", ""),
                a.getOrDefault("NomComercial", null),
                a.getOrDefault("DirEmisor", null),
                a.getOrDefault("Urbanizacion", null),
                a.getOrDefault("Ubigeo", null),
                a.getOrDefault("Departament", null),
                a.getOrDefault("Provincia", null),
                a.getOrDefault("Distrito", null),
                a.getOrDefault("CodDomicilioFiscal", "0001"),
                a.getOrDefault("NumDocReceptor", ""),
                a.getOrDefault("RznSocReceptor", null),
                a.getOrDefault("DirReceptor", null),
                a.getOrDefault("UbigeoReceptor", null),
                mapDetalles(secciones),
                mapRelacionado(campos),
                mapTraslado(secciones),
                List.of(),
                null, // descuentosGlobales — no se mapea desde trama antigua
                null, // anticipos — no se mapea desde trama antigua
                null, // documentosAdicionales — no se mapea desde trama antigua
                null, // guiaRemision — no se mapea desde trama antigua
                null  // firmar - por defecto null = true (se firma si hay certificado)
        );
    }

    /**
     * Mapea la sección de detalle a líneas canónicas.
     *
     * @param secciones secciones de entrada
     * @return lista de detalles
     */
    private List<DetalleCanonico> mapDetalles(SeccionesPayload secciones) {
        if (secciones == null || secciones.listas() == null) {
            return List.of();
        }

        List<Map<String, String>> items = secciones.listas().getOrDefault("B", List.of());
        List<DetalleCanonico> detalles = new ArrayList<>();

        for (Map<String, String> item : items) {
            if (item == null)
                continue;
            detalles.add(new DetalleCanonico(
                    item.getOrDefault("CodItem", null), // código de producto (opcional)
                    item.getOrDefault("NmbItem", ""),
                    decimal(item.get("QtyItem")),
                    decimal(item.get("VlrCodItem")),
                    decimal(item.get("MntIgvItem")),
                    item.getOrDefault("CodigoTipoIgv", ""),
                    item.getOrDefault("UnidadMedida", "NIU"), // unidad de medida
                    null, // iscMonto
                    null)); // iscTipoSistema
        }

        return detalles;
    }

    /**
     * Mapea la sección de documento relacionado.
     *
     * @param campos mapa de campos por sección
     * @return documento relacionado o null
     */
    private DocumentoRelacionadoCanonico mapRelacionado(Map<String, Map<String, String>> campos) {
        Map<String, String> d = campos.getOrDefault("D", Map.of());
        if (d.isEmpty())
            return null;
        
        // Combinar serie y correlativo para numeroDocumento
        String serie = d.getOrDefault("SerieDocAfectado", "");
        String correlativo = d.getOrDefault("CorrelativoDocAfectado", "");
        String numeroDocumento = serie.isEmpty() ? correlativo : serie + "-" + correlativo;
        
        return new DocumentoRelacionadoCanonico(
                d.getOrDefault("TipDocAfectado", ""),
                numeroDocumento,
                d.getOrDefault("CodMotivo", ""),
                d.getOrDefault("DesMotivo", ""));
    }

    /**
     * Mapea la sección de traslado.
     *
     * @param secciones payload con campos y listas
     * @return parte de traslado o null
     */
    private ParteTrasladoCanonico mapTraslado(SeccionesPayload secciones) {
        if (secciones == null || secciones.campos() == null)
            return null;

        Map<String, Map<String, String>> campos = secciones.campos();
        Map<String, String> g = campos.getOrDefault("G", Map.of());
        if (g.isEmpty())
            return null;

        Map<String, String> g1 = campos.getOrDefault("G1", Map.of());
        Map<String, String> g2 = campos.getOrDefault("G2", Map.of());
        Map<String, String> d = campos.getOrDefault("D", Map.of());

        Map<String, List<Map<String, String>>> listas = secciones.listas() != null ? secciones.listas() : Map.of();
        List<Map<String, String>> listaG11 = listas.getOrDefault("G11", List.of());
        Map<String, String> driverItem = !listaG11.isEmpty() ? listaG11.get(0) : Map.of();

        List<Map<String, String>> listaG3 = listas.getOrDefault("G3", List.of());
        Map<String, String> containerItem = !listaG3.isEmpty() ? listaG3.get(0) : Map.of();

        List<Map<String, String>> listaG4 = listas.getOrDefault("G4", List.of());
        List<String> indicadores = new ArrayList<>();
        for (Map<String, String> ind : listaG4) {
            String val = ind.get("IndicadorTraslado");
            if (val != null && !val.isBlank()) {
                indicadores.add(val);
            }
        }

        List<Map<String, String>> listaD = listas.getOrDefault("D", List.of());
        Map<String, String> refItem = !listaD.isEmpty() ? listaD.get(0) : (d.isEmpty() ? Map.of() : d);

        String docRefId = null;
        if (!refItem.isEmpty()) {
            String s = refItem.getOrDefault("SerieRef", "");
            String f = refItem.getOrDefault("FolioRef", "");
            docRefId = s.isBlank() ? f : s + "-" + f;
        }

        String conductorNroDoc = g.getOrDefault("NroDocConductor", driverItem.getOrDefault("ConductorNroDocId", ""));
        String conductorTipoDoc = g.getOrDefault("TipoDocConductor", driverItem.getOrDefault("ConductorTipoDocId", ""));
        String conductorNombres = driverItem.getOrDefault("NombConductor", null);
        String conductorApellidos = driverItem.getOrDefault("ApellConductor", null);
        String conductorLicencia = driverItem.getOrDefault("NumLicenciaCond", null);

        String transportistaNroDoc = g.getOrDefault("NroDocTransportista", g1.getOrDefault("RucTranspor", ""));
        String transportistaRazonSocial = g.getOrDefault("RznSocTransportista", g1.getOrDefault("RazoTrans", ""));
        String transportistaTipoDoc = g.getOrDefault("TipoDocTransportista", g1.getOrDefault("TipoRucTrans", ""));

        String fechaTraslado = g.getOrDefault("FecTraslado", g1.getOrDefault("FecIniTras", ""));

        return new ParteTrasladoCanonico(
                g.getOrDefault("MotTras", g.getOrDefault("TipoTraslado", "")), // tipo_traslado
                g.getOrDefault("ModTras", g.getOrDefault("ModalidadTraslado", "")), // modalidad_traslado
                g.getOrDefault("PuntoPartida", ""), // punto_partida
                g.getOrDefault("PuntoLlegada", ""), // punto_llegada
                g.getOrDefault("DirParUbiGeo", g.getOrDefault("UbigeoPtoPartida", "")), // punto_partida_ubigeo
                g.getOrDefault("DirParDireccion", g.getOrDefault("DireccionPtoPartida", "")), // punto_partida_direccion
                g.getOrDefault("UrbanizacionPtoPartida", ""), // punto_partida_urbanizacion
                g.getOrDefault("DirLlegUbiGeo", g.getOrDefault("UbigeoPtoLlegada", "")), // punto_llegada_ubigeo
                g.getOrDefault("DirLlegDireccion", g.getOrDefault("DireccionPtoLlegada", "")), // punto_llegada_direccion
                g.getOrDefault("UrbanizacionPtoLlegada", ""), // punto_llegada_urbanizacion
                transportistaNroDoc,
                transportistaRazonSocial,
                transportistaTipoDoc,
                g.getOrDefault("PlacaVehicPrinc", g.getOrDefault("PlacaVehiculo", "")), // placa_vehiculo
                g.getOrDefault("MarcaVehiculo", ""), // marca_vehiculo
                conductorNroDoc,
                conductorTipoDoc,
                conductorNombres,
                conductorApellidos,
                conductorLicencia,
                decimal(g.getOrDefault("TotalPesoTraslado", g.getOrDefault("PesoBrutoTotal", "0"))), // peso_bruto_total
                g.getOrDefault("UnmdTotalPeso", g.getOrDefault("UndPesoTotal", "KGM")), // und_peso_total
                Integer.parseInt(g.getOrDefault("NroBultos", "0")), // numero_bultos
                fechaTraslado,
                g2.getOrDefault("CodPueAer", null), // puerto_codigo
                g2.getOrDefault("NmbPueAer", null), // puerto_nombre
                containerItem.getOrDefault("IdContenedor", null), // contenedor_id
                containerItem.getOrDefault("NumPrecinto", null), // precinto_id
                docRefId, // doc_ref_transporte_id
                refItem.getOrDefault("TpoDocRef", null), // doc_ref_transporte_tipo
                refItem.getOrDefault("RucEmisDocRef", null), // doc_ref_transporte_emisor
                indicadores
        );
    }

    /**
     * Convierte un valor en {@code BigDecimal} seguro.
     *
     * @param value valor original
     * @return valor numérico o cero si es inválido
     */
    private BigDecimal decimal(String value) {
        if (value == null || value.isBlank())
            return BigDecimal.ZERO;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }
}
