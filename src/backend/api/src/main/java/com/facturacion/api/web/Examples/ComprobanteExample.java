package com.facturacion.api.web.Examples;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * Ejemplos de requests para generación de XML UBL 2.1.
 *
 * <p>
 * Usados en la documentación Swagger de ComprobanteController.
 * </p>
 *
 * <h2>Formato:</h2>
 * Los ejemplos usan nombres de campo en snake_case para mejor compatibilidad
 * con estándares JSON. El backend también acepta camelCase.
 *
 * <h2>Catálogos de referencia:</h2>
 * <ul>
 * <li>Tipo documento: Catálogo 01 (01=Factura, 03=Boleta, 07=Nota Crédito,
 * 08=Nota Débito)</li>
 * <li>Tipo operación: Catálogo 17 (0101=Venta interna)</li>
 * <li>Código afectación IGV: Catálogo 07 (10=Gravado, 20=Exonerado,
 * 30/31=Inafecto)</li>
 * <li>Leyendas: Catálogo 52 (1000=Monto en letras, 3000=Código interno)</li>
 * </ul>
 */
public final class ComprobanteExample {

    private ComprobanteExample() {
        // Utility class
    }

    // =============================================================================
    // FACTURA ELECTRÓNICA (01) - OPERACIÓN GRAVADA
    // =============================================================================

    /**
     * Ejemplo request - Factura Electrónica con operación gravada.
     *
     * <p>
     * Genera XML UBL 2.1 para Factura con IGV 18%.
     * El mapper calcula automáticamente: valorVenta, montoBaseIGV, totales.
     * </p>
     */
    public static final String FACTURA_REQUEST = """
            {
                "tipo_documento": "01",
                "numero": "F001-1",
                "fecha_emision": "2026-05-01",
                "hora_emision": "10:30:00",
                "fecha_vencimiento": "2026-05-30",
                "tipo_de_operacion": "0101",
                "moneda": "PEN",
                "emisor_ruc": "20123456789",
                "emisor_razonSocial": "K&G ASOCIADOS S.A.C.",
                "emisor_nombreComercial": "K&G LABORATORIOS",
                "emisor_direccion": "Av. Principal 123 - Lima - Lima",
                "emisor_urbanizacion": "Centro Comercial",
                "emisor_departamento": "LIMA",
                "emisor_provincia": "LIMA",
                "emisor_distrito": "LIMA",
                "emisor_ubigeo": "150101",
                "emisor_codigo_domicilio": "0001",
                "receptor_documento": "20445678901",
                "receptor_razonSocial": "CLIENTE PERU S.A.C.",
                "receptor_direccion": "Av. Cliente 456 - Lima",
                "receptor_ubigeo": "150101",
                "detalles": [
                    {
                        "codigo_producto": "MED001",
                        "descripcion": "Antibiótico GENÉRICO 500mg x 30 tablas",
                        "cantidad": 30.00,
                        "valor_unitario": 50.00,
                        "igv": 270.00,
                        "codigo_tipoIgv": "10"
                    },
                    {
                        "codigo_producto": "MED002",
                        "descripcion": "Paracetamol 500mg x 100 tabletas",
                        "cantidad": 5.00,
                        "valor_unitario": 25.00,
                        "igv": 22.50,
                        "codigo_tipoIgv": "10"
                    }
                ],
                "leyendas": [
                    {
                        "codigo_local": "1000",
                        "leyenda": "MIL SETECIENTOS NOVENTA Y DOS CON 50/100 SOLES"
                    }
                ]
            }
            """;

    // =============================================================================
    // BOLETA DE VENTA (03)
    // =============================================================================

