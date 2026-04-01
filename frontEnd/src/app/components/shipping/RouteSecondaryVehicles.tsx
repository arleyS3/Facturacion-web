import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../ui/table";
import { Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import { useFormContext } from "react-hook-form";

interface SecondaryVehicle {
  id: string;
  correlativo: number;
  placa: string;
  certificado: string;
}

export function RouteSecondaryVehicles() {
  const methods = useFormContext();
  const setValue = methods?.setValue;

  const [vehicles, setVehicles] = useState<SecondaryVehicle[]>([]);
  const [placa, setPlaca] = useState("");
  const [certificado, setCertificado] = useState("");

  const syncFormSecondaryVehicles = () => {
    if (setValue && Array.isArray(vehicles)) {
      setValue("shippingSecondaryVehicles", vehicles);
    }
  };

  const addVehicle = () => {
    if (!placa) {
      alert("Ingrese el número de placa del vehículo");
      return;
    }

    const newVehicle: SecondaryVehicle = {
      id: Date.now().toString(),
      correlativo: vehicles.length + 1,
      placa: placa,
      certificado: certificado
    };
    
    const updated = [...vehicles, newVehicle];
    setVehicles(updated);
    
    // Sincronizar con formulario
    setTimeout(() => syncFormSecondaryVehicles(), 0);
    
    // Limpiar campos
    setPlaca("");
    setCertificado("");
  };

  const removeVehicle = (id: string) => {
    const filtered = vehicles.filter(v => v.id !== id);
    const reindexed = filtered.map((v, index) => ({
      ...v,
      correlativo: index + 1
    }));
    setVehicles(reindexed);
    
    // Sincronizar con formulario
    setTimeout(() => syncFormSecondaryVehicles(), 0);
  };

  return (
    <div className="space-y-4">
      {/* Formulario de entrada */}
      <div className="grid grid-cols-1 gap-4">
        <div className="space-y-2">
          <Label htmlFor="placa-secundario">Número de placa del vehículo secundario</Label>
          <Input 
            id="placa-secundario" 
            placeholder="ABC-123"
            className="uppercase"
            value={placa}
            onChange={(e) => setPlaca(e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="certificado-vehicular">
            Tarjeta Única de Circulación Electrónica o Certificado de Habilitación vehicular
          </Label>
          <Input 
            id="certificado-vehicular" 
            placeholder="Número de certificado"
            value={certificado}
            onChange={(e) => setCertificado(e.target.value)}
          />
        </div>

        <div className="flex justify-end">
          <Button onClick={addVehicle}>
            <Plus className="size-4 mr-2" />
            Agregar
          </Button>
        </div>
      </div>

      {/* Tabla de vehículos secundarios */}
      <div className="border rounded-lg overflow-hidden bg-white">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-slate-50">
                <TableHead className="w-[150px]">Correlativo</TableHead>
                <TableHead className="w-[200px]">Placa</TableHead>
                <TableHead className="min-w-[350px]">CertisIsVehicular</TableHead>
                <TableHead className="w-[60px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {vehicles.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} className="text-center text-slate-500 py-12">
                    No hay vehículos secundarios agregados. Complete los campos superiores y presione "Agregar".
                  </TableCell>
                </TableRow>
              ) : (
                vehicles.map((vehicle) => (
                  <TableRow key={vehicle.id}>
                    <TableCell className="font-medium text-slate-600">
                      {vehicle.correlativo}
                    </TableCell>
                    <TableCell className="uppercase">{vehicle.placa}</TableCell>
                    <TableCell>{vehicle.certificado}</TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                        onClick={() => removeVehicle(vehicle.id)}
                      >
                        <Trash2 className="size-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </div>
    </div>
  );
}
