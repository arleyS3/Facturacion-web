type Values = Record<string, any>;

type ShippingItem = {
  NroLinDet: string;
  QtyItem: string;
  UnmdItem: string;
  NmbItem: string;
};

function nonEmpty(value: any): boolean {
  return value != null && value !== "";
}

function cleanObject<T extends Record<string, any>>(obj: T): Partial<T> {
  const entries = Object.entries(obj).filter(([, value]) => nonEmpty(value));
  return Object.fromEntries(entries) as Partial<T>;
}

function toStringValue(value: any, fallback = ""): string {
  if (value == null) return fallback;
  return String(value);
}

function buildItems(values: Values): ShippingItem[] {
  const source = Array.isArray(values.shippingItems)
    ? values.shippingItems
    : Array.isArray(values.products)
      ? values.products
      : [];

  return source.map((item: any, index: number) => ({
    NroLinDet: toStringValue(item.NroLinDet ?? item.correlativo ?? index + 1),
    QtyItem: toStringValue(item.QtyItem ?? item.cantidad ?? "0"),
    UnmdItem: toStringValue(item.UnmdItem ?? item.unidadMedida ?? "NIU"),
    NmbItem: toStringValue(item.NmbItem ?? item.descripcion ?? ""),
  }));
}

function buildReferences(values: Values): Array<Record<string, string>> {
  const source = Array.isArray(values.shippingReferences) ? values.shippingReferences : [];

  return source.map((item: any, index: number) =>
    cleanObject({
      NroLinRef: toStringValue(item.NroLinRef ?? index + 1),
      DescTpoDocRef: toStringValue(item.DescTpoDocRef ?? ""),
      TpoDocRef: toStringValue(item.TpoDocRef ?? ""),
      SerieRef: toStringValue(item.SerieRef ?? ""),
      FolioRef: toStringValue(item.FolioRef ?? ""),
      TipoRucEmisDocRef: toStringValue(item.TipoRucEmisDocRef ?? ""),
      RucEmisDocRef: toStringValue(item.RucEmisDocRef ?? ""),
    }) as Record<string, string>,
  );
}

function buildAdditionalData(values: Values): Array<Record<string, string>> {
  const source = Array.isArray(values.shippingAdditionalData)
    ? values.shippingAdditionalData
    : [];

  return source.map((item: any, index: number) =>
    cleanObject({
      TipoAdicSunat: toStringValue(item.TipoAdicSunat ?? ""),
      NmrLineasDetalle: toStringValue(item.NmrLineasDetalle ?? index + 1),
      NmrLineasAdicSunat: toStringValue(item.NmrLineasAdicSunat ?? ""),
      DescripcionAdicsunat: toStringValue(item.DescripcionAdicsunat ?? ""),
    }) as Record<string, string>,
  );
}

function buildCarriers(values: Values): Array<Record<string, string>> {
  const source = Array.isArray(values.shippingCarriers) ? values.shippingCarriers : [];

  return source.map((item: any, index: number) =>
    cleanObject({
      NroLinTramo: toStringValue(item.correlativo ?? index + 1),
      ModalidadTraslado: toStringValue(item.modalidad ?? ""),
      FechInicioTraslado: toStringValue(item.fechaInicio ?? ""),
      RucTranspor: toStringValue(item.ruc ?? ""),
      TipoRucTrans: toStringValue(item.tipoRuc ?? ""),
      RazoTrans: toStringValue(item.razonSocial ?? ""),
      NumRegMTC: toStringValue(item.numRegMTC ?? ""),
    }) as Record<string, string>,
  );
}

function buildDrivers(values: Values): Array<Record<string, string>> {
  const source = Array.isArray(values.shippingDrivers) ? values.shippingDrivers : [];

  return source.map((item: any, index: number) =>
    cleanObject({
      NroLinConductor: toStringValue(item.correlativo ?? index + 1),
      TipoConductor: toStringValue(item.tipo ?? ""),
      ConductorNroDocId: toStringValue(item.numeroDocumento ?? ""),
      ConductorTipoDocId: toStringValue(item.codTipoDocumento ?? ""),
      NombConductor: toStringValue(item.nombres ?? ""),
      ApellConductor: toStringValue(item.apellidos ?? ""),
      NumLicenciaCond: toStringValue(item.nroLicencia ?? ""),
    }) as Record<string, string>,
  );
}

function buildAirport(values: Values): Array<Record<string, string>> {
  const airport = values.shippingAirport;
  
  if (!airport || typeof airport !== "object") {
    return [];
  }

  return [
    cleanObject({
      NroLinPuertoAerop: "1",
      IdPuertoAerop: toStringValue(airport.codigoPuerto ?? ""),
      NmbPuertoAerop: toStringValue(airport.nombrePuerto ?? ""),
      TipoPuertoAerop: toStringValue(airport.tipoPuerto ?? ""),
    }) as Record<string, string>,
  ];
}

function buildContainers(values: Values): Array<Record<string, string>> {
  const source = Array.isArray(values.shippingContainers) ? values.shippingContainers : [];

  return source.map((item: any, index: number) =>
    cleanObject({
      NroLinContenedor: toStringValue(item.correlativo ?? index + 1),
      IdContenedor: toStringValue(item.idContenedor ?? ""),
      NumPrecinto: toStringValue(item.nroPrecinto ?? ""),
    }) as Record<string, string>,
  );
}

function buildIndicators(values: Values): Array<Record<string, string>> {
  const source = Array.isArray(values.shippingIndicators) ? values.shippingIndicators : [];

  return source.map((item: any, index: number) =>
    cleanObject({
      NroLinIndicadorTrasl: toStringValue(item.correlativo ?? index + 1),
      IndicadorTraslado: toStringValue(item.indicador ?? ""),
    }) as Record<string, string>,
  );
}

