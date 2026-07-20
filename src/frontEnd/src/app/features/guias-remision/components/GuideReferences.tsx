"use client";

import React, { useState } from "react";
import { FileText, Plus, Trash2, Link as LinkIcon, FileCheck } from "lucide-react";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useCatalog } from "@/hooks/useCatalog";
import { useFormContext } from "react-hook-form";
import { toast } from "sonner";

interface Reference {
  id: string;
  correlativo: number;
  tipo: string;
  serie: string;
  folio: string;
  tipoDocEmisor: string;
  rucEmisor: string;
  categoriaRef?: "principal" | "otro";
}

const padToEightDigits = (val: string): string => {
  const digits = val.replace(/\D/g, "");
  if (!digits) return val;
  return digits.padStart(8, "0");
};

export function GuideReferences() {
  const methods = useFormContext();
  const setValue = methods?.setValue;

  const { data: documentosRelacionados, loading: documentosLoading } = useCatalog(
    "/catalogos/documentos-relacionados-transporte"
  );

  const [references, setReferences] = useState<Reference[]>([]);

  // Campos para Documentos Relacionados
  const [tipo, setTipo] = useState("");
  const [serie, setSerie] = useState("");
  const [folio, setFolio] = useState("");
  const [tipoDocEmisor, setTipoDocEmisor] = useState("6");
  const [rucEmisor, setRucEmisor] = useState("");

  // Campos para Otros Documentos Relacionados
  const [otroTipo, setOtroTipo] = useState("99");
  const [otroSerie, setOtroSerie] = useState("");
  const [otroNumero, setOtroNumero] = useState("");
  const [otroRucEmisor, setOtroRucEmisor] = useState("");

  const syncFormReferences = (items: Reference[]) => {
    if (!setValue) return;
    setValue(
      "shippingReferences",
      items.map((r) => ({
        NroLinRef: String(r.correlativo),
        TpoDocRef: r.tipo,
        SerieRef: r.serie,
        FolioRef: r.folio,
        TipoRucEmisDocRef: r.tipoDocEmisor,
        RucEmisDocRef: r.rucEmisor,
      }))
    );
  };

  const addReference = (isOtros = false) => {
    const targetTipo = isOtros ? otroTipo : tipo;
    const targetSerie = (isOtros ? otroSerie : serie).trim().toUpperCase();
    const rawFolio = isOtros ? otroNumero : folio;
    const targetFolio = padToEightDigits(rawFolio.trim());
    const targetTipoDoc = isOtros ? "6" : tipoDocEmisor;
    const targetRuc = (isOtros ? otroRucEmisor : rucEmisor).trim();

    if (!targetTipo || !targetSerie || !targetFolio) {
      toast.error("Complete al menos Tipo, Serie y Número para agregar la referencia");
      return;
    }

    const newReference: Reference = {
      id: `${Date.now()}-${Math.random().toString(36).substr(2, 4)}`,
      correlativo: references.length + 1,
      tipo: targetTipo,
      serie: targetSerie,
      folio: targetFolio,
      tipoDocEmisor: targetTipoDoc,
      rucEmisor: targetRuc,
      categoriaRef: isOtros ? "otro" : "principal",
    };

    const updated = [...references, newReference];
    setReferences(updated);
    syncFormReferences(updated);

    if (isOtros) {
      setOtroTipo("99");
      setOtroSerie("");
      setOtroNumero("");
      setOtroRucEmisor("");
    } else {
      setTipo("");
      setSerie("");
      setFolio("");
      setTipoDocEmisor("6");
      setRucEmisor("");
    }
    toast.success("Documento de referencia agregado");
  };

  const removeReference = (id: string) => {
    const filtered = references.filter((r) => r.id !== id);
    const reindexed = filtered.map((r, index) => ({
      ...r,
      correlativo: index + 1,
    }));
    setReferences(reindexed);
    syncFormReferences(reindexed);
    toast.info("Referencia eliminada");
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        <div className="p-2.5 rounded-lg bg-primary/10 text-primary">
          <LinkIcon className="size-5" aria-hidden="true" />
        </div>
        <div>
          <h3 className="font-semibold text-lg text-foreground tracking-tight">
            Documentos Relacionados y Referencias
          </h3>
          <p className="text-sm text-muted-foreground">
            Asocie comprobantes previos (Facturas, Boletas, Guías) u otros documentos al traslado
          </p>
        </div>
      </div>

      {/* Grid de Formulario */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 1. Documentos de Referencia Principales */}
        <Card className="shadow-sm border-border">
          <CardHeader className="pb-3">
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <FileText className="size-4 text-primary" aria-hidden="true" />
              Comprobantes de Referencia
            </CardTitle>
            <CardDescription className="text-xs">
              Factura, Boleta de Venta o Guía de Remisión de origen
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="space-y-1.5 sm:col-span-3">
                <Label htmlFor="ref-tipo" className="text-xs font-medium">Tipo de Documento</Label>
                <Select value={tipo} onValueChange={setTipo}>
                  <SelectTrigger id="ref-tipo" className="h-9">
                    <SelectValue placeholder={documentosLoading ? "Cargando…" : "Seleccione tipo"} />
                  </SelectTrigger>
                  <SelectContent>
                    {documentosLoading ? (
                      <div className="p-3 text-xs text-muted-foreground">Cargando…</div>
                    ) : documentosRelacionados && documentosRelacionados.length ? (
                      documentosRelacionados.map((opt) => (
                        <SelectItem key={opt.code} value={opt.code}>
                          {opt.label}
                        </SelectItem>
                      ))
                    ) : (
                      <>
                        <SelectItem value="01">01 - Factura</SelectItem>
                        <SelectItem value="03">03 - Boleta de Venta</SelectItem>
                        <SelectItem value="07">07 - Nota de Crédito</SelectItem>
                        <SelectItem value="08">08 - Nota de Débito</SelectItem>
                        <SelectItem value="09">09 - Guía de Remisión Remitente</SelectItem>
                      </>
                    )}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="ref-serie" className="text-xs font-medium">Serie</Label>
                <Input
                  id="ref-serie"
                  placeholder="F001…"
                  value={serie}
                  onChange={(e) => setSerie(e.target.value.toUpperCase())}
                  className="h-9 font-mono uppercase text-xs"
                  autoComplete="off"
                  spellCheck={false}
                />
              </div>

              <div className="space-y-1.5 sm:col-span-2">
                <Label htmlFor="ref-folio" className="text-xs font-medium">Número (Correlativo)</Label>
                <Input
                  id="ref-folio"
                  placeholder="00000001…"
                  value={folio}
                  inputMode="numeric"
                  onChange={(e) => setFolio(e.target.value.replace(/\D/g, ""))}
                  onBlur={() => {
                    if (folio) setFolio(padToEightDigits(folio));
                  }}
                  className="h-9 font-mono text-xs"
                  autoComplete="off"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="tipo-doc-emisor" className="text-xs font-medium">Doc. Emisor</Label>
                <Select value={tipoDocEmisor} onValueChange={setTipoDocEmisor}>
                  <SelectTrigger id="tipo-doc-emisor" className="h-9">
                    <SelectValue placeholder="RUC" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="6">RUC (6)</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-1.5 sm:col-span-2">
                <Label htmlFor="ruc-emisor" className="text-xs font-medium">RUC Emisor Referencia</Label>
                <Input
                  id="ruc-emisor"
                  placeholder="20XXXXXXXXX…"
                  maxLength={11}
                  inputMode="numeric"
                  value={rucEmisor}
                  onChange={(e) => setRucEmisor(e.target.value.replace(/\D/g, ""))}
                  className="h-9 font-mono text-xs"
                  autoComplete="off"
                />
              </div>
            </div>

            {serie && folio && (
              <div className="p-2.5 rounded bg-muted/50 border text-xs flex items-center justify-between">
                <span className="text-muted-foreground">ID Unido:</span>
                <span className="font-mono font-bold text-foreground">
                  {serie}-{padToEightDigits(folio)}
                </span>
              </div>
            )}

            <div className="flex justify-end pt-1">
              <Button onClick={() => addReference(false)} size="sm" className="gap-2 cursor-pointer">
                <Plus className="size-4" aria-hidden="true" />
                Agregar Referencia
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* 2. Otros Documentos Relacionados (Serie + Número Separados) */}
        <Card className="shadow-sm border-border">
          <CardHeader className="pb-3">
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <FileCheck className="size-4 text-primary" aria-hidden="true" />
              Otros Documentos Relacionados
            </CardTitle>
            <CardDescription className="text-xs">
              DAM, Manifiesto de Carga, Orden de Compra u otros documentos
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="otro-tipo" className="text-xs font-medium">Tipo de Documento</Label>
              <Select value={otroTipo} onValueChange={setOtroTipo}>
                <SelectTrigger id="otro-tipo" className="h-9">
                  <SelectValue placeholder="Seleccione tipo" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="99">99 - Otros documentos relacionados</SelectItem>
                  <SelectItem value="50">50 - Declaración Única de Aduanas / DAM</SelectItem>
                  <SelectItem value="52">52 - Despacho Simplificado</SelectItem>
                  <SelectItem value="04">04 - Liquidación de compra</SelectItem>
                  <SelectItem value="31">31 - Guía Transportista</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="otro-serie" className="text-xs font-medium">Serie Documento</Label>
                <Input
                  id="otro-serie"
                  placeholder="0001 / SERIE…"
                  value={otroSerie}
                  onChange={(e) => setOtroSerie(e.target.value.toUpperCase())}
                  className="h-9 font-mono uppercase text-xs"
                  autoComplete="off"
                  spellCheck={false}
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="otro-numero" className="text-xs font-medium">Número Documento</Label>
                <Input
                  id="otro-numero"
                  placeholder="00000001…"
                  value={otroNumero}
                  inputMode="numeric"
                  onChange={(e) => setOtroNumero(e.target.value.replace(/\D/g, ""))}
                  onBlur={() => {
                    if (otroNumero) setOtroNumero(padToEightDigits(otroNumero));
                  }}
                  className="h-9 font-mono text-xs"
                  autoComplete="off"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="otro-ruc" className="text-xs font-medium">RUC Emisor (Opcional)</Label>
              <Input
                id="otro-ruc"
                placeholder="20XXXXXXXXX…"
                maxLength={11}
                inputMode="numeric"
                value={otroRucEmisor}
                onChange={(e) => setOtroRucEmisor(e.target.value.replace(/\D/g, ""))}
                className="h-9 font-mono text-xs"
                autoComplete="off"
              />
            </div>

            {otroSerie && otroNumero && (
              <div className="p-2.5 rounded bg-muted/50 border text-xs flex items-center justify-between">
                <span className="text-muted-foreground">ID Unido:</span>
                <span className="font-mono font-bold text-foreground">
                  {otroSerie}-{padToEightDigits(otroNumero)}
                </span>
              </div>
            )}

            <div className="flex justify-end pt-1">
              <Button onClick={() => addReference(true)} size="sm" variant="secondary" className="gap-2 cursor-pointer">
                <Plus className="size-4" aria-hidden="true" />
                Agregar Otro Doc.
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Tabla General de Referencias */}
      <Card className="shadow-sm border-border">
        <CardHeader className="pb-3">
          <CardTitle className="text-base font-semibold">Lista de Documentos de Referencia Agregados</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {references.length === 0 ? (
            <div className="p-8 text-center text-muted-foreground text-sm">
              No hay documentos de referencia agregados. Complete los datos superiores y presione "Agregar".
            </div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader className="bg-muted/50">
                  <TableRow>
                    <TableHead className="w-[100px]">Línea</TableHead>
                    <TableHead className="w-[120px]">Categoría</TableHead>
                    <TableHead className="w-[120px]">Tipo</TableHead>
                    <TableHead className="w-[140px]">Serie</TableHead>
                    <TableHead className="w-[160px]">Número (Folio)</TableHead>
                    <TableHead className="w-[180px]">ID Unido</TableHead>
                    <TableHead className="w-[160px]">RUC Emisor</TableHead>
                    <TableHead className="w-[60px] text-right">Acciones</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {references.map((r) => (
                    <TableRow key={r.id} className="hover:bg-muted/50">
                      <TableCell className="font-mono text-xs font-semibold">{r.correlativo}</TableCell>
                      <TableCell>
                        <Badge variant={r.categoriaRef === "otro" ? "secondary" : "default"} className="text-[10px]">
                          {r.categoriaRef === "otro" ? "Otro Doc." : "Comprobante"}
                        </Badge>
                      </TableCell>
                      <TableCell className="font-mono text-xs">{r.tipo}</TableCell>
                      <TableCell className="font-mono text-xs font-semibold">{r.serie}</TableCell>
                      <TableCell className="font-mono text-xs font-semibold tabular-nums">{r.folio}</TableCell>
                      <TableCell className="font-mono text-xs font-bold text-primary tabular-nums">
                        {r.serie}-{r.folio}
                      </TableCell>
                      <TableCell className="font-mono text-xs text-muted-foreground">{r.rucEmisor || "—"}</TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8 text-destructive hover:bg-destructive/10 cursor-pointer"
                          onClick={() => removeReference(r.id)}
                          aria-label={`Eliminar referencia ${r.serie}-${r.folio}`}
                        >
                          <Trash2 className="size-4" aria-hidden="true" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

export default GuideReferences;
