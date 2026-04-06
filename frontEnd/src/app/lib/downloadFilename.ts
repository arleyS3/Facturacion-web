type FilenameFallbacks = {
  tipoDoc?: string;
  serie?: string;
};

export const getFilenameFromDisposition = (
  disposition?: string,
  fallback = "trama.txt",
) => {
  if (!disposition) return fallback;
  const match = disposition.match(/filename\*?=(?:UTF-8'')?\"?([^\";]+)\"?/i);
  return match?.[1] ? decodeURIComponent(match[1]) : fallback;
};

const sanitizeFilenamePart = (value: unknown, fallback: string) => {
  if (typeof value !== "string" || !value.trim()) return fallback;
  return value.trim().replace(/[^A-Za-z0-9_-]/g, "-");
};

export const buildFilenameFromPayload = (
  payload: any,
  fallbacks: FilenameFallbacks = {},
) => {
  const a = payload?.secciones?.campos?.A ?? {};
  const ruc = sanitizeFilenamePart(a?.RUTEmis, "sin-ruc");
  const tipoDoc = sanitizeFilenamePart(
    payload?.tipoDocumento,
    fallbacks.tipoDoc ?? "00",
  );
  const serie = sanitizeFilenamePart(a?.Serie, fallbacks.serie ?? "S000");
  const correlativo = sanitizeFilenamePart(a?.Correlativo, "00000000");
  return `${ruc}-${tipoDoc}-${serie}-${correlativo}.txt`;
};

/**
 * Extrae nombre del header Content-Disposition o devuelve fallback.
 * (ya existe getFilenameFromDisposition que hace el trabajo en este archivo)
 */