function buildSecondaryVehicles(values: Values): Array<Record<string, string>> {
  const source = Array.isArray(values.shippingSecondaryVehicles) ? values.shippingSecondaryVehicles : [];

  return source.map((item: any, index: number) =>
    cleanObject({
      NroLinVehSec: toStringValue(item.correlativo ?? index + 1),
      PlacaVehicSec: toStringValue(item.placa ?? ""),
      CertHabVehicularSec: toStringValue(item.certificado ?? ""),
    }) as Record<string, string>,
  );
}

export function buildShippingPayload(values: Values) {
  const tipoDocumento = toStringValue(values.docType, "09");

  const camposA = cleanObject({
    CODI_EMPR: toStringValue(values.companyCode ?? "1"),
    Serie: toStringValue(values.serie ?? "T001"),
    Correlativo: toStringValue(values.correlativo ?? ""),
    FchEmis: toStringValue(values.fechaEmision ?? ""),
    HoraEmision: toStringValue(values.horaEmision ?? ""),
    TipoRucEmis: toStringValue(values.issuerDocType ?? "6"),
    RUTEmis: toStringValue(values.issuerDocNumber ?? ""),
    RznSocEmis: toStringValue(values.issuerSocialName ?? ""),
    CodigoLocalAnexo: toStringValue(values.localCode ?? values.issuerAnexo ?? "0000"),
    TipoRutReceptor: toStringValue(values.receiverDocType ?? ""),
    RUTRecep: toStringValue(values.receiverDocNumber ?? ""),
    RznSocRecep: toStringValue(values.receiverSocialName ?? ""),
  });

  const camposG = cleanObject({
    MotTras: toStringValue(values.gMotTras ?? ""),
    ModTras: toStringValue(values.gModTras ?? ""),
    ObsTras: toStringValue(values.gObsTras ?? ""),
    IdentifTraslado: toStringValue(values.gIdentifTraslado ?? ""),
    DescTraslado: toStringValue(values.gDescTraslado ?? ""),
    PesoBrutoTotItemsSel: toStringValue(values.gPesoBrutoTotItemsSel ?? ""),
    TotalPesoTraslado: toStringValue(values.gTotalPesoTraslado ?? ""),
    UnmdTotalPeso: toStringValue(values.gUnmdTotalPeso ?? ""),
    NroBultos: toStringValue(values.gNroBultos ?? ""),
    SustentoDifPeso: toStringValue(values.gSustentoDifPeso ?? ""),
    PlacaVehicPrinc: toStringValue(values.gPlacaVehicPrinc ?? ""),
    CertHabVehicularPrinc: toStringValue(values.gCertHabVehicularPrinc ?? ""),
    DirParUbiGeo: toStringValue(values.gDirParUbiGeo ?? ""),
    DirParDireccion: toStringValue(values.gDirParDireccion ?? ""),
    RucPuntoPartida: toStringValue(values.gRucPuntoPartida ?? ""),
    CodigoLocalPartida: toStringValue(values.gCodigoLocalPartida ?? ""),
    DirLlegUbiGeo: toStringValue(values.gDirLlegUbiGeo ?? ""),
    DirLlegDireccion: toStringValue(values.gDirLlegDireccion ?? ""),
    RucPuntoLlegada: toStringValue(values.gRucPuntoLlegada ?? ""),
    CodigoLocalLlegada: toStringValue(values.gCodigoLocalLlegada ?? ""),
    RucProveedor: toStringValue(values.gRucProveedor ?? ""),
    TipoRucProveedor: toStringValue(values.gTipoRucProveedor ?? ""),
    RazonProveedor: toStringValue(values.gRazonProveedor ?? ""),
    RucComprador: toStringValue(values.gRucComprador ?? ""),
    TipoRucComprador: toStringValue(values.gTipoRucComprador ?? ""),
    RazonComprador: toStringValue(values.gRazonComprador ?? ""),
  });

  const camposG1 = cleanObject({
    FecIniTras: toStringValue(values.g1FecIniTras ?? ""),
  });

  const listaD = buildReferences(values);
  const listaE = buildAdditionalData(values);
  const listaG1 = buildCarriers(values);
  const listaG11 = buildDrivers(values);
  const listaG2 = buildAirport(values);
  const listaG3 = buildContainers(values);
  const listaG4 = buildIndicators(values);
  const listaG5 = buildSecondaryVehicles(values);

  const campos: Record<string, Record<string, any>> = {
    A: camposA as Record<string, any>,
  };

  if (Object.keys(camposG).length > 0) {
    campos.G = camposG as Record<string, any>;
  }

  if (Object.keys(camposG1).length > 0) {
    campos.G1 = camposG1 as Record<string, any>;
  }

  const listas: Record<string, Array<Record<string, any>>> = {
    B: buildItems(values),
    ...(listaD.length > 0 ? { D: listaD } : {}),
    ...(listaE.length > 0 ? { E: listaE } : {}),
    ...(listaG1.length > 0 ? { G1: listaG1 } : {}),
    ...(listaG11.length > 0 ? { G11: listaG11 } : {}),
    ...(listaG2.length > 0 ? { G2: listaG2 } : {}),
    ...(listaG3.length > 0 ? { G3: listaG3 } : {}),
    ...(listaG4.length > 0 ? { G4: listaG4 } : {}),
    ...(listaG5.length > 0 ? { G5: listaG5 } : {}),
  };

  return {
    tipoDocumento,
    secciones: {
      campos,
      listas,
    },
  };
}

export default buildShippingPayload;
