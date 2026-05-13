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
}

/**
 * Genera el XML UBL 2.1 del comprobante electrónico.
 *
 * @param formData - Los datos del formulario
 * @returns El XML generado como string
 */
export async function generarXml(
  formData: ComprobanteFormData,
): Promise<string> {
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

  return response.data.xml!;
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

/**
 * Genera y descarga el XML del comprobante.
 *
 * @param formData - Los datos del formulario
 * @returns El nombre del archivo descargado
 */
export async function generarYDescargarXml(
  formData: ComprobanteFormData,
): Promise<string> {
  const xml = await generarXml(formData);

  // Generar nombre de archivo basado en el número de documento
  const numero = formData.correlativo?.padStart(8, "0") || "00000000";
  const serie = formData.serie || "F001";
  const tipoDoc = formData.tipoDocumento || "01";

  // Mapear código a prefijo de archivo
  const prefijos: Record<string, string> = {
    "01": "F", // Factura
    "03": "B", // Boleta
    "07": "NC", // Nota de Crédito
    "08": "ND", // Nota de Débito
    "09": "T", // Guía de Remisión
  };

  const prefijo = prefijos[tipoDoc] || "F";
  const nombreArchivo = `${prefijo}${serie}-${numero}.xml`;

  descargarXml(xml, nombreArchivo);

  return nombreArchivo;
}

/**
 * Obtiene el nombre de archivo sugerido para el XML.
 */
export function obtenerNombreArchivoXml(formData: ComprobanteFormData): string {
  const numero = formData.correlativo?.padStart(8, "0") || "00000000";
  const serie = formData.serie || "F001";
  const tipoDoc = formData.tipoDocumento || "01";

  const prefijos: Record<string, string> = {
    "01": "F",
    "03": "B",
    "07": "NC",
    "08": "ND",
    "09": "T",
  };

  const prefijo = prefijos[tipoDoc] || "F";
  return `${prefijo}${serie}-${numero}.xml`;
}

export default {
  generarXml,
  descargarXml,
  generarYDescargarXml,
  obtenerNombreArchivoXml,
};
