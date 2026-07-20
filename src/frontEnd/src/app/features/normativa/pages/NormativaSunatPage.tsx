"use client";

import React from "react";
import { ExternalLink, Search, AlertTriangle, FileText } from "lucide-react";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  Tooltip,
  TooltipTrigger,
  TooltipContent,
} from "@/components/ui/tooltip";
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
} from "@/components/ui/table";
import {
  Alert,
  AlertTitle,
  AlertDescription,
} from "@/components/ui/alert";
import { cn } from "@/components/ui/utils";

type Resolucion = {
  numero: string;
  sumilla: string;
  fecha: string;
  urlPdf: string;
  urlAnexo: string | null;
  categoria: string;
  stale: boolean;
};

const CATEGORIA_COLORS: Record<string, string> = {
  "Facturación Electrónica":
    "bg-blue-100 text-blue-800 border-blue-200 dark:bg-blue-950 dark:text-blue-300 dark:border-blue-800",
  "Guías de Remisión":
    "bg-orange-100 text-orange-800 border-orange-200 dark:bg-orange-950 dark:text-orange-300 dark:border-orange-800",
  Catálogos:
    "bg-green-100 text-green-800 border-green-200 dark:bg-green-950 dark:text-green-300 dark:border-green-800",
  Tributos:
    "bg-red-100 text-red-800 border-red-200 dark:bg-red-950 dark:text-red-300 dark:border-red-800",
  General:
    "bg-gray-100 text-gray-800 border-gray-200 dark:bg-gray-800 dark:text-gray-300 dark:border-gray-700",
};

function categoriaBadgeClass(categoria: string): string {
  return CATEGORIA_COLORS[categoria] ?? CATEGORIA_COLORS["General"];
}

function formatDate(iso: string): string {
  try {
    const date = new Date(iso);
    if (isNaN(date.getTime())) return iso;
    return date.toLocaleDateString("es-PE", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    });
  } catch {
    return iso;
  }
}

export function NormativaSunatPage() {
  const currentYear = new Date().getFullYear();
  const [anio, setAnio] = React.useState(currentYear);
  const [q, setQ] = React.useState("");
  const [resoluciones, setResoluciones] = React.useState<Resolucion[]>([]);
  const [loading, setLoading] = React.useState(true);

  const fetchResoluciones = React.useCallback(
    async (year: number, query: string) => {
      setLoading(true);
      try {
        const params: Record<string, string> = { anio: String(year) };
        if (query.trim()) params.q = query.trim();
        const res = await api.get<Resolucion[]>("/normativa/resoluciones", {
          params,
        });
        setResoluciones(res.data ?? []);
      } catch {
        setResoluciones([]);
      } finally {
        setLoading(false);
      }
    },
    [],
  );

  // Carga inicial
  React.useEffect(() => {
    fetchResoluciones(anio, q);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchResoluciones(anio, q);
  };

  const handleYearChange = (year: number) => {
    setAnio(year);
    fetchResoluciones(year, q);
  };

  const anyStale = resoluciones.some((r) => r.stale);

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Normativa SUNAT</h1>
        <p className="text-muted-foreground text-sm mt-1">
          Consultá resoluciones, decretos y normativas publicadas por SUNAT.
        </p>
      </div>

      {/* Banner de datos desactualizados */}
      {anyStale && (
        <Alert variant="destructive">
          <AlertTriangle className="size-4" />
          <AlertTitle>Datos posiblemente desactualizados</AlertTitle>
          <AlertDescription>
            Algunas resoluciones pueden estar desactualizadas. Verificá la
            fecha de publicación antes de usar la información.
          </AlertDescription>
        </Alert>
      )}

      {/* Filtros */}
      <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <label
            htmlFor="anio"
            className="text-xs font-medium text-muted-foreground"
          >
            Año
          </label>
          <select
            id="anio"
            value={anio}
            onChange={(e) => handleYearChange(Number(e.target.value))}
            className="flex h-10 w-28 rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
          >
            {Array.from({ length: 10 }, (_, i) => currentYear - i).map(
              (y) => (
                <option key={y} value={y}>
                  {y}
                </option>
              ),
            )}
          </select>
        </div>

        <div className="space-y-1.5 flex-1 min-w-[200px]">
          <label
            htmlFor="q"
            className="text-xs font-medium text-muted-foreground"
          >
            Buscar
          </label>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground pointer-events-none" />
            <Input
              id="q"
              placeholder="Buscá por resolución, palabra clave…"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              className="pl-9"
            />
          </div>
        </div>

        <Button type="submit" disabled={loading} className="cursor-pointer">
          {loading ? "Buscando…" : "Buscar"}
        </Button>
      </form>

      {/* Estado de carga */}
      {loading && (
        <div className="flex items-center justify-center py-12 text-muted-foreground">
          <div className="flex flex-col items-center gap-2">
            <div className="size-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
            <span className="text-sm">Cargando resoluciones…</span>
          </div>
        </div>
      )}

      {/* Estado vacío */}
      {!loading && resoluciones.length === 0 && (
        <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
          <FileText className="size-12 mb-3 opacity-30" />
          <p className="text-sm font-medium">No se encontraron resoluciones</p>
          <p className="text-xs mt-1">
            Probá con otro año o términos de búsqueda diferentes.
          </p>
        </div>
      )}

      {/* Tabla de resoluciones */}
      {!loading && resoluciones.length > 0 && (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[120px]">N° Resolución</TableHead>
              <TableHead>Descripción</TableHead>
              <TableHead className="w-[110px]">Fecha</TableHead>
              <TableHead className="w-[160px]">Categoría</TableHead>
              <TableHead className="w-[60px] text-center">Anexo</TableHead>
              <TableHead className="w-[60px] text-right">PDF</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {resoluciones.map((r) => (
              <TableRow key={r.numero}>
                <TableCell className="font-mono text-xs font-medium">
                  {r.numero}
                </TableCell>
                <TableCell className="max-w-md">
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <p className="text-sm leading-snug line-clamp-2 cursor-help">
                        {r.sumilla}
                      </p>
                    </TooltipTrigger>
                    <TooltipContent side="top" className="max-w-md">
                      <p className="text-xs leading-relaxed">{r.sumilla}</p>
                    </TooltipContent>
                  </Tooltip>
                </TableCell>
                <TableCell className="text-xs text-muted-foreground whitespace-nowrap">
                  {formatDate(r.fecha)}
                </TableCell>
                <TableCell>
                  <Badge
                    variant="outline"
                    className={cn(
                      "text-[11px] font-medium",
                      categoriaBadgeClass(r.categoria),
                    )}
                  >
                    {r.categoria}
                  </Badge>
                </TableCell>
                <TableCell className="text-center">
                  {r.urlAnexo ? (
                    <a
                      href={r.urlAnexo}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center justify-center size-8 rounded-md text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                      title="Abrir anexo"
                    >
                      <ExternalLink className="size-4" />
                    </a>
                  ) : (
                    <span className="text-muted-foreground/40">—</span>
                  )}
                </TableCell>
                <TableCell className="text-right">
                  <a
                    href={r.urlPdf}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center justify-center size-8 rounded-md text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                    title="Abrir PDF"
                  >
                    <ExternalLink className="size-4" />
                  </a>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}

export default NormativaSunatPage;
