import api from "./api";
import type { MaybeRucResponse, RucResponse } from "./types/sunat";

/**
 * Consulta información de RUC vía el endpoint del backend.
 *
 * @param ruc número de RUC a consultar
 * @returns objeto RucResponse o null si no existe o hay error
 */
export async function fetchRuc(ruc: string): Promise<MaybeRucResponse> {
  if (!ruc) return null;
  try {
    const res = await api.get<RucResponse>("/sunat", {
      params: { ruc },
      headers: {
        Accept: "application/json",
      },
    });
    if (res.status === 200) return res.data;
    return null;
  } catch (err) {
    console.error("fetchRuc error", err);
    return null;
  }
}

export default fetchRuc;
