import axios from "axios";

const viteEnv = (import.meta as { env?: Record<string, string | undefined> }).env;
const API_BASE_URL =
  viteEnv?.VITE_API_BASE_URL || "http://localhost:8080/api/v1";

/**
 * Cliente Axios compartido para llamadas al backend.
 * Configura la baseURL usando la variable VITE_API_BASE_URL o un fallback.
 */
export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

export default api;
