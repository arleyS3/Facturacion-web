package com.facturacion.api.application;

import com.facturacion.api.web.dto.GenerarTramaRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.HashMap;

@Service
public class RequestValidationService {

    /**
     * Valida la solicitud de generación de trama.
     *
     * <p>Aplica un conjunto de validaciones mínimas trasladadas desde la lógica "desktop".
     * Si se detectan errores, lanza {@link ValidationException} con la lista de mensajes.</p>
     *
     * @param request Request a validar. No puede ser null.
     * @throws IllegalArgumentException si request es null o contiene datos imprescindibles faltantes.
     * @throws ValidationException si la validación detecta errores de negocio.
     */
    public void validar(GenerarTramaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request es requerido");
        }

        String tipoDocumento = request.tipoDocumento();
        var payload = request.secciones();
        Map<String, Map<String, String>> campos = payload != null ? payload.campos() : null;

        List<String> errores = new ArrayList<>();

        String codigoSunat;
        try {
            codigoSunat = TipoDocumentoMapper.toCodigoSunat(tipoDocumento);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }

        // Mínimo común: reglas de desktop
        validarSeccionAComun(errores, codigoSunat, campos);

        // Factura / Boleta / NC / ND: validación mínima de items/totales basada en tramas reales
        if ("01".equals(codigoSunat) || "03".equals(codigoSunat) || "07".equals(codigoSunat) || "08".equals(codigoSunat)) {
            validarComprobante(errores, payload != null ? payload.campos() : null, payload != null ? payload.listas() : null);
        }

        // Guía de remisión (reglas mínimas portadas desde ValidacionSeccionD/ValidacionSeccionG)
        if ("09".equals(codigoSunat)) {
            validarGuiaRemision(errores, payload != null ? payload.campos() : null, payload != null ? payload.listas() : null);
        }

