package com.facturacion.api.web.Examples;

/**
 * Ejemplos de requests para generación de XML UBL 2.1.
 *
 * <p>
 * Usados en la documentación Swagger de ComprobanteController.
 * </p>
 */
public final class ComprobanteExample {

    private ComprobanteExample() {
        // Utility class
    }

    // =============================================================================
    // FACTURA ELECTRÓNICA (01)
    // =============================================================================

    /** Ejemplo request - Factura Electrónica */
    public static final String FACTURA_REQUEST = """
        {
            "tipoDocumento": "01",
            "numero": "F001-1",
            "fechaEmision": "2026-05-01",
            "horaEmision": "10:30:00",
            "fechaVencimiento": "2026-05-30",
            "tipoDeOperacion": "0101",
            "moneda": "PEN",
            "emisorRuc": "20123456789",
            "emisorRazonSocial": "K&G ASOCIADOS S.A.C.",
            "emisorNombreComercial": "K&G LABORATORIOS",
            "emisorDireccion": "Av. Principal 123 - Lima - Lima",
            "emisorUrbanizacion": "Centro Comercial",
            "emisorDepartamento": "LIMA",
            "emisorProvincia": "LIMA",
            "emisorDistrito": "LIMA",
            "emisorUbigeo": "150101",
            "emisorCodigoDomicilio": "0001",
            "receptorDocumento": "20445678901",
            "receptorRazonSocial": "CLIENTE PERU S.A.C.",
            "receptorDireccion": "Av. Cliente 456 - Lima",
            "receptorUbigeo": "150101",
            "detalles": [
                {
                    "codigoProducto": "MED001",
                    "descripcion": "Antibiótico GENÉRICO 500mg x 30 tablas",
                    "cantidad": 30.00,
                    "valorUnitario": 50.00,
                    "igv": 270.00,
                    "codigoTipoIgv": "10"
                }
            ]
        }
        """;

    // =============================================================================
    // BOLETA DE VENTA (03)
    // =============================================================================

    /** Ejemplo request - Boleta de Venta */
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

    /** Ejemplo request - Nota de Crédito */
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

    /** Ejemplo request - Nota de Débito */
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

    /** Ejemplo request - Guía de Remisión */
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
}