import { FileJson, FileCode, FileType, CheckCircle2 } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "./ui/dialog";
import { Button } from "./ui/button";
import { Checkbox } from "./ui/checkbox";
import { Label } from "./ui/label";
import { Separator } from "./ui/separator";
import { Badge } from "./ui/badge";
import { useState } from "react";
import { useFormContext } from "react-hook-form";
import {
  countSelectedFormats,
  getSelectableEmitFormats,
  type EmitFormats,
} from "../lib/emitFormats";

interface EmitDocumentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  documentType: "sales" | "shipping";
  onEmit?: (formats: EmitFormats) => Promise<void> | void;
}

export type { EmitFormats };

/**
 * Dialogo para confirmar la emisión y selección de formatos de salida.
 *
 * @param props.open indica si el diálogo está abierto
 * @param props.onOpenChange callback cuando cambia el estado de apertura
 * @param props.documentType tipo de documento (sales | shipping)
 * @param props.onEmit función que ejecuta la emisión
 */
export function EmitDocumentDialog({ open, onOpenChange, documentType, onEmit }: EmitDocumentDialogProps) {
  const methods = useFormContext();
  const [isEmitting, setIsEmitting] = useState(false);
  const [formats, setFormats] = useState<EmitFormats>({
    xml: documentType === "shipping",
    json: false,
    txt: true,
  });
  const selectableFormats = getSelectableEmitFormats(documentType, {
    xml: true,
    json: true,
    txt: true,
  });

  const docTypeValue = (methods.watch("docType") as string) || "";
  const docTypeCodeValue = (methods.watch("docTypeCode") as string) || "";
  const serieValue = (methods.watch("serie") as string) || "";
  const correlativoValue = (methods.watch("correlativo") as string) || "";
  const fechaEmisionValue = (methods.watch("fechaEmision") as string) || "";

  const documentTypeLabel =
    docTypeValue ||
    (documentType === "shipping" && docTypeCodeValue === "09"
      ? "Guía de Remisión"
      : "");

  const resumenTipo =
    documentTypeLabel ||
    (documentType === "sales" ? "Documento de Venta" : "Guía de Remisión");

  const resumenSerie = [serieValue, correlativoValue].filter(Boolean).join("-") || "-";

  const resumenFecha =
    fechaEmisionValue ||
    new Date().toLocaleDateString("es-PE");

  const handleFormatChange = (format: keyof typeof formats, checked: boolean) => {
    setFormats(prev => ({ ...prev, [format]: checked }));
  };

  const handleEmit = async () => {
    if (isEmitting) return;

    console.log("[EmitDocumentDialog] Click Emitir");
    setIsEmitting(true);
    onOpenChange(false);

    try {
      await onEmit?.(selectedFormats);
    } finally {
      setIsEmitting(false);
    }
  };

  const selectedFormats = getSelectableEmitFormats(documentType, formats);
  const selectedCount = countSelectedFormats(selectedFormats);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-xl">
            <CheckCircle2 className="size-6 text-blue-600" />
            Confirmar Emisión de Documento
          </DialogTitle>
          <DialogDescription>
            Seleccione los formatos en los que desea generar el documento
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6 py-4">
          {/* Resumen del documento */}
          <div className="bg-slate-50 rounded-lg p-4 space-y-2">
            <h4 className="font-medium text-sm text-slate-700 mb-3">Resumen del documento</h4>
            <div className="grid grid-cols-2 gap-2 text-sm">
              <div className="text-slate-600">Tipo:</div>
              <div className="font-medium">
                {resumenTipo}
              </div>
              <div className="text-slate-600">Serie:</div>
              <div className="font-medium">{resumenSerie}</div>
              <div className="text-slate-600">Fecha:</div>
              <div className="font-medium">{resumenFecha}</div>
            </div>
          </div>

          <Separator />

          {/* Selección de formatos */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h4 className="font-medium text-sm text-slate-700">Formatos de salida</h4>
              <Badge variant="outline" className="text-xs">
                {selectedCount} {selectedCount === 1 ? 'formato' : 'formatos'} seleccionado{selectedCount === 1 ? '' : 's'}
              </Badge>
            </div>

            <div className="space-y-3">
              {/* XML - Obligatorio */}
              <div className="flex items-start space-x-3 p-3 border rounded-lg bg-blue-50 border-blue-200">
                <Checkbox
                  id="format-xml"
                  checked={formats.xml}
                  onCheckedChange={(checked) => handleFormatChange('xml', checked as boolean)}
                  disabled={!selectableFormats.xml}
                />
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <Label htmlFor="format-xml" className="font-medium cursor-pointer flex items-center gap-2">
                      <FileCode className="size-4 text-blue-600" />
                      XML
                    </Label>
                    <Badge className="text-xs bg-blue-600">SUNAT</Badge>
                  </div>
                  <p className="text-xs text-slate-600 mt-1">
                    Formato requerido para la emisión electrónica ante SUNAT
                  </p>
                </div>
              </div>


              {/* JSON */}
              <div className="flex items-start space-x-3 p-3 border rounded-lg hover:bg-slate-50 transition-colors">
                <Checkbox
                  id="format-json"
                  checked={selectableFormats.json && formats.json}
                  onCheckedChange={(checked) => handleFormatChange('json', checked as boolean)}
                  disabled={!selectableFormats.json}
                />
                <div className="flex-1">
                  <Label htmlFor="format-json" className="font-medium cursor-pointer flex items-center gap-2">
                    <FileJson className="size-4 text-yellow-600" />
                    JSON
                  </Label>
                  <p className="text-xs text-slate-600 mt-1">
                    Formato estructurado para integraciones y APIs
                  </p>
                </div>
              </div>

              {/* TXT */}
              <div className="flex items-start space-x-3 p-3 border rounded-lg hover:bg-slate-50 transition-colors">
                <Checkbox
                  id="format-txt"
                  checked={formats.txt}
                  onCheckedChange={(checked) => handleFormatChange('txt', checked as boolean)}
                />
                <div className="flex-1">
                  <Label htmlFor="format-txt" className="font-medium cursor-pointer flex items-center gap-2">
                    <FileType className="size-4 text-slate-600" />
                    TXT
                  </Label>
                  <p className="text-xs text-slate-600 mt-1">
                    Archivo de texto plano para importación
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancelar
          </Button>
          <Button 
            onClick={handleEmit}
            disabled={selectedCount === 0 || isEmitting}
            className={documentType === "sales" ? "bg-blue-600 hover:bg-blue-700" : "bg-green-600 hover:bg-green-700"}
          >
            {isEmitting ? "Emitiendo..." : "Emitir Documento"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
