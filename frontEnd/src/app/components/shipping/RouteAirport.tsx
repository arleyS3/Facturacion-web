import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../ui/select";
import { useState } from "react";
import { useFormContext } from "react-hook-form";
import { useCatalog } from "../../hooks/useCatalog";

export function RouteAirport() {
  const methods = useFormContext();
  const setValue = methods?.setValue;

  const [tipoPuerto, setTipoPuerto] = useState<string>("");
  const [nombrePuerto, setNombrePuerto] = useState<string>("");
  const [codigoPuerto, setCodigoPuerto] = useState<string>("");

  const { data: puertos, loading: puertosLoading } = useCatalog("/catalogos/puertos");
  const { data: aeropuertos, loading: aeropuertosLoading } = useCatalog("/catalogos/aeropuertos");

  const syncFormAirport = () => {
    if (setValue) {
      setValue("shippingAirport", {
        tipoPuerto,
        nombrePuerto,
        codigoPuerto
      });
    }
  };

  const handleTipoChange = (value: string) => {
    setTipoPuerto(value);
    setNombrePuerto("");
    setCodigoPuerto("");
    
    if (setValue) {
      setValue("shippingAirport", {
        tipoPuerto: value,
        nombrePuerto: "",
        codigoPuerto: ""
      });
    }
  };

  const handleNombrePuertoChange = (value: string) => {
    setNombrePuerto(value);
    
    const listaPuertos = tipoPuerto === "1" ? aeropuertos : puertos;
    const itemSeleccionado = listaPuertos?.find((item) => item.code === value);
    
    if (itemSeleccionado) {
      setCodigoPuerto(itemSeleccionado.code);
      
      // Sincronizar con formulario
      if (setValue) {
        setValue("shippingAirport", {
          tipoPuerto,
          nombrePuerto: value,
          codigoPuerto: itemSeleccionado.code
        });
      }
    }
  };

  const listaPuertos = tipoPuerto === "1" ? aeropuertos : puertos;
  const cargando = tipoPuerto === "1" ? aeropuertosLoading : puertosLoading;

  return (
    <div className="space-y-4">
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
        <p className="text-sm text-blue-700">
          Puede pegar la imagen desde el portapapeles
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="tipo-puerto">Tipo de Puerto o Aeropuerto</Label>
          <Select value={tipoPuerto} onValueChange={handleTipoChange}>
            <SelectTrigger id="tipo-puerto">
              <SelectValue placeholder="Seleccione el tipo" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="1">Aeropuerto</SelectItem>
              <SelectItem value="2">Puerto</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="codigo-puerto">Código de Puerto o Aeropuerto</Label>
          <Input 
            id="codigo-puerto" 
            placeholder="Se completa automáticamente al seleccionar el nombre"
            value={codigoPuerto}
            readOnly
          />
        </div>

        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="nombre-puerto">Nombre de Puerto o Aeropuerto</Label>
          <Select value={nombrePuerto} onValueChange={handleNombrePuertoChange} disabled={!tipoPuerto}>
            <SelectTrigger id="nombre-puerto">
              <SelectValue placeholder={"Seleccione " + (tipoPuerto === "1" ? "Aeropuerto" : "Puerto")} />
            </SelectTrigger>
            <SelectContent>
              {cargando ? (
                <div className="p-3">Cargando...</div>
              ) : listaPuertos && listaPuertos.length ? (
                listaPuertos.map((opt) => (
                  <SelectItem key={opt.code} value={opt.code}>
                    {opt.label}
                  </SelectItem>
                ))
              ) : (
                <SelectItem value="">Sin datos disponibles</SelectItem>
              )}
            </SelectContent>
          </Select>
        </div>
      </div>
    </div>
  );
}
