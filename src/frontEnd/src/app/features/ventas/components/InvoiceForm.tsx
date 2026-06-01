import { X, FileText, ArrowLeft, FileCode, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ReceiverSection } from "@/features/ventas/components/ReceiverSection";
import { DocumentSection } from "@/features/ventas/components/DocumentSection";
import { ProductsTable } from "@/features/ventas/components/ProductsTable";
import { DescuentosGlobalesSection } from "@/features/ventas/components/DescuentosGlobalesSection";
import { GuiaRemisionSection } from "@/features/ventas/components/GuiaRemisionSection";
import { DocumentosAdicionalesSection } from "@/features/ventas/components/DocumentosAdicionalesSection";
import { Separator } from "@/components/ui/separator";
import { EmitDocumentDialog } from "@/components/EmitDocumentDialog";
import { useState, useEffect } from "react";
import { FormProvider, useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import buildPayload from "@/lib/buildPayload";
import { api } from "@/lib/api";
import { IssuerSection } from "@/features/ventas/components/IssuerSection";
import {
  buildFilenameFromPayload,
  getFilenameFromDisposition,
} from "@/lib/downloadFilename";
import {
  hasServerValidationErrors,
  notifyRequestError,
  showServerValidationErrors,
} from "../../../lib/emitErrorNotification";
import { documentoSchema, type DocumentoFormData } from "@/lib/schemas/documento.schema";
import { generarYDescargarXml } from "@/lib/xmlService";
import { DocumentoRelacionadoSection } from "@/features/ventas/components/DocumentoRelacionadoSection";

type ZodIssue = { path: (string | number)[]; message: string; code?: string };

const toRHFErrors = (issues: ZodIssue[]): Record<string, any> => {
  const errors: Record<string, any> = {};
  for (const issue of issues) {
    const key = (issue.path?.[0] ?? "root") as string;
    if (!errors[key]) {
      errors[key] = { type: issue.code ?? "custom", message: issue.message };
    }
  }
  return errors;
};

const documentoResolver = (values: DocumentoFormData) => {
  const result = documentoSchema.safeParse(values);
  if (result.success) return { values, errors: {} };
  return {
    values: {} as DocumentoFormData,
    errors: toRHFErrors(result.error.issues as ZodIssue[]),
  };
};

/**
 * Componente del formulario de documentos de venta. Maneja el flujo de
 * armado del payload, llamada a /tramas/generar y descarga de TXT.
 */
export function InvoiceForm() {
  const navigate = useNavigate();
  const [showEmitDialog, setShowEmitDialog] = useState(false);
  const [isGeneratingTxt, setIsGeneratingTxt] = useState(false);
  const [isGeneratingXml, setIsGeneratingXml] = useState(false);
  const goHome = () => {
    navigate("/");
  };
  const methods = useForm({
    resolver: documentoResolver,
    mode: "onChange",
    reValidateMode: "onChange",
    defaultValues: {
      // =================================================================
      // DATOS DEL DOCUMENTO (nuevos nombres en español)
      // =================================================================
      tipoDocumento: "",
      serie: "F001",
      correlativo: "",
      fechaEmision: "",
      horaEmision: "",
      fechaVencimiento: "",
      tipoOperacion: "",
      moneda: "PEN",
      
      // =================================================================
      // DATOS DEL EMISOR (nuevos nombres en español)
      // =================================================================
      emisorRuc: "",
      emisorRazonSocial: "",
      emisorNombreComercial: "",
      emisorDireccion: "",
      emisorUrbanizacion: "",
      emisorDepartamento: "",
      emisorProvincia: "",
      emisorDistrito: "",
      emisorUbigeo: "",
      emisorCodigoDomicilio: "0001",
      
      // =================================================================
      // DATOS DEL RECEPTOR (nuevos nombres en español)
      // =================================================================
      receptorDocumento: "",
      receptorRazonSocial: "",
      receptorDireccion: "",
      receptorUbigeo: "",
      
      // =================================================================
      // PRODUCTOS / DETALLES
      // =================================================================
      detalles: [],
      
      // =================================================================
      // DESCUENTOS GLOBALES
      // =================================================================
      descuentosGlobales: [],
      
      // =================================================================
      // DOCUMENTOS RELACIONADOS (opcional)
      // =================================================================
      guiaRemision: undefined,
      documentosAdicionales: [],
      
      // =================================================================
      // LEYENDAS
      // =================================================================
      leyendas: [],
      
      // =================================================================
      // DOCUMENTO RELACIONADO (para notas)
      // =================================================================
      documentoRelacionado: undefined,
      
      // =================================================================
      // CAMPOS ADICIONALES
      // =================================================================
      montoPendiente: "",
      
      // =================================================================
      // CAMPOS LEGACY (para compatibilidad con buildPayload/TXT)
      // =================================================================
      // Estos campos mantienen los nombres old para que buildPayload funcione
      docType: "",
      docTypeCode: "",
      issuerDocType: "",
      issuerDocNumber: "",
      issuerCommercialName: "",
      issuerSocialName: "",
      issuerAddress: "",
      issuerAnexo: "",
      issuerNoDet: false,
      receiverDocType: "",
      receiverDocNumber: "",
      receiverDepartment: "",
      receiverProvince: "",
      receiverDistrict: "",
      products: [], // alias para detalles
    },
  });

  const docValue = methods.watch("tipoDocumento") || methods.watch("docType") || "";
  const isNota = ["07", "08", "Nota de Crédito", "Nota de Débito"].includes(docValue);
  const isCredit = ["07", "Nota de Crédito"].includes(docValue);

  const descargarTxt = async (payload: any) => {
    const preferredName = buildFilenameFromPayload(payload, {
      tipoDoc: "00",
      serie: "S000",
    });
    const response = await api.post("/tramas/descargar", payload, {
      responseType: "blob",
    });

    const serverFilename = getFilenameFromDisposition(
      response.headers?.["content-disposition"],
      preferredName,
    );
    const filename = preferredName || serverFilename;

    const blob = new Blob([response.data], { type: "text/plain;charset=utf-8" });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
    return filename;
  };

  const emitirDocumento = async () => {
    console.log("[InvoiceForm] onEmit fired");
    setIsGeneratingTxt(true);
    const values = methods.getValues();
    const payload = buildPayload(values);
    console.log("[InvoiceForm] PAYLOAD enviado ->", payload);

    try {
      const generarResponse = await api.post("/tramas/generar", payload);
      console.log("[InvoiceForm] Respuesta /tramas/generar ->", generarResponse.data);

      if (
        hasServerValidationErrors(generarResponse.data) &&
        showServerValidationErrors(generarResponse.data)
      ) {
        return;
      }

      const filename = await descargarTxt(payload);
      console.log(`[InvoiceForm] TXT descargado correctamente: ${filename}`);
    } catch (error) {
      console.error("[InvoiceForm] Error al emitir/descargar trama ->", error);
      notifyRequestError(error, "No se pudo emitir el documento");
    } finally {
      setIsGeneratingTxt(false);
    }
  };

  /**
   * Genera y descarga el XML del comprobante.
   */
  const generarXml = async () => {
    console.log("[InvoiceForm] generarXml fired");
    setIsGeneratingXml(true);
    const values = methods.getValues();
    console.log("[InvoiceForm] Generando XML con valores:", values);

    try {
      const filename = await generarYDescargarXml(values);
      console.log(`[InvoiceForm] XML descargado correctamente: ${filename}`);
    } catch (error) {
      console.error("[InvoiceForm] Error al generar XML ->", error);
      notifyRequestError(error, "No se pudo generar el XML");
    } finally {
      setIsGeneratingXml(false);
    }
  };

  // Guardar cambios no guardados al navegar/recargar
  useEffect(() => {
    if (!methods.formState.isDirty) return;
    const handler = (e: BeforeUnloadEvent) => {
      e.preventDefault();
      e.returnValue = "";
    };
    window.addEventListener("beforeunload", handler);
    return () => window.removeEventListener("beforeunload", handler);
  }, [methods.formState.isDirty]);

  return (
    <FormProvider {...methods}>
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pb-24">
      {/* Header */}
      <header className="bg-white border-b border-slate-200 shadow-sm sticky top-0 z-40">
        <div className="container mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button 
                variant="outline" 
                size="sm"
                onClick={goHome}
                className="shrink-0"
              >
                <ArrowLeft className="size-4 mr-2" aria-hidden="true" />
                Volver
              </Button>
              <Separator orientation="vertical" className="h-8" />
              <div className="flex items-center gap-3">
                <div className="bg-blue-600 p-2 rounded-lg">
                  <FileText className="size-6 text-white" aria-hidden="true" />
                </div>
                <div>
                  <h1 className="text-lg font-bold text-slate-900">Documentos de Venta</h1>
                  <p className="text-sm text-slate-600">Factura, Boleta, Nota de Crédito y Nota de Débito</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-6 py-8">
        <div className="space-y-6">
          {/* Información del Documento y Partes */}
          <Card className="shadow-lg">
            <CardContent className="p-6">
              <Tabs defaultValue="documento" className="space-y-6">
                <TabsList className="grid w-full grid-cols-3 h-auto p-1">
                  <TabsTrigger value="documento" className="py-3">
                    <div className="flex flex-col items-center gap-1">
                      <span className="font-medium">1. Documento</span>
                      <span className="text-xs text-slate-500">Tipo y detalles</span>
                    </div>
                  </TabsTrigger>
                  <TabsTrigger value="emisor" className="py-3">
                    <div className="flex flex-col items-center gap-1">
                      <span className="font-medium">2. Datos del Emisor</span>
                      <span className="text-xs text-slate-500">Información del vendedor</span>
                    </div>
                  </TabsTrigger>
                  <TabsTrigger value="receptor" className="py-3">
                    <div className="flex flex-col items-center gap-1">
                      <span className="font-medium">3. Datos del Receptor</span>
                      <span className="text-xs text-slate-500">Información del cliente</span>
                    </div>
                  </TabsTrigger>
                </TabsList>

                <TabsContent value="documento" className="mt-6">
                  <DocumentSection />
                  {isNota && (
                    <div className="mt-6">
                      <DocumentoRelacionadoSection
                        type={isCredit ? "credit" : "debit"}
                      />
                    </div>
                  )}
                </TabsContent>

                <TabsContent value="emisor" className="mt-6">
                  <IssuerSection />
                </TabsContent>

                <TabsContent value="receptor" className="mt-6">
                  <ReceiverSection />
                </TabsContent>
              </Tabs>
            </CardContent>
          </Card>

          {/* Productos - Sección Independiente */}
          <Card className="shadow-lg">
            <CardContent className="p-6">
              <ProductsTable />
            </CardContent>
          </Card>

          {/* Descuentos Globales */}
          <Card className="shadow-lg">
            <CardContent className="p-6">
              <DescuentosGlobalesSection />
            </CardContent>
          </Card>

          {/* Documentos Relacionados */}
          <Card className="shadow-lg">
            <CardContent className="p-6">
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2.5 rounded-lg bg-amber-100">
                  <FileText className="size-5 text-amber-700" aria-hidden="true" />
                </div>
                <div>
                  <h2 className="text-lg font-semibold text-slate-900">Documentos Relacionados</h2>
                  <p className="text-sm text-slate-500">Guía de remisión y documentos adicionales (opcional)</p>
                </div>
              </div>
              <Separator className="mb-6" />
              <div className="space-y-6">
                <GuiaRemisionSection />
                <Separator />
                <DocumentosAdicionalesSection />
              </div>
            </CardContent>
          </Card>
        </div>
      </main>

      {/* Barra de Acciones Flotante */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-slate-200 shadow-lg z-50">
        <div className="container mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="text-sm text-slate-600">
              <p className="font-medium">Complete todos los campos requeridos</p>
            </div>
            <div className="flex items-center gap-3">
              <Button variant="outline" size="lg" className="min-w-[120px]" onClick={goHome} disabled={isGeneratingTxt || isGeneratingXml}>
                <X className="size-4 mr-2" aria-hidden="true" />
                Cancelar
              </Button>
              
              {/* Botón para generar TXT */}
              <Button 
                variant="outline"
                size="lg" 
                className="min-w-[140px]"
                onClick={() => setShowEmitDialog(true)}
                disabled={isGeneratingTxt || isGeneratingXml}
              >
                {isGeneratingTxt ? (
                  <Loader2 className="size-4 mr-2 animate-spin" aria-hidden="true" />
                ) : (
                  <FileText className="size-4 mr-2" aria-hidden="true" />
                )}
                {isGeneratingTxt ? "Generando…" : "Generar TXT"}
              </Button>
              
              {/* Botón para generar XML */}
              <Button 
                size="lg" 
                className="min-w-[140px] bg-blue-600 hover:bg-blue-700"
                onClick={generarXml}
                disabled={isGeneratingTxt || isGeneratingXml}
              >
                {isGeneratingXml ? (
                  <Loader2 className="size-4 mr-2 animate-spin" aria-hidden="true" />
                ) : (
                  <FileCode className="size-4 mr-2" aria-hidden="true" />
                )}
                {isGeneratingXml ? "Generando…" : "Generar XML"}
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Modal de Emisión */}
      <EmitDocumentDialog 
        open={showEmitDialog} 
        onOpenChange={setShowEmitDialog}
        documentType="sales"
        onEmit={() => {
          void emitirDocumento();
        }}
      />
      </div>
    </FormProvider>
  );
}
