import { z } from "zod";

// Regex: solo letras (incluye tildes), espacios, guiones y puntos
const nameRegex = /^[a-zA-ZÁÉÍÓÚáéíóúÑñ\s\-\.]+$/u;
// Regex: al menos una mayúscula, una minúscula y un número
const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/;

export const registroSchema = z
  .object({
    name: z
      .string()
      .min(1, { message: "El nombre es requerido" })
      .min(2, { message: "El nombre debe tener al menos 2 caracteres" })
      .max(100, { message: "El nombre no puede exceder 100 caracteres" })
      .regex(nameRegex, {
        message: "El nombre solo puede contener letras, espacios, guiones y puntos",
      })
      .refine((val) => val.trim().length >= 2, {
        message: "El nombre no puede contener solo espacios",
      }),
    email: z
      .string()
      .email({ message: "El correo electrónico no es válido" })
      .min(1, { message: "El correo electrónico es requerido" })
      .max(254, { message: "El correo no puede exceder 254 caracteres" }),
    password: z
      .string()
      .min(8, { message: "La contraseña debe tener al menos 8 caracteres" })
      .max(128, { message: "La contraseña no puede exceder 128 caracteres" })
      .regex(passwordRegex, {
        message: "La contraseña debe contener al menos una mayúscula, una minúscula y un número",
      }),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Las contraseñas no coinciden",
    path: ["confirmPassword"],
  });

export type RegistroFormData = z.infer<typeof registroSchema>;