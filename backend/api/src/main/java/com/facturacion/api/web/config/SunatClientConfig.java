package com.facturacion.api.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Configuración de cliente REST para consultas hacia el proveedor SUNAT.
 * <p>
 * Define un {@link org.springframework.web.client.RestClient} con la URL base y
 * headers por defecto (Accept: application/json).
 * </p>
 */
@Configuration
public class SunatClientConfig {

    @Bean
    public RestClient sunatRestClient(SunatApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
