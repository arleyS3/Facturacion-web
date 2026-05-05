package com.facturacion.api;

import com.facturacion.api.application.comprobante.ubl.builder.factura.FacturaUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosEncabezadoFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosEmisorFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosPercepcionDetraccionFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosReceptorFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosDescuentoGlobalFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosDescuentoLineaFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosEsquemaImpuestoFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosFirmaFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosReferenciaOrdenFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosTotalesFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosTotalesMonetariosFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosPrecioReferenciaFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosCategoriaImpuestoFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosImpuestoSubtotalFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.DatosImpuestoTotalFacturaUbl;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.FacturaLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.FacturaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.LeyendaUblData;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main de prueba para generar XML de factura electrónica.
 *
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass=com.facturacion.api.GeneradorFacturaTest
 * }</pre>
 */
public class GeneradorFacturaTest {

    /**
     * Ejecuta la generación de XML con datos de prueba.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        try {
            var builder = new FacturaUblBuilder();
            FacturaUblData data = crearFacturaPrueba();
            String xml = builder.construirXml(data);
            
            // Guardar en archivo
            Path outputPath = Paths.get("src/main/resources/resultado/factura.xml");
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, xml);
            
            System.out.println("=".repeat(60));
            System.out.println("FACTURA ELECTRÓNICA - XML UBL 2.1");
            System.out.println("=".repeat(60));
            System.out.println("Archivo guardado: " + outputPath.toAbsolutePath());
            System.out.println("Longitud: " + xml.length() + " caracteres");
            System.out.println("=".repeat(60));
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea un {@link FacturaUblData} de ejemplo con datos reales.
     *
     * @return datos de factura de prueba
     */
    private static FacturaUblData crearFacturaPrueba() {
        // Variables para líneas con pricing reference y descuento
        DatosPrecioReferenciaFacturaUbl precioReferenciaLinea1 = new DatosPrecioReferenciaFacturaUbl(
            new BigDecimal("50.00"),
            "01"
        );

        DatosDescuentoLineaFacturaUbl descuentoLinea1 = new DatosDescuentoLineaFacturaUbl(
            false,
            "00",
            new BigDecimal("0.10"),
            new BigDecimal("5.00"),
            new BigDecimal("50.00")
        );

        List<FacturaLineaUblData> lineas = List.of(
new FacturaLineaUblData(
                1,                                      // numero
                "PROD-001",                               // codigoProducto
                "P001",                                  // codigoProductoSUNAT
                "Antibiótico GENÉRICO 500mg x 30 tablas",    // descripcion
                new BigDecimal("30.00"),                  // cantidad
                "NIU",                                    // unidadMedida
                new BigDecimal("50.00"),                  // precioUnitario
                precioReferenciaLinea1,                   // precioReferencia
                descuentoLinea1,                          // descuentoLinea
                null,                                     // precioUnitarioSinIGV
                new BigDecimal("1500.00"),                 // montoBaseIGV
                new BigDecimal("270.00"),                  // montoIGV
                new BigDecimal("18.00"),                   // porcentajeIGV
                "10",                                      // tipoAfectacionIGV
                null,                                      // montoISC
                null,                                      // tipoSistemaISC
                new BigDecimal("1500.00"),                 // valorVenta
                null                                      // valorVentaUnitario
            ),
            new FacturaLineaUblData(
                2,                                      // numero
                "PROD-002",                               // codigoProducto
                "P002",                                  // codigoProductoSUNAT
                "Jarabe para Tos 100ml",                   // descripcion
                new BigDecimal("5.00"),                   // cantidad
                "NIU",                                    // unidadMedida
                new BigDecimal("25.00"),                   // precioUnitario
                null,                                     // precioReferencia
                null,                                     // descuentoLinea
                null,                                     // precioUnitarioSinIGV
                new BigDecimal("125.00"),                  // montoBaseIGV
                new BigDecimal("22.50"),                   // montoIGV
                new BigDecimal("18.00"),                   // porcentajeIGV
                "10",                                     // tipoAfectacionIGV
                null,                                      // montoISC
                null,                                      // tipoSistemaISC
                new BigDecimal("125.00"),                  // valorVenta
                null                                      // valorVentaUnitario
            )
        );

        List<LeyendaUblData> leyendas = List.of(
            new LeyendaUblData("1000", "MIL SETECIENTOS NOVENTA Y DOS CON 50/100 SOLES")
        );

        DatosEncabezadoFacturaUbl encabezado = new DatosEncabezadoFacturaUbl(
            "F001",               // serie
            "1",                  // correlativo
            "10:30:00",           // horaEmision
            "2026-04-26",         // fechaEmision
            "2026-04-30",         // fechaVencimiento
            "0101",               // tipoDeOperacion
            "PEN",                // moneda
            "01"                  // tipoDocumento
        );

        DatosEmisorFacturaUbl emisor = new DatosEmisorFacturaUbl(
            "20123456789",        // emisorNroDocumento
            "6",                  // emisorTipoDocumento (RUC)
            "K&G ASOCIADOS S.A.",  // emisorRazonSocial
            "K&G LABORATORIOS",   // emisorNombreComercial
            "Av. Principal 123 - Lima - Lima",  // emisorDireccion
            "150101",             // emisorUbigeo
            "CENTRO COMERCIAL",   // emisorUrbanizacion
            "LIMA",              // emisorDepartamento
            "LIMA",              // emisorProvincia
            "LIMA",              // emisorDistrito
            "PE",                // emisorCodigoPais
            "0001"               // emisorCodigoDomicilio
        );

        DatosReceptorFacturaUbl receptor = new DatosReceptorFacturaUbl(
            "20445678901",        // receptorNroDocumento (RUC)
            "6",                  // receptorTipoDocumento (RUC)
            "CLIENTE PERU S.A.C.",// receptorRazonSocial
            "Av. Cliente 456 - Lima",  // receptorDireccion
            "150101",             // receptorUbigeo
            "PE"                  // receptorCodigoPais
        );

        DatosTotalesFacturaUbl totales = new DatosTotalesFacturaUbl(
            new BigDecimal("1625.00"),  // gravadas
            null,                 // exoneradas
            null,                 // inafectas
            null,                 // ISC
            new BigDecimal("292.50"),  // IGV (18%)
            null,                 // IVAP
            new BigDecimal("292.50"),   // totalImpuestos
            new BigDecimal("1917.50"), // importeTotal
            new BigDecimal("1625.00") // valorVenta
        );

        DatosTotalesMonetariosFacturaUbl totalesMonetarios = new DatosTotalesMonetariosFacturaUbl(
            new BigDecimal("1625.00"), // totalValorVenta
            new BigDecimal("1917.50"), // totalPrecioVenta
            null,                 // totalDescuentos
            new BigDecimal("1917.50") // importeTotalPagar
        );

        DatosEsquemaImpuestoFacturaUbl esquemaIgv = new DatosEsquemaImpuestoFacturaUbl(
            "1000",               // codigoTributo
            "IGV",                // nombreTributo
            "VAT"                 // codigoTipoTributo
        );

        DatosCategoriaImpuestoFacturaUbl categoriaIgv = new DatosCategoriaImpuestoFacturaUbl(
            "S",                  // codigoCategoria
            new BigDecimal("18.00"), // porcentaje
            "10",                 // codigoAfectacionIgv
            esquemaIgv
        );

        DatosImpuestoSubtotalFacturaUbl subtotalIgv = new DatosImpuestoSubtotalFacturaUbl(
            new BigDecimal("1625.00"), // montoBase
            new BigDecimal("292.50"),  // montoImpuesto
            categoriaIgv
        );

        DatosImpuestoTotalFacturaUbl impuestosTotales = new DatosImpuestoTotalFacturaUbl(
            new BigDecimal("292.50"),
            List.of(subtotalIgv)
        );

        DatosDescuentoGlobalFacturaUbl descuentoGlobal = new DatosDescuentoGlobalFacturaUbl(
            false,                // esCargo
            "00",                 // codigoMotivo
            new BigDecimal("0.05"), // porcentaje
            new BigDecimal("10.00"), // monto
            new BigDecimal("200.00") // montoBase
        );

        DatosFirmaFacturaUbl firma = new DatosFirmaFacturaUbl(
            "IDSignSP",          // id
            "20123456789",       // rucFirmante
            "K&G ASOCIADOS S.A.", // nombreFirmante
            "#SignatureSP"       // uriReferencia
        );

        DatosReferenciaOrdenFacturaUbl referenciaOrden = new DatosReferenciaOrdenFacturaUbl(
            "7852166"             // idOrden
        );

        DatosPercepcionDetraccionFacturaUbl percepcionDetraccion = new DatosPercepcionDetraccionFacturaUbl(
            null,                 // montoPercepcion
            null,                 // importePercepcion
            null,                 // montoDetraccion
            null                  // codigoDetraccion
        );

        return new FacturaUblData(
            encabezado,
            emisor,
            receptor,
            firma,
            referenciaOrden,
            lineas.size(),
            List.of(descuentoGlobal),
            totales,
            impuestosTotales,
            totalesMonetarios,
            percepcionDetraccion,
            lineas,               // lineas
            leyendas             // leyendas
        );
    }
}
