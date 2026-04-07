# API REST - Facturación Electrónica

API REST desarrollada con Java y Spring Boot para generación, descarga e importación de tramas TXT de comprobantes electrónicos (Factura, Boleta, Nota de Crédito, Nota de Débito y Guía de Remisión), además de catálogos y utilidades de soporte para frontend.

## Tabla de contenido

- Descripción general
- Stack tecnológico
- Estructura del proyecto
- Requisitos
- Ejecución local
- Documentación OpenAPI (Swagger)
- Endpoints principales
- Ejemplos de uso
- Validaciones de negocio
- Pruebas
- Convenciones de respuestas y errores

## Descripción general

La API expone servicios para:

- Generar trama TXT desde un payload por secciones.
- Descargar trama TXT como archivo.
- Importar una trama TXT y convertirla nuevamente a JSON por secciones.
- Consultar catálogos tributarios y operativos (documentos, monedas, IGV, unidades, etc.).
- Consultar ubigeo (departamento, provincia, distrito, ubigeo).
- Obtener plantillas base por tipo de documento.

Nota importante (actualización):

Los catálogos que antes provenían de enums en tiempo de ejecución ahora se sirven desde la base de datos mediante entidades JPA y repositorios Spring Data JPA.
Se añadieron las siguientes entidades bajo `com.facturacion.api.web.models` y sus repositorios en `com.facturacion.api.web.repositories`:

- AeropuertosPeruEntity
- PuertosPeruEntity
- MonedaEntity
- UbigeoEntity
- UnidadesMedidaEntity
- TipoDocumentoIdentidadEntity
- TipoNotaCreditoEntity
- TipoNotaDebitoEntity
- TipoOperacionEntity
- TipoAfectacionIgvEntity
- CodigoTipoTributoEntity
- DocumentosRelacionadosTransporteEntity
- MotivoTrasladoEntity

Esto permite mantener los catálogos dinámicamente y mantener la consistencia con la base de datos Postgres.

## Stack tecnológico

- Java 21
- Spring Boot 4.0.4
- Spring Web MVC
- Spring Validation (Bean Validation)
- springdoc-openapi (Swagger UI)
- Maven Wrapper
- JUnit (módulos de test del proyecto)

## Estructura del proyecto

El backend REST está en:

- `Gestion_Facturacion_Electronica/api`

Paquetes clave:

- `application`: casos de uso y validaciones (`TramaService`, `RequestValidationService`, etc.)
- `web/controller`: endpoints REST
- `web/dto`: contratos de entrada/salida
- `core`: catálogo y modelo de dominio
- `resources/BD`: recursos de catálogos (por ejemplo ISO-4217)

## Requisitos

- JDK 21
- Maven 3.9+ (opcional si usas wrapper)

## Ejecución local

Desde la carpeta `api`:

```bash
./mvnw spring-boot:run
```

Compilar:

```bash
./mvnw clean compile
```

Ejecutar pruebas:

```bash
./mvnw test
```

## Documentación OpenAPI (Swagger)

Con la app levantada:

- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Endpoints principales

Base URL:

`http://localhost:8080/api/v1`

### Tramas

- `POST /tramas/generar`
	: Genera la trama TXT y la devuelve en JSON.
- `POST /tramas/descargar`
	: Devuelve el TXT como `attachment`.
- `POST /tramas/importar`
	: Recibe archivo `.txt` y lo convierte a payload por secciones.

### Catálogos

- `GET /catalogos/tipos-documento`
- `GET /catalogos/tipos-operacion`
- `GET /catalogos/tipos-afectacion-igv`
- `GET /catalogos/tipos-documento-identidad`
- `GET /catalogos/codigos-tipo-tributo`
- `GET /catalogos/monedas`
- `GET /catalogos/unidades-medida`
- `GET /catalogos/motivos-traslado`
- `GET /catalogos/puertos`
- `GET /catalogos/aeropuertos`
- `GET /catalogos/series/{tipoDocumento}`
- `GET /catalogos/tipos-nota-credito`
- `GET /catalogos/tipos-nota-debito`

### Ubigeo

- `GET /catalogos/ubigeo/departamentos`
- `GET /catalogos/ubigeo/provincias?departamento={nombre}`
- `GET /catalogos/ubigeo/distritos?departamento={nombre}&provincia={nombre}`
- `GET /catalogos/ubigeo/buscar?ubigeo={codigo}`

### Plantillas

- `GET /plantillas/{slug}`

## Ejemplos de uso

### 1) Generar trama (JSON)

```bash
curl -X POST "http://localhost:8080/api/v1/tramas/generar" \
	-H "Content-Type: application/json" \
	-d '{
		"tipoDocumento": "01",
		"secciones": {
			"campos": {
				"A": {
					"RUTEmis": "20123456789",
					"TipDocu": "01",
					"Serie": "F001",
					"Correlativo": "00000001"
				}
			},
			"listas": {
				"B": []
			}
		}
	}'
```

### 2) Descargar trama TXT

```bash
curl -X POST "http://localhost:8080/api/v1/tramas/descargar" \
	-H "Content-Type: application/json" \
	-d '{"tipoDocumento":"01","secciones":{"campos":{"A":{"RUTEmis":"20123456789","TipDocu":"01","Serie":"F001","Correlativo":"00000001"}},"listas":{"B":[]}}}' \
	-o trama.txt
```

### 3) Importar trama TXT

```bash
curl -X POST "http://localhost:8080/api/v1/tramas/importar" \
	-F "file=@./api/Ejemplos/20259829594-01-F023-00000651.txt"
```

## Validaciones de negocio

La API aplica validaciones estructurales y de reglas tributarias, incluyendo consistencia entre:

- `MontoItem`
- `CodigoTipoIgv`
- `TasaIgv`
- `ImpuestoIgv`
- Totales por secciones

Conceptos tributarios usados en resumen de montos:

- `MntNeto`: monto total gravado.
- `MntExe`: monto total no gravado/inafecto.
- `MntExo`: monto total exonerado.

## Pruebas

Casos cubiertos (paquete `api/src/test/java/com/facturacion/api/application`):

- `FacturaBoletaExamplesTest`
- `GuiaRemisionValidationTest`
- `IgvAfectacionValidationTest`
- `RequestValidationServiceTest`
- `ValidationUtilsTest`

Ejecución:

```bash
./mvnw test
```

## Convenciones de respuestas y errores

- Respuestas exitosas con estructura DTO por endpoint.
- Errores de validación y negocio retornan `400 Bad Request` con payload de error (`ErrorResponse`).
- En descarga de TXT se usa `Content-Disposition: attachment` con nombre de archivo basado en:
	`ruc-tipoDoc-serie-correlativo.txt`.
