import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../ui/table";
import { Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import { useFormContext } from "react-hook-form";

interface Container {
  id: string;
  correlativo: number;
  idContenedor: string;
  nroPrecinto: string;
}

export function RouteContainers() {
  const methods = useFormContext();
  const setValue = methods?.setValue;

  const [containers, setContainers] = useState<Container[]>([]);
  const [idContenedor, setIdContenedor] = useState("");
  const [nroPrecinto, setNroPrecinto] = useState("");

  const syncFormContainers = () => {
    if (setValue && Array.isArray(containers)) {
      setValue("shippingContainers", containers);
    }
  };

  const addContainer = () => {
    if (!idContenedor) {
      alert("Ingrese el identificador del contenedor");
      return;
    }

    const newContainer: Container = {
      id: Date.now().toString(),
      correlativo: containers.length + 1,
      idContenedor: idContenedor,
      nroPrecinto: nroPrecinto
    };
    
    const updated = [...containers, newContainer];
    setContainers(updated);
    
    // Sincronizar con formulario
    setTimeout(() => syncFormContainers(), 0);
    
    // Limpiar campos
    setIdContenedor("");
    setNroPrecinto("");
  };

  const removeContainer = (id: string) => {
    const filtered = containers.filter(c => c.id !== id);
    const reindexed = filtered.map((c, index) => ({
      ...c,
      correlativo: index + 1
    }));
    setContainers(reindexed);
    
    // Sincronizar con formulario
    setTimeout(() => syncFormContainers(), 0);
  };

  return (
    <div className="space-y-4">
      {/* Formulario de entrada */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="space-y-2">
          <Label htmlFor="id-contenedor">Identificador del Contenedor</Label>
          <Input 
            id="id-contenedor" 
            placeholder="ID del contenedor"
            value={idContenedor}
            onChange={(e) => setIdContenedor(e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="nro-precinto">Número de precinto</Label>
          <Input 
            id="nro-precinto" 
            placeholder="Número de precinto"
            value={nroPrecinto}
            onChange={(e) => setNroPrecinto(e.target.value)}
          />
        </div>

        <div className="flex items-end">
          <Button onClick={addContainer} className="w-full">
            <Plus className="size-4 mr-2" />
            Agregar
          </Button>
        </div>
      </div>

      {/* Tabla de contenedores */}
      <div className="border rounded-lg overflow-hidden bg-white">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-slate-50">
                <TableHead className="w-[150px]">Correlativo</TableHead>
                <TableHead className="min-w-[250px]">IdContenedor</TableHead>
                <TableHead className="min-w-[250px]">Nro Precinto</TableHead>
                <TableHead className="w-[60px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {containers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} className="text-center text-slate-500 py-12">
                    No hay contenedores agregados. Complete los campos superiores y presione "Agregar".
                  </TableCell>
                </TableRow>
              ) : (
                containers.map((container) => (
                  <TableRow key={container.id}>
                    <TableCell className="font-medium text-slate-600">
                      {container.correlativo}
                    </TableCell>
                    <TableCell>{container.idContenedor}</TableCell>
                    <TableCell>{container.nroPrecinto}</TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                        onClick={() => removeContainer(container.id)}
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
