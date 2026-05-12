import { Building2, HelpCircle, Settings } from "lucide-react";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { Button } from "@/components/ui/button";

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

  const noDetFromForm = watch ? Boolean(watch("issuerNoDet")) : false;
  const anexoFromForm = watch ? watch("issuerAnexo") ?? "" : "";
  const issuerCommercialName = watch ? watch("issuerCommercialName") ?? "" : "";
  const docType = watch ? watch("docType") : undefined;

  const [noDetLocal, setNoDetLocal] = useState(false);
  const [anexoLocal, setAnexoLocal] = useState("");
  const [forcedOnlyRuc, setForcedOnlyRuc] = useState(false);
  const [issuerNumber, setIssuerNumber] = useState("");
  const [rucLoading, setRucLoading] = useState(false);
  const [rucError, setRucError] = useState<string | null>(null);
  const [issuerSocialNameLocal, setIssuerSocialNameLocal] = useState("");
  const [issuerAddressLocal, setIssuerAddressLocal] = useState("");
  const issuerReqId = useRef(0);

  const noDet = methods ? noDetFromForm : noDetLocal;
  const anexo = methods ? anexoFromForm : anexoLocal;

  const { data, loading } = useCatalog("/catalogos/tipos-documento-identidad");
  const issuerOptions = data;

  const optionsToShow = forcedOnlyRuc
    ? issuerOptions?.filter((o) => o.code === "6")
    : issuerOptions;

  const handleNoDetChange = (checked: boolean) => {
    if (methods && setValue) {
      setValue("issuerNoDet", checked);
      setValue("issuerAnexo", checked ? "0000" : "");
    } else {
      setNoDetLocal(checked);
      setAnexoLocal(checked ? "0000" : "");
    }
  };

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
      setForcedOnlyRuc(true);
      setValue && setValue("issuerDocType", "6");
    } else {
      setForcedOnlyRuc(false);
    }
  }, [docType, setValue]);

  useEffect(() => {
    if (methods && methods.getValues) {
      const s = methods.getValues("issuerSocialName") || "";
      const a = methods.getValues("issuerAddress") || "";
      const n = methods.getValues("issuerDocNumber") || "";
      setIssuerSocialNameLocal(s);
      setIssuerAddressLocal(a);
      if (!issuerNumber && n) setIssuerNumber(n);
    }
  }, []);

  useEffect(() => {
    if (!issuerNumber) return;

    const handler = setTimeout(async () => {
      if (issuerNumber.length >= 8) {
        issuerReqId.current += 1;
        const reqId = issuerReqId.current;
        setRucLoading(true);
        setRucError(null);

        try {
          const info = await fetchRuc(issuerNumber);
          if (reqId !== issuerReqId.current) return;

          if (info) {
            const nombre = info.nombre || "";
            const direccion = info.direccion || "";
            setIssuerSocialNameLocal(nombre);
            setIssuerAddressLocal(direccion);

            if (methods && methods.setValue) {
              methods.setValue("issuerSocialName", nombre, {
                shouldDirty: true,
                shouldTouch: true,
                shouldValidate: false,
              });
              methods.setValue("issuerAddress", direccion, {
                shouldDirty: true,
                shouldTouch: true,
                shouldValidate: false,
              });
            }
            setRucError(null);
          } else {
            setRucError("No se encontró información para ese número");
          }
        } catch {
          if (reqId === issuerReqId.current) {
            setRucError("Error consultando SUNAT");
          }
        } finally {
          if (reqId === issuerReqId.current) {
            setRucLoading(false);
          }
        }
      }
    }, 3000);

    return () => clearTimeout(handler);
  }, [issuerNumber, methods]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <Building2 className="size-5 text-blue-600" />
          <h3 className="font-semibold text-lg">Datos del Emisor</h3>
        </div>
        {onLoadFromConfig && (
          <Button 
            variant="outline" 
            size="sm"
            onClick={onLoadFromConfig}
            className="text-xs"
          >
            <Settings className="size-3 mr-1" />
            Cargar desde configuración
          </Button>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="issuer-doc-type">Tipo de Documento</Label>
          {methods ? (
            <Select
              value={methods.watch("issuerDocType")}
              onValueChange={(v) => setValue && setValue("issuerDocType", v)}
              defaultValue={optionsToShow?.[0]?.code}
              disabled={forcedOnlyRuc}
            >
              <SelectTrigger id="issuer-doc-type">
                <SelectValue placeholder={loading ? "Cargando..." : "Seleccione tipo"} />
              </SelectTrigger>
              <SelectContent>
                {optionsToShow && optionsToShow.length
                  ? optionsToShow.map((opt) => (
                      <SelectItem key={opt.code} value={opt.code}>
                        {opt.label}
                      </SelectItem>
                    ))
                  : (
                    <>
                      <SelectItem value="6">6 - RUC</SelectItem>
                      <SelectItem value="1">1 - DNI</SelectItem>
                      <SelectItem value="4">4 - Carnet de Extranjería</SelectItem>
                      <SelectItem value="7">7 - Pasaporte</SelectItem>
                    </>
                  )}
              </SelectContent>
            </Select>
          ) : (
            <Select defaultValue={optionsToShow?.[0]?.code ?? "6"} disabled={forcedOnlyRuc}>
              <SelectTrigger id="issuer-doc-type">
                <SelectValue placeholder={loading ? "Cargando..." : "Seleccione tipo"} />
              </SelectTrigger>
              <SelectContent>
                {optionsToShow && optionsToShow.length
                  ? optionsToShow.map((opt) => (
                      <SelectItem key={opt.code} value={opt.code}>
                        {opt.label}
                      </SelectItem>
                    ))
                  : (
                    <>
                      <SelectItem value="6">6 - RUC</SelectItem>
                      <SelectItem value="1">1 - DNI</SelectItem>
                      <SelectItem value="4">4 - Carnet de Extranjería</SelectItem>
                      <SelectItem value="7">7 - Pasaporte</SelectItem>
                    </>
                  )}
              </SelectContent>
            </Select>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="issuer-doc-number">Número de Documento</Label>
          <Input 
            id="issuer-doc-number" 
            placeholder="20123456789"
            className="font-mono"
            maxLength={11}
            value={issuerNumber}
            onChange={(e) => {
              const val = e.target.value.trim();
              setIssuerNumber(val);
              if (methods && methods.setValue) methods.setValue("issuerDocNumber", val);
            }}
          />
          {rucLoading ? (
            <p className="text-sm text-slate-500">Consultando RUC...</p>
          ) : rucError ? (
            <p className="text-sm text-red-500">{rucError}</p>
          ) : null}
        </div>

        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="issuer-social-name">Razón Social</Label>
          <Input 
            id="issuer-social-name" 
            placeholder="Razón social del emisor"
            className="uppercase"
            value={issuerSocialNameLocal}
            onChange={(e) => {
              const v = e.target.value;
              setIssuerSocialNameLocal(v);
              if (methods && methods.setValue) methods.setValue("issuerSocialName", v);
            }}
          />
        </div>

        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="issuer-commercial-name">Nombre Comercial (Opcional)</Label>
          <Input
            id="issuer-commercial-name"
            placeholder="Nombre comercial del emisor"
            value={issuerCommercialName}
            onChange={(e) => {
              if (methods?.setValue) {
                methods.setValue("issuerCommercialName", e.target.value);
              }
            }}
          />
        </div>

        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="issuer-address">Dirección</Label>
          <Input
            id="issuer-address"
            value={issuerAddressLocal}
            onChange={(e) => {
              const v = e.target.value;
              setIssuerAddressLocal(v);
              if (methods && methods.setValue) methods.setValue("issuerAddress", v);
            }}
          />
        </div>
      </div>

      {showAnexo && (
        <div className="bg-slate-50 border border-slate-200 rounded-lg p-4 space-y-4">
          <div className="flex items-center gap-2">
            <h4 className="font-medium text-sm text-slate-700">Código de Local Anexo</h4>
            <Tooltip>
              <TooltipTrigger asChild>
                <button type="button" className="text-slate-400 hover:text-slate-600 transition-colors">
                  <HelpCircle className="size-4" />
                </button>
              </TooltipTrigger>
              <TooltipContent className="max-w-xs">
                <p className="font-semibold mb-1">¿Qué es el Código de Anexo?</p>
                <p>Código del establecimiento anexo donde se realiza la venta.</p>
                <p className="mt-2 text-yellow-200">
                  <strong>Usar "0000" cuando:</strong>
                </p>
                <ul className="list-disc list-inside mt-1 space-y-1">
                  <li>Es el domicilio fiscal</li>
                  <li>No se puede determinar el lugar de venta</li>
                </ul>
              </TooltipContent>
            </Tooltip>
          </div>

          <div className="space-y-3">
            {/* Checkbox primero para mejor flujo */}
            <div className="flex items-start gap-3 p-3 bg-white border border-slate-200 rounded-lg hover:border-blue-300 transition-colors">
              <Checkbox 
                id="no-det" 
                checked={noDet}
                onCheckedChange={handleNoDetChange}
                className="mt-0.5"
              />
              <div className="flex-1">
                <Label htmlFor="no-det" className="font-medium cursor-pointer text-sm">
                  Es domicilio fiscal o lugar no determinado
                </Label>
                <p className="text-xs text-slate-500 mt-1">
                  Marque esta opción para usar el código "0000" automáticamente
                </p>
              </div>
            </div>

            {/* Input del código */}
            <div className="space-y-2">
              <Label htmlFor="issuer-annex" className="text-sm">
                Código de Anexo {noDet && <span className="text-blue-600">(Auto-completado)</span>}
              </Label>
              <div className="relative">
                <Input 
                  id="issuer-annex" 
                  placeholder="Ej: 0001, 0002, 0000"
                  value={anexo}
                  onChange={(e) => {
                    if (methods?.setValue) {
                      methods.setValue("issuerAnexo", e.target.value);
                      if (e.target.value !== "0000") {
                        methods.setValue("issuerNoDet", false);
                      }
                    } else {
                      setAnexoLocal(e.target.value);
                      if (e.target.value !== "0000") {
                        setNoDetLocal(false);
                      }
                    }
                  }}
                  disabled={noDet}
                  className={`font-mono ${noDet ? 'bg-blue-50 border-blue-200 text-blue-700' : ''}`}
                  maxLength={4}
                />
                {noDet && (
                  <div className="absolute right-3 top-1/2 -translate-y-1/2 flex items-center gap-1 text-xs text-blue-600 font-medium">
                    <div className="size-2 rounded-full bg-blue-600" />
                    Automático
                  </div>
                )}
              </div>
              {!noDet && (
                <p className="text-xs text-slate-500">
                  Ingrese el código de 4 dígitos del establecimiento anexo
                </p>
              )}
              {noDet && (
                <div className="flex items-start gap-2 p-2 bg-blue-50 border border-blue-200 rounded text-xs text-blue-700">
                  <div className="size-4 shrink-0 mt-0.5">ℹ️</div>
                  <p>
                    Se está usando "0000" porque esta venta corresponde al domicilio fiscal o el lugar no está determinado.
                  </p>
                </div>
              )}
            </div>

            {/* Ejemplos de códigos comunes */}
            {!noDet && (
              <div className="pt-2">
                <p className="text-xs font-medium text-slate-600 mb-2">Ejemplos comunes:</p>
                <div className="flex flex-wrap gap-2">
                  <button
                    type="button"
                    onClick={() => {
                      if (methods?.setValue) {
                        methods.setValue("issuerAnexo", "0000");
                      } else {
                        setAnexoLocal("0000");
                      }
                    }}
                    className="px-3 py-1 bg-white border border-slate-200 rounded-md text-xs hover:border-blue-300 hover:bg-blue-50 transition-colors"
                  >
                    0000 - Domicilio fiscal
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      if (methods?.setValue) {
                        methods.setValue("issuerAnexo", "0001");
                      } else {
                        setAnexoLocal("0001");
                      }
                    }}
                    className="px-3 py-1 bg-white border border-slate-200 rounded-md text-xs hover:border-blue-300 hover:bg-blue-50 transition-colors"
                  >
                    0001 - Sucursal 1
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      if (methods?.setValue) {
                        methods.setValue("issuerAnexo", "0002");
                      } else {
                        setAnexoLocal("0002");
                      }
                    }}
                    className="px-3 py-1 bg-white border border-slate-200 rounded-md text-xs hover:border-blue-300 hover:bg-blue-50 transition-colors"
                  >
                    0002 - Sucursal 2
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
