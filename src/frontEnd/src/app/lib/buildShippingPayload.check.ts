import assert from "node:assert/strict";

import { buildShippingPayload } from "./buildShippingPayload.ts";

const payload = buildShippingPayload({
  companyCode: "",
  codigoEmpresa: "7",
  issuerDocType: "",
  tipoDocEmisor: "6",
  issuerDocNumber: "",
  numeroDocEmisor: "20123456789",
  issuerSocialName: "",
  razonSocialEmisor: "ACME SAC",
  localCode: "",
  anexoEmisor: "0001",
  shippingAirport: {
    codigoPuerto: "",
    CodPueAer: "LIM",
  },
  shippingItems: [
    {
      NroLinDet: "",
      correlativo: "3",
      QtyItem: "",
      cantidad: "2",
      UnmdItem: "",
      unidadMedida: "NIU",
      NmbItem: "",
      descripcion: "Box",
    },
  ],
});

assert.deepEqual(payload.secciones.campos.A, {
  CODI_EMPR: "7",
  Serie: "T001",
  TipoRucEmis: "6",
  RUTEmis: "20123456789",
  RznSocEmis: "ACME SAC",
  CodigoLocalAnexo: "0001",
});

assert.deepEqual(payload.secciones.campos.G2, {
  CodPueAer: "LIM",
});

assert.deepEqual(payload.secciones.listas.B, [
  {
    NroLinDet: "3",
    QtyItem: "2",
    UnmdItem: "NIU",
    NmbItem: "Box",
  },
]);
