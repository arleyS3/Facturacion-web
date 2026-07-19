import { z } from "zod";

/**
 * Schema unificado para comprobantes electrónicos.
 *
 * Nombres en español para concordar con el backend (ComprobanteCanonico).
 * Este schema se usa tanto para validación del formulario como para
 * construir el payload del XML.
 *
 * .passthrough() preserva campos legacy que buildPayload aún necesita.
 */

// =============================================================================
// HELPERS
// =============================================================================

const requiredString = (message: string) =>
  z.string().trim().min(1, { message });
const optionalString = z.string().optional();

const positiveNumberish = z
  .union([z.string(), z.number()])
  .refine(
    (val) => {
      const n = Number(val);
      return !isNaN(n) && n > 0;
    },
    { message: "Debe ser mayor a 0" },
  );

const nonNegativeNumber = z.number().min(0);

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
  .regex(/^\d{8}$/, {
    message:
      "El correlativo debe tener exactamente 8 dígitos numéricos",
  });

/**
 * Schema para datos ISC anidados (modal de configuración ISC).
 */
const iscDataSchema = z
  .object({
    codigoISC: requiredString("El código ISC es requerido"),
    codigoTipoISC: requiredString("El sistema ISC es requerido"),
    tasa: requiredString("La tasa ISC es requerida"),
    montoISC: z.number().min(0).default(0),
  })
  .passthrough();

/**
 * Schema para un detalle (producto) del comprobante.
 */
const detalleSchema = z
  .object({
    id: requiredString("El ID del producto es requerido"),
    correlativo: z.number().optional(),
    codigo: z.string().optional(),
    descripcion: requiredString("La descripción es requerida"),
    cantidad: positiveNumberish,
    unidadMedida: z.string().default("NIU"),
    precioUnitario: positiveNumberish,
    valorVenta: nonNegativeNumber.default(0),
    tieneDescuento: z.boolean().default(false),
    descuentoPorItem: z.string().optional(),
    descuentoMonto: nonNegativeNumber.default(0),
    tipoAfectacionIGV: requiredString(
      "El tipo de afectación IGV es requerido",
    ),
    codigoTributo: requiredString("El código de tributo es requerido"),
    tasaIGV: z
      .union([z.string(), z.number()])
      .refine(
        (val) => {
          const n = Number(val);
          return !isNaN(n) && n >= 0;
        },
        { message: "La tasa IGV no es válida" },
      ),
    impuestoIGV: nonNegativeNumber.default(0),

    // ISC anidado
    tieneISC: z.boolean().default(false),
    iscData: iscDataSchema.nullable().optional(),

    // Flat ISC compatibility fields (todavía usados por buildPayload)
    codigoISC: z.string().optional(),
    codigoTipoISC: z.string().optional(),
    tasaISC: z.string().optional(),
    montoISC: z.number().optional(),
  })
  .passthrough();

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
 * Se valida condicionalmente en superRefine solo para notas.
 */
const documentoRelacionadoSchema = z.object({
  tipoDocumento: z.string(),
  numeroDocumento: z.string(),
  codigoMotivo: z.string().optional(),
  descripcionMotivo: z.string().optional(),
});

/**
 * Schema para parte de traslado (guías de remisión).
 */
const parteTrasladoSchema = z
  .object({
    tipoTraslado: z.string().optional(),
    modalidadTraslado: z.string().optional(),
    pesoBrutoTotal: z.number().optional(),
    undPesoTotal: z.string().optional(),
    numeroBultos: z.number().optional(),
  })
  .optional();

/**
 * Schema para una guía de remisión referenciada en el comprobante.
 */
export const guiaRemisionSchema = z.object({
  serie: z
    .string()
    .min(1, { message: "La serie es requerida" }),
  numero: z
    .string()
    .min(1, { message: "El número es requerido" }),
  codigoDocumento: z
    .string()
    .min(1, { message: "El tipo de documento es requerido" }),
});

/**
 * Schema para un documento adicional referenciado en el comprobante.
 */
export const documentoAdicionalSchema = z.object({
  id: z
    .string()
    .min(1, { message: "El ID del documento es requerido" }),
  tipoDocumento: z
    .string()
    .min(1, { message: "El tipo de documento es requerido" }),
});

/**
 * Schema para la lista de documentos adicionales.
 */
export const documentosAdicionalesSchema = z.array(documentoAdicionalSchema);

// =============================================================================
// HELPER: validación condicional para notas de crédito/débito
// =============================================================================

