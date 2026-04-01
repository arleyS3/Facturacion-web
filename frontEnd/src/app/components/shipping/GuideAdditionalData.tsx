import { FileText, Plus, Trash2 } from "lucide-react";
import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../ui/select";
import { Button } from "../ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../ui/table";
import { useState } from "react";
import { useFormContext } from "react-hook-form";

interface AdditionalData {
  id: string;
  correlativo: number;
  categoria: string;
  codigo: string;
  descripcion: string;
}

export function GuideAdditionalData() {
  const methods = useFormContext();
  const setValue = methods?.setValue;

  const [additionalData, setAdditionalData] = useState<AdditionalData[]>([]);
  const [categoria, setCategoria] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [codigo, setCodigo] = useState("");

  const syncFormAdditionalData = (items: AdditionalData[]) => {
    if (!setValue) return;
    setValue(
      "shippingAdditionalData",
      items.map((d) => ({
        TipoAdicSunat: d.categoria,
        NmrLineasDetalle: String(d.correlativo),
        NmrLineasAdicSunat: d.codigo,
        DescripcionAdicsunat: d.descripcion,
      })),
    );
  };

  const addData = () => {
    if (!categoria) {
      alert("Seleccione una categoría");
      return;
    }

    const newData: AdditionalData = {
      id: Date.now().toString(),
      correlativo: additionalData.length + 1,
      categoria: categoria,
      codigo: codigo,
      descripcion: descripcion
    };
    
    const updated = [...additionalData, newData];
    setAdditionalData(updated);
    syncFormAdditionalData(updated);
    
    // Limpiar campos
    setCategoria("");
    setDescripcion("");
    setCodigo("");
  };

  const removeData = (id: string) => {
    const filtered = additionalData.filter(d => d.id !== id);
    const reindexed = filtered.map((d, index) => ({
      ...d,
      correlativo: index + 1
    }));
    setAdditionalData(reindexed);
    syncFormAdditionalData(reindexed);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2 mb-6">
        <FileText className="size-5 text-blue-600" />
        <h3 className="font-semibold text-lg">Datos Adicionales</h3>
      </div>

      {/* Formulario de entrada */}
      <div className="bg-slate-50 border border-slate-200 rounded-lg p-4 space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="space-y-2">
            <Label htmlFor="categoria">Categoría</Label>
            <Select value={categoria} onValueChange={setCategoria}>
              <SelectTrigger id="categoria">
                <SelectValue placeholder="Seleccione categoría" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="01">Detalle</SelectItem>
                <SelectItem value="02">Cabecera</SelectItem>
                <SelectItem value="03">Medio de pago</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="descripcion-datos">Descripción Datos Adicionales</Label>
            <Input 
              id="descripcion-datos" 
              placeholder="Descripción del dato adicional"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="codigo-dato">Código Dato Adicional</Label>
            <Input 
              id="codigo-dato" 
              placeholder="Código"
              value={codigo}
              onChange={(e) => setCodigo(e.target.value)}
            />
          </div>
        </div>

        <div className="flex justify-end">
          <Button onClick={addData} size="sm">
            <Plus className="size-4 mr-2" />
            Agregar
          </Button>
        </div>
      </div>

      {/* Tabla de datos adicionales */}
      <div className="border rounded-lg overflow-hidden bg-white">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-slate-50">
                <TableHead className="w-[100px]">Correlativo</TableHead>
                <TableHead className="w-[180px]">Categoría</TableHead>
                <TableHead className="w-[180px]">Código</TableHead>
                <TableHead className="min-w-[300px]">Valor Dato Adi.</TableHead>
                <TableHead className="w-[60px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {additionalData.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="text-center text-slate-500 py-12">
                    No hay datos adicionales agregados. Complete los campos superiores y presione "Agregar".
                  </TableCell>
                </TableRow>
              ) : (
                additionalData.map((data) => (
                  <TableRow key={data.id}>
                    <TableCell className="font-medium text-slate-600">
                      {data.correlativo}
                    </TableCell>
                    <TableCell className="capitalize">{data.categoria}</TableCell>
                    <TableCell>{data.codigo}</TableCell>
                    <TableCell>{data.descripcion}</TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                        onClick={() => removeData(data.id)}
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