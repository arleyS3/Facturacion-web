import axios from "axios";

const viteEnv = (import.meta as { env?: Record<string, string | undefined> }).env;
const API_BASE_URL = viteEnv?.VITE_API_BASE_URL || "/api/v1";

/**
 * Cliente Axios compartido para llamadas al backend.
 * - withCredentials: true → envía cookies httpOnly automáticamente
 * - El token JWT ya no se almacena en localStorage (mitigación XSS)
 * - Axios lee la cookie XSRF-TOKEN y la envía en el header X-XSRF-TOKEN (CSRF)
 */
export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  withCredentials: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
});

export default api;
