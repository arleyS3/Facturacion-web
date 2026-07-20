package com.facturacion.api.application.comprobante.ubl.builder.guiaRemision;

import com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision.GuiaRemisionLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision.GuiaRemisionUblData;
import oasis.names.specification.ubl.schema.xsd.despatchadvice_21.DespatchAdviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuiaRemisionUblBuilderTest {

    private GuiaRemisionUblBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new GuiaRemisionUblBuilder();
    }

    @Test
    void buildDespatchAdvice_generatesSunatStructure() throws Exception {
        GuiaRemisionUblData data = new GuiaRemisionUblData(
                "TSX5",
                "00000013",
                "2026-06-02",
                "12:06:25",
                "2026-06-02",
                "09",
                "09",
                "02",
                "20600948131",
                "IMPALA TERMINALS PERU S.A.C",
                "6",
                "20513462388",
                "DP WORLD CALLAO S.R.L.",
                "6",
                "20513462388",
                "DP WORLD CALLAO S.R.L.",
                "6",
                "070101",
                "CONTRALMIRANTE MORA N° 472 - CALLAO",
                null,
                "070101",
                "AV. MANCO CAPAC NRO. 113 - CALLAO",
                null,
                null,
                null,
                null,
                null,
                null,
                "45361828",
                "1",
                "LINDER",
                "TAFUR ALZAMORA",
                "C45361828",
                new BigDecimal("29370.00"),
                "KGM",
                1,
                "CLL",
                "Callao",
                "HLBU2338162",
                "002SR105262",
                "118-2023-40-61131",
                "50",
                "20602073905",
                List.of("SUNAT_Envio_IndicadorTrasladoTotalDAMoDS", "SUNAT_Envio_IndicadorTrasladoVehiculoM1L"),
                List.of(new GuiaRemisionLineaUblData(1, "ITEM01", "CONCENTRADO DE PLATA", new BigDecimal("29370.00"), "KGM", "118-2023-40-61131"))
        );

        DespatchAdviceType guia = builder.buildDespatchAdvice(data);
        assertNotNull(guia);
        assertEquals("TSX5-00000013", guia.getID().getValue());

        String xml = builder.serializarGuiaRemision(guia);
        assertNotNull(xml);
        assertTrue(xml.contains("09"));
        assertTrue(xml.contains("50"));
        assertTrue(xml.contains("<cbc:FirstName>LINDER</cbc:FirstName>"));
        assertTrue(xml.contains("<cbc:FamilyName>TAFUR ALZAMORA</cbc:FamilyName>"));
        assertTrue(xml.contains("CONTRALMIRANTE MORA"));
        assertTrue(xml.contains("AV. MANCO CAPAC"));
        assertTrue(xml.contains("SUNAT_Envio_IndicadorTrasladoTotalDAMoDS"));
        assertTrue(xml.contains("HLBU2338162"));
        assertTrue(xml.contains("002SR105262"));
        assertTrue(xml.contains("7021"));
        assertTrue(xml.contains("Numero de declaracion aduanera (DAM)"));
    }
}
