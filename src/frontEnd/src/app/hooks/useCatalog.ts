import { useQuery } from "@tanstack/react-query";
import { api } from "../lib/api";

export type CatalogItem = { code: string; label: string; extra?: string };

/**
 * Verifica si la ruta del catálogo tiene parámetros obligatorios incompletos
 * (ej. departamento= o provincia= sin valor), para evitar peticiones inválidas.
 */
function isPathIncomplete(path: string): boolean {
  return (
    !path ||
    path.includes("departamento=&") ||
    (path.includes("provincia=") && path.endsWith("provincia=")) ||
    path.endsWith("departamento=")
  );
}

/**
 * Hook para obtener un catálogo desde el backend con cache de React Query.
 *
 * Internamente usa TanStack Query con staleTime de 5 min y cacheTime de 30 min,
 * por lo que llamados repetidos al mismo path no generan peticiones HTTP extra.
 *
 * @param path ruta para la llamada al API (por ejemplo '/catalogos/monedas')
 */
export function useCatalog(path: string) {
  const { data, isLoading, error } = useQuery<CatalogItem[], Error>({
    queryKey: ["catalog", path],
    queryFn: async () => {
      const res = await api.get<CatalogItem[]>(path);
      return res.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutos antes de refetch
    cacheTime: 30 * 60 * 1000, // 30 minutos en cache
    retry: 1,
    enabled: !isPathIncomplete(path),
  });

  return { data: data ?? null, loading: isLoading, error: error ?? null } as const;
}
