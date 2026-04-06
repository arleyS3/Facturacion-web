import { useEffect, useState } from "react";
import { api } from "../lib/api";

export type CatalogItem = { code: string; label: string; extra?: string };

/**
 * Hook simple para obtener un catálogo desde el backend.
 * Devuelve { data, loading, error } y evita peticiones cuando los parámetros
 * están incompletos.
 *
 * @param path ruta para la llamada al API (por ejemplo '/catalogos/monedas')
 */
export function useCatalog(path: string) {
  const [data, setData] = useState<CatalogItem[] | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    // Avoid firing requests when query params are missing
    if (!path || path.includes("departamento=&") || path.includes("provincia=") && path.includes("provincia=") && path.endsWith("provincia=") || path.endsWith("departamento=")) {
      setData(null);
      setLoading(false);
      setError(null);
      return;
    }
    let mounted = true;
    setLoading(true);
    setError(null);
    api
      .get<CatalogItem[]>(path)
      .then((res) => {
        if (!mounted) return;
        setData(res.data);
      })
      .catch((err) => {
        if (!mounted) return;
        setError(err);
      })
      .finally(() => {
        if (!mounted) return;
        setLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, [path]);

  return { data, loading, error } as const;
}
