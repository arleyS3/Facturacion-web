package com.facturacion.api.application.comprobante.ubl.builder.notaDebito;

import com.facturacion.api.application.comprobante.ubl.mapper.notaDebito.NotaDebitoLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.notaDebito.NotaDebitoUblData;
import java.math.BigDecimal;
import java.util.List;
import oasis.names.specification.ubl.schema.xsd.debitnote_21.DebitNoteType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link NotaDebitoUblBuilder}.
 * <p>
 * Cubre el nuevo método {@code buildDebitNote()} que expone el objeto UBL
 * antes de serializar, y verifica que {@code construirXml()} siga funcionando.
 * </p>
 */
class NotaDebitoUblBuilderTest {

    private NotaDebitoUblBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new NotaDebitoUblBuilder();
    }

    @Test
    void buildDebitNote_returnsNonNullDebitNoteType() {
        NotaDebitoUblData data = crearNotaDebitoMinima();

        DebitNoteType nd = builder.buildDebitNote(data);

        assertNotNull(nd, "buildDebitNote() debe retornar un DebitNoteType no nulo");
        assertEquals("ND01-1", nd.getID().getValue(),
                "El ID debe ser serie-correlativo");
        assertEquals("2.1", nd.getUBLVersionID().getValue(),
                "Debe tener versión UBL 2.1");
    }

    @Test
    void construirXml_returnsNonNullXmlString() throws Exception {
        NotaDebitoUblData data = crearNotaDebitoMinima();

        String xml = builder.construirXml(data);

        assertNotNull(xml, "construirXml() debe retornar un XML no nulo");
        assertFalse(xml.isBlank(), "El XML no debe estar vacío");
        assertTrue(xml.contains("<DebitNote"),
                "El XML debe contener el tag DebitNote");
    }

    @Test
    void buildDebitNote_andSerialize_equalsConstruirXml() throws Exception {
        NotaDebitoUblData data = crearNotaDebitoMinima();

        DebitNoteType nd = builder.buildDebitNote(data);
        String xmlFromSplit = builder.serializar(nd);
        String xmlOriginal = builder.construirXml(data);

        assertEquals(xmlOriginal, xmlFromSplit,
                "buildDebitNote() + serializar() debe producir el mismo XML que construirXml()");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private NotaDebitoUblData crearNotaDebitoMinima() {
        return new NotaDebitoUblData(
                "ND01",             // serie
                "1",                // correlativo
                "2026-01-15",       // fechaEmision
                "08",               // tipoDocumento
                "PEN",              // moneda
                "20123456789",      // emisorNroDocumento
                "EMPRESA SAC",      // emisorRazonSocial
                "12345678",         // receptorNroDocumento
                "CLIENTE FINAL",    // receptorRazonSocial
                "01",               // documentoAfectadoTipo
                "F001",             // documentoAfectadoSerie
                "100",              // documentoAfectadoNumero
                "01",               // codigoMotivo
                "Aumento de valor", // descripcionMotivo
                BigDecimal.ZERO,    // totalImpuestos
                BigDecimal.ZERO,    // importeTotal
                BigDecimal.ZERO,    // valorVenta
                null                // lineas
        );
    }
}
