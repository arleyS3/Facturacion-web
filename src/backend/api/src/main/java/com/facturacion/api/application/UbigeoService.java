package com.facturacion.api.application;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar información de ubigeo (departamentos, provincias, distritos).
 * Los datos se cargan desde un archivo Excel ubicado en el classpath.
 */
@Service
public class UbigeoService {

    /**
     * Cache volátil para almacenar el índice de ubigeo.
     */
    private volatile UbigeoIndex cache;

    /**
     * Lista todos los departamentos ordenados alfabéticamente.
     *
     * @return Lista de nombres de departamentos.
     */
    public List<String> listarDepartamentos() {
        return index().departamentosOrdenados;
    }

    /**
     * Lista todas las provincias correspondientes a un departamento específico.
     *
     * @param departamento Nombre del departamento.
     * @return Lista de nombres de provincias, o una lista vacía si el departamento no existe.
     * @throws IllegalArgumentException Si el parámetro departamento es nulo o está vacío.
     */
    public List<String> listarProvincias(String departamento) {
        if (departamento == null || departamento.isBlank()) {
            throw new IllegalArgumentException("departamento es requerido");
        }
        return index().provinciasPorDepartamento
                .getOrDefault(normalize(departamento), List.of());
    }

    /**
     * Lista todos los distritos correspondientes a un departamento y provincia específicos.
     *
     * @param departamento Nombre del departamento.
     * @param provincia Nombre de la provincia.
     * @return Lista de distritos, o una lista vacía si los datos no existen.
     * @throws IllegalArgumentException Si el departamento o la provincia son nulos o están vacíos.
     */
    public List<UbigeoDistrito> listarDistritos(String departamento, String provincia) {
        if (departamento == null || departamento.isBlank()) {
            throw new IllegalArgumentException("departamento es requerido");
        }
        if (provincia == null || provincia.isBlank()) {
            throw new IllegalArgumentException("provincia es requerida");
        }
        String key = normalize(departamento) + "|" + normalize(provincia);
        return index().distritosPorDepartamentoProvincia.getOrDefault(key, List.of());
    }

    /**
     * Busca un distrito utilizando su código de ubigeo.
     *
     * @param ubigeo Código de ubigeo.
     * @return Un Optional con el distrito correspondiente, o vacío si no se encuentra.
     * @throws IllegalArgumentException Si el ubigeo es nulo o está vacío.
     */
    public Optional<UbigeoDistrito> buscarPorUbigeo(String ubigeo) {
        if (ubigeo == null || ubigeo.isBlank()) {
            throw new IllegalArgumentException("ubigeo es requerido");
        }
        return Optional.ofNullable(index().porCodigoUbigeo.get(ubigeo.trim()));
    }

