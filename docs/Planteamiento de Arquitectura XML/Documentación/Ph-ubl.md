# Documentación: `ph-ubl` + `ph-ublpe`
> Librería Java para generación de XML UBL 2.1 — Facturación Electrónica SUNAT Perú

---

## 📦 Repositorio Principal (Código Fuente)

| Recurso | URL |
|---|---|
| **GitHub ph-ubl** (README + código fuente completo) | https://github.com/phax/ph-ubl |
| **README.md** con instrucciones de uso | https://github.com/phax/ph-ubl/blob/master/README.md |
| **Releases / Changelog** | https://github.com/phax/ph-ubl/releases |
| **Issues / Soporte** | https://github.com/phax/ph-ubl/issues |

---

## 🔬 Código Fuente Clave (Ejemplos Oficiales)

> El ejemplo oficial para crear una factura desde cero y para leer/escribir archivos UBL 2.1 se encuentran en los archivos de test del proyecto.

| Archivo | URL directa |
|---|---|
| **Crear Invoice desde cero** (ejemplo oficial) | https://github.com/phax/ph-ubl/blob/master/ph-ubl21/src/test/java/com/helger/ubl21/CreateInvoiceFromScratchFuncTest.java |
| **Leer y escribir UBL 2.1** (test funcional) | https://github.com/phax/ph-ubl/blob/master/ph-ubl21/src/test/java/com/helger/ubl21/UBL21FuncTest.java |
| **Código fuente ph-ubl21** (clases principales) | https://github.com/phax/ph-ubl/tree/master/ph-ubl21/src/main/java/com/helger/ubl21 |
| **EUBL21DocumentType** (tipos de documento soportados) | https://github.com/phax/ph-ubl/blob/master/ph-ubl21/src/main/java/com/helger/ubl21/EUBL21DocumentType.java |
| **Código fuente ph-ublpe** (extensiones Perú/SUNAT) | https://github.com/phax/ph-ubl/tree/master/ph-ublpe |

---

## 🗄️ Maven Central (Descargar JARs)

| Artefacto | URL en Maven Central |
|---|---|
| **ph-ubl21** | https://repo1.maven.org/maven2/com/helger/ubl/ph-ubl21/ |
| **ph-ublpe** | https://repo1.maven.org/maven2/com/helger/ubl/ph-ublpe/ |
| **ph-ubl21-codelists** | https://repo1.maven.org/maven2/com/helger/ubl/ph-ubl21-codelists/ |

### Dependencias Maven (versión actual: `10.1.0`)

```xml
<!-- ph-ubl21: Clases JAXB estándar UBL 2.1 -->
<dependency>
    <groupId>com.helger.ubl</groupId>
    <artifactId>ph-ubl21</artifactId>
    <version>10.1.0</version>
</dependency>

<!-- ph-ublpe: Extensiones JAXB específicas SUNAT Perú -->
<dependency>
    <groupId>com.helger.ubl</groupId>
    <artifactId>ph-ublpe</artifactId>
    <version>10.1.0</version>
</dependency>

<!-- ph-ubl21-codelists: Enums tipados para catálogos UBL 2.1 (opcional) -->
<dependency>
    <groupId>com.helger.ubl</groupId>
    <artifactId>ph-ubl21-codelists</artifactId>
    <version>10.1.0</version>
</dependency>
```

> ⚠️ **Requisito:** Java 17 o superior para `v10.x`. La última versión compatible con Java 11 fue `9.0.3`.

---

## 📖 Javadoc (Referencia de API)

| Artefacto | URL Javadoc |
|---|---|
| **ph-ubl21** | https://javadoc.io/doc/com.helger.ubl/ph-ubl21 |
| **ph-ublpe** | https://javadoc.io/doc/com.helger.ubl/ph-ublpe |

---

## 🔎 Maven Repository (Versiones y Dependencias)

| Artefacto | URL |
|---|---|
| **ph-ubl21** en mvnrepository | https://mvnrepository.com/artifact/com.helger.ubl/ph-ubl21 |
| **ph-ublpe** en mvnrepository | https://mvnrepository.com/artifact/com.helger.ubl/ph-ublpe |
| **Todos los artefactos `com.helger.ubl`** | https://mvnrepository.com/artifact/com.helger.ubl |
| **ph-ubl21** en Sonatype Central | https://central.sonatype.com/artifact/com.helger.ubl/ph-ubl21 |

---

## 🏛️ Estándar UBL 2.1 Oficial (OASIS)

| Recurso | URL |
|---|---|
| **Especificación UBL 2.1 OASIS** | https://docs.oasis-open.org/ubl/os-UBL-2.1/ |
| **XSDs UBL 2.1** (esquemas XML oficiales) | https://docs.oasis-open.org/ubl/os-UBL-2.1/xsdrt/ |
| **Datypic UBL 2.1 Schema Reference** (referencia visual de todos los campos) | http://www.datypic.com/sc/ubl21/ss.html |

---

## 🇵🇪 Recursos SUNAT Perú

| Recurso | URL |
|---|---|
| **Portal SUNAT CPE** (comprobantes electrónicos) | https://cpe.sunat.gob.pe |
| **Catálogos y guías técnicas para desarrolladores** | https://cpe.sunat.gob.pe/informacion_general/desarrolladores |
| **Guía XML Boleta Electrónica UBL 2.1** | https://cpe.sunat.gob.pe/sites/default/files/inline-files/guia+xml+boleta+version%202-1+1+0_0_0%20(2).pdf |

---

## 📝 Documentación Complementaria

| Recurso | URL |
|---|---|
| **¿Qué es UBL?** (doc interna del proyecto ph-ubl) | https://github.com/phax/ph-ubl/blob/master/docs/What%20is%20UBL.htm |
| **Sitio oficial OASIS UBL** | https://www.oasis-open.org/committees/ubl/ |

---

## 🗂️ Historial de Versiones Relevante

| Versión | Fecha | Cambio clave |
|---|---|---|
| **10.1.0** | Nov 2025 | Actualización a ph-commons 12.1.0, anotaciones JSpecify |
| **10.0.0** | Ago 2025 | **Java 17 requerido como mínimo** |
| **9.0.3** | Sep 2024 | API extendida en `EUBL2*DocumentType` |
| **9.0.2** | Jul 2024 | Actualización de dependencias |
| **9.0.0** | Mar 2024 | Soporte UBL 2.4 añadido |
| **8.0.0** | Abr 2023 | Migración de XSDs a ruta `external/` |

---

## ⚠️ Notas Importantes para SUNAT

- La librería genera XML **estructuralmente válido** según el esquema UBL 2.1.
- Es **responsabilidad del desarrollador** aplicar los catálogos SUNAT actualizados (catálogo 01, 06, 51, 53, etc.).
- SUNAT actualizó sus **Reglas de Validación CPE en abril 2025**, con validaciones más estrictas y ajustes en estructuras XML.
- El XML generado debe ser **firmado digitalmente** con un certificado `.pfx` antes de enviarlo a SUNAT. Para eso usar `org.apache.santuario:xmlsec`.

---

## 💡 Tip Final

Para la parte peruana específica, el archivo más útil es el código fuente de `ph-ublpe` directamente en GitHub, que muestra exactamente qué namespaces y tipos SUNAT están modelados:

👉 https://github.com/phax/ph-ubl/tree/master/ph-ublpe/src/main/java