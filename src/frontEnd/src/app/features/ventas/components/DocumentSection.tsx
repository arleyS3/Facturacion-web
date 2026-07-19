import { useState } from "react";
import { useFormContext } from "react-hook-form";
import { FileText, Calendar, ChevronDown, ChevronUp, HelpCircle } from "lucide-react";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import { DocumentHeader } from "@/components/shared/DocumentHeader";
import { DatePicker } from "@/components/shared/DatePicker";

// Catálogo de leyendas según SUNAT
const CATALOGO_LEYENDAS = [
  {
    codigo: "1000",
    descripcion: "Monto en Letras",
    helper: "Consigne el monto total expresado en palabras. Ejemplo: 'CIEN Y 00/100 SOLES'",
  },
  {
    codigo: "1002",
    descripcion: "TRANSFERENCIA GRATUTA DE UN BIEN Y/O SERVICIO PRESTADO GRATUTAMENTE",
    helper: "Aplicable solo cuando TODAS las operaciones (líneas o ítems) son gratuitas.",
  },
  {
    codigo: "2000",
    descripcion: "COMPROBANTE DE PERCEPCIÓN",
    helper: "Utilizado en operaciones de venta sujetas al Régimen de Percepción del IGV.",
  },
  {
    codigo: "2001",
    descripcion: "BIENES TRANSFERIDOS EN LA AMAZONÍA REGIÓN SELVA PARA SER CONSUMIDOS EN LA MISMA",
    helper: "Aplicable a operaciones exoneradas del IGV según Decreto Supremo N°103-99-EF (Promoción Amazonía).",
  },
  {
    codigo: "2002",
    descripcion: "SERVICIOS PRESTADOS EN LA AMAZONÍA REGIÓN SELVA PARA SER CONSUMIDOS EN LA MISMA",
    helper: "Aplicable a servicios exonerados del IGV según Decreto Supremo N°103-99-EF (Promoción Amazonía).",
  },
  {
    codigo: "2003",
    descripcion: "CONTRATOS DE CONSTRUCCIÓN EJECUTADOS EN LA AMAZONÍA REGIÓN SELVA",
    helper: "Aplicable a contratos de construcción exonerados del IGV según Decreto Supremo N°103-99-EF.",
  },
  {
    codigo: "2004",
    descripcion: "Agencia de Viaje - Paquete turístico",
    helper: "Solo para agencias de viaje y turismo incluidas en el Directorio Nacional de Prestadores de Servicios Turísticos Calificados (MINCETUR).",
  },
  {
    codigo: "2005",
    descripcion: "Venta realizada por emisor itinerante",
    helper: "Consignar cuando el comprobante se otorga durante el recorrido de venta itinerante.",
  },
  {
    codigo: "2006",
    descripcion: "Operación sujeta a detracción",
    helper: "Operaciones sujetas al Sistema de Pago de Obligaciones Tributarias (SPOT) - Decreto Legislativo N°940.",
  },
  {
    codigo: "2007",
    descripcion: "Operación sujeta al IVAP",
    helper: "Operaciones sujetas al Impuesto a las Ventas de Arroz Pilado (IVAP).",
  },
  {
    codigo: "2008",
    descripcion: "VENTA EXONERADA DEL IGV-ISC-IPM. PROHIBIDA LA VENTA FUERA DE LA ZONA COMERCIAL DE TACNA",
    helper: "Aplicar en ventas exoneradas dentro de la Zona Comercial de Tacna.",
  },
  {
    codigo: "2009",
    descripcion: "PRIMERA VENTA DE MERCANCÍA IDENTIFICABLE ENTRE USUARIOS DE LA ZONA COMERCIAL",
    helper: "Para primera venta de mercancía entre usuarios de la Zona Comercial de Tacna.",
  },
  {
    codigo: "2010",
    descripcion: "Restitución Simplificado de Derechos Arancelarios",
    helper: "Aplicar en operaciones con restitución simplificada de derechos arancelarios.",
  },
  {
    codigo: "2011",
    descripcion: "EXPORTACIÓN DE SERVICIOS - DECRETO LEGISLATIVO N° 919",
    helper: "Para exportación de servicios según Decreto Legislativo N°919.",
  },
  {
    codigo: "2012",
    descripcion: "Observaciones relacionadas con el traslado de mercaderías",
    helper: "Consigne observaciones relacionadas con el traslado de mercaderías.",
  },
];

