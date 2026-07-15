import axios from "axios";

const viteEnv = (import.meta as { env?: Record<string, string | boolean | undefined> }).env;

// Producción usa el rewrite same-origin de Vercel; desarrollo puede sobreescribir por env.
const API_BASE_URL = viteEnv?.PROD ? "/api/v1" : viteEnv?.VITE_API_BASE_URL || "/api/v1";
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
