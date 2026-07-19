import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { toast } from "sonner";
import axios from "axios";

export interface CatalogCrudRequest {
  codigo: string;
  descripcion: string;
  codigoTributario?: string;
}

export interface CatalogResponse {
  id: number;
  codigo: string;
  descripcion: string;
  activo: boolean;
  codigoTributario?: string | null;
}

function handleError(error: unknown) {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    const data = error.response?.data as Record<string, string> | undefined;
    const msg = data?.error || data?.message || "Error de conexión";
    if (status === 403) {
      toast.error("No autorizado");
    } else if (status === 409) {
      toast.error("El código ya existe");
    } else if (status === 400) {
      toast.error(msg);
    } else {
      toast.error(msg);
    }
  } else {
    toast.error("Error inesperado");
  }
}

export function useAdminCatalog(tipo: string) {
  const queryClient = useQueryClient();
  const queryKey = ["admin-catalog", tipo];

  const list = useQuery({
    queryKey,
    queryFn: () =>
      api.get<CatalogResponse[]>(`/admin/catalogos/${tipo}`).then((r) => r.data),
    staleTime: 5 * 60 * 1000,
  });

  const create = useMutation({
    mutationFn: (data: CatalogCrudRequest) =>
      api.post(`/admin/catalogos/${tipo}`, data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey }),
    onError: handleError,
  });

  const update = useMutation({
    mutationFn: ({ id, data }: { id: number; data: CatalogCrudRequest }) =>
      api.put(`/admin/catalogos/${tipo}/${id}`, data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey }),
    onError: handleError,
  });

  const remove = useMutation({
    mutationFn: (id: number) =>
      api.delete(`/admin/catalogos/${tipo}/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey }),
    onError: handleError,
  });

  const reactivar = useMutation({
    mutationFn: (id: number) =>
      api.patch(`/admin/catalogos/${tipo}/${id}/reactivar`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey }),
    onError: handleError,
  });

  return { list, create, update, remove, reactivar };
}
