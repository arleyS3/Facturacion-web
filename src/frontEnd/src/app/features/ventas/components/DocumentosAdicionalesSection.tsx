import { Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useState } from "react";
import { useFormContext } from "react-hook-form";

/**
 * Catálogo 12 — Códigos de tipo de documento relacionado SUNAT.
 *
 * | Código | Descripción |
 * |--------|-------------|
 * | 01     | Factura |
 * | 03     | Boleta |
 * | 07     | Nota de Crédito |
 * | 08     | Nota de Débito |
 * | 09     | Guía de Remisión Remitente |
 * | 31     | Guía de Remisión Transportista |
 */
const CATALOGO_12: Record<string, string> = {
  "01": "Factura",
  "03": "Boleta",
  "07": "Nota de Crédito",
  "08": "Nota de Débito",
  "09": "Guía de Remisión Remitente",
  "31": "Guía de Remisión Transportista",
};

interface DocumentoAdicional {
  _key: string;
  id: string;
  tipoDocumento: string;
}

export function DocumentosAdicionalesSection() {
  const methods = useFormContext();

  // =================================================================
  // METHODS PATTERN — identical to DescuentosGlobalesSection.tsx
  // =================================================================
  const [documentosLocal, setDocumentosLocal] = useState<
    DocumentoAdicional[]
  >([]);

  const documentos = methods
    ? ((methods.watch("documentosAdicionales") as DocumentoAdicional[]) ?? [])
    : documentosLocal;

  const setDocumentos = (next: DocumentoAdicional[]) => {
    if (methods?.setValue) {
      methods.setValue("documentosAdicionales", next, {
        shouldDirty: true,
        shouldTouch: true,
      });
      return;
    }
    setDocumentosLocal(next);
  };

  // =================================================================
  // ADD / REMOVE / UPDATE
  // =================================================================

  const addDoc = () => {
    const newDoc: DocumentoAdicional = {
      _key: Date.now().toString(),
      id: "",
      tipoDocumento: "01",
    };
    setDocumentos([...documentos, newDoc]);
  };

  const removeDoc = (_key: string) => {
    setDocumentos(documentos.filter((d) => d._key !== _key));
  };

  const updateDoc = (_key: string, updates: Partial<DocumentoAdicional>) => {
    setDocumentos(
      documentos.map((d) => (d._key === _key ? { ...d, ...updates } : d)),
    );
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold text-lg">Otros Documentos Relacionados</h3>
        <Button onClick={addDoc} size="sm" variant="outline">
          <Plus className="size-4 mr-2" aria-hidden="true" />
          Agregar Documento
        </Button>
      </div>

      <div className="border rounded-lg overflow-hidden">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="min-w-[180px]">
                  ID Documento
                </TableHead>
                <TableHead className="min-w-[200px]">
                  Tipo Documento
                </TableHead>
                <TableHead className="w-[60px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {documentos.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={3}
                    className="text-center text-slate-500 py-8"
                  >
                    No hay documentos adicionales. Haga clic en
                    "Agregar Documento" para comenzar.
                  </TableCell>
                </TableRow>
              ) : (
                documentos.map((doc) => (
                  <TableRow key={doc._key}>
                    <TableCell>
                      <Input
                        value={doc.id}
                        onChange={(e) =>
                          updateDoc(doc._key, {
                            id: e.target.value,
                          })
                        }
                        className="h-9 font-mono"
                        placeholder="F001-00000005"
                        autoComplete="off"
                      />
                    </TableCell>
                    <TableCell>
                      <Select
                        value={doc.tipoDocumento}
                        onValueChange={(value) =>
                          updateDoc(doc._key, {
                            tipoDocumento: value,
                          })
                        }
                      >
                        <SelectTrigger className="h-9">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          {Object.entries(CATALOGO_12).map(
                            ([code, description]) => (
                              <SelectItem key={code} value={code}>
                                {code} - {description}
                              </SelectItem>
                            ),
                          )}
                        </SelectContent>
                      </Select>
                    </TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                        onClick={() => removeDoc(doc._key)}
                        aria-label="Eliminar documento adicional"
                      >
                        <Trash2 className="size-4" aria-hidden="true" />
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

export default DocumentosAdicionalesSection;
