import { api } from "./api";
import buildComprobanteCanonico from "./buildComprobanteCanonico";
import type { ComprobanteFormData } from "./schemas/comprobante.schema";

/**
 * Respuesta del backend al generar XML.
 */
interface GenerarXmlResponse {
  success: boolean;
  tipoDocumento?: string;
  xml?: string;
  error?: string;
  validationErrors?: string[];
}

export interface GenerarXmlResult {
  xml: string;
  validationErrors: string[];
}

/**
 * Genera el XML UBL 2.1 del comprobante electrónico.
 *
 * @param formData - Los datos del formulario
 * @returns Objeto con el XML generado y errores de validación (si los hay)
 */
export async function generarXml(
  formData: ComprobanteFormData,
): Promise<GenerarXmlResult> {
  const canonico = buildComprobanteCanonico(formData);

  console.log(
    "[xmlService] Generando XML con payload:",
    JSON.stringify(canonico, null, 2),
  );

  const response = await api.post<GenerarXmlResponse>(
    "/comprobantes/generar-xml",
    canonico,
  );

  if (!response.data.success) {
    throw new Error(response.data.error || "Error al generar XML");
  }

  return {
    xml: response.data.xml!,
    validationErrors: response.data.validationErrors ?? [],
  };
}

/**
 * Descarga el XML como archivo.
 *
 * @param xml - Contenido del XML
 * @param nombreArchivo - Nombre del archivo (ej: "F001-00000001.xml")
 */
export function descargarXml(xml: string, nombreArchivo: string): void {
  const blob = new Blob([xml], { type: "application/xml;charset=utf-8" });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = nombreArchivo;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

export interface GenerarYDescargarXmlResult {
  filename: string;
  validationErrors: string[];
}

/**
 * Genera y descarga el XML del comprobante.
 *
 * @param formData - Los datos del formulario
 * @returns Objeto con el nombre del archivo descargado y errores de validación
 */
export async function generarYDescargarXml(
  formData: ComprobanteFormData,
): Promise<GenerarYDescargarXmlResult> {
  const { xml, validationErrors } = await generarXml(formData);

  const nombreArchivo = obtenerNombreArchivoXml(formData);

  descargarXml(xml, nombreArchivo);

  return { filename: nombreArchivo, validationErrors };
}

/**
 * Obtiene el nombre de archivo sugerido para el XML.
 */
export function obtenerNombreArchivoXml(formData: ComprobanteFormData): string {
  const ruc = sanitizeFilenamePart(formData.emisorRuc || formData.issuerDocNumber, "sin-ruc");
  const tipoDoc = sanitizeFilenamePart(toSunatDocumentCode(formData.tipoDocumento || "01"), "01");
  const serie = sanitizeFilenamePart(formData.serie || "F001", "F001");
  const correlativo = sanitizeFilenamePart(
    formData.correlativo?.padStart(8, "0") || "00000000",
    "00000000",
  );

  return `${ruc}-${tipoDoc}-${serie}-${correlativo}.xml`;
}

function toSunatDocumentCode(documentType: string): string {
  const codesByLabel: Record<string, string> = {
    Factura: "01",
    Boleta: "03",
    "Nota de Crédito": "07",
    "Nota de Débito": "08",
    "Guía de Remisión": "09",
    "01": "01",
    "03": "03",
    "07": "07",
    "08": "08",
    "09": "09",
  };

  return codesByLabel[documentType] || "01";
}

function sanitizeFilenamePart(value: unknown, fallback: string): string {
  if (typeof value !== "string" || !value.trim()) return fallback;
  return value.trim().replace(/[^A-Za-z0-9_-]/g, "-");
}

export default {
  generarXml,
  descargarXml,
  generarYDescargarXml,
  obtenerNombreArchivoXml,
};
