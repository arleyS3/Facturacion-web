# Operaciones Gravadas, Exoneradas e Inafectas

Explicación de los tipos de operaciones tributarias en facturas electrónicas SUNAT.

---

## El precio de un producto tiene "capas"

Imagina que vendes un celular en **S/ 1,000**:

| Capa | Descripción |
|------|-------------|
| **Valor de venta** | El precio base sin impuestos (S/ 847.46) |
| **IGV (18%)** | El impuesto que agregas (S/ 152.54) |
| **Precio final** | Lo que paga el cliente (S/ 1,000) |

> **Nota:** El IGV se calcula sobre el valor de venta, no sobre el precio final.  
> Si el valor de venta es S/ 847.46, el IGV = 847.46 × 18% = S/ 152.54

---

## Las 3 categorías de operaciones

### 1. Operaciones Gravadas (con IGV)

Es el caso **normal**. Vendes algo y le agregas IGV.

**Ejemplo:** Vendes un celular. El precio tiene IGV incluido.

```json
{
  "totalValorVentaGravadas": 847.46,
  "igv": 152.54
}
```

**Cuándo usarlo:** Cuando el producto o servicio está sujeto al IGV (la mayoría de ventas).

---

### 2. Operaciones Exoneradas (sin IGV - por ley)

El producto **no paga IGV** porque la ley lo exime, pero el precio sigue siendo el mismo.

**Ejemplo:** Vendes un libro. Los libros están exonerados del IGV por ley (Ley del IGV, Artículo 2).

```json
{
  "totalValorVentaExoneradas": 1000.00,
  "igv": 0
}
```

**Cuándo usarlo:** Productos/servicios exonerados por Ley (ej: libros, periódicos, productos agrícolas nativos).

---

### 3. Operaciones Inafectas (sin IGV - no corresponde)

El producto **nunca tuvo IGV** porque no aplica el impuesto (no es venta de bienes ni servicios gravables).

**Ejemplo:** Vendes un vehículo usado. La venta de usados no está afecta al IGV.

```json
{
  "totalValorVentaInafectas": 5000.00,
  "igv": 0
}
```

**Cuándo usarlo:** Operaciones que no son actividad gravable (ej: venta de usados, intereses por préstamos).

---

## Resumen visual

```
┌─────────────────────────────────────────────────┐
│           PRECIO QUE PAGA EL CLIENTE            │
├─────────────────────────────────────────────────┤
│  OPERACIÓN                                       │
│  ├─ Gravada → tiene IGV (18%)                 │
│  ├─ Exonerada → no tiene IGV por ley          │
│  └─ Inafecta → no tiene IGV (no aplica)       │
└─────────────────────────────────────────────────┘
```

---

## En el documento SUNAT

Cada categoría se suma por separado:

```
Subtotal gravado:     S/ 847.46
Subtotal exonerado:  S/ 500.00
Subtotal inafecto:   S/ 2,000.00
─────────────────────────────────
IGV (18%):           S/ 152.54
Total:               S/ 3,499.54
```

Esto le permite a SUNAT saber **qué tipo de operaciones** realizó la empresa y aplicar las reglas correctas.

---

## Referencias документа SUNAT

- **Campo 23:** Total valor de venta - operaciones gravadas
- **Campo 24:** Total valor de venta - operaciones exoneradas
- **Campo 25:** Total valor de venta - operaciones inafectas

> Estos campos solo se incluyen si hay al menos una línea de ítem en esa categoría.