package com.facturacion.api.application;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NormativaServiceTest {

    private static final String BASE_URI =
            "https://www.sunat.gob.pe/legislacion/superin/2025/indices/";

    private NormativaService service;

    @BeforeEach
    void setUp() {
        service = new NormativaService();
    }

    @Test
    void parseDocument_extraeResolucionesCorrectamente() throws Exception {
        var doc = Jsoup.parse(cargarHtml("/normativa/indcor-2025-sample.html"), BASE_URI);

        var resoluciones = service.parseDocument(doc);

        assertEquals(5, resoluciones.size());
    }

    @Test
    void parseDocument_numeroYUrlPdf() throws Exception {
        var doc = Jsoup.parse(cargarHtml("/normativa/indcor-2025-sample.html"), BASE_URI);

        var resoluciones = service.parseDocument(doc);
        var primera = resoluciones.getFirst();

        assertEquals("000401-2025", primera.numero());
        assertTrue(primera.urlPdf().contains("000401-2025.pdf"));
        assertTrue(primera.urlPdf().startsWith("http"));
    }

    @Test
    void parseDocument_sumillaYFecha() throws Exception {
        var doc = Jsoup.parse(cargarHtml("/normativa/indcor-2025-sample.html"), BASE_URI);

        var resoluciones = service.parseDocument(doc);

        var conFecha = resoluciones.get(1);
        assertEquals("000392-2025", conFecha.numero());
        assertEquals("31/12/2025", conFecha.fecha());
        assertTrue(conFecha.sumilla().contains("Sistema Integrado"));

        var sinFecha = resoluciones.get(0);
        assertEquals("", sinFecha.fecha());
    }

    @Test
    void parseDocument_asignaCategoriaGeneral() throws Exception {
        var doc = Jsoup.parse(cargarHtml("/normativa/indcor-2025-sample.html"), BASE_URI);

        var resoluciones = service.parseDocument(doc);
        // 000401-2025 no contiene keywords de categoría específica
        assertEquals("General", resoluciones.get(0).categoria());
    }

    @Test
    void parseDocument_asignaFeDeErratas() throws Exception {
        var doc = Jsoup.parse(cargarHtml("/normativa/indcor-2025-sample.html"), BASE_URI);

        var resoluciones = service.parseDocument(doc);
        // 000355-2025 contiene "FE DE ERRATAS" en la sumilla
        assertEquals("FE DE ERRATAS", resoluciones.get(2).categoria());
    }

    @Test
    void parseDocument_staleInicialEsFalse() throws Exception {
        var doc = Jsoup.parse(cargarHtml("/normativa/indcor-2025-sample.html"), BASE_URI);

        var resoluciones = service.parseDocument(doc);

        assertFalse(resoluciones.getFirst().stale());
    }

    @Test
    void extraerNumero_extraeDesdeUrlAbsoluta() {
        assertEquals("000401-2025",
                NormativaService.extraerNumero(
                        "https://www.sunat.gob.pe/legislacion/superin/2025/000401-2025.pdf"));
    }

    @Test
    void extraerNumero_extraeDesdeUrlRelativa() {
        assertEquals("000401-2025",
                NormativaService.extraerNumero("../000401-2025.pdf"));
    }

    @Test
    void categorizar_detectaFeDeErratas() {
        assertEquals("FE DE ERRATAS",
                NormativaService.categorizar("FE DE ERRATAS Resolución de Superintendencia..."));
    }

    @Test
    void categorizar_defaultAGeneral() {
        assertEquals("General",
                NormativaService.categorizar("Aprueba el set de indicadores..."));
    }

    @Test
    void categorizar_facturacionElectronica() {
        assertEquals("Facturación Electrónica",
                NormativaService.categorizar("Modifica el CPE para factura electrónica"));
        assertEquals("Facturación Electrónica",
                NormativaService.categorizar("Guía de remisión - NOTA CRÉDITO electrónica"));
    }

    @Test
    void categorizar_guiasRemision() {
        assertEquals("Guías de Remisión",
                NormativaService.categorizar("Modifica la GUÍA DE REMISIÓN remitente"));
    }

    @Test
    void categorizar_catalogos() {
        assertEquals("Catálogos",
                NormativaService.categorizar("Actualiza el ANEXO 8 Catálogo de códigos"));
    }

    @Test
    void categorizar_tributos() {
        assertEquals("Tributos",
                NormativaService.categorizar("Modifica el IGV para el sector construcción"));
    }

    @Test
    void obtenerResolucion_filtraPorNumero() throws Exception {
        var doc = Jsoup.parse(cargarHtml("/normativa/indcor-2025-sample.html"), BASE_URI);
        var todas = service.parseDocument(doc);

        var encontrada = todas.stream()
                .filter(r -> r.numero().equals("000392-2025"))
                .findFirst();

        assertTrue(encontrada.isPresent());
        assertEquals("31/12/2025", encontrada.get().fecha());
    }

    @Test
    void parseDocument_extraeUrlAnexo() throws Exception {
        var doc = Jsoup.parse(cargarHtml("/normativa/indcor-2025-sample.html"), BASE_URI);

        var resoluciones = service.parseDocument(doc);
        // 000048-2026 tiene anexo
        var conAnexo = resoluciones.stream()
                .filter(r -> r.numero().equals("000048-2026"))
                .findFirst().orElseThrow();

        assertNotNull(conAnexo.urlAnexo());
        assertTrue(conAnexo.urlAnexo().contains("anexo-000048-2026.pdf"));
        // Sumilla no debe contener "Anexo" ni el link
        assertFalse(conAnexo.sumilla().contains("Anexo"));
    }

    @Test
    void parseDocument_sinAnexo_esNull() throws Exception {
        var doc = Jsoup.parse(cargarHtml("/normativa/indcor-2025-sample.html"), BASE_URI);

        var resoluciones = service.parseDocument(doc);

        assertNull(resoluciones.get(0).urlAnexo());
    }

    @Test
    void parseDocument_ignoraHeaderRow() throws Exception {
        var doc = Jsoup.parse(cargarHtml("/normativa/indcor-2025-sample.html"), BASE_URI);

        var resoluciones = service.parseDocument(doc);

        // La fila del header (Resolución | Sumilla | Publicado) no tiene link PDF
        assertTrue(resoluciones.stream().noneMatch(r -> r.numero().equals("Resolución")));
    }

    private String cargarHtml(String resource) throws IOException, URISyntaxException {
        URL url = getClass().getResource(resource);
        assertNotNull(url, "Resource not found: " + resource);
        return Files.readString(Path.of(url.toURI()));
    }
}
