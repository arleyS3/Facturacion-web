package com.facturacion.api.application;

import com.facturacion.api.web.dto.SeccionesPayload;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para importar una trama TXT y convertirla a un payload por secciones.
 * <p>
 * El formato esperado es líneas separadas por salto de línea con campos
 * separados por `;`. Detecta secciones, campos y listas (índices) y construye
 * un {@link com.facturacion.api.web.dto.SeccionesPayload} con la estructura
 * resultante.
 * </p>
 */
@Service
public class TramaImportService {

    /**
     * Importa el contenido del stream que contiene la trama TXT y devuelve el
     * resultado con el tipo de documento detectado y el payload por secciones.
     *
     * @param inputStream flujo de datos de la trama (UTF-8)
     * @return resultado de la importación con payload y tipo de documento
     * @throws IOException si ocurre un error de lectura
     */
    public ImportResult importar(InputStream inputStream) throws IOException {
        Map<String, Map<String, String>> campos = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> listas = new LinkedHashMap<>();

        String tipoDte = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(";", -1);
                if (parts.length < 2) continue;

                String seccion = parts[0].trim();
                String campo = parts[1].trim();

                String idxRaw = parts.length >= 3 ? parts[2].trim() : "";
                String valor = parts.length >= 4 ? parts[3] : "";
                valor = valor != null ? valor.trim() : "";

                // Detectar tipo documento desde A;TipoDTE;;01 etc.
                if ("A".equals(seccion) && "TipoDTE".equalsIgnoreCase(campo)) {
                    tipoDte = valor;
                }

                boolean isLista = !idxRaw.isEmpty();
                if (!isLista) {
                    campos.computeIfAbsent(seccion, k -> new LinkedHashMap<>())
                            .put(campo, valor);
                    continue;
                }

                int idx;
                try {
                    idx = Integer.parseInt(idxRaw);
                } catch (NumberFormatException e) {
                    // si el índice no es numérico, lo tratamos como campo simple
                    campos.computeIfAbsent(seccion, k -> new LinkedHashMap<>())
                            .put(campo, valor);
                    continue;
                }

                List<Map<String, String>> list = listas.computeIfAbsent(seccion, k -> new ArrayList<>());
                while (list.size() < idx) {
                    list.add(new HashMap<>());
                }
                list.get(idx - 1).put(campo, valor);
            }
        }

        SeccionesPayload payload = new SeccionesPayload(campos, listas);
        String tipoDocumento = mapCodigoToTipoDocumento(tipoDte);
        return new ImportResult(tipoDocumento, payload);
    }

    /**
     * Mapea el código interno de TipoDTE (p. ej. "01") al nombre legible del
     * documento (p. ej. "Factura").
     *
     * @param tipoDte código detectado en la trama
     * @return nombre legible del tipo de documento o {@code null} si no se
     * reconoce
     */
    private static String mapCodigoToTipoDocumento(String tipoDte) {
        if (tipoDte == null || tipoDte.isBlank()) {
            return null;
        }
        return switch (tipoDte) {
            case "01" -> "Factura";
            case "03" -> "Boleta";
            case "07" -> "Nota de crédito";
            case "08" -> "Nota de débito";
            case "09" -> "Guia de Remision";
            case "31" -> "Guia de Remision Transportista";
            default -> null;
        };
    }

    /**
     * Resultado de la importación: tipo de documento detectado y el payload con
     * secciones.
     *
     * @param tipoDocumento nombre legible del documento (Factura, Boleta, ...)
     * @param secciones     payload con las secciones y listas detectadas
     */
    public record ImportResult(String tipoDocumento, SeccionesPayload secciones) {
    }
}
