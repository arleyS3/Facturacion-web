/**
 * Clase de prueba para generación de facturas UBL 2.1.
 * 
 * <p>
 * Esta clase permite generar un XML de factura electrónica en formato UBL 2.1
 * para probar el flujo completo de construcción del documento. Genera un
 * archivo XML en {@code src/main/resources/resultado/factura.xml}.
 * </p>
 * 
 * <p>
 * <b>Uso:</b> Ejecutar el método {@code main()} directamente. No requiere
 * configuración adicional ya que usa datos de prueba embebidos.
 * </p>
 * 
 * <p>
 * <b>Nota:</b> Esta clase es solo para pruebas de desarrollo. En producción,
 * los comprobantes se generan a través del endpoint REST:
 * {@code POST /api/v1/comprobantes/generar-xml}
 * </p>
 * 
 * @author Sistema de Facturación
 * @version 1.0.0
 * @since 1.0.0
 * @see FacturaUblBuilder
 * @see com.facturacion.api.web.controller.ComprobanteController
 */
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
     * Crea un {@link FacturaUblData} de ejemplo basado en FacturaExampleReal.xml.
     * Incluye 3 tipos de operaciones: gravada, exonerada e inafecta.
     *
     * @return datos de factura de prueba
     */
    private static FacturaUblData crearFacturaPrueba() {
        // =====================================================
        // LÍNEAS DE DETALLE (5 líneas: 3 gravadas, 1 exonerada, 1 inafecta)
        // =====================================================

        // Línea 1: Gravada - 2000 unidades, precio 98.00 (con IGV), descuento 10%
        // Precio sin IGV: 83.05, IGV: 14.95, Valor venta: 149491.53
        DatosPrecioReferenciaFacturaUbl precioRefLinea1 = new DatosPrecioReferenciaFacturaUbl(
            new BigDecimal("98.00"),  // precio con IGV
            "01"                      // tipo precio: unitario
        );
        DatosDescuentoLineaFacturaUbl descuentoLinea1 = new DatosDescuentoLineaFacturaUbl(
            false,                    // esCargo = false (descuento)
            "00",                     // código motivo
            new BigDecimal("0.10"),   // 10% descuento
            new BigDecimal("16610.17"), // monto descuento
            new BigDecimal("166101.69") // monto base
        );

        FacturaLineaUblData linea1 = new FacturaLineaUblData(
            1,                          // numero
            "GLG199",                   // codigoProducto ( SellersItemIdentification )
            "52161515",                 // codigoProductoSUNAT ( UNSPSC )
            "Grabadora LG Externo Modelo: GE20LU10", // descripcion
            new BigDecimal("2000.00"),  // cantidad
            "NIU",                      // unidadMedida
            new BigDecimal("83.05"),     // precioUnitario (sin IGV)
            precioRefLinea1,            // precioReferencia
            descuentoLinea1,            // descuentoLinea
            new BigDecimal("83.05"),    // precioUnitarioSinIGV
            new BigDecimal("149491.53"), // montoBaseIGV
            new BigDecimal("26908.47"), // montoIGV
            new BigDecimal("18.00"),    // porcentajeIGV
            "10",                       // tipoAfectacionIGV (Gravado)
            null,                       // montoISC
            null,                       // tipoSistemaISC
            new BigDecimal("149491.53"), // valorVenta
            null                        // valorVentaUnitario
        );

        // Línea 2: Gravada - 300 unidades, precio 620.00 (con IGV), descuento 15%
        // Precio sin IGV: 525.42, IGV: 94.58, Valor venta: 133983.05
        DatosPrecioReferenciaFacturaUbl precioRefLinea2 = new DatosPrecioReferenciaFacturaUbl(
            new BigDecimal("620.00"),
            "01"
        );
        DatosDescuentoLineaFacturaUbl descuentoLinea2 = new DatosDescuentoLineaFacturaUbl(
            false,
            "00",
            new BigDecimal("0.15"),
            new BigDecimal("23644.07"),
            new BigDecimal("157627.12")
        );

        FacturaLineaUblData linea2 = new FacturaLineaUblData(
            2,
            "MVS546",
            "43211902",
            "Monitor LCD ViewSonic VG2028WM 20",
            new BigDecimal("300.00"),
            "NIU",
            new BigDecimal("525.42"),
            precioRefLinea2,
            descuentoLinea2,
            new BigDecimal("525.42"),
            new BigDecimal("133983.05"),
            new BigDecimal("24116.95"),
            new BigDecimal("18.00"),
            "10",                       // Gravado
            null,
            null,
            new BigDecimal("133983.05"),
            null
        );

        // Línea 3: EXONERADA - 250 unidades, precio 52.00 (con IGV), sin descuento
        // No tiene IGV, código 20 = Exonerado
        DatosPrecioReferenciaFacturaUbl precioRefLinea3 = new DatosPrecioReferenciaFacturaUbl(
            new BigDecimal("52.00"),
            "01"
        );

        FacturaLineaUblData linea3 = new FacturaLineaUblData(
            3,
            "MPC35",
            "43202010",
            "Memoria DDR-3 B1333 Kingston",
            new BigDecimal("250.00"),
            "NIU",
            new BigDecimal("52.00"),    // precio incluye IGV pero está exonerado
            precioRefLinea3,
            null,                        // sin descuento
            new BigDecimal("52.00"),
            new BigDecimal("13000.00"), // montoBase (valor venta)
            new BigDecimal("0.00"),      // montoIGV = 0 (exonerado)
            new BigDecimal("18.00"),
            "20",                       // tipoAfectacionIGV (Exonerado)
            null,
            null,
            new BigDecimal("13000.00"),
            null
        );

        // Línea 4: Gravada - 500 unidades, precio 196.00 (con IGV), sin descuento
        // Precio sin IGV: 166.10, IGV: 29.90, Valor venta: 83050.85
        DatosPrecioReferenciaFacturaUbl precioRefLinea4 = new DatosPrecioReferenciaFacturaUbl(
            new BigDecimal("196.00"),
            "01"
        );

        FacturaLineaUblData linea4 = new FacturaLineaUblData(
            4,
            "TMS22",
            "43211706",
            "Teclado Microsoft SideWinder X6",
            new BigDecimal("500.00"),
            "NIU",
            new BigDecimal("166.10"),
            precioRefLinea4,
            null,
            new BigDecimal("166.10"),
            new BigDecimal("83050.85"),
            new BigDecimal("14949.15"),
            new BigDecimal("18.00"),
            "10",                       // Gravado
            null,
            null,
            new BigDecimal("83050.85"),
            null
        );

        // Línea 5: INAFECTA (operación gratuita) - 1 unidad, valor referencial 30.00
        // Código 31 = Inafecto - Operaciones gratuitas
        DatosPrecioReferenciaFacturaUbl precioRefLinea5 = new DatosPrecioReferenciaFacturaUbl(
            new BigDecimal("30.00"),
            "02"                       // tipo precio: valor referencial
        );

        FacturaLineaUblData linea5 = new FacturaLineaUblData(
            5,
            "WCG01",
            "45121520",
            "Web cam Genius iSlim 310VVU",
            new BigDecimal("1.00"),
            "NIU",
            new BigDecimal("0.00"),     // precio = 0 (gratuito)
            precioRefLinea5,            // precioReferencia con tipo 02
            null,
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("18.00"),
            "31",                       // tipoAfectacionIGV (Inafecto - Gratuito)
            null,
            null,
            new BigDecimal("0.00"),     // valorVenta = 0
            null
        );

        List<FacturaLineaUblData> lineas = List.of(linea1, linea2, linea3, linea4, linea5);

        // =====================================================
        // LEYENDAS
        // =====================================================
        List<LeyendaUblData> leyendas = List.of(
            new LeyendaUblData("1000", "CUATROCIENTOS VEINTITRES MIL DOSCIENTOS VEINTICINCO Y 00/100"),
            new LeyendaUblData("3000", "0501002017051400452")  // código interno
        );

        // =====================================================
        // ENCABEZADO
        // =====================================================
        DatosEncabezadoFacturaUbl encabezado = new DatosEncabezadoFacturaUbl(
            "F001",               // serie
            "4355",               // correlativo
            "13:25:51",           // horaEmision
            "2017-05-14",         // fechaEmision
            null,                 // fechaVencimiento (opcional)
            "0101",               // tipoDeOperacion (Venta interna)
            "PEN",                // moneda
            "01"                  // tipoDocumento (Factura)
        );

        // =====================================================
        // EMISOR
        // =====================================================
        DatosEmisorFacturaUbl emisor = new DatosEmisorFacturaUbl(
            "20100454523",        // emisorNroDocumento (RUC)
            "6",                  // emisorTipoDocumento (RUC)
            "Soporte Tecnológicos EIRL",  // emisorRazonSocial
            "Tu Soporte",         // emisorNombreComercial
            null,                 // emisorDireccion
            null,                 // emisorUbigeo
            null,                 // emisorUrbanizacion
            null,                 // emisorDepartamento
            null,                 // emisorProvincia
            null,                 // emisorDistrito
            "PE",                 // emisorCodigoPais
            "0000"                // emisorCodigoDomicilio
        );

        // =====================================================
        // RECEPTOR
        // =====================================================
        DatosReceptorFacturaUbl receptor = new DatosReceptorFacturaUbl(
            "20587896411",        // receptorNroDocumento (RUC)
            "6",                  // receptorTipoDocumento (RUC)
            "SERVICABINAS S.A.",  // receptorRazonSocial
            null,                 // receptorDireccion
            null,                 // receptorUbigeo
            "PE"                  // receptorCodigoPais
        );

        // =====================================================
        // TOTALES (basados en FacturaExampleReal.xml)
        // =====================================================
        // Gravadas: 348199.15, Exoneradas: 12350.00, Inafectas: 30.00
        // IGV: 62675.85, Total impuestos: 62675.85
        // Valor venta: 419779.66, Importe total: 423225.00
        DatosTotalesFacturaUbl totales = new DatosTotalesFacturaUbl(
            new BigDecimal("348199.15"), // gravadas
            new BigDecimal("12350.00"),  // exoneradas
            new BigDecimal("30.00"),     // inafectas
            null,                        // ISC
            new BigDecimal("62675.85"),  // IGV
            null,                        // IVAP
            new BigDecimal("62675.85"),  // totalImpuestos
            new BigDecimal("423225.00"), // importeTotal
            new BigDecimal("419779.66") // valorVenta
        );

        // =====================================================
        // TOTALES MONETARIOS
        // =====================================================
        DatosTotalesMonetariosFacturaUbl totalesMonetarios = new DatosTotalesMonetariosFacturaUbl(
            new BigDecimal("419779.66"), // totalValorVenta
            new BigDecimal("423225.00"), // totalPrecioVenta
            new BigDecimal("59230.51"),  // totalDescuentos (18976.27 global + otros)
            new BigDecimal("423225.00")  // importeTotalPagar
        );

        // =====================================================
        // DESCUENTO GLOBAL
        // =====================================================
        DatosDescuentoGlobalFacturaUbl descuentoGlobal = new DatosDescuentoGlobalFacturaUbl(
            false,                    // esCargo = false (descuento)
            "00",                     // código motivo
            new BigDecimal("0.05"),   // 5% descuento
            new BigDecimal("18976.27"), // monto
            new BigDecimal("379525.42") // monto base
        );

        // =====================================================
        // FIRMA
        // =====================================================
        DatosFirmaFacturaUbl firma = new DatosFirmaFacturaUbl(
            "IDSignSP",              // id
            "20100454523",           // rucFirmante
            "SOPORTE TECNOLOGICO EIRL", // nombreFirmante
            "#SignatureSP"           // uriReferencia
        );

        // =====================================================
        // REFERENCIA ORDEN
        // =====================================================
        DatosReferenciaOrdenFacturaUbl referenciaOrden = new DatosReferenciaOrdenFacturaUbl(
            "7852166"                // idOrden
        );

        // =====================================================
        // PERCEPCIÓN / DETRACCIÓN (opcional)
        // =====================================================
        DatosPercepcionDetraccionFacturaUbl percepcionDetraccion = new DatosPercepcionDetraccionFacturaUbl(
            null,                     // montoPercepcion
            null,                     // importePercepcion
            null,                     // montoDetraccion
            null                      // codigoDetraccion
        );

        return new FacturaUblData(
            encabezado,
            emisor,
            receptor,
            firma,
            referenciaOrden,
            5,                      // totalLineas
            List.of(descuentoGlobal),
            totales,
            null,                   // impuestosTotales (usamos totales directamente)
            totalesMonetarios,
            percepcionDetraccion,
            lineas,
            leyendas
        );
    }
}
