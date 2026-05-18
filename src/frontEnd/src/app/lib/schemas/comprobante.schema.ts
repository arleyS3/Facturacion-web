import { z } from "zod";

/**
 * Schema unificado para comprobantes electrónicos.
 * 
 * Nombres en español para concordar con el backend (ComprobanteCanonico).
 * Este schema se usa tanto para validación del formulario como para
 * construir el payload del XML.
 */

// =============================================================================
// SCHEMAS AUXILIARES
// =============================================================================

/**
 * Schema para el correlativo del documento.
 * Debe ser exactamente 8 dígitos numéricos.
 */
const correlativoSchema = z
  .string()
  .min(1, { message: "El correlativo es requerido" })
  .regex(/^\d{8}$/, { message: "El correlativo debe tener exactamente 8 dígitos numéricos" });

/**
 * Schema para el monto neto pendiente (campo opcional).
 */
const montoPendienteSchema = z
  .string()
  .optional()
  .refine(
    (val) => {
      if (!val || val.trim() === "") return true;
      return /^\d+(\.\d{1,2})?$/.test(val);
    },
    { message: "El monto debe ser un número válido con hasta 2 decimales" }
  );

/**
 * Schema para un detalle (producto) del comprobante.
 */
const detalleSchema = z.object({
  id: z.string(),
  correlativo: z.number().optional(),
  codigo: z.string().optional(),
  descripcion: z.string(),
  cantidad: z.string().or(z.number()),
  unidadMedida: z.string().default("NIU"),
  precioUnitario: z.string().or(z.number()),
  valorVenta: z.number().default(0),
  tieneDescuento: z.boolean().default(false),
  descuentoPorItem: z.string().optional(),
  descuentoMonto: z.number().default(0),
  tipoAfectacionIGV: z.string().default("10"), // 10=Gravado, 20=Exonerado, 30/31=Inafecto
  codigoTributo: z.string().default("1000"),
  tasaIGV: z.string().default("18"),
  impuestoIGV: z.number().default(0),
  // Campos adicionales para ISC
  codigoISC: z.string().optional(),
  codigoTipoISC: z.string().optional(),
  tasaISC: z.string().optional(),
  montoISC: z.number().optional(),
});

/**
 * Schema para un descuento global del comprobante.
 * Corresponde a AllowanceCharge de SUNAT UBL 2.1.
 */
const descuentoGlobalSchema = z.object({
  id: z.string(),
  esCargo: z.boolean().default(false),
  codigoMotivo: z.string(),
  porcentaje: z.string(),
  monto: z.string(),
  montoBase: z.string(),
});

/**
 * Schema para una leyenda del comprobante.
 */
const leyendaSchema = z.object({
  codigoLocal: z.string(), // Catálogo 52 (1000, 2006, etc.)
  leyenda: z.string(),
});

/**
 * Schema para documento relacionado (notas de crédito/débito).
 */
const documentoRelacionadoSchema = z.object({
  tipoDocumento: z.string(),
  numeroDocumento: z.string(),
  codigoMotivo: z.string().optional(),
}).optional();

/**
 * Schema para parte de traslado (guías de remisión).
 */
const parteTrasladoSchema = z.object({
  tipoTraslado: z.string().optional(),
  modalidadTraslado: z.string().optional(),
  pesoBrutoTotal: z.number().optional(),
  undPesoTotal: z.string().optional(),
  numeroBultos: z.number().optional(),
}).optional();

// =============================================================================
// SCHEMA PRINCIPAL
// =============================================================================

