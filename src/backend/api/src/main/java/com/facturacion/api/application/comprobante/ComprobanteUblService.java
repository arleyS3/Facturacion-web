package com.facturacion.api.application.comprobante;

import com.facturacion.api.application.TipoDocumentoMapper;
import com.facturacion.api.application.comprobante.ubl.mapper.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.strategy.UblDocumentoStrategy;
import com.facturacion.api.web.dto.GenerarTramaRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio de generación de XML UBL 2.1.
 * Despacha a la {@link UblDocumentoStrategy} correspondiente según el tipo de documento.
 */
@Service
public class ComprobanteUblService {

    private final Map<String, UblDocumentoStrategy> strategies;

    public ComprobanteUblService(List<UblDocumentoStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(UblDocumentoStrategy::codigoTipoDocumento, Function.identity()));
    }

    public String generarXml(GenerarTramaRequest request) {
        String codigo = TipoDocumentoMapper.toCodigoSunat(request.tipoDocumento());
        UblDocumentoStrategy strategy = strategies.get(codigo);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No hay estrategia UBL implementada para tipoDocumento: " + request.tipoDocumento());
        }

        var secciones = request.secciones();
        var camposA  = secciones != null && secciones.campos() != null ? secciones.campos().get("A")  : Map.<String,String>of();
        var camposA2 = secciones != null && secciones.campos() != null ? secciones.campos().get("A2") : Map.<String,String>of();
        var lineasB  = secciones != null && secciones.listas() != null ? secciones.listas().get("B")  : List.<Map<String,String>>of();

        ComprobanteCanonico canonico = new ComprobanteCanonico(codigo, camposA, camposA2, lineasB);
        return strategy.generarXml(canonico);
    }
}
