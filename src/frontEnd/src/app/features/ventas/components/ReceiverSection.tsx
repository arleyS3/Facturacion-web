import { Users } from "lucide-react";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";
import { useState, useEffect, useMemo, useRef } from "react";
import { useCatalog } from "@/hooks/useCatalog";
import { api } from "@/lib/api";
import type { CatalogItem } from "@/hooks/useCatalog";
import { useFormContext } from "react-hook-form";
import { fetchRuc } from "../../../lib/sunat";

function normalizeText(value: string) {
  return value
    .toString()
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "");
}

function codeEndsWith(code: string, suffix: string) {
  return code === suffix || code.endsWith(suffix);
}

function codeStartsWith(code: string, prefix: string) {
  return code === prefix || code.startsWith(prefix);
}

export function ReceiverSection() {
  const [noCorres, setNoCorres] = useState(false);
  const [razonSocial, setRazonSocial] = useState("");

  const handleNoCorresChange = (checked: boolean) => {
    setNoCorres(checked);
    if (checked) {
      setRazonSocial("-");
    } else {
      setRazonSocial("");
    }
  };

  const methods = useFormContext();
  const watch = methods ? methods.watch : undefined;
  const setValue = methods ? methods.setValue : undefined;
  const docType = watch ? watch("docType") : undefined;
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

  const [forcedOnlyRucReceiver, setForcedOnlyRucReceiver] = useState(false);

  useEffect(() => {
    // para Receptor: si docType es "Factura" => solo RUC (code "6")
    if (docType === "Factura") {
      setForcedOnlyRucReceiver(true);
      setValue && setValue("receiverDocType", "6");
    } else {
      setForcedOnlyRucReceiver(false);
    }
  }, [docType, setValue]);

  const receiverOptions = data;
  const optionsToShow = forcedOnlyRucReceiver
    ? receiverOptions?.filter((o) => o.code === "6")
    : receiverOptions;

  const [receiverNumber, setReceiverNumber] = useState("");
  const [rucLoadingReceiver, setRucLoadingReceiver] = useState(false);
  const [rucErrorReceiver, setRucErrorReceiver] = useState<string | null>(null);
  const [receiverSocialNameLocal, setReceiverSocialNameLocal] = useState("");
  const [receiverAddressLocal, setReceiverAddressLocal] = useState("");

  const receiverReqId = useRef(0);

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
    ? (methods.watch("receiverDepartment") as string)
    : receiverDepartmentLocal;
  const receiverProvince = methods
    ? (methods.watch("receiverProvince") as string)
    : receiverProvinceLocal;
  const receiverDistrict = methods
    ? (methods.watch("receiverDistrict") as string)
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
      methods.setValue("receiverDistrict", found.code);
      setReceiverDistrictLocal(found.code);
      methods.setValue("receiverUbigeo", fullUbigeo || found.code);
      pendingUbigeoRef.current = { ...pending, step: "done" };
      queueMicrotask(() => {
        suppressUbigeoResetsRef.current = false;
      });
    } else if (fullUbigeo) {
      // At least set ubigeo numeric if district list doesn't contain it
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
      methods.setValue("receiverDistrict", "");
      methods.setValue("receiverUbigeo", "");
      setReceiverDistrictLocal("");
    }
    prevProvRef.current = receiverProvince;
  }, [methods, receiverProvince]);

  useEffect(() => {
    if (methods && methods.getValues) {
      const s = methods.getValues("receiverSocialName") || "";
      const a = methods.getValues("receiverAddress") || "";
      setReceiverSocialNameLocal(s);
      setReceiverAddressLocal(a);
      const d = methods.getValues("receiverDepartment") || "";
      const p = methods.getValues("receiverProvince") || "";
      const di = methods.getValues("receiverDistrict") || "";
      setReceiverDepartmentLocal(d);
      setReceiverProvinceLocal(p);
      setReceiverDistrictLocal(di);
    }
  }, []);

  useEffect(() => {
    if (!receiverNumber) return;
    const handler = setTimeout(async () => {
      if (receiverNumber.length >= 8) {
        // mark this request id so only latest will update state
        receiverReqId.current += 1;
        const reqId = receiverReqId.current;
        setRucLoadingReceiver(true);
        setRucErrorReceiver(null);
        try {
          const info = await fetchRuc(receiverNumber);
          if (reqId !== receiverReqId.current) return; // stale
          if (info) {
            const nombre = info.nombre || "";
            const direccion = info.direccion || "";
            // update local state so UI shows values
            setReceiverSocialNameLocal(nombre);
            setReceiverAddressLocal(direccion);
            // Map SUNAT response to form values and ubigeo selects
            const deptName = (info.departamento || "").toString().trim();
            const provName = (info.provincia || "").toString().trim();
            const distName = (info.distrito || "").toString().trim();
            const ubigeoCode = (info.ubigeo || "").toString().trim();

            if (methods && methods.setValue) {
              methods.setValue("receiverSocialName", nombre);
              methods.setValue("receiverAddress", direccion);

              // Try to resolve departamento -> code and then walk the same UI flow
              try {
                suppressUbigeoResetsRef.current = true;

                const depsRes = await api.get<CatalogItem[]>(
                  "/catalogos/ubigeo/departamentos",
                );
                const deps = depsRes.data || [];

                const normalizedDept = normalizeText(deptName);
                const normalizedProv = normalizeText(provName);
                const normalizedDist = normalizeText(distName);

                let depCode = "";
                // province/district are selected later via pipeline after options load

                // Resolve departamento/provincia by text (many backends use name-like codes)
                if (normalizedDept) {
                  const dep = deps.find(
                    (d) =>
                      normalizeText(d.label) === normalizedDept ||
                      normalizeText(d.code) === normalizedDept,
                  );
                  depCode = dep?.code || "";
                }

                // Apply in order so dependent selects have valid parents
                if (depCode) {
                  methods.setValue("receiverDepartment", depCode);
                  setReceiverDepartmentLocal(depCode);
                }

                // Seed pipeline: after department is set, provinces/districts will load via hooks
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
                  methods.setValue("receiverUbigeo", fullUbigeo);
                }
              } catch (err) {
                console.error("ubigeo mapping error", err);
              } finally {
                suppressUbigeoResetsRef.current = false;
              }
            } else {
              setRazonSocial(nombre || "");
              // set local ubigeo names (but prefer numeric ubigeo code for district)
              if (info.departamento)
                setReceiverDepartmentLocal(info.departamento);
              if (info.provincia) setReceiverProvinceLocal(info.provincia);
              // Prefer numeric ubigeo code when available
              setReceiverDistrictLocal(info.ubigeo || info.distrito || "");
            }
            setRucErrorReceiver(null);
          } else {
            setRucErrorReceiver("No se encontró información para ese número");
          }
        } catch (e) {
          if (reqId === receiverReqId.current)
            setRucErrorReceiver("Error consultando SUNAT");
        } finally {
          if (reqId === receiverReqId.current) setRucLoadingReceiver(false);
        }
      }
    }, 3000); // debounce 3s

    return () => clearTimeout(handler);
  }, [receiverNumber]);

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2 mb-6">
        <Users className="size-5 text-blue-600" />
        <h3 className="font-semibold text-lg">Datos del Receptor</h3>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="receiver-doc-type">Tipo de Documento</Label>
          {methods ? (
            <Select
              value={methods.watch("receiverDocType")}
              onValueChange={(v) => setValue && setValue("receiverDocType", v)}
              defaultValue={optionsToShow?.[0]?.code}
              disabled={forcedOnlyRucReceiver}
            >
              <SelectTrigger id="receiver-doc-type">
                <SelectValue
                  placeholder={loading ? "Cargando..." : "Seleccione tipo"}
                />
              </SelectTrigger>
              <SelectContent>
                {optionsToShow && optionsToShow.length ? (
                  optionsToShow.map((opt) => (
                    <SelectItem key={opt.code} value={opt.code}>
                      {opt.label}
                    </SelectItem>
                  ))
                ) : (
                  <>
                    <SelectItem value="ruc">
                      Registro Único de Contribuyentes
                    </SelectItem>
                    <SelectItem value="dni">DNI</SelectItem>
                    <SelectItem value="passport">Pasaporte</SelectItem>
                  </>
                )}
              </SelectContent>
            </Select>
          ) : (
            <Select
              defaultValue={optionsToShow?.[0]?.code}
              disabled={forcedOnlyRucReceiver}
            >
              <SelectTrigger id="receiver-doc-type">
                <SelectValue
                  placeholder={loading ? "Cargando..." : "Seleccione tipo"}
                />
              </SelectTrigger>
              <SelectContent>
                {optionsToShow && optionsToShow.length ? (
                  optionsToShow.map((opt) => (
                    <SelectItem key={opt.code} value={opt.code}>
                      {opt.label}
                    </SelectItem>
                  ))
                ) : (
                  <>
                    <SelectItem value="ruc">
                      Registro Único de Contribuyentes
                    </SelectItem>
                    <SelectItem value="dni">DNI</SelectItem>
                    <SelectItem value="passport">Pasaporte</SelectItem>
                  </>
                )}
              </SelectContent>
            </Select>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="receiver-doc-number">N° Documento</Label>
          <Input
            id="receiver-doc-number"
            placeholder="20545990998"
            value={receiverNumber}
            onChange={(e) => {
              const val = (e.target as HTMLInputElement).value;
              setReceiverNumber(val.trim());
              if (methods && methods.setValue)
                methods.setValue("receiverDocNumber", val.trim());
            }}
          />
          {rucLoadingReceiver ? (
            <p className="text-sm text-slate-500">Consultando RUC...</p>
          ) : rucErrorReceiver ? (
            <p className="text-sm text-red-500">{rucErrorReceiver}</p>
          ) : null}
        </div>

        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="receiver-social-name">Razón Social</Label>
          <div className="flex items-center gap-4">
            <Input
              id="receiver-social-name"
              className="flex-1"
              value={receiverSocialNameLocal}
              onChange={(e) => {
                const v = e.target.value;
                setReceiverSocialNameLocal(v);
                if (methods && methods.setValue)
                  methods.setValue("receiverSocialName", v);
                if (v !== "-") setNoCorres(false);
              }}
              disabled={noCorres}
            />
            <div className="flex items-center gap-2">
              <Checkbox
                id="no-corres"
                checked={noCorres}
                onCheckedChange={handleNoCorresChange}
              />
              <Label htmlFor="no-corres" className="font-normal cursor-pointer">
                No Corres.
              </Label>
            </div>
          </div>
        </div>

        {/* Departamento -> Provincias -> Distritos flow */}
        <div className="space-y-2">
          <Label htmlFor="receiver-department">Departamento</Label>
          <Select
            value={receiverDepartment}
            onValueChange={(v) => {
              console.debug("[ubigeo] manual select department", v);
              // cancel any pending autocomplete pipeline when user interacts manually
              pendingUbigeoRef.current = null;
              setReceiverDepartmentLocal(v);
              methods?.setValue?.("receiverDepartment", v);
            }}
          >
            <SelectTrigger id="receiver-department">
              <SelectValue
                placeholder={
                  depsLoading ? "Cargando..." : "Seleccione departamento"
                }
              />
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
        </div>

        <div className="space-y-2">
          <Label htmlFor="receiver-province">Provincia</Label>
          <Select
            value={receiverProvince}
            onValueChange={(v) => {
              console.debug("[ubigeo] manual select province", v);
              // cancel any pending autocomplete pipeline when user interacts manually
              pendingUbigeoRef.current = null;
              setReceiverProvinceLocal(v);
              methods?.setValue?.("receiverProvince", v);
              // try to eagerly fetch districts for this province to populate the select
              (async () => {
                try {
                  console.debug(
                    "[ubigeo] eager fetch districts for",
                    receiverDepartment,
                    v,
                  );
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
            <SelectTrigger id="receiver-province">
              <SelectValue
                placeholder={
                  provLoading ? "Cargando..." : "Seleccione provincia"
                }
              />
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
        </div>

        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="receiver-address">Dirección</Label>
          <Input
            id="receiver-address"
            value={receiverAddressLocal}
            onChange={(e) => {
              const v = e.target.value;
              setReceiverAddressLocal(v);
              if (methods && methods.setValue)
                methods.setValue("receiverAddress", v);
            }}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="receiver-district">Distrito</Label>
          <Select
            value={receiverDistrict}
            onValueChange={(v) => {
              setReceiverDistrictLocal(v);
              methods?.setValue?.("receiverDistrict", v);
              methods?.setValue?.("receiverUbigeo", v);
            }}
            disabled={!receiverProvince}
          >
            <SelectTrigger id="receiver-district">
              <SelectValue
                placeholder={
                  distLoading ? "Cargando..." : "Seleccione distrito"
                }
              />
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
        </div>

        <div className="space-y-2">
          <Label htmlFor="receiver-ubigeo">Ubigeo</Label>
          {methods ? (
            <Input
              id="receiver-ubigeo"
              value={methods.watch("receiverUbigeo") || ""}
              readOnly
            />
          ) : (
            <Input
              id="receiver-ubigeo"
              value={receiverDistrictLocal || ""}
              readOnly
            />
          )}
        </div>
      </div>

      {
        //Reglas:
        // si doctype es "Factura o Boleta" => mostrar campo código de autorización
        (docType === "Factura" || docType === "Boleta") && (
          <div className="bg-slate-50 p-4 rounded-lg space-y-4">
            <h4 className="font-medium text-sm text-slate-700">
              Código de Autorización
            </h4>
            <div className="flex items-center gap-4">
              <Input className="flex-1" placeholder="Código de autorización" />
              <div className="flex items-center gap-2">
                <Checkbox id="mas-cod" />
                <Label htmlFor="mas-cod" className="font-normal cursor-pointer">
                  Más Cod.
                </Label>
              </div>
            </div>
          </div>
        )
      }

      {
        //Reglas:
        // si doctype es "Nota de Crédito o Nota de Débito" => mostrar campo sustento y tipo de nota de crédito
        (docType === "Nota de Crédito" || docType === "Nota de Débito") && (
          <div className="space-y-2">
            <Label htmlFor="sustento">Sustento</Label>
            <Input id="sustento" placeholder="Información de sustento" />
          </div>
        )
      }

      {(docType === "Nota de Crédito" || docType === "Nota de Débito") && (
        <div className="space-y-2">
          <Label htmlFor="tipo-nota">Tipo de Nota</Label>
          <Select
            value={methods.watch("tipoNota") ?? ""}
            onValueChange={(v) => methods.setValue("tipoNota", v)}
            disabled={tiposNotaLoading}
          >
            <SelectTrigger id="tipo-nota">
              <SelectValue
                placeholder={
                  tiposNotaLoading ? "Cargando..." : "Seleccione tipo"
                }
              />
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
        </div>
      )}
    </div>
  );
}
