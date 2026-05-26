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
import { useFormContext } from "react-hook-form";
import type { ComprobanteFormData } from "@/lib/schemas/comprobante.schema";

const MOTIVOS_CATALOGO_09 = [
  { code: "01", label: "Anulación de la operación" },
  { code: "02", label: "Anulación por error en el RUC" },
  { code: "03", label: "Corrección por error en la descripción" },
  { code: "04", label: "Descuento global" },
  { code: "05", label: "Descuento por ítem" },
  { code: "06", label: "Devolución por ítem" },
  { code: "07", label: "Bonificación" },
  { code: "08", label: "Disminución en el valor" },
  { code: "09", label: "Otros conceptos" },
];

/**
 * Sección de referencia al documento que modifica la Nota de Crédito.
 * Usa el campo documentoRelacionado del schema del comprobante.
 * Campos: tipoDocumento, numeroDocumento (serie-correlativo), codigoMotivo.
 */
export function CreditNoteReferenceSection() {
  const { watch, setValue } = useFormContext<ComprobanteFormData>();

  const tipoDocumento  = watch("documentoRelacionado.tipoDocumento") ?? "01";
  const numeroDocumento = watch("documentoRelacionado.numeroDocumento") ?? "";
  const codigoMotivo   = watch("documentoRelacionado.codigoMotivo") ?? "";

  // Separar serie y correlativo para mostrarlos por separado
  const [serieRef, correlativoRef] = numeroDocumento.includes("-")
    ? numeroDocumento.split("-", 2)
    : [numeroDocumento, ""];

  const actualizarNumero = (serie: string, correlativo: string) => {
    const numero = correlativo ? `${serie}-${correlativo}` : serie;
    setValue("documentoRelacionado.numeroDocumento", numero);
  };

  return (
    <div className="bg-amber-50 border border-amber-200 rounded-lg p-5 space-y-4">
      <div className="flex items-center gap-2">
        <Link className="size-4 text-amber-600" />
        <h4 className="font-semibold text-amber-900">Documento que Modifica</h4>
        <span className="text-xs text-amber-600 bg-amber-100 px-2 py-0.5 rounded-full">
          Obligatorio para Nota de Crédito
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="space-y-2">
          <Label className="text-xs font-medium">Tipo de Documento Ref.</Label>
          <Select
            value={tipoDocumento}
            onValueChange={(v) =>
              setValue("documentoRelacionado.tipoDocumento", v)
            }
          >
            <SelectTrigger className="h-9">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="01">01 - Factura</SelectItem>
              <SelectItem value="03">03 - Boleta de Venta</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label className="text-xs font-medium">Serie Ref.</Label>
          <Input
            className="h-9 font-mono uppercase"
            placeholder="F001"
            value={serieRef}
            onChange={(e) =>
              actualizarNumero(e.target.value.toUpperCase(), correlativoRef)
            }
          />
        </div>

        <div className="space-y-2">
          <Label className="text-xs font-medium">Correlativo Ref.</Label>
          <Input
            className="h-9 font-mono"
            placeholder="00000001"
            value={correlativoRef}
            onChange={(e) => actualizarNumero(serieRef, e.target.value)}
          />
        </div>
      </div>

      <div className="space-y-2">
        <Label className="text-xs font-medium">Motivo (Catálogo 09)</Label>
        <Select
          value={codigoMotivo}
          onValueChange={(v) =>
            setValue("documentoRelacionado.codigoMotivo", v)
          }
        >
          <SelectTrigger className="h-9">
            <SelectValue placeholder="Seleccione motivo" />
          </SelectTrigger>
          <SelectContent>
            {MOTIVOS_CATALOGO_09.map((m) => (
              <SelectItem key={m.code} value={m.code}>
                {m.code} - {m.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {serieRef && correlativoRef && (
        <div className="text-xs text-amber-700 bg-amber-100 rounded px-3 py-2">
          Modificando:{" "}
          <span className="font-mono font-semibold">
            {serieRef}-{correlativoRef}
          </span>
          {codigoMotivo && (
            <>
              {" "}
              · Motivo:{" "}
              <span className="font-semibold">
                {codigoMotivo} -{" "}
                {MOTIVOS_CATALOGO_09.find((m) => m.code === codigoMotivo)?.label}
              </span>
            </>
          )}
        </div>
      )}
    </div>
  );
}
