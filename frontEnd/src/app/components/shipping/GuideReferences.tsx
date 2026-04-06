import { FileText, Plus, Trash2 } from "lucide-react";
import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../ui/select";
import { Button } from "../ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../ui/table";
import { useState } from "react";
import { useCatalog } from "../../hooks/useCatalog";
import { useFormContext } from "react-hook-form";

interface Reference {
  id: string;
  correlativo: number;
  tipo: string;
  serie: string;
  folio: string;
  tipoDocEmisor: string;
  rucEmisor: string;
}

/**
 * Componente para gestionar documentos de referencia asociados a la guía.
 */
export function GuideReferences() {
  const methods = useFormContext();
  const setValue = methods?.setValue;

  const { data: documentosRelacionados, loading: documentosLoading } = useCatalog(
    "/catalogos/documentos-relacionados-transporte",
  );

  const [references, setReferences] = useState<Reference[]>([]);
  const [tipo, setTipo] = useState("");
  const [serie, setSerie] = useState("");
  const [folio, setFolio] = useState("");
  const [tipoDocEmisor, setTipoDocEmisor] = useState("6");
  const [rucEmisor, setRucEmisor] = useState("");

  const syncFormReferences = (items: Reference[]) => {
    if (!setValue) return;
    setValue(
      "shippingReferences",
      items.map((r) => ({
        NroLinRef: String(r.correlativo),
        TpoDocRef: r.tipo,
        SerieRef: r.serie,
        FolioRef: r.folio,
        TipoRucEmisDocRef: r.tipoDocEmisor,
        RucEmisDocRef: r.rucEmisor,
      })),
    );
  };

  const addReference = () => {
    if (!tipo || !serie || !folio) {
      alert("Complete al menos Tipo, Serie y Folio");
      return;
    }

    const newReference: Reference = {
      id: Date.now().toString(),
      correlativo: references.length + 1,
      tipo: tipo,
      serie: serie,
      folio: folio,
      tipoDocEmisor: tipoDocEmisor,
      rucEmisor: rucEmisor
    };
    
    const updated = [...references, newReference];
    setReferences(updated);
    syncFormReferences(updated);
    
    // Limpiar campos
    setTipo("");
    setSerie("");
    setFolio("");
    setTipoDocEmisor("6");
    setRucEmisor("");
  };

  const removeReference = (id: string) => {
    const filtered = references.filter(r => r.id !== id);
    const reindexed = filtered.map((r, index) => ({
      ...r,
      correlativo: index + 1
    }));
    setReferences(reindexed);
    syncFormReferences(reindexed);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2 mb-6">
        <FileText className="size-5 text-blue-600" />
        <h3 className="font-semibold text-lg">Documentos de Referencia</h3>
      </div>

      {/* Formulario de entrada */}
      <div className="bg-slate-50 border border-slate-200 rounded-lg p-4 space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="space-y-2">
            <Label htmlFor="ref-tipo">Tipo</Label>
            <Select value={tipo} onValueChange={setTipo}>
              <SelectTrigger id="ref-tipo">
                <SelectValue placeholder={documentosLoading ? "Cargando..." : "Seleccione tipo"} />
              </SelectTrigger>
              <SelectContent>
                {documentosLoading ? (
                  <div className="p-3">Cargando...</div>
                ) : documentosRelacionados && documentosRelacionados.length ? (
                  documentosRelacionados.map((opt) => (
                    <SelectItem key={opt.code} value={opt.code}>
                      {opt.label}
                    </SelectItem>
                  ))
                ) : (
                  <>
                    <SelectItem value="01">01 - Factura</SelectItem>
                    <SelectItem value="03">03 - Boleta de Venta</SelectItem>
                    <SelectItem value="07">07 - Nota de Crédito</SelectItem>
                    <SelectItem value="08">08 - Nota de Débito</SelectItem>
                    <SelectItem value="09">09 - Guía de Remisión Remitente</SelectItem>
                  </>
                )}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="ref-serie">Serie</Label>
            <Input 
              id="ref-serie" 
              placeholder="F001"
              value={serie}
              onChange={(e) => setSerie(e.target.value)}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="ref-folio">Folio</Label>
            <Input 
              id="ref-folio" 
              placeholder="Número correlativo"
              value={folio}
              onChange={(e) => setFolio(e.target.value)}
            />
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="tipo-doc-emisor" className="text-sm">
              Tipo de documento de identidad del emisor del documento relacionado
            </Label>
            <Select value={tipoDocEmisor} onValueChange={setTipoDocEmisor}>
              <SelectTrigger id="tipo-doc-emisor">
                <SelectValue placeholder="RUC" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="6">Registro Unico de Contribuyente</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="ruc-emisor" className="text-sm">
              Número de RUC del emisor del documento relacionado
            </Label>
            <Input 
              id="ruc-emisor" 
              placeholder="20XXXXXXXXX"
              value={rucEmisor}
              onChange={(e) => setRucEmisor(e.target.value)}
            />
          </div>
        </div>

        <div className="flex justify-end">
          <Button onClick={addReference} size="sm">
            <Plus className="size-4 mr-2" />
            Agregar
          </Button>
        </div>
      </div>

      {/* Tabla de referencias */}
      <div className="border rounded-lg overflow-hidden bg-white">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-slate-50">
                <TableHead className="w-[100px]">Correlativo</TableHead>
                <TableHead className="w-[100px]">Tipo</TableHead>
                <TableHead className="w-[120px]">Serie</TableHead>
                <TableHead className="w-[150px]">Folio</TableHead>
                <TableHead className="w-[140px]">T.Doc.Emisor</TableHead>
                <TableHead className="w-[180px]">RUCEmisor</TableHead>
                <TableHead className="w-[60px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {references.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center text-slate-500 py-12">
                    No hay referencias agregadas. Complete los campos superiores y presione "Agregar".
                  </TableCell>
                </TableRow>
              ) : (
                references.map((reference) => (
                  <TableRow key={reference.id}>
                    <TableCell className="font-medium text-slate-600">
                      {reference.correlativo}
                    </TableCell>
                    <TableCell>{reference.tipo}</TableCell>
                    <TableCell>{reference.serie}</TableCell>
                    <TableCell>{reference.folio}</TableCell>
                    <TableCell>{reference.tipoDocEmisor}</TableCell>
                    <TableCell>{reference.rucEmisor}</TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                        onClick={() => removeReference(reference.id)}
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
