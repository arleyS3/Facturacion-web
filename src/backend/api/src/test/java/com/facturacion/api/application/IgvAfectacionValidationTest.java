package com.facturacion.api.application;

import com.facturacion.api.web.dto.GenerarTramaRequest;
import com.facturacion.api.web.dto.SeccionesPayload;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IgvAfectacionValidationTest {

    @Test
    void exonerado_noPermiteIgvNiTasa() {
        var service = new RequestValidationService();
        var req = new GenerarTramaRequest("01", new SeccionesPayload(
                Map.of("A", seccionABase()),
                Map.of("B", List.of(Map.of(
                        "NroLinDet", "1",
                        "QtyItem", "1",
                        "UnmdItem", "NIU",
                        "NmbItem", "ITEM",
                        "MontoItem", "10.00",
                        "CodigoTipoIgv", "9998",
                        "TasaIgv", "18",
                        "ImpuestoIgv", "1.80"
                )))
        ));

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validar(req));
        assertTrue(ex.getDetails().stream().anyMatch(s -> s.contains("CodigoTipoIgv=9998")));
    }

    @Test
    void inafecto_noPermiteIgvNiTasa() {
        var service = new RequestValidationService();
        var req = new GenerarTramaRequest("01", new SeccionesPayload(
                Map.of("A", seccionABase()),
                Map.of("B", List.of(Map.of(
                        "NroLinDet", "1",
                        "QtyItem", "1",
                        "UnmdItem", "NIU",
                        "NmbItem", "ITEM",
                        "MontoItem", "10.00",
                        "CodigoTipoIgv", "9995",
                        "TasaIgv", "18",
                        "ImpuestoIgv", "1.80"
                )))
        ));

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validar(req));
        assertTrue(ex.getDetails().stream().anyMatch(s -> s.contains("CodigoTipoIgv=9995")));
    }

    @Test
    void a2_requiereCodigo9998_siHayExonerado() {
        var service = new RequestValidationService();

        Map<String, String> a = seccionABase();
        a.put("MntExo", "10.00");
        a.put("MntTotal", "10.00");

        var req = new GenerarTramaRequest("01", new SeccionesPayload(
                Map.of("A", a),
                Map.of(
                        "B", List.of(Map.of(
                                "NroLinDet", "1",
                                "QtyItem", "1",
                                "UnmdItem", "NIU",
                                "NmbItem", "ITEM",
                                "MontoItem", "10.00",
                                "CodigoTipoIgv", "9998",
                                "TasaIgv", "0",
                                "ImpuestoIgv", "0.00"
                        )),
                        "A2", List.of(Map.of(
                                "CodigoImpuesto", "1000",
                                "MontoImpuesto", "0.00",
                                "TasaImpuesto", "18",
                                "MontoImpuestoBase", "0.00"
                        ))
                )
        ));

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validar(req));
        assertTrue(ex.getDetails().stream().anyMatch(s -> s.contains("Falta registro 9998")));
    }

    @Test
    void gravado_conA2_1000_pasa() {
        var service = new RequestValidationService();
        Map<String, String> a = seccionABase();
        a.put("MntNeto", "100.00");
        a.put("MntTotalIgv", "18.00");
        a.put("MntTotal", "118.00");

        var req = new GenerarTramaRequest("01", new SeccionesPayload(
                Map.of("A", a),
                Map.of(
                        "B", List.of(Map.of(
                                "NroLinDet", "1",
                                "QtyItem", "1",
                                "UnmdItem", "NIU",
                                "NmbItem", "ITEM",
                                "PrcItemSinIgv", "100.00",
                                "MontoItem", "100.00",
                                "CodigoTipoIgv", "1000",
                                "TasaIgv", "18",
                                "ImpuestoIgv", "18.00",
                                "PrcItem", "118.00"
                        )),
                        "A2", List.of(Map.of(
                                "CodigoImpuesto", "1000",
                                "MontoImpuesto", "18.00",
                                "TasaImpuesto", "18.00",
                                "MontoImpuestoBase", "100.00"
                        ))
                )
        ));

        assertDoesNotThrow(() -> service.validar(req));
    }

    @Test
    void mixto_gravadoYExonerado_requiereA2_1000_y_9998_y_cuadraBase() {
        var service = new RequestValidationService();

        Map<String, String> a = seccionABase();
        a.put("MntNeto", "100.00");
        a.put("MntExo", "50.00");
        a.put("MntTotalIgv", "18.00");
        a.put("MntTotal", "168.00");

        var req = new GenerarTramaRequest("01", new SeccionesPayload(
                Map.of("A", a),
                Map.of(
                        "B", List.of(
                                Map.of(
                                        "NroLinDet", "1",
                                        "QtyItem", "1",
                                        "UnmdItem", "NIU",
                                        "NmbItem", "GRAVADO",
                                        "MontoItem", "100.00",
                                        "CodigoTipoIgv", "1000",
                                        "TasaIgv", "18",
                                        "ImpuestoIgv", "18.00"
                                ),
                                Map.of(
                                        "NroLinDet", "2",
                                        "QtyItem", "1",
                                        "UnmdItem", "NIU",
                                        "NmbItem", "EXONERADO",
                                        "MontoItem", "50.00",
                                        "CodigoTipoIgv", "9998",
                                        "TasaIgv", "0",
                                        "ImpuestoIgv", "0.00"
                                )
                        ),
                        "A2", List.of(
                                Map.of(
                                        "CodigoImpuesto", "1000",
                                        "MontoImpuesto", "18.00",
                                        "TasaImpuesto", "18.00",
                                        "MontoImpuestoBase", "100.00"
                                ),
                                Map.of(
                                        "CodigoImpuesto", "9998",
                                        "MontoImpuesto", "0.00",
                                        "TasaImpuesto", "0",
                                        "MontoImpuestoBase", "50.00"
                                )
                        )
                )
        ));

        assertDoesNotThrow(() -> service.validar(req));
    }

    @Test
    void gratuitas_conMntTotGrat_noPermiteIgv() {
        var service = new RequestValidationService();
        Map<String, String> a = seccionABase();
        a.put("MntTotGrat", "10.00");
        a.put("MntTotalIgv", "0.00");
        a.put("MntTotal", "0.00");

        var req = new GenerarTramaRequest("01", new SeccionesPayload(
                Map.of("A", a),
                Map.of("B", List.of(Map.of(
                        "NroLinDet", "1",
                        "QtyItem", "1",
                        "UnmdItem", "NIU",
                        "NmbItem", "GRATUITO",
                        "MontoItem", "10.00",
                        "CodigoTipoIgv", "9998",
                        "TasaIgv", "0",
                        "ImpuestoIgv", "0.00"
                )))
        ));

                try {
                        service.validar(req);
                } catch (ValidationException ex) {
                        throw new AssertionError("No debería fallar. Details=" + ex.getDetails(), ex);
                }
    }

    private static Map<String, String> seccionABase() {
        Map<String, String> a = new HashMap<>();
        a.put("CODI_EMPR", "1");
        a.put("TipoDTE", "01");
        a.put("Serie", "F001");
        a.put("Correlativo", "00000001");
        a.put("FchEmis", "2026-03-24");
        a.put("HoraEmision", "10:11:12");
        a.put("TipoRucEmis", "6");
        a.put("RUTEmis", "20123456789");
        a.put("RznSocEmis", "EMPRESA SAC");
        a.put("CodigoLocalAnexo", "01");
        a.put("TipoRutReceptor", "1");
        a.put("RUTRecep", "12345678");
        a.put("RznSocRecep", "CLIENTE");
        return a;
    }
}
