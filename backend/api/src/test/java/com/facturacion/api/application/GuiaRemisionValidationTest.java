package com.facturacion.api.application;

import com.facturacion.api.web.dto.GenerarTramaRequest;
import com.facturacion.api.web.dto.SeccionesPayload;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GuiaRemisionValidationTest {

    @Test
    void guiaRemision_requiereMotivoTraslado() {
        var service = new RequestValidationService();

        Map<String, String> a = new HashMap<>();
        a.put("CodEmp", "123456789");
        a.put("Serie", "T001");
        a.put("Correlativo", "00000001");
        a.put("FchEmis", "2026-03-24");
        a.put("HorEmis", "10:11:12");
        a.put("TipoDocEmis", "6");
        a.put("RUTEmis", "20123456789");
        a.put("RznSocEmis", "EMPRESA SAC");
        a.put("CodAnxEmi", "01");
        a.put("TipoDocRec", "1");
        a.put("RUTRecep", "12345678");
        a.put("RznSocRecep", "CLIENTE");

        Map<String, String> g = Map.of();

        var req = new GenerarTramaRequest(
                "Guia de Remision",
                new SeccionesPayload(Map.of("A", a, "G", g), Map.of())
        );

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validar(req));
        assertTrue(ex.getDetails().stream().anyMatch(s -> s.startsWith("G.MotTras")));
    }

    @Test
    void guiaRemision_motivo08_requiereNroBultos() {
        var service = new RequestValidationService();

        Map<String, String> a = new HashMap<>();
        a.put("CodEmp", "123456789");
        a.put("Serie", "T001");
        a.put("Correlativo", "00000001");
        a.put("FchEmis", "2026-03-24");
        a.put("HorEmis", "10:11:12");
        a.put("TipoDocEmis", "6");
        a.put("RUTEmis", "20123456789");
        a.put("RznSocEmis", "EMPRESA SAC");
        a.put("CodAnxEmi", "01");
        a.put("TipoDocRec", "1");
        a.put("RUTRecep", "12345678");
        a.put("RznSocRecep", "CLIENTE");

        Map<String, String> g = Map.of("MotTras", "08");

        var req = new GenerarTramaRequest(
                "Guia de Remision",
                new SeccionesPayload(Map.of("A", a, "G", g), Map.of())
        );

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validar(req));
        assertTrue(ex.getDetails().stream().anyMatch(s -> s.startsWith("G.NroBultos")));
    }

    @Test
    void guiaRemision_tipoDocRef_invalido_falla() {
        var service = new RequestValidationService();

        Map<String, String> a = new HashMap<>();
        a.put("CodEmp", "123456789");
        a.put("Serie", "T001");
        a.put("Correlativo", "00000001");
        a.put("FchEmis", "2026-03-24");
        a.put("HorEmis", "10:11:12");
        a.put("TipoDocEmis", "6");
        a.put("RUTEmis", "20123456789");
        a.put("RznSocEmis", "EMPRESA SAC");
        a.put("CodAnxEmi", "01");
        a.put("TipoDocRec", "1");
        a.put("RUTRecep", "12345678");
        a.put("RznSocRecep", "CLIENTE");

        Map<String, String> g = Map.of("MotTras", "01");
        Map<String, String> d = Map.of("TipoDocRef", "99");

        var req = new GenerarTramaRequest(
                "Guia de Remision",
                new SeccionesPayload(Map.of("A", a, "G", g, "D", d), Map.of())
        );

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validar(req));
        assertTrue(ex.getDetails().stream().anyMatch(s -> s.startsWith("D.TipoDocRef")));
    }

    @Test
    void guiaRemision_motivo08_requiereG2CodPueAer() {
        var service = new RequestValidationService();

        Map<String, String> a = new HashMap<>();
        a.put("CodEmp", "123456789");
        a.put("Serie", "T001");
        a.put("Correlativo", "00000001");
        a.put("FchEmis", "2026-03-24");
        a.put("HorEmis", "10:11:12");
        a.put("TipoDocEmis", "6");
        a.put("RUTEmis", "20123456789");
        a.put("RznSocEmis", "EMPRESA SAC");
        a.put("CodAnxEmi", "01");
        a.put("TipoDocRec", "1");
        a.put("RUTRecep", "12345678");
        a.put("RznSocRecep", "CLIENTE");

        Map<String, String> g = Map.of("MotTras", "08", "NroBultos", "1");

        var req = new GenerarTramaRequest(
                "09",
                new SeccionesPayload(Map.of("A", a, "G", g), Map.of())
        );

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validar(req));
        assertTrue(ex.getDetails().stream().anyMatch(s -> s.startsWith("G2.CodPueAer")));
    }

    @Test
    void guiaRemision_modalidad02_requiereFecIniTras_y_conductor_si_no_M1L() {
        var service = new RequestValidationService();

        Map<String, String> a = new HashMap<>();
        a.put("CodEmp", "123456789");
        a.put("Serie", "T001");
        a.put("Correlativo", "00000001");
        a.put("FchEmis", "2026-03-24");
        a.put("HorEmis", "10:11:12");
        a.put("TipoDocEmis", "6");
        a.put("RUTEmis", "20123456789");
        a.put("RznSocEmis", "EMPRESA SAC");
        a.put("CodAnxEmi", "01");
        a.put("TipoDocRec", "1");
        a.put("RUTRecep", "12345678");
        a.put("RznSocRecep", "CLIENTE");

        Map<String, String> g = Map.of("MotTras", "01", "ModTras", "02");
        Map<String, String> g1 = Map.of();

        var req = new GenerarTramaRequest(
                "Guia de Remision",
                new SeccionesPayload(Map.of("A", a, "G", g, "G1", g1), Map.of("G4", List.of()))
        );

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validar(req));
        assertTrue(ex.getDetails().stream().anyMatch(s -> s.startsWith("G1.FecIniTras")));
        assertTrue(ex.getDetails().stream().anyMatch(s -> s.startsWith("G11")));
    }
}
