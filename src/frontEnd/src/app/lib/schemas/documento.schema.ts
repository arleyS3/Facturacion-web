import { z } from "zod";

/**
 * Schema de validación para el correlativo del documento.
 * Debe ser exactamente 8 dígitos numéricos.
 */
const correlativoSchema = z
  .string()
  .min(1, { message: "El correlativo es requerido" })
  .regex(/^\d{8}$/, { message: "El correlativo debe tener exactamente 8 dígitos numéricos" });

/**
 * Schema de validación para el monto neto pendiente.
 * Debe ser un número decimal válido con hasta 2 decimales (formato ##.##)
 * Acepta valores como: 0, 0.5, 0.50, 100.25, 1000.99
 * No acepta: números negativos, más de 2 decimales, texto
 */
const montoPendienteSchema = z
  .string()
  .optional()
  .refine(
    (val) => {
      // Si está vacío o no existe, es válido (campo opcional)
      if (!val || val.trim() === "") return true;
      // Verificar que sea un número válido con hasta 2 decimales
      return /^\d+(\.\d{1,2})?$/.test(val);
    },
    { message: "El monto debe ser un número válido con hasta 2 decimales (ej: 100.50)" }
  );

export const documentoSchema = z.object({
  correlativo: correlativoSchema,
  montoPendiente: montoPendienteSchema,
});

export type DocumentoFormData = z.infer<typeof documentoSchema>;