export const comprobanteSchema = z.object({
  // =================================================================
  // DATOS DEL DOCUMENTO
  // =================================================================
  tipoDocumento: z.string().min(1, { message: "El tipo de documento es requerido" }),
  serie: z.string().min(1, { message: "La serie es requerida" }),
  correlativo: correlativoSchema,
  fechaEmision: z.string().min(1, { message: "La fecha de emisión es requerida" }),
  horaEmision: z.string().optional(),
  fechaVencimiento: z.string().optional(),
  tipoOperacion: z.string().min(1, { message: "El tipo de operación es requerido" }),
  moneda: z.string().default("PEN"),

  // =================================================================
  // DATOS DEL EMISOR (de la configuración de la empresa)
  // =================================================================
  emisorRuc: z.string().min(1, { message: "El RUC del emisor es requerido" }),
  emisorRazonSocial: z.string().min(1, { message: "La razón social del emisor es requerida" }),
  emisorNombreComercial: z.string().optional(),
  emisorDireccion: z.string().min(1, { message: "La dirección del emisor es requerida" }),
  emisorUrbanizacion: z.string().optional(),
  emisorDepartamento: z.string().optional(),
  emisorProvincia: z.string().optional(),
  emisorDistrito: z.string().optional(),
  emisorUbigeo: z.string().optional(),
  emisorCodigoDomicilio: z.string().default("0001"),

  // =================================================================
  // DATOS DEL RECEPTOR
  // =================================================================
  receptorDocumento: z.string().min(1, { message: "El documento del receptor es requerido" }),
  receptorRazonSocial: z.string().min(1, { message: "La razón social del receptor es requerida" }),
  receptorDireccion: z.string().optional(),
  receptorUbigeo: z.string().optional(),

  // =================================================================
  // PRODUCTOS / DETALLES
  // =================================================================
  detalles: z.array(detalleSchema).min(1, { message: "Debe agregar al menos un producto" }),

  // =================================================================
  // DESCUENTOS GLOBALES (opcional)
  // =================================================================
  descuentosGlobales: z.array(descuentoGlobalSchema).optional(),

  // =================================================================
  // LEYENDAS (opcional)
  // =================================================================
  leyendas: z.array(leyendaSchema).optional(),

  // =================================================================
  // DOCUMENTO RELACIONADO (para notas)
  // =================================================================
  documentoRelacionado: documentoRelacionadoSchema,

  // =================================================================
  // CAMPOS ADICIONALES
  // =================================================================
  montoPendiente: montoPendienteSchema,
});

/**
 * Schema para el payload legacy del TXT (mantiene compatibilidad).
 * Este se usa internamente en buildPayload.
 */
export const payloadLegacySchema = z.object({
  docType: z.string().optional(),
  docTypeCode: z.string().optional(),
  // Emisor
  emisorRuc: z.string().optional(),
  emisorRazonSocial: z.string().optional(),
  emisorNombreComercial: z.string().optional(),
  emisorDireccion: z.string().optional(),
  emisorAnexo: z.string().optional(),
  // Receptor
  receptorDocumento: z.string().optional(),
  receptorRazonSocial: z.string().optional(),
  receptorDireccion: z.string().optional(),
  receptorUbigeo: z.string().optional(),
  // Documento
  serie: z.string().optional(),
  correlativo: z.string().optional(),
  fechaEmision: z.string().optional(),
  horaEmision: z.string().optional(),
  tipoOperacion: z.string().optional(),
  moneda: z.string().optional(),
  // Productos
  products: z.array(detalleSchema).optional(),
  // Otros
  montoPendiente: z.string().optional(),
  // Campos legacy (para compatibilidad con buildPayload)
  issuerDocType: z.string().optional(),
  issuerDocNumber: z.string().optional(),
  issuerSocialName: z.string().optional(),
  issuerCommercialName: z.string().optional(),
  issuerAddress: z.string().optional(),
  localCode: z.string().optional(),
  receiverDocType: z.string().optional(),
  receiverDocNumber: z.string().optional(),
  receiverSocialName: z.string().optional(),
  receiverAddress: z.string().optional(),
  companyCode: z.string().optional(),
  companyId: z.string().optional(),
  emissionDate: z.string().optional(),
  emissionTime: z.string().optional(),
}).catchall(z.any());

// =============================================================================
// TIPOS EXPORTADOS
// =============================================================================

export type ComprobanteFormData = z.infer<typeof comprobanteSchema>;
export type DetalleFormData = z.infer<typeof detalleSchema>;
export type DescuentoGlobalFormData = z.infer<typeof descuentoGlobalSchema>;
export type LeyendaFormData = z.infer<typeof leyendaSchema>;
export type DocumentoRelacionadoFormData = z.infer<typeof documentoRelacionadoSchema>;
export type PayloadLegacyFormData = z.infer<typeof payloadLegacySchema>;