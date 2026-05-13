import { Building2, Settings, Loader2, Info } from "lucide-react";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

import { useCatalog } from "@/hooks/useCatalog";
import { fetchRuc } from "@/lib/sunat";
import { useFormContext } from "react-hook-form";
import { useEffect, useRef, useState } from "react";

interface IssuerDataProps {
  showAnexo?: boolean;
  onLoadFromConfig?: () => void;
}

export function IssuerData({ showAnexo = true, onLoadFromConfig }: IssuerDataProps) {
  const methods = useFormContext();
  const watch = methods ? methods.watch : undefined;
  const setValue = methods ? methods.setValue : undefined;

  // =================================================================
  // CAMPOS NUEVOS EN ESPAÑOL (schema unificado)
  // =================================================================
  const emisorRuc = watch ? watch("emisorRuc") ?? "" : "";
  const emisorRazonSocial = watch ? watch("emisorRazonSocial") ?? "" : "";
  const emisorNombreComercial = watch ? watch("emisorNombreComercial") ?? "" : "";
  const emisorDireccion = watch ? watch("emisorDireccion") ?? "" : "";
  const emisorCodigoDomicilio = watch ? watch("emisorCodigoDomicilio") ?? "" : "";
  const emisorTipoDoc = watch ? watch("emisorTipoDoc") ?? "" : "";
  
  // =================================================================
  // CAMPOS LEGACY (para compatibilidad con buildPayload/TXT)
  // =================================================================
  const anexoFromForm = watch ? watch("anexoEmisor") ?? "" : "";
  const nombreComercialEmisor = watch ? watch("nombreComercialEmisor") ?? "" : "";
  const docType = watch ? watch("docType") : undefined;

  const [anexoLocal, setAnexoLocal] = useState("");
  const [soloRucObligatorio, setSoloRucObligatorio] = useState(false);
  const [numeroDocumento, setNumeroDocumento] = useState("");
  const [cargandoRuc, setCargandoRuc] = useState(false);
  const [errorRuc, setErrorRuc] = useState<string | null>(null);
  const [razonSocialLocal, setRazonSocialLocal] = useState("");
  const [direccionLocal, setDireccionLocal] = useState("");
  const refIdRuc = useRef(0);

  const anexo = methods ? anexoFromForm : anexoLocal;

  const { data, loading } = useCatalog("/catalogos/tipos-documento-identidad");
  const opcionesEmisor = data;

  const opcionesFiltradas = soloRucObligatorio
    ? opcionesEmisor?.filter((o: { code: string }) => o.code === "6")
    : opcionesEmisor;

  useEffect(() => {
    //Regla:
    // Si el tipo de documento es Factura, Boleta, Nota de Crédito, Nota de Débito
    // o Guía de Remisión Remitente (código 09), el tipo de documento del emisor
    // solo debe permitir RUC (código 6).
    if (
      docType === "Factura" ||
      docType === "Boleta" ||
      docType === "Nota de Crédito" ||
      docType === "Nota de Débito" ||
      docType === "09" ||
      docType === "Guía Remision" ||
      docType === "Guia Remitente"
    ) {
      setSoloRucObligatorio(true);
      if (setValue) {
        // Nuevo nombre en español
        setValue("emisorTipoDoc", "6");
        // Legacy (para buildPayload)
        setValue("tipoDocEmisor", "6");
      }
    } else {
      setSoloRucObligatorio(false);
    }
  }, [docType, setValue]);

  useEffect(() => {
    if (methods && methods.getValues) {
      // Nuevos nombres en español
      const s = methods.getValues("emisorRazonSocial") || methods.getValues("razonSocialEmisor") || "";
      const a = methods.getValues("emisorDireccion") || methods.getValues("direccionEmisor") || "";
      const n = methods.getValues("emisorRuc") || methods.getValues("numeroDocEmisor") || "";
      setRazonSocialLocal(s);
      setDireccionLocal(a);
      if (!numeroDocumento && n) setNumeroDocumento(n);
    }
  }, []);

  useEffect(() => {
    if (!numeroDocumento) return;

    const handler = setTimeout(async () => {
      if (numeroDocumento.length >= 8) {
        refIdRuc.current += 1;
        const reqId = refIdRuc.current;
        setCargandoRuc(true);
        setErrorRuc(null);

        try {
          const info = await fetchRuc(numeroDocumento);
          if (reqId !== refIdRuc.current) return;

          if (info) {
            const nombre = info.nombre || "";
            const direccion = info.direccion || "";
            setRazonSocialLocal(nombre);
            setDireccionLocal(direccion);

            if (methods && methods.setValue) {
              // Nuevos nombres en español + legacy para compatibilidad
              methods.setValue("emisorRazonSocial", nombre, {
                shouldDirty: true,
                shouldTouch: true,
                shouldValidate: false,
              });
              methods.setValue("emisorDireccion", direccion, {
                shouldDirty: true,
                shouldTouch: true,
                shouldValidate: false,
              });
              // Legacy (para buildPayload)
              methods.setValue("razonSocialEmisor", nombre, {
                shouldDirty: true,
                shouldTouch: true,
                shouldValidate: false,
              });
              methods.setValue("direccionEmisor", direccion, {
                shouldDirty: true,
                shouldTouch: true,
                shouldValidate: false,
              });
            }
            setErrorRuc(null);
          } else {
            setErrorRuc("No se encontró información para ese número");
          }
        } catch {
          if (reqId === refIdRuc.current) {
            setErrorRuc("Error consultando SUNAT");
          }
        } finally {
          if (reqId === refIdRuc.current) {
            setCargandoRuc(false);
          }
        }
      }
    }, 3000);

    return () => clearTimeout(handler);
  }, [numeroDocumento, methods]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="p-2 rounded-lg bg-primary/10">
            <Building2 className="size-5 text-primary" />
          </div>
          <div>
            <h3 className="font-semibold text-lg text-foreground">Datos del Emisor</h3>
            <p className="text-sm text-muted-foreground">Información fiscal del establecimiento</p>
          </div>
        </div>
        {onLoadFromConfig && (
          <Button 
            variant="outline" 
            size="sm"
            onClick={onLoadFromConfig}
          >
            <Settings className="size-4 mr-2" />
            Cargar configuración
          </Button>
        )}
      </div>

      {/* Form Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {/* Tipo de Documento */}
        <div className="space-y-2">
          <Label htmlFor="issuer-doc-type">
            Tipo de Documento
            <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
          </Label>
          {methods ? (
            <Select
              value={emisorTipoDoc || methods.watch("tipoDocEmisor")}
              onValueChange={(v) => {
                if (setValue) {
                  // Nuevo nombre en español
                  setValue("emisorTipoDoc", v);
                  // Legacy (para buildPayload)
                  setValue("tipoDocEmisor", v);
                }
              }}
              defaultValue={opcionesFiltradas?.[0]?.code}
              disabled={soloRucObligatorio}
            >
              <SelectTrigger id="issuer-doc-type" className="h-10">
                <SelectValue placeholder={loading ? "Cargando..." : "Seleccione tipo"} />
              </SelectTrigger>
              <SelectContent>
                {opcionesFiltradas && opcionesFiltradas.length
                  ? opcionesFiltradas.map((opt: { code: string; label: string }) => (
                      <SelectItem key={opt.code} value={opt.code}>
                        {opt.label}
                      </SelectItem>
                    ))
                  : (
                    <>
                      <SelectItem value="6">RUC</SelectItem>
                      <SelectItem value="1">DNI</SelectItem>
                      <SelectItem value="4">Carnet de Extranjería</SelectItem>
                      <SelectItem value="7">Pasaporte</SelectItem>
                    </>
                  )}
              </SelectContent>
            </Select>
          ) : (
            <Select defaultValue={opcionesFiltradas?.[0]?.code ?? "6"} disabled={soloRucObligatorio}>
              <SelectTrigger id="issuer-doc-type" className="h-10">
                <SelectValue placeholder={loading ? "Cargando..." : "Seleccione tipo"} />
              </SelectTrigger>
              <SelectContent>
                {opcionesFiltradas && opcionesFiltradas.length
                  ? opcionesFiltradas.map((opt: { code: string; label: string }) => (
                      <SelectItem key={opt.code} value={opt.code}>
                        {opt.label}
                      </SelectItem>
                    ))
                  : (
                    <>
                      <SelectItem value="6">RUC</SelectItem>
                      <SelectItem value="1">DNI</SelectItem>
                      <SelectItem value="4">Carnet de Extranjería</SelectItem>
                      <SelectItem value="7">Pasaporte</SelectItem>
                    </>
                  )}
              </SelectContent>
            </Select>
          )}
        </div>

        {/* Número de Documento */}
        <div className="space-y-2">
          <Label htmlFor="issuer-doc-number">
            Número de Documento
            <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
          </Label>
          <div className="relative">
            <Input 
              id="issuer-doc-number" 
              placeholder="20123456789"
              className="font-mono h-10 pr-10"
              maxLength={11}
              value={emisorRuc || numeroDocumento}
              onChange={(e) => {
                const val = e.target.value.trim();
                setNumeroDocumento(val);
                if (methods && methods.setValue) {
                  // Nuevo nombre en español
                  methods.setValue("emisorRuc", val);
                  // Legacy (para buildPayload)
                  methods.setValue("numeroDocEmisor", val);
                }
              }}
              aria-describedby={cargandoRuc ? "doc-loading" : errorRuc ? "doc-error" : undefined}
            />
            {cargandoRuc && (
              <div className="absolute right-3 top-1/2 -translate-y-1/2">
                <Loader2 className="size-4 animate-spin text-muted-foreground" />
              </div>
            )}
          </div>
          {cargandoRuc && (
            <p id="doc-loading" className="text-xs text-muted-foreground flex items-center gap-1">
              <Loader2 className="size-3 animate-spin" />
              Consultando RUC en SUNAT...
            </p>
          )}
          {errorRuc && (
            <p id="doc-error" className="text-xs text-destructive flex items-center gap-1" role="alert">
              <Info className="size-3" />
              {errorRuc}
            </p>
          )}
        </div>

        {/* Razón Social */}
        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="issuer-social-name">
            Razón Social
            <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
          </Label>
          <Input 
            id="issuer-social-name" 
            placeholder="Nombre o razón social del emisor"
            className="uppercase h-10"
            value={emisorRazonSocial || razonSocialLocal}
            onChange={(e) => {
              const v = e.target.value;
              setRazonSocialLocal(v);
              if (methods && methods.setValue) {
                // Nuevo nombre en español
                methods.setValue("emisorRazonSocial", v);
                // Legacy (para buildPayload)
                methods.setValue("razonSocialEmisor", v);
              }
            }}
          />
        </div>

        {/* Nombre Comercial */}
        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="issuer-commercial-name" className="text-sm">
            Nombre Comercial
            <span className="text-muted-foreground ml-1">(opcional)</span>
          </Label>
          <Input
            id="issuer-commercial-name"
            placeholder="Nombre comercial del establecimiento"
            className="h-10"
            value={emisorNombreComercial}
            onChange={(e) => {
              const v = e.target.value;
              if (methods?.setValue) {
                // Nuevo nombre en español
                methods.setValue("emisorNombreComercial", v);
                // Legacy (para buildPayload)
                methods.setValue("nombreComercialEmisor", v);
              }
            }}
          />
        </div>

        {/* Dirección */}
        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="issuer-address">
            Dirección
            <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
          </Label>
          <Input
            id="issuer-address"
            placeholder="Dirección del establecimiento"
            className="h-10"
            value={emisorDireccion || direccionLocal}
            onChange={(e) => {
              const v = e.target.value;
              setDireccionLocal(v);
              if (methods && methods.setValue) {
                // Nuevo nombre en español
                methods.setValue("emisorDireccion", v);
                // Legacy (para buildPayload)
                methods.setValue("direccionEmisor", v);
              }
            }}
          />
        </div>
      </div>

      {/* Sección de Anexo - Simplificada */}
      {showAnexo && (
        <Card>
          <CardContent className="p-4 space-y-4">
            <div className="flex items-center gap-2">
              <Info className="size-4 text-muted-foreground" />
              <h4 className="font-medium text-sm">Código de Local Anexo</h4>
            </div>
            
            <div className="space-y-3">
              <div className="space-y-2">
                <Input 
                  id="issuer-annex" 
                  placeholder="0000"
                  value={emisorCodigoDomicilio || anexo}
                  onChange={(e) => {
                    const v = e.target.value;
                    if (methods?.setValue) {
                      // Nuevo nombre en español
                      methods.setValue("emisorCodigoDomicilio", v);
                      // Legacy (para buildPayload)
                      methods.setValue("anexoEmisor", v);
                    } else {
                      setAnexoLocal(v);
                    }
                  }}
                  className="font-mono h-9 w-24"
                  maxLength={4}
                />
                <p className="text-xs text-muted-foreground">
                  Use <strong>0000</strong> para domicilio fiscal
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
