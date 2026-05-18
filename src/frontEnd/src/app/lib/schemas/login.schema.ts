import { z } from "zod";

export const loginSchema = z.object({
  email:
  z.email({ message: "El correo electrónico no es válido" })
    .min(1, { message: "El correo electrónico es requerido" })
    .max(254, { message: "El correo no puede exceder 254 caracteres" }),
  password: z
    .string()
    .min(8, { message: "La contraseña debe tener al menos 8 caracteres" })
    .max(128, { message: "La contraseña no puede exceder 128 caracteres" })
});

export type LoginFormData = z.infer<typeof loginSchema>;
