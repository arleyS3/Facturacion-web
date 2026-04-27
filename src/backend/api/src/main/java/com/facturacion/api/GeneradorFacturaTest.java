package com.facturacion.api;

import com.facturacion.api.application.comprobante.ubl.builder.factura.FacturaUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.FacturaLineaUblData;
import com.facturacion.api.application.comprobante.ubl.mapper.factura.FacturaUblData;

import java.math.BigDecimal;
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
            
            System.out.println("=".repeat(60));
            System.out.println("FACTURA ELECTRÓNICA - XML UBL 2.1");
            System.out.println("=".repeat(60));
            System.out.println(xml);
            System.out.println("=".repeat(60));
            System.out.println("Longitud: " + xml.length() + " caracteres");
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea un {@link FacturaUblData} de ejemplo para pruebas manuales.
     *
     * @return datos de factura de prueba
     */
    private static FacturaUblData crearFacturaPrueba() {
        List<FacturaLineaUblData> lineas = List.of(
            new FacturaLineaUblData(
                1,
                "PRD-001",
                null,
                "Producto de prueba",
                new BigDecimal("2.00"),
                "NIU",
                new BigDecimal("50.00"),
                null,
                null,
                new BigDecimal("18.00"),
                new BigDecimal("18.00"),
                "1000",
                null,
                null,
                new BigDecimal("100.00"),
                null
            )
        );

        return new FacturaUblData(
            "F001",
            "1",
            "2026-04-26",
            null,
            "PEN",
            "01",
            "20123456789",
            "6",
            "MI EMPRESA SAC",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "PE",
            "67890123456",
            "1",
            "JUAN PEREZ",
            null,
            null,
            "PE",
            new BigDecimal("100.00"),
            null,
            null,
            null,
            new BigDecimal("18.00"),
            null,
            new BigDecimal("18.00"),
            new BigDecimal("118.00"),
            null,
            null,
            null,
            null,
            new BigDecimal("100.00"),
            lineas
        );
    }
}
