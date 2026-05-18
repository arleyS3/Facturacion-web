package com.facturacion.api.application.comprobante.ubl.mapper.factura;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.modelo.DescuentoGlobalCanonico;
import com.facturacion.api.application.comprobante.modelo.DetalleCanonico;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link FacturaUblMapper}.
 * <p>
 * Cubre el mapeo de descuentos globales desde el modelo canónico
 * hasta los datos UBL de factura.
 * </p>
 */
class FacturaUblMapperTest {

    private FacturaUblMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FacturaUblMapper();
    }

    // =========================================================================
    // toDatosDescuentoGlobal — mapeo de un DescuentoGlobalCanonico individual
    // =========================================================================

    @Test
    void toDatosDescuentoGlobal_singleItem_mapsAllFieldsCorrectly() {
        DescuentoGlobalCanonico canonico = new DescuentoGlobalCanonico(
                false,
                "00",
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                new BigDecimal("500.00")
        );

        DatosDescuentoGlobalFacturaUbl result = mapper.toDatosDescuentoGlobal(canonico);

        assertFalse(result.esCargo());
        assertEquals("00", result.codigoMotivo());
        assertEquals(0, new BigDecimal("10.00").compareTo(result.porcentaje()),
                "porcentaje debe ser 10.00");
        assertEquals(0, new BigDecimal("50.00").compareTo(result.monto()),
                "monto debe ser 50.00");
        assertEquals(0, new BigDecimal("500.00").compareTo(result.montoBase()),
                "montoBase debe ser 500.00");
    }

    // =========================================================================
    // fromCanonico — integración: descuentos globales en el flujo completo
    // =========================================================================

    @Test
    void fromCanonico_nullDescuentosGlobales_returnsEmptyDescuentosList() {
        ComprobanteCanonico canonico = crearCanonicoBase(null);

        FacturaUblData data = mapper.fromCanonico(canonico);

        assertNotNull(data.descuentosGlobales());
        assertTrue(data.descuentosGlobales().isEmpty(),
                "descuentosGlobales debe ser lista vacía cuando canonico.descuentosGlobales() es null");
    }

    @Test
    void fromCanonico_conDescuentosGlobales_losIncluyeEnFacturaUblData() {
        List<DescuentoGlobalCanonico> descuentos = List.of(
                new DescuentoGlobalCanonico(false, "00",
                        new BigDecimal("10.00"), new BigDecimal("100.00"),
                        new BigDecimal("1000.00")),
                new DescuentoGlobalCanonico(false, "00",
                        new BigDecimal("5.00"), new BigDecimal("50.00"),
                        new BigDecimal("1000.00"))
        );
        ComprobanteCanonico canonico = crearCanonicoBase(descuentos);

        FacturaUblData data = mapper.fromCanonico(canonico);

        assertNotNull(data.descuentosGlobales());
        assertEquals(2, data.descuentosGlobales().size(),
                "debe contener 2 descuentos globales");

        DatosDescuentoGlobalFacturaUbl primero = data.descuentosGlobales().get(0);
        assertFalse(primero.esCargo());
        assertEquals("00", primero.codigoMotivo());
        assertEquals(0, new BigDecimal("100.00").compareTo(primero.monto()),
                "primer descuento monto = 100.00");
    }

    @Test
    void fromCanonico_conDescuentos_importeTotalPagarRestaDescuentos() {
        List<DescuentoGlobalCanonico> descuentos = List.of(
                new DescuentoGlobalCanonico(false, "00",
                        new BigDecimal("10.00"), new BigDecimal("100.00"),
                        new BigDecimal("1000.00"))
        );
        ComprobanteCanonico canonico = crearCanonicoBase(descuentos);

        FacturaUblData data = mapper.fromCanonico(canonico);

        DatosTotalesMonetariosFacturaUbl totales = data.totalesMonetarios();
        // totalPrecioVenta (con IGV 18%) para 1 item de 100.00 = 118.00
        // descuento = 100.00 → importeTotalPagar = 118.00 - 100.00 = 18.00
        assertEquals(0, new BigDecimal("100.00").compareTo(totales.totalDescuentos()),
                "totalDescuentos debe ser 100.00");
        assertEquals(0, new BigDecimal("18.00").compareTo(totales.importeTotalPagar()),
                "importeTotalPagar = totalPrecioVenta - descuentos = 118.00 - 100.00 = 18.00");
    }

    @Test
    void fromCanonico_sinDescuentos_importeTotalPagarNoCambia() {
        // descuentos null
        ComprobanteCanonico canonicoSinDescuentos = crearCanonicoBase(null);

        FacturaUblData data = mapper.fromCanonico(canonicoSinDescuentos);

        DatosTotalesMonetariosFacturaUbl totales = data.totalesMonetarios();
        assertEquals(0, BigDecimal.ZERO.compareTo(totales.totalDescuentos()),
                "totalDescuentos debe ser 0 cuando no hay descuentos");
        assertEquals(0, totales.totalPrecioVenta().compareTo(totales.importeTotalPagar()),
                "sin descuentos, importeTotalPagar debe ser igual a totalPrecioVenta");
    }

    // =========================================================================
    // Helpers de construcción
    // =========================================================================

    /**
     * Crea un {@link ComprobanteCanonico} mínimo con un detalle gravado
     * de 100.00 (valorUnitario) * 1 (cantidad) = 100.00 valorVenta,
     * IGV 18.00 → totalPrecioVenta = 118.00.
     */
    private ComprobanteCanonico crearCanonicoBase(
            List<DescuentoGlobalCanonico> descuentosGlobales) {
        DetalleCanonico detalle = new DetalleCanonico(
                "PROD001",               // codigoProducto
                "Producto de prueba",    // descripcion
                new BigDecimal("1"),     // cantidad
                new BigDecimal("100.00"), // valorUnitario
                new BigDecimal("18.00"),   // igv
                "10",                    // codigoTipoIgv (10 = Gravado)
                "NIU"                    // unidadMedida
        );

        return new ComprobanteCanonico(
                "01",                   // tipoDocumento
                "F001-00000001",        // numero
                "2026-05-18",           // fechaEmision
                null,                   // horaEmision
                null,                   // fechaVencimiento
                null,                   // tipoDeOperacion
                "PEN",                  // moneda
                "20123456789",          // emisorRuc
                "EMPRESA SAC",          // emisorRazonSocial
                null,                   // emisorNombreComercial
                null,                   // emisorDireccion
                null,                   // emisorUrbanizacion
                null,                   // emisorDepartamento
                null,                   // emisorProvincia
                null,                   // emisorDistrito
                null,                   // emisorUbigeo
                null,                   // emisorCodigoDomicilio
                "12345678",             // receptorDocumento
                null,                   // receptorRazonSocial
                null,                   // receptorDireccion
                null,                   // receptorUbigeo
                List.of(detalle),       // detalles
                null,                   // documentoRelacionado
                null,                   // parteTraslado
                null,                   // leyendas
                descuentosGlobales,     // descuentosGlobales
                null                    // firmar
        );
    }
}
