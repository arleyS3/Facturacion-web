package com.facturacion.api.application;

import com.facturacion.api.web.dto.GenerarTramaRequest;
import com.facturacion.api.web.dto.SeccionesPayload;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestValidationServiceTest {

    @Test
    void validaSeccionAComun_ok() {
        var service = new RequestValidationService();

        Map<String, String> a = new HashMap<>();
        a.put("CodEmp", "123456789");
        a.put("Serie", "F001");
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

        var req = new GenerarTramaRequest("Factura", new SeccionesPayload(
            Map.of("A", a),
            Map.of("B", List.of(Map.of(
                "NroLinDet", "1",
                "QtyItem", "1",
                "UnmdItem", "NIU",
                "NmbItem", "ITEM",
                "MontoItem", "10.00",
                "ImpuestoIgv", "0.00"
            )))
        ));
        assertDoesNotThrow(() -> service.validar(req));
    }

    @Test
    void validaSeccionAComun_falla_con_detalles() {
        var service = new RequestValidationService();

        Map<String, String> a = new HashMap<>();
        a.put("CodEmp", "");
        a.put("Serie", "B001");
        a.put("Correlativo", "1");
        a.put("FchEmis", "24/03/2026");
        a.put("HorEmis", "99:99:99");
        a.put("TipoDocEmis", "6");
        a.put("RUTEmis", "123");
        a.put("RznSocEmis", "");
        a.put("CodAnxEmi", "");
        a.put("TipoDocRec", "1");
        a.put("RUTRecep", "123");
        a.put("RznSocRecep", "");

        var req = new GenerarTramaRequest("Factura", new SeccionesPayload(Map.of("A", a), Map.of()));

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validar(req));
        assertEquals("Validación falló", ex.getMessage());
        assertTrue(ex.getDetails().size() >= 5);
        assertTrue(ex.getDetails().stream().anyMatch(s -> s.startsWith("A.Serie")));
    }

    @Test
    void validaSeccionB_consistenciaDesktopStyle_ok() {
        var service = new RequestValidationService();

        Map<String, String> a = new HashMap<>();
        a.put("CodEmp", "1");
        a.put("Serie", "F001");
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

        Map<String, String> b1 = new HashMap<>();
        b1.put("NroLinDet", "1");
        b1.put("QtyItem", "2");
        b1.put("UnmdItem", "NIU");
        b1.put("NmbItem", "ITEM");
        b1.put("PrcItemSinIgv", "100.00");
        b1.put("PrcItem", "118.00");
        b1.put("MontoItem", "200.00");
        b1.put("TasaIgv", "18");
        b1.put("ImpuestoIgv", "36.00");

        var req = new GenerarTramaRequest("Factura", new SeccionesPayload(
                Map.of("A", a),
                Map.of("B", List.of(b1))
        ));

        assertDoesNotThrow(() -> service.validar(req));
    }
}