const isNoteDoc = (val: string | undefined) => {
  if (!val) return false;
  const v = val.trim();
  return (
    v === "07" ||
    v === "08" ||
    v === "Nota de Crédito" ||
    v === "Nota de Débito"
  );
};

const validateConditionalRules: z.SuperRefine<any> = (data, ctx) => {
  const docType =
    data.tipoDocumento ?? data.docType ?? data.docTypeCode ?? "";
  const isNote = isNoteDoc(docType);

  if (isNote) {
    // documentoRelacionado es obligatorio
    if (!data.documentoRelacionado) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message:
          "Para notas de crédito/débito el documento relacionado es obligatorio",
        path: ["documentoRelacionado"],
      });
      return;
    }

    const rel = data.documentoRelacionado;

    if (!rel.tipoDocumento || rel.tipoDocumento.trim() === "") {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "El tipo de documento relacionado es requerido",
        path: ["documentoRelacionado", "tipoDocumento"],
      });
    }

    if (!rel.numeroDocumento || rel.numeroDocumento.trim() === "") {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "El número de documento relacionado es requerido",
        path: ["documentoRelacionado", "numeroDocumento"],
      });
    }

    const hasMotivo =
      (rel.codigoMotivo && rel.codigoMotivo.trim() !== "") ||
      (data.tipoNota && data.tipoNota.trim() !== "");

    if (!hasMotivo) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "El motivo de la nota es requerido",
        path: ["documentoRelacionado", "codigoMotivo"],
      });
    }
  }
};

const validateIscCompatibility: z.SuperRefine<any> = (data, ctx) => {
  if (!data.tieneISC) return;

  const hasNested =
    data.iscData &&
    data.iscData.codigoISC &&
    data.iscData.codigoTipoISC &&
    data.iscData.tasa;

  const hasFlat =
    data.codigoISC &&
    data.codigoTipoISC &&
    (data.tasaISC || data.tasa) &&
    data.montoISC !== undefined &&
    Number(data.montoISC) >= 0;

  if (hasNested || hasFlat) return;

  // El que esté más completo marca el error
  if (!data.iscData?.codigoISC && !data.codigoISC) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      message: "El código ISC es requerido cuando tiene ISC",
      path: ["iscData", "codigoISC"],
    });
  }
  if (!data.iscData?.codigoTipoISC && !data.codigoTipoISC) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      message: "El sistema ISC es requerido cuando tiene ISC",
      path: ["iscData", "codigoTipoISC"],
    });
  }
  if (!data.iscData?.tasa && !data.tasaISC && !data.tasa) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      message: "La tasa ISC es requerida cuando tiene ISC",
      path: ["iscData", "tasa"],
    });
  }
};

// =============================================================================
// SCHEMA PRINCIPAL
// =============================================================================

