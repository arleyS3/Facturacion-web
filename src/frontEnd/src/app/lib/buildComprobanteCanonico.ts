/**
 * Mapeador para generar el payload del comprobante electrónico.
 * Convierte los datos del formulario al formato que espera el backend.
 * 
 * @module lib/buildComprobanteCanonico
 */

import type { ComprobanteFormData, DetalleFormData } from "./schemas/comprobante.schema";

/**
 * Interface para el payload del comprobante canónico.
 * Usa nombres de campo en español (snake_case) para concordar con el backend.
 * 
 * @typedef {Object} ComprobanteCanonicoPayload
 * @property {string} tipo_documento - Código SUNAT del tipo de documento (01=Factura, 03=Boleta, etc.)
 * @property {string} numero - Número del documento en formato serie-correlativo (ej: F001-00000001)
 * @property {string} fecha_emision - Fecha de emisión en formato YYYY-MM-DD
 * @property {string} [hora_emision] - Hora de emisión en formato HH:mm:ss
 * @property {string} [fecha_vencimiento] - Fecha de vencimiento en formato YYYY-MM-DD
 * @property {string} tipo_de_operacion - Código del tipo de operación según catálogo SUNAT
 * @property {string} moneda - Código ISO de la moneda (PEN=soles, USD=dólares)
 * @property {string} emisor_ruc - RUC del emisor (11 dígitos)
 * @property {string} emisor_razonSocial - Razón social del emisor
 * @property {string} [emisor_nombreComercial] - Nombre comercial del emisor
 * @property {string} emisor_direccion - Dirección del emisor
 * @property {string} [emisor_urbanizacion] - Urbanización/Sector
 * @property {string} [emisor_departamento] - Departamento del ubigeo
 * @property {string} [emisor_provincia] - Provincia del ubigeo
 * @property {string} [emisor_distrito] - Distrito del ubigeo
 * @property {string} [emisor_ubigeo] - Código ubigeo de la ubicación
 * @property {string} emisor_codigoDomicilio - Código del domicilio fiscal
 * @property {string} receptor_documento - Número de documento del receptor
 * @property {string} receptor_razonSocial - Razón social o nombre del receptor
 * @property {string} [receptor_direccion] - Dirección del receptor
 * @property {string} [receptor_ubigeo] - Código ubigeo del receptor
 * @property {DetallePayload[]} detalles - Lista de items del comprobante
 * @property {LeyendaPayload[]} [leyendas] - Lista de leyendas adicionales
 * @property {DocumentoRelacionadoPayload} [documento_relacionado] - Datos del documento relacionado (para notas)
 */
interface ComprobanteCanonicoPayload {
  // Documento
  tipo_documento: string;
  numero: string;
  fecha_emision: string;
  hora_emision?: string;
  fecha_vencimiento?: string;
  tipo_de_operacion: string;
  moneda: string;
  
  // Emisor
  emisor_ruc: string;
  emisor_razonSocial: string;
  emisor_nombreComercial?: string;
  emisor_direccion: string;
  emisor_urbanizacion?: string;
  emisor_departamento?: string;
  emisor_provincia?: string;
  emisor_distrito?: string;
  emisor_ubigeo?: string;
  emisor_codigoDomicilio: string;
  
  // Receptor
  receptor_documento: string;
  receptor_razonSocial: string;
  receptor_direccion?: string;
  receptor_ubigeo?: string;
  
  // Detalles
  detalles: DetallePayload[];
  
  // Adicionales
  leyendas?: LeyendaPayload[];
  documento_relacionado?: DocumentoRelacionadoPayload;
  
  /** Indica si se debe firmar digitalmente el XML */
  firmar?: boolean;
}

/**
 * Interface para un detalle (ítem) del comprobante.
 * 
 * @typedef {Object} DetallePayload
 * @property {string} [codigo_producto] - Código del producto
 * @property {string} descripcion - Descripción del producto/servicio
 * @property {number} cantidad - Cantidad del item
 * @property {number} valor_unitario - Valor unitario sin IGV
 * @property {number} igv - Monto del IGV calculado
 * @property {string} codigo_tipoIgv - Código de tipo de afectación IGV (10=Gravado, 20=Exonerado, 30/31=Inafecto)
 * @property {string} [unidad_medida] - Código de unidad de medida (catálogo SUNAT, ej: NIU, KG, ZZ)
 */
interface DetallePayload {
  codigo_producto?: string;
  descripcion: string;
  cantidad: number;
  valor_unitario: number;
  igv: number;
  codigo_tipoIgv: string;
  unidad_medida?: string;
}

/**
 * Interface para una leyenda del comprobante.
 * 
 * @typedef {Object} LeyendaPayload
 * @property {string} codigo_local - Código de la leyenda según catálogo 52 SUNAT
 * @property {string} leyenda - Texto de la leyenda
 */
interface LeyendaPayload {
  codigo_local: string;
  leyenda: string;
}

