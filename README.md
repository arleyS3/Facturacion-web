<div align="center" style="margin-bottom: 1rem">
  <h1 style="margin: .4rem 0">Facturación Web</h1>
  <p style="margin: .2rem 0; font-size:1.05rem">Generación y descarga de tramas TXT para comprobantes electrónicos</p>
  <p>
    <img alt="Java" src="https://img.shields.io/badge/Java-21-informational?style=flat" />
    <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen?style=flat" />
    <img alt="React" src="https://img.shields.io/badge/React-18-blue?style=flat" />
    <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-%5E4.0-blue?style=flat" />
  </p>
</div>

Este repositorio contiene dos partes principales: el backend (API REST en Java Spring Boot) en <code>backend/api</code> y el frontEnd (aplicación web) en <code>frontEnd</code>.

<!-- Tecnologías -->
<h2>Stack de tecnologías</h2>

<table>
  <tr>
    <td valign="top" width="50%">
      <h3>Backend</h3>
      <ul>
        <li>Java 21</li>
        <li>Spring Boot 4.x (Web MVC, Validation)</li>
        <li>Maven (wrapper incluido)</li>
        <li>springdoc-openapi (Swagger)</li>
        <li>JUnit</li>
      </ul>
    </td>
    <td valign="top" width="50%">
      <h3>FrontEnd</h3>
      <ul>
        <li>Node.js (recomendado v20+), npm</li>
        <li>React 18 + TypeScript</li>
        <li>Vite</li>
        <li>React Router, React Hook Form, Axios</li>
        <li>Tailwind CSS, Radix UI, TanStack React Query</li>
      </ul>
    </td>
  </tr>
</table>

<h2>Estructura de ramas (convención)</h2>

<p>
  <a href="https://imgs.search.brave.com/XtBQ_bUrT8-3EfIIEa1GOuMDgVlB-Pcf6F_ttsWNHag/rs:fit:0:180:1:0/g:ce/aHR0cHM6Ly9jZG4u/aWNvbnNjb3V0LmNv/bS9pY29uL3ByZW1p/dW0vcG5nLTI1Ni10/aHVtYi9yYW1hLWlj/b24tc3ZnLWRvd25s/b2FkLXBuZy0zMjU4/NzIxLnBuZz9mPXdl/YnAmdz0xMjg" target="_blank" rel="noopener">
    <img alt="rama git" src="https://imgs.search.brave.com/XtBQ_bUrT8-3EfIIEa1GOuMDgVlB-Pcf6F_ttsWNHag/rs:fit:0:180:1:0/g:ce/aHR0cHM6Ly9jZG4u/aWNvbnNjb3V0LmNv/bS9pY29uL3ByZW1p/dW0vcG5nLTI1Ni10/aHVtYi9yYW1hLWlj/b24tc3ZnLWRvd25s/b2FkLXBuZy0zMjU4/NzIxLnBuZz9mPXdl/YnAmdz0xMjg" style="height:40px; vertical-align:middle; margin-right:8px;" />
  </a>

  <ul style="display:inline-block; vertical-align:middle; text-align:left;">
    <li><strong>main</strong> — rama de producción estable. Solo merges desde <code>develop</code> o <code>hotfix/*</code> aprobados.</li>
    <li><strong>develop</strong> — rama de integración para la próxima versión.</li>
    <li><strong>feature/&lt;nombre&gt;</strong> — desarrollo de nuevas funcionalidades (derivan de <code>develop</code>).</li>
    <li><strong>hotfix/&lt;nombre&gt;</strong> — correcciones urgentes (derivan de <code>main</code>, luego merge a <code>main</code> y <code>develop</code>).</li>
  </ul>

  <div style="margin-top:.6rem">Usa nombres descriptivos, por ejemplo <code>feature/emitir-trama</code> o <code>hotfix/fix-encoding-txt</code>.</div>
</p>

<h2>Ejecución local (orden recomendado)</h2>

<p>Se recomienda iniciar primero el backend y, una vez disponible, iniciar el frontEnd.</p>

<details>
<summary><strong>1) Backend (primero)</strong></summary>

<p>Requisitos: JDK 21. El backend está en <code>backend/api</code> y usa el wrapper de Maven.</p>

<pre><code># ir a la carpeta del API
cd src/backend/api

# ejecutar la aplicación
./mvnw spring-boot:run
</code></pre>

<p>Con la app levantada, la API está disponible en <code>http://localhost:8080/api/v1</code>.</p>

<p>Swagger / OpenAPI:</p>
<ul>
  <li><code>http://localhost:8080/swagger-ui</code></li>
  <li><code>http://localhost:8080/api-docs</code></li>
</ul>

</details>

<details>
<summary><strong>2) Frontend (después de levantar backend)</strong></summary>

<p>Requisitos: Node.js y npm.</p>

<pre><code>cd src/frontEnd
npm install
npm run dev
</code></pre>

<p>Variable de entorno importante:</p>
<ul>
  <li><code>VITE_API_BASE_URL</code> — por ejemplo <code>http://localhost:8080/api/v1</code></li>
</ul>

<p>El frontEnd espera que el backend esté disponible para las operaciones de generación/descarga de tramas.</p>

</details>

<h2>Resumen rápido</h2>

- Levantar backend: <code>cd backend/api &amp;&amp; ./mvnw spring-boot:run</code>
- Levantar frontend: <code>cd frontEnd &amp;&amp; npm install &amp;&amp; npm run dev</code>
- Ramas: usa <code>develop</code> para integración, <code>feature/*</code> para trabajo en curso, <code>hotfix/*</code> para arreglos urgentes y <code>main</code> para producción.

