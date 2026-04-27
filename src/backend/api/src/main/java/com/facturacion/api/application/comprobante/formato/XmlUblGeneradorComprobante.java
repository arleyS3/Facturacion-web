package com.facturacion.api.application.comprobante.formato;

import com.facturacion.api.application.TipoDocumentoMapper;
import com.facturacion.api.application.comprobante.FormatoSalidaComprobante;
import com.facturacion.api.application.comprobante.ResultadoComprobante;
import com.facturacion.api.application.comprobante.mapper.SeccionesToCanonicoMapper;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.io.UblXmlWriter;
import com.facturacion.api.application.comprobante.ubl.strategy.UblDocumentoStrategy;
import com.facturacion.api.application.comprobante.ubl.strategy.UblStrategyRegistry;
import com.facturacion.api.application.comprobante.ubl.validator.SunatCatalogValidator;
import com.facturacion.api.application.comprobante.ubl.validator.UblSchemaValidator;
import com.facturacion.api.web.dto.GenerarTramaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Generador de comprobantes UBL en formato XML.
 */
@Component
@RequiredArgsConstructor
public class XmlUblGeneradorComprobante implements GeneradorComprobante {

    private final SeccionesToCanonicoMapper canonicoMapper;
    private final SunatCatalogValidator sunatCatalogValidator;
    private final UblStrategyRegistry strategyRegistry;
    private final UblXmlWriter ublXmlWriter;
    private final UblSchemaValidator schemaValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    public FormatoSalidaComprobante formato() {
        return FormatoSalidaComprobante.XML;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultadoComprobante generar(GenerarTramaRequest request) throws Exception {
        ComprobanteCanonico canonico = canonicoMapper.map(request);
        sunatCatalogValidator.validar(canonico);

        String codigoSunat = TipoDocumentoMapper.toCodigoSunat(request.tipoDocumento());
        UblDocumentoStrategy strategy = strategyRegistry.getByCodigo(codigoSunat);

        String xmlRaw = strategy.generarXml(canonico);
        String xml = ublXmlWriter.normalize(xmlRaw);
        schemaValidator.validar(xml);

        String nombreArchivo = buildNombreArchivo(canonico);
        return ResultadoComprobante.xml(xml, nombreArchivo);
    }

    /**
     * Construye el nombre de archivo según datos canónicos.
     *
     * @param canonico comprobante canónico
     * @return nombre de archivo
     */
    private String buildNombreArchivo(ComprobanteCanonico canonico) {
        String ruc = safe(canonico.emisorRuc(), "sin-ruc");
        String tipo = safe(canonico.tipoDocumento(), "00");
        String numero = safe(canonico.numero(), "S000-00000000").replaceAll("[^A-Za-z0-9_-]", "-");
        return ruc + "-" + tipo + "-" + numero + ".xml";
    }

    /**
     * Normaliza un texto para usarlo como parte del nombre de archivo.
     *
     * @param value valor original
     * @param fallback valor por defecto
     * @return valor normalizado
     */
    private static String safe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim().replaceAll("[^A-Za-z0-9_-]", "-");
    }
}
