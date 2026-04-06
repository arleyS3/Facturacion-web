package com.facturacion.api.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración para el cliente de consulta SUNAT.
 * <p>
 * Se cargan desde propiedades con prefijo <code>sunat.api</code> y permiten
 * configurar la URL base y el token de acceso al servicio externo.
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "sunat.api")
public class SunatApiProperties {

    private String baseUrl = "https://api.apis.net.pe";
    private String token = "";

    /**
     * URL base del proveedor de consulta SUNAT.
     *
     * @return base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Token de acceso para el proveedor de consulta (opcional).
     *
     * @return token en texto
     */
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
