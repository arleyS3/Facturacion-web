# 🎨 Sistema de Diseño — AutonomiFlow | Facturación Web

> **Proyecto:** Facturación Web (Perú — SUNAT)
> **Stack:** React 18 + Vite + Tailwind CSS 4 + MUI 7 + Radix UI
> **Brand:** AutonomiFlow
> **Versión:** 1.0.0

---

## Índice

1. [Filosofía & Brand](#1-filosofía--brand)
2. [Paleta de Color — Light Mode](#2-paleta-de-color--light-mode)
3. [Paleta de Color — Dark Mode](#3-paleta-de-color--dark-mode)
4. [Tokens Semánticos (CSS Custom Properties)](#4-tokens-semánticos-css-custom-properties)
5. [Tipografía](#5-tipografía)
6. [Spacing & Sizing](#6-spacing--sizing)
7. [Border Radius & Sombras](#7-border-radius--sombras)
8. [Colores de Estado (Status)](#8-colores-de-estado-status)
9. [Colores para Charts](#9-colores-para-charts)
10. [Iconos](#10-iconos)
11. [Integración con MUI 7 Theme](#11-integración-con-mui-7-theme)
12. [Reglas de Uso & Anti-Patrones](#12-reglas-de-uso--anti-patrones)

---

## 1. Filosofía & Brand

### El concepto

AutonomiFlow es una plataforma de facturación electrónica peruana. La identidad visual combina:

- **Confianza institucional** → El azul profundo (`#07283A`) transmite solidez, seguridad, seriedad fiscal.
- **Acción moderna** → El azul vibrante (`#155dfc`) para interacciones: botones, links, CTA. Tecnología, no burocracia.
- **Energía controlada** → El cian (`#00B8D9`) como acento de información y highlights. Da vida sin ser estridente.
- **Éxito financiero** → El verde menta (`#6FD3AF`) para estados de pago, completado, entregado. Flujo de caja positivo.

### Paleta del logo (referencia)

| Color | Hex | Rol |
|-------|-----|-----|
| Azul profundo | `#07283A` | Texto principal del logo, fondo dark mode |
| Cian brillante | `#00B8D9` | Parte superior del ícono |
| Cian medio | `#1AAFC6` | Gradiente medio del ícono |
| Verde menta | `#6FD3AF` | Paloma/tick de verificación |
| Verde-azulado claro | `#4AC8D6` | Transición cian→menta |
| Azul sombra | `#063147` | Sombra/detalle del símbolo |

---

## 2. Paleta de Color — Light Mode

### Brand Colors (raw)

| Token | Hex | Oklch | RGB | Uso |
|-------|-----|-------|-----|-----|
| `brand-navy` | `#07283A` | `oklch(19.8% 0.038 248)` | `rgb(7, 40, 58)` | Fondo dark, sidebar, logo |
| `brand-cyan` | `#00B8D9` | `oklch(68.5% 0.173 215)` | `rgb(0, 184, 217)` | Acento, info badges |
| `brand-cyan-medium` | `#1AAFC6` | `oklch(65.2% 0.138 210)` | `rgb(26, 175, 198)` | Gradientes |
| `brand-cyan-light` | `#4AC8D6` | `oklch(72.1% 0.112 208)` | `rgb(74, 200, 214)` | Transiciones |
| `brand-mint` | `#6FD3AF` | `oklch(78.5% 0.122 167)` | `rgb(111, 211, 175)` | Success, pagado |
| `brand-shadow` | `#063147` | `oklch(17.5% 0.032 250)` | `rgb(6, 49, 71)` | Sombras del logo |

### Semantic Colors (Light)

| Token | Hex | Oklch | RGB |
|-------|-----|-------|-----|
| `--background` | `#F9FAFB` | `oklch(97.9% 0.004 264)` | `rgb(249, 250, 251)` |
| `--foreground` | `#0F172A` | `oklch(14.5% 0.026 264)` | `rgb(15, 23, 42)` |
| `--card` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--card-foreground` | `#0F172A` | `oklch(14.5% 0.026 264)` | `rgb(15, 23, 42)` |
| `--popover` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--popover-foreground` | `#0F172A` | `oklch(14.5% 0.026 264)` | `rgb(15, 23, 42)` |
| `--primary` | `#155dfc` | `oklch(54.6% 0.245 263)` | `rgb(21, 93, 252)` |
| `--primary-foreground` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--primary-hover` | `#1d4ed8` | `oklch(46.2% 0.228 262)` | `rgb(29, 78, 216)` |
| `--primary-soft` | `#EEF2FF` | `oklch(94.5% 0.028 264)` | `rgb(238, 242, 255)` |
| `--secondary` | `#F0F7FA` | `oklch(95.5% 0.014 210)` | `rgb(240, 247, 250)` |
| `--secondary-foreground` | `#07283A` | `oklch(19.8% 0.038 248)` | `rgb(7, 40, 58)` |
| `--muted` | `#F1F5F9` | `oklch(95.1% 0.008 264)` | `rgb(241, 245, 249)` |
| `--muted-foreground` | `#64748B` | `oklch(55.6% 0.041 264)` | `rgb(100, 116, 139)` |
| `--accent` | `#00B8D9` | `oklch(68.5% 0.173 215)` | `rgb(0, 184, 217)` |
| `--accent-foreground` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--accent-soft` | `#ECFEFF` | `oklch(96.5% 0.032 210)` | `rgb(236, 254, 255)` |
| `--destructive` | `#DC2626` | `oklch(49.5% 0.196 28)` | `rgb(220, 38, 38)` |
| `--destructive-foreground` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--success` | `#6FD3AF` | `oklch(78.5% 0.122 167)` | `rgb(111, 211, 175)` |
| `--success-foreground` | `#065F46` | `oklch(45.2% 0.138 168)` | `rgb(6, 95, 70)` |
| `--warning` | `#F59E0B` | `oklch(72.5% 0.164 78)` | `rgb(245, 158, 11)` |
| `--warning-foreground` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--info` | `#00B8D9` | `oklch(68.5% 0.173 215)` | `rgb(0, 184, 217)` |
| `--info-foreground` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--border` | `#E2E8F0` | `oklch(89.2% 0.012 264)` | `rgb(226, 232, 240)` |
| `--input` | `#E2E8F0` | `oklch(89.2% 0.012 264)` | `rgb(226, 232, 240)` |
| `--input-background` | `#F8FAFC` | `oklch(97.2% 0.004 264)` | `rgb(248, 250, 252)` |
| `--ring` | `#155dfc` | `oklch(54.6% 0.245 263)` | `rgb(21, 93, 252)` |
| `--radius` | `0.625rem` | — | `10px` |
| `--sidebar` | `#F8FAFC` | `oklch(97.2% 0.004 264)` | `rgb(248, 250, 252)` |
| `--sidebar-foreground` | `#0F172A` | `oklch(14.5% 0.026 264)` | `rgb(15, 23, 42)` |
| `--sidebar-primary` | `#155dfc` | `oklch(54.6% 0.245 263)` | `rgb(21, 93, 252)` |
| `--sidebar-primary-foreground` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--sidebar-accent` | `#F0F7FA` | `oklch(95.5% 0.014 210)` | `rgb(240, 247, 250)` |
| `--sidebar-accent-foreground` | `#07283A` | `oklch(19.8% 0.038 248)` | `rgb(7, 40, 58)` |
| `--sidebar-border` | `#E2E8F0` | `oklch(89.2% 0.012 264)` | `rgb(226, 232, 240)` |
| `--sidebar-ring` | `#155dfc` | `oklch(54.6% 0.245 263)` | `rgb(21, 93, 252)` |

---

## 3. Paleta de Color — Dark Mode

### Semantic Colors (Dark)

| Token | Hex | Oklch | RGB |
|-------|-----|-------|-----|
| `--background` | `#07283A` | `oklch(19.8% 0.038 248)` | `rgb(7, 40, 58)` |
| `--foreground` | `#F1F5F9` | `oklch(92.1% 0.009 264)` | `rgb(241, 245, 249)` |
| `--card` | `#0A2E42` | `oklch(24.5% 0.039 248)` | `rgb(10, 46, 66)` |
| `--card-foreground` | `#F1F5F9` | `oklch(92.1% 0.009 264)` | `rgb(241, 245, 249)` |
| `--popover` | `#0A2E42` | `oklch(24.5% 0.039 248)` | `rgb(10, 46, 66)` |
| `--popover-foreground` | `#F1F5F9` | `oklch(92.1% 0.009 264)` | `rgb(241, 245, 249)` |
| `--primary` | `#3B82F6` | `oklch(62.3% 0.196 263)` | `rgb(59, 130, 246)` |
| `--primary-foreground` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--primary-hover` | `#60A5FA` | `oklch(70.2% 0.155 263)` | `rgb(96, 165, 250)` |
| `--primary-soft` | `#1E3A5F` | `oklch(30.2% 0.047 264)` | `rgb(30, 58, 95)` |
| `--secondary` | `#1A3F52` | `oklch(32.5% 0.038 240)` | `rgb(26, 63, 82)` |
| `--secondary-foreground` | `#E2E8F0` | `oklch(89.2% 0.012 264)` | `rgb(226, 232, 240)` |
| `--muted` | `#1E3A4F` | `oklch(29.8% 0.038 244)` | `rgb(30, 58, 79)` |
| `--muted-foreground` | `#94A3B8` | `oklch(64.2% 0.035 264)` | `rgb(148, 163, 184)` |
| `--accent` | `#22D3EE` | `oklch(78.2% 0.142 210)` | `rgb(34, 211, 238)` |
| `--accent-foreground` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--accent-soft` | `#155672` | `oklch(38.5% 0.092 240)` | `rgb(21, 86, 114)` |
| `--destructive` | `#EF4444` | `oklch(58.2% 0.201 28)` | `rgb(239, 68, 68)` |
| `--destructive-foreground` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--success` | `#6FD3AF` | `oklch(78.5% 0.122 167)` | `rgb(111, 211, 175)` |
| `--success-foreground` | `#022C22` | `oklch(22% 0.058 165)` | `rgb(2, 44, 34)` |
| `--warning` | `#FBBF24` | `oklch(79.5% 0.158 83)` | `rgb(251, 191, 36)` |
| `--warning-foreground` | `#0F172A` | `oklch(14.5% 0.026 264)` | `rgb(15, 23, 42)` |
| `--info` | `#22D3EE` | `oklch(78.2% 0.142 210)` | `rgb(34, 211, 238)` |
| `--info-foreground` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--border` | `#1E3A4F` | `oklch(29.8% 0.038 244)` | `rgb(30, 58, 79)` |
| `--input` | `#1E3A4F` | `oklch(29.8% 0.038 244)` | `rgb(30, 58, 79)` |
| `--input-background` | `#0A2E42` | `oklch(24.5% 0.039 248)` | `rgb(10, 46, 66)` |
| `--ring` | `#3B82F6` | `oklch(62.3% 0.196 263)` | `rgb(59, 130, 246)` |
| `--sidebar` | `#0A2E42` | `oklch(24.5% 0.039 248)` | `rgb(10, 46, 66)` |
| `--sidebar-foreground` | `#F1F5F9` | `oklch(92.1% 0.009 264)` | `rgb(241, 245, 249)` |
| `--sidebar-primary` | `#3B82F6` | `oklch(62.3% 0.196 263)` | `rgb(59, 130, 246)` |
| `--sidebar-primary-foreground` | `#FFFFFF` | `oklch(100% 0 0)` | `rgb(255, 255, 255)` |
| `--sidebar-accent` | `#1A3F52` | `oklch(32.5% 0.038 240)` | `rgb(26, 63, 82)` |
| `--sidebar-accent-foreground` | `#E2E8F0` | `oklch(89.2% 0.012 264)` | `rgb(226, 232, 240)` |
| `--sidebar-border` | `#1E3A4F` | `oklch(29.8% 0.038 244)` | `rgb(30, 58, 79)` |
| `--sidebar-ring` | `#3B82F6` | `oklch(62.3% 0.196 263)` | `rgb(59, 130, 246)` |

---

## 4. Tokens Semánticos (CSS Custom Properties)

### Archivo: `src/styles/theme.css`

El theme actual ya tiene la estructura correcta con `:root` para light y `.dark` para dark mode + `@theme inline` para mapear a Tailwind. **Solo hay que reemplazar los valores.**

Consulta la sección [Apéndice A](#apéndice-a-themecss-completo) para el contenido exacto del archivo.

### Convención de naming

- `--{role}`: color principal del rol semántico
- `--{role}-foreground`: color del texto/icono sobre ese rol
- `--{role}-hover`: variante hover (para interactive elements)
- `--{role}-soft`: variante de fondo suave (para badges, tags, backgrounds)

### Tailwind CSS v4

Con `@theme inline` en `theme.css`, los tokens se mapean automáticamente a clases Tailwind:
- `bg-background`, `text-foreground`
- `bg-primary`, `text-primary-foreground`
- `bg-accent`, `text-accent-foreground`
- `border-border`
- `ring-ring`

---

## 5. Tipografía

### Font Stack

| Rol | Fuente | Peso | Fallback |
|-----|--------|------|----------|
| Body / UI | **Inter** | 400 (regular), 500 (medium), 600 (semibold) | `system-ui, sans-serif` |
| Monospace (datos) | **JetBrains Mono** | 400, 500, 600 | `monospace` |

**Razón:** Inter es limpia, altamente legible en pantalla, y tiene excelente soporte de pesos. JetBrains Mono para datos numéricos (RUC, montos, fechas) alinea perfecto.

### Escala Tipográfica

| Nivel | Tamaño | Line-Height | Weight | Font Family | Uso |
|-------|--------|-------------|--------|-------------|-----|
| `display` | `clamp(2.5rem, 5vw, 3.5rem)` | `1.1` | `700` | Inter | Hero pages, títulos grandes |
| `h1` | `1.75rem` (28px) | `1.3` | `600` | Inter | Títulos de página |
| `h2` | `1.5rem` (24px) | `1.4` | `600` | Inter | Secciones |
| `h3` | `1.25rem` (20px) | `1.4` | `600` | Inter | Subtítulos |
| `h4` | `1.125rem` (18px) | `1.5` | `500` | Inter | Cards, grupos |
| `body` | `1rem` (16px) | `1.5` | `400` | Inter | Párrafos, labels |
| `body-sm` | `0.875rem` (14px) | `1.5` | `400` | Inter | Texto secundario |
| `caption` | `0.75rem` (12px) | `1.5` | `500` | Inter | Labels pequeños, badges |
| `data` | `0.875rem` (14px) | `1.4` | `500` | JetBrains Mono | RUC, montos, IDs |
| `data-lg` | `1.25rem` (20px) | `1.3` | `600` | JetBrains Mono | Montos grandes, totales |
| `data-sm` | `0.75rem` (12px) | `1.4` | `500` | JetBrains Mono | Tablas densas |

### Google Fonts

```css
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap');
```

### Archivo `src/styles/fonts.css`

```css
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap');

:root {
  --font-body: 'Inter', system-ui, sans-serif;
  --font-mono: 'JetBrains Mono', monospace;
}
```

### Tailwind font config

Con `@theme inline` en theme.css:

```css
@theme inline {
  --font-body: 'Inter', system-ui, sans-serif;
  --font-mono: 'JetBrains Mono', monospace;
}
```

Uso: `font-body` y `font-mono`.

---

## 6. Spacing & Sizing

### Escala (Tailwind default)

| Token | px | Uso típico |
|-------|----|------------|
| `1` | 4px | Micro espaciado, iconos |
| `2` | 8px | Espaciado entre elementos cercanos |
| `3` | 12px | Padding interno de componentes |
| `4` | 16px | Gap entre secciones, padding de cards |
| `5` | 20px | Padding de contenedores |
| `6` | 24px | Separación entre secciones relacionadas |
| `8` | 32px | Separación entre secciones mayores |
| `10` | 40px | Padding de página, hero spacing |
| `12` | 48px | Separación de landing sections |
| `16` | 64px | Separación mayor, page sections |
| `20` | 80px | Separación de page sections grandes |

### Regla de oro

- **Múltiplos de 4px** para todo (padding, margin, gap)
- **Múltiplos de 8px** para distancias entre componentes
- Inputs y botones: **altura mínima 44px** (touch target)
- Cards: **padding 24px** (`p-6`)

---

## 7. Border Radius & Sombras

### Border Radius

| Token | Valor | Uso |
|-------|-------|-----|
| `--radius-sm` | `6px` | Badges, tags, inputs |
| `--radius-md` | `8px` | Botones, selects |
| `--radius-lg` | `10px` | Cards, modales, dropdowns |
| `--radius-xl` | `14px` | Dialogs, sheets grandes |

### Elevation / Sombras

| Nivel | Uso | Tailwind |
|-------|-----|----------|
| 0 | Superficies planas | `shadow-none` |
| 1 | Cards, dropdowns | `shadow-sm` |
| 2 | Modales, popovers | `shadow-md` |
| 3 | Dialogs, drawers | `shadow-lg` |
| 4 | Toast, notificaciones | `shadow-xl` |

> ⚠️ No usar sombras múltiples ni muy difusas. Mantener sutileza.

---

## 8. Colores de Estado (Status)

| Estado | Light | Dark | Uso |
|--------|-------|------|-----|
| **Pagado / Completado** | `--success` | `--success` | Badge de pago, boleta emitida |
| **Pendiente** | `--warning` | `--warning` | Factura por pagar, por emitir |
| **Rechazado / Anulado** | `--destructive` | `--destructive` | Nota de crédito, anulación |
| **En proceso** | `--info` | `--info` | Envío a SUNAT, validando |
| **Borrador** | `--muted-foreground` | `--muted-foreground` | En edición |

### Estados en facturación peruana (SUNAT)

| Estado SUNAT | Color | Icono |
|-------------|-------|-------|
| ACEPTADO | `--success` | `CheckCircle2` |
| RECHAZADO | `--destructive` | `XCircle` |
| BAJA | `--destructive` | `FileX2` |
| POR ENVIAR | `--warning` | `Clock` |
| EN PROCESO | `--info` | `Loader2` |

> ⚠️ **CRÍTICO:** Nunca usar solo color para diferenciar estados. Siempre acompañar con icono + texto.

---

## 9. Colores para Charts

| Token | Light | Dark | Uso |
|-------|-------|------|-----|
| `--chart-1` | `#155dfc` | `#3B82F6` | Primary series |
| `--chart-2` | `#00B8D9` | `#22D3EE` | Secondary series |
| `--chart-3` | `#6FD3AF` | `#6FD3AF` | Success series |
| `--chart-4` | `#F59E0B` | `#FBBF24` | Warning series |
| `--chart-5` | `#8B5CF6` | `#A78BFA` | Compare series |

---

## 10. Iconos

**Librería obligatoria:** [Lucide React](https://lucide.dev) (ya instalada: `lucide-react` v0.487.0)

### Reglas

| Regla | Estándar |
|-------|----------|
| **Formato** | SVG (Lucide), **nunca emojis** como iconos |
| **Tamaños** | `16px` (sm), `20px` (md), `24px` (lg), `32px` (xl) |
| **Stroke** | Por defecto (1.5px en Lucide) |
| **Color** | `currentColor` o token semántico |
| **Accesibilidad** | `aria-hidden="true"` en decorativos, `aria-label` en interactivos |

### Iconos comunes del dominio

| Concepto | Icono Lucide |
|----------|-------------|
| Factura / Boleta | `FileText`, `Receipt` |
| Guía de Remisión | `Truck` |
| Nota de Crédito | `FileBadge`, `ArrowLeftFromLine` |
| SUNAT | `Building2`, `Landmark` |
| Pagado | `CheckCircle2` |
| Pendiente | `Clock` |
| Anulado | `XCircle`, `FileX2` |
| Descargar PDF | `FileDown`, `Download` |
| Enviar | `Send`, `Upload` |
| Buscar | `Search` |
| Cliente | `User`, `Building` |
| Monto / Total | `DollarSign` |
| Calendario | `Calendar` |
| Filtros | `Filter` |
| Más opciones | `MoreHorizontal`, `EllipsisVertical` |

---

## 11. Integración con MUI 7 Theme

Dado que el proyecto usa MUI 7, los tokens de CSS se pueden mapear al theme de MUI. Crear archivo `src/app/lib/mui-theme.ts`:

```typescript
import { createTheme } from '@mui/material/styles';

export const muiTheme = createTheme({
  palette: {
    mode: 'light', // o 'dark'
    primary: {
      main: '#155dfc',
      light: '#EEF2FF',
      dark: '#1d4ed8',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#00B8D9',
      light: '#ECFEFF',
      dark: '#1AAFC6',
      contrastText: '#ffffff',
    },
    error: {
      main: '#DC2626',
    },
    warning: {
      main: '#F59E0B',
      contrastText: '#ffffff',
    },
    info: {
      main: '#00B8D9',
    },
    success: {
      main: '#6FD3AF',
      contrastText: '#065F46',
    },
    background: {
      default: '#F9FAFB',
      paper: '#FFFFFF',
    },
    text: {
      primary: '#0F172A',
      secondary: '#64748B',
    },
    divider: '#E2E8F0',
  },
  typography: {
    fontFamily: '"Inter", system-ui, sans-serif',
    h1: { fontWeight: 600, fontSize: '1.75rem', lineHeight: 1.3 },
    h2: { fontWeight: 600, fontSize: '1.5rem', lineHeight: 1.4 },
    h3: { fontWeight: 600, fontSize: '1.25rem', lineHeight: 1.4 },
    h4: { fontWeight: 500, fontSize: '1.125rem', lineHeight: 1.5 },
    body1: { fontWeight: 400, fontSize: '1rem', lineHeight: 1.5 },
    body2: { fontWeight: 400, fontSize: '0.875rem', lineHeight: 1.5 },
    button: { fontWeight: 500, textTransform: 'none' },
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          padding: '8px 20px',
          minHeight: 44,
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 8,
          },
        },
      },
    },
  },
});
```

Para dark mode, cambiar `mode: 'dark'` y usar los valores de dark mode.

---

## 12. Reglas de Uso & Anti-Patrones

### Accesibilidad (CRÍTICO)

| Regla | Valor |
|-------|-------|
| Contraste texto normal | ≥ 4.5:1 |
| Contraste texto grande (≥18px) | ≥ 3:1 |
| Touch targets | ≥ 44×44px |
| Focus rings visibles | 2-3px, `--ring` |
| Color + icono + texto | Nunca color solo |

### Anti-Patrones — NO hacer

| ❌ Anti-patrón | ✅ Alternativa |
|----------------|----------------|
| Usar emojis como iconos (🎉 🚀 ⚙️) | Lucide SVG (`PartyPopper`, `Rocket`, `Settings`) |
| Hardcodear hex colors en componentes | Usar tokens CSS (`bg-primary`, `text-muted-foreground`) |
| Texto gris sobre fondo gris claro | Usar `--muted-foreground` sobre `--background` |
| Placeholder como único label | `<Label>` + `<Input>` visibles |
| Animaciones >500ms | 150-300ms, exit más rápido que enter |
| Pie chart con >5 categorías | Bar chart horizontal |
| Color como único indicador de estado | Icono + texto + color |
| Fondo blanco puro en dark mode | `--background: #07283A` con texto claro |
| `100vh` en mobile (cubre notch) | `100dvh` / `min-h-dvh` |

### Modo oscuro

- El navi profundo del brand (`#07283A`) **es** el fondo del dark mode — consistente con el logo
- Los textos primarios en dark: `#F1F5F9`
- Los textos secundarios: `#94A3B8`
- Bordes sutiles: `#1E3A4F`
- No invertir colores — usar variantes desaturadas y más claras

---

## Apéndice A: `theme.css` Completo

Reemplazar el contenido de `src/styles/theme.css` con:

<details>
<summary>Ver theme.css completo</summary>

```css
@custom-variant dark (&:is(.dark *));

/* ==================== LIGHT MODE ==================== */
:root {
  --font-size: 16px;
  --background: #F9FAFB;
  --foreground: #0F172A;
  --card: #FFFFFF;
  --card-foreground: #0F172A;
  --popover: #FFFFFF;
  --popover-foreground: #0F172A;
  --primary: #155dfc;
  --primary-foreground: #FFFFFF;
  --primary-hover: #1d4ed8;
  --primary-soft: #EEF2FF;
  --secondary: #F0F7FA;
  --secondary-foreground: #07283A;
  --muted: #F1F5F9;
  --muted-foreground: #64748B;
  --accent: #00B8D9;
  --accent-foreground: #FFFFFF;
  --accent-soft: #ECFEFF;
  --destructive: #DC2626;
  --destructive-foreground: #FFFFFF;
  --success: #6FD3AF;
  --success-foreground: #065F46;
  --warning: #F59E0B;
  --warning-foreground: #FFFFFF;
  --info: #00B8D9;
  --info-foreground: #FFFFFF;
  --border: #E2E8F0;
  --input: #E2E8F0;
  --input-background: #F8FAFC;
  --switch-background: #CBD5E1;
  --ring: #155dfc;
  --radius: 0.625rem;
  --font-body: 'Inter', system-ui, sans-serif;
  --font-mono: 'JetBrains Mono', monospace;
  --sidebar: #F8FAFC;
  --sidebar-foreground: #0F172A;
  --sidebar-primary: #155dfc;
  --sidebar-primary-foreground: #FFFFFF;
  --sidebar-accent: #F0F7FA;
  --sidebar-accent-foreground: #07283A;
  --sidebar-border: #E2E8F0;
  --sidebar-ring: #155dfc;
  --chart-1: #155dfc;
  --chart-2: #00B8D9;
  --chart-3: #6FD3AF;
  --chart-4: #F59E0B;
  --chart-5: #8B5CF6;
}

/* ==================== DARK MODE ==================== */
.dark {
  --background: #07283A;
  --foreground: #F1F5F9;
  --card: #0A2E42;
  --card-foreground: #F1F5F9;
  --popover: #0A2E42;
  --popover-foreground: #F1F5F9;
  --primary: #3B82F6;
  --primary-foreground: #FFFFFF;
  --primary-hover: #60A5FA;
  --primary-soft: #1E3A5F;
  --secondary: #1A3F52;
  --secondary-foreground: #E2E8F0;
  --muted: #1E3A4F;
  --muted-foreground: #94A3B8;
  --accent: #22D3EE;
  --accent-foreground: #FFFFFF;
  --accent-soft: #155672;
  --destructive: #EF4444;
  --destructive-foreground: #FFFFFF;
  --success: #6FD3AF;
  --success-foreground: #022C22;
  --warning: #FBBF24;
  --warning-foreground: #0F172A;
  --info: #22D3EE;
  --info-foreground: #FFFFFF;
  --border: #1E3A4F;
  --input: #1E3A4F;
  --input-background: #0A2E42;
  --switch-background: #334155;
  --ring: #3B82F6;
  --font-body: 'Inter', system-ui, sans-serif;
  --font-mono: 'JetBrains Mono', monospace;
  --sidebar: #0A2E42;
  --sidebar-foreground: #F1F5F9;
  --sidebar-primary: #3B82F6;
  --sidebar-primary-foreground: #FFFFFF;
  --sidebar-accent: #1A3F52;
  --sidebar-accent-foreground: #E2E8F0;
  --sidebar-border: #1E3A4F;
  --sidebar-ring: #3B82F6;
  --chart-1: #3B82F6;
  --chart-2: #22D3EE;
  --chart-3: #6FD3AF;
  --chart-4: #FBBF24;
  --chart-5: #A78BFA;
}

/* ==================== TAILWIND THEME MAPPING ==================== */
@theme inline {
  --color-background: var(--background);
  --color-foreground: var(--foreground);
  --color-card: var(--card);
  --color-card-foreground: var(--card-foreground);
  --color-popover: var(--popover);
  --color-popover-foreground: var(--popover-foreground);
  --color-primary: var(--primary);
  --color-primary-foreground: var(--primary-foreground);
  --color-primary-hover: var(--primary-hover);
  --color-primary-soft: var(--primary-soft);
  --color-secondary: var(--secondary);
  --color-secondary-foreground: var(--secondary-foreground);
  --color-muted: var(--muted);
  --color-muted-foreground: var(--muted-foreground);
  --color-accent: var(--accent);
  --color-accent-foreground: var(--accent-foreground);
  --color-accent-soft: var(--accent-soft);
  --color-destructive: var(--destructive);
  --color-destructive-foreground: var(--destructive-foreground);
  --color-success: var(--success);
  --color-success-foreground: var(--success-foreground);
  --color-warning: var(--warning);
  --color-warning-foreground: var(--warning-foreground);
  --color-info: var(--info);
  --color-info-foreground: var(--info-foreground);
  --color-border: var(--border);
  --color-input: var(--input);
  --color-input-background: var(--input-background);
  --color-switch-background: var(--switch-background);
  --color-ring: var(--ring);
  --color-chart-1: var(--chart-1);
  --color-chart-2: var(--chart-2);
  --color-chart-3: var(--chart-3);
  --color-chart-4: var(--chart-4);
  --color-chart-5: var(--chart-5);
  --color-sidebar: var(--sidebar);
  --color-sidebar-foreground: var(--sidebar-foreground);
  --color-sidebar-primary: var(--sidebar-primary);
  --color-sidebar-primary-foreground: var(--sidebar-primary-foreground);
  --color-sidebar-accent: var(--sidebar-accent);
  --color-sidebar-accent-foreground: var(--sidebar-accent-foreground);
  --color-sidebar-border: var(--sidebar-border);
  --color-sidebar-ring: var(--sidebar-ring);
  --radius-sm: calc(var(--radius) - 4px);
  --radius-md: calc(var(--radius) - 2px);
  --radius-lg: var(--radius);
  --radius-xl: calc(var(--radius) + 4px);
  --font-body: var(--font-body);
  --font-mono: var(--font-mono);
}
```

</details>

---

## Apéndice B: Guía Rápida para Implementar

### Orden de refactorización visual

1. **Actualizar `src/styles/theme.css`** → reemplazar valores con los de este documento
2. **Actualizar `src/styles/fonts.css`** → agregar import de Google Fonts + variables
3. **Crear `src/app/lib/mui-theme.ts`** → para que MUI use los mismos tokens
4. **Revisar componentes** → reemplazar colores hardcodeados con tokens (`bg-primary`, `text-muted-foreground`, etc.)
5. **Verificar accesibilidad** → contraste, focus rings, touch targets

---

*Documento generado con UI/UX Pro Max — Design Intelligence*
