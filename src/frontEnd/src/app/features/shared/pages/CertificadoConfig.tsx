"use client";

import React from "react";
import {
  Shield,
  ShieldCheck,
  ShieldAlert,
  Search,
  Plus,
  Loader2,
  Trash2,
  Eye,
  EyeOff,
  RefreshCw,
  FileCheck,
  Building2,
  Calendar,
} from "lucide-react";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { toast } from "sonner";

interface CertificadoInfo {
  id?: number;
  rucEmisor: string;
  aliasCertificado?: string;
  activo?: boolean;
  fechaVigencia?: string;
  creadoAt?: string;
  mensaje?: string;
}

const formatDate = (iso?: string) => {
  if (!iso) return "No registrada";
  try {
    return new Intl.DateTimeFormat("es-PE", {
      dateStyle: "medium",
    }).format(new Date(iso));
  } catch {
    return iso;
  }
};

const getVencimientoInfo = (fecha?: string) => {
  if (!fecha) return { label: "Sin vigencia", color: "text-slate-500" };
  const vence = new Date(fecha);
  const hoy = new Date();
  const diff = vence.getTime() - hoy.getTime();
  const dias = Math.ceil(diff / (1000 * 60 * 60 * 24));
  if (dias < 0) return { label: `Vencido (${Math.abs(dias)}d)`, color: "text-red-600" };
  if (dias === 0) return { label: "Vence hoy", color: "text-red-600" };
  if (dias <= 30) return { label: `Vence en ${dias}d`, color: "text-amber-600" };
  return { label: `Válido hasta ${formatDate(fecha)}`, color: "text-emerald-600" };
};

