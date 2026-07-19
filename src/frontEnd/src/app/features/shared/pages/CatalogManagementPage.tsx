"use client";

import React, { useState } from "react";
import { Plus, Pencil, Trash2, RotateCcw, Loader2 } from "lucide-react";

import {
  Table,
  TableHeader,
  TableBody,
  TableHead,
  TableRow,
  TableCell,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog";
import {
  Tabs,
  TabsList,
  TabsTrigger,
  TabsContent,
} from "@/components/ui/tabs";
import {
  useAdminCatalog,
  type CatalogResponse,
  type CatalogCrudRequest,
} from "@/hooks/useAdminCatalog";

// ---------------------------------------------------------------------------
// Constantes
// ---------------------------------------------------------------------------

const CATALOGOS = [
  { key: "tipos-operacion", label: "Tipos de Operación" },
  { key: "tipos-nota-credito", label: "Tipos de Nota de Crédito" },
  { key: "tipos-nota-debito", label: "Tipos de Nota de Débito" },
  { key: "sistemas-isc", label: "Sistemas ISC" },
  { key: "tipos-afectacion-igv", label: "Tipos de Afectación IGV" },
  { key: "tipos-documento-identidad", label: "Tipos de Documento de Identidad" },
  { key: "motivos-traslado", label: "Motivos de Traslado" },
];

const IGV_TIPO = "tipos-afectacion-igv";

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function isIgvTab(tipo: string) {
  return tipo === IGV_TIPO;
}

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

type DialogMode = "create" | "edit";

interface DialogState {
  open: boolean;
  mode: DialogMode;
  editingItem: CatalogResponse | null;
}

// ---------------------------------------------------------------------------
// SkeletonTable
// ---------------------------------------------------------------------------

function SkeletonTable({ rows = 5 }: { rows?: number }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, i) => (
        <Skeleton key={i} className="h-10 w-full" />
      ))}
    </div>
  );
}

// ---------------------------------------------------------------------------
// CatalogTab
// ---------------------------------------------------------------------------

