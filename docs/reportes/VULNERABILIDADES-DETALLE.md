# Vulnerabilidades — Detalle con Evidencia

**Proyecto**: Facturacion-web  
**Fecha**: 2026-06-22  
**Fuente**: OWASP ZAP scan + Code Review  
**Reportes ZAP**: [`reporte-frontend.html`](./reporte-frontend.html), [`reporte-api.html`](./reporte-api.html)

---

## 1. F1 — JWT almacenado en localStorage

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Categoría** | OWASP Top 10 — A2:2021 Broken Authentication |
| **Archivo(s)** | `src/frontEnd/src/app/features/autenticacion/pages/Login.tsx:59`, `src/frontEnd/src/app/lib/api.ts:17` |

### Evidencia ZAP

**Reporte**: `reporte-api.html` — sección Cookie Without Secure Flag y Cookie without SameSite Attribute

El ZAP detectó que las cookies de sesión no tienen flags de seguridad:
- `Cookie Without Secure Flag [10011]` — 4 URLs afectadas
- `Cookie without SameSite Attribute [10054]` — 4 URLs afectadas

Esto confirma que el manejo de sesión/token no sigue buenas prácticas. El JWT se guarda en `localStorage` y se envía como `Authorization: Bearer <token>` sin usar cookies HttpOnly.

### Impacto

Un XSS (por dependencia vulnerable, input mal sanitizado, o extensión maliciosa) permite ejecutar `localStorage.getItem('token')` y robar el JWT. Con él, el atacante puede:
- Suplantar al usuario
- Generar XML firmados
- Enviar comprobantes al OSE
- Acceder a datos tributarios

### Solución propuesta

1. Backend: en la respuesta de `/auth/login`, setear el JWT como cookie HttpOnly, Secure, SameSite=Strict
2. Frontend: eliminar `localStorage.setItem("token", ...)`, leer el token desde la cookie
3. Implementar refresh token rotation

```java
// Backend — AuthController.java
Cookie cookie = new Cookie("accessToken", token);
cookie.setHttpOnly(true);
cookie.setSecure(true);
cookie.setAttribute("SameSite", "Strict");
cookie.setPath("/api");
response.addCookie(cookie);
```

---

## 2. F4 — Sin headers de seguridad (CSP, X-Frame-Options)

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Categoría** | OWASP Top 10 — A5:2021 Security Misconfiguration |
| **Archivo(s)** | `src/frontEnd/index.html`, Vercel (sin configuración) |

### Evidencia ZAP

**Reporte**: `reporte-frontend.html` — 8 warnings de seguridad

El ZAP Automated Scan encontró **8 warnings** directamente relacionados:

| Warning | URL afectadas |
|---------|--------------|
| `Missing Anti-clickjacking Header [10020]` | 4 URLs (login, /, robots.txt, sitemap.xml) |
| `X-Content-Type-Options Header Missing [10021]` | 6 URLs |
| `Content Security Policy (CSP) Header Not Set [10038]` | 4 URLs |
| `Permissions Policy Header Not Set [10063]` | 5 URLs |
| `Timestamp Disclosure - Unix [10096]` | 3 instancias en JS bundle |
| `Cross-Domain Misconfiguration [10098]` | 6 URLs |
| `Proxy Disclosure [40025]` | 7 URLs |
| `Cross-Origin-Embedder-Policy Header Missing [90004]` | 8 URLs |

### Impacto

- Sin **CSP**: cualquier script externo puede ejecutarse (XSS)
- Sin **X-Frame-Options**: la app puede ser iframeada (clickjacking)
- Sin **X-Content-Type-Options**: riesgo de MIME sniffing
- Sin **HSTS**: conexiones HTTP no se upgrading a HTTPS
- Sin **Permissions-Policy**: APIs del navegador (geolocation, cámara) accesibles

### Solución propuesta

Agregar `vercel.json` en la raíz del frontend:

