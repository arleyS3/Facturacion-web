import React, { useState } from "react";
import { useNavigate, Link } from "react-router";
import { useForm } from "react-hook-form";
import { Eye, EyeOff, AlertCircle, Loader2 } from "lucide-react";

import { api, REFRESH_TOKEN_KEY, setAccessToken } from "@/lib/api";
import { loginSchema, type LoginFormData } from "@/lib/schemas/login.schema";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import logoEmpresa from "@/assets/logo-empresa.png";

type ZodIssue = { path: (string | number)[]; message: string; code?: string };

const toRHFErrors = (issues: ZodIssue[]): Record<string, any> => {
  const errors: Record<string, any> = {};
  for (const issue of issues) {
    const key = (issue.path?.[0] ?? "root") as string;
    if (!errors[key]) {
      errors[key] = { type: issue.code ?? "custom", message: issue.message };
    }
  }
  return errors;
};

const resolver = (values: LoginFormData) => {
  const result = loginSchema.safeParse(values);
  if (result.success) return { values, errors: {} };
  return {
    values: {} as LoginFormData,
    errors: toRHFErrors(result.error.issues as ZodIssue[]),
  };
};

export const Login: React.FC = () => {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver,
    mode: "onSubmit",
    reValidateMode: "onChange",
    defaultValues: { email: "", password: "" },
  });

  const [showPassword, setShowPassword] = useState(false);

  const onSubmit = async (data: LoginFormData) => {
    try {
      const response = await api.post("/auth/login", data);

      if (response.status === 200) {
        // Guardar token en memoria (no localStorage) para requests autenticadas
        if (response.data?.accessToken) {
          setAccessToken(response.data.accessToken);
        }
        if (response.data?.refreshToken) {
          localStorage.setItem(REFRESH_TOKEN_KEY, response.data.refreshToken);
        }
        if (data.email) {
          localStorage.setItem("user_email", data.email);
        }
        navigate("/home", { replace: true });
        return;
      }

      // status no es 200
      const message = response.data?.message ?? "Error de autenticación";
      setError("root" as any, { type: "server", message });
    } catch (err: any) {
      const message =
        err?.response?.data?.message ??
        "Error de conexión. Verifica que el servidor Backend esté encendido.";
      setError("root" as any, { type: "server", message });
    }
  };

  return (
    <div className="flex min-h-dvh w-full bg-background">
      {/* ── Lado izquierdo: Branding ── */}
      <div className="hidden lg:flex lg:w-1/2 items-center justify-center bg-card relative overflow-hidden">
        <img
          src={logoEmpresa}
          alt="Logo de la Empresa"
          className="w-full h-full object-contain p-12"
        />
      </div>

      {/* ── Lado derecho: Formulario ── */}
      <div className="flex w-full lg:w-1/2 items-center justify-center p-6 sm:p-10">
        <div className="w-full max-w-md space-y-8">
          {/* Header */}
          <div className="space-y-1.5">
            <h2 className="text-2xl font-bold tracking-tight text-foreground">
              ¡Bienvenido a AutonomiFlow!
            </h2>
            <p className="text-sm text-muted-foreground leading-relaxed">
              Emisor de comprobantes electrónicos SUNAT
            </p>
          </div>

          <form
            className="space-y-5"
            onSubmit={handleSubmit(onSubmit)}
            noValidate
          >
            {/* Error general del backend */}
            {errors.root && (
              <div
                role="alert"
                aria-live="polite"
                className="flex items-start gap-3 p-4 bg-destructive/10 border border-destructive/20 rounded-lg text-destructive text-sm"
              >
                <AlertCircle className="size-4 mt-0.5 shrink-0 text-destructive" />
                <span>{errors.root.message}</span>
              </div>
            )}

            {/* Email */}
            <div className="space-y-1.5">
              <Label htmlFor="email">
                Correo electrónico
                <span className="text-destructive ml-0.5" aria-hidden="true">
                  *
                </span>
              </Label>
              <Input
                id="email"
                type="email"
                placeholder="correo@ejemplo.com"
                autoComplete="email"
                aria-required="true"
                aria-invalid={!!errors.email}
                aria-describedby={errors.email ? "email-error" : undefined}
                {...register("email")}
                className={
                  errors.email
                    ? "border-destructive focus-visible:ring-destructive/50"
                    : ""
                }
              />
              {errors.email && (
                <p
                  id="email-error"
                  role="alert"
                  className="text-xs text-destructive flex items-center gap-1"
                >
                  <AlertCircle className="size-3" />
                  {errors.email.message}
                </p>
              )}
            </div>

            {/* Contraseña */}
            <div className="space-y-1.5">
              <Label htmlFor="password">
                Contraseña
                <span className="text-destructive ml-0.5" aria-hidden="true">
                  *
                </span>
              </Label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  placeholder="••••••••"
                  autoComplete="current-password"
                  aria-required="true"
                  aria-invalid={!!errors.password}
                  aria-describedby={
                    errors.password ? "password-error" : "password-hint"
                  }
                  {...register("password")}
                  className={`pr-10 ${errors.password ? "border-destructive focus-visible:ring-destructive/50" : ""}`}
                />
                <button
                  type="button"
                  aria-label={
                    showPassword ? "Ocultar contraseña" : "Mostrar contraseña"
                  }
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground/80 transition-colors cursor-pointer"
                  onClick={() => setShowPassword((prev) => !prev)}
                >
                  {showPassword ? (
                    <EyeOff className="size-4" />
                  ) : (
                    <Eye className="size-4" />
                  )}
                </button>
              </div>
              {errors.password ? (
                <p
                  id="password-error"
                  role="alert"
                  className="text-xs text-destructive flex items-center gap-1"
                >
                  <AlertCircle className="size-3" />
                  {errors.password.message}
                </p>
              ) : (
                <p id="password-hint" className="text-xs text-muted-foreground">
                  Al menos 8 caracteres
                </p>
              )}
              <div className="flex justify-end">
                <button
                  type="button"
                  className="text-xs font-medium text-primary hover:text-primary-hover transition-colors cursor-pointer"
                >
                  ¿Olvidaste tu contraseña?
                </button>
              </div>
            </div>

            {/* Submit */}
            <Button
              className="w-full"
              type="submit"
              disabled={isSubmitting}
              aria-disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="size-4 animate-spin" />
                  Conectando...
                </>
              ) : (
                "Iniciar sesión"
              )}
            </Button>
          </form>

          {/* Registro */}
          <p className="text-center text-sm text-muted-foreground">
            ¿No tienes una cuenta?{" "}
            <Link
              to="/registro"
              className="font-medium text-primary hover:text-primary-hover transition-colors"
            >
              Regístrate aquí
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
