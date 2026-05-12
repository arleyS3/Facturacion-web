import { Save, X, FileText, ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ReceiverSection } from "@/features/ventas/components/ReceiverSection";
import { DocumentSection } from "@/features/ventas/components/DocumentSection";
import { ProductsTable } from "@/features/ventas/components/ProductsTable";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { EmitDocumentDialog } from "@/components/EmitDocumentDialog";
import { useState } from "react";
import { FormProvider, useForm } from "react-hook-form";
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

/**
 * Componente del formulario de documentos de venta. Maneja el flujo de
 * armado del payload, llamada a /tramas/generar y descarga de TXT.
 */
export function InvoiceForm() {
  const [showEmitDialog, setShowEmitDialog] = useState(false);
  const goHome = () => {
    // Regreso confiable: recarga Home para evitar quedarse en vista previa con URL cambiada.
    window.location.assign("/");
  };
  const methods = useForm({
    defaultValues: {
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
      receiverSocialName: "",
      receiverAddress: "",
      receiverDepartment: "",
      receiverProvince: "",
      receiverDistrict: "",
      receiverUbigeo: "",
      moneda: "",
      serie: "F001",
      correlativo: "",
      fechaEmision: "",
      horaEmision: "",
      products: [],
    },
  });

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
    }
  };

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
                <ArrowLeft className="size-4 mr-2" />
                Volver
              </Button>
              <Separator orientation="vertical" className="h-8" />
              <div className="flex items-center gap-3">
                <div className="bg-blue-600 p-2 rounded-lg">
                  <FileText className="size-6 text-white" />
                </div>
                <div>
                  <h1 className="text-lg font-bold text-slate-900">Documentos de Venta</h1>
                  <p className="text-sm text-slate-600">Factura, Boleta y Nota de Crédito</p>
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
              <Button variant="outline" size="lg" className="min-w-[120px]" onClick={goHome}>
                <X className="size-4 mr-2" />
                Cancelar
              </Button>
              <Button 
                size="lg" 
                className="min-w-[140px] bg-blue-600 hover:bg-blue-700"
                onClick={() => setShowEmitDialog(true)}
              >
                <Save className="size-4 mr-2" />
                Emitir Documento
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
