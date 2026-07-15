import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useCatalog } from "@/hooks/useCatalog";
import { useFormContext } from "react-hook-form";

/**
 * Sección para ingresar datos del proveedor y comprador en la guía.
 */
export function ProviderBuyer() {
  const methods = useFormContext();
  const setValue = methods?.setValue;
  const watch = methods?.watch;

  const { data: tiposDocIdentidad, loading: tiposDocLoading } = useCatalog(
    "/catalogos/tipos-documento-identidad",
  );

  return (
    <div className="space-y-6">
      {/* Datos del Proveedor */}
      <div className="space-y-4">
        <h4 className="font-medium text-slate-700">Datos del Proveedor</h4>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="provider-doc-type">Tipo de Documento (Identidad)</Label>
            <Select
              value={watch?.("gTipoRucProveedor") ?? tiposDocIdentidad?.[0]?.code ?? "6"}
              onValueChange={(v) => setValue?.("gTipoRucProveedor", v)}
              defaultValue={tiposDocIdentidad?.[0]?.code ?? "6"}
            >
              <SelectTrigger id="provider-doc-type">
                <SelectValue placeholder={tiposDocLoading ? "Cargando..." : "Seleccione tipo"} />
              </SelectTrigger>
              <SelectContent>
                {tiposDocLoading ? (
                  <div className="p-3">Cargando...</div>
                ) : tiposDocIdentidad && tiposDocIdentidad.length ? (
                  tiposDocIdentidad.map((opt) => (
                    <SelectItem key={`provider-${opt.code}`} value={opt.code}>
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
            <Label htmlFor="provider-doc-number">N° Documento</Label>
            <Input
              id="provider-doc-number"
              placeholder="Número de documento"
              value={watch?.("gRucProveedor") ?? ""}
              onChange={(e) => setValue?.("gRucProveedor", e.target.value)}
            />
          </div>

          <div className="space-y-2 md:col-span-2">
            <Label htmlFor="provider-name">Razón Social</Label>
            <Input
              id="provider-name"
              placeholder="Nombre del proveedor"
              value={watch?.("gRazonProveedor") ?? ""}
              onChange={(e) => setValue?.("gRazonProveedor", e.target.value)}
            />
          </div>
        </div>
      </div>

      {/* Datos del Comprador */}
      <div className="space-y-4">
        <h4 className="font-medium text-slate-700">Datos del Comprador</h4>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="buyer-doc-type">Tipo de Documento (Identidad)</Label>
            <Select
              value={watch?.("gTipoRucComprador") ?? tiposDocIdentidad?.[0]?.code ?? "6"}
              onValueChange={(v) => setValue?.("gTipoRucComprador", v)}
              defaultValue={tiposDocIdentidad?.[0]?.code ?? "6"}
            >
              <SelectTrigger id="buyer-doc-type">
                <SelectValue placeholder={tiposDocLoading ? "Cargando..." : "Seleccione tipo"} />
              </SelectTrigger>
              <SelectContent>
                {tiposDocLoading ? (
                  <div className="p-3">Cargando...</div>
                ) : tiposDocIdentidad && tiposDocIdentidad.length ? (
                  tiposDocIdentidad.map((opt) => (
                    <SelectItem key={`buyer-${opt.code}`} value={opt.code}>
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
            <Label htmlFor="buyer-doc-number">N° Documento</Label>
            <Input
              id="buyer-doc-number"
              placeholder="Número de documento"
              value={watch?.("gRucComprador") ?? ""}
              onChange={(e) => setValue?.("gRucComprador", e.target.value)}
            />
          </div>

          <div className="space-y-2 md:col-span-2">
            <Label htmlFor="buyer-name">Razón Social</Label>
            <Input
              id="buyer-name"
              placeholder="Nombre del comprador"
              value={watch?.("gRazonComprador") ?? ""}
              onChange={(e) => setValue?.("gRazonComprador", e.target.value)}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
