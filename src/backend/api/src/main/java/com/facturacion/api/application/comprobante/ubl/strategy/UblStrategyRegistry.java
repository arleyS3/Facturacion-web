package com.facturacion.api.application.comprobante.ubl.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registro de estrategias UBL por código SUNAT.
 */
@Component
public class UblStrategyRegistry {

    private final Map<String, UblDocumentoStrategy> strategies;

    /**
     * Construye el registro con las estrategias disponibles.
     *
     * @param strategyList lista de estrategias
     */
    public UblStrategyRegistry(List<UblDocumentoStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(UblDocumentoStrategy::codigoSunat, Function.identity()));
    }

    /**
     * Obtiene la estrategia por código SUNAT.
     *
     * @param codigoSunat código SUNAT
     * @return estrategia registrada
     * @throws IllegalArgumentException si no existe estrategia
     */
    public UblDocumentoStrategy getByCodigo(String codigoSunat) {
        var strategy = strategies.get(codigoSunat);
        if (strategy == null) {
            throw new IllegalArgumentException("No existe estrategia UBL para tipo: " + codigoSunat);
        }
        return strategy;
    }
}
