
  # FrontEnd - Facturación Web

  Aplicación web para generar tramas de comprobantes electrónicos y guías de remisión, con una interfaz moderna y orientada al flujo operativo.

  Este frontEnd consume servicios HTTP del backend para construir payloads, validarlos y descargar archivos TXT listos para su procesamiento.

  ## Tabla de contenido

  1. Descripción general
  2. Tecnologías
  3. Requisitos previos
  4. Instalación y ejecución
  5. Variables de entorno
  6. Funcionalidades actuales
  7. Estructura del proyecto
  8. Flujo funcional
  9. Scripts disponibles
  10. Notas y recomendaciones

  ## 1. Descripción general

  El proyecto está construido con React + Vite y presenta dos módulos principales:

  - Documentos de venta
  - Guías de remisión

  Ambos módulos permiten completar formularios, transformar la información a la estructura esperada por el backend y descargar la trama TXT generada.

  ## 2. Tecnologías

  Tecnologías principales:

  - React 18
  - TypeScript
  - Vite 6
  - React Router 7
  - React Hook Form
  - Axios
  - Tailwind CSS 4
  - Componentes UI basados en Radix UI
  - Lucide React (iconografía)

  Herramientas y librerías de soporte relevantes:

  - TanStack React Query (consultas y caché)
  - MUI (dependencias disponibles en el proyecto)
  - Sonner (notificaciones)

  ## 3. Requisitos previos

  - Node.js 20 o superior recomendado
  - npm 10 o superior recomendado
  - Backend disponible para consumir endpoints de emisión y descarga

  ## 4. Instalación y ejecución

  Instalar dependencias:

  npm install

  Iniciar entorno de desarrollo:

  npm run dev

  Generar build de producción:

  npm run build

  ## 5. Variables de entorno

  El proyecto utiliza la variable:

  - VITE_API_BASE_URL

  Ejemplo local (.env):

  VITE_API_BASE_URL=http://localhost:8080/api/v1

  Ejemplo producción (.env.production):

  VITE_API_BASE_URL=https://gestionfacturacionelectronica-production.up.railway.app/api/v1

  Adicionalmente existe un endpoint serverless para consulta SUNAT en api/sunat/ruc.js, que utiliza:

  - SUNAT_TOKEN (preferido)
  - VITE_SUNAT_TOKEN (fallback)
  - token por query/header como último recurso

  ## 6. Funcionalidades actuales

  ### Módulo Home

  - Pantalla de entrada con selección de flujo:
    - Documentos de venta
    - Guías de remisión

  ### Módulo Documentos de venta

  - Formulario por secciones:
    - Datos del documento
    - Datos del emisor
    - Datos del receptor
    - Tabla de productos
  - Construcción del payload para backend mediante buildPayload
  - Emisión de trama con llamada a:
    - POST /tramas/generar
  - Descarga de TXT con llamada a:
    - POST /tramas/descargar
  - Generación de nombre de archivo de descarga
  - Manejo de errores y visualización de validaciones de servidor

  ### Módulo Guías de remisión

  - Formulario por pestañas:
    - Información de guía
    - Detalle
    - Referencias
    - Tramo
    - Datos adicionales
  - Construcción del payload mediante buildShippingPayload
  - Emisión y descarga de trama TXT usando:
    - POST /tramas/generar
    - POST /tramas/descargar
  - Manejo de validaciones y errores de backend

  ### Integración SUNAT (proxy)

  - Endpoint GET api/sunat/ruc
  - Proxy hacia api.apis.net.pe para consulta de RUC
  - Control básico de método HTTP, parámetro numero y token

  ## 7. Estructura del proyecto

  Estructura relevante:

  - src/app/pages
    - Home.tsx
    - SalesDocuments.tsx
    - ShippingGuide.tsx
  - src/app/components
    - InvoiceForm.tsx
    - EmitDocumentDialog.tsx
    - Componentes de secciones de documento y guía
  - src/app/lib
    - api.ts (cliente Axios)
    - buildPayload.ts
    - buildShippingPayload.ts
    - downloadFilename.ts
    - emitErrorNotification.ts
  - api/sunat/ruc.js
    - endpoint serverless para consulta de RUC

  ## 8. Flujo funcional

  1. Usuario selecciona un módulo en Home.
  2. Completa formulario de documento o guía.
  3. El frontEnd arma el payload según el tipo de operación.
  4. Se envía a POST /tramas/generar para validación y generación.
  5. Si no hay errores de validación, se solicita TXT en POST /tramas/descargar.
  6. El navegador descarga el archivo con nombre sugerido.

  ## 9. Scripts disponibles

  - npm run dev: inicia el servidor de desarrollo con Vite
  - npm run build: genera el build de producción

  ## 10. Notas y recomendaciones

  - Mantener sincronizados los campos del formulario con los builders de payload.
  - Si cambian reglas del backend, actualizar primero buildPayload y buildShippingPayload.
  - Para producción, definir correctamente VITE_API_BASE_URL y SUNAT_TOKEN en el entorno de despliegue.
  - Se recomienda agregar pruebas unitarias para los builders de payload y validaciones de transformación.

  ---

  Si deseas, puedo ampliar este README con:

  1. Sección de troubleshooting (errores comunes y solución).
  2. Guía de despliegue (Vercel u otro proveedor).
  3. Contrato de API esperado por cada endpoint del backend.
  