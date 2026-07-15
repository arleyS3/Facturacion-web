import { Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useState, useMemo, useEffect } from "react";
import { useFormContext } from "react-hook-form";

/**
 * Catálogo 53 — Códigos de descuento global SUNAT.
 *
 * | Código | Descripción |
 * |--------|-------------|
 * | 00     | Descuento global por ítem |
 * | 01     | Descuento global por línea (post-MVP) |
 * | 02     | Descuento global por valor de venta (post-MVP) |
 * | 47     | Cargos globales (post-MVP) |
 */
const CATALOGO_53: Record<string, string> = {
  "00": "Descuento global por ítem",
};

interface DescuentoGlobal {
  id: string;
  esCargo: boolean;
  codigoMotivo: string;
  porcentaje: string;
  monto: string;
  montoBase: string;
}

export function DescuentosGlobalesSection() {
  const methods = useFormContext();

  // =================================================================
  // METHODS PATTERN — identical to ProductsTable.tsx lines 94-130
  // =================================================================
  const [discountsLocal, setDiscountsLocal] = useState<DescuentoGlobal[]>([]);

  const discounts = methods
    ? ((methods.watch("descuentosGlobales") as DescuentoGlobal[]) ?? [])
    : discountsLocal;

  const setDiscounts = (next: DescuentoGlobal[]) => {
    if (methods?.setValue) {
      methods.setValue("descuentosGlobales", next, {
        shouldDirty: true,
        shouldTouch: true,
      });
      return;
    }
    setDiscountsLocal(next);
  };

  // =================================================================
  // MONTO BASE — auto-calculado desde el valor venta de productos
  // =================================================================

  const watchedProducts = methods
    ? ((methods.watch("detalles") as any[]) ?? (methods.watch("products") as any[]) ?? [])
    : [];

  const totalBase = useMemo(
    () =>
      watchedProducts.reduce(
        (sum, p) => sum + (Number(p.valorVenta) || 0),
        0,
      ),
    [watchedProducts],
  );

  // =================================================================
  // ADD / REMOVE
  // =================================================================

  const addDiscount = () => {
    const newDiscount: DescuentoGlobal = {
      id: Date.now().toString(),
      esCargo: false,
      codigoMotivo: "00",
      porcentaje: "",
      monto: "",
      montoBase: String(totalBase),
    };
    setDiscounts([...discounts, newDiscount]);
  };

  const removeDiscount = (id: string) => {
    const filtered = discounts.filter((d) => d.id !== id);
    setDiscounts(filtered);
  };

  // =================================================================
  // SYNC montoBase y monto cuando cambian los productos o porcentaje
  // =================================================================

  useEffect(() => {
    if (discounts.length === 0) return;
    const synced = discounts.map((d) => {
      const pct = parseFloat(d.porcentaje) || 0;
      return {
        ...d,
        montoBase: String(totalBase),
        monto: String((totalBase * (pct / 100)).toFixed(2)),
      };
    });
    setDiscounts(synced);
  }, [totalBase]); // eslint-disable-line react-hooks/exhaustive-deps

  // =================================================================
  // UPDATE
  // =================================================================

  const updateDiscount = (id: string, updates: Partial<DescuentoGlobal>) => {
    const updated = discounts.map((d) => {
      if (d.id === id) {
        const merged = { ...d, ...updates };
        // Si cambió el porcentaje, recalcular monto desde el totalBase
        if (updates.porcentaje !== undefined) {
          const pct = parseFloat(merged.porcentaje) || 0;
          merged.monto = String((totalBase * (pct / 100)).toFixed(2));
        }
        return merged;
      }
      return d;
    });
    setDiscounts(updated);
  };

  // =================================================================
  // TOTAL
  // =================================================================

  const totalDescuento = discounts.reduce((sum, d) => {
    const pct = parseFloat(d.porcentaje) || 0;
    const monto = totalBase * (pct / 100);
    const esCargo = d.esCargo === true;
    // Only sum descuentos (esCargo == false). Cargos increase the total.
    return esCargo ? sum - monto : sum + monto;
  }, 0);

  const formatNumber = (n: number) =>
    new Intl.NumberFormat("es-PE", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(n);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold text-lg">Descuentos Globales</h3>
        <Button onClick={addDiscount} size="sm" variant="outline">
          <Plus className="size-4 mr-2" aria-hidden="true" />
          Agregar Descuento Global
        </Button>
      </div>

      <div className="border rounded-lg overflow-hidden">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="min-w-[180px]">
                  Código Motivo
                </TableHead>
                <TableHead className="w-[120px]">
                  % Descuento
                </TableHead>
                <TableHead className="w-[140px]">
                  Monto Descuento
                </TableHead>
                <TableHead className="w-[140px]">
                  Base
                </TableHead>
                <TableHead className="w-[60px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {discounts.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={5}
                    className="text-center text-slate-500 py-8"
                  >
                    No hay descuentos globales. Haga clic en
                    "Agregar Descuento Global" para comenzar.
                  </TableCell>
                </TableRow>
              ) : (
                discounts.map((discount) => (
                  <TableRow key={discount.id}>
                    <TableCell>
                      <Select
                        value={discount.codigoMotivo}
                        onValueChange={(value) =>
                          updateDiscount(discount.id, {
                            codigoMotivo: value,
                          })
                        }
                      >
                        <SelectTrigger className="h-9">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          {Object.entries(CATALOGO_53).map(
                            ([code, description]) => (
                              <SelectItem key={code} value={code}>
                                {code} - {description}
                              </SelectItem>
                            ),
                          )}
                        </SelectContent>
                      </Select>
                    </TableCell>
                    <TableCell>
                      <Input
                        value={discount.porcentaje}
                        onChange={(e) =>
                          updateDiscount(discount.id, {
                            porcentaje: e.target.value,
                          })
                        }
                        className="h-9 text-sm font-mono"
                        placeholder="0.00"
                        type="number"
                        step="0.01"
                        autoComplete="off"
                        inputMode="decimal"
                      />
                    </TableCell>
                    <TableCell>
                      <div className="font-mono text-sm font-semibold text-blue-700 bg-blue-50 px-2 py-1.5 rounded border border-blue-200 h-9 flex items-center">
                        {formatNumber(
                          totalBase *
                            ((parseFloat(discount.porcentaje) || 0) / 100),
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="font-mono text-sm text-slate-700 bg-slate-50 px-2 py-1.5 rounded border h-9 flex items-center">
                        {formatNumber(totalBase)}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                        onClick={() => removeDiscount(discount.id)}
                        aria-label="Eliminar descuento global"
                      >
                        <Trash2 className="size-4" aria-hidden="true" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </div>

      {discounts.length > 0 && (
        <div className="flex justify-end">
          <div className="bg-gradient-to-br from-slate-50 to-green-50 border-2 border-slate-300 rounded-lg p-4 min-w-[250px]">
            <div className="flex justify-between items-center">
              <span className="font-medium text-slate-700">
                Total Descuento Global:
              </span>
              <span className="font-mono font-bold text-lg text-green-700">
                {formatNumber(totalDescuento)}
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default DescuentosGlobalesSection;
