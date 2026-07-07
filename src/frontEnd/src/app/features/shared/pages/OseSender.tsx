"use client";

import React from "react";
import { Upload, Send, CheckCircle2, AlertCircle, FileText, X, Clock } from "lucide-react";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";

type EstadoEnvio = "idle" | "enviando" | "exito" | "error";
type EstadoConsulta = "idle" | "consultando" | "en-proceso" | "exito" | "error";

const TICKET_REGEX = /\b\d{15,}\b/;
const STATUS_IN_PROGRESS = "98";
const MAX_STATUS_ATTEMPTS = 12;
const STATUS_POLL_INTERVAL_MS = 5000;

export function OseSender() {
  const [archivo, setArchivo] = React.useState<File | null>(null);
  const [estado, setEstado] = React.useState<EstadoEnvio>("idle");
  const [resultado, setResultado] = React.useState<string>("");
  const [ticket, setTicket] = React.useState<string>("");
  const [estadoConsulta, setEstadoConsulta] = React.useState<EstadoConsulta>("idle");
  const [respuestaTicket, setRespuestaTicket] = React.useState<string>("");
  const [intentosConsulta, setIntentosConsulta] = React.useState(0);
  const consultaTimerRef = React.useRef<number | null>(null);

  React.useEffect(() => () => clearConsultaTimer(), []);

  const handleArchivoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file && file.name.endsWith(".xml")) {
      setArchivo(file);
      setEstado("idle");
      setResultado("");
      setTicket("");
      setRespuestaTicket("");
      setEstadoConsulta("idle");
      setIntentosConsulta(0);
      clearConsultaTimer();
    } else {
      alert("Seleccioná un archivo .xml válido");
    }
  };

  const enviarAOSE = async () => {
    if (!archivo) return;

    setEstado("enviando");
    setResultado("");
    setTicket("");
    setRespuestaTicket("");
    setEstadoConsulta("idle");
    setIntentosConsulta(0);
    clearConsultaTimer();

    try {
      const formData = new FormData();
      formData.append("archivo", archivo);

      const res = await api.post("/ose/enviar-xml", formData, {
        headers: { "Content-Type": "multipart/form-data" },
        timeout: 30000,
      });

      const resultadoOse = res.data.resultado || JSON.stringify(res.data, null, 2);
      const ticketOse = extractTicket(resultadoOse);

      setResultado(resultadoOse);
      setTicket(ticketOse);
      setEstado(res.data.success ? "exito" : "error");

      if (ticketOse) {
        await consultarTicket(ticketOse);
      }
    } catch (err: any) {
      setResultado(err.response?.data?.error || err.message || "Error de conexión");
      setEstado("error");
    }
  };

  const consultarTicket = async (ticketOse = ticket, intento = 1) => {
    if (!ticketOse) return;

    clearConsultaTimer();
    setEstadoConsulta("consultando");
    setIntentosConsulta(intento);

    try {
      const res = await api.get(`/ose/status/${encodeURIComponent(ticketOse)}`, {
        timeout: 30000,
      });
      const respuesta = res.data.resultado || JSON.stringify(res.data, null, 2);
      setRespuestaTicket(respuesta);

      if (isStatusInProgress(respuesta)) {
        setEstadoConsulta("en-proceso");
        if (intento < MAX_STATUS_ATTEMPTS) {
          consultaTimerRef.current = window.setTimeout(
            () => consultarTicket(ticketOse, intento + 1),
            STATUS_POLL_INTERVAL_MS,
          );
        }
        return;
      }

      setEstadoConsulta(res.data.success ? "exito" : "error");
    } catch (err: any) {
      setRespuestaTicket(err.response?.data?.error || err.message || "Error de conexión");
      setEstadoConsulta("error");
    }
  };

  const limpiar = () => {
    setArchivo(null);
    setEstado("idle");
    setResultado("");
    setTicket("");
    setRespuestaTicket("");
    setEstadoConsulta("idle");
    setIntentosConsulta(0);
    clearConsultaTimer();
  };

  const clearConsultaTimer = () => {
    if (consultaTimerRef.current === null) return;
    window.clearTimeout(consultaTimerRef.current);
    consultaTimerRef.current = null;
  };

  const extractTicket = (value: string) => value.match(TICKET_REGEX)?.[0] ?? "";

  const isStatusInProgress = (value: string) => {
    const normalizedValue = value.trim();
    return normalizedValue === STATUS_IN_PROGRESS || normalizedValue.includes("statusCode>98<");
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

      {ticket && (
        <div className="mt-6 rounded-xl border border-blue-200 bg-blue-50 p-5">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <div className="flex items-center gap-2">
                {estadoConsulta === "en-proceso" ? (
                  <Clock className="size-5 text-amber-600" />
                ) : (
                  <CheckCircle2 className="size-5 text-blue-600" />
                )}
                <span className="font-semibold text-blue-900">Ticket recibido</span>
              </div>
              <p className="mt-1 font-mono text-sm text-blue-950">{ticket}</p>
              {estadoConsulta === "en-proceso" && (
                <p className="mt-1 text-sm text-amber-700">
                  Estado 98: el OSE sigue procesando. Se consultará automáticamente cada 5 segundos.
                </p>
              )}
            </div>
            <Button
              variant="outline"
              onClick={() => consultarTicket()}
              disabled={estadoConsulta === "consultando" || estadoConsulta === "en-proceso"}
              className="border-blue-300 bg-white text-blue-900 hover:bg-blue-100"
            >
              {estadoConsulta === "consultando" || estadoConsulta === "en-proceso"
                ? "Esperando OSE..."
                : "Consultar ticket"}
            </Button>
          </div>

          <div className="mt-4 rounded-lg border border-blue-200 bg-white p-4">
            <div className="mb-2 flex items-center justify-between gap-3">
              <span className="text-sm font-semibold text-blue-900">Respuesta de getStatus</span>
              {intentosConsulta > 0 && (
                <span className="text-xs text-blue-700">
                  Intento {intentosConsulta}/{MAX_STATUS_ATTEMPTS}
                </span>
              )}
            </div>
            {estadoConsulta === "consultando" ? (
              <p className="text-sm text-blue-800">Consultando estado del ticket...</p>
            ) : respuestaTicket ? (
              <pre className="max-h-96 overflow-auto rounded-lg bg-blue-100/60 p-4 text-sm text-blue-950">
                {respuestaTicket}
              </pre>
            ) : (
              <p className="text-sm text-blue-800">
                Cuando el OSE devuelva el estado, se mostrará acá.
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
