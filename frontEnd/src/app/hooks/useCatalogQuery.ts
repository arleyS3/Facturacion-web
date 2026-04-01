import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import type { CatalogItem } from './useCatalog';

export function useCatalogQuery(path: string) {
  return useQuery<CatalogItem[], Error>(['catalog', path], async () => {
    const res = await api.get<CatalogItem[]>(path);
    return res.data;
  }, {
    staleTime: 1000 * 60 * 5, // 5 minutes
    cacheTime: 1000 * 60 * 30,
    retry: 1,
  });
}
