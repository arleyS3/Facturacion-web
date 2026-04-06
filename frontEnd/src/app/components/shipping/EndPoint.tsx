import { useState } from "react";
import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../ui/select";
import { Button } from "../ui/button";
import { useCatalog } from "../../hooks/useCatalog";
import { useFormContext } from "react-hook-form";
import { Map } from "lucide-react";

/**
 * Componente para seleccionar el punto de llegada (departamento/provincia/distrito)
 * y dirección asociada para la guía de remisión.
 */
export function EndPoint() {
  const methods = useFormContext();
  const setValue = methods?.setValue;
  const watch = methods?.watch;

  const [department, setDepartment] = useState("");
  const [province, setProvince] = useState("");
  const [district, setDistrict] = useState("");
  const [ubigeo, setUbigeo] = useState("");
  const [showLocationModal, setShowLocationModal] = useState(false);

  const { data: departments, loading: depsLoading } = useCatalog(
    "/catalogos/ubigeo/departamentos",
  );

  const provincesPath = department
    ? `/catalogos/ubigeo/provincias?departamento=${encodeURIComponent(department)}`
    : "";
  const { data: provinces, loading: provLoading } = useCatalog(provincesPath);

  const districtsPath = department && province
    ? `/catalogos/ubigeo/distritos?departamento=${encodeURIComponent(department)}&provincia=${encodeURIComponent(province)}`
    : "";
  const { data: districts, loading: distLoading } = useCatalog(districtsPath);

  const handleLocationSelect = (lat: number, lng: number, address: string) => {
    setValue?.("gDirLlegDireccion", address);
  };

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="end-department">Departamento</Label>
          <Select
            value={department}
            onValueChange={(v) => {
              setDepartment(v);
              setProvince("");
              setDistrict("");
              setUbigeo("");
            }}
          >
            <SelectTrigger id="end-department">
              <SelectValue placeholder={depsLoading ? "Cargando..." : "Seleccione departamento"} />
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
          <Label htmlFor="end-province">Provincia</Label>
          <Select
            value={province}
            onValueChange={(v) => {
              setProvince(v);
              setDistrict("");
              setUbigeo("");
            }}
            disabled={!department}
          >
            <SelectTrigger id="end-province">
              <SelectValue placeholder={provLoading ? "Cargando..." : "Seleccione provincia"} />
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

        <div className="space-y-2">
          <Label htmlFor="end-district">Distrito</Label>
          <Select
            value={district}
            onValueChange={(v) => {
              setDistrict(v);
              setUbigeo(v);
              setValue?.("gDirLlegUbiGeo", v);
            }}
            disabled={!province}
          >
            <SelectTrigger id="end-district">
              <SelectValue placeholder={distLoading ? "Cargando..." : "Seleccione distrito"} />
            </SelectTrigger>
            <SelectContent>
              {districts && districts.length
                ? districts.map((opt) => (
                    <SelectItem key={opt.code} value={opt.code}>
                      {opt.label}
                    </SelectItem>
                  ))
                : null}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="end-ubigeo">Código Ubigeo</Label>
          <Input id="end-ubigeo" placeholder="Código ubigeo" value={watch?.("gDirLlegUbiGeo") ?? ubigeo} readOnly />
        </div>

        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="end-address">Dirección</Label>
          <div className="flex gap-2">
            <Input
              id="end-address"
              placeholder="Dirección de llegada"
              value={watch?.("gDirLlegDireccion") ?? ""}
              onChange={(e) => setValue?.("gDirLlegDireccion", e.target.value)}
              className="flex-1"
            />
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="end-ruc">Número de RUC asociado al punto de llegada</Label>
          <Input
            id="end-ruc"
            placeholder="Número de RUC"
            value={watch?.("gRucPuntoLlegada") ?? ""}
            onChange={(e) => setValue?.("gRucPuntoLlegada", e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="end-establishment">Código de establecimiento de punto de llegada</Label>
          <Input
            id="end-establishment"
            placeholder="Código de establecimiento"
            value={watch?.("gCodigoLocalLlegada") ?? ""}
            onChange={(e) => setValue?.("gCodigoLocalLlegada", e.target.value)}
          />
        </div>
      </div>
    </div>
  );
}