/**
 * Interface para documento relacionado (notas de crédito/débito).
 * 
 * @typedef {Object} DocumentoRelacionadoPayload
 * @property {string} tipo_documento - Tipo de documento que se modifica
 * @property {string} numero_documento - Número del documento que se modifica
 * @property {string} [codigo_motivo] - Código del motivo de la nota
 */
interface DocumentoRelacionadoPayload {
  tipo_documento: string;
  numero_documento: string;
  codigo_motivo?: string;
}

/**
 * Convierte el tipo de documento de la UI (label) al código SUNAT.
 * 
 * @param tipo - Tipo de documento en texto (ej: "Factura", "Boleta")
 * @returns Código SUNAT de 2 dígitos
 */
function convertirTipoDocumento(tipo: string): string {
  /** Mapa de tipos de documento a códigos SUNAT */
  const mapa: Record<string, string> = {
    "Factura": "01",
    "Boleta": "03",
    "Nota de Crédito": "07",
    "Nota de Débito": "08",
    "Guía de Remisión": "09",
    "09": "09",
    "01": "01",
    "03": "03",
    "07": "07",
    "08": "08",
  };
  return mapa[tipo] || "01";
}

/**
 * Convierte el tipo de afectación IGV del formulario al código SUNAT.
 * 
 * @param codigo - Código del tipo de afectación IGV
 * @returns Código SUNAT para el tipo de afectación
 */
function convertirTipoAfectacionIgv(codigo: string): string {
  /** Códigos de afectación IGV según catálogo 07 SUNAT */
  const mapa: Record<string, string> = {
    "10": "10", // Gravado - Operación onerosa
    "20": "20", // Exonerado - Operación onerosa
    "30": "30", // Inafecto - Operación onerosa
    "31": "31", // Inafecto - Operación gratuita
    "1000": "10", // Legacy gravado
    "9998": "20", // Legacy exonerado
    "9995": "30", // Legacy inafecto
  };
  return mapa[codigo] || "10";
}

/**
 * Formatea el número correlativo a formato serie-correlativo.
 * 
 * @param serie - Serie del documento (ej: "F001")
 * @param correlativo - Correlativo de 8 dígitos
 * @returns Número formateado (ej: "F001-00000001")
 */
function formatearNumeroDocumento(serie: string, correlativo: string): string {
  const correlativoFormateado = correlativo.padStart(8, "0");
  return `${serie}-${correlativoFormateado}`;
}

/**
 * Convierte un producto del formulario al formato de detalle para el backend.
 * 
 * @param producto - Datos del producto del formulario
 * @returns Objeto detalle con nombres en español
 */
function convertirDetalle(producto: DetalleFormData): DetallePayload {
  // El frontend debe enviar el IGV ya calculado
  // Si no viene calculado, lo calculamos
  const cantidad = Number(producto.cantidad) || 0;
  const precioUnitario = Number(producto.precioUnitario) || 0;
  const tipoAfectacion = convertirTipoAfectacionIgv(producto.tipoAfectacionIGV);
  
  // Calcular valor de venta (base gravable)
  const valorVenta = Number(producto.valorVenta) || (cantidad * precioUnitario);
  
  // Calcular IGV si no viene
  let igv = Number(producto.impuestoIGV) || 0;
  if (igv === 0 && tipoAfectacion === "10") {
    igv = valorVenta * 0.18;
  }
  
  return {
    /** Código interno del producto */
    codigo_producto: producto.codigo || undefined,
    /** Descripción del producto o servicio */
    descripcion: producto.descripcion || "",
    /** Cantidad unidades */
    cantidad: cantidad,
    /** Precio unitario sin IGV */
    valor_unitario: parseFloat(precioUnitario.toFixed(2)),
    /** Monto de IGV calculado */
    igv: parseFloat(igv.toFixed(2)),
    /** Código de tipo de afectación IGV (10=Gravado, 20=Exonerado, etc.) */
    codigo_tipoIgv: tipoAfectacion,
    /** Código de unidad de medida (catálogo SUNAT, ej: NIU, KG, ZZ) */
    unidad_medida: producto.unidadMedida || "NIU",
  };
}

/**
 * Construye el payload del comprobante canónico para enviar al backend.
 * Convierte los datos del formulario a nombres de campo en español (snake_case).
 * 
 * @param values - Datos del formulario (de useForm)
 * @returns Objeto payload listo para enviar al endpoint /comprobantes/generar-xml
 * 
 * @example
 * const payload = buildComprobanteCanonico(formValues);
 * // {
 * //   tipo_documento: "01",
 * //   numero: "F001-00000001",
 * //   emisor_ruc: "20123456789",
 * //   ...
 * // }
 */
