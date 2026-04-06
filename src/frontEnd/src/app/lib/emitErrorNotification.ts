import axios from "axios";
import { sileo } from "sileo";

interface ValidationResponse {
  message?: string;
  details?: unknown;
  timestamp?: string;
}

function parseValidationDetails(details: unknown): string[] {
  if (!Array.isArray(details)) return [];
  return details.filter((item): item is string => typeof item === "string" && item.trim().length > 0);
}

export function hasServerValidationErrors(data: unknown): boolean {
  if (!data || typeof data !== "object") return false;

  const payload = data as ValidationResponse;
  const details = parseValidationDetails(payload.details);

  return details.length > 0 && typeof payload.message === "string";
}

export function showServerValidationErrors(data: unknown): boolean {
  if (!hasServerValidationErrors(data)) return false;

  const payload = data as ValidationResponse;
  const details = parseValidationDetails(payload.details);

  sileo.error({
    title: payload.message || "Validación falló",
    description: details.join("\n"),
  });

  return true;
}

export function notifyRequestError(error: unknown, fallbackTitle: string) {
  if (axios.isAxiosError(error)) {
    const responseData = error.response?.data;
    if (showServerValidationErrors(responseData)) return;

    const serverMessage =
      typeof responseData?.message === "string" && responseData.message.trim().length > 0
        ? responseData.message
        : "No se pudo completar la operación. Intente nuevamente.";

    sileo.error({
      title: fallbackTitle,
      description: serverMessage,
    });
    return;
  }

  sileo.error({
    title: fallbackTitle,
    description: "Ocurrió un error inesperado. Intente nuevamente.",
  });
}
