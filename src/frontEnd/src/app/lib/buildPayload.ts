import {
  OPTIONAL_CAMPO_SECTIONS,
  OPTIONAL_LISTA_SECTIONS,
  type OptionalCampoSection,
  type OptionalListaSection,
} from "./payloadSections";

/**
 * Convierte etiquetas legibles de tipo de documento a código SUNAT.
 *
 * @param label etiqueta (por ejemplo "Factura")
 * @returns código SUNAT (ej. "01") o undefined
 */
export function docTypeLabelToCode(label?: string) {
  if (!label) return undefined;
  const l = label.toString().toLowerCase();
  if (l.includes("factura")) return "01";
  if (l.includes("boleta")) return "03";
  if (l.includes("nota de crédito") || l.includes("nota credito")) return "07";
  if (l.includes("nota de débito") || l.includes("nota debito")) return "08";
  return undefined;
}

type Values = Record<string, any>;

type Totals = {
  mntNeto: number;
  mntExe: number;
  mntExo: number;
};

type NormalizedProduct = {
  NroLinDet: string;
  QtyItem: string;
  UnmdItem: string;
  NmbItem: string;
  MontoItem: string;
  CodigoTipoIgv: string;
  TasaIgv: string;
  ImpuestoIgv: string;
  _montoItemNum: number;
  _codigoTipoIgv: string;
};

class NumberFormatter {
  static toNumber(value: any): number {
    const n = Number(value);
    return Number.isFinite(n) ? n : 0;
  }

  static toFixed2(value: number): string {
    return value.toFixed(2);
  }
}

class DocumentTypeResolver {
  resolve(values: Values): string {
    return values.docTypeCode || docTypeLabelToCode(values.docType) || "01";
  }
}

class ProductsSectionBuilder {
  build(values: Values): { items: Record<string, any>[]; totals: Totals } {
    const productsSource = values.products || values.items || [];

    const normalizedProducts: NormalizedProduct[] = productsSource.map((p: any, i: number) => {
      const montoItemNum = Math.max(
        0,
        (p.valorVenta != null
          ? NumberFormatter.toNumber(p.valorVenta)
          : NumberFormatter.toNumber(p.total) - NumberFormatter.toNumber(p.impuestoIGV ?? p.igv)) -
          NumberFormatter.toNumber(p.descuentoMonto),
      );

      const codigoTipoIgv = p.codigoTributo ?? p.codigoTipoIgv ?? "9998";

      return {
        NroLinDet: String(i + 1),
        QtyItem: String(p.cantidad ?? "0"),
        UnmdItem: p.unidadMedida ?? "NIU",
        NmbItem: p.descripcion ?? "",
        MontoItem: NumberFormatter.toFixed2(montoItemNum),
        CodigoTipoIgv: codigoTipoIgv,
        TasaIgv: String(p.tasaIGV ?? p.tasaIgv ?? "0"),
        ImpuestoIgv: NumberFormatter.toFixed2(NumberFormatter.toNumber(p.impuestoIGV ?? p.igv)),
        _montoItemNum: montoItemNum,
        _codigoTipoIgv: codigoTipoIgv,
      };
    });

    const totals: Totals = {
      mntNeto: normalizedProducts
        .filter((p) => p._codigoTipoIgv === "1000")
        .reduce((sum, p) => sum + p._montoItemNum, 0),
      mntExe: normalizedProducts
        .filter((p) => p._codigoTipoIgv === "9995")
        .reduce((sum, p) => sum + p._montoItemNum, 0),
      mntExo: normalizedProducts
        .filter((p) => p._codigoTipoIgv === "9998")
        .reduce((sum, p) => sum + p._montoItemNum, 0),
    };

    const items = normalizedProducts.map(({ _montoItemNum, _codigoTipoIgv, ...item }) => item);
    return { items, totals };
  }
}

class MainFieldsSectionBuilder {
  build(values: Values, tipoDocumento: string, totals: Totals): Record<string, any> {
    return {
      CODI_EMPR: values.companyCode ?? values.companyId ?? "1",
      TipoDTE: tipoDocumento,
      Serie: values.serie ?? "",
      Correlativo: values.correlativo ?? "",
      FchEmis: values.fechaEmision ?? values.emissionDate ?? "",
      HoraEmision: values.horaEmision ?? values.emissionTime ?? "",
      TipoMoneda: values.moneda ?? "PEN",
      TipoRucEmis: values.issuerDocType ?? values.emitterDocType ?? "",
      RUTEmis: values.issuerDocNumber ?? values.emitterDocNumber ?? "",
      RznSocEmis: values.issuerSocialName ?? values.emitterSocialName ?? "",
      CodigoLocalAnexo: values.localCode ?? "0000",
      TipoRutReceptor: values.receiverDocType ?? "",
      RUTRecep: values.receiverDocNumber ?? "",
      RznSocRecep: values.receiverSocialName ?? "",
      DirRecep: values.receiverAddress ?? "",
      TipoOperacion: values.tipoOperacion ?? "",
      MntNeto: NumberFormatter.toFixed2(totals.mntNeto),
      MntExe: NumberFormatter.toFixed2(totals.mntExe),
      MntExo: NumberFormatter.toFixed2(totals.mntExo),
      // Referencia Nota de Crédito/Débito
      ...(values.refTipoDocumento ? { TipoDocRef: values.refTipoDocumento } : {}),
      ...(values.refSerie        ? { SerieRef: values.refSerie }           : {}),
      ...(values.refCorrelativo  ? { CorrelativoRef: values.refCorrelativo }: {}),
      ...(values.refCodMotivo    ? { CodMotivo: values.refCodMotivo }       : {}),
      ...(values.refDescMotivo   ? { DescMotivo: values.refDescMotivo }     : {}),
    };
  }
}

