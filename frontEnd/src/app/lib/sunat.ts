import api from "./api";
import type { MaybeRucResponse, RucResponse } from "./types/sunat";

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
