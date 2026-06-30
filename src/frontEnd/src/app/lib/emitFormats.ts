export type EmitDocumentType = "sales" | "shipping";

export interface EmitFormats {
  xml: boolean;
  json: boolean;
  txt: boolean;
}

export function getSelectableEmitFormats(
  documentType: EmitDocumentType,
  formats: EmitFormats,
): EmitFormats {
  return {
    xml: documentType === "shipping" && formats.xml,
    json: false,
    txt: formats.txt,
  };
}

export function countSelectedFormats(formats: EmitFormats): number {
  return Object.values(formats).filter(Boolean).length;
}
