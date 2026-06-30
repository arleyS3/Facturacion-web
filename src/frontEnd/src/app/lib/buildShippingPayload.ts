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

function firstNonEmpty(...values: any[]): any {
  return values.find(nonEmpty);
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
    NroLinDet: toStringValue(firstNonEmpty(item.NroLinDet, item.correlativo, index + 1)),
    QtyItem: toStringValue(firstNonEmpty(item.QtyItem, item.cantidad, "0")),
    UnmdItem: toStringValue(firstNonEmpty(item.UnmdItem, item.unidadMedida, "NIU")),
    NmbItem: toStringValue(firstNonEmpty(item.NmbItem, item.descripcion, "")),
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

function buildAirport(values: Values): Record<string, string> {
  const airport = values.shippingAirport;

  if (!airport || typeof airport !== "object") {
    return {};
  }

  return cleanObject({
    CodPueAer: toStringValue(firstNonEmpty(airport.codigoPuerto, airport.CodPueAer, "")),
  }) as Record<string, string>;
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

/**
 * Construye el payload para guías de remisión compatible con el backend.
 *
 * @param values objeto con campos del formulario de guía
 * @returns payload con { tipoDocumento, secciones }
 */
export function buildShippingPayload(values: Values) {
  const tipoDocumento = toStringValue(values.docType, "09");

  const camposA = cleanObject({
    CODI_EMPR: toStringValue(firstNonEmpty(values.companyCode, values.codigoEmpresa, "1")),
    Serie: toStringValue(values.serie ?? "T001"),
    Correlativo: toStringValue(values.correlativo ?? ""),
    FchEmis: toStringValue(values.fechaEmision ?? ""),
    HoraEmision: toStringValue(values.horaEmision ?? ""),
    TipoRucEmis: toStringValue(firstNonEmpty(values.issuerDocType, values.emisorTipoDoc, values.tipoDocEmisor, "6")),
    RUTEmis: toStringValue(firstNonEmpty(values.issuerDocNumber, values.emisorRuc, values.numeroDocEmisor, "")),
    RznSocEmis: toStringValue(firstNonEmpty(values.issuerSocialName, values.emisorRazonSocial, values.razonSocialEmisor, "")),
    CodigoLocalAnexo: toStringValue(firstNonEmpty(values.localCode, values.issuerAnexo, values.emisorCodigoDomicilio, values.anexoEmisor, "0000")),
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
    RucProv: toStringValue(values.gRucProveedor ?? ""),
    TipoRucProv: toStringValue(values.gTipoRucProveedor ?? ""),
    RznSocProv: toStringValue(values.gRazonProveedor ?? ""),
    RucComp: toStringValue(values.gRucComprador ?? ""),
    TipoRucComp: toStringValue(values.gTipoRucComprador ?? ""),
    RznSocComp: toStringValue(values.gRazonComprador ?? ""),
  });

  const camposG1 = cleanObject({
    FecIniTras: toStringValue(values.g1FecIniTras ?? ""),
  });

  const listaD = buildReferences(values);
  const listaE = buildAdditionalData(values);
  const listaG1 = buildCarriers(values);
  const listaG11 = buildDrivers(values);
  const camposG2 = buildAirport(values);
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

  if (Object.keys(camposG2).length > 0) {
    campos.G2 = camposG2 as Record<string, any>;
  }

  const listas: Record<string, Array<Record<string, any>>> = {
    B: buildItems(values),
    ...(listaD.length > 0 ? { D: listaD } : {}),
    ...(listaE.length > 0 ? { E: listaE } : {}),
    ...(listaG1.length > 0 ? { G1: listaG1 } : {}),
    ...(listaG11.length > 0 ? { G11: listaG11 } : {}),
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
