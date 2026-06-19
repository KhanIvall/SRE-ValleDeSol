---
marp: true
theme: default
paginate: true
size: 16:9
header: 'SRE Valle del Sol — DSY1106 EP2'
footer: 'Skarlett Tropan · Ari Araya · 4 jun 2026'
style: |
  section.lead h1 { font-size: 1.6em; }
  section.lead h2 { font-size: 1.1em; font-weight: normal; color: #444; }
  .logos { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
  .logos img { height: 72px; width: auto; object-fit: contain; }
  table { font-size: 0.78em; }
  blockquote { font-size: 0.85em; border-left: 4px solid #2563eb; }
---

<!-- _class: lead -->

<div class="logos">
  <img src="image_d000c2.png" alt="Duoc UC" />
  <img src="image_d003ed.png" alt="Municipalidad Valle del Sol" />
</div>

# SRE Valle del Sol

## Sistema de Respuesta de Emergencias

**DSY1106 — Desarrollo Fullstack III**  
Evaluación Parcial 2

### Skarlett Tropan · Ari Araya

<!-- Notas:
**Skarlett (~30 s):** Saludar al docente. Presentarnos como Skarlett Tropan y Ari Araya. Indicar que exponemos el SRE de la Municipalidad Valle del Sol, continuación del caso EP1.
**Ari (~30 s):** Comentar que en EP2 implementamos frontend NPM y backend con BFF y microservicios, y que hoy explicamos arquitectura, patrones y trabajo colaborativo con Git.
-->

---

## ¿Qué resuelve el SRE?

- **Panel único** para operadores: incidente + recursos + zona de riesgo
- Ciclo de vida del incidente: `Reportado → En progreso → Controlado → Cerrado`
- Logística **desacoplada** (`incidenteAsignadoId`, sin FK entre microservicios)
- **Tolerancia a fallas**: respuesta de contingencia si un servicio no responde

> Continuidad EP1: gestión operativa de emergencias municipales

<!-- Notas:
**Skarlett (~45 s):** El municipio necesita coordinar brigadas con información geográfica y de riesgo. En EP2 materializamos eso en código: monorepo con tres microservicios de dominio más capa de agregación.
**Ari (~45 s):** El operador no debe consumir tres APIs. Necesita un resumen en una consulta. Si un servicio cae, el panel debe mostrar modo contingencia —no solo un error HTTP— para mantener continuidad operativa.
-->

---

## Arquitectura del monorepo

```
sre-ui (React NPM) → API Gateway :8080 → BFF :8085
                              ↓
        incidentes · recursos · zonasriesgo  (Eureka :8761)
```

| Capa | Componente | Rol |
|------|------------|-----|
| Presentación | `@valledelsol/sre-ui` | Panel React empaquetado NPM |
| Perímetro | Spring Cloud Gateway | Entrada única `/bff/**` |
| Agregación | BFF + WebClient | Un DTO: `EmergenciaResumenDto` |
| Dominio | 3 microservicios | Bounded contexts independientes |

<!-- Notas:
**Ari (~1 min):** Arquitectura de microservicios. El frontend solo habla con el BFF vía Gateway. EmergenciaFacadeService consulta en paralelo incidentes, recursos y zonas de riesgo y arma el resumen.
**Skarlett (~1 min):** Cada MS tiene su persistencia y se registra en Eureka. Recursos referencia incidentes por ID lógico, sin compartir base de datos. Eso permite escalar y desplegar dominios por separado.
-->

---

## Patrones de diseño — Backend

| Patrón | Módulo | Problema que resuelve |
|--------|--------|------------------------|
| **Factory Method** | `incidentes` | Reglas distintas por estado (ej. GPS antes de `EN_PROGRESO`) |
| **Adapter** | `zonasriesgo` | Proveedor climático intercambiable (`WeatherDataPort`) |
| **Facade** | `bff` | Una API agregada para el panel |
| **Repository** | los 3 MS | Separar negocio de persistencia JPA |

**Regla de negocio:** código `INC-002` — sin coordenadas no hay despacho de brigadas

<!-- Notas:
**Skarlett (~1 min):** Factory Method con EstadoIncidenteFactory y handlers por estado; evitamos un switch centralizado. EnProgresoEstadoHandler valida lat/lon antes de EN_PROGRESO.
**Ari (~50 s):** Adapter en zonasriesgo: el servicio usa WeatherDataPort; FakeWeatherAdapter simula clima en desarrollo. Podemos cambiar proveedor sin tocar ZonaRiesgoService.
-->

---

## BFF, resiliencia y frontend NPM

### Backend — flujo crítico

1. `GET /bff/emergencias/{id}/resumen`
2. Consultas **paralelas** (WebClient + `Mono.zip`)
3. **Circuit Breaker** → `resumenFallback` → `datosContingencia: true`

### Frontend — 4 patrones

| Patrón | Archivo |
|--------|---------|
| Facade | `emergenciaApi.js` |
| Custom Hook | `useEmergenciaResumen.js` |
| Provider | `AlertContext.jsx` |
| Compound Components | `EmergenciaPanel.jsx` |

<!-- Notas:
**Ari (~1 min):** Resilience4j en el BFF: si hay timeout, resumenFallback devuelve datos degradados y el panel muestra badge Contingencia en EmergenciaPanel.Header.
**Skarlett (~1 min):** En sre-ui el Facade oculta URLs y JWT; el Hook encapsula loading/error; Provider evita prop drilling en alertas; Compound Components componen Header/Body/Footer con contexto compartido.
-->

---

## Patrones arquitectónicos y arquetipos Maven

### Arquitectura

- Microservicios por dominio · **BFF** · **API Gateway** · **Eureka**
- **Circuit Breaker** (Resilience4j) · Capas `Controller → Service → Repository`

### Arquetipos (`archetypes/`)

| Arquetipo | Genera |
|-----------|--------|
| `sre-microservice-archetype` | incidentes, recursos, zonasriesgo |
| `sre-bff-archetype` | módulo BFF |

`mvn archetype:generate` — estructura homogénea en el monorepo `sre-parent`

<!-- Notas:
**Skarlett (~1 min):** Los arquetipos estandarizan capas, dependencias y registro Eureka. sre-microservice-archetype para dominio; sre-bff-archetype incluye WebFlux, WebClient y base para Facade con resiliencia.
**Ari (~30 s):** Los patrones arquitectónicos complementan los de diseño: Gateway como entrada única, Eureka sin URLs fijas, BFF como capa anti-corrupción hacia el frontend NPM.
-->

---

## Plan de branching — Git Flow simplificado

```
feature/ep2-*  ──PR (squash)──►  develop  ──PR (merge)──►  main
```

| Rama | Propósito |
|------|-----------|
| `main` | Código estable para entrega y demo |
| `develop` | Integración del equipo EP2 |
| `feature/*` | Trabajo por módulo (vida corta) |

- **Commits:** Conventional Commits (`feat(ep2):`, `docs(ep2):`)
- **Regla:** ningún merge sin revisión del compañero
- **Evidencia:** PR #2, #3 y #4 en GitHub

<!-- Notas:
**Skarlett (~1 min):** Elegimos Git Flow simplificado: develop integra antes de main. Descartamos trunk-based puro por riesgo de conflictos en monorepo con trabajo paralelo. Yo: MS dominio, arquetipos y gateway. Ari: BFF, frontend y docs.
**Ari (~1 min):** Todo entra por Pull Request. Squash a develop, merge commit a main. Commits como feat(ep2): arquetipos Maven. PRs documentados en el plan de branching para la defensa.
-->

---

## Conflicto resuelto — PR #3

| | |
|---|---|
| **Archivo** | `pom.xml` raíz (`sre-parent`) |
| **Origen** | PR #2 (módulo `archetypes`) vs rama `feature/ep2-readmes-tests` |
| **Causa** | Edición concurrente de `<modules>` y `<properties>` |
| **Solución** | Unificar 3 módulos + conservar `maven.surefire.version` |
| **Validación** | `mvn -q validate` + tests del alcance |

```bash
git merge origin/develop   # CONFLICT in pom.xml
git add pom.xml
git commit -m "fix(ep2): resolver conflicto con develop en pom.xml"
```

<!-- Notas:
**Ari (~45 s):** Al sincronizar mi rama con develop antes del PR #3, Git marcó conflicto en pom.xml: yo agregué Surefire y reordené modules; Skarlett ya había integrado archetypes en el PR #2.
**Skarlett (~45 s):** Acordamos por chat: tres modules sin duplicar, orden por capa, y mantener la property de tests de Ari. Validamos con mvn validate; yo revisé el PR antes del squash. Quedó en el plan de branching como evidencia.
-->

---

## Calidad, pruebas y cierre

### Pruebas unitarias

| Módulo | Tests |
|--------|-------|
| incidentes | `EstadoIncidenteFactoryTest`, `IncidenteServiceTest` |
| zonasriesgo | `FakeWeatherAdapterTest`, `ZonaRiesgoServiceTest` |
| bff | `EmergenciaFacadeServiceTest` (fallback) |
| sre-ui | `useEmergenciaResumen.test.js`, `emergenciaApi.test.js` |

### Conclusiones

- ≥4 patrones en **frontend y backend**, justificados por problema real
- Arquitectura **escalable**: BFF + microservicios + arquetipos Maven
- Colaboración trazable: **Git, PRs y conflicto documentado**

**Repo:** https://github.com/KhanIvall/SRE-ValleDeSol

### ¿Preguntas?

<!-- Notas:
**Ari (~45 s):** Buenas prácticas: DTOs en BFF separados de entidades JPA, excepciones tipadas, README por módulo. Tests cubren transiciones de estado, adapter, fallback del BFF y hook del frontend.
**Skarlett (~45 s):** Cerramos: la solución es mantenible y alineada al municipio. Patrones no son decorativos —resuelven despacho sin GPS, clima cambiante y panel único. Quedamos atentas a preguntas. Gracias.
-->
