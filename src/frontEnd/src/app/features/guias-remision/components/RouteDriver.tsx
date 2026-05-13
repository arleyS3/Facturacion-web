import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import { useFormContext } from "react-hook-form";
import { useCatalog } from "@/hooks/useCatalog";

interface Driver {
  id: string;
  correlativo: number;
  tipo: string;
  codTipoDocumento: string;
  numeroDocumento: string;
  nombres: string;
  apellidos: string;
  nroLicencia: string;
}

/**
 * Componente para administrar conductores relacionados al traslado.
 */
export function RouteDriver() {
  const methods = useFormContext();
  const setValue = methods?.setValue;

  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [tipoConductor, setTipoConductor] = useState("Principal");
  const [tipoDocumento, setTipoDocumento] = useState("1");
  const [numeroDocumento, setNumeroDocumento] = useState("");
  const [nombre, setNombre] = useState("");
  const [apellidos, setApellidos] = useState("");
  const [licencia, setLicencia] = useState("");
  const { data: tiposDocIdentidad, loading: tiposDocLoading } = useCatalog("/catalogos/tipos-documento-identidad");

  const syncFormDrivers = () => {
    if (setValue && Array.isArray(drivers)) {
      setValue("shippingDrivers", drivers);
    }
  };

  const addDriver = () => {
    if (!numeroDocumento || !nombre) {
      alert("Complete al menos Número de Documento y Nombre");
      return;
    }

    const newDriver: Driver = {
      id: Date.now().toString(),
      correlativo: drivers.length + 1,
      tipo: tipoConductor,
      codTipoDocumento: tipoDocumento,
      numeroDocumento: numeroDocumento,
      nombres: nombre,
      apellidos: apellidos,
      nroLicencia: licencia
    };
    
    const updated = [...drivers, newDriver];
    setDrivers(updated);
    
    // Sincronizar con formulario
    setTimeout(() => syncFormDrivers(), 0);
    
    // Limpiar campos
    setTipoConductor("Principal");
    setTipoDocumento("1");
    setNumeroDocumento("");
    setNombre("");
    setApellidos("");
    setLicencia("");
  };

  const removeDriver = (id: string) => {
    const filtered = drivers.filter(d => d.id !== id);
    const reindexed = filtered.map((d, index) => ({
      ...d,
      correlativo: index + 1
    }));
    setDrivers(reindexed);
    
    // Sincronizar con formulario
    setTimeout(() => syncFormDrivers(), 0);
  };

  return (
    <div className="space-y-4">
      {/* Formulario de entrada */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="tipo-conductor">Tipo de Conductor</Label>
          <Select value={tipoConductor} onValueChange={setTipoConductor}>
            <SelectTrigger id="tipo-conductor">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="Principal">Principal</SelectItem>
              <SelectItem value="Secundario">Secundario</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="numero-doc-identidad">Número documento de Identidad</Label>
          <Input 
            id="numero-doc-identidad" 
            placeholder="Número de documento"
            value={numeroDocumento}
            onChange={(e) => setNumeroDocumento(e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="tipo-documento-conductor">Tipo de Documento</Label>
          <Select value={tipoDocumento} onValueChange={setTipoDocumento}>
            <SelectTrigger id="tipo-documento-conductor">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {tiposDocLoading ? (
                <div className="p-3">Cargando...</div>
              ) : tiposDocIdentidad && tiposDocIdentidad.length ? (
                tiposDocIdentidad.map((opt) => (
                  <SelectItem key={opt.code} value={opt.code}>
                    {opt.code} - {opt.label}
                  </SelectItem>
                ))
              ) : (
                <>
                  <SelectItem value="1">1 - DNI</SelectItem>
                  <SelectItem value="4">4 - Carné de Extranjería</SelectItem>
                  <SelectItem value="7">7 - Pasaporte</SelectItem>
                  <SelectItem value="6">6 - RUC</SelectItem>
                </>
              )}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="apellidos-conductor">Apellidos</Label>
          <Input 
            id="apellidos-conductor" 
            placeholder="Apellidos del conductor"
            value={apellidos}
            onChange={(e) => setApellidos(e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="nombre-conductor">Nombre</Label>
          <Input 
            id="nombre-conductor" 
            placeholder="Nombre del conductor"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="licencia-conducir">Licencia de Conducir</Label>
          <Input 
            id="licencia-conducir" 
            placeholder="Número de licencia"
            value={licencia}
            onChange={(e) => setLicencia(e.target.value)}
          />
        </div>
      </div>

      <div className="flex justify-end">
        <Button onClick={addDriver} size="sm">
          <Plus className="size-4 mr-2" />
          Agregar
        </Button>
      </div>

      {/* Tabla de conductores */}
      <div className="border rounded-lg overflow-hidden bg-white">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-slate-50">
                <TableHead className="w-[100px]">Correlativo</TableHead>
                <TableHead className="w-[120px]">Tipo</TableHead>
                <TableHead className="w-[160px]">Cod. Tipo Documento</TableHead>
                <TableHead className="w-[180px]">Número Documento</TableHead>
                <TableHead className="min-w-[150px]">Nombres</TableHead>
                <TableHead className="min-w-[150px]">Apellidos</TableHead>
                <TableHead className="w-[140px]">Nro. Licencia</TableHead>
                <TableHead className="w-[60px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {drivers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className="text-center text-slate-500 py-12">
                    No hay conductores agregados. Complete los campos superiores y presione "Agregar".
                  </TableCell>
                </TableRow>
              ) : (
                drivers.map((driver) => (
                  <TableRow key={driver.id}>
                    <TableCell className="font-medium text-slate-600">
                      {driver.correlativo}
                    </TableCell>
                    <TableCell>{driver.tipo}</TableCell>
                    <TableCell>{driver.codTipoDocumento}</TableCell>
                    <TableCell>{driver.numeroDocumento}</TableCell>
                    <TableCell>{driver.nombres}</TableCell>
                    <TableCell>{driver.apellidos}</TableCell>
                    <TableCell>{driver.nroLicencia}</TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                        onClick={() => removeDriver(driver.id)}
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
