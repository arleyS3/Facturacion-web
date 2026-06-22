package com.facturacion.api.application.comprobante.ubl.builder.factura;

import com.facturacion.api.application.comprobante.ubl.mapper.factura.*;
import java.math.BigDecimal;
import java.util.List;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link FacturaUblBuilder}.
 * <p>
 * Cubre el nuevo método {@code buildInvoice()} que expone el objeto UBL
 * antes de serializar, y verifica que {@code construirXml()} siga funcionando.
 * </p>
 */
class FacturaUblBuilderTest {

    private FacturaUblBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new FacturaUblBuilder();
    }

    @Test
    void buildInvoice_returnsNonNullInvoiceType() {
        FacturaUblData data = crearFacturaMinima();

        InvoiceType invoice = builder.buildInvoice(data);

        assertNotNull(invoice, "buildInvoice() debe retornar un InvoiceType no nulo");
        assertEquals("F001-1", invoice.getID().getValue(),
                "El ID del comprobante debe ser serie-correlativo");
        assertEquals("2.1", invoice.getUBLVersionID().getValue(),
                "Debe tener versión UBL 2.1");
    }

    @Test
    void construirXml_returnsNonNullXmlString() throws Exception {
        FacturaUblData data = crearFacturaMinima();

        String xml = builder.construirXml(data);

        assertNotNull(xml, "construirXml() debe retornar un XML no nulo");
        assertFalse(xml.isBlank(), "El XML no debe estar vacío");
        assertTrue(xml.contains("<Invoice"),
                "El XML debe contener el tag Invoice");
    }

    @Test
    void buildInvoice_andSerialize_equalsConstruirXml() throws Exception {
        FacturaUblData data = crearFacturaMinima();

        InvoiceType invoice = builder.buildInvoice(data);
        String xmlFromSplit = builder.serializarFactura(invoice);
        String xmlOriginal = builder.construirXml(data);

        assertEquals(xmlOriginal, xmlFromSplit,
                "buildInvoice() + serializarFactura() debe producir el mismo XML que construirXml()");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Crea los datos mínimos necesarios para construir una factura.
     * Solo incluye encabezado y emisor; el resto se deja en null/listas vacías.
     */
    private FacturaUblData crearFacturaMinima() {
        DatosEncabezadoFacturaUbl encabezado = new DatosEncabezadoFacturaUbl(
                "F001",             // serie
                "1",                // correlativo
                "12:00:00",         // horaEmision
                "2026-01-15",       // fechaEmision
                null,               // fechaVencimiento
                "0101",             // tipoDeOperacion
                "PEN",              // moneda
                "01"                // tipoDocumento
        );

        DatosEmisorFacturaUbl emisor = new DatosEmisorFacturaUbl(
                "20123456789",      // nroDocumento
                "6",                // tipoDocumento
                "EMPRESA SAC",      // razonSocial
                null,               // nombreComercial
                null,               // direccion
                null,               // ubigeo
                null,               // urbanizacion
                null,               // departamento
                null,               // provincia
                null,               // distrito
                null,               // codigoPais
                "0001"              // codigoDomicilio
        );

        return new FacturaUblData(
                encabezado,         // encabezado
                emisor,             // emisor
                null,               // receptor
                null,               // firma
                null,               // referenciaOrden
                null,               // totalLineas
                null,               // descuentosGlobales
                null,               // totales
                null,               // impuestosTotales
                null,               // totalesMonetarios
                null,               // percepcionDetraccion
                null,               // lineas
                null,               // leyendas
                null,               // guiaRemisionId
                null,               // guiaRemisionCodigo
                null                // documentosAdicionales
        );
    }
}
