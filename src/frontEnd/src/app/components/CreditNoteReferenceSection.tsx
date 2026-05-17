import { Link } from "lucide-react";
import { Label } from "./ui/label";
import { Input } from "./ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { useFormContext } from "react-hook-form";

const MOTIVOS_CATALOGO_09 = [
  { code: "01", label: "01 - Anulación de la operación" },
  { code: "02", label: "02 - Anulación por error en el RUC" },
  { code: "03", label: "03 - Corrección por error en la descripción" },
  { code: "04", label: "04 - Descuento global" },
  { code: "05", label: "05 - Descuento por ítem" },
  { code: "06", label: "06 - Devolución por ítem" },
  { code: "07", label: "07 - Bonificación" },
  { code: "08", label: "08 - Disminución en el valor" },
  { code: "09", label: "09 - Otros conceptos" },
];

/**
 * Sección de referencia al documento que modifica la Nota de Crédito.
 * Captura: tipo de doc referenciado, serie, correlativo, motivo (catálogo 09).
 * Los valores se guardan en el form y buildPayload los incluye en campos.A.
 */
export function CreditNoteReferenceSection() {
  const { watch, setValue } = useFormContext();

  const tipoDocRef     = watch("refTipoDocumento") ?? "01";
  const serieRef       = watch("refSerie") ?? "";
  const correlativoRef = watch("refCorrelativo") ?? "";
  const codMotivo      = watch("refCodMotivo") ?? "";
  const descMotivo     = watch("refDescMotivo") ?? "";

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
          <Select value={tipoDocRef} onValueChange={(v) => setValue("refTipoDocumento", v)}>
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
            onChange={(e) => setValue("refSerie", e.target.value.toUpperCase())}
          />
        </div>

        <div className="space-y-2">
          <Label className="text-xs font-medium">Correlativo Ref.</Label>
          <Input
            className="h-9 font-mono"
            placeholder="00000001"
            value={correlativoRef}
            onChange={(e) => setValue("refCorrelativo", e.target.value)}
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label className="text-xs font-medium">Motivo (Catálogo 09)</Label>
          <Select value={codMotivo} onValueChange={(v) => {
            setValue("refCodMotivo", v);
            const found = MOTIVOS_CATALOGO_09.find((m) => m.code === v);
            if (found) setValue("refDescMotivo", found.label.split(" - ")[1]);
          }}>
            <SelectTrigger className="h-9">
              <SelectValue placeholder="Seleccione motivo" />
            </SelectTrigger>
            <SelectContent>
              {MOTIVOS_CATALOGO_09.map((m) => (
                <SelectItem key={m.code} value={m.code}>{m.label}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label className="text-xs font-medium">Descripción del Motivo</Label>
          <Input
            className="h-9"
            placeholder="Se completa automáticamente"
            value={descMotivo}
            onChange={(e) => setValue("refDescMotivo", e.target.value)}
          />
        </div>
      </div>

      {serieRef && correlativoRef && (
        <div className="text-xs text-amber-700 bg-amber-100 rounded px-3 py-2">
          Modificando: <span className="font-mono font-semibold">{serieRef}-{correlativoRef}</span>
          {codMotivo && <> · Motivo: <span className="font-semibold">{codMotivo}</span></>}
        </div>
      )}
    </div>
  );
}
