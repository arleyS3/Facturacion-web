# Reporte de Vulnerabilidades — Facturacion-web

**Fecha**: 2026-06-22
**Scope**: Frontend (Vercel) + Backend (Railway)
**Total**: 12 vulnerabilidades (6 frontend + 6 backend)

---

## Frontend

### 🔴 F1. JWT almacenado en localStorage

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Archivo(s)** | `src/frontEnd/src/app/lib/api.ts:17`, `src/frontEnd/src/app/features/autenticacion/pages/Login.tsx:59` |
| **Impacto** | Un XSS (por dependencia, input mal sanitizado, o extensión maliciosa) permite leer `localStorage.getItem('token')` y robar el JWT. Con él se puede suplantar al usuario, emitir facturas, acceder a datos tributarios. |
| **Solución** | Migrar a httpOnly cookies con `Secure` y `SameSite=Strict`. El backend debe setear la cookie en la respuesta de login. Alternativa: mantener Bearer pero con token en memoria volátil. |

```ts
// api.ts:17 — actual
const token = localStorage.getItem('token');
if (token) {
  config.headers.Authorization = `Bearer ${token}`;
}

// Login.tsx:59 — actual
localStorage.setItem("token", accessToken);
```

---

### 🔴 F2. Token SUNAT expuesto en query string + variable VITE_

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Archivo(s)** | `src/frontEnd/api/sunat/ruc.js:16-17` |
| **Impacto** | La serverless function acepta `?token=...` en la URL. El token queda en logs de Vercel, referrer headers, bookmarks. Además usa `process.env.VITE_SUNAT_TOKEN` como fallback — las variables `VITE_` se renderizan en el bundle JS público. |
| **Solución** | Eliminar el `queryToken`. Renombrar `VITE_SUNAT_TOKEN` a `SUNAT_TOKEN`. La serverless corre en Node, no en el bundle, así que puede leer cualquier variable de entorno. |

```js
// ruic.js:16-17 — ELIMINAR
const queryToken = typeof req.query?.token === "string" ? req.query.token.trim() : "";
const token = process.env.SUNAT_TOKEN || process.env.VITE_SUNAT_TOKEN || headerToken || queryToken;
```

---

### 🔴 F3. console.log con datos sensibles de facturación

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Archivo(s)** | `InvoiceForm.tsx:191`, `InvoiceForm.tsx:222`, `ShippingGuide.tsx:101`, `xmlService.ts:33` |
| **Impacto** | Datos tributarios completos (RUC, productos, montos, IGV, series) se escriben a la consola. Cualquier extensión maliciosa o persona con DevTools puede leerlos. |
| **Solución** | Eliminar todos los `console.log` que expongan payloads de producción. Usar logger condicional con `import.meta.env.DEV`. |

```ts
// InvoiceForm.tsx:191 — REEMPLAZAR
if (import.meta.env.DEV) console.log("[InvoiceForm] PAYLOAD enviado ->", payload);
```

---

### 🔴 F4. Sin headers de seguridad (CSP, X-Frame-Options, etc.)

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Archivo(s)** | `src/frontEnd/index.html`, Vercel (sin configuración) |
| **Impacto** | Sin CSP cualquier script externo puede ejecutarse. Sin X-Frame-Options la app puede ser iframeada (clickjacking). Sin HSTS, conexiones HTTP no se upgraden. |
| **Solución** | Agregar headers via `vercel.json` o meta tags en `index.html`. El scan de ZAP confirmó: 8 warnings por headers faltantes. |

```json
// vercel.json (raíz del frontend)
{
  "headers": [
    {
      "source": "/(.*)",
      "headers": [
        { "key": "X-Frame-Options", "value": "DENY" },
        { "key": "X-Content-Type-Options", "value": "nosniff" },
        { "key": "Content-Security-Policy", "value": "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; connect-src 'self' https://facturacion-web.up.railway.app" },
        { "key": "Permissions-Policy", "value": "geolocation=(), microphone=(), camera=()" },
        { "key": "Referrer-Policy", "value": "strict-origin-when-cross-origin" }
      ]
    }
  ]
}
```

---

### 🟡 F5. xlsx@0.18.5 con CVEs conocidas

| Campo | Detalle |
|-------|---------|
| **Riesgo** | MEDIO |
| **Archivo(s)** | `src/frontEnd/package.json:73` |
| **Impacto** | SheetJS xlsx < 0.19.0 tiene prototype pollution (CVE-2023-30533). El import de Excel podría ser un vector si un atacante sube un archivo malicioso. |
| **Solución** | El package `xlsx` está abandonado en npm (última versión 0.18.5). Evaluar migrar a librería alternativa mantenida como `exceljs` o `@handsontable/xlsx`. |

---

### 🟡 F6. refreshToken en localStorage

| Campo | Detalle |
|-------|---------|
| **Riesgo** | MEDIO |
| **Archivo(s)** | `src/frontEnd/src/app/components/layout/AppLayout.tsx:190` |
| **Impacto** | `localStorage.removeItem("refreshToken")` confirma que el refresh token se guarda en localStorage. Sin rotación ni expiry corto, un XSS permite obtener tokens nuevos indefinidamente. |
| **Solución** | Mover refresh token a httpOnly cookie o memoria volátil. Implementar refresh token rotation (el backend debe invalidar el anterior al emitir uno nuevo). |

