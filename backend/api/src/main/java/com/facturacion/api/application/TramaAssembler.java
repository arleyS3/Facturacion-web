package com.facturacion.api.application;

import com.facturacion.api.core.documento.DocumentoElectronico;
import com.facturacion.api.core.documento.DocumentoElectronicoFactory;
import com.facturacion.api.web.dto.GenerarTramaRequest;
import com.facturacion.api.web.dto.SeccionesPayload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * En el desktop, la trama se arma concatenando tramas por sección (A + B + A2 ...).
 * Aquí empezamos con una implementación mínima y extensible: concatena secciones por orden
 * según tipoDocumento, leyendo valores desde el JSON.
 *
 * Esto permite ir reemplazando gradualmente con la lógica real de Seccion* del proyecto.
 */
@Component
public class TramaAssembler {

    private static final Set<String> INDEX_KEYS = Set.of(
            "NroLinDet",
            "NmrLineasDetalle",
            "NroLinMail",
            "NroLin",
            "NroLinea"
    );

    public String generar(GenerarTramaRequest request) {
        String codigo = TipoDocumentoMapper.toCodigoSunat(request.tipoDocumento());
        DocumentoElectronico doc = DocumentoElectronicoFactory.crearDocumentoElectronico(codigo);

        SeccionesPayload payload = request.secciones();
        if (payload != null && payload.campos() != null) {
            payload.campos().forEach((seccion, campos) -> {
                if (campos == null) return;
                var target = resolveSeccion(doc, seccion);
                if (target != null) {
                    campos.forEach(target::agregarCampoConValor);
                }
            });
        }

        // Soporte de listas para integración simple con frontend.
        // Convención: cada item en la lista se convierte a campos con valor = idx (1..n) y valor2 = valor.
        // Esto hace que el formato final sea: PREFIJO;CAMPO;idx;valor\n (como en tu TXT import/export).
        if (payload != null && payload.listas() != null) {
            payload.listas().forEach((seccion, items) -> {
                if (items == null) return;
                var target = resolveSeccion(doc, seccion);
                if (target == null) return;

                int fallbackIdx = 1;
                for (Map<String, String> item : items) {
                    if (item == null) continue;
                    String idxStr = resolveIdxStr(item, fallbackIdx);
                    fallbackIdx++;
                    item.forEach((k, v) -> target.agregarCampoConValor(k, idxStr, v));
                }
            });
        }

        return doc.generaTramaTXT().toString();
    }

    private static com.facturacion.api.core.seccion.Seccion<?> resolveSeccion(DocumentoElectronico doc, String seccion) {
        return switch (seccion) {
            case "A" -> doc.getSeccionA();
            case "A5" -> doc.getSeccionA5();
            case "B" -> doc.getSeccionB();
            case "A2" -> doc.getSeccionA2();
            case "B2" -> doc.getSeccionB2();
            case "D" -> doc.getSeccionD();
            case "E" -> doc.getSeccionE();
            case "G" -> doc.getSeccionG();
            case "M" -> doc.getSeccionM();
            case "G1" -> doc.getSeccionG1();
            case "G11" -> doc.getSeccionG11();
            case "G2" -> doc.getSeccionG2();
            case "G3" -> doc.getSeccionG3();
            case "G4" -> doc.getSeccionG4();
            case "G5" -> doc.getSeccionG5();
            default -> null;
        };
    }

    private static String resolveIdxStr(Map<String, String> item, int fallbackIdx) {
        for (String key : INDEX_KEYS) {
            String raw = item.get(key);
            if (raw == null) continue;
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) continue;
            try {
                int idx = Integer.parseInt(trimmed);
                if (idx > 0) {
                    return String.valueOf(idx);
                }
            } catch (NumberFormatException ignored) {
                // usar fallback
            }
        }
        return String.valueOf(fallbackIdx);
    }

    // mapeo centralizado en TipoDocumentoMapper
}
