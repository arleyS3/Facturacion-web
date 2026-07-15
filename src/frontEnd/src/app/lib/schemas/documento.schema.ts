import { z } from "zod";

/**
 * Schema de validación para el correlativo del documento.
 * Debe ser exactamente 8 dígitos numéricos.
 */
const correlativoSchema = z
  .string()
  .min(1, { message: "El correlativo es requerido" })
  .regex(/^\d{8}$/, { message: "El correlativo debe tener exactamente 8 dígitos numéricos" });

export const documentoSchema = z.object({
  correlativo: correlativoSchema,
});

export type DocumentoFormData = z.infer<typeof documentoSchema>;