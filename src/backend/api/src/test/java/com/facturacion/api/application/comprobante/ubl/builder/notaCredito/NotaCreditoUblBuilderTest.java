package com.facturacion.api.application.comprobante.ubl.builder.notaCredito;

import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.NotaCreditoLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.notaCredito.NotaCreditoUblData;
import java.math.BigDecimal;
import java.util.List;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link NotaCreditoUblBuilder}.
 * <p>
 * Cubre el nuevo método {@code buildCreditNote()} que expone el objeto UBL
 * antes de serializar, y verifica que {@code construirXml()} siga funcionando.
 * </p>
 */
class NotaCreditoUblBuilderTest {

    private NotaCreditoUblBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new NotaCreditoUblBuilder();
    }

    @Test
    void buildCreditNote_returnsNonNullCreditNoteType() {
        NotaCreditoUblData data = crearNotaCreditoMinima();

        CreditNoteType nc = builder.buildCreditNote(data);

        assertNotNull(nc, "buildCreditNote() debe retornar un CreditNoteType no nulo");
        assertEquals("NC01-1", nc.getID().getValue(),
                "El ID debe ser serie-correlativo");
        assertEquals("2.1", nc.getUBLVersionID().getValue(),
                "Debe tener versión UBL 2.1");
    }

    @Test
    void construirXml_returnsNonNullXmlString() throws Exception {
        NotaCreditoUblData data = crearNotaCreditoMinima();

        String xml = builder.construirXml(data);

        assertNotNull(xml, "construirXml() debe retornar un XML no nulo");
        assertFalse(xml.isBlank(), "El XML no debe estar vacío");
        assertTrue(xml.contains("<CreditNote"),
                "El XML debe contener el tag CreditNote");
    }

    @Test
    void buildCreditNote_andSerialize_equalsConstruirXml() throws Exception {
        NotaCreditoUblData data = crearNotaCreditoMinima();

        CreditNoteType nc = builder.buildCreditNote(data);
        String xmlFromSplit = builder.serializar(nc);
        String xmlOriginal = builder.construirXml(data);

        assertEquals(xmlOriginal, xmlFromSplit,
                "buildCreditNote() + serializar() debe producir el mismo XML que construirXml()");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private NotaCreditoUblData crearNotaCreditoMinima() {
        return new NotaCreditoUblData(
                "NC01",             // serie
                "1",                // correlativo
                "2026-01-15",       // fechaEmision
                "07",               // tipoDocumento
                "PEN",              // moneda
                "20123456789",      // emisorNroDocumento
                "EMPRESA SAC",      // emisorRazonSocial
                "0001",             // emisorCodigoDomicilio
                "12345678",         // receptorNroDocumento
                "6",                // receptorTipoDocumento
                "CLIENTE FINAL",    // receptorRazonSocial
                "01",               // documentoAfectadoTipo
                "F001",             // documentoAfectadoSerie
                "100",              // documentoAfectadoNumero
                "01",               // codigoMotivo
                "Anulación",        // descripcionMotivo
                BigDecimal.ZERO,    // gravadas
                BigDecimal.ZERO,    // exoneradas
                BigDecimal.ZERO,    // inafectas
                BigDecimal.ZERO,    // igv
                BigDecimal.ZERO,    // totalImpuestos
                BigDecimal.ZERO,    // importeTotal
                BigDecimal.ZERO,    // valorVenta
                null,               // guiaRemisionId
                null,               // guiaRemisionCodigo
                null,               // documentosAdicionales
                null                // lineas
        );
    }
}
