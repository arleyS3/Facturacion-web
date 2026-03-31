package com.facturacion.api.application;

import com.facturacion.api.web.dto.GenerarTramaRequest;
import com.facturacion.api.web.dto.SeccionesPayload;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FacturaBoletaExamplesTest {

    private final TramaImportService importService = new TramaImportService();
    private final RequestValidationService validationService = new RequestValidationService();
    private final TramaAssembler tramaAssembler = new TramaAssembler();

    @Test
    void ejemploFactura_conDescuento_importaYValida() throws Exception {
        ImportarTramaResponseWrap wrap = importar("api/Ejemplos/F01F004-61_descuento.txt");
        GenerarTramaRequest req = new GenerarTramaRequest(wrap.tipoDocumento, wrap.payload);

        try {
            validationService.validar(req);
        } catch (ValidationException ex) {
            throw new AssertionError("No debería fallar validación para ejemplo con descuento. Details=" + ex.getDetails(), ex);
        }
    }

    @Test
    void ejemploFactura_exportacionRespetaIndiceNroLinDet10() throws Exception {
        ImportarTramaResponseWrap wrap = importar("api/Ejemplos/20373697720-03-B001-00001287.txt");

        String trama = tramaAssembler.generar(new GenerarTramaRequest(wrap.tipoDocumento, wrap.payload));
        // El ejemplo usa NroLinDet 10; al exportar debe mantenerse ese idx
        assertTrue(trama.contains("B;NroLinDet;10;10"), "Debe mantener idx=10 en B.NroLinDet");
        assertTrue(trama.contains("B;QtyItem;10;"), "Debe mantener idx=10 en B.QtyItem");
    }

    @Test
    void b1SinNroLinDet_fallaValidacion() {
        Map<String, String> a = new HashMap<>();
        a.put("CODI_EMPR", "1");
        a.put("TipoDTE", "01");
        a.put("Serie", "F001");
        a.put("Correlativo", "00000001");
        a.put("FchEmis", "2025-01-01");
        a.put("HoraEmision", "10:00:00");
        a.put("TipoRucEmis", "6");
        a.put("RUTEmis", "20123456789");
        a.put("RznSocEmis", "EMPRESA");
        a.put("CodigoLocalAnexo", "0000");
        a.put("TipoRutReceptor", "6");
        a.put("RUTRecep", "20123456789");
        a.put("RznSocRecep", "CLIENTE");

        SeccionesPayload payload = new SeccionesPayload(
            Map.of("A", a),
                Map.of(
                        "B", List.of(Map.of(
                                "NroLinDet", "1",
                                "QtyItem", "1",
                                "UnmdItem", "NIU",
                                "NmbItem", "ITEM",
                                "MontoItem", "10.00",
                                "ImpuestoIgv", "0.00"
                        )),
                        "B1", List.of(Map.of(
                                "IndCargoDescuento", "0",
                                "CodigoCargoDescuento", "00",
                                "FactorCargoDescuento", "0.5",
                                "MontoCargoDescuento", "1.00",
                                "MBaseCargoDescuento", "2.00"
                        ))
                )
        );

        GenerarTramaRequest req = new GenerarTramaRequest("01", payload);
        try {
            validationService.validar(req);
            throw new AssertionError("Se esperaba ValidationException");
        } catch (ValidationException ex) {
            assertTrue(ex.getDetails().stream().anyMatch(s -> s.startsWith("B1.NroLinDet")));
        }
    }

    private static ImportarTramaResponseWrap importar(String workspaceRelativePath) throws IOException {
        // tests corren con user.dir = <repo>/api
        Path p = Path.of(System.getProperty("user.dir")).resolve(workspaceRelativePath.replaceFirst("^api/", ""));
        byte[] bytes = Files.readAllBytes(p);

        // algunos ejemplos vienen con encoding no-UTF8 (por ejemplo cp1252/latin1)
        String txt;
        try {
            txt = new String(bytes, StandardCharsets.UTF_8);
            // si no es UTF-8 válido, puede “reventar” aquí o dejar caracteres reemplazo; por eso hacemos fallback
        } catch (Exception ex) {
            txt = null;
        }

        byte[] toParse;
        if (txt != null && txt.contains("\uFFFD")) {
            // símbolo de reemplazo -> probablemente encoding distinto
            toParse = new String(bytes, StandardCharsets.ISO_8859_1).getBytes(StandardCharsets.UTF_8);
        } else if (txt == null) {
            toParse = new String(bytes, StandardCharsets.ISO_8859_1).getBytes(StandardCharsets.UTF_8);
        } else {
            toParse = bytes;
        }

        var resp = new TramaImportService().importar(new ByteArrayInputStream(toParse));
        return new ImportarTramaResponseWrap(resp.tipoDocumento(), resp.secciones());
    }

    private record ImportarTramaResponseWrap(String tipoDocumento, SeccionesPayload payload) {
    }
}
