package com.facturacion.api.application.comprobante.ubl.builder.boleta;

import com.facturacion.api.application.comprobante.ubl.mapper.boleta.BoletaLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.boleta.BoletaUblData;
import java.math.BigDecimal;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link BoletaUblBuilder}.
 * <p>
 * Cubre el nuevo método {@code buildInvoice()} que expone el objeto UBL
 * antes de serializar, y verifica que {@code construirXml()} siga funcionando.
 * </p>
 */
class BoletaUblBuilderTest {

    private BoletaUblBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new BoletaUblBuilder();
    }

    @Test
    void buildInvoice_returnsNonNullInvoiceType() {
        BoletaUblData data = crearBoletaMinima();

        InvoiceType invoice = builder.buildInvoice(data);

        assertNotNull(invoice, "buildInvoice() debe retornar un InvoiceType no nulo");
        assertEquals("B001-1", invoice.getID().getValue(),
                "El ID del comprobante debe ser serie-correlativo");
        assertEquals("2.1", invoice.getUBLVersionID().getValue(),
                "Debe tener versión UBL 2.1");
    }

    @Test
    void construirXml_returnsNonNullXmlString() throws Exception {
        BoletaUblData data = crearBoletaMinima();

        String xml = builder.construirXml(data);

        assertNotNull(xml, "construirXml() debe retornar un XML no nulo");
        assertFalse(xml.isBlank(), "El XML no debe estar vacío");
        assertTrue(xml.contains("<Invoice"),
                "El XML debe contener el tag Invoice");
    }

    @Test
    void buildInvoice_andSerialize_equalsConstruirXml() throws Exception {
        BoletaUblData data = crearBoletaMinima();

        InvoiceType invoice = builder.buildInvoice(data);
        String xmlFromSplit = builder.serializar(invoice, data);
        String xmlOriginal = builder.construirXml(data);

        assertEquals(xmlOriginal, xmlFromSplit,
                "buildInvoice() + serializar() debe producir el mismo XML que construirXml()");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private BoletaUblData crearBoletaMinima() {
        return new BoletaUblData(
                "B001",             // serie
                "1",                // correlativo
                "2026-01-15",       // fechaEmision
                "12:00:00",         // horaEmision
                "PEN",              // moneda
                "20123456789",      // emisorNroDocumento
                "6",                // emisorTipoDocumento
                "EMPRESA SAC",      // emisorRazonSocial
                "12345678",         // receptorNroDocumento
                "1",                // receptorTipoDocumento
                "CLIENTE FINAL",    // receptorRazonSocial
                new BigDecimal("100.00"),  // gravadas
                BigDecimal.ZERO,    // exoneradas
                BigDecimal.ZERO,    // inafectas
                new BigDecimal("18.00"),   // IGV
                new BigDecimal("18.00"),   // totalImpuestos
                new BigDecimal("118.00"),  // importeTotal
                new BigDecimal("100.00"),  // valorVenta
                null,               // lineas
                null,               // leyendas
                false,              // tieneDescuentoGlobal
                null,               // descuentoMonto
                null,               // descuentoBase
                null,               // guiaRemisionId
                null,               // guiaRemisionCodigo
                null,               // documentosAdicionales
                null,               // anticipoId
                null,               // anticipoTipoDoc
                null,               // anticipoMonto
                null,               // anticipoMoneda
                null                // anticipoRucEmisor
        );
    }
}
