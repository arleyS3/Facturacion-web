import axios from "axios";

const viteEnv = (import.meta as { env?: Record<string, string | undefined> }).env;

// Prioridad: env var de Vite → fallback a Railway directo
const API_BASE_URL = viteEnv?.VITE_API_BASE_URL || "https://facturacion-web.up.railway.app/api/v1";
export const REFRESH_TOKEN_KEY = "refresh_token";

/**
 * Token JWT en memoria (no localStorage / evitar XSS persistente).
 * Se pierde al recargar la página, pero la cookie httpOnly es el fallback.
 */
let accessToken: string | null = null;

export const setAccessToken = (token: string | null) => {
  accessToken = token;
};

export const getAccessToken = () => accessToken;

/**
 * Cliente Axios compartido para llamadas al backend.
 * - withCredentials: true → envía cookies httpOnly como fallback
 * - Interceptor: agrega Authorization: Bearer <token> desde memoria
 * - Si no hay token en memoria, la cookie httpOnly es el respaldo
 */
export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  withCredentials: true,
});

// Interceptor: agrega el token Bearer si existe en memoria
api.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

export default api;