    /**
     * Obtiene el índice de datos, cargándolo desde el cache o desde el archivo Excel en el classpath.
     *
     * @return Índice de ubigeo cargado.
     */
    private UbigeoIndex index() {
        var local = cache;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (cache == null) {
                cache = loadFromClasspathExcel();
            }
            return cache;
        }
    }

    /**
     * Carga los datos de ubigeo desde un archivo Excel ubicado en el classpath.
     *
     * @return Índice de ubigeo construido desde el archivo Excel.
     * @throws IllegalStateException Si el archivo no se encuentra o no puede ser procesado.
     */
    private UbigeoIndex loadFromClasspathExcel() {
        try {
            var resource = new ClassPathResource("BD/Ubigeo.xlsx");
            ensureResourceExists(resource);
            try (InputStream is = resource.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
                Sheet sheet = workbook.getSheetAt(0);
                return buildIndexFromSheet(sheet);
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar ubigeo desde BD/Ubigeo.xlsx", e);
        }
    }

    /**
     * Valida que el recurso exista.
     */
    private static void ensureResourceExists(ClassPathResource resource) {
        if (!resource.exists()) {
            throw new IllegalStateException("No se encontró el recurso BD/Ubigeo.xlsx en classpath");
        }
    }

    /**
     * Construye el índice de ubigeo a partir de la hoja del workbook.
     */
    private UbigeoIndex buildIndexFromSheet(Sheet sheet) {
        Map<String, String> depOriginal = new LinkedHashMap<>();
        Map<String, Map<String, String>> provOriginal = new LinkedHashMap<>();
        Map<String, List<UbigeoDistrito>> distPorDepProv = new LinkedHashMap<>();
        Map<String, UbigeoDistrito> porUbigeo = new LinkedHashMap<>();

        boolean primeraFila = true;
        for (Row row : sheet) {
            if (primeraFila) {
                primeraFila = false; // saltar header
                continue;
            }
            processRow(row, depOriginal, provOriginal, distPorDepProv, porUbigeo);
        }

        List<String> deps = depOriginal.values().stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        Map<String, List<String>> provsPorDep = buildProvinciasMap(provOriginal);
        Map<String, List<UbigeoDistrito>> distritosOrdenados = buildDistritosMap(distPorDepProv);

        return new UbigeoIndex(deps, provsPorDep, distritosOrdenados, porUbigeo);
    }

    /**
     * Procesa una fila de la hoja y actualiza las estructuras auxiliares.
     */
    private void processRow(Row row,
                            Map<String, String> depOriginal,
                            Map<String, Map<String, String>> provOriginal,
                            Map<String, List<UbigeoDistrito>> distPorDepProv,
                            Map<String, UbigeoDistrito> porUbigeo) {
        String ubigeo = getCellString(row, 0);
        String departamento = getCellString(row, 1);
        String provincia = getCellString(row, 2);
        String distrito = getCellString(row, 3);

        if (departamento.isBlank() || provincia.isBlank() || distrito.isBlank() || ubigeo.isBlank()) {
            return;
        }

        String depKey = normalize(departamento);
        String provKey = normalize(provincia);

        depOriginal.putIfAbsent(depKey, departamento);
        provOriginal.computeIfAbsent(depKey, k -> new LinkedHashMap<>()).putIfAbsent(provKey, provincia);

        String depProvKey = depKey + "|" + provKey;
        UbigeoDistrito item = new UbigeoDistrito(ubigeo, distrito, provincia, departamento);

        distPorDepProv.computeIfAbsent(depProvKey, k -> new ArrayList<>()).add(item);
        porUbigeo.putIfAbsent(ubigeo, item);
    }

    /**
     * Construye el mapa de provincias por departamento y las ordena.
     */
    private Map<String, List<String>> buildProvinciasMap(Map<String, Map<String, String>> provOriginal) {
        Map<String, List<String>> provsPorDep = new LinkedHashMap<>();
        for (var entry : provOriginal.entrySet()) {
            List<String> provs = entry.getValue().values().stream()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            provsPorDep.put(entry.getKey(), provs);
        }
        return provsPorDep;
    }

    /**
     * Ordena los distritos por nombre dentro de cada clave departamento|provincia.
     */
    private Map<String, List<UbigeoDistrito>> buildDistritosMap(Map<String, List<UbigeoDistrito>> distPorDepProv) {
        Map<String, List<UbigeoDistrito>> distritosOrdenados = new LinkedHashMap<>();
        for (var entry : distPorDepProv.entrySet()) {
            List<UbigeoDistrito> list = entry.getValue().stream()
                    .sorted(Comparator.comparing(UbigeoDistrito::distrito, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
            distritosOrdenados.put(entry.getKey(), list);
        }
        return distritosOrdenados;
    }

    /**
     * Obtiene el valor de una celda como texto.
     *
     * @param row Fila que contiene la celda.
     * @param idx Índice de la celda en la fila.
     * @return Valor de la celda como texto, o una cadena vacía si la celda está vacía.
     */
    private static String getCellString(Row row, int idx) {
        var cell = row.getCell(idx);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    yield String.valueOf((long) numericValue);
                }
                yield String.valueOf(numericValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception ignored) {
                    try {
                        yield String.valueOf((long) cell.getNumericCellValue());
                    } catch (Exception ignored2) {
                        yield "";
                    }
                }
            }
            default -> "";
        };
    }

    /**
     * Normaliza una cadena eliminando espacios y convirtiéndola a minúsculas.
     *
     * @param s Cadena a normalizar.
     * @return Cadena normalizada.
     */
    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Clase interna para representar el índice de ubigeo, que contiene listas y mapas ordenados.
     *
     * @param departamentosOrdenados Lista de departamentos ordenados.
     * @param provinciasPorDepartamento Mapa de provincias por departamento.
     * @param distritosPorDepartamentoProvincia Mapa de distritos por departamento y provincia.
     * @param porCodigoUbigeo Mapa de distritos por código de ubigeo.
     */
    private record UbigeoIndex(
            List<String> departamentosOrdenados,
            Map<String, List<String>> provinciasPorDepartamento,
            Map<String, List<UbigeoDistrito>> distritosPorDepartamentoProvincia,
            Map<String, UbigeoDistrito> porCodigoUbigeo
    ) {}

    /**
     * Registro que representa un distrito con información de ubigeo.
     *
     * @param ubigeo Código de ubigeo.
     * @param distrito Nombre del distrito.
     * @param provincia Nombre de la provincia.
     * @param departamento Nombre del departamento.
     */
    public record UbigeoDistrito(String ubigeo, String distrito, String provincia, String departamento) {}
}