const comprobanteBaseSchema = z.object({
  // =================================================================
  // DATOS DEL DOCUMENTO
  // =================================================================
  codigoEmpresa: requiredString("El código de empresa es requerido"),
  tipoDocumento: requiredString("El tipo de documento es requerido"),
  serie: requiredString("La serie es requerida"),
  correlativo: correlativoSchema,
  fechaEmision: requiredString("La fecha de emisión es requerida"),
  horaEmision: optionalString,
  fechaVencimiento: optionalString,
  tipoOperacion: requiredString("El tipo de operación es requerido"),
  moneda: z.string().default("PEN"),

  // =================================================================
  // DATOS DEL EMISOR (de la configuración de la empresa)
  // =================================================================
  emisorTipoDoc: requiredString(
    "El tipo de documento del emisor es requerido",
  ),
  emisorRuc: requiredString("El RUC del emisor es requerido"),
  emisorRazonSocial: requiredString(
    "La razón social del emisor es requerida",
  ),
  emisorNombreComercial: optionalString,
  emisorDireccion: requiredString(
    "La dirección del emisor es requerida",
  ),
  emisorUrbanizacion: optionalString,
  emisorDepartamento: optionalString,
  emisorProvincia: optionalString,
  emisorDistrito: optionalString,
  emisorUbigeo: optionalString,
  emisorCodigoDomicilio: z.string().default("0001"),

  // =================================================================
  // DATOS DEL RECEPTOR
  // =================================================================
  receptorTipoDoc: requiredString(
    "El tipo de documento del receptor es requerido",
  ),
  receptorDocumento: requiredString(
    "El documento del receptor es requerido",
  ),
  receptorRazonSocial: requiredString(
    "La razón social del receptor es requerida",
  ),
  receptorDireccion: requiredString(
    "La dirección del receptor es requerida",
  ),
  receptorDepartamento: requiredString(
    "El departamento del receptor es requerido",
  ),
  receptorProvincia: requiredString(
    "La provincia del receptor es requerida",
  ),
  receptorDistrito: requiredString(
    "El distrito del receptor es requerido",
  ),
  receptorUbigeo: optionalString,

  // =================================================================
  // PRODUCTOS / DETALLES
  // =================================================================
  detalles: z
    .array(detalleSchema)
    .min(1, { message: "Debe agregar al menos un producto" }),

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
  documentoRelacionado: documentoRelacionadoSchema.optional(),

  // =================================================================
  // GUÍA DE REMISIÓN (opcional)
  // =================================================================
  guiaRemision: guiaRemisionSchema.optional(),

  // =================================================================
  // DOCUMENTOS ADICIONALES (opcional)
  // =================================================================
  documentosAdicionales: documentosAdicionalesSchema.optional(),

  // =================================================================
  // CAMPOS DE NOTAS
  // =================================================================
  tipoNota: optionalString,

  // =================================================================
  // LEGACY ALIASES (compatibilidad con buildPayload)
  // =================================================================
  // Doc aliases
  docType: optionalString,
  docTypeCode: optionalString,
  companyCode: optionalString,
  companyId: optionalString,
  emissionDate: optionalString,
  emissionTime: optionalString,

  // Emisor aliases
  tipoDocEmisor: optionalString,
  numeroDocEmisor: optionalString,
  razonSocialEmisor: optionalString,
  nombreComercialEmisor: optionalString,
  direccionEmisor: optionalString,
  anexoEmisor: optionalString,
  issuerDocType: optionalString,
  issuerDocNumber: optionalString,
  issuerSocialName: optionalString,
  issuerCommercialName: optionalString,
  issuerAddress: optionalString,
  issuerAnexo: optionalString,
  issuerNoDet: z.boolean().optional(),
  localCode: z.string().optional(),

  // Receptor aliases
  tipoDocReceptor: optionalString,
  numeroDocReceptor: optionalString,
  razonSocialReceptor: optionalString,
  direccionReceptor: optionalString,
  receiverDocType: optionalString,
  receiverDocNumber: optionalString,
  receiverSocialName: optionalString,
  receiverAddress: optionalString,
  receiverDepartment: optionalString,
  receiverProvince: optionalString,
  receiverDistrict: optionalString,
  receiverUbigeo: optionalString,

  // Detalles alias
  products: z.array(detalleSchema).optional(),
}).passthrough();

/**
 * Schema principal del comprobante con validaciones condicionales.
 * .passthrough() preserva campos legacy no modelados explícitamente.
 */
export const comprobanteSchema =
  comprobanteBaseSchema.superRefine(validateConditionalRules);

// =============================================================================
// SCHEMA LEGACY (para payload TXT, no para validación de formulario)
// =============================================================================

export const payloadLegacySchema = z
  .object({
    docType: z.string().optional(),
    docTypeCode: z.string().optional(),
    emisorRuc: z.string().optional(),
    emisorRazonSocial: z.string().optional(),
    emisorNombreComercial: z.string().optional(),
    emisorDireccion: z.string().optional(),
    emisorAnexo: z.string().optional(),
    receptorDocumento: z.string().optional(),
    receptorRazonSocial: z.string().optional(),
    receptorDireccion: z.string().optional(),
    receptorUbigeo: z.string().optional(),
    serie: z.string().optional(),
    correlativo: z.string().optional(),
    fechaEmision: z.string().optional(),
    horaEmision: z.string().optional(),
    tipoOperacion: z.string().optional(),
    moneda: z.string().optional(),
    products: z.array(detalleSchema).optional(),
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
  })
  .catchall(z.any());

// =============================================================================
// TIPOS EXPORTADOS
// =============================================================================

export type ComprobanteFormData = z.infer<typeof comprobanteSchema>;
export type DetalleFormData = z.infer<typeof detalleSchema>;
export type DescuentoGlobalFormData = z.infer<typeof descuentoGlobalSchema>;
export type LeyendaFormData = z.infer<typeof leyendaSchema>;
export type DocumentoRelacionadoFormData = z.infer<
  typeof documentoRelacionadoSchema
>;
export type GuiaRemisionFormData = z.infer<typeof guiaRemisionSchema>;
export type DocumentoAdicionalFormData = z.infer<
  typeof documentoAdicionalSchema
>;
export type PayloadLegacyFormData = z.infer<typeof payloadLegacySchema>;
