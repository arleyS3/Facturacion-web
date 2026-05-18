import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { TransportData } from "./TransportData";
import { TransportDestination } from "./TransportDestination";
import { StartPoint } from "./StartPoint";
import { EndPoint } from "./EndPoint";
import { ProviderBuyer } from "./ProviderBuyer";
import { DocumentHeader } from "@/components/shared/DocumentHeader";
import { FileText } from "lucide-react";
import { IssuerData } from "@/components/shared/IssuerData";
import { useFormContext } from "react-hook-form";

/**
 * Agrupa las secciones principales del formulario de guía (traslado,
 * transportista, partida, llegada y proveedor).
 */
export function GuideInformation() {
  const methods = useFormContext();

  return (
    <div className="space-y-6">
            <div className="flex items-center gap-2 mb-6">
              <FileText className="size-5 text-blue-600" />
              <h3 className="font-semibold text-lg">Información del Documento</h3>
            </div>
      <DocumentHeader type="shipping" methods={methods} />
      <IssuerData showAnexo={false} />
      {/* Secciones en Accordion */}
      <Accordion type="multiple" defaultValue={["traslado", "transportista"]} className="space-y-4">
        <AccordionItem value="traslado" className="bg-white border border-slate-200 rounded-lg px-4">
          <AccordionTrigger className="hover:no-underline py-4">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center size-8 rounded-full bg-blue-100 text-blue-700 font-semibold text-sm">
                1
              </div>
              <div className="text-left">
                <h4 className="font-semibold">Datos del Traslado</h4>
                <p className="text-xs text-slate-500">Motivo y descripción del traslado</p>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent className="pb-4 pt-2">
            <TransportData />
          </AccordionContent>
        </AccordionItem>

        <AccordionItem value="transportista" className="bg-white border border-slate-200 rounded-lg px-4">
          <AccordionTrigger className="hover:no-underline py-4">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center size-8 rounded-full bg-blue-100 text-blue-700 font-semibold text-sm">
                2
              </div>
              <div className="text-left">
                <h4 className="font-semibold">Tercero o Destinatario</h4>
                <p className="text-xs text-slate-500">Datos del tercero o destinatario</p>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent className="pb-4 pt-2">
            <TransportDestination />
          </AccordionContent>
        </AccordionItem>

        <AccordionItem value="partida" className="bg-white border border-slate-200 rounded-lg px-4">
          <AccordionTrigger className="hover:no-underline py-4">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center size-8 rounded-full bg-green-100 text-green-700 font-semibold text-sm">
                3
              </div>
              <div className="text-left">
                <h4 className="font-semibold">Punto de Partida</h4>
                <p className="text-xs text-slate-500">Dirección y ubicación de origen</p>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent className="pb-4 pt-2">
            <StartPoint />
          </AccordionContent>
        </AccordionItem>

        <AccordionItem value="llegada" className="bg-white border border-slate-200 rounded-lg px-4">
          <AccordionTrigger className="hover:no-underline py-4">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center size-8 rounded-full bg-red-100 text-red-700 font-semibold text-sm">
                4
              </div>
              <div className="text-left">
                <h4 className="font-semibold">Punto de Llegada</h4>
                <p className="text-xs text-slate-500">Dirección y ubicación de destino</p>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent className="pb-4 pt-2">
            <EndPoint />
          </AccordionContent>
        </AccordionItem>

        <AccordionItem value="proveedor" className="bg-white border border-slate-200 rounded-lg px-4">
          <AccordionTrigger className="hover:no-underline py-4">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center size-8 rounded-full bg-purple-100 text-purple-700 font-semibold text-sm">
                5
              </div>
              <div className="text-left">
                <h4 className="font-semibold">Proveedor / Comprador</h4>
                <p className="text-xs text-slate-500">Datos del proveedor o comprador</p>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent className="pb-4 pt-2">
            <ProviderBuyer />
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  );
}
