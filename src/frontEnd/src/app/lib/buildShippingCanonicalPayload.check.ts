import assert from "node:assert/strict";

import { buildShippingCanonicalPayload } from "./buildShippingCanonicalPayload.ts";

const payload = buildShippingCanonicalPayload({
  serie: "T001",
  correlativo: "7",
  fechaEmision: "2026-06-29",
  issuerDocNumber: "20123456789",
  issuerSocialName: "ACME SAC",
  receiverDocNumber: "20987654321",
  receiverSocialName: "CLIENTE SAC",
  gMotTras: "01",
  gModTras: "02",
  g1FecIniTras: "2026-06-30",
  gDirParUbiGeo: "150101",
  gDirParDireccion: "Av. Inicio 123",
  gDirLlegUbiGeo: "150102",
  gDirLlegDireccion: "Av. Llegada 456",
  gTotalPesoTraslado: "12.5",
  gUnmdTotalPeso: "KGM",
  gNroBultos: "3",
  gPlacaVehicPrinc: "ABC123",
  shippingDrivers: [{ numeroDocumento: "12345678", codTipoDocumento: "1" }],
  shippingItems: [{ QtyItem: "2", UnmdItem: "NIU", NmbItem: "Box" }],
});

assert.equal(payload.tipo_documento, "09");
assert.equal(payload.numero, "T001-00000007");
assert.equal(payload.parte_traslado.modalidad_traslado, "02");
assert.equal(payload.parte_traslado.peso_bruto_total, 12.5);
assert.deepEqual(payload.detalles, [
  {
    codigo_producto: undefined,
    descripcion: "Box",
    cantidad: 2,
    valor_unitario: 0.01,
    igv: 0,
    codigo_tipoIgv: "30",
    unidad_medida: "NIU",
  },
]);

const fallbackRecipientPayload = buildShippingCanonicalPayload({
  serie: "T001",
  correlativo: "8",
  fechaEmision: "2026-06-29",
  issuerDocNumber: "20123456789",
  issuerSocialName: "ACME SAC",
  receiverDocNumber: " ",
  receiverSocialName: " ",
  gRucComprador: "20987654321",
  gRazonComprador: "COMPRADOR SAC",
  shippingItems: [{ QtyItem: "1", NmbItem: "Box" }],
});

assert.equal(fallbackRecipientPayload.receptor_documento, "20987654321");
assert.equal(fallbackRecipientPayload.receptor_razonSocial, "COMPRADOR SAC");
assert.notEqual(fallbackRecipientPayload.receptor_razonSocial.trim(), "");

const missingDatePayload = buildShippingCanonicalPayload({
  serie: "T001",
  correlativo: "9",
});

assert.equal(missingDatePayload.fecha_emision, "");
