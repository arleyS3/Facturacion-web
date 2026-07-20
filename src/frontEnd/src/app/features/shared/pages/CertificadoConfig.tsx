"use client";

import React from "react";
import {
  ChevronDown,
  Eye,
  EyeOff,
  Loader2,
  Search,
  Shield,
  ShieldAlert,
  ShieldCheck,
  Trash2,
} from "lucide-react";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";

interface CertificadoInfo {
  id?: number;
  rucEmisor: string;
  aliasCertificado?: string;
  activo?: boolean;
  fechaVigencia?: string;
  creadoAt?: string;
}

const formatDate = (iso?: string) => {
  if (!iso) return "";
  try {
    return new Intl.DateTimeFormat("es-PE", { dateStyle: "long" }).format(new Date(iso));
  } catch {
    return iso;
  }
};

const getVencimientoInfo = (fecha?: string) => {
  if (!fecha) return null;
  const vence = new Date(fecha);
  const hoy = new Date();
  const diff = vence.getTime() - hoy.getTime();
  const dias = Math.ceil(diff / (1000 * 60 * 60 * 24));
  if (dias < 0) return { label: `Vencido desde hace ${Math.abs(dias)} días`, color: "text-red-600" };
  if (dias === 0) return { label: "Vence hoy", color: "text-red-600" };
  if (dias <= 30) return { label: `Vence en ${dias} días`, color: "text-amber-600" };
  return { label: `Vence: ${formatDate(fecha)}`, color: "text-muted-foreground" };
};

