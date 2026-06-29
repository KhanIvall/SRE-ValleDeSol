# Informe de pruebas unitarias — SRE Valle del Sol (EP3)

**Asignatura:** DSY1106 — Desarrollo Fullstack III  
**Requisito pauta:** cobertura mínima del **60%** en todos los componentes  
**Fecha de medición:** junio 2026 — rama `feature/ep3-entrega`

## 1. Resumen ejecutivo

| Componente | Herramienta | Líneas | Instrucciones | Ramas | Cumple ≥60% |
|------------|-------------|--------|---------------|-------|-------------|
| `incidentes` | JaCoCo 0.8.11 | **74,5%** | 71,3% | 46,2% | Sí (líneas) |
| `recursos` | JaCoCo 0.8.11 | **66,3%** | 64,5% | 54,5% | Sí |
| `zonasriesgo` | JaCoCo 0.8.11 | **87,0%** | 86,2% | 56,7% | Sí |
| `bff` | JaCoCo 0.8.11 | **97,3%** | 98,2% | 75,0% | Sí |
| `sre-ui` (hooks/services) | Vitest + v8 | **80,1%** | 80,1% | 71,8% | Sí |

Todos los módulos evaluados superan el **60% en cobertura de líneas**, criterio principal de la rúbrica EP3.

## 2. Cómo ejecutar las pruebas

### Backend (Maven + JaCoCo)

Desde la raíz del monorepo:

```bash
# Todos los microservicios y BFF
mvn test -pl businessdomain/incidentes,businessdomain/recursos,businessdomain/zonasriesgo,infraestructuredomain/bff -am

# Un módulo
mvn -pl businessdomain/incidentes test
mvn -pl infraestructuredomain/bff test
```

**Reportes HTML JaCoCo** (generados en `target/site/jacoco/index.html` de cada módulo):

- `businessdomain/incidentes/target/site/jacoco/index.html`
- `businessdomain/recursos/target/site/jacoco/index.html`
- `businessdomain/zonasriesgo/target/site/jacoco/index.html`
- `infraestructuredomain/bff/target/site/jacoco/index.html`

### Frontend (Vitest + cobertura)

```bash
cd frontend/sre-ui
npm install --legacy-peer-deps
npm run test:coverage
```

Reporte HTML: `frontend/sre-ui/coverage/index.html`

## 3. Inventario de pruebas por componente

### 3.1 Microservicio incidentes

| Tipo | Clase | Descripción |
|------|-------|-------------|
| Unitaria | `EstadoIncidenteFactoryTest` | Factory Method — transiciones de estado |
| Unitaria | `IncidenteServiceTest` | Crear y cambiar estado con mocks |
| Integración | `IncidenteIntegrationTest` | MockMvc + H2 — persistencia real |
| E2E | `IncidenteE2ETest` | Flujo REST completo |

### 3.2 Microservicio recursos

| Tipo | Clase | Descripción |
|------|-------|-------------|
| Unitaria | `RecursoServiceTest` | Reglas de asignación |
| Integración | `RecursoIntegrationTest` | CRUD y asignación en H2 |
| E2E | `RecursoE2ETest` | API REST end-to-end |

### 3.3 Microservicio zonasriesgo

| Tipo | Clase | Descripción |
|------|-------|-------------|
| Unitaria | `FakeWeatherAdapterTest` | Patrón Adapter — clima simulado |
| Unitaria | `ZonaRiesgoServiceTest` | Cálculo de nivel de riesgo |
| Unitaria | `ZonaRiesgoRecalibrarTest` | Recalibración |
| Integración | `ZonaRiesgoIntegrationTest` | Persistencia JPA |
| E2E | `ZonaRiesgoE2ETest` | API REST |

### 3.4 BFF

| Tipo | Clase | Descripción |
|------|-------|-------------|
| Unitaria | `EmergenciaFacadeServiceTest` | Fallback de contingencia |
| Integración | `EmergenciaFacadeIntegrationTest` | Facade real + MockWebServer |
| E2E | `BffEmergenciaE2ETest` | `GET /bff/emergencias/{id}/resumen` |
| Smoke | `BffApplicationTests` | Contexto Spring |

### 3.5 Frontend `@valledelsol/sre-ui`

| Archivo | Qué prueba |
|---------|------------|
| `emergenciaApi.test.js` | Facade HTTP — éxito y error |
| `useEmergenciaResumen.test.js` | Custom Hook — carga y errores |
| `useIncidentesActivos.test.js` | Polling de focos activos |
| `useWeather.test.js` | Barra meteorológica (fetch mock) |

> La cobertura frontend se mide sobre `src/hooks`, `src/services` y `src/context` (lógica testeable). Los componentes visuales (`Dashboard`, mapa Leaflet) se validan en demo integrada.

## 4. Ejemplos de resultados

### Ejemplo 1 — Factory Method (incidentes)

`EstadoIncidenteFactoryTest` verifica que no se puede pasar a `EN_PROGRESO` sin coordenadas GPS, alineado al requisito municipal.

### Ejemplo 2 — Circuit Breaker (BFF)

`EmergenciaFacadeServiceTest.resumenFallback_retornaDatosContingencia` confirma que ante fallo el operador recibe `datosContingencia: true` y tipo `CONTINGENCIA`.

### Ejemplo 3 — Persistencia (incidentes)

`IncidenteIntegrationTest.postIncidente_datosValidos_retorna201YPersiste`:

- HTTP 201 Created
- Cuerpo JSON con `id` asignado
- Registro recuperable vía `IncidenteRepository`

### Ejemplo 4 — Frontend Hook

`useIncidentesActivos.test.js` valida filtrado de incidentes `CERRADO` y estado `online`/`offline`.

## 5. Patrones de diseño cubiertos por pruebas

| Patrón | Componente | Test que lo evidencia |
|--------|------------|------------------------|
| Factory Method | incidentes | `EstadoIncidenteFactoryTest` |
| Repository | los 3 MS | Tests de integración con JPA |
| Adapter | zonasriesgo | `FakeWeatherAdapterTest` |
| Facade | BFF + frontend | `EmergenciaFacadeIntegrationTest`, `emergenciaApi.test.js` |
| Circuit Breaker | BFF | `EmergenciaFacadeServiceTest` |
| Custom Hook | frontend | `useEmergenciaResumen.test.js` |

## 6. Notas

- Los reportes JaCoCo/Vitest se generan al ejecutar los comandos anteriores; inclúyelos en el ZIP de entrega exportando las carpetas `target/site/jacoco` y `coverage/`.
- Spring Boot 4 requiere `spring-boot-webtestclient` en el BFF para compilar tests E2E con `WebTestClient`.
