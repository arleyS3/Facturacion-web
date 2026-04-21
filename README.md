# 🧾 Facturación Web

> Generación y descarga de tramas TXT para comprobantes electrónicos.

![Java](https://img.shields.io/badge/Java-21-informational?style=flat)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen?style=flat)
![React](https://img.shields.io/badge/React-18-blue?style=flat)
![TypeScript](https://img.shields.io/badge/TypeScript-%5E4.0-blue?style=flat)

Repositorio full-stack compuesto por una API REST en Java Spring Boot (`src/backend/api`) y una aplicación web en React + TypeScript (`src/frontEnd`).

---

## 📌 Tecnologías

### Backend
- Java 21
- Spring Boot 4.x (Web MVC, Validation)
- Maven (wrapper incluido)
- springdoc-openapi (Swagger)
- JUnit

### Frontend
- Node.js v20+ / npm
- React 18 + TypeScript
- Vite
- React Router, React Hook Form, Axios
- Tailwind CSS, Radix UI, TanStack React Query

---

## 🌿 Ramas

| Rama | Descripción |
|------|-------------|
| `main` | Producción estable. Solo merges desde `develop` o `hotfix/*` aprobados. |
| `develop` | Integración para la próxima versión. |
| `feature/<nombre>` | Nuevas funcionalidades (derivan de `develop`). |
| `hotfix/<nombre>` | Correcciones urgentes (derivan de `main`, luego merge a `main` y `develop`). |

> Ejemplos: `feature/emitir-trama`, `hotfix/fix-encoding-txt`

---

## 🚀 Ejecución local

Se recomienda iniciar primero el backend y luego el frontend.

### 1) Backend

**Requisito:** JDK 21

```bash
cd src/backend/api
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080/api/v1`.

| Herramienta | URL |
|-------------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui` |
| OpenAPI Docs | `http://localhost:8080/api-docs` |

### 2) Frontend

**Requisito:** Node.js v20+ y npm

```bash
cd src/frontEnd
npm install
npm run dev
```

**Variable de entorno requerida:**

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

---

## 📂 Evidencias

<!-- Agrega capturas de pantalla o GIFs en /docs/evidencias y enlázalos aquí -->

Ver carpeta [`/docs/evidencias`](./docs/evidencias)

---

## ✅ Funcionalidades actuales

- [ ] Generación de tramas TXT para comprobantes electrónicos
- [ ] Descarga de tramas generadas
- [ ] <!-- Agrega más funcionalidades aquí -->

---

## ⚙️ Requisitos previos

- JDK 21
- Node.js v20+
- npm
- Variables de entorno configuradas (ver `.env.example` si existe)
