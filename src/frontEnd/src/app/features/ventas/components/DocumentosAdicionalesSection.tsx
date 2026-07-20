import { Plus, Trash2, FileCode } from "lucide-react";
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
import { Badge } from "@/components/ui/badge";
import { useState } from "react";
import { useFormContext } from "react-hook-form";

/**
 * Catálogo 12 — Códigos de tipo de documento relacionado SUNAT.
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
  id: string; // ID Unido (ej. F001-00000005)
  serie?: string;
  numero?: string;
  tipoDocumento: string;
}

const padTo8Digits = (val: string): string => {
  const digits = val.replace(/\D/g, "");
  if (!digits) return val;
  return digits.padStart(8, "0");
};

function parseId(id: string): { serie: string; numero: string } {
  if (!id) return { serie: "", numero: "" };
  if (id.includes("-")) {
    const [s, n] = id.split("-", 2);
    return { serie: s || "", numero: n || "" };
  }
  return { serie: id, numero: "" };
}

export function DocumentosAdicionalesSection() {
  const methods = useFormContext();

  const [documentosLocal, setDocumentosLocal] = useState<DocumentoAdicional[]>([]);

  const rawDocs = methods
    ? ((methods.watch("documentosAdicionales") as DocumentoAdicional[]) ?? [])
    : documentosLocal;

  // Sincronizar serie y numero desde id si no existen explícitamente
  const documentos = rawDocs.map((doc) => {
    const parsed = parseId(doc.id);
    return {
      ...doc,
      serie: doc.serie !== undefined ? doc.serie : parsed.serie,
      numero: doc.numero !== undefined ? doc.numero : parsed.numero,
    };
  });

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

  const addDoc = () => {
    const newDoc: DocumentoAdicional = {
      _key: Date.now().toString(),
      id: "",
      serie: "",
      numero: "",
      tipoDocumento: "01",
    };
    setDocumentos([...documentos, newDoc]);
  };

  const removeDoc = (_key: string) => {
    setDocumentos(documentos.filter((d) => d._key !== _key));
  };

  const updateSerie = (_key: string, newSerie: string) => {
    const uppercaseSerie = newSerie.toUpperCase().trim();
    setDocumentos(
      documentos.map((d) => {
        if (d._key !== _key) return d;
        const num = d.numero || "";
        const idUnido = uppercaseSerie && num ? `${uppercaseSerie}-${num}` : uppercaseSerie || num;
        return { ...d, serie: uppercaseSerie, id: idUnido };
      })
    );
  };

  const updateNumero = (_key: string, newNum: string) => {
    const cleanNum = newNum.replace(/\D/g, "");
    setDocumentos(
      documentos.map((d) => {
        if (d._key !== _key) return d;
        const s = d.serie || "";
        const idUnido = s && cleanNum ? `${s}-${cleanNum}` : s || cleanNum;
        return { ...d, numero: cleanNum, id: idUnido };
      })
    );
  };

  const handleNumeroBlur = (_key: string) => {
    setDocumentos(
      documentos.map((d) => {
        if (d._key !== _key) return d;
        if (!d.numero) return d;
        const paddedNum = padTo8Digits(d.numero);
        const s = d.serie || "";
        const idUnido = s && paddedNum ? `${s}-${paddedNum}` : s || paddedNum;
        return { ...d, numero: paddedNum, id: idUnido };
      })
    );
  };

  const updateTipo = (_key: string, tipo: string) => {
    setDocumentos(
      documentos.map((d) => (d._key === _key ? { ...d, tipoDocumento: tipo } : d))
    );
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="font-semibold text-lg flex items-center gap-2">
            <FileCode className="size-5 text-primary" aria-hidden="true" />
            Otros Documentos Relacionados
          </h3>
          <p className="text-xs text-muted-foreground">
            Asocie documentos adicionales por Serie y Número. Se concatenarán automáticamente como un único ID para la emisión.
          </p>
        </div>
        <Button onClick={addDoc} size="sm" variant="outline" className="cursor-pointer">
          <Plus className="size-4 mr-2" aria-hidden="true" />
          Agregar Documento
        </Button>
      </div>

      <div className="border rounded-lg overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader className="bg-muted/50">
              <TableRow>
                <TableHead className="min-w-[140px]">Serie</TableHead>
                <TableHead className="min-w-[180px]">Número (Correlativo)</TableHead>
                <TableHead className="min-w-[240px]">Tipo Documento</TableHead>
                <TableHead className="w-[60px] text-right"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {documentos.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={4}
                    className="text-center text-muted-foreground py-8 text-sm"
                  >
                    No hay documentos adicionales. Haga clic en "Agregar Documento" para comenzar.
                  </TableCell>
                </TableRow>
              ) : (
                documentos.map((doc) => {
                  return (
                    <TableRow key={doc._key} className="hover:bg-muted/50">
                      <TableCell>
                        <Input
                          value={doc.serie || ""}
                          onChange={(e) => updateSerie(doc._key, e.target.value)}
                          className="h-9 font-mono uppercase text-xs"
                          placeholder="F001…"
                          autoComplete="off"
                          spellCheck={false}
                        />
                      </TableCell>
                      <TableCell>
                        <Input
                          value={doc.numero || ""}
                          onChange={(e) => updateNumero(doc._key, e.target.value)}
                          onBlur={() => handleNumeroBlur(doc._key)}
                          className="h-9 font-mono text-xs"
                          placeholder="00000005…"
                          inputMode="numeric"
                          autoComplete="off"
                        />
                      </TableCell>
                      <TableCell>
                        <Select
                          value={doc.tipoDocumento}
                          onValueChange={(value) => updateTipo(doc._key, value)}
                        >
                          <SelectTrigger className="h-9 text-xs">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            {Object.entries(CATALOGO_12).map(([code, description]) => (
                              <SelectItem key={code} value={code} className="text-xs">
                                {code} - {description}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0 text-destructive hover:bg-destructive/10 cursor-pointer"
                          onClick={() => removeDoc(doc._key)}
                          aria-label="Eliminar documento adicional"
                        >
                          <Trash2 className="size-4" aria-hidden="true" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>
      </div>
    </div>
  );
}

export default DocumentosAdicionalesSection;
