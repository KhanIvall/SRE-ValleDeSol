# @valledelsol/sre-ui

Componentes frontend **NPM** (React + Vite) para el **Sistema de Respuesta de Emergencias (SRE)** — Municipalidad Valle del Sol.

Consume el agregado del **BFF**: `GET /bff/emergencias/{id}/resumen` (vía API Gateway).

## Patrones de diseño implementados

| Patrón | Ubicación | Problema que resuelve |
|--------|-----------|------------------------|
| **Custom Hook** | `src/hooks/useEmergenciaResumen.js` | Reutilizar lógica de carga/errores sin duplicar en cada pantalla |
| **Provider (Context)** | `src/context/AlertContext.jsx` | Alertas globales sin prop drilling |
| **Compound Components** | `src/components/emergencia/EmergenciaPanel.jsx` | Componer UI del panel (`Header`, `Body`, `Footer`) de forma flexible |
| **Facade** | `src/services/emergenciaApi.js` | Un único punto de acceso HTTP al BFF |

## Requisitos

- Node.js 18+
- Backend en ejecución: Eureka, microservicios, **BFF** (8085), **API Gateway** (8080)

## Instalación

```bash
cd frontend/sre-ui
npm install
cp .env.example .env
```

Edita `.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_AUTH_TOKEN=   # JWT si el gateway exige Authorization
```

## Ejecutar en desarrollo

```bash
npm run dev
```

Abre http://localhost:5173 — el proxy de Vite reenvía `/bff` al gateway.

## Build de la app

```bash
npm run build
npm run preview
```

## Empaquetar como librería NPM

```bash
npm run build -- --mode lib
```

Exporta desde `src/index.js`: `EmergenciaPanel`, `useEmergenciaResumen`, `AlertProvider`, etc.

## Pruebas

```bash
npm test
```

## Estructura

```text
src/
  components/     # UI (Dashboard, EmergenciaPanel, …)
  context/        # AlertProvider
  hooks/          # useEmergenciaResumen
  services/       # emergenciaApi (Facade)
  styles/
```

## Integración en otro proyecto React

```bash
npm install file:../sre-ui
```

```jsx
import { AlertProvider, EmergenciaPanel, useEmergenciaResumen } from '@valledelsol/sre-ui';
```