export function DocumentSection() {
  const methods = useFormContext();
  const watch = methods ? methods.watch : undefined;
  const setValue = methods ? methods.setValue : undefined;
  
  // =================================================================
  // CAMPOS NUEVOS EN ESPAÑOL (del schema unificado)
  // =================================================================
  const fechaVencimientoForm = watch ? watch("fechaVencimiento") ?? "" : "";
  
  // =================================================================
  // ESTADOS LOCALES + FALLBACK
  // =================================================================
  const [leyendasExpandido, setLeyendasExpandido] = useState(false);
  const [leyendasSeleccionadasLocal, setLeyesSeleccionadasLocal] = useState<string[]>([]);
  const [fechaVencimientoLocal, setFechaVencimientoLocal] = useState("");

  // Usar valores del form si existen, sino los locales
  const fechaVencimiento = fechaVencimientoForm || fechaVencimientoLocal;
  const leyendasSeleccionadas = leyendasSeleccionadasLocal;

  const toggleLeyenda = (codigo: string) => {
    const nuevas = leyendasSeleccionadasLocal.includes(codigo) 
      ? leyendasSeleccionadasLocal.filter((c) => c !== codigo)
      : [...leyendasSeleccionadasLocal, codigo];
    
    setLeyesSeleccionadasLocal(nuevas);
    
    // También guardar en el formulario con los nuevos nombres en español
    if (setValue) {
      const leyendasFormateadas = nuevas.map(codigo => {
        const leyenda = CATALOGO_LEYENDAS.find(l => l.codigo === codigo);
        return {
          codigoLocal: codigo,
          leyenda: leyenda?.descripcion || ""
        };
      });
      setValue("leyendas", leyendasFormateadas);
    }
  };

  // Guardar fecha de vencimiento en el formulario cuando cambie
  const handleFechaVencimientoChange = (value: string) => {
    setFechaVencimientoLocal(value);
    if (setValue) {
      setValue("fechaVencimiento", value);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header de sección */}
      <div className="flex items-center gap-3">
        <div className="p-2.5 rounded-lg bg-primary/10">
          <FileText className="size-5 text-primary" aria-hidden="true" />
        </div>
        <div>
          <h3 className="font-semibold text-lg text-foreground">Información del Documento</h3>
          <p className="text-sm text-muted-foreground">Configure los datos principales del comprobante</p>
        </div>
      </div>

      {/* DocumentHeader - Datos principales del documento */}
      <DocumentHeader type="sales" methods={methods} formState={methods.formState} />

      {/* Sección de campos adicionales */}
      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Calendar className="size-4 text-muted-foreground" aria-hidden="true" />
              <CardTitle className="text-sm font-medium">Datos Adicionales</CardTitle>
              <span className="text-xs text-muted-foreground">(Opcional)</span>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-5">
          {/* Fecha de vencimiento */}
          <div className="space-y-2">
            <Label htmlFor="fechaVencimiento" className="text-sm font-medium">
              Fecha de Vencimiento
            </Label>
            <DatePicker
              value={fechaVencimiento}
              onChange={handleFechaVencimientoChange}
              placeholder="Seleccione fecha de vencimiento"
            />
            <p className="text-xs text-muted-foreground">
              Fecha límite de pago del documento (opcional)
            </p>
          </div>

          {/* Selector de leyendas */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <Label className="text-sm font-medium">Leyendas</Label>
              <button
                type="button"
                onClick={() => setLeyendasExpandido(!leyendasExpandido)}
                className="flex items-center gap-1 text-sm text-primary hover:text-primary/80 transition-colors"
              >
                {leyendasExpandido ? (
                  <>
                    <ChevronUp className="size-4" />
                    <span>Ocultar opciones</span>
                  </>
                ) : (
                  <>
                    <ChevronDown className="size-4" />
                    <span>Ver {CATALOGO_LEYENDAS.length} opciones</span>
                  </>
                )}
              </button>
            </div>

            {/* Leyendas seleccionadas (tags) */}
            {leyendasSeleccionadas.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {leyendasSeleccionadas.map((codigo) => {
                  const leyenda = CATALOGO_LEYENDAS.find((l) => l.codigo === codigo);
                  return (
                    <span
                      key={codigo}
                      className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-primary/10 text-primary text-sm"
                    >
                      <span className="font-mono text-xs">{codigo}</span>
                      <span className="text-xs">{leyenda?.descripcion.substring(0, 25)}...</span>
                      <button
                        type="button"
                        onClick={() => toggleLeyenda(codigo)}
                        className="ml-1 hover:text-primary/60 transition-colors"
                        aria-label={`Remover leyenda ${codigo}`}
                      >
                        ×
                      </button>
                    </span>
                  );
                })}
              </div>
            )}

            {/* Catálogo de leyendas en accordion */}
            {leyendasExpandido && (
              <div className="border border-border rounded-lg overflow-hidden">
                <div className="bg-muted/30 px-3 py-2 border-b border-border">
                  <p className="text-xs text-muted-foreground">
                    Seleccione una o más leyendas aplicables al documento
                  </p>
                </div>
                <div className="divide-y divide-border max-h-80 overflow-y-auto">
                  {CATALOGO_LEYENDAS.map((leyenda) => {
                    const estaSeleccionada = leyendasSeleccionadas.includes(leyenda.codigo);
                    return (
                      <label
                        key={leyenda.codigo}
                        className={`flex items-start gap-3 p-3 cursor-pointer transition-colors hover:bg-muted/50 ${
                          estaSeleccionada ? "bg-primary/5" : ""
                        }`}
                      >
                        <Checkbox
                          id={`leyenda-${leyenda.codigo}`}
                          checked={estaSeleccionada}
                          onCheckedChange={() => toggleLeyenda(leyenda.codigo)}
                          className="mt-0.5"
                        />
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="font-mono text-xs bg-muted px-1.5 py-0.5 rounded text-foreground">
                              {leyenda.codigo}
                            </span>
                            <span className="text-sm font-medium text-foreground truncate">
                              {leyenda.descripcion}
                            </span>
                            <TooltipProvider>
                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <HelpCircle className="size-4 text-muted-foreground shrink-0" aria-hidden="true" />
                                </TooltipTrigger>
                                <TooltipContent side="top" className="max-w-xs">
                                  <p className="text-xs">{leyenda.helper}</p>
                                </TooltipContent>
                              </Tooltip>
                            </TooltipProvider>
                          </div>
                          <p className="text-xs text-muted-foreground line-clamp-2">
                            {leyenda.helper}
                          </p>
                        </div>
                      </label>
                    );
                  })}
                </div>
              </div>
            )}

            {leyendasSeleccionadas.length === 0 && !leyendasExpandido && (
              <p className="text-xs text-muted-foreground">
                No hay leyendas seleccionadas. Haga clic en "Ver opciones" para seleccionar.
              </p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Tips útiles */}
      <div className="rounded-lg bg-muted/50 p-4 flex items-start gap-3">
        <div className="size-5 rounded-full bg-primary/20 flex items-center justify-center shrink-0 mt-0.5">
          <span className="text-xs font-bold text-primary">i</span>
        </div>
        <div className="text-sm text-muted-foreground">
          <p className="font-medium text-foreground mb-1">Información útil</p>
          <ul className="space-y-1 text-xs">
            <li>• La fecha de vencimiento es útil para facturas con crédito</li>
            <li>• Las leyendas se incluyen en la impresión del documento</li>
            <li>• Use el icono de ayuda (?) junto a cada leyenda para entender cuándo usarla</li>
          </ul>
        </div>
      </div>
    </div>
  );
}