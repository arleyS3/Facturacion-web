package com.facturacion.api.application;

import com.facturacion.api.web.dto.SeccionesPayload;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlantillaService {

        /**
         * Devuelve una plantilla (secciones + listas) básica según el slug solicitado.
         *
         * @param slug Identificador legible (ej. "factura", "boleta").
         * @return SeccionesPayload con campos y listas por defecto para el tipo.
         */
        public SeccionesPayload plantillaPorSlug(String slug) {
                if (slug == null) {
                        return new SeccionesPayload(Map.of(), Map.of());
                }

                return switch (slug.toLowerCase()) {
                        case "factura" -> plantillaFactura();
                        case "boleta" -> plantillaBoleta();
                        case "nota-credito" -> plantillaNotaCredito();
                        case "nota-debito" -> plantillaNotaDebito();
                        case "guia-remision" -> plantillaGuiaRemision();
                        case "guia-remision-transportista" -> plantillaGuiaRemisionTransportista();
                        default -> new SeccionesPayload(Map.of(), Map.of());
                };
        }

        private static SeccionesPayload plantillaFactura() {
                Map<String, Map<String, String>> campos = new LinkedHashMap<>();
                Map<String, List<Map<String, String>>> listas = new LinkedHashMap<>();

                Map<String, String> a = new LinkedHashMap<>();
                a.put("TipoDTE", "01");
                a.put("Serie", "");
                a.put("Correlativo", "");
                a.put("FchEmis", "");
                a.put("HoraEmision", "");
                a.put("TipoMoneda", "");
                a.put("RUTEmis", "");
                a.put("TipoRucEmis", "6");
                a.put("RznSocEmis", "");
                a.put("DirEmis", "");
                a.put("ComuEmis", "");
                a.put("PaisEmis", "PE");
                a.put("TipoRutReceptor", "");
                a.put("RUTRecep", "");
                a.put("RznSocRecep", "");
                a.put("DirRecep", "");
                a.put("TipoOperacion", "0101");
                a.put("CantidadItem", "");
                a.put("FormaPago", "Contado");
                a.put("MntNeto", "");
                a.put("MntExe", "0.00");
                a.put("MntExo", "0.00");
                a.put("MntTotGrat", "0.00");
                a.put("MntTotalIgv", "");
                a.put("MntTotal", "");
                a.put("MntRedondeo", "0.00");
                campos.put("A", a);

                // Recomendado como listas por idx (como tu TXT):
                listas.put("A5", List.of(Map.of(
                                "Cuota", "",
                                "MontoCuota", "",
                                "FechaVencCuota", "")));

                listas.put("B", List.of(Map.ofEntries(
                                Map.entry("NroLinDet", ""),
                                Map.entry("QtyItem", ""),
                                Map.entry("UnmdItem", ""),
                                Map.entry("VlrCodigo", ""),
                                Map.entry("NmbItem", ""),
                                Map.entry("PrcItem", ""),
                                Map.entry("PrcItemSinIgv", ""),
                                Map.entry("MontoItem", ""),
                                Map.entry("IndExe", ""),
                                Map.entry("CodigoTipoIgv", ""),
                                Map.entry("TasaIgv", ""),
                                Map.entry("ImpuestoIgv", ""),
                                Map.entry("MontoBaseImp", ""),
                                Map.entry("CodigoProductoSunat", ""))));

                // Descuentos/cargos por item
                listas.put("B1", List.of(Map.of(
                                "NroLinDet", "",
                                "IndCargoDescuento", "",
                                "CodigoCargoDescuento", "",
                                "FactorCargoDescuento", "",
                                "MontoCargoDescuento", "",
                                "MBaseCargoDescuento", "")));

                listas.put("E", List.of(Map.of(
                                "TipoAdicSunat", "01",
                                "NmrLineasAdicSunat", "",
                                "DescripcionAdicSunat", "")));

                listas.put("M", List.of(Map.of(
                                "NroLinMail", "",
                                "MailEnvi", "")));

                // A2 también es lista por idx en tu Boleta/TXT
                listas.put("A2", List.of(Map.of(
                                "CodigoImpuesto", "",
                                "MontoImpuesto", "",
                                "TasaImpuesto", "",
                                "MontoImpuestoBase", "")));

                return new SeccionesPayload(campos, listas);
        }

        private static SeccionesPayload plantillaBoleta() {
                Map<String, Map<String, String>> campos = new LinkedHashMap<>();
                Map<String, List<Map<String, String>>> listas = new LinkedHashMap<>();

                Map<String, String> a = new LinkedHashMap<>();
                a.put("TipoDTE", "03");
                a.put("Serie", "");
                a.put("Correlativo", "");
                a.put("FchEmis", "");
                a.put("HoraEmision", "");
                a.put("RUTEmis", "");
                a.put("TipoRucEmis", "6");
                a.put("RznSocEmis", "");
                a.put("DirEmis", "");
                a.put("CodigoLocalAnexo", "0000");
                a.put("TipoRutReceptor", "");
                a.put("RUTRecep", "");
                a.put("RznSocRecep", "");
                a.put("DirRecep", "");
                a.put("TipoMoneda", "PEN");
                a.put("CantidadItem", "");
                a.put("FormaPago", "Contado");
                a.put("MntNeto", "");
                a.put("MntExe", "0.00");
                a.put("MntExo", "0.00");
                a.put("MntTotGrat", "0.00");
                a.put("MntTotalIgv", "");
                a.put("MntTotal", "");
                campos.put("A", a);

                listas.put("A2", List.of(Map.of(
                                "CodigoImpuesto", "",
                                "MontoImpuesto", "",
                                "TasaImpuesto", "",
                                "MontoImpuestoBase", "")));

                // Map.of supports up to 10 key/value pairs. Use Map.ofEntries for larger maps.
                listas.put("B", List.of(Map.ofEntries(
                                Map.entry("NroLinDet", ""),
                                Map.entry("QtyItem", ""),
                                Map.entry("UnmdItem", ""),
                                Map.entry("VlrCodigo", ""),
                                Map.entry("NmbItem", ""),
                                Map.entry("PrcItem", ""),
                                Map.entry("PrcItemSinIgv", ""),
                                Map.entry("MontoItem", ""),
                                Map.entry("IndExe", ""),
                                Map.entry("CodigoTipoIgv", ""),
                                Map.entry("TasaIgv", ""),
                                Map.entry("ImpuestoIgv", ""),
                                Map.entry("MontoBaseImp", ""),
                                Map.entry("CodigoProductoSunat", ""))));

                listas.put("B1", List.of(Map.of(
                                "NroLinDet", "",
                                "IndCargoDescuento", "",
                                "CodigoCargoDescuento", "",
                                "FactorCargoDescuento", "",
                                "MontoCargoDescuento", "",
                                "MBaseCargoDescuento", "")));

                listas.put("E", List.of(Map.of(
                                "TipoAdicSunat", "01",
                                "NmrLineasAdicSunat", "",
                                "DescripcionAdicSunat", "")));

                return new SeccionesPayload(campos, listas);
        }

        private static SeccionesPayload plantillaNotaCredito() {
                // Estructura similar a Factura (A + B + A2) y puede incluir E/M según tu trama
                // real.
                Map<String, Map<String, String>> campos = new LinkedHashMap<>();
                Map<String, List<Map<String, String>>> listas = new LinkedHashMap<>();

                campos.put("A", new LinkedHashMap<>(Map.of(
                                "TipoDTE", "07",
                                "Serie", "",
                                "Correlativo", "",
                                "FchEmis", "",
                                "TipoMoneda", "",
                                "RUTEmis", "")));

                listas.put("B", List.of(Map.of(
                                "NroLinDet", "",
                                "QtyItem", "",
                                "UnmdItem", "",
                                "NmbItem", "",
                                "MontoItem", "")));

                listas.put("A2", List.of(Map.of(
                                "CodigoImpuesto", "",
                                "MontoImpuesto", "",
                                "TasaImpuesto", "",
                                "MontoImpuestoBase", "")));

                return new SeccionesPayload(campos, listas);
        }

        private static SeccionesPayload plantillaNotaDebito() {
                Map<String, Map<String, String>> campos = new LinkedHashMap<>();
                Map<String, List<Map<String, String>>> listas = new LinkedHashMap<>();

                campos.put("A", new LinkedHashMap<>(Map.of(
                                "TipoDTE", "08",
                                "Serie", "",
                                "Correlativo", "",
                                "FchEmis", "",
                                "TipoMoneda", "",
                                "RUTEmis", "")));

                listas.put("B", List.of(Map.of(
                                "NroLinDet", "",
                                "QtyItem", "",
                                "UnmdItem", "",
                                "NmbItem", "",
                                "MontoItem", "")));

                listas.put("A2", List.of(Map.of(
                                "CodigoImpuesto", "",
                                "MontoImpuesto", "",
                                "TasaImpuesto", "",
                                "MontoImpuestoBase", "")));

                return new SeccionesPayload(campos, listas);
        }

        private static SeccionesPayload plantillaGuiaRemision() {
                Map<String, Map<String, String>> campos = new LinkedHashMap<>();
                Map<String, List<Map<String, String>>> listas = new LinkedHashMap<>();

                campos.put("A", new LinkedHashMap<>(Map.of(
                                "TipoDTE", "09",
                                "Serie", "",
                                "Correlativo", "",
                                "FchEmis", "",
                                "RUTEmis", "")));

                campos.put("G", new LinkedHashMap<>(Map.of(
                                "MOT_TRASLADO", "",
                                "MOD_TRASLADO", "",
                                "FEC_TRASLADO", "")));

                // Secciones típicamente repetibles en guía
                listas.put("B2", List.of(Map.of(
                                "NRO_LIN_DET", "",
                                "QTY_ITEM", "",
                                "UNMD_ITEM", "",
                                "NMB_ITEM", "")));
                listas.put("G1", List.of(Map.of(
                                "PTO_PARTIDA_UBIGEO", "",
                                "PTO_LLEGADA_UBIGEO", "")));
                listas.put("G11", List.of(Map.of(
                                "TIPO_DOC_COND", "",
                                "NRO_DOC_COND", "")));
                listas.put("E", List.of(Map.of(
                                "TipoAdicSunat", "01",
                                "NmrLineasAdicSunat", "",
                                "DescripcionAdicSunat", "")));

                return new SeccionesPayload(campos, listas);
        }

        private static SeccionesPayload plantillaGuiaRemisionTransportista() {
                Map<String, Map<String, String>> campos = new LinkedHashMap<>();
                Map<String, List<Map<String, String>>> listas = new LinkedHashMap<>();

                campos.put("A", new LinkedHashMap<>(Map.of(
                                "TipoDTE", "31",
                                "Serie", "",
                                "Correlativo", "",
                                "FchEmis", "",
                                "RUTEmis", "")));
                campos.put("G", new LinkedHashMap<>(Map.of(
                                "MOT_TRASLADO", "",
                                "FEC_TRASLADO", "")));

                return new SeccionesPayload(campos, listas);
        }
}
