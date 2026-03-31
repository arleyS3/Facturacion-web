package com.facturacion.api.application;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UbigeoService {

    private volatile UbigeoIndex cache;

    public List<String> listarDepartamentos() {
        return index().departamentosOrdenados;
    }

    public List<String> listarProvincias(String departamento) {
        if (departamento == null || departamento.isBlank()) {
            throw new IllegalArgumentException("departamento es requerido");
        }
        return index().provinciasPorDepartamento
                .getOrDefault(normalize(departamento), List.of());
    }

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

    public Optional<UbigeoDistrito> buscarPorUbigeo(String ubigeo) {
        if (ubigeo == null || ubigeo.isBlank()) {
            throw new IllegalArgumentException("ubigeo es requerido");
        }
        return Optional.ofNullable(index().porCodigoUbigeo.get(ubigeo.trim()));
    }

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

    private UbigeoIndex loadFromClasspathExcel() {
        try {
            var resource = new ClassPathResource("BD/Ubigeo.xlsx");
            if (!resource.exists()) {
                throw new IllegalStateException("No se encontró el recurso BD/Ubigeo.xlsx en classpath");
            }
            try (InputStream is = resource.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
                Sheet sheet = workbook.getSheetAt(0);

                boolean primeraFila = true;
                Map<String, String> depOriginal = new LinkedHashMap<>();
                Map<String, Map<String, String>> provOriginal = new LinkedHashMap<>();
                Map<String, List<UbigeoDistrito>> distPorDepProv = new LinkedHashMap<>();
                Map<String, UbigeoDistrito> porUbigeo = new LinkedHashMap<>();

                for (Row row : sheet) {
                    if (primeraFila) {
                        primeraFila = false;
                        continue;
                    }

                    String ubigeo = cellString(row, 0);
                    String departamento = cellString(row, 1);
                    String provincia = cellString(row, 2);
                    String distrito = cellString(row, 3);

                    if (departamento.isBlank() || provincia.isBlank() || distrito.isBlank() || ubigeo.isBlank()) {
                        continue;
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

                List<String> deps = depOriginal.values().stream()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();

                Map<String, List<String>> provsPorDep = new LinkedHashMap<>();
                for (var entry : provOriginal.entrySet()) {
                    List<String> provs = entry.getValue().values().stream()
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .toList();
                    provsPorDep.put(entry.getKey(), provs);
                }

                Map<String, List<UbigeoDistrito>> distritosOrdenados = new LinkedHashMap<>();
                for (var entry : distPorDepProv.entrySet()) {
                    List<UbigeoDistrito> list = entry.getValue().stream()
                            .sorted(Comparator.comparing(UbigeoDistrito::distrito, String.CASE_INSENSITIVE_ORDER))
                            .collect(Collectors.toList());
                    distritosOrdenados.put(entry.getKey(), list);
                }

                return new UbigeoIndex(deps, provsPorDep, distritosOrdenados, porUbigeo);
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar ubigeo desde BD/Ubigeo.xlsx", e);
        }
    }

    private static String cellString(Row row, int idx) {
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

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private record UbigeoIndex(
            List<String> departamentosOrdenados,
            Map<String, List<String>> provinciasPorDepartamento,
            Map<String, List<UbigeoDistrito>> distritosPorDepartamentoProvincia,
            Map<String, UbigeoDistrito> porCodigoUbigeo
    ) {}

    public record UbigeoDistrito(String ubigeo, String distrito, String provincia, String departamento) {}
}
