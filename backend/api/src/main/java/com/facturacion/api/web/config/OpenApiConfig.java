package com.facturacion.api.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(
            @Value("${spring.application.name:api}") String applicationName,
            @Value("${app.api.version:1.0}") String apiVersion,
            @Value("${app.api.server-url:http://localhost:8080}") String serverUrl
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " - API")
                        .version(apiVersion)
                        .description("API para generar, descargar e importar tramas TXT por secciones (Factura/Boleta/NC/ND/GR).")
                )
                .servers(List.of(new Server().url(serverUrl).description("Default")));
    }
}
