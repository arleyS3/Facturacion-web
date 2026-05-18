"use client";

import { useForm, FormProvider } from "react-hook-form";
import { DocumentSection } from "@/features/ventas/components/DocumentSection";
import { ReceiverSection } from "@/features/ventas/components/ReceiverSection";
import { IssuerSection } from "@/features/ventas/components/IssuerSection";

/**
 * Tipado para el formulario de documentos de venta
 * Los nombres de campos están en español para el equipo
 */
interface FormDataDocumento {
  // Datos de la empresa/emisor
  codigoEmpresa: string;
  tipoDocumento: string;
  razonSocialEmisor: string;
  direccionEmisor: string;
  tipoDocEmisor: string;
  
  // Datos del receptor/cliente
  razonSocialReceptor: string;
  direccionReceptor: string;
  tipoDocReceptor: string;
  
  // Datos monetarios
  tipoMoneda: string;
}

export default function DocumentForm() {
  // useForm crea el "contenedor" de valores del formulario
  const methods = useForm<FormDataDocumento>({
    defaultValues: {
      codigoEmpresa: "1",
      tipoDocumento: "",
      razonSocialEmisor: "",
      direccionEmisor: "",
      tipoDocEmisor: "",
      razonSocialReceptor: "",
      direccionReceptor: "",
      tipoDocReceptor: "",
      tipoMoneda: "",
    },
  });

  /**
   * Función que se ejecuta al enviar el formulario
   * @param datos - Objeto con todos los valores del formulario
   */
  const alEnviarFormulario = (datos: FormDataDocumento) => {
    console.log("Datos del formulario:", datos);
    // aca iría la llamada a la API
  };

  return (
    // FormProvider reparte los métodos a todos los hijos (sections)
    <FormProvider {...methods}>
      <form 
        onSubmit={methods.handleSubmit(alEnviarFormulario)} 
        className="space-y-6"
      >
        {/* Sección 1: Datos del documento */}
        <DocumentSection />
        
        {/* Sección 2: Datos del emisor (empresa) */}
        <IssuerSection />
        
        {/* Sección 3: Datos del receptor (cliente) */}
        <ReceiverSection />
        
        {/* Botón de enviar */}
        <div className="flex gap-2">
          <button 
            type="submit" 
            className="px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90"
          >
            Guardar
          </button>
        </div>
      </form>
    </FormProvider>
  );
}