    /**
     * Ejemplo request - Boleta de Venta.
     *
     * <p>
     * Para boletas, el receptor puede ser DNI (documento válido).
     * El modelo es igual a la factura.
     * </p>
     */
    public static final String BOLETA_REQUEST = """
            {
                "tipoDocumento": "03",
                "numero": "B001-1",
                "fechaEmision": "2026-05-01",
                "horaEmision": "11:00:00",
                "moneda": "PEN",
                "emisorRuc": "20123456789",
                "emisorRazonSocial": "K&G ASOCIADOS S.A.C.",
                "emisorDireccion": "Av. Principal 123 - Lima",
                "emisorUbigeo": "150101",
                "emisorCodigoDomicilio": "0001",
                "receptorDocumento": "12345678",
                "receptorRazonSocial": "Consumidor Final",
                "detalles": [
                    {
                        "codigoProducto": "MED002",
                        "descripcion": "Paracetamol 500mg x 100 tabletas",
                        "cantidad": 2.00,
                        "valorUnitario": 15.00,
                        "igv": 5.40,
                        "codigoTipoIgv": "10"
                    }
                ]
            }
            """;

    // =============================================================================
    // NOTA DE CRÉDITO (07)
    // =============================================================================

    /**
     * Ejemplo request - Nota de Crédito.
     *
     * <p>
     * Tipos: 01=Anulación de operación, 02=Anulación por error de RUC, etc.
     * Catálogo 09 SUNAT.
     * </p>
     */
    public static final String NOTA_CREDITO_REQUEST = """
            {
                "tipoDocumento": "07",
                "numero": "F001-2",
                "fechaEmision": "2026-05-01",
                "horaEmision": "12:00:00",
                "moneda": "PEN",
                "emisorRuc": "20123456789",
                "emisorRazonSocial": "K&G ASOCIADOS S.A.C.",
                "emisorDireccion": "Av. Principal 123 - Lima",
                "emisorUbigeo": "150101",
                "emisorCodigoDomicilio": "0001",
                "receptorDocumento": "20445678901",
                "receptorRazonSocial": "CLIENTE PERU S.A.C.",
                "documentoRelacionado": {
                    "tipoDocumento": "01",
                    "numeroDocumento": "F001-1",
                    "codigoMotivo": "01"
                },
                "detalles": [
                    {
                        "descripcion": "Descuento por artículo defectuoso",
                        "cantidad": 1.00,
                        "valorUnitario": -50.00,
                        "igv": -9.00,
                        "codigoTipoIgv": "10"
                    }
                ]
            }
            """;

    // =============================================================================
    // NOTA DE DÉBITO (08)
    // =============================================================================

    /**
     * Ejemplo request - Nota de Débito.
     *
     * <p>
     * Tipos: 01=Intereses por mora, 02=Aumento en valor, etc.
     * Catálogo 10 SUNAT.
     * </p>
     */
    public static final String NOTA_DEBITO_REQUEST = """
            {
                "tipoDocumento": "08",
                "numero": "F001-3",
                "fechaEmision": "2026-05-02",
                "horaEmision": "14:00:00",
                "moneda": "PEN",
                "emisorRuc": "20123456789",
                "emisorRazonSocial": "K&G ASOCIADOS S.A.C.",
                "emisorDireccion": "Av. Principal 123 - Lima",
                "emisorUbigeo": "150101",
                "emisorCodigoDomicilio": "0001",
                "receptorDocumento": "20445678901",
                "receptorRazonSocial": "CLIENTE PERU S.A.C.",
                "documentoRelacionado": {
                    "tipoDocumento": "01",
                    "numeroDocumento": "F001-1",
                    "codigoMotivo": "02"
                },
                "detalles": [
                    {
                        "descripcion": "Recargo por mora de pago",
                        "cantidad": 1.00,
                        "valorUnitario": 25.00,
                        "igv": 4.50,
                        "codigoTipoIgv": "10"
                    }
                ]
            }
            """;

    // =============================================================================
    // GUÍA DE REMISIÓN (09)
    // =============================================================================

