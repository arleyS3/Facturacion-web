import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../ui/select";
import { useCatalog } from "../../hooks/useCatalog";
import { useFormContext } from "react-hook-form";

export function TransportDestination() {
  const methods = useFormContext();
  const setValue = methods?.setValue;
  const watch = methods?.watch;

  const { data: tiposDocIdentidad, loading: tiposDocLoading } = useCatalog(
    "/catalogos/tipos-documento-identidad",
  );

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="dest-doc-type">Tipo de Documento</Label>
          <Select
            value={watch?.("receiverDocType") ?? tiposDocIdentidad?.[0]?.code ?? "6"}
            onValueChange={(v) => setValue?.("receiverDocType", v)}
            defaultValue={tiposDocIdentidad?.[0]?.code ?? "6"}
          >
            <SelectTrigger id="dest-doc-type">
              <SelectValue placeholder={tiposDocLoading ? "Cargando..." : "Seleccione tipo"} />
            </SelectTrigger>
            <SelectContent>
              {tiposDocLoading ? (
                <div className="p-3">Cargando...</div>
              ) : tiposDocIdentidad && tiposDocIdentidad.length ? (
                tiposDocIdentidad.map((opt) => (
                  <SelectItem key={opt.code} value={opt.code}>
                    {opt.label}
                  </SelectItem>
                ))
              ) : (
                <>
                  <SelectItem value="6">6 - RUC</SelectItem>
                  <SelectItem value="1">1 - DNI</SelectItem>
                  <SelectItem value="4">4 - Carné de Extranjería</SelectItem>
                </>
              )}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="dest-doc-number">N° Documento</Label>
          <Input
            id="dest-doc-number"
            placeholder="Número de documento"
            value={watch?.("receiverDocNumber") ?? ""}
            onChange={(e) => setValue?.("receiverDocNumber", e.target.value)}
          />
        </div>

        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="dest-name">Razón Social / Nombre</Label>
          <Input
            id="dest-name"
            placeholder="Nombre del transportista o destinatario"
            value={watch?.("receiverSocialName") ?? ""}
            onChange={(e) => setValue?.("receiverSocialName", e.target.value)}
          />
        </div>
      </div>
    </div>
  );
}