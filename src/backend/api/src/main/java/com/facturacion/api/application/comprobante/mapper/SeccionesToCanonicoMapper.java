package com.facturacion.api.application.comprobante.mapper;

import com.facturacion.api.application.TipoDocumentoMapper;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import com.facturacion.api.application.comprobante.modelo.DocumentoRelacionadoCanonico;
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
                a.getOrDefault("CodMoneda", "PEN"),
                a.getOrDefault("RUTEmis", ""),
                a.getOrDefault("NumDocReceptor", ""),
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                mapDetalles(secciones),
                mapRelacionado(campos),
                mapTraslado(campos));
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
                    item.getOrDefault("CodigoTipoIgv", "")));
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
        return new DocumentoRelacionadoCanonico(
                d.getOrDefault("TipDocAfectado", ""),
                d.getOrDefault("SerieDocAfectado", ""),
                d.getOrDefault("CorrelativoDocAfectado", ""),
                d.getOrDefault("CodMotivo", ""),
                d.getOrDefault("DesMotivo", ""));
    }

    /**
     * Mapea la sección de traslado.
     *
     * @param campos mapa de campos por sección
     * @return parte de traslado o null
     */
    private ParteTrasladoCanonico mapTraslado(Map<String, Map<String, String>> campos) {
        Map<String, String> g = campos.getOrDefault("G", Map.of());
        if (g.isEmpty())
            return null;
        return new ParteTrasladoCanonico(
                g.getOrDefault("MotivoTraslado", ""),
                g.getOrDefault("ModalidadTraslado", ""),
                g.getOrDefault("PuntoPartida", ""),
                g.getOrDefault("PuntoLlegada", ""),
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null);
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
