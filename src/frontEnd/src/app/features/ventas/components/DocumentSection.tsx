import { FileText } from "lucide-react";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useCatalog } from "@/hooks/useCatalog";
import { useFormContext } from "react-hook-form";
import { DocumentHeader } from "@/components/shared/DocumentHeader";
export function DocumentSection() {
  const methods = useFormContext();
  const {data: tipsOperations, loading: tipsOperationsLoading} = useCatalog("/catalogos/tipos-operacion");

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2 mb-6">
        <FileText className="size-5 text-blue-600" />
        <h3 className="font-semibold text-lg">Información del Documento</h3>
      </div>

      <DocumentHeader type="sales" methods={methods} />

      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h4 className="font-medium text-sm text-blue-900 mb-3">Leyendas y Operaciones</h4>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="leyendas">Leyendas</Label>
            <Input  id="leyendas" placeholder="Información adicional" />
          </div>
          <div className="space-y-2">
            <Label htmlFor="operaciones">Operaciones</Label>
            <Select  defaultValue=""
            value={methods ? methods.watch("tipoOperacion") : undefined}
            onValueChange={(v) => {
              if (methods && methods.setValue) {
                methods.setValue("tipoOperacion", v);
              }
            }}
          >
            <SelectTrigger>
              <SelectValue placeholder={tipsOperationsLoading ? "Cargando..." : "Seleccione operación"} />
            </SelectTrigger>
              <SelectContent>
                {tipsOperationsLoading ? (
                  <div className="p-3">Cargando...</div>
                ) : tipsOperations && tipsOperations.length ? (
                  tipsOperations.map((op) => (
                    <SelectItem key={op.code} value={op.code}>
                      {op.code} - {op.label}
                    </SelectItem>
                  ))
                ) : null}
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>
    </div>
  );
}
