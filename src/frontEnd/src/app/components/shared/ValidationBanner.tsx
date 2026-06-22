"use client";

import { AlertTriangle, ChevronDown, ChevronRight } from "lucide-react";
import { Alert, AlertTitle, AlertDescription } from "@/components/ui/alert";
import {
  Collapsible,
  CollapsibleTrigger,
  CollapsibleContent,
} from "@/components/ui/collapsible";
import { Button } from "@/components/ui/button";
import { useState } from "react";

interface ValidationBannerProps {
  errors: string[];
}

/**
 * Banner no bloqueante que muestra advertencias de validación del XML generado.
 * Si el array de errores está vacío o es undefined, no renderiza nada.
 */
export function ValidationBanner({ errors }: ValidationBannerProps) {
  const [isOpen, setIsOpen] = useState(false);

  if (!errors || errors.length === 0) {
    return null;
  }

  return (
    <Alert className="border-amber-400 bg-amber-50 [&>svg]:text-amber-600">
      <AlertTriangle className="size-4" aria-hidden="true" />
      <AlertTitle className="text-amber-800">
        Se encontraron {errors.length} advertencia(s) de validación en el XML
        generado
      </AlertTitle>
      <AlertDescription>
        <Collapsible open={isOpen} onOpenChange={setIsOpen}>
          <CollapsibleTrigger asChild>
            <Button
              variant="ghost"
              size="sm"
              className="mt-1 h-auto px-0 text-amber-700 hover:text-amber-900 hover:bg-transparent"
            >
              {isOpen ? (
                <ChevronDown className="size-4 mr-1" aria-hidden="true" />
              ) : (
                <ChevronRight className="size-4 mr-1" aria-hidden="true" />
              )}
              {isOpen ? "Ocultar detalles" : "Ver detalles"}
            </Button>
          </CollapsibleTrigger>
          <CollapsibleContent className="mt-2">
            <ul className="list-disc pl-5 space-y-1 text-amber-700">
              {errors.map((error, index) => (
                <li key={index}>{error}</li>
              ))}
            </ul>
          </CollapsibleContent>
        </Collapsible>
      </AlertDescription>
    </Alert>
  );
}

export default ValidationBanner;