export function buildComprobanteCanonico(values: ComprobanteFormData): ComprobanteCanonicoPayload {
  // Convertir detalles (productos)
  const detalles = (values.detalles || []).map(convertirDetalle);
  
  // Calcular subtotales por tipo de operación IGV
  const totales = detalles.reduce(
    (acc, d) => {
      switch (d.codigo_tipoIgv) {
        case "10": // Gravado
          acc.gravado += d.valor_unitario * d.cantidad;
          acc.igvTotal += d.igv;
          break;
        case "20": // Exonerado
          acc.exonerado += d.valor_unitario * d.cantidad;
          break;
        case "30":
        case "31": // Inafecto
          acc.inafecto += d.valor_unitario * d.cantidad;
          break;
      }
      return acc;
    },
    { gravado: 0, exonerado: 0, inafecto: 0, igvTotal: 0 }
  );

  // Construir el payload para el backend con nombres en español
  const canonico: ComprobanteCanonicoPayload = {
    // ========================================
    // DATOS DEL DOCUMENTO
    // ========================================
    
    /** Código SUNAT del tipo de documento (01=Factura, 03=Boleta, etc.) */
    tipo_documento: convertirTipoDocumento(values.tipoDocumento || values.docType || "01"),
    
    /** Número del documento en formato serie-correlativo */
    numero: formatearNumeroDocumento(values.serie || "F001", values.correlativo || "00000000"),
    
    /** Fecha de emisión en formato YYYY-MM-DD */
    fecha_emision: values.fechaEmision || new Date().toISOString().split("T")[0],
    
    /** Hora de emisión en formato HH:mm:ss */
    hora_emision: values.horaEmision || new Date().toTimeString().slice(0, 8),
    
    /** Fecha de vencimiento (opcional) */
    fecha_vencimiento: values.fechaVencimiento || undefined,
    
    /** Código del tipo de operación (catalogo 17) */
    tipo_de_operacion: values.tipoOperacion || "0101",
    
    /** Código ISO de la moneda */
    moneda: values.moneda || "PEN",
    
    // ========================================
    // DATOS DEL EMISOR
    // ========================================
    
    /** RUC del emisor (11 dígitos) */
    emisor_ruc: values.emisorRuc || values.issuerDocNumber || "",
    
    /** Razón social del emisor */
    emisor_razonSocial: values.emisorRazonSocial || values.issuerSocialName || "",
    
    /** Nombre comercial del emisor (opcional) */
    emisor_nombreComercial: values.emisorNombreComercial || values.issuerCommercialName || undefined,
    
    /** Dirección del emisor */
    emisor_direccion: values.emisorDireccion || values.issuerAddress || "",
    
    /** Urbanización/Sector (opcional) */
    emisor_urbanizacion: values.emisorUrbanizacion || undefined,
    
    /** Departamento del ubigeo (opcional) */
    emisor_departamento: values.emisorDepartamento || undefined,
    
    /** Provincia del ubigeo (opcional) */
    emisor_provincia: values.emisorProvincia || undefined,
    
    /** Distrito del ubigeo (opcional) */
    emisor_distrito: values.emisorDistrito || undefined,
    
    /** Código ubigeo de la ubicación */
    emisor_ubigeo: values.emisorUbigeo || undefined,
    
    /** Código del domicilio fiscal */
    emisor_codigoDomicilio: values.emisorCodigoDomicilio || values.emisorAnexo || "0001",
    
    // ========================================
    // DATOS DEL RECEPTOR
    // ========================================
    
    /** Número de documento del receptor */
    receptor_documento: values.receptorDocumento || values.receiverDocNumber || "",
    
    /** Razón social o nombre del receptor */
    receptor_razonSocial: values.receptorRazonSocial || values.receiverSocialName || "",
    
    /** Dirección del receptor (opcional) */
    receptor_direccion: values.receptorDireccion || values.receiverAddress || undefined,
    
    /** Código ubigeo del receptor (opcional) */
    receptor_ubigeo: values.receptorUbigeo || undefined,
    
    // ========================================
    // DETALLES / PRODUCTOS
    // ========================================
    
    /** Lista de items del comprobante */
    detalles: detalles,
  };

  // Agregar leyendas si existen
  if (values.leyendas && values.leyendas.length > 0) {
    /** Lista de leyendas adicionales */
    canonico.leyendas = values.leyendas.map((l) => ({
      /** Código de la leyenda según catálogo 52 */
      codigo_local: l.codigoLocal,
      /** Texto de la leyenda */
      leyenda: l.leyenda,
    }));
  }

  // Agregar documento relacionado si existe (para notas de crédito/débito)
  if (values.documentoRelacionado) {
    /** Datos del documento que se modifica */
    canonico.documento_relacionado = {
      /** Tipo de documento que se modifica */
      tipo_documento: convertirTipoDocumento(values.documentoRelacionado.tipoDocumento),
      /** Número del documento que se modifica */
      numero_documento: values.documentoRelacionado.numeroDocumento,
      /** Código del motivo de la nota */
      codigo_motivo: values.documentoRelacionado.codigoMotivo,
    };
  }

  // Indicar que no se firme el XML (la firma se implementará después)
  canonico.firmar = false;

  return canonico;
}

export default buildComprobanteCanonico;

// Exportar tipos para uso en otros archivos
export type { 
  ComprobanteCanonicoPayload, 
  DetallePayload, 
  LeyendaPayload, 
  DocumentoRelacionadoPayload 
};