        if (!errores.isEmpty()) {
            throw new ValidationException("Validación falló", errores);
        }
    }

    /**
     * Valida reglas específicas para Guía de Remisión.
     *
     * @param errores Lista donde se acumulan mensajes de error.
     * @param campos  Mapa de campos por sección.
     * @param listas  Mapa de listas por sección.
     */
    private static void validarGuiaRemision(
            List<String> errores,
            Map<String, Map<String, String>> campos,
            Map<String, List<Map<String, String>>> listas
    ) {
        Map<String, String> g = campos != null ? campos.get("G") : null;
        String motivoTraslado = get(g, "MotTras");
        if (!ValidationUtils.requerido(motivoTraslado)) {
            errores.add("G.MotTras: El motivo de traslado es obligatorio");
        }

        // Sección D: tipo de documento de referencia permitido (01,03,04,09,12,48)
        // En el desktop se usa para permitir agregar info emisor.
        Map<String, String> d = campos != null ? campos.get("D") : null;
        String tipoDocRef = get(d, "TipoDocRef");
        if (ValidationUtils.requerido(tipoDocRef) && !tipoDocReferenciaPermitidoGR(tipoDocRef)) {
            errores.add("D.TipoDocRef: TipoDocRef no permitido para GR (01,03,04,09,12,48)");
        }

        // Reglas condicionales mínimas (del desktop):
        // - Si motivo 08/09: requiere NroBultos
        if ("08".equalsIgnoreCase(motivoTraslado) || "09".equalsIgnoreCase(motivoTraslado)) {
            String nroBultos = get(g, "NroBultos");
            if (!ValidationUtils.requerido(nroBultos)) {
                errores.add("G.NroBultos: Requerido para motivo 08/09");
            }
        }

        // - Si motivo 02 o 07: requiere Proveedor (se modela como campos en G: RucProv/RznSocProv)
        if ("02".equalsIgnoreCase(motivoTraslado) || "07".equalsIgnoreCase(motivoTraslado)) {
            String rucProv = get(g, "RucProv");
            String rznProv = get(g, "RznSocProv");
            if (!ValidationUtils.requerido(rucProv) || !ValidationUtils.requerido(rznProv)) {
                errores.add("G.Proveedor: Requerido para motivo 02/07 (RucProv y RznSocProv)");
            }
        }

        // - Si motivo 03: requiere Comprador (RucComp/RznSocComp)
        if ("03".equalsIgnoreCase(motivoTraslado)) {
            String rucComp = get(g, "RucComp");
            String rznComp = get(g, "RznSocComp");
            if (!ValidationUtils.requerido(rucComp) || !ValidationUtils.requerido(rznComp)) {
                errores.add("G.Comprador: Requerido para motivo 03 (RucComp y RznSocComp)");
            }
        }

        // - Si existe lista de indicadores (G4), algunas reglas dependen de indicadores.
        // Por ahora solo validamos presencia de indicador requerido para 08/09 cuando se envíe PesoBrutoSustento.
        List<Map<String, String>> indicadores = listas != null ? listas.get("G4") : null;
        List<String> codigosIndicador = indicadores != null
                ? indicadores.stream().map(m -> m.get("IndicadorTraslado")).filter(s -> s != null && !s.isBlank()).toList()
                : List.of();

        if (("08".equalsIgnoreCase(motivoTraslado) || "09".equalsIgnoreCase(motivoTraslado))
                && ValidationUtils.requerido(get(g, "PesBruTot"))) {
            boolean tieneIndicador = codigosIndicador.stream()
                    .anyMatch(s -> "SUNAT_Envio_IndicadorTrasladoTotalDAMoDS".equalsIgnoreCase(s));
            if (!tieneIndicador) {
                errores.add("G4.IndicadorTraslado: Falta SUNAT_Envio_IndicadorTrasladoTotalDAMoDS para motivo 08/09");
            }
        }
        // G2: puerto/aeropuerto requerido para motivo 08/09 (desktop: ValidacionSeccionG2)
        if ("08".equalsIgnoreCase(motivoTraslado) || "09".equalsIgnoreCase(motivoTraslado)) {
            Map<String, String> g2 = campos != null ? campos.get("G2") : null;
            String codPuertoAeropuerto = get(g2, "CodPueAer");
            if (!ValidationUtils.requerido(codPuertoAeropuerto)) {
                errores.add("G2.CodPueAer: Requerido para motivo 08/09");
            }
        }

        // G1: FecIniTras requerido para modalidad 02 (transporte público)
        String modalidadTraslado = get(g, "ModTras");
        if ("02".equalsIgnoreCase(modalidadTraslado)) {
            Map<String, String> g1 = campos != null ? campos.get("G1") : null;
            String fecIniTras = get(g1, "FecIniTras");
            if (!ValidationUtils.requerido(fecIniTras)) {
                errores.add("G1.FecIniTras: Requerido para modalidad 02");
            }

            // Conductor (G11) requerido si no hay indicador M1L
            boolean tieneM1L = codigosIndicador.stream()
                    .anyMatch(s -> "M1L".equalsIgnoreCase(s));
            if (!tieneM1L) {
                List<Map<String, String>> conductores = listas != null ? listas.get("G11") : null;
                if (conductores == null || conductores.isEmpty()) {
                    errores.add("G11: Requerido al menos un conductor para modalidad 02 (sin M1L)");
                }
            }
        }
    }

    /**
     * Valida las reglas mínimas para Guía de Remisión.
     * Añade mensajes a la lista de errores cuando aplica.
     */

    private static void validarComprobante(
            List<String> errores,
            Map<String, Map<String, String>> campos,
            Map<String, List<Map<String, String>>> listas
    ) {
        Map<String, String> aRaw = campos != null ? campos.get("A") : null;
        Map<String, String> a = normalizeSeccionA(aRaw);

        // Items (B)
        List<Map<String, String>> items = listas != null ? listas.get("B") : null;
        if (items == null || items.isEmpty()) {
            errores.add("B: Se requiere al menos un item en sección B");
            return;
        }

        // CantidadItem (si viene) debe coincidir con cantidad de items
        Integer cantidadItem = parseIntSafe(a.get("CantidadItem"));
        if (cantidadItem != null && cantidadItem > 0 && cantidadItem != items.size()) {
            errores.add("A.CantidadItem: No coincide con el número de items en B");
        }

        Map<String, Map<String, String>> descuentosPorLinea = indexByNroLinDet(listas != null ? listas.get("B1") : null);

        BigDecimal sumaMontoItem = BigDecimal.ZERO;
        BigDecimal sumaIgvItem = BigDecimal.ZERO;
        BigDecimal sumaGravadoBase = BigDecimal.ZERO;
        BigDecimal sumaExonerado = BigDecimal.ZERO;
        BigDecimal sumaInafecto = BigDecimal.ZERO;
        for (Map<String, String> item : items) {
            if (item == null) continue;

            String nroLinDet = trimToNull(item.get("NroLinDet"));
            if (nroLinDet == null) {
                errores.add("B.NroLinDet: Requerido");
            }

            String codigoAnexoEmisor = get(a, "CodAnxEmi");
            if (!ValidationUtils.requerido(codigoAnexoEmisor)) {
                errores.add("A.CodAnxEmi: El código de anexo del emisor es obligatorio");
            }

            String nroDocReceptor = get(a, "RUTRecep");
            if (!ValidationUtils.requerido(nroDocReceptor)) {
                errores.add("A.RUTRecep: El número de documento del receptor es obligatorio");
            }

            String razonSocialReceptor = get(a, "RznSocRecep");
            if (!ValidationUtils.requerido(razonSocialReceptor)) {
                errores.add("A.RznSocRecep: La razón social del receptor es obligatoria");
            }

            String qty = trimToNull(item.get("QtyItem"));
            if (qty == null) {
                errores.add("B.QtyItem(" + safe(nroLinDet) + "): Requerido");
            }

            String unmd = trimToNull(item.get("UnmdItem"));
            if (unmd == null) {
                errores.add("B.UnmdItem(" + safe(nroLinDet) + "): Requerido");
            }

            String nombre = trimToNull(item.get("NmbItem"));
            if (nombre == null) {
                errores.add("B.NmbItem(" + safe(nroLinDet) + "): Requerido");
            }

            String montoItemRaw = trimToNull(item.get("MontoItem"));
            BigDecimal montoItem = parseDecimalSafe(montoItemRaw);
            if (montoItem == null) {
                errores.add("B.MontoItem(" + safe(nroLinDet) + "): Requerido y numérico");
            } else {
                sumaMontoItem = sumaMontoItem.add(montoItem);
            }

            BigDecimal igvItem = parseDecimalSafe(trimToNull(item.get("ImpuestoIgv")));
            if (igvItem != null) {
                sumaIgvItem = sumaIgvItem.add(igvItem);
            }

            // Afectación IGV por item
            String codigoTipoIgv = trimToNull(item.get("CodigoTipoIgv"));
            BigDecimal tasaPct = parseDecimalSafe(trimToNull(item.get("TasaIgv")));
            if (codigoTipoIgv != null && montoItem != null) {
                switch (codigoTipoIgv) {
                    case "1000" -> {
                        // Gravado: suma base gravada y validar tasa si viene
                        sumaGravadoBase = sumaGravadoBase.add(montoItem);
                        if (tasaPct != null && tasaPct.compareTo(BigDecimal.ZERO) <= 0) {
                            errores.add("B.TasaIgv(" + safe(nroLinDet) + "): Debe ser > 0 para CodigoTipoIgv=1000");
                        }
                    }
                    case "9998" -> {
                        sumaExonerado = sumaExonerado.add(montoItem);
                        if (tasaPct != null && tasaPct.compareTo(BigDecimal.ZERO) != 0) {
                            errores.add("B.TasaIgv(" + safe(nroLinDet) + "): Debe ser 0 para CodigoTipoIgv=9998");
                        }
                        if (igvItem != null && igvItem.compareTo(BigDecimal.ZERO) != 0) {
                            errores.add("B.ImpuestoIgv(" + safe(nroLinDet) + "): Debe ser 0 para CodigoTipoIgv=9998");
                        }
                    }
                    case "9995" -> {
                        sumaInafecto = sumaInafecto.add(montoItem);
                        if (tasaPct != null && tasaPct.compareTo(BigDecimal.ZERO) != 0) {
                            errores.add("B.TasaIgv(" + safe(nroLinDet) + "): Debe ser 0 para CodigoTipoIgv=9995");
                        }
                        if (igvItem != null && igvItem.compareTo(BigDecimal.ZERO) != 0) {
                            errores.add("B.ImpuestoIgv(" + safe(nroLinDet) + "): Debe ser 0 para CodigoTipoIgv=9995");
                        }
                    }
                    default -> {
                        // otros códigos: por ahora no validamos, solo permitimos
                    }
                }
            }

            // Consistencia tipo desktop (si vienen campos)
            validarConsistenciaItemDesktopStyle(errores, nroLinDet, item, descuentosPorLinea.get(nroLinDet));
        }

        // Descuentos/cargos por item (B1) - si existen, validar que referencien NroLinDet
        List<Map<String, String>> descuentos = listas != null ? listas.get("B1") : null;
        if (descuentos != null) {
            for (Map<String, String> d : descuentos) {
                if (d == null) continue;
                String nroLinDet = trimToNull(d.get("NroLinDet"));
                if (nroLinDet == null) {
                    errores.add("B1.NroLinDet: Requerido");
                    continue;
                }
                // Campos típicos del descuento
                requireNonBlank(errores, "B1.IndCargoDescuento(" + nroLinDet + ")", d.get("IndCargoDescuento"));
                requireNonBlank(errores, "B1.CodigoCargoDescuento(" + nroLinDet + ")", d.get("CodigoCargoDescuento"));
                // numéricos
                if (trimToNull(d.get("FactorCargoDescuento")) != null && parseDecimalSafe(d.get("FactorCargoDescuento")) == null) {
                    errores.add("B1.FactorCargoDescuento(" + nroLinDet + "): Debe ser numérico");
                }
                if (trimToNull(d.get("MontoCargoDescuento")) != null && parseDecimalSafe(d.get("MontoCargoDescuento")) == null) {
                    errores.add("B1.MontoCargoDescuento(" + nroLinDet + "): Debe ser numérico");
                }
                if (trimToNull(d.get("MBaseCargoDescuento")) != null && parseDecimalSafe(d.get("MBaseCargoDescuento")) == null) {
                    errores.add("B1.MBaseCargoDescuento(" + nroLinDet + "): Debe ser numérico");
                }
            }
        }

        // Totales básicos: si vienen, validar coherencia simple.
        BigDecimal mntTotGrat = parseDecimalSafe(a.get("MntTotGrat"));
        boolean hayGratuitas = mntTotGrat != null && mntTotGrat.compareTo(BigDecimal.ZERO) > 0;

        BigDecimal mntTotal = parseDecimalSafe(a.get("MntTotal"));
        if (mntTotal != null && !hayGratuitas) {
            // No imponemos igualdad exacta por redondeos/escenarios, solo que sea >= suma items.
            if (mntTotal.compareTo(sumaMontoItem) < 0) {
                errores.add("A.MntTotal: No puede ser menor que la suma de B.MontoItem");
            }
        }

        BigDecimal mntTotalIgv = parseDecimalSafe(a.get("MntTotalIgv"));
        if (mntTotalIgv != null) {
            // tolerancia: no estricta, solo si difiere demasiado
            BigDecimal diff = mntTotalIgv.subtract(sumaIgvItem).abs();
            if (diff.compareTo(new BigDecimal("0.05")) > 0) {
                errores.add("A.MntTotalIgv: No coincide con la suma de B.ImpuestoIgv (tolerancia 0.05)");
            }
        }

        // Gratuitas (mínimo): si hay monto gratuito declarado, IGV debe ser 0
        if (hayGratuitas) {
            if (sumaIgvItem.compareTo(BigDecimal.ZERO) != 0) {
                errores.add("A.MntTotGrat: Con gratuitas, la suma de IGV por item debe ser 0");
            }
            if (mntTotalIgv != null && mntTotalIgv.compareTo(BigDecimal.ZERO) != 0) {
                errores.add("A.MntTotalIgv: Con gratuitas, debe ser 0");
            }
        }

        // Totales por afectación (si vienen)
        BigDecimal mntNeto = parseDecimalSafe(a.get("MntNeto"));
        if (mntNeto != null) {
            if (!nearlyEquals(mntNeto, sumaGravadoBase, new BigDecimal("0.10"))) {
                errores.add("A.MntNeto: No coincide con la suma de items gravados (CodigoTipoIgv=1000) (tol 0.10)");
            }
        }
        BigDecimal mntExo = parseDecimalSafe(a.get("MntExo"));
        if (mntExo != null) {
            if (!nearlyEquals(mntExo, sumaExonerado, new BigDecimal("0.10"))) {
                errores.add("A.MntExo: No coincide con la suma de items exonerados (CodigoTipoIgv=9998) (tol 0.10)");
            }
        }
        BigDecimal mntExe = parseDecimalSafe(a.get("MntExe"));
        if (mntExe != null) {
            if (!nearlyEquals(mntExe, sumaInafecto, new BigDecimal("0.10"))) {
                errores.add("A.MntExe: No coincide con la suma de items inafectos (CodigoTipoIgv=9995) (tol 0.10)");
            }
        }

        // A2 resumen impuestos (si viene) debe contener al menos un registro 1000 o 9998 cuando existan montos
        List<Map<String, String>> a2 = listas != null ? listas.get("A2") : null;
        if (a2 != null && !a2.isEmpty()) {
            boolean tiene1000 = a2.stream().anyMatch(m -> "1000".equals(trimToNull(m.get("CodigoImpuesto"))));
            boolean tiene9998 = a2.stream().anyMatch(m -> "9998".equals(trimToNull(m.get("CodigoImpuesto"))));
            if (sumaGravadoBase.compareTo(BigDecimal.ZERO) > 0 && !tiene1000) {
                errores.add("A2.CodigoImpuesto: Falta registro 1000 para items gravados");
            }
            if (sumaExonerado.compareTo(BigDecimal.ZERO) > 0 && !tiene9998) {
                errores.add("A2.CodigoImpuesto: Falta registro 9998 para items exonerados");
            }

            // Validación fina A2 contra cálculos (si vienen montos)
            validarResumenA2(errores, a2, sumaGravadoBase, sumaIgvItem, sumaExonerado);
        }
    }

    private static void validarResumenA2(
            List<String> errores,
            List<Map<String, String>> a2,
            BigDecimal baseGravada,
            BigDecimal igvTotal,
            BigDecimal baseExonerada
    ) {
        // 1000: base y monto impuesto deberían cuadrar
        Map<String, String> reg1000 = a2.stream()
                .filter(m -> "1000".equals(trimToNull(m.get("CodigoImpuesto"))))
                .findFirst()
                .orElse(null);
        if (reg1000 != null) {
            BigDecimal base = parseDecimalSafe(trimToNull(reg1000.get("MontoImpuestoBase")));
            if (base != null && !nearlyEquals(base, baseGravada, new BigDecimal("0.10"))) {
                errores.add("A2.MontoImpuestoBase(1000): No coincide con base gravada (tol 0.10)");
            }
            BigDecimal monto = parseDecimalSafe(trimToNull(reg1000.get("MontoImpuesto")));
            if (monto != null && !nearlyEquals(monto, igvTotal, new BigDecimal("0.10"))) {
                errores.add("A2.MontoImpuesto(1000): No coincide con IGV total (tol 0.10)");
            }
        }

        // 9998: base debería cuadrar (cuando la columna exista; en ejemplos a veces no aparece)
        Map<String, String> reg9998 = a2.stream()
                .filter(m -> "9998".equals(trimToNull(m.get("CodigoImpuesto"))))
                .findFirst()
                .orElse(null);
        if (reg9998 != null) {
            BigDecimal base = parseDecimalSafe(trimToNull(reg9998.get("MontoImpuestoBase")));
            if (base != null && !nearlyEquals(base, baseExonerada, new BigDecimal("0.10"))) {
                errores.add("A2.MontoImpuestoBase(9998): No coincide con base exonerada (tol 0.10)");
            }
            BigDecimal monto = parseDecimalSafe(trimToNull(reg9998.get("MontoImpuesto")));
            if (monto != null && monto.compareTo(BigDecimal.ZERO) != 0) {
                errores.add("A2.MontoImpuesto(9998): Debe ser 0");
            }
        }
    }

    private static void validarConsistenciaItemDesktopStyle(
            List<String> errores,
            String nroLinDet,
            Map<String, String> item,
            Map<String, String> descuentoB1
    ) {
        // Desktop: tasa IGV fija 0.18 => TasaIgv 18
        BigDecimal tasaPct = parseDecimalSafe(trimToNull(item.get("TasaIgv")));
        BigDecimal tasa = null;
        if (tasaPct != null) {
            tasa = tasaPct.divide(new BigDecimal("100"));
        }

        BigDecimal qty = parseDecimalSafe(trimToNull(item.get("QtyItem")));
        BigDecimal prcSin = parseDecimalSafe(trimToNull(item.get("PrcItemSinIgv")));
        BigDecimal montoItem = parseDecimalSafe(trimToNull(item.get("MontoItem")));
        BigDecimal igv = parseDecimalSafe(trimToNull(item.get("ImpuestoIgv")));
        BigDecimal prcCon = parseDecimalSafe(trimToNull(item.get("PrcItem")));

        BigDecimal baseParaMonto = null;
        if (descuentoB1 != null) {
            BigDecimal mBase = parseDecimalSafe(trimToNull(descuentoB1.get("MBaseCargoDescuento")));
            if (mBase != null) {
                baseParaMonto = mBase;
            }
        }

        // Si hay descuento, el ejemplo muestra que PrcItemSinIgv puede ser la base (antes del descuento)
        // y MontoItem el valor final; hacemos chequeo suave: MontoItem <= baseParaMonto.
        if (baseParaMonto != null && montoItem != null && montoItem.compareTo(baseParaMonto) > 0) {
            errores.add("B.MontoItem(" + safe(nroLinDet) + "): No debe exceder B1.MBaseCargoDescuento");
        }

        // Si vienen qty y prcSin y montoItem, validar montoItem ~= qty*prcSin (o contra base si hay descuento)
        if (qty != null && prcSin != null && montoItem != null) {
            BigDecimal esperado = qty.multiply(prcSin);
            // tolerancia por redondeo / decimales largos
            if (!nearlyEquals(esperado, montoItem, new BigDecimal("0.10"))) {
                // si hay descuento, no forzamos igualdad estricta
                if (baseParaMonto == null) {
                    errores.add("B.MontoItem(" + safe(nroLinDet) + "): No coincide con QtyItem*PrcItemSinIgv (tol 0.10)");
                }
            }
        }

        // Si vienen tasa y montoItem e igv: igv ~= montoItem*tasa
        if (tasa != null && montoItem != null && igv != null) {
            BigDecimal esperadoIgv = montoItem.multiply(tasa);
            if (!nearlyEquals(esperadoIgv, igv, new BigDecimal("0.10"))) {
                errores.add("B.ImpuestoIgv(" + safe(nroLinDet) + "): No coincide con MontoItem*TasaIgv (tol 0.10)");
            }
        }

        // Si vienen prcSin, tasa y prcCon: prcCon ~= prcSin*(1+tasa)
        // Nota: cuando hay descuentos/cargos (B1), en ejemplos reales prcSin puede ser base pre-descuento.
        if (descuentoB1 == null && tasa != null && prcSin != null && prcCon != null) {
            BigDecimal esperadoCon = prcSin.multiply(BigDecimal.ONE.add(tasa));
            if (!nearlyEquals(esperadoCon, prcCon, new BigDecimal("0.10"))) {
                errores.add("B.PrcItem(" + safe(nroLinDet) + "): No coincide con PrcItemSinIgv*(1+TasaIgv) (tol 0.10)");
            }
        }
    }

    private static boolean nearlyEquals(BigDecimal a, BigDecimal b, BigDecimal tolerance) {
        if (a == null || b == null) return false;
        return a.subtract(b).abs().compareTo(tolerance) <= 0;
    }

    private static Map<String, Map<String, String>> indexByNroLinDet(List<Map<String, String>> list) {
        Map<String, Map<String, String>> out = new HashMap<>();
        if (list == null) return out;
        for (Map<String, String> row : list) {
            if (row == null) continue;
            String nro = trimToNull(row.get("NroLinDet"));
            if (nro == null) continue;
            out.put(nro, row);
        }
        return out;
    }

    private static Map<String, String> normalizeSeccionA(Map<String, String> a) {
        Map<String, String> out = new HashMap<>();
        if (a != null) out.putAll(a);

        // aliases usados en tramas reales vs validaciones previas
        alias(out, "HorEmis", "HoraEmision");
        alias(out, "TipoDocEmis", "TipoRucEmis");
        alias(out, "TipoDocRec", "TipoRutReceptor");
        alias(out, "CodAnxEmi", "CodigoLocalAnexo");
        alias(out, "CodEmp", "CODI_EMPR");
        return out;
    }

    private static void alias(Map<String, String> map, String canonical, String alternate) {
        String c = trimToNull(map.get(canonical));
        if (c != null) return;
        String alt = trimToNull(map.get(alternate));
        if (alt != null) map.put(canonical, alt);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String safe(String s) {
        return s == null ? "?" : s;
    }

    private static Integer parseIntSafe(String raw) {
        String t = trimToNull(raw);
        if (t == null) return null;
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal parseDecimalSafe(String raw) {
        String t = trimToNull(raw);
        if (t == null) return null;
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void requireNonBlank(List<String> errores, String label, String value) {
        if (value == null || value.isBlank()) {
            errores.add(label + ": Requerido");
        }
    }

    private static boolean tipoDocReferenciaPermitidoGR(String tipoDocReferencia) {
        return switch (tipoDocReferencia) {
            case "01", "03", "04", "09", "12", "48" -> true;
            default -> false;
        };
    }

    /**
     * Valida la sección A con reglas comunes (serie, correlativo, fecha, emisor, etc.).
     */
    private static void validarSeccionAComun(
            List<String> errores,
            String tipoDocumentoSunat,
            Map<String, Map<String, String>> campos
        ) {
        Map<String, String> a = campos != null ? campos.get("A") : null;
        a = normalizeSeccionA(a);

        // Nota: los nombres exactos de campos vienen de tu SeccionA en core.
        // Usamos los que ya vienen en plantillas/importación actuales.
        String codigoEmpresa = get(a, "CodEmp");
        if (!ValidationUtils.requerido(codigoEmpresa)) {
            errores.add("A.CodEmp: El codigo de la empresa es obligatorio");
        } else {
            String ce = codigoEmpresa.trim();
            boolean esNumerico = ce.chars().allMatch(Character::isDigit);
            if (!esNumerico || ce.length() < 1 || ce.length() > 9) {
                errores.add("A.CodEmp: El codigo de la empresa debe ser numérico (1 a 9 dígitos)");
            }
        }

        String serie = get(a, "Serie");
        if (!ValidationUtils.requerido(serie)) {
            errores.add("A.Serie: La serie del documento es obligatoria");
        } else if (!ValidationUtils.serieSegunTipo(serie, tipoDocumentoSunat)) {
            errores.add("A.Serie: Serie no válida para el tipo de documento");
        }

        String correlativo = get(a, "Correlativo");
        if (!ValidationUtils.requerido(correlativo)) {
            errores.add("A.Correlativo: El correlativo del documento es obligatorio");
        } else if (!ValidationUtils.correlativo8(correlativo)) {
            errores.add("A.Correlativo: El correlativo debe ser numérico de 8 dígitos");
        }

        String fechaEmision = get(a, "FchEmis");
        if (!ValidationUtils.requerido(fechaEmision)) {
            errores.add("A.FchEmis: La fecha de emisión es obligatoria");
        } else if (!ValidationUtils.fechaIso(fechaEmision)) {
            errores.add("A.FchEmis: El formato de fecha debe ser en AAAA-MM-DD");
        }

        String horaEmision = get(a, "HorEmis");
        if (!ValidationUtils.requerido(horaEmision)) {
            errores.add("A.HorEmis: La hora de emisión es obligatoria");
        } else if (!ValidationUtils.hora(horaEmision)) {
            errores.add("A.HorEmis: El formato de hora debe ser HH:MM:SS");
        }

        String tipoDocEmisor = get(a, "TipoDocEmis");
        String nroDocEmisor = get(a, "RUTEmis");
        if (!ValidationUtils.requerido(nroDocEmisor)) {
            errores.add("A.RUTEmis: El número de documento del emisor es obligatorio");
        } else if ("6".equals(tipoDocEmisor) && !ValidationUtils.ruc(nroDocEmisor)) {
            errores.add("A.RUTEmis: El RUC del emisor debe tener 11 dígitos y comenzar con 10 o 20");
        }

        String razonSocialEmisor = get(a, "RznSocEmis");
        if (!ValidationUtils.requerido(razonSocialEmisor)) {
            errores.add("A.RznSocEmis: La razón social del emisor es obligatoria");
        }


        String tipoDocReceptor = get(a, "TipoDocRec");


    }

    private static String get(Map<String, String> map, String key) {
        if (map == null) return null;
        return map.get(key);
    }



    private static void requireCampo(List<String> errores, Map<String, Map<String, String>> campos, String seccion, String campo) {
        if (campos == null) {
            errores.add(seccion + "." + campo);
            return;
        }
        Map<String, String> s = campos.get(seccion);
        if (s == null) {
            errores.add(seccion + "." + campo);
            return;
        }
        String v = s.get(campo);
        if (v == null || v.isBlank()) {
            errores.add(seccion + "." + campo);
        }
    }
}