    /**
     * Ejemplo request - Guía de Remisión Electrónica.
     *
     * <p>
     * Para transporte de bienes. Tipo traslado: 01=Transporte público,
     * 02=Transporte privado.
     * Modalidad: 01=Por cuenta propia, 02=Por cuenta de terceros.
     * </p>
     */
    public static final String GUIA_REMISION_REQUEST = """
            {
                "tipoDocumento": "09",
                "numero": "T001-1",
                "fechaEmision": "2026-05-01",
                "horaEmision": "08:00:00",
                "moneda": "PEN",
                "emisorRuc": "20123456789",
                "emisorRazonSocial": "K&G ASOCIADOS S.A.C.",
                "emisorDireccion": "Av. Principal 123 - Lima",
                "emisorUbigeo": "150101",
                "emisorCodigoDomicilio": "0001",
                "receptorDocumento": "20445678901",
                "receptorRazonSocial": "DISTRIBUIDORA ABC S.A.C.",
                "receptorDireccion": "Av. Receiver 789 - Callao",
                "receptorUbigeo": "150102",
                "parteTraslado": {
                    "tipoTraslado": "01",
                    "modalidadTraslado": "01",
                    "pesoBrutoTotal": 25.50,
                    "undPesoTotal": "KGM",
                    "numeroBultos": 3
                },
                "detalles": [
                    {
                        "descripcion": "Cajas de medicamentos",
                        "cantidad": 10.00,
                        "codigoProducto": "CAJA001",
                        "igv": 0.00,
                        "codigoTipoIgv": "30"
                    }
                ]
            }
            """;

    // =============================================================================
    // FACTURA CON TRES TIPOS DE OPERACIÓN (GRAVADA, EXONERADA, INAFECTA)
    // =============================================================================

    /**
     * Ejemplo request - Factura con operaciones gravadas, exoneradas e inafectas.
     *
     * <p>
     * Este ejemplo muestra una factura con los tres tipos de operación IGV:
     * - Línea 1-3: Gravadas (codigoTipoIgv = "10")
     * - Línea 4: Exonerada (codigoTipoIgv = "20")
     * - Línea 5: Inafecta gratuita (codigoTipoIgv = "31")
     *
     * El builder genera automáticamente los tres subtotales de impuestos.
     * </p>
     */
    public static final String FACTURA_MULTIPLE_OPERACIONES_REQUEST = """
            {
                "tipoDocumento": "01",
                "numero": "F001-4355",
                "fechaEmision": "2017-05-14",
                "horaEmision": "13:25:51",
                "tipoDeOperacion": "0101",
                "moneda": "PEN",
                "emisorRuc": "20100454523",
                "emisorRazonSocial": "Soporte Tecnológicos EIRL",
                "emisorNombreComercial": "Tu Soporte",
                "emisorCodigoDomicilio": "0000",
                "receptorDocumento": "20587896411",
                "receptorRazonSocial": "SERVICABINAS S.A.",
                "detalles": [
                    {
                        "codigoProducto": "GLG199",
                        "descripcion": "Grabadora LG Externo Modelo: GE20LU10",
                        "cantidad": 2000.00,
                        "valorUnitario": 83.05,
                        "igv": 26908.47,
                        "codigoTipoIgv": "10"
                    },
                    {
                        "codigoProducto": "MVS546",
                        "descripcion": "Monitor LCD ViewSonic VG2028WM 20",
                        "cantidad": 300.00,
                        "valorUnitario": 525.42,
                        "igv": 24116.95,
                        "codigoTipoIgv": "10"
                    },
                    {
                        "codigoProducto": "MPC35",
                        "descripcion": "Memoria DDR-3 B1333 Kingston",
                        "cantidad": 250.00,
                        "valorUnitario": 52.00,
                        "igv": 0.00,
                        "codigoTipoIgv": "20"
                    },
                    {
                        "codigoProducto": "TMS22",
                        "descripcion": "Teclado Microsoft SideWinder X6",
                        "cantidad": 500.00,
                        "valorUnitario": 166.10,
                        "igv": 14949.15,
                        "codigoTipoIgv": "10"
                    },
                    {
                        "codigoProducto": "WCG01",
                        "descripcion": "Web cam Genius iSlim 310VVU",
                        "cantidad": 1.00,
                        "valorUnitario": 0.00,
                        "igv": 0.00,
                        "codigoTipoIgv": "31"
                    }
                ],
                "leyendas": [
                    {
                        "codigoLocal": "1000",
                        "leyenda": "CUATROCIENTOS VEINTITRES MIL DOSCIENTOS VEINTICINCO Y 00/100"
                    },
                    {
                        "codigoLocal": "3000",
                        "leyenda": "0501002017051400452"
                    }
                ]
            }
            """;

