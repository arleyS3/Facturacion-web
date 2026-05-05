package com.facturacion.api.web;

/**
 * Ejemplos de requests y responses para documentación Swagger.
 *
 * <p>
 * Estos ejemplos permiten documentar los endpoints de manera más legible
 * en la especificación OpenAPI/Swagger.
 * </p>
 */
public final class Examples {

    private Examples() {
        // Utility class
    }

    // =============================================================================
    // FACTURA ELECTRÓNICA
    // =============================================================================

    /**
     * Ejemplo de request para generar una Factura Electrónica.
     *
     * <p>
     * Documento: Factura Electrónica
     * Código SUNAT: 01
     * </p>
     */
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
                    "descripcion": "Antibiótico GENÉRICO 500mg x 30 tablas",
                    "cantidad": 30.00,
                    "valorUnitario": 50.00,
                    "igv": 270.00,
                    "codigoTipoIgv": "10"
                },
                {
                    "descripcion": "Jarabe para Tos 100ml",
                    "cantidad": 5.00,
                    "valorUnitario": 25.00,
                    "igv": 22.50,
                    "codigoTipoIgv": "10"
                }
            ]
        }
        """;

    /**
     * Ejemplo de response exitoso para generación de Factura.
     */
    public static final String FACTURA_RESPONSE_SUCCESS = """
        {
            "success": true,
            "tipoDocumento": "01",
            "xml": "<Invoice>...</Invoice>"
        }
        """;

    /**
     * Ejemplo de response de error por validación.
     */
    public static final String FACTURA_RESPONSE_VALIDATION_ERROR = """
        {
            "message": "Validación falló",
            "details": [
                "emisorRuc: RUC debe tener 11 dígitos",
                "detalles[0].cantidad: Cantidad es requerida"
            ],
            "timestamp": "2026-05-01T10:30:00Z"
        }
        """;

    /**
     * Ejemplo de response de error por tipo de documento no soportado.
     */
    public static final String FACTURA_RESPONSE_UNSUPPORTED_TYPE = """
        {
            "success": false,
            "error": "Tipo de documento no soportado: 99"
        }
        """;

    // =============================================================================
    // BOLETA DE VENTA (próximamente)
    // =============================================================================

    /**
     * Ejemplo de request para generar una Boleta de Venta.
     *
     * <p>
     * Documento: Boleta de Venta
     * Código SUNAT: 03
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
            "receptorDocumento": "12345678",
            "receptorRazonSocial": "Consumidor Final",
            "detalles": [
                {
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
    // NOTA DE CRÉDITO (próximamente)
    // =============================================================================

    /**
     * Ejemplo de request para generar una Nota de Crédito.
     *
     * <p>
     * Documento: Nota de Crédito
     * Código SUNAT: 07
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
}