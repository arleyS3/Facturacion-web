"use client";

import { useForm, FormProvider } from "react-hook-form";
import { DocumentSection } from "@/features/ventas/components/DocumentSection";
import { ReceiverSection } from "@/features/ventas/components/ReceiverSection";
import { IssuerSection } from "@/features/ventas/components/IssuerSection";

export default function DocumentForm() {
  const methods = useForm({
    defaultValues: {
      companyCode: "1",
      docType: "",
      issuerDocType: "",
      receiverDocType: "",
      moneda: "",
      issuerSocialName: "",
      issuerAddress: "",
      receiverSocialName: "",
      receiverAddress: "",
    },
  });

  const onSubmit = (values: any) => {
    console.log("form submit", values);
  };

  return (
    <FormProvider {...methods}>
      <form onSubmit={methods.handleSubmit(onSubmit)} className="space-y-6">
        <DocumentSection />
        <IssuerSection />
        <ReceiverSection />
        <div className="flex gap-2">
          <button type="submit" className="btn btn-primary">Guardar</button>
        </div>
      </form>
    </FormProvider>
  );
}
