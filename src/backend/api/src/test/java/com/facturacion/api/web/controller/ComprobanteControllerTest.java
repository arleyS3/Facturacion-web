package com.facturacion.api.web.controller;

import com.facturacion.api.application.comprobante.dto.GenerarXmlResult;
import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.signature.XmlSignatureService;
import com.facturacion.api.application.comprobante.ubl.strategy.UblDocumentoStrategy;
import com.facturacion.api.security.JwtService;
import com.facturacion.api.security.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link ComprobanteController}.
 * <p>
 * Verifies that the POST {@code /api/v1/comprobantes/generar-xml} endpoint
 * returns {@code validationErrors} in the response body, using MockMvc with
 * mocked strategies.
 * </p>
 */
@WebMvcTest(ComprobanteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ComprobanteControllerTest.TestStrategyConfig.class)
class ComprobanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private XmlSignatureService xmlSignatureService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    /**
     * The strategy bean injected into the controller context.
     * Configured via {@link TestStrategyConfig} with a default answer that
     * returns {@code "01"} for {@code codigoSunat()}. Individual tests
     * override {@code generarXml} behavior as needed.
     */
    @Autowired
    private UblDocumentoStrategy facturaStrategy;

    /**
     * Minimal valid request body for a Factura (01).
     * All {@code @NotBlank} / {@code @NotEmpty} constraints are satisfied.
     * Uses snake_case field names matching the {@code @JsonProperty} annotations.
     */
    private static final String FACTURA_REQUEST_JSON = """
            {
                "tipo_documento": "01",
                "numero": "F001-1",
                "fecha_emision": "2026-05-01",
                "moneda": "PEN",
                "emisor_ruc": "20123456789",
                "emisor_razonSocial": "K&G ASOCIADOS S.A.C.",
                "receptor_documento": "20456789001",
                "detalles": [
                    {
                        "descripcion": "Producto de prueba",
                        "cantidad": 1.00,
                        "valor_unitario": 100.00,
                        "igv": 18.00,
                        "codigo_tipoIgv": "10"
                    }
                ]
            }
            """;

    /**
     * Test configuration providing the strategy bean with a default answer
     * that returns {@code "01"} for {@code codigoSunat()} at context-init time.
     */
    @org.springframework.boot.test.context.TestConfiguration
    static class TestStrategyConfig {
        @Bean
        UblDocumentoStrategy facturaStrategy() {
            return mock(UblDocumentoStrategy.class, invocation -> {
                if ("codigoSunat".equals(invocation.getMethod().getName())) {
                    return "01";
                }
                // Default: return a valid GenerarXmlResult so the controller
                // works even before per-test when() setup
                if ("generarXml".equals(invocation.getMethod().getName())) {
                    return new GenerarXmlResult("<Invoice>stub</Invoice>", List.of());
                }
                return null;
            });
        }
    }

    @Test
    void generarXml_returnsValidationErrorsInResponse_whenStrategyReturnsErrors() throws Exception {
        when(facturaStrategy.generarXml(any())).thenReturn(
                new GenerarXmlResult("<Invoice>test</Invoice>",
                        List.of("cvc-complex-type.2.4.a: Element ID missing")));
        when(xmlSignatureService.trySignXmlResult(any(), any())).thenReturn(
                new XmlSignatureService.ResultadoFirmaXml("<Invoice>test</Invoice>", true, "OK"));

        mockMvc.perform(post("/api/v1/comprobantes/generar-xml")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FACTURA_REQUEST_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.xml").value("<Invoice>test</Invoice>"))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[0]").value("cvc-complex-type.2.4.a: Element ID missing"));
    }

    @Test
    void generarXml_returnsEmptyValidationErrors_whenXmlIsValid() throws Exception {
        when(facturaStrategy.generarXml(any())).thenReturn(
                new GenerarXmlResult("<Invoice>valid</Invoice>", List.of()));
        when(xmlSignatureService.trySignXmlResult(any(), any())).thenReturn(
                new XmlSignatureService.ResultadoFirmaXml("<Invoice>valid</Invoice>", true, "OK"));

        mockMvc.perform(post("/api/v1/comprobantes/generar-xml")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FACTURA_REQUEST_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors").isEmpty());
    }

    @Test
    void generarXml_returns400_whenTipoDocumentoNotSupported() throws Exception {
        mockMvc.perform(post("/api/v1/comprobantes/generar-xml")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FACTURA_REQUEST_JSON.replace("\"01\"", "\"99\"")))
                .andExpect(status().isBadRequest());
    }
}
