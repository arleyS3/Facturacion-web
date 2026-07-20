import { Users, Loader2, Info } from "lucide-react";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";
import { Card, CardContent } from "@/components/ui/card";
import {
  FormField,
  FormItem,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import { useState, useEffect, useMemo, useRef } from "react";
import { useCatalog } from "@/hooks/useCatalog";
import { api } from "@/lib/api";
import type { CatalogItem } from "@/hooks/useCatalog";
import { useFormContext } from "react-hook-form";
import { fetchRuc } from "@/lib/sunat";

function normalizeText(value: string) {
  return value
    .toString()
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "");
}

export function ReceiverSection() {
  const [noCorresponde, setNoCorresponde] = useState(false);

  const handleNoCorrespondeChange = (checked: boolean) => {
    setNoCorresponde(checked);
  };

  const methods = useFormContext();
  const watch = methods ? methods.watch : undefined;
  const setValue = methods ? methods.setValue : undefined;
  const docType = watch ? watch("docType") : undefined;
  
  // =================================================================
  // CAMPOS NUEVOS EN ESPAÑOL (schema unificado)
  // =================================================================
  const receptorDocumento = watch ? watch("receptorDocumento") ?? "" : "";
  const receptorRazonSocial = watch ? watch("receptorRazonSocial") ?? "" : "";
  const receptorDireccion = watch ? watch("receptorDireccion") ?? "" : "";
  const receptorUbigeo = watch ? watch("receptorUbigeo") ?? "" : "";
  const receptorTipoDoc = watch ? watch("receptorTipoDoc") ?? "" : "";
  const receptorDepartamento = watch ? watch("receptorDepartamento") ?? "" : "";
  const receptorProvincia = watch ? watch("receptorProvincia") ?? "" : "";
  const receptorDistrito = watch ? watch("receptorDistrito") ?? "" : "";
  
  const { data, loading } = useCatalog("/catalogos/tipos-documento-identidad");
  const notaEndpoint =
    docType === "Nota de Crédito"
      ? "/catalogos/tipos-nota-credito"
      : docType === "Nota de Débito"
        ? "/catalogos/tipos-nota-debito"
        : null;
  const { data: tiposNota, loading: tiposNotaLoading } = useCatalog(
    notaEndpoint ?? "",
  );

  const [soloRucReceptor, setSoloRucReceptor] = useState(false);

  useEffect(() => {
    // para Receptor: si docType es "Factura" (o "01") => solo RUC (code "6")
    const isFactura = docType === "Factura" || docType === "01" || !docType;
    if (isFactura) {
      setSoloRucReceptor(true);
      if (setValue && methods) {
        if (methods.getValues("receptorTipoDoc") !== "6") {
          setValue("receptorTipoDoc", "6", { shouldValidate: true });
        }
        if (methods.getValues("tipoDocReceptor") !== "6") {
          setValue("tipoDocReceptor", "6");
        }
      }
    } else {
      setSoloRucReceptor(false);
      // Si el receptorTipoDoc está vacío al cambiar a otro tipo (Boleta, etc.), asignar "1" (DNI) o conservar
      if (setValue && methods && !methods.getValues("receptorTipoDoc")) {
        setValue("receptorTipoDoc", "1", { shouldValidate: true });
        setValue("tipoDocReceptor", "1");
      }
    }
  }, [docType, setValue, methods]);

  const opcionesReceptor = data;
  const opcionesFiltradas = soloRucReceptor
    ? opcionesReceptor?.filter((o) => o.code === "6")
    : opcionesReceptor;

  const [numeroDocumento, setNumeroDocumento] = useState("");
  const [cargandoRuc, setCargandoRuc] = useState(false);
  const [errorRuc, setErrorRuc] = useState<string | null>(null);
  const [razonSocialLocal, setRazonSocialLocal] = useState("");
  const [direccionLocal, setDireccionLocal] = useState("");

  const refIdRuc = useRef(0);
  const lastFetchedRef = useRef<string>("");

  // When SUNAT is autocompleting ubigeo selects, avoid reset effects from user-driven changes.
  const suppressUbigeoResetsRef = useRef(false);

  const pendingUbigeoRef = useRef<{
    deptName: string;
    provName: string;
    distName: string;
    ubigeo: string;
    step: "dept" | "prov" | "dist" | "done";
  } | null>(null);

  // Ubigeo states (local fallbacks when form methods absent)
  const [receiverDepartmentLocal, setReceiverDepartmentLocal] = useState("");
  const [receiverProvinceLocal, setReceiverProvinceLocal] = useState("");
  const [receiverDistrictLocal, setReceiverDistrictLocal] = useState("");

  const receiverDepartment = methods
    ? (methods.watch("receptorDepartamento") || methods.watch("receiverDepartment") as string)
    : receiverDepartmentLocal;
  const receiverProvince = methods
    ? (methods.watch("receptorProvincia") || methods.watch("receiverProvince") as string)
    : receiverProvinceLocal;
  const receiverDistrict = methods
    ? (methods.watch("receptorDistrito") || methods.watch("receiverDistrict") as string)
    : receiverDistrictLocal;

  const { data: departments, loading: depsLoading } = useCatalog(
    "/catalogos/ubigeo/departamentos",
  );

  const provincesPath = useMemo(() => {
    if (!receiverDepartment) return null;
    return `/catalogos/ubigeo/provincias?departamento=${encodeURIComponent(receiverDepartment)}`;
  }, [receiverDepartment]);
  const { data: provinces, loading: provLoading } = useCatalog(
    provincesPath ?? "",
  );

  const districtsPath = useMemo(() => {
    if (!receiverDepartment || !receiverProvince) return null;
    return `/catalogos/ubigeo/distritos?departamento=${encodeURIComponent(receiverDepartment)}&provincia=${encodeURIComponent(receiverProvince)}`;
  }, [receiverDepartment, receiverProvince]);
  const { data: districts, loading: distLoading } = useCatalog(
    districtsPath ?? "",
  );
  const [manualDistricts, setManualDistricts] = useState<CatalogItem[] | null>(
    null,
  );

  // Autocomplete pipeline: dept -> prov -> dist -> ubigeo
  useEffect(() => {
    // debug
    const p = pendingUbigeoRef.current;
    if (p)
      console.debug(
        "[ubigeo pipeline] pending",
        p.step,
        p.deptName,
        p.provName,
        p.distName,
        p.ubigeo,
      );
  }, [provinces, districts, departments]);

  // If department not resolved yet, wait for departments list and try to match
  useEffect(() => {
    if (!methods) return;
    const pending = pendingUbigeoRef.current;
    if (!pending || pending.step !== "dept") return;
    if (!departments || departments.length === 0) return;

    const target = normalizeText(pending.deptName);
    const found = departments.find(
      (d) =>
        normalizeText(d.label) === target || normalizeText(d.code) === target,
    );
    if (found?.code) {
      console.debug("[ubigeo pipeline] resolved dept ->", found.code);
      suppressUbigeoResetsRef.current = true;
      // Nuevo nombre en español
      methods.setValue("receptorDepartamento", found.code);
      // Legacy (para buildPayload)
      methods.setValue("receiverDepartment", found.code);
      setReceiverDepartmentLocal(found.code);
      pendingUbigeoRef.current = { ...pending, step: "prov" };
      queueMicrotask(() => {
        suppressUbigeoResetsRef.current = false;
      });
    }
  }, [methods, departments]);

  useEffect(() => {
    if (!methods) return;
    const pending = pendingUbigeoRef.current;
    if (!pending || pending.step !== "prov") return;
    if (!receiverDepartment) return;
    if (!provinces || provinces.length === 0) return;

    const target = normalizeText(pending.provName);
    const found = provinces.find(
      (p) =>
        normalizeText(p.label) === target || normalizeText(p.code) === target,
    );
    if (!found)
      console.debug(
        "[ubigeo pipeline] province not found for",
        pending.provName,
        "provinces size",
        provinces.length,
      );
    if (found?.code) {
      suppressUbigeoResetsRef.current = true;
      // Nuevo nombre en español
      methods.setValue("receptorProvincia", found.code);
      // Legacy (para buildPayload)
      methods.setValue("receiverProvince", found.code);
      setReceiverProvinceLocal(found.code);
      pendingUbigeoRef.current = { ...pending, step: "dist" };
      queueMicrotask(() => {
        suppressUbigeoResetsRef.current = false;
      });
    }
  }, [methods, provinces, receiverDepartment]);

  useEffect(() => {
    if (!methods) return;
    const pending = pendingUbigeoRef.current;
    if (!pending || pending.step !== "dist") return;
    if (!receiverDepartment || !receiverProvince) return;
    if (!districts || districts.length === 0) return;

    const fullUbigeo =
      pending.ubigeo && pending.ubigeo.length >= 6
        ? pending.ubigeo.slice(0, 6)
        : "";
    const targetDist = normalizeText(pending.distName);

    const found =
      (fullUbigeo ? districts.find((d) => d.code === fullUbigeo) : undefined) ||
      (targetDist
        ? districts.find((d) => normalizeText(d.label) === targetDist)
        : undefined);

    if (found?.code) {
      suppressUbigeoResetsRef.current = true;
      // Nuevo nombre en español
      methods.setValue("receptorDistrito", found.code);
      methods.setValue("receptorUbigeo", fullUbigeo || found.code);
      // Legacy (para buildPayload)
      methods.setValue("receiverDistrict", found.code);
      methods.setValue("receiverUbigeo", fullUbigeo || found.code);
      setReceiverDistrictLocal(found.code);
      pendingUbigeoRef.current = { ...pending, step: "done" };
      queueMicrotask(() => {
        suppressUbigeoResetsRef.current = false;
      });
    } else if (fullUbigeo) {
      // At least set ubigeo numeric if district list doesn't contain it
      methods.setValue("receptorUbigeo", fullUbigeo);
      methods.setValue("receiverUbigeo", fullUbigeo);
      pendingUbigeoRef.current = { ...pending, step: "done" };
    }
  }, [methods, districts, receiverDepartment, receiverProvince]);

  // If user selects a province but useCatalog didn't return districts (race or hook guard), fetch manually
  useEffect(() => {
    if (!receiverDepartment || !receiverProvince) {
      setManualDistricts(null);
      return;
    }
    if (districts && districts.length) {
      setManualDistricts(null);
      return;
    }
    if (suppressUbigeoResetsRef.current) return;

    let mounted = true;
    (async () => {
      try {
        console.debug(
          "[ubigeo] manual fetch districts for",
          receiverDepartment,
          receiverProvince,
        );
        const res = await api.get<CatalogItem[]>(
          `/catalogos/ubigeo/distritos?departamento=${encodeURIComponent(receiverDepartment)}&provincia=${encodeURIComponent(receiverProvince)}`,
        );
        if (!mounted) return;
        setManualDistricts(res.data || []);
      } catch (err) {
        console.error("manual districts fetch error", err);
      }
    })();
    return () => {
      mounted = false;
    };
  }, [receiverDepartment, receiverProvince, districts]);

  // Clear province/district ONLY when department code actually changes to a DIFFERENT value
  const prevDeptRef = useRef(receiverDepartment);
  useEffect(() => {
    if (!methods) return;
    if (suppressUbigeoResetsRef.current) return;

    // Solo resetear si el departamento cambió de un valor a otro distintito (y no es el primer render)
    if (
      prevDeptRef.current !== receiverDepartment &&
      prevDeptRef.current !== undefined
    ) {
      console.debug("[ubigeo] department changed, resetting children");
      // Nuevos nombres en español
      methods.setValue("receptorProvincia", "");
      methods.setValue("receptorDistrito", "");
      methods.setValue("receptorUbigeo", "");
      // Legacy (para buildPayload)
      methods.setValue("receiverProvince", "");
      methods.setValue("receiverDistrict", "");
      methods.setValue("receiverUbigeo", "");
      setReceiverProvinceLocal("");
      setReceiverDistrictLocal("");
    }
    prevDeptRef.current = receiverDepartment;
  }, [methods, receiverDepartment]);

  // Clear district ONLY when province code actually changes
  const prevProvRef = useRef(receiverProvince);
  useEffect(() => {
    if (!methods) return;
    if (suppressUbigeoResetsRef.current) return;

    if (
      prevProvRef.current !== receiverProvince &&
      prevProvRef.current !== undefined
    ) {
      console.debug("[ubigeo] province changed, resetting district");
      // Nuevos nombres en español
      methods.setValue("receptorDistrito", "");
      methods.setValue("receptorUbigeo", "");
      // Legacy (para buildPayload)
      methods.setValue("receiverDistrict", "");
      methods.setValue("receiverUbigeo", "");
      setReceiverDistrictLocal("");
    }
    prevProvRef.current = receiverProvince;
  }, [methods, receiverProvince]);

  useEffect(() => {
    if (methods && methods.getValues) {
      // Si receptorTipoDoc está vacío, asignar un valor por defecto ("6" para RUC o "1" para DNI)
      const currentTipoDoc = methods.getValues("receptorTipoDoc") || methods.getValues("tipoDocReceptor");
      if (!currentTipoDoc && setValue) {
        const defaultDoc = (docType === "Factura" || docType === "01" || !docType) ? "6" : "1";
        if (methods.getValues("receptorTipoDoc") !== defaultDoc) {
          setValue("receptorTipoDoc", defaultDoc, { shouldValidate: true });
          setValue("tipoDocReceptor", defaultDoc);
        }
      }
      // Nuevos nombres en español + legacy
      const s = methods.getValues("receptorRazonSocial") || methods.getValues("razonSocialReceptor") || "";
      const a = methods.getValues("receptorDireccion") || methods.getValues("direccionReceptor") || "";
      setRazonSocialLocal((prev) => (prev !== s ? s : prev));
      setDireccionLocal((prev) => (prev !== a ? a : prev));
      const d = methods.getValues("receptorDepartamento") || methods.getValues("receiverDepartment") || "";
      const p = methods.getValues("receptorProvincia") || methods.getValues("receiverProvince") || "";
      const di = methods.getValues("receptorDistrito") || methods.getValues("receiverDistrict") || "";
      setReceiverDepartmentLocal((prev) => (prev !== d ? d : prev));
      setReceiverProvinceLocal((prev) => (prev !== p ? p : prev));
      setReceiverDistrictLocal((prev) => (prev !== di ? di : prev));
    }
  }, [methods, setValue, docType]);

  useEffect(() => {
    if (!numeroDocumento || numeroDocumento.length < 8) return;
    
    // Evitar llamada si ya tenemos los datos para este número
    if (numeroDocumento === lastFetchedRef.current) return;
    
    const handler = setTimeout(async () => {
      // Verificar nuevamente antes de llamar (evita race conditions)
      if (numeroDocumento !== lastFetchedRef.current) {
        lastFetchedRef.current = numeroDocumento;
        
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
            const deptName = (info.departamento || "").toString().trim();
            const provName = (info.provincia || "").toString().trim();
            const distName = (info.distrito || "").toString().trim();
            const ubigeoCode = (info.ubigeo || "").toString().trim();

            if (methods && methods.setValue) {
              // Nuevos nombres en español
              methods.setValue("receptorRazonSocial", nombre, { shouldValidate: true });
              methods.setValue("receptorDireccion", direccion, { shouldValidate: true });
              // Legacy (para buildPayload)
              methods.setValue("razonSocialReceptor", nombre);
              methods.setValue("direccionReceptor", direccion);

              try {
                suppressUbigeoResetsRef.current = true;

                const depsRes = await api.get<CatalogItem[]>(
                  "/catalogos/ubigeo/departamentos",
                );
                const deps = depsRes.data || [];

                const normalizedDept = normalizeText(deptName);

                let depCode = "";
                if (normalizedDept) {
                  const dep = deps.find(
                    (d) =>
                      normalizeText(d.label) === normalizedDept ||
                      normalizeText(d.code) === normalizedDept,
                  );
                  depCode = dep?.code || "";
                }

                if (depCode) {
                  methods.setValue("receptorDepartamento", depCode);
                  methods.setValue("receiverDepartment", depCode);
                  setReceiverDepartmentLocal(depCode);
                }

                const fullUbigeo =
                  ubigeoCode && ubigeoCode.length >= 6
                    ? ubigeoCode.slice(0, 6)
                    : "";
                pendingUbigeoRef.current = {
                  deptName,
                  provName,
                  distName,
                  ubigeo: fullUbigeo,
                  step: depCode ? "prov" : "dept",
                };

                // Set ubigeo as early as possible
                if (fullUbigeo) {
                  methods.setValue("receptorUbigeo", fullUbigeo);
                  methods.setValue("receiverUbigeo", fullUbigeo);
                }
              } catch (err) {
                console.error("ubigeo mapping error", err);
              } finally {
                suppressUbigeoResetsRef.current = false;
              }
            } else {
              setRazonSocialLocal(nombre || "");
              if (info.departamento) {
                methods.setValue("receptorDepartamento", info.departamento);
                methods.setValue("receiverDepartment", info.departamento);
                setReceiverDepartmentLocal(info.departamento);
              }
              if (info.provincia) {
                methods.setValue("receptorProvincia", info.provincia);
                methods.setValue("receiverProvince", info.provincia);
                setReceiverProvinceLocal(info.provincia);
              }
              const distVal = info.ubigeo || info.distrito || "";
              if (distVal) {
                methods.setValue("receptorDistrito", distVal);
                methods.setValue("receiverDistrict", distVal);
                setReceiverDistrictLocal(distVal);
              }
            }
            setErrorRuc(null);
          } else {
            setErrorRuc("No se encontró información para ese número");
          }
        } catch (e) {
          if (reqId === refIdRuc.current)
            setErrorRuc("Error consultando SUNAT");
        } finally {
          if (reqId === refIdRuc.current) setCargandoRuc(false);
        }
      }
    }, 3000);

    return () => clearTimeout(handler);
  }, [numeroDocumento, methods]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        <div className="p-2 rounded-lg bg-primary/10">
          <Users className="size-5 text-primary" aria-hidden="true" />
        </div>
        <div>
          <h3 className="font-semibold text-lg text-foreground">Datos del Receptor</h3>
          <p className="text-sm text-muted-foreground">Información del cliente o empresa</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {/* Tipo de Documento */}
        <FormField
          name="receptorTipoDoc"
          render={({ field }) => (
            <FormItem className="space-y-2">
              <Label htmlFor="receiver-doc-type">
                Tipo de Documento
                <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
              </Label>
              <FormControl>
                <Select
                  value={field.value || receptorTipoDoc || (soloRucReceptor ? "6" : "1")}
                  onValueChange={(v) => {
                    field.onChange(v);
                    if (setValue) {
                      setValue("receptorTipoDoc", v, { shouldValidate: true });
                      setValue("tipoDocReceptor", v);
                    }
                  }}
                  disabled={soloRucReceptor}
                >
                  <SelectTrigger id="receiver-doc-type" className="h-10">
                    <SelectValue placeholder={loading ? "Cargando…" : "Seleccione tipo"} />
                  </SelectTrigger>
                  <SelectContent>
                    {opcionesFiltradas && opcionesFiltradas.length ? (
                      opcionesFiltradas.map((opt) => (
                        <SelectItem key={opt.code} value={opt.code}>
                          {opt.label}
                        </SelectItem>
                      ))
                    ) : (
                      <>
                        <SelectItem value="6">RUC</SelectItem>
                        <SelectItem value="1">DNI</SelectItem>
                        <SelectItem value="4">Carnet de Extranjería</SelectItem>
                        <SelectItem value="7">Pasaporte</SelectItem>
                      </>
                    )}
                  </SelectContent>
                </Select>
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Número de Documento */}
        <FormField
          name="receptorDocumento"
          render={() => (
            <FormItem className="space-y-2">
              <Label htmlFor="receiver-doc-number">
                Número de Documento
                <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
              </Label>
              <div className="relative">
                <Input
                  id="receiver-doc-number"
                  placeholder="20545990998"
                  className="font-mono h-10 pr-10"
                  autoComplete="off"
                  inputMode="numeric"
                  value={receptorDocumento || numeroDocumento}
                  onChange={(e) => {
                    const val = e.target.value.trim();
                    setNumeroDocumento(val);
                    if (methods && methods.setValue) {
                      methods.setValue("receptorDocumento", val, { shouldValidate: true });
                      methods.setValue("numeroDocReceptor", val);
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
                <p id="doc-loading" className="text-xs text-muted-foreground flex items-center gap-1" aria-live="polite">
                  <Loader2 className="size-3 animate-spin" aria-hidden="true" />
                  Consultando RUC en SUNAT…
                </p>
              )}
              {errorRuc && (
                <p id="doc-error" className="text-xs text-destructive flex items-center gap-1" role="alert">
                  <Info className="size-3" />
                  {errorRuc}
                </p>
              )}
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Razón Social */}
        <FormField
          name="receptorRazonSocial"
          render={() => (
            <FormItem className="space-y-2 md:col-span-2">
              <Label htmlFor="receiver-social-name">
                Razón Social
                <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
              </Label>
              <div className="flex items-center gap-3">
                {cargandoRuc ? (
                  <Skeleton className="h-10 flex-1 bg-muted rounded-md" />
                ) : (
                  <Input
                    id="receiver-social-name"
                    className="flex-1 h-10 uppercase"
                    value={receptorRazonSocial || razonSocialLocal}
                    onChange={(e) => {
                      const v = e.target.value;
                      setRazonSocialLocal(v);
                      if (methods && methods.setValue) {
                        methods.setValue("receptorRazonSocial", v, { shouldValidate: true });
                        methods.setValue("razonSocialReceptor", v);
                      }
                      if (v !== "-") setNoCorresponde(false);
                    }}
                    disabled={noCorresponde}
                    placeholder="Nombre o razón social del receptor"
                  />
                )}
                <div className="flex items-center gap-2 px-3 py-2 bg-muted rounded-md">
                  <Checkbox
                    id="no-corres"
                    checked={noCorresponde}
                    onCheckedChange={handleNoCorrespondeChange}
                    className="shrink-0"
                  />
                  <Label htmlFor="no-corres" className="text-sm cursor-pointer whitespace-nowrap">
                    No corresponde
                  </Label>
                </div>
              </div>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Departamento */}
        <FormField
          name="receptorDepartamento"
          render={() => (
            <FormItem className="space-y-2">
              <Label htmlFor="receiver-department">
                Departamento
                <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
              </Label>
              {cargandoRuc ? (
                <Skeleton className="h-10 w-full bg-muted rounded-md" />
              ) : (
                <Select
                  value={receiverDepartment}
                  onValueChange={(v) => {
                    pendingUbigeoRef.current = null;
                    setReceiverDepartmentLocal(v);
                    methods?.setValue?.("receptorDepartamento", v, { shouldValidate: true });
                    methods?.setValue?.("receiverDepartment", v);
                  }}
                >
                  <SelectTrigger id="receiver-department" className="h-10">
                    <SelectValue placeholder={depsLoading ? "Cargando…" : "Seleccione departamento"} />
                  </SelectTrigger>
                  <SelectContent>
                    {departments && departments.length
                      ? departments.map((opt) => (
                          <SelectItem key={opt.code} value={opt.code}>
                            {opt.label}
                          </SelectItem>
                        ))
                      : null}
                  </SelectContent>
                </Select>
              )}
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Provincia */}
        <FormField
          name="receptorProvincia"
          render={() => (
            <FormItem className="space-y-2">
              <Label htmlFor="receiver-province">
                Provincia
                <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
              </Label>
              {cargandoRuc ? (
                <Skeleton className="h-10 w-full bg-muted rounded-md" />
              ) : (
                <Select
                  value={receiverProvince}
                  onValueChange={(v) => {
                    pendingUbigeoRef.current = null;
                    setReceiverProvinceLocal(v);
                    methods?.setValue?.("receptorProvincia", v, { shouldValidate: true });
                    methods?.setValue?.("receiverProvince", v);
                    (async () => {
                      try {
                        const res = await api.get<CatalogItem[]>(
                          `/catalogos/ubigeo/distritos?departamento=${encodeURIComponent(receiverDepartment)}&provincia=${encodeURIComponent(v)}`,
                        );
                        setManualDistricts(res.data || []);
                      } catch (err) {
                        console.error("eager districts fetch error", err);
                      }
                    })();
                  }}
                  disabled={!receiverDepartment}
                >
                  <SelectTrigger id="receiver-province" className="h-10">
                    <SelectValue placeholder={provLoading ? "Cargando…" : "Seleccione provincia"} />
                  </SelectTrigger>
                  <SelectContent>
                    {provinces && provinces.length
                      ? provinces.map((opt) => (
                          <SelectItem key={opt.code} value={opt.code}>
                            {opt.label}
                          </SelectItem>
                        ))
                      : null}
                  </SelectContent>
                </Select>
              )}
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Distrito */}
        <FormField
          name="receptorDistrito"
          render={() => (
            <FormItem className="space-y-2">
              <Label htmlFor="receiver-district">
                Distrito
                <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
              </Label>
              {cargandoRuc ? (
                <Skeleton className="h-10 w-full bg-muted rounded-md" />
              ) : (
                <Select
                  value={receiverDistrict}
                  onValueChange={(v) => {
                    setReceiverDistrictLocal(v);
                    methods?.setValue?.("receptorDistrito", v, { shouldValidate: true });
                    methods?.setValue?.("receiverDistrict", v);
                    methods?.setValue?.("receiverUbigeo", v);
                  }}
                  disabled={!receiverProvince}
                >
                  <SelectTrigger id="receiver-district" className="h-10">
                    <SelectValue placeholder={distLoading ? "Cargando…" : "Seleccione distrito"} />
                  </SelectTrigger>
                  <SelectContent>
                    {(districts && districts.length
                      ? districts
                      : manualDistricts || []
                    ).map((opt) => (
                      <SelectItem key={opt.code} value={opt.code}>
                        {opt.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Ubigeo */}
        <div className="space-y-2">
          <Label htmlFor="receiver-ubigeo">Ubigeo</Label>
          {cargandoRuc ? (
            <Skeleton className="h-10 w-full bg-muted rounded-md font-mono" />
          ) : methods ? (
            <Input
              id="receiver-ubigeo"
              value={methods.watch("receiverUbigeo") || ""}
              readOnly
              className="h-10 bg-muted font-mono"
            />
          ) : (
            <Input
              id="receiver-ubigeo"
              value={receiverDistrictLocal || ""}
              readOnly
              className="h-10 bg-muted font-mono"
            />
          )}
        </div>

        {/* Dirección */}
        <FormField
          name="receptorDireccion"
          render={() => (
            <FormItem className="space-y-2 md:col-span-2">
              <Label htmlFor="receiver-address">
                Dirección
                <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
              </Label>
              {cargandoRuc ? (
                <Skeleton className="h-10 w-full bg-muted rounded-md" />
              ) : (
                <Input
                  id="receiver-address"
                  className="h-10"
                  value={receptorDireccion || direccionLocal}
                  onChange={(e) => {
                    const v = e.target.value;
                    setDireccionLocal(v);
                    if (methods && methods.setValue) {
                      methods.setValue("receptorDireccion", v);
                      methods.setValue("direccionReceptor", v);
                    }
                  }}
                  placeholder="Dirección del receptor"
                />
              )}
              <FormMessage />
            </FormItem>
          )}
        />
      </div>

      {/* Código de Autorización - Solo Factura/Boleta */}
      {(docType === "Factura" || docType === "Boleta") && (
        <Card>
          <CardContent className="p-4 space-y-4">
            <h4 className="font-medium text-sm">Código de Autorización</h4>
            <div className="flex items-center gap-4">
              <Input 
                className="flex-1 h-10" 
                placeholder="Código de autorización" 
                autoComplete="off"
              />
              <div className="flex items-center gap-2">
                <Checkbox id="mas-cod" />
                <Label htmlFor="mas-cod" className="text-sm cursor-pointer">
                  Más códigos
                </Label>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Sustento y Tipo de Nota - Solo Notas */}
      {(docType === "Nota de Crédito" || docType === "Nota de Débito") && (
        <Card>
          <CardContent className="p-4 space-y-4">
            <div className="space-y-2">
              <Label htmlFor="sustento">
                Sustento
                <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
              </Label>
              <textarea
                id="sustento"
                className="flex w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50 min-h-[80px] resize-y"
                placeholder="Información de sustento de la nota"
                value={methods.watch("documentoRelacionado.descripcionMotivo") ?? ""}
                onChange={(e) => methods.setValue("documentoRelacionado.descripcionMotivo", e.target.value)}
                aria-describedby="sustento-helper"
              />
              <p id="sustento-helper" className="text-xs text-muted-foreground">
                Describa el motivo de la nota
              </p>
            </div>
            <FormField
              name="tipoNota"
              render={() => (
                <FormItem className="space-y-2">
                  <Label htmlFor="tipo-nota">
                    Tipo de Nota
                    <span className="text-destructive ml-0.5" aria-hidden="true">*</span>
                  </Label>
                  <Select
                    value={methods.watch("tipoNota") ?? ""}
                    onValueChange={(v) => methods.setValue("tipoNota", v)}
                    disabled={tiposNotaLoading}
                  >
                    <SelectTrigger id="tipo-nota" className="h-10">
                      <SelectValue placeholder={tiposNotaLoading ? "Cargando…" : "Seleccione tipo"} />
                    </SelectTrigger>
                    <SelectContent>
                      {tiposNota && tiposNota.length
                        ? tiposNota.map((opt) => (
                            <SelectItem key={opt.code} value={opt.code}>
                              {opt.label}
                            </SelectItem>
                          ))
                        : null}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
          </CardContent>
        </Card>
      )}
    </div>
  );
}
