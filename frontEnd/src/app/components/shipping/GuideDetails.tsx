import { Package, Plus, Trash2 } from "lucide-react";
import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../ui/select";
import { Button } from "../ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../ui/table";
import { useEffect, useState } from "react";
import { useCatalog } from "../../hooks/useCatalog";
import { useFormContext } from "react-hook-form";

interface Product {
  id: string;
  correlativo: number;
  descripcion: string;
  cantidad: string;
  unidadMedida: string;
}

export function GuideDetails() {
  const methods = useFormContext();
  const setValue = methods?.setValue;
  const getValues = methods?.getValues;

  const { data: unidadesMedida, loading: unidadesLoading } = useCatalog(
    "/catalogos/unidades-medida",
  );
  const [products, setProducts] = useState<Product[]>([]);
  const [cantidad, setCantidad] = useState("");
  const [nombre, setNombre] = useState("");
  const [unidadMedida, setUnidadMedida] = useState("NIU");

  useEffect(() => {
    const persisted = getValues?.("shippingItems") ?? [];
    if (!Array.isArray(persisted)) return;

    const hydrated: Product[] = persisted.map((item: any, index: number) => ({
      id: `persisted-${index}-${item?.NroLinDet ?? index + 1}`,
      correlativo: Number(item?.NroLinDet) || index + 1,
      descripcion: item?.NmbItem ?? "",
      cantidad: item?.QtyItem ?? "",
      unidadMedida: item?.UnmdItem ?? "NIU",
    }));

    setProducts(hydrated);
  }, [getValues]);

  const syncFormItems = (items: Product[]) => {
    if (!setValue) return;
    setValue(
      "shippingItems",
      items.map((p) => ({
        NroLinDet: String(p.correlativo),
        QtyItem: p.cantidad,
        UnmdItem: p.unidadMedida,
        NmbItem: p.descripcion,
      })),
    );
  };

  const addProduct = () => {
    if (!cantidad || !nombre) {
      alert("Complete todos los campos antes de agregar");
      return;
    }

    const newProduct: Product = {
      id: Date.now().toString(),
      correlativo: products.length + 1,
      descripcion: nombre,
      cantidad: cantidad,
      unidadMedida: unidadMedida
    };
    
    const updated = [...products, newProduct];
    setProducts(updated);
    syncFormItems(updated);
    
    // Limpiar campos
    setCantidad("");
    setNombre("");
    setUnidadMedida("NIU");
  };

  const removeProduct = (id: string) => {
    const filtered = products.filter(p => p.id !== id);
    // Reajustar correlativos
    const reindexed = filtered.map((p, index) => ({
      ...p,
      correlativo: index + 1
    }));
    setProducts(reindexed);
    syncFormItems(reindexed);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2 mb-6">
        <Package className="size-5 text-blue-600" />
        <h3 className="font-semibold text-lg">Detalle de Productos</h3>
      </div>

      {/* Formulario de entrada */}
      <div className="bg-slate-50 border border-slate-200 rounded-lg p-4">
        <div className="grid grid-cols-1 md:grid-cols-12 gap-4 items-end">
          <div className="space-y-2 md:col-span-2">
            <Label htmlFor="cantidad">Cantidad</Label>
            <Input 
              id="cantidad" 
              placeholder="0" 
              type="number"
              value={cantidad}
              onChange={(e) => setCantidad(e.target.value)}
            />
          </div>

          <div className="space-y-2 md:col-span-5">
            <Label htmlFor="nombre">Nombre</Label>
            <Input 
              id="nombre" 
              placeholder="Descripción del producto"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
            />
          </div>

          <div className="space-y-2 md:col-span-3">
            <Label htmlFor="unidad-medida">Unidad de Medida</Label>
            <Select value={unidadMedida} onValueChange={setUnidadMedida}>
              <SelectTrigger id="unidad-medida">
                <SelectValue placeholder={unidadesLoading ? "Cargando..." : "Seleccione unidad"} />
              </SelectTrigger>
              <SelectContent>
                {unidadesLoading ? (
                  <div className="p-3">Cargando...</div>
                ) : unidadesMedida && unidadesMedida.length ? (
                  unidadesMedida.map((item) => (
                    <SelectItem key={item.code} value={item.code}>
                      {item.label}
                    </SelectItem>
                  ))
                ) : (
                  <>
                    <SelectItem value="NIU">NIU - Unidades</SelectItem>
                    <SelectItem value="ZZ">ZZ - Servicios</SelectItem>
                    <SelectItem value="KGM">KGM - Kilogramos</SelectItem>
                    <SelectItem value="MTR">MTR - Metro</SelectItem>
                    <SelectItem value="LTR">LTR - Litro</SelectItem>
                    <SelectItem value="BX">BX - Caja</SelectItem>
                    <SelectItem value="PK">PK - Paquete</SelectItem>
                    <SelectItem value="DZN">DZN - Docena</SelectItem>
                  </>
                )}
              </SelectContent>
            </Select>
          </div>

          <div className="md:col-span-2">
            <Button onClick={addProduct} className="w-full">
              <Plus className="size-4 mr-2" />
              Agregar
            </Button>
          </div>
        </div>
      </div>

      {/* Tabla de productos */}
      <div className="border rounded-lg overflow-hidden bg-white">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-slate-50">
                <TableHead className="w-[100px]">Correlativo</TableHead>
                <TableHead className="min-w-[300px]">Descripción</TableHead>
                <TableHead className="w-[120px]">Cantidad</TableHead>
                <TableHead className="w-[180px]">Unidad Medida</TableHead>
                <TableHead className="w-[60px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {products.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="text-center text-slate-500 py-12">
                    No hay productos agregados. Complete los campos superiores y presione "Agregar".
                  </TableCell>
                </TableRow>
              ) : (
                products.map((product) => (
                  <TableRow key={product.id}>
                    <TableCell className="font-medium text-slate-600">
                      {product.correlativo}
                    </TableCell>
                    <TableCell>{product.descripcion}</TableCell>
                    <TableCell className="text-right">{product.cantidad}</TableCell>
                    <TableCell>{product.unidadMedida}</TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50"
                        onClick={() => removeProduct(product.id)}
                      >
                        <Trash2 className="size-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </div>
    </div>
  );
}