import {
  Plus,
  Trash2,
  ChevronDown,
  ChevronUp,
  Settings2,
  Info,
} from "lucide-react";
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
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { useState, Fragment, useEffect } from "react";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { useCatalog } from "@/hooks/useCatalog";
import { useFormContext } from "react-hook-form";

interface ISCData {
  codigoISC: string;
  codigoTipoISC: string;
  tasa: string;
  montoISC: number;
}

interface Product {
  id: string;
  correlativo: number;
  codigo: string;
  descripcion: string;
  cantidad: string;
  unidadMedida: string;
  precioUnitario: string;

  // Campos calculados
  valorVenta: number;

  // Descuento
  tieneDescuento: boolean;
  descuentoPorItem: string;
  descuentoMonto: number;

  // IGV
  tipoAfectacionIGV: string;
  codigoTributo: string;
  tasaIGV: string;
  impuestoIGV: number;

  // ISC
  tieneISC: boolean;
  iscData: ISCData | null;

  // Total final
  total: number;
}

export function ProductsTable() {
  const methods = useFormContext();
  const { data: unidadesMedida } = useCatalog(
    "/catalogos/unidades-medida",
  );
  const { data: tiposAfectacionIgv } = useCatalog(
    "/catalogos/tipos-afectacion-igv",
  );
  const { data: codigosTipoTributo } = useCatalog(
    "/catalogos/codigos-tipo-tributo",
  );

  // =================================================================
  // CAMPOS NUEVOS EN ESPAÑOL + LEGACY
  // =================================================================
  const [productsLocal, setProductsLocal] = useState<Product[]>([]);
  
  // Primero busca los nuevos nombres en español, luego los legacy
  const products = methods
    ? ((methods.watch("detalles") as Product[]) ?? (methods.watch("products") as Product[]) ?? [])
    : productsLocal;

  const setProducts = (next: Product[]) => {
    if (methods?.setValue) {
      // Nuevo nombre en español
      methods.setValue("detalles", next, {
        shouldDirty: true,
        shouldTouch: true,
      });
      // Legacy (para buildPayload)
      methods.setValue("products", next, {
        shouldDirty: true,
        shouldTouch: true,
      });
      return;
    }
    setProductsLocal(next);
  };

  useEffect(() => {
    if (!methods?.getValues || !methods?.setValue) return;
    // Primero verifica los nuevos nombres, luego los legacy
    const current = methods.getValues("detalles") || methods.getValues("products");
    if (!Array.isArray(current)) {
      methods.setValue("detalles", []);
      methods.setValue("products", []);
    }
  }, [methods]);

  const [expandedRows, setExpandedRows] = useState<Set<string>>(
    new Set(),
  );
  const [iscModalOpen, setIscModalOpen] = useState(false);
  const [currentISCProduct, setCurrentISCProduct] = useState<
    string | null
  >(null);
  const [tempISCData, setTempISCData] = useState<ISCData>({
    codigoISC: "",
    codigoTipoISC: "",
    tasa: "",
    montoISC: 0,
  });

  const addProduct = () => {
    const newProduct: Product = {
      id: Date.now().toString(),
      correlativo: products.length + 1,
      codigo: "",
      descripcion: "",
      cantidad: "",
      unidadMedida: "NIU",
      precioUnitario: "",
      valorVenta: 0,
      tieneDescuento: false,
      descuentoPorItem: "",
      descuentoMonto: 0,
      tipoAfectacionIGV: "10",
      codigoTributo: "1000",
      tasaIGV: "18",
      impuestoIGV: 0,
      tieneISC: false,
      iscData: null,
      total: 0,
    };
    setProducts([...products, newProduct]);
  };

  const removeProduct = (id: string) => {
    const product = products.find((p) => p.id === id);
    if (!product) return;
    if (!window.confirm(`¿Eliminar producto "${product.descripcion || `#${product.correlativo}`}"?`)) return;
    const filtered = products.filter((p) => p.id !== id);
    // Recalcular correlativos
    const reindexed = filtered.map((p, index) => ({
      ...p,
      correlativo: index + 1,
    }));
    setProducts(reindexed);
    setExpandedRows((prev) => {
      const newSet = new Set(prev);
      newSet.delete(id);
      return newSet;
    });
  };

  const toggleRow = (id: string) => {
    setExpandedRows((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(id)) {
        newSet.delete(id);
      } else {
        newSet.add(id);
      }
      return newSet;
    });
  };

  const calculateProduct = (product: Product): Product => {
    const cantidad = parseFloat(product.cantidad) || 0;
    const precioUnitario =
      parseFloat(product.precioUnitario) || 0;

    // Valor Venta
    const valorVenta = cantidad * precioUnitario;

    // Descuento
    let descuentoMonto = 0;
    if (product.tieneDescuento) {
      const descuentoPorItem =
        parseFloat(product.descuentoPorItem) || 0;
      descuentoMonto = descuentoPorItem * cantidad;
    }

    // Base imponible (después de descuento)
    const baseImponible = valorVenta - descuentoMonto;

    // Impuesto IGV
    let impuestoIGV = 0;
    if (product.codigoTributo === "1000") {
      // IGV
      const tasaIGV = parseFloat(product.tasaIGV) || 0;
      impuestoIGV = baseImponible * (tasaIGV / 100);
    }

    // ISC
    let montoISC = 0;
    if (product.tieneISC && product.iscData) {
      const tasaISC = parseFloat(product.iscData.tasa) || 0;
      montoISC = valorVenta * (tasaISC / 100);
    }

    // Total
    const total = baseImponible + impuestoIGV + montoISC;

    return {
      ...product,
      valorVenta,
      descuentoMonto,
      impuestoIGV,
      iscData: product.iscData
        ? { ...product.iscData, montoISC }
        : null,
      total,
    };
  };

  const updateProduct = (
    id: string,
    updates: Partial<Product>,
  ) => {
    const updated = products.map((p) => {
      if (p.id === id) {
        const merged = { ...p, ...updates };
        return calculateProduct(merged);
      }
      return p;
    });
    setProducts(updated);
  };

  const openISCModal = (productId: string) => {
    const product = products.find((p) => p.id === productId);
    if (product?.iscData) {
      setTempISCData(product.iscData);
    } else {
      setTempISCData({
        codigoISC: "",
        codigoTipoISC: "",
        tasa: "",
        montoISC: 0,
      });
    }
    setCurrentISCProduct(productId);
    setIscModalOpen(true);
  };

  const saveISCData = () => {
    if (currentISCProduct) {
      updateProduct(currentISCProduct, {
        iscData: tempISCData,
      });
    }
    setIscModalOpen(false);
    setCurrentISCProduct(null);
  };

  const calculateTotals = () => {
    const subtotal = products.reduce(
      (sum, p) => sum + p.valorVenta,
      0,
    );
    const descuentoTotal = products.reduce(
      (sum, p) => sum + p.descuentoMonto,
      0,
    );
    const igvTotal = products.reduce(
      (sum, p) => sum + p.impuestoIGV,
      0,
    );
    const iscTotal = products.reduce(
      (sum, p) => sum + (p.iscData?.montoISC || 0),
      0,
    );
    const total = products.reduce((sum, p) => sum + p.total, 0);

    return {
      subtotal,
      descuentoTotal,
      igvTotal,
      iscTotal,
      total,
    };
  };

  const totals = calculateTotals();

  const formatNumber = (n: number) =>
    new Intl.NumberFormat("es-PE", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(n);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold text-lg">
          Productos / Servicios
        </h3>
        <Button onClick={addProduct} size="sm">
          <Plus className="size-4 mr-2" aria-hidden="true" />
          Agregar Producto
        </Button>
      </div>

      <div className="border rounded-lg overflow-hidden">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-[50px]">#</TableHead>
                <TableHead className="w-[120px]">
                  Código
                </TableHead>
                <TableHead className="min-w-[200px]">
                  Descripción
                </TableHead>
                <TableHead className="w-[100px]">
                  Cant.
                </TableHead>
                <TableHead className="w-[130px]">
                  Und. Medida
                </TableHead>
                <TableHead className="w-[110px]">
                  P.U.
                </TableHead>
                <TableHead className="w-[110px]">
                  Valor Venta
                </TableHead>
                <TableHead className="w-[110px]">
                  Total
                </TableHead>
                <TableHead className="w-[100px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {products.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={9}
                    className="text-center text-slate-500 py-12"
                  >
                    No hay productos agregados. Haga clic en
                    "Agregar Producto" para comenzar.
                  </TableCell>
                </TableRow>
              ) : (
                products.map((product) => (
                  <Fragment key={product.id}>
                    {/* Fila principal */}
                    <TableRow
                      className={
                        expandedRows.has(product.id)
                          ? "bg-blue-50/50"
                          : ""
                      }
                    >
                      <TableCell className="text-center font-medium text-slate-600">
                        {product.correlativo}
                      </TableCell>
                      <TableCell>
                        <Input
                          value={product.codigo}
                          onChange={(e) =>
                            updateProduct(product.id, {
                              codigo: e.target.value,
                            })
                          }
                          className="h-9 text-sm"
                          placeholder="Código"
                        />
                      </TableCell>
                      <TableCell>
                        <Input
                          value={product.descripcion}
                          onChange={(e) =>
                            updateProduct(product.id, {
                              descripcion: e.target.value,
                            })
                          }
                          className="h-9 text-sm"
                          placeholder="Descripción del producto"
                        />
                      </TableCell>
                      <TableCell>
                        <Input
                          value={product.cantidad}
                          onChange={(e) =>
                            updateProduct(product.id, {
                              cantidad: e.target.value,
                            })
                          }
                          className="h-9 text-sm"
                          placeholder="0"
                          type="number"
                          step="0.01"
                          autoComplete="off"
                          inputMode="decimal"
                        />
                      </TableCell>
                      <TableCell>
                        <Select
                          value={product.unidadMedida}
                          onValueChange={(value) =>
                            updateProduct(product.id, {
                              unidadMedida: value,
                            })
                          }
                        >
                          <SelectTrigger className="h-9">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            {unidadesMedida?.map((item) => (
                              <SelectItem
                                key={item.code}
                                value={item.code}
                              >
                                {item.label}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </TableCell>
                      <TableCell>
                        <Input
                          value={product.precioUnitario}
                          onChange={(e) =>
                            updateProduct(product.id, {
                              precioUnitario: e.target.value,
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
                        <div className="font-mono text-sm text-slate-700 bg-slate-50 px-2 py-1.5 rounded border">
                          {formatNumber(product.valorVenta)}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="font-mono text-sm font-semibold text-blue-700 bg-blue-50 px-2 py-1.5 rounded border border-blue-200">
                          {formatNumber(product.total)}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-1">
                          <Button
                            variant="ghost"
                            size="sm"
                            className="h-8 w-8 p-0 text-blue-600 hover:text-blue-700 hover:bg-blue-50"
                            onClick={() =>
                              toggleRow(product.id)
                            }
                            aria-label={expandedRows.has(product.id) ? "Colapsar fila" : "Expandir fila"}
                          >
                            {expandedRows.has(product.id) ? (
                              <ChevronUp className="size-4" aria-hidden="true" />
                            ) : (
                              <ChevronDown className="size-4" aria-hidden="true" />
                            )}
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                            onClick={() =>
                              removeProduct(product.id)
                            }
                            aria-label={`Eliminar producto ${product.correlativo}`}
                          >
                            <Trash2 className="size-4" aria-hidden="true" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>

                    {/* Fila expandida */}
                    {expandedRows.has(product.id) && (
                      <TableRow className="bg-gradient-to-r from-blue-50/50 to-slate-50/50">
                        <TableCell colSpan={9} className="p-0">
                          <div className="p-6 space-y-6">
                            {/* Sección Descuento */}
                            <div className="bg-white border-2 border-orange-200 rounded-lg p-4 space-y-4">
                              <div className="flex items-center gap-2 mb-3">
                                <div className="size-2 rounded-full bg-orange-500" />
                                <h4 className="font-semibold text-orange-900">
                                  Descuento
                                </h4>
                              </div>

                              <div className="flex items-start gap-3">
                                <Checkbox
                                  id={`desc-${product.id}`}
                                  checked={
                                    product.tieneDescuento
                                  }
                                  onCheckedChange={(checked) =>
                                    updateProduct(product.id, {
                                      tieneDescuento:
                                        checked as boolean,
                                      descuentoPorItem: checked
                                        ? product.descuentoPorItem
                                        : "",
                                    })
                                  }
                                  className="mt-1"
                                />
                                <div className="flex-1">
                                  <Label
                                    htmlFor={`desc-${product.id}`}
                                    className="font-medium cursor-pointer"
                                  >
                                    Aplicar descuento a este
                                    producto
                                  </Label>
                                  <p className="text-xs text-slate-500 mt-1">
                                    El descuento se multiplicará
                                    por la cantidad
                                  </p>
                                </div>
                              </div>

                              {product.tieneDescuento && (
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
                                  <div className="space-y-2">
                                    <Label className="text-xs">
                                      Descuento por ítem
                                    </Label>
                                    <Input
                                      value={
                                        product.descuentoPorItem
                                      }
                                      onChange={(e) =>
                                        updateProduct(
                                          product.id,
                                          {
                                            descuentoPorItem:
                                              e.target.value,
                                          },
                                        )
                                      }
                                      className="font-mono"
                                      placeholder="0.00"
                                      type="number"
                                      step="0.01"
                                      autoComplete="off"
                                      inputMode="decimal"
                                    />
                                  </div>
                                  <div className="space-y-2">
                                    <Label className="text-xs text-slate-600">
                                      Cantidad
                                    </Label>
                                    <div className="font-mono text-sm px-3 py-2 bg-slate-100 rounded border">
                                      {product.cantidad || "0"}
                                    </div>
                                  </div>
                                  <div className="space-y-2">
                                    <Label className="text-xs font-semibold text-orange-700">
                                      Descuento Total
                                    </Label>
                                    <div className="font-mono text-sm font-bold px-3 py-2 bg-orange-100 text-orange-700 rounded border border-orange-300">
                                      {formatNumber(product.descuentoMonto)}
                                    </div>
                                  </div>
                                </div>
                              )}
                            </div>

                            {/* Sección IGV */}
                            <div className="bg-white border-2 border-blue-200 rounded-lg p-4 space-y-4">
                              <div className="flex items-center gap-2 mb-3">
                                <div className="size-2 rounded-full bg-blue-500" />
                                <h4 className="font-semibold text-blue-900">
                                  Tributo - IGV
                                </h4>
                              </div>

                              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                                <div className="space-y-2">
                                  <Label className="text-xs">
                                    Tipo Afectación IGV
                                  </Label>
                                  <Select
                                    value={
                                      product.tipoAfectacionIGV
                                    }
                                    onValueChange={(value) =>
                                      updateProduct(
                                        product.id,
                                        {
                                          tipoAfectacionIGV:
                                            value,
                                        },
                                      )
                                    }
                                  >
                                    <SelectTrigger>
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      {tiposAfectacionIgv?.map(
                                        (item) => (
                                          <SelectItem
                                            key={item.code}
                                            value={item.code}
                                          >
                                            {item.label}
                                          </SelectItem>
                                        ),
                                      )}
                                    </SelectContent>
                                  </Select>
                                </div>

                                <div className="space-y-2">
                                  <Label className="text-xs">
                                    Código de Tributo
                                  </Label>
                                  <Select
                                    value={
                                      product.codigoTributo
                                    }
                                    onValueChange={(value) => {
                                      // Cuando se selecciona IGV, auto-llenar tasa al 18%
                                      if (value === "1000") {
                                        updateProduct(
                                          product.id,
                                          {
                                            codigoTributo:
                                              value,
                                            tasaIGV: "18",
                                          },
                                        );
                                      } else {
                                        updateProduct(
                                          product.id,
                                          {
                                            codigoTributo:
                                              value,
                                            tasaIGV: "0",
                                          },
                                        );
                                      }
                                    }}
                                  >
                                    <SelectTrigger>
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      {codigosTipoTributo?.map(
                                        (item) => (
                                          <SelectItem
                                            key={item.code}
                                            value={item.code}
                                          >
                                            {item.label}
                                          </SelectItem>
                                        ),
                                      )}
                                    </SelectContent>
                                  </Select>
                                </div>

                                <div className="space-y-2">
                                  <div className="flex items-center gap-1">
                                    <Label className="text-xs">
                                      Tasa IGV (%)
                                    </Label>
                                    <Tooltip>
                                      <TooltipTrigger asChild>
                                        <button
                                          type="button"
                                          className="text-slate-400"
                                          aria-label="Información sobre tasa IGV"
                                        >
                                          <Info className="size-3" aria-hidden="true" />
                                        </button>
                                      </TooltipTrigger>
                                      <TooltipContent>
                                        Se llena automáticamente
                                        al seleccionar IGV
                                      </TooltipContent>
                                    </Tooltip>
                                  </div>
                                  <Input
                                    value={product.tasaIGV}
                                    onChange={(e) =>
                                      updateProduct(
                                        product.id,
                                        {
                                          tasaIGV:
                                            e.target.value,
                                        },
                                      )
                                    }
                                    className="font-mono"
                                    placeholder="0"
                                    type="number"
                                    step="0.01"
                                    autoComplete="off"
                                    inputMode="decimal"
                                    disabled={
                                      product.codigoTributo !==
                                      "1000"
                                    }
                                  />
                                </div>

                                <div className="space-y-2">
                                  <Label className="text-xs font-semibold text-blue-700">
                                    Impuesto IGV
                                  </Label>
                                  <div className="font-mono text-sm font-bold px-3 py-2 bg-blue-100 text-blue-700 rounded border border-blue-300">
                                      {formatNumber(product.impuestoIGV)}
                                    </div>
                                </div>
                              </div>
                            </div>

                            {/* Sección ISC */}
                            <div className="bg-white border-2 border-purple-200 rounded-lg p-4 space-y-4">
                              <div className="flex items-center justify-between">
                                <div className="flex items-center gap-2">
                                  <div className="size-2 rounded-full bg-purple-500" />
                                  <h4 className="font-semibold text-purple-900">
                                    ISC - Impuesto Selectivo al
                                    Consumo
                                  </h4>
                                </div>
                                {product.tieneISC &&
                                  product.iscData && (
                                    <div className="text-sm font-mono font-bold text-purple-700 bg-purple-100 px-3 py-1 rounded">
                                      Monto ISC:{" "}
                                      {formatNumber(product.iscData.montoISC)}
                                    </div>
                                  )}
                              </div>

                              <div className="flex items-start gap-3">
                                <Checkbox
                                  id={`isc-${product.id}`}
                                  checked={product.tieneISC}
                                  onCheckedChange={(
                                    checked,
                                  ) => {
                                    if (checked) {
                                      openISCModal(product.id);
                                      updateProduct(
                                        product.id,
                                        { tieneISC: true },
                                      );
                                    } else {
                                      updateProduct(
                                        product.id,
                                        {
                                          tieneISC: false,
                                          iscData: null,
                                        },
                                      );
                                    }
                                  }}
                                  className="mt-1"
                                />
                                <div className="flex-1">
                                  <Label
                                    htmlFor={`isc-${product.id}`}
                                    className="font-medium cursor-pointer"
                                  >
                                    Este producto está afecto a
                                    ISC
                                  </Label>
                                  <p className="text-xs text-slate-500 mt-1">
                                    Aplica a productos como
                                    bebidas alcohólicas,
                                    cigarrillos, combustibles,
                                    etc.
                                  </p>
                                </div>
                              </div>

                              {product.tieneISC && (
                                <div className="pt-2">
                                  <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() =>
                                      openISCModal(product.id)
                                    }
                                    className="border-purple-300 text-purple-700 hover:bg-purple-50"
                                  >
                                    <Settings2 className="size-4 mr-2" aria-hidden="true" />
                                    Configurar detalles de ISC
                                  </Button>

                                  {product.iscData && (
                                    <div className="mt-3 grid grid-cols-2 md:grid-cols-4 gap-3 text-xs">
                                      <div>
                                        <div className="text-slate-500 mb-1">
                                          Código ISC
                                        </div>
                                        <div className="font-medium">
                                          {product.iscData
                                            .codigoISC || "-"}
                                        </div>
                                      </div>
                                      <div>
                                        <div className="text-slate-500 mb-1">
                                          Tipo Sistema
                                        </div>
                                        <div className="font-medium">
                                          {product.iscData
                                            .codigoTipoISC ||
                                            "-"}
                                        </div>
                                      </div>
                                      <div>
                                        <div className="text-slate-500 mb-1">
                                          Tasa
                                        </div>
                                        <div className="font-medium">
                                          {product.iscData.tasa
                                            ? `${product.iscData.tasa}%`
                                            : "-"}
                                        </div>
                                      </div>
                                      <div>
                                        <div className="text-slate-500 mb-1">
                                          Monto Base
                                        </div>
                                        <div className="font-medium font-mono">
                                          {formatNumber(product.valorVenta)}
                                        </div>
                                      </div>
                                    </div>
                                  )}
                                </div>
                              )}
                            </div>
                          </div>
                        </TableCell>
                      </TableRow>
                    )}
                  </Fragment>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </div>

      {/* Totales */}
      <div className="bg-gradient-to-br from-slate-50 to-blue-50 border-2 border-slate-300 rounded-lg p-6">
        <div className="space-y-3 max-w-md ml-auto">
          <div className="flex justify-between items-center text-sm">
            <span className="text-slate-600">
              Subtotal (Valor Venta):
            </span>
            <span className="font-mono font-medium">
              {formatNumber(totals.subtotal)}
            </span>
          </div>
          {totals.descuentoTotal > 0 && (
            <div className="flex justify-between items-center text-sm">
              <span className="text-orange-700">
                Total Descuentos:
              </span>
              <span className="font-mono font-medium text-orange-700">
                -{formatNumber(totals.descuentoTotal)}
              </span>
            </div>
          )}
          <div className="flex justify-between items-center text-sm">
            <span className="text-slate-600">
              Total IGV (18%):
            </span>
            <span className="font-mono font-medium">
              {formatNumber(totals.igvTotal)}
            </span>
          </div>
          {totals.iscTotal > 0 && (
            <div className="flex justify-between items-center text-sm">
              <span className="text-purple-700">
                Total ISC:
              </span>
              <span className="font-mono font-medium text-purple-700">
                {formatNumber(totals.iscTotal)}
              </span>
            </div>
          )}
          <div className="border-t-2 border-slate-300 pt-3 flex justify-between items-center">
            <span className="font-bold text-lg">
              Total a Pagar:
            </span>
            <span className="font-mono font-bold text-2xl text-blue-700">
              {formatNumber(totals.total)}
            </span>
          </div>
        </div>
      </div>

      {/* Modal ISC */}
      <Dialog
        open={iscModalOpen}
        onOpenChange={setIscModalOpen}
      >
        <DialogContent className="max-w-2xl" style={{ overscrollBehavior: "contain" }}>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <div className="size-8 rounded-lg bg-purple-100 flex items-center justify-center">
                <Settings2 className="size-5 text-purple-700" aria-hidden="true" />
              </div>
              Configuración de ISC
            </DialogTitle>
            <DialogDescription>
              Configure los detalles del Impuesto Selectivo al
              Consumo para este producto
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="isc-codigo">Código ISC</Label>
                <Select
                  value={tempISCData.codigoISC}
                  onValueChange={(value) =>
                    setTempISCData({
                      ...tempISCData,
                      codigoISC: value,
                    })
                  }
                >
                  <SelectTrigger id="isc-codigo">
                    <SelectValue placeholder="Seleccione código" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="2000">
                      2000 - ISC
                    </SelectItem>
                    <SelectItem value="2001">
                      2001 - Arroz pilado
                    </SelectItem>
                    <SelectItem value="2002">
                      2002 - Bebidas alcohólicas
                    </SelectItem>
                    <SelectItem value="2003">
                      2003 - Cigarrillos
                    </SelectItem>
                    <SelectItem value="2004">
                      2004 - Combustibles
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="isc-sistema">Sistema de ISC</Label>
                <Select
                  value={tempISCData.codigoTipoISC}
                  onValueChange={(value) =>
                    setTempISCData({
                      ...tempISCData,
                      codigoTipoISC: value,
                    })
                  }
                >
                  <SelectTrigger id="isc-sistema">
                    <SelectValue placeholder="Seleccione sistema" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="01">
                      01 - Sistema al Valor
                    </SelectItem>
                    <SelectItem value="02">
                      02 - Sistema Específico
                    </SelectItem>
                    <SelectItem value="03">
                      03 - Sistema al Valor (Precio de Venta al
                      Público)
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="isc-tasa">Tasa ISC (%)</Label>
                <Select
                  value={tempISCData.tasa}
                  onValueChange={(value) =>
                    setTempISCData({
                      ...tempISCData,
                      tasa: value,
                    })
                  }
                >
                  <SelectTrigger id="isc-tasa">
                    <SelectValue placeholder="Seleccione tasa" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="10">10%</SelectItem>
                    <SelectItem value="20">20%</SelectItem>
                    <SelectItem value="25">25%</SelectItem>
                    <SelectItem value="30">30%</SelectItem>
                    <SelectItem value="40">40%</SelectItem>
                    <SelectItem value="50">50%</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label className="text-slate-600">
                  Monto ISC (Calculado)
                </Label>
                <div className="font-mono text-lg font-bold px-3 py-2 bg-purple-100 text-purple-700 rounded border border-purple-300">
                  {(() => {
                    const product = products.find(
                      (p) => p.id === currentISCProduct,
                    );
                    if (product && tempISCData.tasa) {
                      const tasa =
                        parseFloat(tempISCData.tasa) || 0;
                      const monto =
                        product.valorVenta * (tasa / 100);
                      return formatNumber(monto);
                    }
                    return formatNumber(0);
                  })()}
                </div>
              </div>
            </div>

            <div className="bg-purple-50 border border-purple-200 rounded-lg p-3 text-sm text-purple-900">
              <div className="font-semibold mb-1">
                ℹ️ Información
              </div>
              <p>
                El monto ISC se calcula sobre el valor venta del
                producto (Cantidad × Precio Unitario) aplicando
                la tasa seleccionada.
              </p>
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIscModalOpen(false)}
            >
              Cancelar
            </Button>
            <Button
              onClick={saveISCData}
              className="bg-purple-600 hover:bg-purple-700"
            >
              Guardar Configuración
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