    // =============================================================================
    // FACTURA CON DESCUENTO POR LÍNEA
    // =============================================================================

    /**
     * Ejemplo request - Factura con descuento por línea de detalle.
     *
     * <p>
     * Los descuentos por línea se manejan a través del modelo DetalleCanonico
     * extension (agregando campos adicionales). En este ejemplo el precio ya
     * incluye el descuento aplicado.
     * </p>
     */
    public static final String FACTURA_CON_DESCUENTO_LINEA_REQUEST = """
            {
                "tipoDocumento": "01",
                "numero": "F001-100",
                "fechaEmision": "2026-05-01",
                "horaEmision": "09:00:00",
                "tipoDeOperacion": "0101",
                "moneda": "PEN",
                "emisorRuc": "20123456789",
                "emisorRazonSocial": "K&G ASOCIADOS S.A.C.",
                "emisorNombreComercial": "K&G LABORATORIOS",
                "emisorCodigoDomicilio": "0001",
                "receptorDocumento": "20445678901",
                "receptorRazonSocial": "CLIENTE PERU S.A.C.",
                "detalles": [
                    {
                        "codigoProducto": "MED001",
                        "descripcion": "Antibiótico GENÉRICO 500mg x 30 tablas",
                        "cantidad": 30.00,
                        "valorUnitario": 45.00,
                        "igv": 243.00,
                        "codigoTipoIgv": "10"
                    },
                    {
                        "codigoProducto": "MED002",
                        "descripcion": "Paracetamol 500mg x 100 tabletas - OFERTA",
                        "cantidad": 10.00,
                        "valorUnitario": 13.50,
                        "igv": 22.50,
                        "codigoTipoIgv": "10"
                    }
                ],
                "leyendas": [
                    {
                        "codigoLocal": "1000",
                        "leyenda": "MIL CUATROCIENTOS NOVENTA Y CINCO CON 50/100 SOLES"
                    }
                ]
            }
            """;

    // =============================================================================
    // FACTURA CON OPERACIÓN EXONERADA
    // =============================================================================

    /**
     * Ejemplo request - Factura con operación exonerada.
     *
     * <p>
     * Las operaciones exoneradas (codigoTipoIgv = "20") no pagan IGV.
     * Ejemplos: bienes con resolución de exoneración, servicios去医院.
     * El monto base exonerado se incluye en el total pero sin IGV.
     * </p>
     */
    public static final String FACTURA_EXONERADA_REQUEST = """
            {
                "tipoDocumento": "01",
                "numero": "F001-200",
                "fechaEmision": "2026-05-01",
                "horaEmision": "10:00:00",
                "tipoDeOperacion": "0101",
                "moneda": "PEN",
                "emisorRuc": "20123456789",
                "emisorRazonSocial": "K&G ASOCIADOS S.A.C.",
                "emisorCodigoDomicilio": "0001",
                "receptorDocumento": "20445678901",
                "receptorRazonSocial": "CLIENTE PERU S.A.C.",
                "detalles": [
                    {
                        "codigoProducto": "MED100",
                        "descripcion": "Medicamentos varios ( exonerados )",
                        "cantidad": 50.00,
                        "valorUnitario": 100.00,
                        "igv": 0.00,
                        "codigoTipoIgv": "20"
                    },
                    {
                        "codigoProducto": "MED101",
                        "descripcion": "Jeringas descartables",
                        "cantidad": 100.00,
                        "valorUnitario": 5.00,
                        "igv": 90.00,
                        "codigoTipoIgv": "10"
                    }
                ],
                "leyendas": [
                    {
                        "codigoLocal": "1000",
                        "leyenda": "CINCO MIL NOVENTA Y 00/100 SOLES"
                    }
                ]
            }
            """;

