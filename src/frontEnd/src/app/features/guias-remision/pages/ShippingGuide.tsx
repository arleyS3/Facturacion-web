import { useNavigate } from "react-router";
import { ArrowLeft, Save, X, Truck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Separator } from "@/components/ui/separator";
import { EmitDocumentDialog } from "@/components/EmitDocumentDialog";
import { GuideInformation } from "@/features/guias-remision/components/GuideInformation";
import { GuideDetails } from "@/features/guias-remision/components/GuideDetails";
import { GuideReferences } from "@/features/guias-remision/components/GuideReferences";
import { GuideRoute } from "@/features/guias-remision/components/GuideRoute";
import { GuideAdditionalData } from "@/features/guias-remision/components/GuideAdditionalData";
import { useState } from "react";
import { FormProvider, useForm } from "react-hook-form";
import { api } from "@/lib/api";
import buildShippingPayload from "@/lib/buildShippingPayload";
import {
  buildFilenameFromPayload,
  getFilenameFromDisposition,
} from "@/lib/downloadFilename";
import {
  hasServerValidationErrors,
  notifyRequestError,
  showServerValidationErrors,
} from "@/lib/emitErrorNotification";

export function ShippingGuide() {
  const navigate = useNavigate();
  const [showEmitDialog, setShowEmitDialog] = useState(false);
  const methods = useForm({
    defaultValues: {
      companyCode: "1",
      docType: "09",
      serie: "T001",
      correlativo: "",
      fechaEmision: "",
      horaEmision: "",
      issuerDocType: "6",
      issuerDocNumber: "",
      issuerSocialName: "",
      issuerCommercialName: "",
      issuerAddress: "",
      issuerAnexo: "",
      issuerNoDet: false,
      shippingItems: [],
      shippingReferences: [],
      shippingAdditionalData: [],
      shippingCarriers: [],
      shippingDrivers: [],
      shippingAirport: {
        tipoPuerto: "",
        nombrePuerto: "",
        codigoPuerto: "",
      },
      shippingContainers: [],
      shippingIndicators: [],
      shippingSecondaryVehicles: [],
    },
  });
  const goHome = () => {
    navigate("/", { replace: true });
    // Fallback: si la vista no se actualiza por estado del router, forzar navegación real.
    setTimeout(() => {
      if (window.location.pathname !== "/") {
        window.location.assign("/");
      }
    }, 0);
  };

  const descargarTxt = async (payload: any) => {
    const preferredName = buildFilenameFromPayload(payload, {
      tipoDoc: "09",
      serie: "T001",
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

  const emitirGuia = async () => {
    const values = methods.getValues();
    const payload = buildShippingPayload(values);

    console.log("[ShippingGuide] PAYLOAD enviado ->", payload);

    try {
      const generarResponse = await api.post("/tramas/generar", payload);
      console.log("[ShippingGuide] Respuesta /tramas/generar ->", generarResponse.data);

      if (
        hasServerValidationErrors(generarResponse.data) &&
        showServerValidationErrors(generarResponse.data)
      ) {
        return;
      }

      const filename = await descargarTxt(payload);
      console.log(`[ShippingGuide] TXT descargado correctamente: ${filename}`);
    } catch (error) {
      console.error("[ShippingGuide] Error al emitir guía ->", error);
      notifyRequestError(error, "No se pudo emitir la guía");
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
                <div className="bg-green-600 p-2 rounded-lg">
                  <Truck className="size-6 text-white" />
                </div>
                <div>
                  <h1 className="text-lg font-bold text-slate-900">Guía de Remisión Electrónica</h1>
                  <p className="text-sm text-slate-600">Documento de traslado de mercancías</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-6 py-8">
        <Card className="shadow-lg">
          <CardContent className="p-6">
            <Tabs defaultValue="informacion" className="space-y-6">
              <TabsList className="grid w-full grid-cols-5 h-auto p-1">
                <TabsTrigger value="informacion" className="py-3">
                  <div className="flex flex-col items-center gap-1">
                    <span className="font-medium text-sm">Información Guía</span>
                  </div>
                </TabsTrigger>
                <TabsTrigger value="detalle" className="py-3">
                  <div className="flex flex-col items-center gap-1">
                    <span className="font-medium text-sm">Detalle</span>
                  </div>
                </TabsTrigger>
                <TabsTrigger value="referencias" className="py-3">
                  <div className="flex flex-col items-center gap-1">
                    <span className="font-medium text-sm">Referencias</span>
                  </div>
                </TabsTrigger>
                <TabsTrigger value="tramo" className="py-3">
                  <div className="flex flex-col items-center gap-1">
                    <span className="font-medium text-sm">Tramo</span>
                  </div>
                </TabsTrigger>
                <TabsTrigger value="adicionales" className="py-3">
                  <div className="flex flex-col items-center gap-1">
                    <span className="font-medium text-sm">Datos Adicionales</span>
                  </div>
                </TabsTrigger>
              </TabsList>

              <TabsContent value="informacion" className="mt-6">
                <GuideInformation />
              </TabsContent>

              <TabsContent value="detalle" className="mt-6">
                <GuideDetails />
              </TabsContent>

              <TabsContent value="referencias" className="mt-6">
                <GuideReferences />
              </TabsContent>

              <TabsContent value="tramo" className="mt-6">
                <GuideRoute />
              </TabsContent>

              <TabsContent value="adicionales" className="mt-6">
                <GuideAdditionalData />
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>
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
                className="min-w-[140px] bg-green-600 hover:bg-green-700"
                onClick={() => setShowEmitDialog(true)}
              >
                <Save className="size-4 mr-2" />
                Emitir Guía
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Modal de Emisión */}
      <EmitDocumentDialog 
        open={showEmitDialog} 
        onOpenChange={setShowEmitDialog}
        documentType="shipping"
        onEmit={() => {
          void emitirGuia();
        }}
      />
    </div>
    </FormProvider>
  );
}