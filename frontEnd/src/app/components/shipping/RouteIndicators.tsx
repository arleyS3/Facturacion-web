import { Label } from "../ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../ui/select";
import { Button } from "../ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../ui/table";
import { Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import { useFormContext } from "react-hook-form";

interface Indicator {
  id: string;
  correlativo: number;
  indicador: string;
}

/**
 * Componente para administrar indicadores de traslado en la guía de remisión.
 * Permite agregar/remover indicadores y sincronizarlos con el formulario.
 */
export function RouteIndicators() {
  const methods = useFormContext();
  const setValue = methods?.setValue;

  const [indicators, setIndicators] = useState<Indicator[]>([]);
  const [indicador, setIndicador] = useState("");

  const syncFormIndicators = () => {
    if (setValue && Array.isArray(indicators)) {
      setValue("shippingIndicators", indicators);
    }
  };

  const addIndicator = () => {
    if (!indicador) {
      alert("Seleccione un indicador de traslado");
      return;
    }

    const newIndicator: Indicator = {
      id: Date.now().toString(),
      correlativo: indicators.length + 1,
      indicador: indicador
    };
    
    const updated = [...indicators, newIndicator];
    setIndicators(updated);
    
    // Sincronizar con formulario
    setTimeout(() => syncFormIndicators(), 0);
    
    // Limpiar campo
    setIndicador("");
  };

  const removeIndicator = (id: string) => {
    const filtered = indicators.filter(i => i.id !== id);
    const reindexed = filtered.map((i, index) => ({
      ...i,
      correlativo: index + 1
    }));
    setIndicators(reindexed);
    
    // Sincronizar con formulario
    setTimeout(() => syncFormIndicators(), 0);
  };

  return (
    <div className="space-y-4">
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
        <p className="text-sm text-blue-700">
          Puede pegar la imagen desde el portapapeles
        </p>
      </div>

      {/* Formulario de entrada */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="indicador-traslado">Indicador de traslado</Label>
          <Select value={indicador} onValueChange={setIndicador}>
            <SelectTrigger id="indicador-traslado">
              <SelectValue placeholder="Seleccione indicador de traslado" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="SUNAT_Envio_IndicadorTransbordoProgramado">
                SUNAT_Envio_IndicadorTransbordoProgramado
              </SelectItem>
              <SelectItem value="SUNAT_Envio_IndicadorRetornoVehiculoEnvaseVacio">
                SUNAT_Envio_IndicadorRetornoVehiculoEnvaseVacio
              </SelectItem>
              <SelectItem value="SUNAT_Envio_IndicadorRetornoVehiculoVacio">
                SUNAT_Envio_IndicadorRetornoVehiculoVacio
              </SelectItem>
              <SelectItem value="SUNAT_Envio_IndicadorTrasladoVehiculoM1L">
                SUNAT_Envio_IndicadorTrasladoVehiculoM1L
              </SelectItem>
              <SelectItem value="SUNAT_Envio_IndicadorTrasladoTotalDAMoDS">
                SUNAT_Envio_IndicadorTrasladoTotalDAMoDS
              </SelectItem>
              <SelectItem value="SUNAT_Envio_IndicadorVehiculoConductoresTransp">
                SUNAT_Envio_IndicadorVehiculoConductoresTransp
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="flex items-end">
          <Button onClick={addIndicator} className="w-full">
            <Plus className="size-4 mr-2" />
            Agregar
          </Button>
        </div>
      </div>

      {/* Tabla de indicadores */}
      <div className="border rounded-lg overflow-hidden bg-white">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-slate-50">
                <TableHead className="w-[150px]">Correlativo</TableHead>
                <TableHead className="min-w-[400px]">Indicador</TableHead>
                <TableHead className="w-[60px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {indicators.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={3} className="text-center text-slate-500 py-12">
                    No hay indicadores agregados. Seleccione un indicador y presione "Agregar".
                  </TableCell>
                </TableRow>
              ) : (
                indicators.map((indicator) => (
                  <TableRow key={indicator.id}>
                    <TableCell className="font-medium text-slate-600">
                      {indicator.correlativo}
                    </TableCell>
                    <TableCell>{indicator.indicador}</TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                        onClick={() => removeIndicator(indicator.id)}
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
