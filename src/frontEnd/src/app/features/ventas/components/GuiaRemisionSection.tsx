import { FileText, Info, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { useState, useMemo } from "react";
import { useFormContext } from "react-hook-form";
import { useCatalog } from "@/hooks/useCatalog";
import type { GuiaRemisionFormData } from "@/lib/schemas/comprobante.schema";

/**
 * Catálogo 01 (guía de remisión) — códigos SUNAT para tipo de documento guía.
 * Se usa como fallback cuando el endpoint del catálogo no está disponible.
 */
const CATALOGO_01_GUIA: Record<string, string> = {
  "09": "Guía de Remisión Remitente",
  "31": "Guía de Remisión Transportista",
};

export function GuiaRemisionSection() {
  const methods = useFormContext();
  const { data: catalogData } = useCatalog("/catalogos/tipos-documento");

  /**
   * Opciones para el Select de tipo de documento.
   * Intenta usar el catálogo del backend filtrado para códigos de guía,
   * con fallback inline cuando la API no está disponible.
   */
  const tipoDocumentoOptions = useMemo(() => {
    if (catalogData && catalogData.length > 0) {
      const filtered = catalogData.filter(
        (item) => item.code === "09" || item.code === "31",
      );
      if (filtered.length > 0) {
        return filtered.map((item) => ({
          value: item.code,
          label: item.label,
        }));
      }
    }
    return Object.entries(CATALOGO_01_GUIA).map(([code, label]) => ({
      value: code,
      label,
    }));
  }, [catalogData]);

  // =================================================================
  // LOCAL STATE + SYNC PATTERN (single object)
  // =================================================================

  const [guiaLocal, setGuiaLocal] = useState<GuiaRemisionFormData | undefined>(
    undefined,
  );

  const guiaRemision = methods
    ? (methods.watch("guiaRemision") as GuiaRemisionFormData | undefined)
    : guiaLocal;

  const setGuiaRemision = (value: GuiaRemisionFormData | undefined) => {
    if (methods?.setValue) {
      methods.setValue("guiaRemision", value, {
        shouldDirty: true,
        shouldTouch: true,
      });
      return;
    }
    setGuiaLocal(value);
  };

  // =================================================================
  // UPDATE HELPER
  // =================================================================

  const updateGuiaRemision = (updates: Partial<GuiaRemisionFormData>) => {
    const current = guiaRemision ?? {
      serie: "",
      numero: "",
      codigoDocumento: "",
    };
    setGuiaRemision({ ...current, ...updates });
  };

  const clearGuiaRemision = () => {
    setGuiaRemision(undefined);
  };

  // =================================================================
  // FORMATTED ID HELPER
  // =================================================================

  const formattedId =
    guiaRemision?.serie && guiaRemision?.numero
      ? `${guiaRemision.serie}-${guiaRemision.numero}`
      : null;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <h3 className="font-semibold text-lg">Guía de Remisión</h3>
          <Tooltip>
            <TooltipTrigger asChild>
              <button
                type="button"
                className="text-slate-400 hover:text-slate-600 transition-colors"
                aria-label="Información sobre guía de remisión"
              >
                <Info className="size-4" aria-hidden="true" />
              </button>
            </TooltipTrigger>
            <TooltipContent className="max-w-[280px]">
              <p>
                Referencia a una guía de remisión relacionada con el
                comprobante. Se usa cuando la factura o boleta ampara el
                traslado de bienes.
              </p>
            </TooltipContent>
          </Tooltip>
        </div>
        {guiaRemision && (
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={clearGuiaRemision}
            className="h-8 text-xs text-slate-500 hover:text-red-600 hover:bg-red-50"
            aria-label="Limpiar guía de remisión"
          >
            <X className="size-3.5 mr-1" aria-hidden="true" />
            Limpiar
          </Button>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="space-y-2">
          <Label htmlFor="guia-serie">
            Serie
          </Label>
          <Input
            id="guia-serie"
            value={guiaRemision?.serie ?? ""}
            onChange={(e) => updateGuiaRemision({ serie: e.target.value })}
            className="h-9 font-mono"
            placeholder="T001"
            maxLength={4}
            autoComplete="off"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="guia-numero">
            Número
          </Label>
          <Input
            id="guia-numero"
            value={guiaRemision?.numero ?? ""}
            onChange={(e) => updateGuiaRemision({ numero: e.target.value })}
            className="h-9 font-mono"
            placeholder="00000001"
            maxLength={8}
            autoComplete="off"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="guia-tipo-documento">
            Tipo Documento
          </Label>
          <Select
            value={guiaRemision?.codigoDocumento ?? ""}
            onValueChange={(value) =>
              updateGuiaRemision({ codigoDocumento: value })
            }
          >
            <SelectTrigger id="guia-tipo-documento" className="h-9">
              <SelectValue placeholder="Seleccione tipo" />
            </SelectTrigger>
            <SelectContent>
              {tipoDocumentoOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.value} - {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {formattedId && (
        <div className="flex items-center gap-2 text-sm text-slate-500 bg-slate-50 border border-slate-200 rounded-md px-3 py-2">
          <FileText className="size-4 text-slate-400 shrink-0" aria-hidden="true" />
          <span>
            ID en XML:{" "}
            <span className="font-mono font-medium text-slate-700">
              {formattedId}
            </span>
          </span>
        </div>
      )}
    </div>
  );
}

export default GuiaRemisionSection;
