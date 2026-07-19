import { Link } from "lucide-react";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  FormField,
  FormItem,
  FormMessage,
} from "@/components/ui/form";
import { useFormContext } from "react-hook-form";
import { useCatalog } from "@/hooks/useCatalog";
import type { ComprobanteFormData } from "@/lib/schemas/comprobante.schema";

type DocumentoRelacionadoType = "credit" | "debit";

interface DocumentoRelacionadoSectionProps {
  type: DocumentoRelacionadoType;
}

/**
 * Sección de referencia al documento que modifica la Nota de Crédito o Débito.
 * Usa el campo documentoRelacionado del schema del comprobante.
 * Muestra el catálogo de motivos correspondiente según el tipo (09 para NC, 10 para ND).
 *
 * @param type - "credit" para Nota de Crédito (catálogo 09) o "debit" para Nota de Débito (catálogo 10)
 */
export function DocumentoRelacionadoSection({ type }: DocumentoRelacionadoSectionProps) {
  const { watch, setValue } = useFormContext<ComprobanteFormData>();

  const tipoDocumento  = watch("documentoRelacionado.tipoDocumento") ?? "01";
  const numeroDocumento = watch("documentoRelacionado.numeroDocumento") ?? "";
  const codigoMotivo   = watch("documentoRelacionado.codigoMotivo") ?? "";

  const catalogPath = type === "credit"
    ? "/catalogos/tipos-nota-credito"
    : "/catalogos/tipos-nota-debito";

  const { data: catalogo, loading: catalogoLoading } = useCatalog(catalogPath);

  const catalogoLabel = `Catálogo 0${type === "credit" ? "9" : "10"}`;
  const notaLabel = type === "credit" ? "Crédito" : "Débito";

  // Separar serie y correlativo para mostrarlos por separado
  const [serieRef, correlativoRef] = numeroDocumento.includes("-")
    ? numeroDocumento.split("-", 2)
    : [numeroDocumento, ""];

  const actualizarNumero = (serie: string, correlativo: string) => {
    const numero = correlativo ? `${serie}-${correlativo}` : serie;
    setValue("documentoRelacionado.numeroDocumento", numero, {
      shouldDirty: true,
      shouldValidate: true,
    });
  };

  return (
    <div className="bg-amber-50 border border-amber-200 rounded-lg p-5 space-y-4">
      <div className="flex items-center gap-2">
        <Link className="size-4 text-amber-600" />
        <h4 className="font-semibold text-amber-900">Documento que Modifica</h4>
        <span
          className="text-xs text-amber-600 bg-amber-100 px-2 py-0.5 rounded-full"
          aria-live="polite"
        >
          Obligatorio para Nota de {notaLabel}
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <FormField
          name="documentoRelacionado.tipoDocumento"
          render={() => (
            <FormItem className="space-y-2">
              <Label className="text-xs font-medium">Tipo de Documento Ref.</Label>
              <Select
                value={tipoDocumento}
                onValueChange={(v) =>
                  setValue("documentoRelacionado.tipoDocumento", v, {
                    shouldDirty: true,
                    shouldValidate: true,
                  })
                }
              >
                <SelectTrigger className="h-9 min-h-[44px]">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="01">01 - Factura</SelectItem>
                  <SelectItem value="03">03 - Boleta de Venta</SelectItem>
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          name="documentoRelacionado.numeroDocumento"
          render={() => (
            <FormItem className="space-y-2">
              <Label className="text-xs font-medium">Serie Ref.</Label>
              <Input
                className="h-9 font-mono uppercase"
                placeholder="F001"
                value={serieRef}
                onChange={(e) =>
                  actualizarNumero(e.target.value.toUpperCase(), correlativoRef)
                }
              />
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="space-y-2">
          <Label className="text-xs font-medium">Correlativo Ref.</Label>
          <Input
            className="h-9 font-mono"
            placeholder="00000001"
            value={correlativoRef}
            onChange={(e) => actualizarNumero(serieRef, e.target.value)}
            inputMode="numeric"
          />
        </div>
      </div>

      <FormField
        name="documentoRelacionado.codigoMotivo"
        render={() => (
          <FormItem className="space-y-2">
            <Label className="text-xs font-medium">Motivo ({catalogoLabel})</Label>
            <Select
              value={codigoMotivo}
              onValueChange={(v) =>
                setValue("documentoRelacionado.codigoMotivo", v, {
                  shouldDirty: true,
                  shouldValidate: true,
                })
              }
              disabled={catalogoLoading}
            >
              <SelectTrigger className="h-9 min-h-[44px]" aria-label="Motivo">
                <SelectValue placeholder={catalogoLoading ? "Cargando..." : "Seleccione motivo"} />
              </SelectTrigger>
              <SelectContent>
                {catalogo?.map((m) => (
                  <SelectItem key={m.code} value={m.code}>
                    {m.code} - {m.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <FormMessage />
          </FormItem>
        )}
      />

      {serieRef && correlativoRef && (
        <div className="text-xs text-amber-700 bg-amber-100 rounded px-3 py-2">
          Modificando:{" "}
          <span className="font-mono font-semibold">
            {serieRef}-{correlativoRef}
          </span>
          {codigoMotivo && catalogo && (
            <>
              {" "}
              · Motivo:{" "}
              <span className="font-semibold">
                {codigoMotivo} -{" "}
                {catalogo.find((m) => m.code === codigoMotivo)?.label}
              </span>
            </>
          )}
        </div>
      )}
    </div>
  );
}
