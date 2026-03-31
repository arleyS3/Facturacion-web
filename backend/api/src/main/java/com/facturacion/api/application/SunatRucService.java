package com.facturacion.api.application;

import com.facturacion.api.web.config.SunatApiProperties;
import com.facturacion.api.web.dto.RucResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SunatRucService {

    private final RestClient sunatRestClient;
    private final SunatApiProperties properties;

    public SunatRucService(RestClient sunatRestClient, SunatApiProperties properties) {
        this.sunatRestClient = sunatRestClient;
        this.properties = properties;
    }

    public RucResponse consultarPorRuc(String ruc) {
        if (properties.getToken() == null || properties.getToken().isBlank()) {
            throw new IllegalStateException("No se configuró SUNAT_API_TOKEN");
        }

        return sunatRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/ruc").queryParam("numero", ruc).build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken())
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return response.bodyTo(RucResponse.class);
                    }
                    return null;
                });
    }
}
