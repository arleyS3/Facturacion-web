import { Truck } from "lucide-react";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "../ui/accordion";
import { RouteCarrier } from "./RouteCarrier";
import { RouteDriver } from "./RouteDriver";
import { RouteAirport } from "./RouteAirport";
import { RouteContainers } from "./RouteContainers";
import { RouteIndicators } from "./RouteIndicators";
import { RouteSecondaryVehicles } from "./RouteSecondaryVehicles";

/**
 * Agrupa las secciones del tramo de la guía: transportista, conductor,
 * aeropuerto, contenedores, indicadores y vehículos secundarios.
 */
export function GuideRoute() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2 mb-6">
        <Truck className="size-5 text-blue-600" />
        <h3 className="font-semibold text-lg">Información del Tramo</h3>
      </div>

      {/* Secciones en Accordion */}
      <Accordion type="multiple" defaultValue={["transportista", "conductor"]} className="space-y-4">
        <AccordionItem value="transportista" className="bg-white border border-slate-200 rounded-lg px-4">
          <AccordionTrigger className="hover:no-underline py-4">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center size-8 rounded-full bg-blue-100 text-blue-700 font-semibold text-sm">
                1
              </div>
              <div className="text-left">
                <h4 className="font-semibold">Transportista</h4>
                <p className="text-xs text-slate-500">Datos del transportista y modalidad</p>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent className="pb-4 pt-2">
            <RouteCarrier />
          </AccordionContent>
        </AccordionItem>

        <AccordionItem value="conductor" className="bg-white border border-slate-200 rounded-lg px-4">
          <AccordionTrigger className="hover:no-underline py-4">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center size-8 rounded-full bg-green-100 text-green-700 font-semibold text-sm">
                2
              </div>
              <div className="text-left">
                <h4 className="font-semibold">Conductor</h4>
                <p className="text-xs text-slate-500">Información del conductor del vehículo</p>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent className="pb-4 pt-2">
            <RouteDriver />
          </AccordionContent>
        </AccordionItem>

        <AccordionItem value="aeropuerto" className="bg-white border border-slate-200 rounded-lg px-4">
          <AccordionTrigger className="hover:no-underline py-4">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center size-8 rounded-full bg-purple-100 text-purple-700 font-semibold text-sm">
                3
              </div>
              <div className="text-left">
                <h4 className="font-semibold">Aeropuerto</h4>
                <p className="text-xs text-slate-500">Puerto o aeropuerto de transporte</p>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent className="pb-4 pt-2">
            <RouteAirport />
          </AccordionContent>
        </AccordionItem>

        <AccordionItem value="contenedores" className="bg-white border border-slate-200 rounded-lg px-4">
          <AccordionTrigger className="hover:no-underline py-4">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center size-8 rounded-full bg-orange-100 text-orange-700 font-semibold text-sm">
                4
              </div>
              <div className="text-left">
                <h4 className="font-semibold">Contenedores</h4>
                <p className="text-xs text-slate-500">Contenedores y precintos de seguridad</p>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent className="pb-4 pt-2">
            <RouteContainers />
          </AccordionContent>
        </AccordionItem>

        <AccordionItem value="indicadores" className="bg-white border border-slate-200 rounded-lg px-4">
          <AccordionTrigger className="hover:no-underline py-4">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center size-8 rounded-full bg-teal-100 text-teal-700 font-semibold text-sm">
                5
              </div>
              <div className="text-left">
                <h4 className="font-semibold">Indicadores de Traslado</h4>
                <p className="text-xs text-slate-500">Indicadores especiales de SUNAT</p>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent className="pb-4 pt-2">
            <RouteIndicators />
          </AccordionContent>
        </AccordionItem>

        <AccordionItem value="vehiculos-secundarios" className="bg-white border border-slate-200 rounded-lg px-4">
          <AccordionTrigger className="hover:no-underline py-4">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center size-8 rounded-full bg-pink-100 text-pink-700 font-semibold text-sm">
                6
              </div>
              <div className="text-left">
                <h4 className="font-semibold">Vehículos Secundarios</h4>
                <p className="text-xs text-slate-500">Vehículos adicionales en el traslado</p>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent className="pb-4 pt-2">
            <RouteSecondaryVehicles />
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  );
}