export function CertificadoConfig() {
  const [certificados, setCertificados] = React.useState<CertificadoInfo[]>([]);
  const [cargandoLista, setCargandoLista] = React.useState(true);
  const [searchTerm, setSearchTerm] = React.useState("");

  // Modal de Subida / Modificación
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [certRuc, setCertRuc] = React.useState("");
  const [certAlias, setCertAlias] = React.useState("");
  const [certPassword, setCertPassword] = React.useState("");
  const [certFechaVigencia, setCertFechaVigencia] = React.useState("");
  const [certFile, setCertFile] = React.useState<File | null>(null);
  const [showPassword, setShowPassword] = React.useState(false);
  const [guardandoCert, setGuardandoCert] = React.useState(false);
  const [togglingRuc, setTogglingRuc] = React.useState<string | null>(null);

  const cargarCertificados = React.useCallback(async () => {
    setCargandoLista(true);
    try {
      const res = await api.get<CertificadoInfo[]>("/configuracion-certificado");
      setCertificados(res.data || []);
    } catch (err: any) {
      console.error("Error al cargar lista de certificados", err);
      toast.error("No se pudo cargar la lista de certificados");
    } finally {
      setCargandoLista(false);
    }
  }, []);

  React.useEffect(() => {
    void cargarCertificados();
  }, [cargarCertificados]);

  const abrirModalNuevo = () => {
    setCertRuc("");
    setCertAlias("");
    setCertPassword("");
    setCertFechaVigencia("");
    setCertFile(null);
    setShowPassword(false);
    setDialogOpen(true);
  };

  const abrirModalEditar = (cert: CertificadoInfo) => {
    setCertRuc(cert.rucEmisor);
    setCertAlias(cert.aliasCertificado || "");
    setCertPassword("");
    setCertFechaVigencia(cert.fechaVigencia ? cert.fechaVigencia.split("T")[0] : "");
    setCertFile(null);
    setShowPassword(false);
    setDialogOpen(true);
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

  const guardarCertificado = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!certRuc || certRuc.length !== 11) {
      toast.error("El RUC emisor debe tener exactamente 11 dígitos");
      return;
    }
    if (!certFile) {
      toast.error("Debe seleccionar el archivo .pfx o .p12 del certificado");
      return;
    }
    if (!certPassword) {
      toast.error("Ingrese la contraseña del certificado");
      return;
    }

    setGuardandoCert(true);
    try {
      const base64 = await fileToBase64(certFile);
      const payload: Record<string, string> = {
        rucEmisor: certRuc,
        certificadoBase64: base64,
        password: certPassword,
      };
      if (certAlias.trim()) payload.aliasCertificado = certAlias.trim();
      if (certFechaVigencia) payload.fechaVigencia = certFechaVigencia;

      const res = await api.post<CertificadoInfo>("/configuracion-certificado", payload);

      toast.success(res.data.mensaje || "Certificado guardado y activado exitosamente");
      setDialogOpen(false);
      await cargarCertificados();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || "Error al guardar el certificado";
      toast.error(msg);
    } finally {
      setGuardandoCert(false);
    }
  };

  const toggleEstado = async (rucEmisor: string, estadoActual?: boolean) => {
    setTogglingRuc(rucEmisor);
    try {
      const nuevoEstado = !Boolean(estadoActual);
      const res = await api.put<CertificadoInfo>(
        `/configuracion-certificado/${rucEmisor}/estado?activo=${nuevoEstado}`
      );
      toast.success(res.data.mensaje || `Certificado ${nuevoEstado ? "activado" : "desactivado"}`);
      await cargarCertificados();
    } catch (err: any) {
      const msg = err.response?.data?.message || "Error al cambiar el estado del certificado";
      toast.error(msg);
    } finally {
      setTogglingRuc(null);
    }
  };

  const eliminarCertificado = async (rucEmisor: string) => {
    if (!confirm(`¿Está seguro de desactivar el certificado para RUC ${rucEmisor}?`)) return;

    try {
      const res = await api.delete<CertificadoInfo>(`/configuracion-certificado/${rucEmisor}`);
      toast.success(res.data.mensaje || "Certificado desactivado");
      await cargarCertificados();
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Error al desactivar el certificado");
    }
  };

  const certificadosFiltrados = certificados.filter(
    (c) =>
      c.rucEmisor.includes(searchTerm) ||
      (c.aliasCertificado && c.aliasCertificado.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const totalCertificados = certificados.length;
  const totalActivos = certificados.filter((c) => c.activo).length;
  const totalInactivos = totalCertificados - totalActivos;

  return (
    <div className="container mx-auto px-4 py-8 space-y-6 max-w-6xl">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-3 rounded-xl bg-blue-600/10 text-blue-600 dark:bg-blue-400/20 dark:text-blue-400">
            <Shield className="size-6" aria-hidden="true" />
          </div>
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-foreground">
              Certificados Digitales
            </h1>
            <p className="text-sm text-muted-foreground">
              Gestión de certificados PKCS#12 (.pfx) para la firma electrónica UBL
            </p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={cargarCertificados}
            disabled={cargandoLista}
            className="gap-2"
          >
            <RefreshCw className={`size-4 ${cargandoLista ? "animate-spin" : ""}`} />
            Actualizar
          </Button>
          <Button size="sm" onClick={abrirModalNuevo} className="gap-2 bg-blue-600 hover:bg-blue-700">
            <Plus className="size-4" />
            Nuevo Certificado
          </Button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="shadow-sm">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-xs font-medium text-muted-foreground uppercase">Total Registrados</p>
              <h2 className="text-2xl font-bold mt-1">{totalCertificados}</h2>
            </div>
            <div className="p-2.5 bg-muted rounded-lg">
              <FileCheck className="size-5 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>
        <Card className="shadow-sm border-emerald-200 dark:border-emerald-900/50">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-xs font-medium text-emerald-600 dark:text-emerald-400 uppercase">
                Activos para Firma
              </p>
              <h2 className="text-2xl font-bold mt-1 text-emerald-700 dark:text-emerald-300">
                {totalActivos}
              </h2>
            </div>
            <div className="p-2.5 bg-emerald-100 dark:bg-emerald-900/30 rounded-lg">
              <ShieldCheck className="size-5 text-emerald-600 dark:text-emerald-400" />
            </div>
          </CardContent>
        </Card>
        <Card className="shadow-sm border-slate-200 dark:border-slate-800">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-xs font-medium text-muted-foreground uppercase">Inactivos / Desactivados</p>
              <h2 className="text-2xl font-bold mt-1 text-slate-700 dark:text-slate-300">
                {totalInactivos}
              </h2>
            </div>
            <div className="p-2.5 bg-slate-100 dark:bg-slate-800 rounded-lg">
              <ShieldAlert className="size-5 text-slate-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Main Table Card */}
      <Card className="shadow-md border border-slate-200 dark:border-slate-800">
        <CardHeader className="pb-3">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div>
              <CardTitle className="text-base font-semibold">Lista de Certificados</CardTitle>
              <CardDescription className="text-xs">
                Certificados guardados en el sistema organizados por RUC Emisor
              </CardDescription>
            </div>
            <div className="relative w-full sm:w-64">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
              <Input
                placeholder="Buscar por RUC o alias..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-9 h-9 text-xs"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent className="p-0">
          {cargandoLista ? (
            <div className="flex flex-col items-center justify-center py-12 text-muted-foreground gap-3">
              <Loader2 className="size-6 animate-spin text-blue-600" />
              <p className="text-sm">Cargando certificados...</p>
            </div>
          ) : certificadosFiltrados.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground gap-3">
              <Shield className="size-10 text-slate-300 dark:text-slate-700" />
              <div>
                <p className="text-sm font-medium text-foreground">
                  {searchTerm ? "No se encontraron certificados" : "No hay certificados registrados"}
                </p>
                <p className="text-xs text-muted-foreground mt-1">
                  {searchTerm
                    ? "Intente buscar con otro RUC o alias"
                    : "Haga clic en 'Nuevo Certificado' para subir el primer archivo PKCS#12 (.pfx)"}
                </p>
              </div>
              {!searchTerm && (
                <Button size="sm" onClick={abrirModalNuevo} className="mt-2 gap-2">
                  <Plus className="size-4" />
                  Agregar Certificado
                </Button>
              )}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader className="bg-slate-50 dark:bg-slate-900/50">
                  <TableRow>
                    <TableHead className="w-[180px]">RUC Emisor</TableHead>
                    <TableHead className="w-[200px]">Alias</TableHead>
                    <TableHead className="w-[140px]">Estado</TableHead>
                    <TableHead className="w-[180px]">Vigencia</TableHead>
                    <TableHead className="w-[140px]">Registrado</TableHead>
                    <TableHead className="text-right w-[140px]">Acciones</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {certificadosFiltrados.map((cert) => {
                    const venc = getVencimientoInfo(cert.fechaVigencia);
                    const isToggling = togglingRuc === cert.rucEmisor;

                    return (
                      <TableRow key={cert.rucEmisor} className="hover:bg-slate-50/50 dark:hover:bg-slate-900/50">
                        {/* RUC */}
                        <TableCell className="font-mono text-sm font-semibold text-foreground">
                          <div className="flex items-center gap-2">
                            <Building2 className="size-4 text-blue-600 shrink-0" />
                            <span>{cert.rucEmisor}</span>
                          </div>
                        </TableCell>

                        {/* Alias */}
                        <TableCell className="text-sm">
                          <span className="text-slate-700 dark:text-slate-300 font-medium">
                            {cert.aliasCertificado || `certificado-${cert.rucEmisor}`}
                          </span>
                        </TableCell>

                        {/* Estado (Switch Interactivo) */}
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Switch
                              checked={Boolean(cert.activo)}
                              disabled={isToggling}
                              onCheckedChange={() => toggleEstado(cert.rucEmisor, cert.activo)}
                              aria-label={`Cambiar estado del certificado RUC ${cert.rucEmisor}`}
                            />
                            <Badge
                              variant={cert.activo ? "default" : "secondary"}
                              className={
                                cert.activo
                                  ? "bg-emerald-600 hover:bg-emerald-700 text-white"
                                  : "bg-slate-200 text-slate-700 dark:bg-slate-800 dark:text-slate-400"
                              }
                            >
                              {isToggling ? "Actualizando..." : cert.activo ? "Activo" : "Inactivo"}
                            </Badge>
                          </div>
                        </TableCell>

                        {/* Vigencia */}
                        <TableCell>
                          <div className="flex items-center gap-1.5 text-xs">
                            <Calendar className="size-3.5 text-muted-foreground shrink-0" />
                            <span className={venc.color}>{venc.label}</span>
                          </div>
                        </TableCell>

                        {/* Fecha Creación */}
                        <TableCell className="text-xs text-muted-foreground">
                          {formatDate(cert.creadoAt)}
                        </TableCell>

                        {/* Acciones */}
                        <TableCell className="text-right">
                          <div className="flex items-center justify-end gap-1">
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => abrirModalEditar(cert)}
                              title="Reemplazar / Modificar certificado"
                              className="h-8 px-2 text-xs"
                            >
                              <RefreshCw className="size-3.5 mr-1" />
                              Reemplazar
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => eliminarCertificado(cert.rucEmisor)}
                              title="Desactivar certificado"
                              className="h-8 w-8 p-0 text-destructive hover:bg-destructive/10"
                            >
                              <Trash2 className="size-3.5" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Modal Dialog para Agregar / Reemplazar Certificado */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <ShieldCheck className="size-5 text-blue-600" />
              {certificados.some((c) => c.rucEmisor === certRuc)
                ? "Reemplazar Certificado Digital"
                : "Nuevo Certificado Digital"}
            </DialogTitle>
            <DialogDescription className="text-xs">
              Cargue el archivo PKCS#12 (.pfx / .p12) provisto por SUNAT o la Entidad Certificadora.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={guardarCertificado} className="space-y-4 py-2">
            {/* RUC Emisor */}
            <div className="space-y-1.5">
              <Label htmlFor="modal-ruc" className="text-xs font-semibold">
                RUC del Emisor <span className="text-destructive">*</span>
              </Label>
              <Input
                id="modal-ruc"
                placeholder="20100119065"
                maxLength={11}
                value={certRuc}
                onChange={(e) => setCertRuc(e.target.value.replace(/\D/g, ""))}
                className="font-mono text-sm h-10"
                required
              />
            </div>

            {/* Archivo .pfx / .p12 */}
            <div className="space-y-1.5">
              <Label htmlFor="modal-file" className="text-xs font-semibold">
                Archivo del Certificado (.pfx / .p12) <span className="text-destructive">*</span>
              </Label>
              <div
                onClick={() => document.getElementById("modal-file")?.click()}
                className="border-2 border-dashed border-slate-300 dark:border-slate-700 rounded-lg p-5 cursor-pointer hover:border-blue-500 transition-colors bg-slate-50/50 dark:bg-slate-900/50 flex flex-col items-center justify-center gap-2 text-center"
              >
                {certFile ? (
                  <>
                    <FileCheck className="size-8 text-blue-600" />
                    <div>
                      <p className="text-sm font-semibold text-foreground">{certFile.name}</p>
                      <p className="text-xs text-muted-foreground mt-0.5">
                        {(certFile.size / 1024).toFixed(1)} KB
                      </p>
                    </div>
                  </>
                ) : (
                  <>
                    <Shield className="size-8 text-muted-foreground" />
                    <div>
                      <p className="text-xs font-medium text-foreground">
                        Haga clic para examinar su equipo
                      </p>
                      <p className="text-[11px] text-muted-foreground mt-0.5">
                        Formatos soportados: .pfx o .p12
                      </p>
                    </div>
                  </>
                )}
                <input
                  id="modal-file"
                  type="file"
                  accept=".pfx,.p12"
                  className="hidden"
                  onChange={(e) => {
                    const f = e.target.files?.[0];
                    if (f) {
                      if (!f.name.endsWith(".pfx") && !f.name.endsWith(".p12")) {
                        toast.error("El archivo debe tener extensión .pfx o .p12");
                        return;
                      }
                      setCertFile(f);
                    }
                  }}
                />
              </div>
            </div>

            {/* Contraseña */}
            <div className="space-y-1.5">
              <Label htmlFor="modal-password" className="text-xs font-semibold">
                Contraseña del Certificado <span className="text-destructive">*</span>
              </Label>
              <div className="relative">
                <Input
                  id="modal-password"
                  type={showPassword ? "text" : "password"}
                  placeholder="Contraseña del archivo .pfx"
                  value={certPassword}
                  onChange={(e) => setCertPassword(e.target.value)}
                  className="pr-10 h-10"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((prev) => !prev)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                >
                  {showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
                </button>
              </div>
            </div>

            {/* Alias opcional */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="modal-alias" className="text-xs font-semibold">
                  Alias (Opcional)
                </Label>
                <Input
                  id="modal-alias"
                  placeholder="autonomiflow-demo"
                  value={certAlias}
                  onChange={(e) => setCertAlias(e.target.value)}
                  className="h-9 text-xs"
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="modal-vigencia" className="text-xs font-semibold">
                  Fecha de Vigencia (Opcional)
                </Label>
                <Input
                  id="modal-vigencia"
                  type="date"
                  value={certFechaVigencia}
                  onChange={(e) => setCertFechaVigencia(e.target.value)}
                  className="h-9 text-xs"
                />
              </div>
            </div>

            <DialogFooter className="pt-3 gap-2">
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)} disabled={guardandoCert}>
                Cancelar
              </Button>
              <Button type="submit" disabled={guardandoCert} className="bg-blue-600 hover:bg-blue-700">
                {guardandoCert ? (
                  <>
                    <Loader2 className="size-4 mr-2 animate-spin" />
                    Guardando...
                  </>
                ) : (
                  <>
                    <ShieldCheck className="size-4 mr-2" />
                    Guardar y Activar
                  </>
                )}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default CertificadoConfig;