    // =============================================================================
    // FACTURA CON OPERACIÓN INAFECTA
    // =============================================================================

    /**
     * Ejemplo request - Factura con operación inafecta (no onerosa/gratuita).
     *
     * <p>
     * Las operaciones inafectas incluyen:
     * - Código 30: Operación onerosa inafecta (exportación)
     * - Código 31: Operación gratuita (bonificaciones, muestras gratis)
     *
     * El valor unitario puede ser cero para operaciones gratuitas.
     * </p>
     */
    public static final String FACTURA_INAFECTA_REQUEST = """
            {
                "tipoDocumento": "01",
                "numero": "F001-300",
                "fechaEmision": "2026-05-01",
                "horaEmision": "11:00:00",
                "tipoDeOperacion": "0101",
                "moneda": "PEN",
                "emisorRuc": "20123456789",
                "emisorRazonSocial": "K&G ASOCIADOS S.A.C.",
                "emisorCodigoDomicilio": "0001",
                "receptorDocumento": "20445678901",
                "receptorRazonSocial": "CLIENTE PERU S.A.C.",
                "detalles": [
                    {
                        "codigoProducto": "PROD001",
                        "descripcion": "Producto vendido",
                        "cantidad": 10.00,
                        "valorUnitario": 100.00,
                        "igv": 180.00,
                        "codigoTipoIgv": "10"
                    },
                    {
                        "codigoProducto": "BONUS001",
                        "descripcion": "Muestra gratuita - Producto promocional",
                        "cantidad": 5.00,
                        "valorUnitario": 0.00,
                        "igv": 0.00,
                        "codigoTipoIgv": "31"
                    }
                ],
                "leyendas": [
                    {
                        "codigoLocal": "1000",
                        "leyenda": "MIL OCHOCIENTOS Y 00/100 SOLES"
                    }
                ]
            }
            """;

    // =============================================================================
    // FACTURA CON REFERENCIA DE ORDEN (anticipos)
    // =============================================================================

    /**
     * Ejemplo request - Factura con referencia de orden (anticipo).
     *
     * <p>
     * El campo 'documentoRelacionado' se usa para referenciar anticipos.
     * Tipo documento relacionado: 02 = Anticipo
     * </p>
     */
    public static final String FACTURA_CON_ORDEN_REQUEST = """
            {
                "tipoDocumento": "01",
                "numero": "F001-400",
                "fechaEmision": "2026-05-01",
                "horaEmision": "12:00:00",
                "tipoDeOperacion": "0102",
                "moneda": "PEN",
                "emisorRuc": "20123456789",
                "emisorRazonSocial": "K&G ASOCIADOS S.A.C.",
                "emisorCodigoDomicilio": "0001",
                "receptorDocumento": "20445678901",
                "receptorRazonSocial": "CLIENTE PERU S.A.C.",
                "documentoRelacionado": {
                    "tipoDocumento": "02",
                    "numeroDocumento": "0"
                },
                "detalles": [
                    {
                        "codigoProducto": "SERV001",
                        "descripcion": "Servicio de mantenimiento - Anticipo 50%",
                        "cantidad": 1.00,
                        "valorUnitario": 500.00,
                        "igv": 90.00,
                        "codigoTipoIgv": "10"
                    }
                ],
                "leyendas": [
                    {
                        "codigoLocal": "1000",
                        "leyenda": "QUINIENTOS NOVENTA Y 00/100 SOLES"
                    }
                ]
            }
            """;
}
