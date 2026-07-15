import { useForm, type UseFormProps } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

/**
 * Wrapper sobre useForm que conecta Zod como resolver.
 *
 * Te da:
 * - type-safe form sin escribir interfaces a mano
 * - validación en tiempo real vía react-hook-form
 * - mensajes de error desde Zod automáticamente
 *
 * Uso:
 *   const { register, handleSubmit, formState: { errors } } = useZodForm(loginSchema);
 */
export function useZodForm<T extends object>(
  schema: import('zod').ZodSchema<T>,
  options?: UseFormProps<T>
) {
  return useForm<T>({
    resolver: zodResolver(schema),
    mode: 'onBlur',
    ...options,
  });
}