---

## Backend

### 🔴 B1. Toda la API expuesta sin autenticación

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Archivo(s)** | `SecurityConfig.java:32-33` |
| **Impacto** | `.requestMatchers("/api/**").permitAll()` anula toda la seguridad. Cualquier persona puede generar XML firmados, enviar a SUNAT, leer/configurar certificados sin autenticarse. |
| **Solución** | Cambiar a rutas específicas: solo `/api/v1/auth/**` debe ser `permitAll()`. El resto debe requerir autenticación. |

```java
// SecurityConfig.java — REEMPLAZAR
.requestMatchers("/api/v1/auth/**").permitAll()
.requestMatchers("/api/v1/tramas/**").authenticated()
.requestMatchers("/api/v1/comprobantes/**").authenticated()
// ... el resto authenticated()
```

---

### 🔴 B2. .env con credenciales reales de BD en el repo

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Archivo(s)** | `src/backend/api/.env` |
| **Impacto** | `DB_PASSWORD=RAFn5ifFCUtuezj4` — credenciales de Supabase PostgreSQL expuestas. Acceso completo a BD de producción: usuarios, certificados digitales, datos tributarios. |
| **Solución** | Rotar inmediatamente las credenciales en Supabase. Usar variables de entorno reales en Railway, no `.env`. Agregar `.env` a `.gitignore` si no lo está ya. |

---

### 🔴 B3. JWT Secret hardcodeado con fallback

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Archivo(s)** | `application.yaml:62-63` |
| **Impacto** | `jwt.secret: ${JWT_SECRET:8sB0ug_A4Ur73UX5gtrGY1vaWeVvzCllXgV24g-pX78}`. Si `JWT_SECRET` no está configurado en Railway, se usa el literal. Cualquiera con acceso al código puede forjar JWTs. |
| **Solución** | Eliminar el default después del `:`. Generar una clave real con `openssl rand -base64 32` y setearla como variable de entorno en Railway. |

---

### 🔴 B4. Clave de encriptación AES hardcodeada

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Archivo(s)** | `application.yaml:53-55` |
| **Impacto** | `encryption.key: ${ENCRYPTION_KEY:ClaveEncriptacion32Caracteres!}` — clave AES-256 para certificados digitales. Si no está configurada en Railway, las contraseñas de los .pfx son descifrables con solo leer el código. |
| **Solución** | Eliminar el default. La `ENCRYPTION_KEY` debe ser estrictamente variable de entorno. Considerar KMS / Vault. |

---

### 🔴 B5. JWT logueado en System.out

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Archivo(s)** | `JwtAuthFilter.java:48-49` |
| **Impacto** | `System.out.println("TOKEN RECIBIDO: " + token)` — cada request loguea el JWT completo. Si los logs van a CloudWatch/Datadog, los tokens de acceso quedan persistidos. |
| **Solución** | Eliminar esos `System.out.println`. Usar `log.debug()` solo si es estrictamente necesario, y solo el subject, no el token completo. |

---

### 🟠 B6. Login sin validación de entrada + sin rate limiting

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Archivo(s)** | `LoginRequest.java`, `RegisterRequest.java`, `AuthController.java` |
| **Impacto** | `LoginRequest` y `RegisterRequest` no tienen `@NotBlank` ni `@Email`. El controller no usa `@Valid`. Se puede enviar `{"email": "", "password": ""}`. Además, no hay rate limiting — ataques de fuerza bruta ilimitados. |
| **Solución** | Agregar `@Email @NotBlank` en email, `@NotBlank @Size(min=8)` en password, `@Valid` en el controller. Implementar rate limiting con bucket4j o similar. |

```java
// LoginRequest.java
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password
) {}

// AuthController.java
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) { ... }
```

---

## Asignación sugerida (6 personas × 2 vulnerabilidades)

| Persona | Frontend | Backend |
|---------|----------|---------|
| 👤 1 | **F1** JWT localStorage | **B1** API sin auth |
| 👤 2 | **F2** Token SUNAT query string | **B2** .env credenciales |
| 👤 3 | **F3** console.log datos sensibles | **B3** JWT Secret hardcodeado |
| 👤 4 | **F4** Headers de seguridad | **B4** Clave AES hardcodeada |
| 👤 5 | **F5** xlsx con CVEs | **B5** JWT logueado |
| 👤 6 | **F6** refreshToken en localStorage | **B6** Login sin validación |

---

## Notas

- Las vulnerabilidades **B1, B2, B3, B4** son críticas y deberían resolverse **antes de cualquier otro cambio**.
- ZAP scan (Automated + Manual Explore) no encontró XSS, SQLi, RCE, ni CSRF explotable en el frontend.
- El `pnpm audit` del frontend encontró 14 dependencias con CVEs; se resolvieron react-router, vite, axios. Queda pendiente xlsx.
- OWASP Dependency-Check para backend pendiente (requiere Docker con `sg docker`).