function CatalogTab({ tipo, label }: { tipo: string; label: string }) {
  const { list, create, update, remove, reactivar } = useAdminCatalog(tipo);
  const [dialog, setDialog] = useState<DialogState>({
    open: false,
    mode: "create",
    editingItem: null,
  });

  // Form state
  const [formCodigo, setFormCodigo] = useState("");
  const [formDescripcion, setFormDescripcion] = useState("");
  const [formCodigoTrib, setFormCodigoTrib] = useState("");

  // ── Dialog helpers ────────────────────────────────────────────────────

  function openCreate() {
    setFormCodigo("");
    setFormDescripcion("");
    setFormCodigoTrib("");
    setDialog({ open: true, mode: "create", editingItem: null });
  }

  function openEdit(item: CatalogResponse) {
    setFormCodigo(item.codigo);
    setFormDescripcion(item.descripcion);
    setFormCodigoTrib(item.codigoTributario ?? "");
    setDialog({ open: true, mode: "edit", editingItem: item });
  }

  function closeDialog() {
    setDialog({ open: false, mode: "create", editingItem: null });
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!formDescripcion.trim()) return;

    const payload: CatalogCrudRequest = {
      codigo: formCodigo.trim(),
      descripcion: formDescripcion.trim(),
    };
    if (isIgvTab(tipo) && formCodigoTrib.trim()) {
      payload.codigoTributario = formCodigoTrib.trim();
    }

    if (dialog.mode === "create") {
      if (!formCodigo.trim()) return;
      create.mutate(payload, { onSettled: closeDialog });
    } else if (dialog.mode === "edit" && dialog.editingItem) {
      update.mutate(
        { id: dialog.editingItem.id, data: payload },
        { onSettled: closeDialog },
      );
    }
  }

  function handleToggleActivo(item: CatalogResponse) {
    if (item.activo) {
      remove.mutate(item.id);
    } else {
      reactivar.mutate(item.id);
    }
  }

  const isLoading = list.isLoading || list.isFetching;
  const isPending = create.isPending || update.isPending || remove.isPending;

  // ── Render ────────────────────────────────────────────────────────────

  return (
    <>
      {/* Toolbar */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold">{label}</h3>
        <Button size="sm" onClick={openCreate} disabled={isPending}>
          <Plus className="size-4" />
          Nuevo
        </Button>
      </div>

      {/* Table */}
      {isLoading ? (
        <SkeletonTable rows={5} />
      ) : (
        <div className="rounded-lg border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Código</TableHead>
                <TableHead>Descripción</TableHead>
                {isIgvTab(tipo) && <TableHead>Cód. Tributario</TableHead>}
                <TableHead>Estado</TableHead>
                <TableHead className="text-right">Acciones</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {list.data?.length === 0 && (
                <TableRow>
                  <TableCell
                    colSpan={isIgvTab(tipo) ? 5 : 4}
                    className="text-center text-muted-foreground py-8"
                  >
                    No hay registros
                  </TableCell>
                </TableRow>
              )}
              {list.data?.map((item) => (
                <TableRow key={item.id}>
                  <TableCell className="font-mono text-xs">
                    {item.codigo}
                  </TableCell>
                  <TableCell>{item.descripcion}</TableCell>
                  {isIgvTab(tipo) && (
                    <TableCell className="font-mono text-xs">
                      {item.codigoTributario ?? "—"}
                    </TableCell>
                  )}
                  <TableCell>
                    <Badge
                      variant={item.activo ? "default" : "secondary"}
                    >
                      {item.activo ? "Activo" : "Inactivo"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end gap-1">
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => openEdit(item)}
                        disabled={isPending}
                        title="Editar"
                      >
                        <Pencil className="size-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleToggleActivo(item)}
                        disabled={isPending}
                        title={item.activo ? "Desactivar" : "Activar"}
                      >
                        {item.activo ? (
                          <Trash2 className="size-4 text-destructive" />
                        ) : (
                          <RotateCcw className="size-4" />
                        )}
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Dialog create / edit */}
      <Dialog open={dialog.open} onOpenChange={(open) => !open && closeDialog()}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {dialog.mode === "create" ? "Nuevo registro" : "Editar registro"}
            </DialogTitle>
            <DialogDescription>
              {dialog.mode === "create"
                ? "Completá los campos para crear un nuevo registro."
                : "Modificá la descripción del registro."}
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Código — solo en creación */}
            {dialog.mode === "create" && (
              <div className="space-y-1.5">
                <Label htmlFor="codigo">
                  Código <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="codigo"
                  value={formCodigo}
                  onChange={(e) => setFormCodigo(e.target.value)}
                  placeholder="01"
                  required
                  maxLength={10}
                />
              </div>
            )}

            {/* Descripción */}
            <div className="space-y-1.5">
              <Label htmlFor="descripcion">
                Descripción <span className="text-destructive">*</span>
              </Label>
              <Input
                id="descripcion"
                value={formDescripcion}
                onChange={(e) => setFormDescripcion(e.target.value)}
                placeholder="Descripción del registro"
                required
                maxLength={250}
              />
            </div>

            {/* Código Tributario — solo IGV */}
            {isIgvTab(tipo) && (
              <div className="space-y-1.5">
                <Label htmlFor="codigoTributario">Código Tributario</Label>
                <Input
                  id="codigoTributario"
                  value={formCodigoTrib}
                  onChange={(e) => setFormCodigoTrib(e.target.value)}
                  placeholder="XX"
                  maxLength={2}
                />
              </div>
            )}

            <DialogFooter>
              <DialogClose asChild>
                <Button type="button" variant="outline" disabled={isPending}>
                  Cancelar
                </Button>
              </DialogClose>
              <Button type="submit" disabled={isPending}>
                {isPending && <Loader2 className="size-4 animate-spin" />}
                {dialog.mode === "create" ? "Crear" : "Guardar"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
}

// ---------------------------------------------------------------------------
// CatalogManagementPage
// ---------------------------------------------------------------------------

export function CatalogManagementPage() {
  const [activeTab, setActiveTab] = useState(CATALOGOS[0].key);

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold tracking-tight">
          Mantenimiento de Catálogos
        </h1>
        <p className="text-sm text-muted-foreground mt-1">
          Administrá los catálogos SUNAT desde esta pantalla.
        </p>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="mb-6 flex-wrap h-auto">
          {CATALOGOS.map((cat) => (
            <TabsTrigger key={cat.key} value={cat.key} className="text-xs sm:text-sm">
              {cat.label}
            </TabsTrigger>
          ))}
        </TabsList>

        {CATALOGOS.map((cat) => (
          <TabsContent key={cat.key} value={cat.key}>
            <CatalogTab tipo={cat.key} label={cat.label} />
          </TabsContent>
        ))}
      </Tabs>
    </div>
  );
}

export default CatalogManagementPage;