export function CertificadoConfig() {
  const [certificado, setCertificado] = React.useState<CertificadoInfo | null>(null);
  const [cargandoCert, setCargandoCert] = React.useState(false);
  const [consultaRuc, setConsultaRuc] = React.useState("");
  const [certFile, setCertFile] = React.useState<File | null>(null);
  const [certPassword, setCertPassword] = React.useState("");
  const [certRuc, setCertRuc] = React.useState("");
  const [certAlias, setCertAlias] = React.useState("");
  const [showPassword, setShowPassword] = React.useState(false);
  const [guardandoCert, setGuardandoCert] = React.useState(false);
  const [eliminandoCert, setEliminandoCert] = React.useState(false);

  /* ── API ── */

  const consultarCertificado = async () => {
    if (consultaRuc.length !== 11) return;

    setCargandoCert(true);
    setCertificado(null);
    try {
      const res = await api.get(`/configuracion-certificado/${consultaRuc}`);
      setCertificado(res.data);
    } catch (err: any) {
      if (err.response?.status === 404) {
        setCertificado(null);
      } else {
        toast.error(err.response?.data?.message || "Error al consultar certificado");
      }
    } finally {
      setCargandoCert(false);
    }
  };

  const abrirFormConfig = (ruc: string) => {
    setCertRuc(ruc);
    setCertFile(null);
    setCertPassword("");
    setCertAlias("");
  };

  const guardarCertificado = async () => {
    if (!certFile || !certPassword || !certRuc) return;

    setGuardandoCert(true);
    try {
      const base64 = await fileToBase64(certFile);

      const payload: Record<string, string> = {
        rucEmisor: certRuc,
        certificadoBase64: base64,
        password: certPassword,
      };
      if (certAlias.trim()) payload.aliasCertificado = certAlias.trim();

      const res = await api.post("/configuracion-certificado", payload);
      setCertificado(res.data);
      setConsultaRuc(certRuc);
      toast.success("Certificado guardado exitosamente");
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || "Error al guardar certificado";
      toast.error(msg);
    } finally {
      setGuardandoCert(false);
    }
  };

  const eliminarCertificado = async () => {
    if (!certificado?.rucEmisor) return;

    setEliminandoCert(true);
    try {
      await api.delete(`/configuracion-certificado/${certificado.rucEmisor}`);
      setCertificado(null);
      toast.success("Certificado eliminado");
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || "Error al eliminar certificado";
      toast.error(msg);
    } finally {
      setEliminandoCert(false);
    }
  };

  const fileToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const result = reader.result as string;
        resolve(result.split(",")[1]);
      };
      reader.onerror = () => reject(new Error("Error al leer el archivo"));
      reader.readAsDataURL(file);
    });
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-10 space-y-8">
      {/* ── Header ── */}
      <div className="flex items-center gap-3">
        <div className="size-10 rounded-lg bg-primary/10 flex items-center justify-center">
          <Shield className="size-5 text-primary" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-foreground">Certificado Digital</h1>
          <p className="text-sm text-muted-foreground mt-0.5">
            Gestioná tu certificado digital para firmar comprobantes electrónicos
          </p>
        </div>
      </div>

      {/* ════════════════════════════════════════════ */}
      {/* SECCIÓN CERTIFICADO DIGITAL                  */}
      {/* ════════════════════════════════════════════ */}
      <section
        aria-label="Configuración del certificado digital"
        className="bg-card border border-border rounded-xl overflow-hidden"
      >
        <div className="flex items-center justify-between gap-4 px-6 py-4 border-b border-border">
          <div className="flex items-center gap-3 min-w-0">
            <div
              className={`size-10 rounded-lg flex items-center justify-center shrink-0 ${
                certificado
                  ? "bg-green-100 dark:bg-green-900/30"
                  : "bg-muted"
              }`}
            >
              {cargandoCert ? (
                <Loader2 className="size-5 animate-spin text-muted-foreground" />
              ) : certificado ? (
                <ShieldCheck className="size-5 text-green-600 dark:text-green-400" />
              ) : (
                <Shield className="size-5 text-muted-foreground" />
              )}
            </div>
            <div className="min-w-0">
              <h2 className="text-base font-semibold text-foreground truncate">
                Certificado Digital
              </h2>
              {cargandoCert ? (
                <p className="text-xs text-muted-foreground mt-0.5">Consultando...</p>
              ) : certificado ? (
                <div className="flex flex-wrap items-center gap-x-3 gap-y-0.5 mt-0.5">
                  <span className="text-xs text-green-700 dark:text-green-400 font-medium">
                    Configurado para RUC {certificado.rucEmisor}
                  </span>
                  {(() => {
                    const info = getVencimientoInfo(certificado.fechaVigencia);
                    return info ? (
                      <span className={`text-xs ${info.color} dark:opacity-90`}>{info.label}</span>
                    ) : null;
                  })()}
                  {certificado.aliasCertificado && (
                    <span className="text-xs text-muted-foreground">
                      Alias: {certificado.aliasCertificado}
                    </span>
                  )}
                </div>
              ) : (
                <p className="text-xs text-muted-foreground mt-0.5">
                  No hay certificado configurado
                </p>
              )}
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            {certificado && (
              <Button
                variant="outline"
                size="sm"
                onClick={eliminarCertificado}
                disabled={eliminandoCert}
                className="text-red-600 border-red-200 hover:bg-red-50 hover:text-red-700 dark:text-red-400 dark:border-red-800 dark:hover:bg-red-950"
              >
                {eliminandoCert ? (
                  <Loader2 className="size-4 animate-spin" />
                ) : (
                  <Trash2 className="size-4" />
                )}
                <span className="hidden sm:inline">Eliminar</span>
              </Button>
            )}
          </div>
        </div>

        {/* ── Formulario de configuración ── */}
        <div className="p-6 space-y-5">
          <h3 className="text-sm font-semibold text-foreground">
            {certificado && certificado.rucEmisor === certRuc
              ? "Reemplazar certificado"
              : "Configurar certificado"}
          </h3>

          {/* RUC */}
          <div className="space-y-1.5">
            <label htmlFor="cert-ruc" className="text-sm font-medium text-foreground">
              RUC del emisor
            </label>
            <input
              id="cert-ruc"
              type="text"
              maxLength={11}
              placeholder="20100119065"
              value={certRuc}
              onChange={(e) => setCertRuc(e.target.value.replace(/\D/g, ""))}
              className="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            />
          </div>

          {/* Archivo .pfx */}
          <div className="space-y-1.5">
            <label
              htmlFor="cert-file"
              className="flex flex-col items-center justify-center gap-2.5 border-2 border-dashed border-border rounded-lg p-6 cursor-pointer hover:border-primary/50 transition-colors bg-background"
            >
              {certFile ? (
                <>
                  <ShieldCheck className="size-8 text-primary" />
                  <div className="text-center">
                    <p className="text-sm font-medium text-foreground">{certFile.name}</p>
                    <p className="text-xs text-muted-foreground mt-0.5">
                      {(certFile.size / 1024).toFixed(1)} KB
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      setCertFile(null);
                    }}
                    className="text-xs text-muted-foreground hover:text-foreground underline underline-offset-2 transition-colors"
                  >
                    Quitar archivo
                  </button>
                </>
              ) : (
                <>
                  <Shield className="size-8 text-muted-foreground" />
                  <div className="text-center">
                    <p className="text-sm font-medium text-foreground">
                      Seleccioná el archivo .pfx del certificado
                    </p>
                    <p className="text-xs text-muted-foreground mt-1">
                      Archivo PKCS#12 (.pfx o .p12) emitido por SUNAT o entidad autorizada
                    </p>
                  </div>
                </>
              )}
              <input
                id="cert-file"
                type="file"
                accept=".pfx,.p12"
                className="hidden"
                onChange={(e) => {
                  const file = e.target.files?.[0];
                  if (file) {
                    if (!file.name.endsWith(".pfx") && !file.name.endsWith(".p12")) {
                      toast.error("El archivo debe ser .pfx o .p12");
                      return;
                    }
                    setCertFile(file);
                  }
                }}
              />
            </label>
          </div>

          {/* Contraseña */}
          <div className="space-y-1.5">
            <label htmlFor="cert-password" className="text-sm font-medium text-foreground">
              Contraseña del certificado
            </label>
            <div className="relative">
              <input
                id="cert-password"
                type={showPassword ? "text" : "password"}
                placeholder="Contraseña del archivo .pfx"
                value={certPassword}
                onChange={(e) => setCertPassword(e.target.value)}
                autoComplete="new-password"
                className="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 pr-10 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              />
              <button
                type="button"
                onClick={() => setShowPassword((prev) => !prev)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                tabIndex={-1}
              >
                {showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
              </button>
            </div>
          </div>

          {/* Alias (opcional) */}
          <div className="space-y-1.5">
            <label htmlFor="cert-alias" className="text-sm font-medium text-foreground">
              Alias <span className="text-muted-foreground font-normal">(opcional)</span>
            </label>
            <input
              id="cert-alias"
              type="text"
              placeholder="certificado-empresa"
              value={certAlias}
              onChange={(e) => setCertAlias(e.target.value)}
              className="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            />
            <p className="text-xs text-muted-foreground">
              Si se omite, se usa el primer alias con clave privada del .pfx
            </p>
          </div>

          {/* Botones */}
          <div className="flex items-center gap-3 pt-2">
            <Button
              onClick={guardarCertificado}
              disabled={guardandoCert || !certFile || !certPassword || certRuc.length !== 11}
            >
              {guardandoCert ? (
                <>
                  <Loader2 className="size-4 animate-spin" />
                  Guardando...
                </>
              ) : (
                <>
                  <ShieldCheck className="size-4" />
                  {certificado ? "Reemplazar certificado" : "Guardar certificado"}
                </>
              )}
            </Button>
          </div>

          <p className="text-xs text-muted-foreground">
            La contraseña se cifra con AES-256 antes de almacenarse. El certificado se usa
            automáticamente al generar XML o enviar a OSE.
          </p>
        </div>

        {/* ── Consultar certificado existente (colapsable) ── */}
        <details className="border-t border-border group">
          <summary className="px-6 py-3 text-sm font-medium text-muted-foreground hover:text-foreground cursor-pointer select-none flex items-center gap-2 list-none [&::-webkit-details-marker]:hidden">
            <ChevronDown className="size-4 transition-transform group-open:rotate-180" />
            <span>Consultar certificado existente</span>
          </summary>
          <div className="px-6 pb-4 space-y-4">
            <div className="flex items-end gap-3">
              <div className="flex-1 space-y-1.5">
                <label htmlFor="cert-consulta-ruc" className="text-sm font-medium text-foreground">
                  RUC del emisor
                </label>
                <input
                  id="cert-consulta-ruc"
                  type="text"
                  maxLength={11}
                  placeholder="20100119065"
                  value={consultaRuc}
                  onChange={(e) => setConsultaRuc(e.target.value.replace(/\D/g, ""))}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" && consultaRuc.length === 11) consultarCertificado();
                  }}
                  className="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                />
              </div>
              <Button
                variant="secondary"
                onClick={consultarCertificado}
                disabled={consultaRuc.length !== 11 || cargandoCert}
              >
                {cargandoCert ? (
                  <Loader2 className="size-4 animate-spin" />
                ) : (
                  <Search className="size-4" />
                )}
                Consultar
              </Button>
            </div>

            {certificado && (
              <div className="flex items-center gap-2 rounded-lg bg-green-50 dark:bg-green-950/30 border border-green-200 dark:border-green-800 p-3">
                <ShieldCheck className="size-4.5 text-green-600 dark:text-green-400 shrink-0" />
                <div className="text-sm text-green-800 dark:text-green-300 flex-1">
                  <p className="font-medium">
                    Certificado configurado para RUC {certificado.rucEmisor}
                  </p>
                  {(() => {
                    const info = getVencimientoInfo(certificado.fechaVigencia);
                    return info ? (
                      <p className="text-xs mt-0.5">{info.label}</p>
                    ) : null;
                  })()}
                  {certificado.aliasCertificado && (
                    <p className="text-xs mt-0.5 opacity-80">
                      Alias: {certificado.aliasCertificado}
                    </p>
                  )}
                </div>
              </div>
            )}

            {consultaRuc.length === 11 && !cargandoCert && !certificado && (
              <div className="flex items-center gap-2 rounded-lg bg-muted/50 border border-border p-3">
                <ShieldAlert className="size-4.5 text-muted-foreground shrink-0" />
                <p className="text-sm text-muted-foreground flex-1">
                  No hay certificado configurado para este RUC.
                </p>
                <Button size="sm" onClick={() => abrirFormConfig(consultaRuc)}>
                  Configurar ahora
                </Button>
              </div>
            )}
          </div>
        </details>
      </section>
    </div>
  );
}
