import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { AlertCircle, CalendarIcon } from "lucide-react";
import { format } from "date-fns";
import { es } from "date-fns/locale";
import { cn } from "@/components/ui/utils";

import { useCatalog } from "@/hooks/useCatalog";
import { useCatalogQuery } from "@/hooks/useCatalogQuery";
import Autocomplete from "@mui/material/Autocomplete";
import TextField from "@mui/material/TextField";
import { useEffect, useRef, useState } from "react";

interface DocumentHeaderProps {
  type: "sales" | "shipping";
  methods?: {
    watch?: (name: string) => any;
    setValue?: (name: string, value: any) => void;
    getValues?: (name: string) => any;
  };
  formState?: {
    errors?: Record<string, { message?: string }>;
  };
}

export function DocumentHeader({
  type,
  methods,
  formState,
}: DocumentHeaderProps) {
  const [useFechaActual, setUseFechaActual] = useState(true);
  const [useHoraActual, setUseHoraActual] = useState(true);
  const [selectedSerie, setSelectedSerie] = useState<string>("");
  const [series, setSeries] = useState<string[]>([]);
  const [fechaEmision, setFechaEmision] = useState(
    () => methods?.getValues?.("fechaEmision") ?? "",
  );
  const [horaEmision, setHoraEmision] = useState(
    () => methods?.getValues?.("horaEmision") ?? "",
  );
  const setValue = methods?.setValue;
  const isSales = type === "sales";
  const [codigoEmpresaLocal, setCompanyCodeLocal] = useState(
    () => methods?.getValues?.("codigoEmpresa") ?? "1",
  );
  const [shippingDocType, setShippingDocType] = useState("09");
  const watchedDocType = methods?.watch?.("tipoDocumento");
  const watchedCompanyCode = methods?.watch?.("codigoEmpresa");
  const watchedCorrelativo = methods?.watch?.("correlativo");

  const { data: tiposDocumento, loading: loadingTiposDocumento } = useCatalog(
    isSales ? "/catalogos/tipos-documento" : "",
  );
  const { data: tiposOperacion, loading: loadingTiposOperacion } = useCatalog(
    isSales ? "/catalogos/tipos-operacion" : "",
  );
  const { data: monedas, isLoading: monedasLoading } =
    useCatalogQuery("/catalogos/monedas");

  const normalizeSeriesDocType = (value?: string) => {
    if (!value) return "";
    if (value === "Nota de Débito" || value === "Nota de débito")
      return "Nota de Débito";
    if (value === "Nota de Crédito" || value === "Nota de crédito")
      return "Nota de Crédito";
    return value;
  };

  const currentDocType = isSales
    ? (watchedDocType ??
      methods?.watch?.("docType") ??
      tiposDocumento?.[0]?.label ??
      "")
    : "Guía de Remisión";

  const currentShippingDocType =
    methods?.watch?.("docType") ??
    methods?.watch?.("tipoDocumento") ??
    shippingDocType;
  const currentSeriesDocType = isSales
    ? normalizeSeriesDocType(currentDocType)
    : currentShippingDocType;

  const seriesPath = currentSeriesDocType
    ? `/catalogos/series/${encodeURIComponent(currentSeriesDocType)}`
    : "";
  const { data: seriesData, loading: seriesLoading } = useCatalog(seriesPath);

  useEffect(() => {
    if (Array.isArray(seriesData)) {
      const first = seriesData[0] as unknown;
      if (typeof first === "string") {
        setSeries(seriesData as unknown as string[]);
      } else if (
        first &&
        typeof first === "object" &&
        "code" in (first as Record<string, unknown>)
      ) {
        setSeries(
          (seriesData as Array<{ code?: string }>)
            .map((s) => s.code ?? "")
            .filter(Boolean),
        );
      } else {
        setSeries([]);
      }
    } else {
      setSeries([]);
    }
  }, [isSales, seriesData]);

  useEffect(() => {
    const currentSerie = methods?.getValues?.("serie") ?? selectedSerie;

    if (seriesLoading) {
      return;
    }

    if (series && series.length > 0) {
      if (currentSerie && series.includes(currentSerie)) {
        return;
      }

      if (setValue) {
        setValue("serie", series[0]);
      } else {
        setSelectedSerie(series[0]);
      }
      return;
    }

    if (!currentSeriesDocType) {
      if (setValue) setValue("serie", "");
      setSelectedSerie("");
    }
  }, [
    series,
    setValue,
    methods,
    selectedSerie,
    seriesLoading,
    currentSeriesDocType,
  ]);

  const [correlativo, setCorrelativo] = useState<string>(() => {
    return methods?.getValues?.("correlativo") ?? watchedCorrelativo ?? "";
  });
  const [montoPendiente, setMontoPendiente] = useState<string>(() => {
    return methods?.getValues?.("montoPendiente") ?? "";
  });
  const formatTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);

  const formatCorrelativo = (value?: string) => {
    const v = value ?? correlativo;
    const digits = v.replace(/\D/g, "");
    if (digits.length === 0) return;
    // Solo pad a 8 dígitos si tiene menos de 8, no truncar si tiene más
    const formatted = digits.padStart(8, "0");
    if (formatted !== v) {
      setCorrelativo(formatted);
      if (setValue) setValue("correlativo", formatted);
    }
  };

  useEffect(() => {
    if (formatTimeout.current) {
      clearTimeout(formatTimeout.current);
      formatTimeout.current = null;
    }
    if (!correlativo) return;
    const digits = correlativo.replace(/\D/g, "");
    // Solo formatear si tiene menos de 8 dígitos (pad), no truncar si tiene más de 8
    if (digits.length < 8 && digits !== correlativo) {
      formatTimeout.current = setTimeout(() => {
        formatCorrelativo();
        formatTimeout.current = null;
      }, 1000);
    }

    return () => {
      if (formatTimeout.current) {
        clearTimeout(formatTimeout.current);
        formatTimeout.current = null;
      }
    };
  }, [correlativo, setValue]);

  const obtenerFechaActual = () => {
    const fecha = new Date();
    return fecha.toISOString().split("T")[0];
  };

  const obtenerHoraActual = () => {
    const fecha = new Date();
    return fecha.toTimeString().split(" ")[0];
  };

  useEffect(() => {
    if (useFechaActual) {
      const fecha = obtenerFechaActual();
      setFechaEmision(fecha);
      if (setValue) setValue("fechaEmision", fecha);
    }
  }, [useFechaActual, setValue]);

  useEffect(() => {
    if (useHoraActual) {
      const intervalo = setInterval(() => {
        const hora = obtenerHoraActual();
        setHoraEmision(hora);
        if (setValue) setValue("horaEmision", hora);
      }, 1000);
      return () => clearInterval(intervalo);
    }
  }, [useHoraActual, setValue]);

  const defaultMonedas = [
    { code: "PEN", label: "Soles" },
    { code: "USD", label: "Dólares" },
    { code: "EUR", label: "Euros" },
  ];

  // Combinar monedas de API con defaults, eliminando duplicados por código
  const opcionesMoneda = (() => {
    if (!monedas || !monedas.length) return defaultMonedas;
    // Crear Map para eliminar duplicados usando el código como clave
    const mapa = new Map(defaultMonedas.map((m) => [m.code, m]));
    monedas.forEach((m) => {
      if (m.code && m.label && !mapa.has(m.code)) {
        mapa.set(m.code, m);
      }
    });
    return Array.from(mapa.values());
  })();

  const monedaValue = methods?.watch?.("moneda") || "";
  const selectedMoneda =
    opcionesMoneda.find((m) => m.code === monedaValue) || null;
  const showTipoPago = isSales && currentDocType === "Factura";
  const codigoEmpresaValue = watchedCompanyCode ?? codigoEmpresaLocal;
  const [fechaVencimiento, setFechaVencimiento] = useState<Date | undefined>(
    () => {
      const raw = methods?.getValues?.("fechaVencimiento");
      return raw ? new Date(raw + "T00:00:00") : undefined;
    },
  );
  const tipoPago = methods?.watch?.("tipoPago") ?? "contado";
  const showFechaVencimiento = showTipoPago && tipoPago === "credito";

  return (
    <div className="space-y-6">
      {/* Identificación Visual */}
      <div className="rounded-lg p-0">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="space-y-2">
            <Label className="text-xs font-medium">Código de Empresa</Label>
            <Input
              placeholder="Ej: EMP001"
              className="h-9 font-mono"
              value={codigoEmpresaValue}
              onChange={(e) => {
                const value = e.target.value;
                if (setValue) {
                  setValue("codigoEmpresa", value);
                } else {
                  setCompanyCodeLocal(value);
                }
              }}
            />
          </div>

          <div className="space-y-2">
            <Label className="text-xs font-medium">
              {isSales ? "Tipo de Documento" : "Tipo de Guía"}
            </Label>
            <Select
              value={isSales ? currentDocType : currentShippingDocType}
              onValueChange={(v) => {
                if (isSales) {
                  // Actualizar tanto el campo legacy como el nuevo
                  setValue?.("docType", v);
                  setValue?.("tipoDocumento", v);
                  return;
                }

                if (methods?.setValue) {
                  methods.setValue("docType", v);
                  methods.setValue("tipoDocumento", v);
                } else {
                  setShippingDocType(v);
                }
              }}
              defaultValue={isSales ? (tiposDocumento?.[0]?.label ?? "") : "09"}
            >
              <SelectTrigger className="h-9" id="doc-type">
                <SelectValue
                  placeholder={
                    loadingTiposDocumento
                      ? "Cargando..."
                      : "Selecione un Tipo de Documento"
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {isSales ? (
                  tiposDocumento && tiposDocumento.length ? (
                    tiposDocumento.map((opt) => (
                      <SelectItem key={opt.code} value={opt.label}>
                        {opt.label}
                      </SelectItem>
                    ))
                  ) : (
                    <>
                      <SelectItem value="Factura">Factura</SelectItem>
                      <SelectItem value="Boleta">Boleta</SelectItem>
                      <SelectItem value="Nota de Crédito">
                        Nota de Crédito
                      </SelectItem>
                      <SelectItem value="Nota de Débito">
                        Nota de Débito
                      </SelectItem>
                    </>
                  )
                ) : (
                  <>
                    <SelectItem value="09">Guía Remitente</SelectItem>
                    <SelectItem value="31">Guía Transportista</SelectItem>
                  </>
                )}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label className="text-xs font-medium">Serie</Label>
            <Select
              value={methods?.watch?.("serie") ?? ""}
              onValueChange={(v) => setValue?.("serie", v)}
              disabled={seriesLoading || !series.length}
            >
              <SelectTrigger id="serie" className="h-9">
                <SelectValue
                  placeholder={
                    seriesLoading
                      ? "Cargando..."
                      : !series.length
                        ? "Sin series disponibles"
                        : "Seleccione serie"
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {seriesLoading ? (
                  <div className="p-3">Cargando...</div>
                ) : series && series.length ? (
                  series.map((s) => (
                    <SelectItem key={s} value={s}>
                      {s}
                    </SelectItem>
                  ))
                ) : null}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label className="text-xs font-medium">Correlativo</Label>
            <Input
              placeholder="00000001"
              className={`h-9 font-mono ${formState?.errors?.correlativo ? "border-destructive focus-visible:ring-destructive/50" : ""}`}
              value={correlativo}
              onChange={(e) => {
                const val = e.target.value.replace(/\D/g, ""); // Solo dígitos
                setCorrelativo(val);
                if (setValue) setValue("correlativo", val);
              }}
              onBlur={() => formatCorrelativo()}
              aria-invalid={!!formState?.errors?.correlativo}
              aria-describedby={
                formState?.errors?.correlativo ? "correlativo-error" : undefined
              }
            />
            {formState?.errors?.correlativo && (
              <p
                id="correlativo-error"
                role="alert"
                className="text-xs text-destructive flex items-center gap-1"
              >
                <AlertCircle className="size-3" />
                {formState.errors.correlativo.message}
              </p>
            )}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <Checkbox
                id="fecha-actual"
                checked={useFechaActual}
                onCheckedChange={(checked) =>
                  setUseFechaActual(checked as boolean)
                }
              />
              <Label
                htmlFor="fecha-actual"
                className="text-xs font-medium cursor-pointer"
              >
                Usar fecha actual automáticamente
              </Label>
            </div>
            <Input
              type="date"
              value={fechaEmision}
              onChange={(e) => {
                setFechaEmision(e.target.value);
                setValue?.("fechaEmision", e.target.value);
                setUseFechaActual(false);
              }}
              disabled={useFechaActual}
              className={`h-9 ${useFechaActual ? "bg-slate-50" : ""}`}
            />
          </div>

          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <Checkbox
                id="hora-actual"
                checked={useHoraActual}
                onCheckedChange={(checked) =>
                  setUseHoraActual(checked as boolean)
                }
              />
              <Label
                htmlFor="hora-actual"
                className="text-xs font-medium cursor-pointer"
              >
                Usar hora actual automáticamente
              </Label>
            </div>
            <Input
              type="time"
              step="1"
              value={horaEmision}
              onChange={(e) => {
                setHoraEmision(e.target.value);
                setValue?.("horaEmision", e.target.value);
                setUseHoraActual(false);
              }}
              disabled={useHoraActual}
              className={`h-9 ${useHoraActual ? "bg-slate-50" : ""}`}
            />
          </div>
        </div>
      </div>

      {isSales ? (
        <div className="bg-white border border-slate-200 rounded-lg p-4">
          <h4 className="font-medium text-sm text-slate-900 mb-3">
            Información Comercial
          </h4>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="space-y-2">
              <Label htmlFor="tipo-operacion">Tipo de Operación</Label>
              <Select
                value={methods?.watch?.("tipoOperacion") ?? ""}
                onValueChange={(v) => setValue?.("tipoOperacion", v)}
              >
                <SelectTrigger id="tipo-operacion" className="h-9">
                  <SelectValue
                    placeholder={
                      loadingTiposOperacion
                        ? "Cargando..."
                        : "Seleccione tipo de operación"
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  {tiposOperacion && tiposOperacion.length ? (
                    tiposOperacion.map((opt) => (
                      <SelectItem key={opt.code} value={opt.code}>
                        {opt.label}
                      </SelectItem>
                    ))
                  ) : (
                    <div className="px-3 py-2 text-sm text-muted-foreground">
                      {loadingTiposOperacion
                        ? "Cargando..."
                        : "Sin opciones disponibles"}
                    </div>
                  )}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="moneda">Moneda</Label>
              <Autocomplete
                id="moneda"
                options={opcionesMoneda}
                getOptionLabel={(option) => `${option.code} - ${option.label}`}
                value={selectedMoneda}
                onChange={(_e, newValue) => {
                  setValue?.("moneda", newValue ? newValue.code : "");
                }}
                isOptionEqualToValue={(option, value) =>
                  option.code === value.code
                }
                loading={monedasLoading}
                disabled={monedasLoading}
                size="small"
                popupIcon={null}
                sx={{
                  "& .MuiInputBase-root": {
                    borderRadius: "0.5rem",
                    border: "1px solid #e5e7eb",
                    backgroundColor: "#fff",
                    fontSize: "1rem",
                    minHeight: "2.5rem",
                    paddingLeft: 0.5,
                    boxShadow: "none",
                    transition: "border-color 0.2s",
                  },
                  "& .MuiInputBase-root.Mui-focused": {
                    borderColor: "#2563eb",
                    boxShadow: "0 0 0 2px #bfdbfe",
                  },
                  "& .MuiOutlinedInput-notchedOutline": {
                    border: "none",
                  },
                  "& .MuiAutocomplete-endAdornment": {
                    display: "none",
                  },
                  "& .MuiInputBase-input": {
                    padding: "0.5rem 0.75rem",
                  },
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    placeholder={
                      monedasLoading ? "Cargando..." : "Seleccione moneda"
                    }
                    label=""
                    InputLabelProps={{ shrink: false }}
                    className="bg-white border border-slate-200 rounded-lg focus-within:border-blue-600 focus-within:ring-2 focus-within:ring-blue-200 text-base"
                  />
                )}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="tipo-pago">Tipo de Pago</Label>
              <Select
                value={methods?.watch?.("tipoPago") ?? "contado"}
                onValueChange={(v) => setValue?.("tipoPago", v)}
                defaultValue="contado"
                disabled={!showTipoPago}
              >
                <SelectTrigger id="tipo-pago">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="contado">Contado</SelectItem>
                  <SelectItem value="credito">Crédito</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="monto-pendiente">Monto Neto Pendiente</Label>
              <Input
                id="monto-pendiente"
                placeholder="0.00"
                className={`font-mono ${formState?.errors?.montoPendiente ? "border-destructive focus-visible:ring-destructive/50" : ""}`}
                value={montoPendiente}
                onChange={(e) => {
                  const val = e.target.value.replace(/[^0-9.]/g, ""); // Solo dígitos y punto
                  setMontoPendiente(val);
                  if (setValue) setValue("montoPendiente", val);
                }}
                aria-invalid={!!formState?.errors?.montoPendiente}
                aria-describedby={
                  formState?.errors?.montoPendiente
                    ? "monto-pendiente-error"
                    : undefined
                }
              />
              {formState?.errors?.montoPendiente && (
                <p
                  id="monto-pendiente-error"
                  role="alert"
                  className="text-xs text-destructive flex items-center gap-1"
                >
                  <AlertCircle className="size-3" />
                  {formState.errors.montoPendiente.message}
                </p>
              )}
            </div>
          </div>

          {showFechaVencimiento && (
            <div className="mt-4 border-t border-slate-100 pt-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="fecha-vencimiento">
                    Fecha de Vencimiento
                  </Label>
                  <Popover>
                    <PopoverTrigger asChild>
                      <Button
                        variant="outline"
                        data-empty={!fechaVencimiento}
                        className="w-full justify-start text-left font-normal data-[empty=true]:text-muted-foreground"
                      >
                        <CalendarIcon />
                        {fechaVencimiento ? (
                          format(fechaVencimiento, "PPP", { locale: es })
                        ) : (
                          <span>Seleccionar fecha</span>
                        )}
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0" align="start">
                      <Calendar
                        mode="single"
                        selected={fechaVencimiento}
                        onSelect={(date) => {
                          setFechaVencimiento(date);
                          setValue?.("fechaVencimiento", date ? format(date, "yyyy-MM-dd") : "");
                        }}
                        locale={es}
                        initialFocus
                      />
                    </PopoverContent>
                  </Popover>
                </div>
              </div>
            </div>
          )}
        </div>
      ) : null}
    </div>
  );
}
