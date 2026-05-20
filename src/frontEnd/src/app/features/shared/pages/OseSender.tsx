"use client";

import React from "react";
import { Upload, Send, CheckCircle2, AlertCircle, FileText, X } from "lucide-react";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";

type EstadoEnvio = "idle" | "enviando" | "exito" | "error";

export function OseSender() {
  const [archivo, setArchivo] = React.useState<File | null>(null);
  const [estado, setEstado] = React.useState<EstadoEnvio>("idle");
  const [resultado, setResultado] = React.useState<string>("");

  const handleArchivoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file && file.name.endsWith(".xml")) {
      setArchivo(file);
      setEstado("idle");
      setResultado("");
    } else {
      alert("Seleccioná un archivo .xml válido");
    }
  };

  const enviarAOSE = async () => {
    if (!archivo) return;

    setEstado("enviando");
    setResultado("");

    try {
      const formData = new FormData();
      formData.append("archivo", archivo);

      const res = await api.post("/ose/enviar-xml", formData, {
        headers: { "Content-Type": "multipart/form-data" },
        timeout: 30000,
      });

      setResultado(res.data.resultado || JSON.stringify(res.data, null, 2));
      setEstado(res.data.success ? "exito" : "error");
    } catch (err: any) {
      setResultado(err.response?.data?.error || err.message || "Error de conexión");
      setEstado("error");
    }
  };

  const limpiar = () => {
    setArchivo(null);
    setEstado("idle");
    setResultado("");
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      {/* Header */}
      <div className="flex items-center gap-3 mb-8">
        <div className="size-10 rounded-lg bg-primary/10 flex items-center justify-center">
          <Upload className="size-5 text-primary" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-foreground">Enviar a OSE</h1>
          <p className="text-sm text-muted-foreground mt-0.5">
            Subí un XML UBL 2.1 y envialo al Operador de Servicios Electrónicos para validación
          </p>
        </div>
      </div>

      {/* Subir archivo */}
      <div className="bg-card border border-border rounded-xl p-6 space-y-4">
        <label
          htmlFor="xml-upload"
          className="flex flex-col items-center justify-center gap-3 border-2 border-dashed border-border rounded-lg p-8 cursor-pointer hover:border-primary/50 transition-colors bg-muted/30"
        >
          <FileText className="size-10 text-muted-foreground" />
          <div className="text-center">
            <p className="text-sm font-medium text-foreground">
              {archivo ? archivo.name : "Hacé clic para seleccionar un archivo XML"}
            </p>
            <p className="text-xs text-muted-foreground mt-1">
              {archivo
                ? `${(archivo.size / 1024).toFixed(1)} KB`
                : "Solo archivos .xml UBL 2.1"}
            </p>
          </div>
          <input
            id="xml-upload"
            type="file"
            accept=".xml"
            className="hidden"
            onChange={handleArchivoChange}
          />
        </label>

        {/* Acciones */}
        {archivo && (
          <div className="flex items-center gap-3">
            <Button
              onClick={enviarAOSE}
              disabled={estado === "enviando"}
              className="flex-1"
            >
              {estado === "enviando" ? (
                <>Enviando...</>
              ) : (
                <>
                  <Send className="size-4" />
                  Enviar a OSE
                </>
              )}
            </Button>
            <Button variant="outline" onClick={limpiar} disabled={estado === "enviando"}>
              <X className="size-4" />
              Limpiar
            </Button>
          </div>
        )}
      </div>

      {/* Resultado */}
      {resultado && (
        <div
          className={`mt-6 rounded-xl border p-5 ${
            estado === "exito"
              ? "bg-green-50 border-green-200"
              : "bg-red-50 border-red-200"
          }`}
        >
          <div className="flex items-center gap-2 mb-3">
            {estado === "exito" ? (
              <>
                <CheckCircle2 className="size-5 text-green-600" />
                <span className="font-semibold text-green-800">Respuesta del OSE</span>
              </>
            ) : (
              <>
                <AlertCircle className="size-5 text-red-600" />
                <span className="font-semibold text-red-800">Error</span>
              </>
            )}
          </div>
          <pre
            className={`text-sm overflow-auto max-h-96 rounded-lg p-4 ${
              estado === "exito"
                ? "bg-green-100/50 text-green-900"
                : "bg-red-100/50 text-red-900"
            }`}
          >
            {resultado}
          </pre>
        </div>
      )}
    </div>
  );
}
