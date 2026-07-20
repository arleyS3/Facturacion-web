type Values = Record<string, any>;

function nonEmpty(value: any): boolean {
  return value != null && String(value).trim() !== "";
}

function firstNonEmpty(...values: any[]): any {
  return values.find(nonEmpty);
}

function toNumber(value: any, fallback: number): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

function toInteger(value: any): number | undefined {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? Math.trunc(parsed) : undefined;
}

function cleanObject<T extends Record<string, any>>(obj: T): Partial<T> {
  return Object.fromEntries(Object.entries(obj).filter(([, value]) => nonEmpty(value))) as Partial<T>;
}

function formatNumber(serie: string, correlativo: string): string {
  return `${serie}-${correlativo.padStart(8, "0")}`;
}

function buildDetails(values: Values) {
  const source = Array.isArray(values.shippingItems) ? values.shippingItems : [];

  return source.map((item: any) => ({
    codigo_producto: item.codigoProducto || item.codigo || undefined,
    descripcion: firstNonEmpty(item.NmbItem, item.descripcion, ""),
    cantidad: toNumber(firstNonEmpty(item.QtyItem, item.cantidad), 0),
    valor_unitario: 0.01,
    igv: 0,
    codigo_tipoIgv: "30",
    unidad_medida: firstNonEmpty(item.UnmdItem, item.unidadMedida, "NIU"),
  }));
}

export function buildShippingCanonicalPayload(values: Values) {
  const serie = String(values.serie || "T001");
  const correlativo = String(values.correlativo || "");
  const carrier = Array.isArray(values.shippingCarriers) ? values.shippingCarriers[0] : undefined;
  const driver = Array.isArray(values.shippingDrivers) ? values.shippingDrivers[0] : undefined;
  const airport = values.shippingAirport && typeof values.shippingAirport === "object" ? values.shippingAirport : undefined;
  const container = Array.isArray(values.shippingContainers) ? values.shippingContainers[0] : undefined;
  const reference = Array.isArray(values.shippingReferences) ? values.shippingReferences[0] : undefined;

  const indicators = Array.isArray(values.shippingIndicators)
    ? values.shippingIndicators.map((i: any) => i.indicador ?? i.IndicadorTraslado).filter(nonEmpty)
    : [];

  const docRefId = reference
    ? reference.SerieRef && reference.FolioRef
      ? `${reference.SerieRef}-${reference.FolioRef}`
      : firstNonEmpty(reference.FolioRef, reference.NroLinRef, "")
    : undefined;

  return {
    tipo_documento: "09",
    numero: formatNumber(serie, correlativo),
    fecha_emision: values.fechaEmision || "",
    hora_emision: values.horaEmision || undefined,
    moneda: "PEN",
    emisor_ruc: firstNonEmpty(values.issuerDocNumber, values.emisorRuc, values.numeroDocEmisor, ""),
    emisor_razonSocial: firstNonEmpty(values.issuerSocialName, values.emisorRazonSocial, values.razonSocialEmisor, ""),
    emisor_nombreComercial: firstNonEmpty(values.issuerCommercialName, values.emisorNombreComercial),
    emisor_direccion: firstNonEmpty(values.issuerAddress, values.emisorDireccion, ""),
    emisor_codigoDomicilio: firstNonEmpty(values.localCode, values.issuerAnexo, values.emisorCodigoDomicilio, values.anexoEmisor, "0000"),
    receptor_documento: firstNonEmpty(values.receiverDocNumber, values.gRucComprador, values.gRucProveedor, ""),
    receptor_razonSocial: firstNonEmpty(values.receiverSocialName, values.gRazonComprador, values.gRazonProveedor, ""),
    detalles: buildDetails(values),
    parte_traslado: cleanObject({
      tipo_traslado: values.gMotTras,
      modalidad_traslado: firstNonEmpty(values.gModTras, carrier?.modalidad),
      punto_partida_ubigeo: values.gDirParUbiGeo,
      punto_partida_direccion: values.gDirParDireccion,
      punto_llegada_ubigeo: values.gDirLlegUbiGeo,
      punto_llegada_direccion: values.gDirLlegDireccion,
      transportista_nro_documento: carrier?.ruc ?? carrier?.RucTranspor,
      transportista_razonSocial: carrier?.razonSocial ?? carrier?.RazoTrans,
      transportista_tipo_documento: carrier?.tipoRuc ?? carrier?.TipoRucTrans,
      placa_vehiculo: firstNonEmpty(values.gPlacaVehicPrinc, carrier?.PlacaVehicPrinc),
      conductor_nro_documento: driver?.numeroDocumento ?? driver?.ConductorNroDocId,
      conductor_tipo_documento: driver?.codTipoDocumento ?? driver?.ConductorTipoDocId,
      conductor_nombres: driver?.nombres ?? driver?.NombConductor,
      conductor_apellidos: driver?.apellidos ?? driver?.ApellConductor,
      conductor_licencia: driver?.nroLicencia ?? driver?.NumLicenciaCond,
      peso_bruto_total: toNumber(firstNonEmpty(values.gTotalPesoTraslado, values.gPesoBrutoTotItemsSel), 0),
      und_peso_total: values.gUnmdTotalPeso || "KGM",
      numero_bultos: toInteger(values.gNroBultos),
      fecha_traslado: firstNonEmpty(values.g1FecIniTras, carrier?.fechaInicio),
      puerto_codigo: airport?.codigoPuerto ?? airport?.CodPueAer,
      puerto_nombre: airport?.nombrePuerto,
      contenedor_id: container?.idContenedor ?? container?.IdContenedor,
      precinto_id: container?.nroPrecinto ?? container?.NumPrecinto,
      doc_ref_transporte_id: docRefId,
      doc_ref_transporte_tipo: reference?.TpoDocRef,
      doc_ref_transporte_emisor: reference?.RucEmisDocRef,
      indicadores: indicators.length > 0 ? indicators : undefined,
    }),
    firmar: values.firmar ?? true,
  };
}

export default buildShippingCanonicalPayload;
