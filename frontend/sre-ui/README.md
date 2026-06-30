# @valledelsol/sre-ui

Componentes frontend **NPM** (React 19 + Vite 6) para el **Sistema de Respuesta de Emergencias (SRE)** — Municipalidad Valle del Sol.

Panel **centro de operaciones** con mapa interactivo, focos en tiempo real y consulta al agregado del **BFF**: `GET /bff/emergencias/{id}/resumen`.

## Funcionalidades del panel

| Sección | Descripción |
|---------|-------------|
| **Barra meteorológica** | Clima en tiempo real (Open-Meteo): temperatura, viento, calidad del aire y reloj local |
| **Focos activos** | Lista de incidentes no cerrados, actualización cada 30 s, alerta sonora en nuevos `EN_PROGRESO` |
| **Mapa Leaflet** | Marcadores por estado, círculos de riesgo por zona y conos de propagación según viento |
| **Búsqueda por ID** | Consulta resumen BFF y acumula historial (hasta 10 incidentes) |
| **Detalle** | `EmergenciaPanel` + `ResumenDetalle`: incidente, zona de riesgo, recursos y modo contingencia |
| **Estadísticas** | Contadores por estado y recursos activos del historial de sesión |

## Patrones de diseño implementados

| Patrón | Ubicación | Problema que resuelve |
|--------|-----------|------------------------|
| **Custom Hook** | `useEmergenciaResumen.js`, `useIncidentesActivos.js`, `useWeather.js` | Lógica reutilizable (resumen, polling, clima) |
| **Provider (Context)** | `AlertContext.jsx` | Alertas globales sin prop drilling |
| **Compound Components** | `EmergenciaPanel.jsx` | Componer UI (`Header`, `Body`, `Footer`) de forma flexible |
| **Facade** | `emergenciaApi.js` | Un único punto de acceso HTTP al BFF |

## Requisitos

- **Node.js** 18+
- **Conexión a internet** (barra meteorológica vía Open-Meteo)
- **Backend en ejecución** (orden recomendado):
  1. Eureka (8761)
  2. Microservicios: incidentes, recursos, zonasriesgo
  3. BFF (8085)
  4. **API Gateway (8080)** — necesario para el panel de focos activos y el script `cargar-datos-prueba.ps1`

El API Gateway es **opcional** si solo pruebas la búsqueda manual por ID de incidente.

## Instalación

```bash
cd frontend/sre-ui
npm install --legacy-peer-deps
```

> `react-leaflet@4` declara peer dependency en React 18; el proyecto usa React 19 — requiere `--legacy-peer-deps`.

Copia el archivo de entorno:

```bash
cp .env.example .env
# Windows PowerShell:
copy .env.example .env
```

### Configuración `.env`

**Desarrollo local — resumen BFF (mínimo):**

```env
VITE_API_BASE_URL=
```

Con la URL vacía, las peticiones a `/bff/...` pasan por el **proxy de Vite** hacia `http://localhost:8085`.

**Con API Gateway** (focos activos + script de demo):

```env
VITE_API_BASE_URL=http://localhost:8080
# VITE_AUTH_TOKEN=Bearer TU_TOKEN_KEYCLOAK   # solo si el filtro JWT está activo
```

> El hook `useIncidentesActivos` consulta `http://localhost:8080/incidentes` directamente. Para ver focos en tiempo real, el gateway debe estar en ejecución.

Reinicia `npm run dev` después de modificar `.env`.

## Ejecución en desarrollo

```bash
npm run dev
```

Abre http://localhost:5173

### Probar el panel

**Modo mínimo (solo BFF):**

1. Crea datos de prueba según [README raíz](../../README.md) — *Paso 5*.
2. Ingresa `1` en **Buscar incidente por N°**.
3. Pulsa **Buscar**.

**Modo completo (mapa + focos):**

1. Levanta el API Gateway (`8080`).
2. Ejecuta `.\cargar-datos-prueba.ps1` desde la raíz del monorepo.
3. Abre el panel: verás focos activos, mapa con conos y podrás hacer clic en un foco para ver su detalle.

## Build y preview

```bash
npm run build
npm run preview
```

## Empaquetar como librería NPM

```bash
npm run build:lib
```

Exporta desde `src/index.js`: `EmergenciaPanel`, `useEmergenciaResumen`, `AlertProvider`, etc.

## Pruebas automatizadas

```bash
npm test
npm run test:coverage
```

| Archivo de test | Qué valida |
|-----------------|------------|
| `emergenciaApi.test.js` | Facade HTTP al BFF |
| `useEmergenciaResumen.test.js` | Custom Hook de resumen |
| `useIncidentesActivos.test.js` | Polling de focos activos |
| `useWeather.test.js` | Barra meteorológica (mock fetch) |

Reporte HTML: `coverage/index.html` tras `npm run test:coverage` (~**80%** líneas en hooks/services).

## Estructura del código

```text
src/
  components/
    Dashboard.jsx          # Pantalla principal
    FocosActivos.jsx       # Panel lateral de incidentes activos
    WeatherBar.jsx         # Barra meteorológica
    mapa/                  # MapaIncidentes, MarkerIncidente (Leaflet)
    emergencia/            # EmergenciaPanel, ResumenDetalle
  context/                 # AlertProvider
  hooks/                   # useEmergenciaResumen, useIncidentesActivos, useWeather
  services/                # emergenciaApi (Facade)
  styles/
```

## Integración en otro proyecto React

```bash
npm install file:../sre-ui
```

```jsx
import { AlertProvider, EmergenciaPanel, useEmergenciaResumen } from '@valledelsol/sre-ui';
```

## Solución de problemas

| Síntoma | Solución |
|---------|----------|
| `npm install` falla (ERESOLVE) | Usar `npm install --legacy-peer-deps` |
| Error 500 al consultar resumen | Verificar Eureka, microservicios y BFF `UP`; crear incidente de prueba |
| Error de red / CORS en `/bff` | `VITE_API_BASE_URL` vacío y proxy a 8085; reiniciar `npm run dev` |
| Focos activos “Backend sin conexión” | Levantar API Gateway en `:8080` |
| Sin zona ni recursos | Normal si no cargaste datos; ver README raíz paso 5 |
| Mapa sin conos de propagación | Requiere internet (Open-Meteo) y al menos un incidente `EN_PROGRESO` en el historial o focos |
