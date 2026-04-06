import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../ui/select";
import { Button } from "../ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../ui/table";
import { Checkbox } from "../ui/checkbox";
import { Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import { useFormContext } from "react-hook-form";
import { useCatalog } from "../../hooks/useCatalog";

interface Carrier {
  id: string;
  correlativo: number;
  modalidad: string;
  fechaInicio: string;
  tipoRuc: string;
  ruc: string;
  razonSocial: string;
  numRegMTC: string;
}

/**
 * Componente para administrar transportistas (carriers) en la guía de remisión.
 */
export function RouteCarrier() {
  const methods = useFormContext();
  const setValue = methods?.setValue;

  const [carriers, setCarriers] = useState<Carrier[]>([]);
  const [modalidad, setModalidad] = useState("");
  const [transportePublico, setTransportePublico] = useState(false);
  const [tipoDoc, setTipoDoc] = useState("6");
  const [nroDoc, setNroDoc] = useState("");
  const [razonSocial, setRazonSocial] = useState("");
  const [registroMTC, setRegistroMTC] = useState("");
  const [fechaInicio, setFechaInicio] = useState("");
  const { data: tiposDocIdentidad, loading: tiposDocLoading } = useCatalog("/catalogos/tipos-documento-identidad");

  const syncFormCarriers = () => {
    if (setValue && Array.isArray(carriers)) {
      setValue("shippingCarriers", carriers);
    }
  };

  const addCarrier = () => {
    if (!modalidad || !nroDoc) {
      alert("Complete al menos Modalidad y N° Documento");
      return;
    }

    const newCarrier: Carrier = {
      id: Date.now().toString(),
      correlativo: carriers.length + 1,
      modalidad: modalidad,
      fechaInicio: fechaInicio,
      tipoRuc: tipoDoc,
      ruc: nroDoc,
      razonSocial: razonSocial,
      numRegMTC: registroMTC
    };
    
    const updated = [...carriers, newCarrier];
    setCarriers(updated);
    
    // Sincronizar con formulario
    setTimeout(() => syncFormCarriers(), 0);
    
    // Limpiar campos
    setModalidad("");
    setTransportePublico(false);
    setTipoDoc("6");
    setNroDoc("");
    setRazonSocial("");
    setRegistroMTC("");
    setFechaInicio("");
  };

  const removeCarrier = (id: string) => {
    const filtered = carriers.filter(c => c.id !== id);
    const reindexed = filtered.map((c, index) => ({
      ...c,
      correlativo: index + 1
    }));
    setCarriers(reindexed);
    
    // Sincronizar con formulario
    setTimeout(() => syncFormCarriers(), 0);
  };

  return (
    <div className="space-y-4">
      {/* Formulario de entrada */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="modalidad">Modalidad</Label>
          <Select value={modalidad} onValueChange={setModalidad}>
            <SelectTrigger id="modalidad">
              <SelectValue placeholder="Seleccione modalidad" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="01">01 - Transporte Público</SelectItem>
              <SelectItem value="02">02 - Transporte Privado</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="fecha-inicio-traslado">Fecha de inicio de Traslado</Label>
          <Input 
            id="fecha-inicio-traslado" 
            type="date"
            value={fechaInicio}
            onChange={(e) => setFechaInicio(e.target.value)}
          />
        </div>


        <div className="space-y-2">
          <Label htmlFor="tipo-doc-transportista">Tipo Doc.</Label>
          <Select value={tipoDoc} onValueChange={setTipoDoc}>
            <SelectTrigger id="tipo-doc-transportista">
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
                  <SelectItem value="6">6 - RUC</SelectItem>
                  <SelectItem value="1">1 - DNI</SelectItem>
                  <SelectItem value="4">4 - Carné de Extranjería</SelectItem>
                </>
              )}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="nro-doc-transportista">Nro. Doc.</Label>
          <Input 
            id="nro-doc-transportista" 
            placeholder="Número de documento"
            value={nroDoc}
            onChange={(e) => setNroDoc(e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="razon-social-transportista">Razón Social</Label>
          <Input 
            id="razon-social-transportista" 
            placeholder="Razón social del transportista"
            value={razonSocial}
            onChange={(e) => setRazonSocial(e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="registro-mtc">Registro MTC</Label>
          <Input 
            id="registro-mtc" 
            placeholder="Número de registro MTC"
            value={registroMTC}
            onChange={(e) => setRegistroMTC(e.target.value)}
          />
        </div>
      </div>

      <div className="flex justify-end">
        <Button onClick={addCarrier} size="sm">
          <Plus className="size-4 mr-2" />
          Agregar
        </Button>
      </div>

      {/* Tabla de transportistas */}
      <div className="border rounded-lg overflow-hidden bg-white">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-slate-50">
                <TableHead className="w-[100px]">Correlativo</TableHead>
                <TableHead className="w-[120px]">Modalidad</TableHead>
                <TableHead className="w-[140px]">Fecha Ini.</TableHead>
                <TableHead className="w-[120px]">Tipo Ruc. Tra.</TableHead>
                <TableHead className="w-[150px]">Ruc. Trans.</TableHead>
                <TableHead className="min-w-[200px]">RazónSocial</TableHead>
                <TableHead className="w-[150px]">NumRegMTC</TableHead>
                <TableHead className="w-[60px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {carriers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className="text-center text-slate-500 py-12">
                    No hay transportistas agregados. Complete los campos superiores y presione "Agregar".
                  </TableCell>
                </TableRow>
              ) : (
                carriers.map((carrier) => (
                  <TableRow key={carrier.id}>
                    <TableCell className="font-medium text-slate-600">
                      {carrier.correlativo}
                    </TableCell>
                    <TableCell>{carrier.modalidad}</TableCell>
                    <TableCell>{carrier.fechaInicio}</TableCell>
                    <TableCell>{carrier.tipoRuc}</TableCell>
                    <TableCell>{carrier.ruc}</TableCell>
                    <TableCell>{carrier.razonSocial}</TableCell>
                    <TableCell>{carrier.numRegMTC}</TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                        onClick={() => removeCarrier(carrier.id)}
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
