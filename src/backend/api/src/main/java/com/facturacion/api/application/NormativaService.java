package com.facturacion.api.application;

import com.facturacion.api.web.dto.ResolucionSunatResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

/**
 * Servicio que consulta y cachea las Resoluciones de Superintendencia publicadas
 * en el portal SUNAT. Los listados se obtienen del índice correlativo anual y se
 * cachean con expiración de 6 horas. Un chequeo asíncrono detecta
 * desactualización comparando el número de resoluciones contra la página actual.
 */
@Service
public class NormativaService {

    private static final String SUNAT_URL_TEMPLATE =
            "https://www.sunat.gob.pe/legislacion/superin/%d/indices/indcor.htm";

    private final Cache<Integer, List<ResolucionSunatResponse>> cache;
    private final Cache<Integer, Boolean> staleGuard;

    // ponytail: Set de años con datos stale; persiste hasta próxima actualización exitosa
    private final Set<Integer> staleYears = ConcurrentHashMap.newKeySet();

    public NormativaService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(6))
                .build();
        this.staleGuard = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(6))
                .build();
    }

    /**
     * Retorna todas las resoluciones de un año. Sirve desde caché si está
     * disponible y dispara una verificación asíncrona de vigencia.
     * Si SUNAT no responde y hay datos en caché, los devuelve con stale=true.
     */
    public List<ResolucionSunatResponse> obtenerResoluciones(int anio) {
        var cached = cache.getIfPresent(anio);
        if (cached != null) {
            triggerStaleCheck(anio, cached);
            return marcarStale(cached, staleYears.contains(anio));
        }
        try {
            return fetchAndCache(anio);
        } catch (RuntimeException e) {
            // ponytail: si SUNAT falló en el primer fetch devolvemos vacío
            return List.of();
        }
    }

    private List<ResolucionSunatResponse> marcarStale(
            List<ResolucionSunatResponse> lista, boolean stale) {
        if (!stale) return lista;
        return lista.stream()
                .map(r -> new ResolucionSunatResponse(
                        r.numero(), r.sumilla(), r.fecha(), r.urlPdf(), r.categoria(), true))
                .toList();
    }

    /**
     * Busca una resolución específica por número dentro del listado del año.
     */
    public Optional<ResolucionSunatResponse> obtenerResolucion(int anio, String numero) {
        return obtenerResoluciones(anio).stream()
                .filter(r -> r.numero().equals(numero))
                .findFirst();
    }

    /**
     * Dispara una única verificación asíncrona por cada ciclo de vida de la
     * entrada en {@link #staleGuard}. Evita múltiples conexiones simultáneas
     * para el mismo año.
     */
    private void triggerStaleCheck(int anio, List<ResolucionSunatResponse> actual) {
        staleGuard.get(anio, k -> {
            CompletableFuture.runAsync(() -> checkStale(anio, actual));
            return true;
        });
    }

    /**
     * Verifica si el número de resoluciones en SUNAT cambió. Si detecta una
     * diferencia, invalida la entrada en {@link #cache} para que el próximo
     * acceso la refresque.
     */
    private void checkStale(int anio, List<ResolucionSunatResponse> actual) {
        try {
            String url = String.format(SUNAT_URL_TEMPLATE, anio);
            Document doc = Jsoup.connect(url).timeout(10_000).get();
            long freshCount = doc.select("tr").stream()
                    .filter(this::esFilaResolucion)
                    .count();

            if (freshCount != actual.size()) {
                cache.invalidate(anio);
            }
            // Si llegamos acá, SUNAT respondió bien -> limpiamos stale
            staleYears.remove(anio);
        } catch (IOException e) {
            // ponytail: SUNAT no respondió, marcamos stale
            staleYears.add(anio);
        }
    }

    private boolean esFilaResolucion(Element tr) {
        Element link = tr.selectFirst("td.tlin a[href]");
        return link != null && link.attr("href").endsWith(".pdf");
    }

    private List<ResolucionSunatResponse> fetchAndCache(int anio) {
        try {
            var resoluciones = fetchResoluciones(anio);
            cache.put(anio, resoluciones);
            return resoluciones;
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al obtener resoluciones SUNAT para " + anio, e);
        }
    }

    /**
     * Nivel paquete para testabilidad. Descarga y parsea el índice correlativo
     * de resoluciones de SUNAT para el año indicado.
     */
    List<ResolucionSunatResponse> fetchResoluciones(int anio) throws IOException {
        String url = String.format(SUNAT_URL_TEMPLATE, anio);
        Document doc = Jsoup.connect(url).timeout(15_000).get();
        return parseDocument(doc);
    }

    /**
     * Nivel paquete para testabilidad. Parsea el HTML de la página de SUNAT y
     * extrae cada resolución como {@link ResolucionSunatResponse}.
     */
    List<ResolucionSunatResponse> parseDocument(Document doc) {
        List<ResolucionSunatResponse> results = new ArrayList<>();

        for (Element row : doc.select("table tr")) {
            var cells = row.select("td.tlin");
            if (cells.size() < 2) continue;

            Element link = cells.get(0).selectFirst("a[href]");
            if (link == null) continue;

            String relHref = link.attr("href");
            if (relHref == null || !relHref.endsWith(".pdf")) continue;

            String absHref = link.attr("abs:href");
            String urlPdf = absHref.isEmpty() ? relHref : absHref;

            String numero = extraerNumero(relHref);

            String sumilla = cells.get(1).text()
                    .replaceAll("\\s+", " ")
                    .trim();

            String fecha = cells.size() > 2
                    ? cells.get(2).text().replaceAll("\\s+", " ").trim()
                    : "";

            results.add(new ResolucionSunatResponse(
                    numero, sumilla, fecha, urlPdf, categorizar(sumilla), false));
        }

        return results;
    }

    /**
     * Extrae el identificador de la resolución desde la URL del PDF.
     * Ej: "https://.../000401-2025.pdf" → "000401-2025"
     */
    static String extraerNumero(String href) {
        int barra = href.lastIndexOf('/');
        int punto = href.lastIndexOf('.');
        return (barra >= 0 && punto > barra)
                ? href.substring(barra + 1, punto)
                : href;
    }

    // Keywords por categoría (orden de prioridad: más específico primero)
    private static final List<CategoriaRule> CATEGORIA_RULES = List.of(
        new CategoriaRule("FE DE ERRATAS", "FE DE ERRATAS"),
        new CategoriaRule("Facturación Electrónica",
            "COMPROBANTE", "CPE", "FACTURA", "BOLETA", "NOTA CRÉDITO", "NOTA DÉBITO"),
        new CategoriaRule("Guías de Remisión",
            "GUÍA DE REMISIÓN", "GUIA DE REMISION", "GR REMITENTE", "GR TRANSPORTISTA"),
        new CategoriaRule("Catálogos",
            "CATÁLOGO", "CATALOGO", "ANEXO"),
        new CategoriaRule("Tributos",
            "ISC", "IGV", "IMPUESTO", "TRIBUTO", "RENTA")
    );

    /**
     * Asigna una categoría según el contenido de la sumilla.
     */
    static String categorizar(String sumilla) {
        String upper = sumilla.toUpperCase();
        for (var rule : CATEGORIA_RULES) {
            if (rule.keywords().stream().anyMatch(upper::contains)) {
                return rule.categoria();
            }
        }
        return "General";
    }

    private record CategoriaRule(String categoria, List<String> keywords) {
        CategoriaRule(String categoria, String... keywords) {
            this(categoria, List.of(keywords));
        }
    }
}
