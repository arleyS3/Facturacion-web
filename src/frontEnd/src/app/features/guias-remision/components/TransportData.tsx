import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { useCatalog } from "@/hooks/useCatalog";
import { useFormContext } from "react-hook-form";

/**
 * Sección con datos del traslado (motivo, peso, fechas, modalidad, observaciones).
 */
export function TransportData() {
  const methods = useFormContext();
  const setValue = methods?.setValue;
  const watch = methods?.watch;

  const { data: motivosTraslado, loading: motivosLoading } = useCatalog(
    "/catalogos/motivos-traslado",
  );

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="transport-mode">Motivo Traslado</Label>
          <Select
            value={watch?.("gMotTras") ?? motivosTraslado?.[0]?.code ?? "01"}
            onValueChange={(v) => setValue?.("gMotTras", v)}
            defaultValue={motivosTraslado?.[0]?.code ?? "01"}
          >
            <SelectTrigger id="transport-mode">
              <SelectValue placeholder={motivosLoading ? "Cargando..." : "Seleccione motivo"} />
            </SelectTrigger>
            <SelectContent>
              {motivosLoading ? (
                <div className="p-3">Cargando...</div>
              ) : motivosTraslado && motivosTraslado.length ? (
                motivosTraslado.map((motivo) => (
                  <SelectItem key={motivo.code} value={motivo.code}>
                    {motivo.label}
                  </SelectItem>
                ))
              ) : (
                <>
                  <SelectItem value="01">01 - Venta</SelectItem>
                  <SelectItem value="02">02 - Compra</SelectItem>
                  <SelectItem value="04">04 - Traslado entre establecimientos de la misma empresa</SelectItem>
                  <SelectItem value="08">08 - Importación</SelectItem>
                  <SelectItem value="09">09 - Exportación</SelectItem>
                  <SelectItem value="13">13 - Otros</SelectItem>
                </>
              )}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="transport-description">Descripción Motivo Traslado</Label>
          <Input
            id="transport-description"
            placeholder="Detalle del motivo de traslado"
            value={watch?.("gDescTraslado") ?? ""}
            onChange={(e) => setValue?.("gDescTraslado", e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="transport-start-date">Fecha Inicio Traslado</Label>
          <Input
            id="transport-start-date"
            type="date"
            value={watch?.("g1FecIniTras") ?? ""}
            onChange={(e) => setValue?.("g1FecIniTras", e.target.value)}
          />
        </div>

        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="observations">Observaciones</Label>
          <Textarea
            id="observations"
            placeholder="Observaciones adicionales sobre el traslado"
            rows={3}
            value={watch?.("gObsTras") ?? ""}
            onChange={(e) => setValue?.("gObsTras", e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="transport-id">Identificador de Traslado</Label>
          <Select
            value={watch?.("gIdentifTraslado") ?? ""}
            onValueChange={(v) => setValue?.("gIdentifTraslado", v)}
          >
            <SelectTrigger id="transport-id">
              <SelectValue placeholder="Seleccione un identificador de traslado" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="SUNAT-Envío">SUNAT-Envío</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="transport-modalidad">Modalidad Traslado</Label>
          <Select
            value={watch?.("gModTras") ?? ""}
            onValueChange={(v) => setValue?.("gModTras", v)}
          >
            <SelectTrigger id="transport-modalidad">
              <SelectValue placeholder="Seleccione modalidad" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="01">01 - Transporte Público</SelectItem>
              <SelectItem value="02">02 - Transporte Privado</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="unit-measure">Unidad de Medida</Label>
          <Select
            value={watch?.("gUnmdTotalPeso") ?? ""}
            onValueChange={(v) => setValue?.("gUnmdTotalPeso", v)}
          >
            <SelectTrigger id="unit-measure">
              <SelectValue placeholder="Seleccione una unidad de medida" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="KGM">Peso bruto medido en kilogramos</SelectItem>
              <SelectItem value="TNE">Peso bruto medido en toneladas</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="total-weight">Peso Bruto Total</Label>
          <Input
            id="total-weight"
            type="number"
            step="0.01"
            placeholder="0.00"
            value={watch?.("gPesoBrutoTotItemsSel") ?? ""}
            onChange={(e) => setValue?.("gPesoBrutoTotItemsSel", e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="total-transfer-weight">Total Peso Traslado</Label>
          <Input
            id="total-transfer-weight"
            type="number"
            step="0.01"
            placeholder="0.00"
            value={watch?.("gTotalPesoTraslado") ?? ""}
            onChange={(e) => setValue?.("gTotalPesoTraslado", e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="num-packages">Número de Bultos</Label>
          <Input
            id="num-packages"
            type="number"
            placeholder="0"
            value={watch?.("gNroBultos") ?? ""}
            onChange={(e) => setValue?.("gNroBultos", e.target.value)}
          />
        </div>

        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="weight-difference">Sustento por Diferencia de Peso</Label>
          <Textarea 
            id="weight-difference" 
            placeholder="Explique el sustento en caso de existir diferencia de peso"
            rows={3}
            value={watch?.("gSustentoDifPeso") ?? ""}
            onChange={(e) => setValue?.("gSustentoDifPeso", e.target.value)}
          />
        </div>

        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="vehicle-cert">Tarjeta Única de Circulación Electrónica o Certificado de Habilitación Vehicular</Label>
          <Input
            id="vehicle-cert"
            placeholder="Número de tarjeta o certificado"
            value={watch?.("gCertHabVehicularPrinc") ?? ""}
            onChange={(e) => setValue?.("gCertHabVehicularPrinc", e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="vehicle-plate">Placa de Vehículo Principal</Label>
          <Input
            id="vehicle-plate"
            placeholder="ABC-123"
            className="uppercase"
            value={watch?.("gPlacaVehicPrinc") ?? ""}
            onChange={(e) => setValue?.("gPlacaVehicPrinc", e.target.value.toUpperCase())}
          />
        </div>
      </div>
    </div>
  );
}