```json
{
  "headers": [
    {
      "source": "/(.*)",
      "headers": [
        { "key": "X-Frame-Options", "value": "DENY" },
        { "key": "X-Content-Type-Options", "value": "nosniff" },
        { "key": "Content-Security-Policy", "value": "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; connect-src 'self' https://facturacion-web.up.railway.app" },
        { "key": "Permissions-Policy", "value": "geolocation=(), microphone=(), camera=()" },
        { "key": "Referrer-Policy", "value": "strict-origin-when-cross-origin" },
        { "key": "Strict-Transport-Security", "value": "max-age=31536000; includeSubDomains" }
      ]
    }
  ]
}
```

---

## 3. F2 — Token SUNAT expuesto en query string + variable VITE_

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Categoría** | OWASP Top 10 — A5:2021 Security Misconfiguration |
| **Archivo(s)** | `src/frontEnd/api/sunat/ruc.js:16-17` |

### Evidencia

**Reporte ZAP**: `reporte-api.html` — el endpoint `/api/v1/sunat?ruc=...` fue detectado por ZAP durante el Manual Explore. El token SUNAT viaja como parte del request en la serverless function de Vercel.

**Evidencia en código**:

```js
// api/sunat/ruc.js:16-17 — token aceptado por query string
const queryToken = typeof req.query?.token === "string" ? req.query.token.trim() : "";
const token = process.env.SUNAT_TOKEN || process.env.VITE_SUNAT_TOKEN || headerToken || queryToken;
```

El uso de `VITE_SUNAT_TOKEN` expone el token porque las variables con prefijo `VITE_` se renderizan en el bundle JS de producción. Cualquier persona puede abrir DevTools → Sources y ver el token en el código compilado.

### Impacto

- El token SUNAT puede quedar en:
  - Logs de Vercel (serverless functions)
  - Referrer headers (si alguien comparte la URL)
  - Bookmarks / historial del navegador
  - Bundle JS público (por el prefijo VITE_)
- Con el token, un atacante puede consultar RUCs de forma gratuita usando la API de SUNAT

### Solución propuesta

1. Eliminar `queryToken` de la función — nunca pasar tokens por query string
2. Renombrar `VITE_SUNAT_TOKEN` a `SUNAT_TOKEN` (sin prefijo `VITE_`)
3. La serverless function de Vercel corre en Node, puede leer cualquier variable de entorno

---

## 4. B5 — JWT logueado en System.out

| Campo | Detalle |
|-------|---------|
| **Riesgo** | ALTO |
| **Categoría** | OWASP Top 10 — A9:2021 Security Logging and Monitoring Failures |
| **Archivo(s)** | `src/backend/api/src/main/java/com/facturacion/api/security/JwtAuthFilter.java:48-49` |

### Evidencia ZAP

**Reporte**: `reporte-api.html` — la detección de cookies sin Secure flag (sección 1) confirma que los tokens se exponen en la comunicación. Adicionalmente, el análisis de código encontró que el JWT se loguea completo en cada request.

### Evidencia en código

```java
// JwtAuthFilter.java:48-49
System.out.println("TOKEN RECIBIDO: " + token);
System.out.println("¿Está en blacklist?: " + isBlacklisted);
```

Cada request HTTP loguea el JWT completo a `System.out`. Si Railway usa agregadores de logs (CloudWatch, Datadog, Logtail, etc.), los tokens de acceso quedan persistidos por días o semanas.

### Impacto

- Cualquier persona con acceso a los logs de Railway puede leer JWTs activos
- Los logs pueden quedar expuestos por:
  - Acceso directo a Railway Dashboard
  - Brechas de seguridad en el proveedor de logs
  - Exportaciones de logs a terceros
- Con un JWT válido, el atacante puede suplantar al usuario

### Solución propuesta

```java
// JwtAuthFilter.java — REEMPLAZAR
if (log.isDebugEnabled()) {
    log.debug("Token recibido para usuario: {}", email);
}
```

Eliminar el `System.out.println` del token completo. Si se necesita debug, usar `log.debug()` con solo el email/usuario, nunca el token completo.

---

## Referencias

- OWASP ZAP Reports: [`reporte-frontend.html`](./reporte-frontend.html), [`reporte-api.html`](./reporte-api.html)
- Reporte completo de vulnerabilidades: [`VULNERABILIDADES.md`](./VULNERABILIDADES.md)
- Código fuente: `/src/frontEnd/` y `/src/backend/api/`
