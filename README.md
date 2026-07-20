<div align="center">
  <img src="./docs/logo/Logo%20Transparente.png" alt="AutonomiFlow" width="170" />

# AutonomiFlow

  <p><strong>Generación y descarga de tramas TXT y XML UBL 2.1 para comprobantes electrónicos.</strong></p>

  [![Java 21](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
  [![React 18](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react&logoColor=black)](https://react.dev/)
  [![TypeScript](https://img.shields.io/badge/TypeScript-%5E4.0-3178C6?style=flat-square&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
</div>

Repositorio full-stack compuesto por una API REST en Java Spring Boot (`src/backend/api`) y una aplicación web en React + TypeScript (`src/frontEnd`).

---

## 📌 Stack Tecnológico

<details open>
<summary><strong>Backend</strong> — Java 21 + Spring Boot 4.x</summary>
<br/>

| Categoría | Tecnología |
| ----------- | ------------ |
| **Lenguaje** | ![Java](https://img.shields.io/badge/Java_21-007396?style=flat-square&logo=openjdk&logoColor=white) |
| **Framework** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot_4.x-6DB33F?style=flat-square&logo=springboot&logoColor=white) *(Web MVC, Validation)* |
| **Build** | ![Maven](https://img.shields.io/badge/Maven_Wrapper-C71A36?style=flat-square&logo=apachemaven&logoColor=white) |
| **Documentación API** | ![Swagger](https://img.shields.io/badge/springdoc--openapi-85EA2D?style=flat-square&logo=swagger&logoColor=black) |
| **Testing** | ![JUnit](https://img.shields.io/badge/JUnit_5-25A162?style=flat-square&logo=junit5&logoColor=white) |
| **SOAP Client** | WebServiceTemplate + JAXB |
| **XML Processing** | DOM Parser, JAXB |

</details>

<details open>
<summary><strong>Frontend</strong> — React 18 + TypeScript</summary>
<br/>

| Categoría | Tecnología |
| ----------- | ------------ |
| **Framework** | ![React](https://img.shields.io/badge/React_18-61DAFB?style=flat-square&logo=react&logoColor=black) ![TypeScript](https://img.shields.io/badge/TypeScript_5-3178C6?style=flat-square&logo=typescript&logoColor=white) |
| **Build** | ![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white) |
| **Routing** | ![React Router](https://img.shields.io/badge/React_Router-CA4245?style=flat-square&logo=reactrouter&logoColor=white) |
| **Forms** | ![React Hook Form](https://img.shields.io/badge/React_Hook_Form-EC5990?style=flat-square&logo=reacthookform&logoColor=white) + ![Zod](https://img.shields.io/badge/Zod-3E67B1?style=flat-square&logo=zod&logoColor=white) *(schemas y validación)* |
| **HTTP** | ![Axios](https://img.shields.io/badge/Axios-5A29E4?style=flat-square&logo=axios&logoColor=white) |
| **Caching / Estado** | ![TanStack Query](https://img.shields.io/badge/TanStack_Query-FF4154?style=flat-square&logo=reactquery&logoColor=white) *(catálogos SUNAT)* |
| **Estilos** | ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white) |
| **Componentes** | ![shadcn/ui](https://img.shields.io/badge/shadcn/ui-000000?style=flat-square&logo=shadcnui&logoColor=white) *(Radix UI + Tailwind)* |
| **Íconos** | ![Lucide](https://img.shields.io/badge/Lucide-F56565?style=flat-square&logo=lucide&logoColor=white) |
| **UI Library** | MUI Autocomplete *(importación Excel)* |
| **Excel** | SheetJS (xlsx) *(importación productos)* |
| **Notificaciones** | Sonner *(toasts)* |

</details>

---

## 🌿 Ramas

Flujo recomendado para desarrollo, integración y correcciones urgentes.

| Rama | Descripción |
| ------ | ------------- |
| `main` | Producción estable. Solo merges desde `develop` o `hotfix/*` aprobados. |
| `develop` | Integración para la próxima versión. |
| `feature/<nombre>` | Nuevas funcionalidades (derivan de `develop`). |
| `hotfix/<nombre>` | Correcciones urgentes (derivan de `main`, luego merge a `main` y `develop`). |

> Ejemplos: `feature/emitir-trama`, `hotfix/fix-encoding-txt`

---

## 📂 Evidencias

Ubicaciones de evidencias:

- Carpeta base del proyecto: [`/docs/evidencias`](./docs/evidencias)
- Imágenes de flujo Git: [`/git-sim_media/Imagenes`](./git-sim_media/Imagenes)
- Video de flujo Git: [`/git-sim_media/Videos`](./git-sim_media/Videos)

### Capturas

![Git branch develop](./git-sim_media/Imagenes/git-develop.jpg)
![Historial y autores](./git-sim_media/Imagenes/git-history-users.png)
![Git Graph en VS Code](./git-sim_media/Imagenes/vs-code-grafica_1.png)
![Git Graph en VS Code](./git-sim_media/Imagenes/vs-code-grafica_2.png)
![Git Graph en VS Code](./git-sim_media/Imagenes/vs-code-grafica_3.png)

### Video

<video src="./git-sim_media/Videos/flujo-git.mp4" controls width="100%">
 Tu navegador no soporta la etiqueta de video.
</video>

Enlace directo: [Ver flujo-git.mp4](https://drive.google.com/file/d/14akqxC7xYTa6ZageN674umi7EHmAruQy/view?usp=sharing)

---

## ✅ Funcionalidades implementadas

### Comprobantes Electrónicos (UBL 2.1 / SUNAT)

| Comprobante | Backend | Frontend | Estado |
| ------------- | --------- | ---------- | -------- |
| Factura Electrónica (01) | ✅ XML con InvoiceType, IGV, ISC, descuentos, docs relacionados | ✅ Formulario completo + builder canónico | ✅ Completo |
| Boleta de Venta (03) | ✅ XML con InvoiceType simplificado | ✅ Mismo formulario que Factura | ✅ Completo |
| Nota de Crédito (07) | ✅ XML con CreditNoteType, DiscrepancyResponse (catálogo 09), BillingReference | ✅ DocumentoRelacionadoSection + Sustento | ✅ Completo |
| Nota de Débito (08) | ✅ XML con DebitNoteType, DiscrepancyResponse (catálogo 10), BillingReference | ✅ DocumentoRelacionadoSection + Sustento | ✅ Completo |
| Guía de Remisión (09) | ✅ XML con DespatchAdviceType (transporte, remitente, destinatario) | ⏳ En desarrollo | En desarrollo |

### Funcionalidades transversales

- [x] **Generación de XML UBL 2.1** para Factura, Boleta, Nota de Crédito y Nota de Débito
- [x] **Documentos Relacionados**: Guías de remisión, documentos adicionales y anticipos
- [x] **Descuentos Globales**: Descuentos por ítem y globales con AllowanceCharge
- [x] **Catálogos SUNAT**: Cacheo con TanStack Query (staleTime 5min, cacheTime 30min)
- [x] **Monto en Letras**: Generación automática (SON XXX CON YY/100 SOLES)
- [x] **OSE Client**: Firma XMLDSig, ZIP Base64 y envío SOAP `sendBill` a OSE/SUNAT
- [x] **Importación Excel/CSV**: Carga masiva de productos desde archivo
- [x] **Login** con JWT
- [x] **Swagger UI** en `/swagger-ui`
- [x] **Accesibilidad**: ARIA labels, roles, descripciones, navegación por teclado

### PRs mergeados

| PR | Feature |
| ---- | --------- |
| [#12](https://github.com/ArleyUTP/Facturacion-web/pull/12) | Factura Electrónica UBL 2.1 |
| [#13](https://github.com/ArleyUTP/Facturacion-web/pull/13) | Integración Frontend XML UBL 2.1 |
| [#14](https://github.com/ArleyUTP/Facturacion-web/pull/14) | Accesibilidad Ventas |
| [#16](https://github.com/ArleyUTP/Facturacion-web/pull/16) | Cacheo Catálogos |
| [#18](https://github.com/ArleyUTP/Facturacion-web/pull/18) | Boleta Electrónica UBL |
| [#19](https://github.com/ArleyUTP/Facturacion-web/pull/19) | Descuentos Globales |
| [#21](https://github.com/ArleyUTP/Facturacion-web/pull/21) | Documentos Relacionados |
| [#22](https://github.com/ArleyUTP/Facturacion-web/pull/22) | Nota de Crédito UBL |
| [#24](https://github.com/ArleyUTP/Facturacion-web/pull/24) | Nota de Débito UBL |
| [#26](https://github.com/ArleyUTP/Facturacion-web/pull/26) | Frontend Nota de Crédito y Débito |

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

## 🔐 Firma XML y envío OSE/SUNAT

### Comportamiento por ambiente

| Endpoint | Desarrollo / Sin certificado | Producción / Con certificado |
|----------|------------------------------|------------------------------|
| `POST /generar-xml` | XML **sin firma** (se genera igual) | XML firmado con XMLDSig RSA-SHA256 |
| `POST /ose/enviar-xml` | Error (requiere certificado) | XML firmado + ZIP + `sendBill` |

El endpoint de generación de XML (`/generar-xml`) usa `trySignXml()`: si no hay certificado configurado, genera el XML sin firma y continúa. Esto permite desarrollar y probar el XML aunque aún no tengas el certificado.

### Flujo de envío a OSE/SUNAT

```text
XML UBL 2.1
↓
Firma XMLDSig con certificado PFX/P12 del emisor
↓
ZIP con nombre RUC-TIPO-SERIE-CORRELATIVO.ZIP
↓
Base64 del ZIP
↓
sendBill al OSE/SUNAT
↓
CDR base64 o SOAP Fault
```

### Variables de entorno

| Variable | Uso |
| ---------- | ----- |
| `ENCRYPTION_KEY` | Clave de 32 caracteres usada para cifrar la contraseña del certificado en BD. |
| `OSE_ENDPOINT_URL` | Endpoint SOAP de envío. |
| `OSE_USERNAME` | Usuario WS-Security. Ejemplo test: `20100119065MODDATOS`. |
| `OSE_PASSWORD` | Password WS-Security. |
| `OSE_SOAP_ACTION_SEND_BILL` | SOAPAction de `sendBill`. |
| `OSE_SOAP_ACTION_GET_STATUS` | SOAPAction de `getStatus`. |

Perfiles comunes:

```env
# Estela / OSE Test
OSE_ENDPOINT_URL=https://ose-test.com/ol-ti-itcpe/billService.svc
OSE_SOAP_ACTION_SEND_BILL=urn:sendBill
OSE_SOAP_ACTION_GET_STATUS=urn:getStatus
OSE_USERNAME=20100119065MODDATOS
OSE_PASSWORD=moddatos
```

```env
# SUNAT beta directa
OSE_ENDPOINT_URL=https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService
OSE_SOAP_ACTION_SEND_BILL=sendBill
OSE_SOAP_ACTION_GET_STATUS=getStatus
OSE_USERNAME=20100119065MODDATOS
OSE_PASSWORD=moddatos
```

No mezclar endpoint y SOAPAction entre perfiles: si el endpoint es Estela, usar `urn:sendBill`; si es SUNAT beta directa, usar `sendBill`.

### Configurar certificado digital (PFX/P12)

El backend acepta **solo formato PKCS#12 (.pfx o .p12)**. Es el estándar que entregan SUNAT y las entidades autorizadas (CertiSurf, Idenperú, etc.). Almacena el certificado cifrado en la tabla `configuracion_certificado`.

#### 1. Convertir el archivo .pfx a Base64

```bash
# Linux
base64 -w 0 certificado.p12 > certificado.p12.base64

# macOS
base64 -i certificado.p12 -o certificado.p12.base64
```

#### 2. Guardarlo via API REST

```bash
curl -X POST http://localhost:8080/api/v1/configuracion-certificado \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "rucEmisor": "20100119065",
    "certificadoBase64": "PEGAR_BASE64_DEL_PFX",
    "password": "CONTRASENA_DEL_PFX",
    "aliasCertificado": "ALIAS_SI_APLICA",
    "fechaVigencia": "2027-12-31"
  }'
```

- `password`: contraseña del archivo .pfx, el backend la cifra automáticamente con AES-256 usando `ENCRYPTION_KEY`.
- `aliasCertificado`: **opcional**. Si se omite, el sistema usa el primer alias del keystore que contenga una clave privada.
- `fechaVigencia`: **opcional**. Fecha de expiración del certificado para futuras alertas de renovación.

#### 3. Una vez configurado

El próximo `POST /generar-xml` o `POST /ose/enviar-xml` para ese RUC firmará automáticamente el XML. No requiere reiniciar el servidor ni tocar configuración.

### Certificado de desarrollo vs producción

| | Autofirmado (dev) | Real SUNAT (prod) |
| --- | --- | --- |
| **Generación** | `keytool` o `openssl` | Entidad certificadora autorizada |
| **Aceptación OSE** | ❌ Rechazado | ✅ Aceptado |
| **Validez** | Técnica (prueba flujo XMLDSig) | Tributaria (comprobante válido) |

> ⚠️ Un certificado autofirmado sirve solo para probar que el XML queda firmado con XMLDSig. El OSE/SUNAT lo rechazará porque no fue emitido por una entidad autorizada.

#### Generar certificado local de demo

El certificado local queda en `.local-certs/`, una carpeta ignorada por Git. No subas ni commitees archivos `.p12`, `.pfx`, `.pem`, `.key`, `.crt` o contraseñas reales.

```bash
keytool -genkeypair \
  -alias autonomiflow-demo \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore .local-certs/autonomiflow-demo-signing.p12 \
  -storepass changeit \
  -keypass changeit \
  -validity 365 \
  -dname "CN=AutonomiFlow Demo, OU=Dev, O=AutonomiFlow, L=Lima, ST=Lima, C=PE"
```

Valores para probar en local:

| Campo | Valor de demo |
| ----- | ------------- |
| Archivo | `.local-certs/autonomiflow-demo-signing.p12` |
| Contraseña | `changeit` |
| Alias | `autonomiflow-demo` |

#### Cargarlo desde OSE Sender

1. Levantá backend y frontend.
2. Entrá a **Envío a OSE** (`/ose-sender`).
3. En **Certificado Digital**, ingresá el RUC emisor y presioná **Consultar**.
4. Si no hay certificado, presioná **Configurar ahora**.
5. Seleccioná `.local-certs/autonomiflow-demo-signing.p12`, ingresá la contraseña `changeit` y el alias `autonomiflow-demo`.
6. Guardá el certificado y generá un XML para ese RUC.

El XML esperado incluirá una firma similar a:

```xml
<ds:Signature>
  <ds:SignedInfo>...</ds:SignedInfo>
  <ds:SignatureValue>...</ds:SignatureValue>
  <ds:KeyInfo>...</ds:KeyInfo>
</ds:Signature>
```

Si intentás enviarlo al OSE/SUNAT, el XML puede estar técnicamente firmado, pero será rechazado por usar un certificado autofirmado de demo.

### Envío XML firmado

El nombre del archivo debe seguir el formato SUNAT:

```text
RUC-TIPO-SERIE-CORRELATIVO.xml
20100119065-01-F123-00012506.xml
```

```bash
curl -X POST http://localhost:8080/api/v1/ose/enviar-xml \
  -F "archivo=@20100119065-01-F123-00012506.xml"
```

El backend valida el nombre del archivo. Si el archivo subido no cumple el formato SUNAT, intenta reconstruirlo desde el XML usando `AccountingSupplierParty`, el tipo de documento y el `cbc:ID`; si no puede reconstruirlo, rechaza el comprobante.

### Gestión desde el frontend

La configuración del certificado se realiza desde el frontend en **Envío a OSE** (`/ose-sender`):

- Subir el archivo .pfx/.p12 desde el navegador
- Ingresar la contraseña
- Ver estado del certificado (vigente, próximo a vencer, vencido)
- Eliminar/reemplazar certificado

---

## ⚙️ Requisitos previos

- JDK 21
- Node.js v20+
- npm
- Variables de entorno configuradas