class OptionalFieldsSectionBuilder {
  build(values: Values): Record<string, Record<string, any>> {
    const sections: Record<string, Record<string, any>> = {};
    for (const sectionName of OPTIONAL_CAMPO_SECTIONS) {
      const sectionData = this.getSectionData(values, sectionName);
      if (this.hasData(sectionData)) {
        sections[sectionName] = sectionData;
      }
    }
    return sections;
  }

  private getSectionData(values: Values, sectionName: OptionalCampoSection): Record<string, any> {
    const fromSecciones = values?.secciones?.campos?.[sectionName];
    if (this.isPlainObject(fromSecciones)) {
      return { ...fromSecciones };
    }

    const fromCamposKey = values?.[`campos${sectionName}`];
    if (this.isPlainObject(fromCamposKey)) {
      return { ...fromCamposKey };
    }

    const fromDirectKey = values?.[sectionName];
    if (this.isPlainObject(fromDirectKey)) {
      return { ...fromDirectKey };
    }

    return this.getPrefixedFields(values, sectionName);
  }

  private getPrefixedFields(values: Values, sectionName: OptionalCampoSection): Record<string, any> {
    const result: Record<string, any> = {};
    const sectionLower = sectionName.toLowerCase();

    Object.keys(values).forEach((key) => {
      const match = key.match(/^([a-z0-9]+)[._](.+)$/i);
      if (!match) return;

      const prefix = match[1]?.toLowerCase();
      const field = match[2];
      if (prefix !== sectionLower || !field) return;

      result[field] = values[key];
    });

    return result;
  }

  private hasData(value: Record<string, any>): boolean {
    return Object.keys(value).some((key) => value[key] != null && value[key] !== "");
  }

  private isPlainObject(value: any): value is Record<string, any> {
    return value != null && typeof value === "object" && !Array.isArray(value);
  }
}

class OptionalListSectionBuilder {
  build(values: Values): Record<string, Record<string, any>[]> {
    const sections: Record<string, Record<string, any>[]> = {};
    for (const sectionName of OPTIONAL_LISTA_SECTIONS) {
      const sectionData = this.getSectionData(values, sectionName);
      if (sectionData.length > 0) {
        sections[sectionName] = sectionData;
      }
    }
    return sections;
  }

  private getSectionData(values: Values, sectionName: OptionalListaSection): Record<string, any>[] {
    const fromSecciones = values?.secciones?.listas?.[sectionName];
    if (Array.isArray(fromSecciones)) {
      return fromSecciones.filter(this.isPlainObject);
    }

    const fromListaKey = values?.[`lista${sectionName}`] ?? values?.[`listas${sectionName}`];
    if (Array.isArray(fromListaKey)) {
      return fromListaKey.filter(this.isPlainObject);
    }

    const fromDirectKey = values?.[sectionName];
    if (Array.isArray(fromDirectKey)) {
      return fromDirectKey.filter(this.isPlainObject);
    }
    if (this.isPlainObject(fromDirectKey) && Object.keys(fromDirectKey).length > 0) {
      return [fromDirectKey];
    }

    return [];
  }

  private isPlainObject(value: any): value is Record<string, any> {
    return value != null && typeof value === "object" && !Array.isArray(value);
  }
}

class PayloadBuilder {
  private readonly documentTypeResolver = new DocumentTypeResolver();
  private readonly productsSectionBuilder = new ProductsSectionBuilder();
  private readonly mainFieldsSectionBuilder = new MainFieldsSectionBuilder();
  private readonly optionalFieldsSectionBuilder = new OptionalFieldsSectionBuilder();
  private readonly optionalListSectionBuilder = new OptionalListSectionBuilder();

  build(values: Values) {
    const tipoDocumento = this.documentTypeResolver.resolve(values);
    const { items, totals } = this.productsSectionBuilder.build(values);

    const campos: Record<string, Record<string, any>> = {
      A: this.mainFieldsSectionBuilder.build(values, tipoDocumento, totals),
      ...this.optionalFieldsSectionBuilder.build(values),
    };

    return {
      tipoDocumento,
      secciones: {
        campos,
        listas: {
          B: items,
          ...this.optionalListSectionBuilder.build(values),
        },
      },
    };
  }
}

export function buildPayload(values: any) {
  return new PayloadBuilder().build(values ?? {});
}

export default buildPayload;
