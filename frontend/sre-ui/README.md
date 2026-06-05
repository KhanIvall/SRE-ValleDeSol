# @valledelsol/sre-ui

Componentes frontend **NPM** (React + Vite) para el **Sistema de Respuesta de Emergencias (SRE)** — Municipalidad Valle del Sol.

Consume el agregado del **BFF**: `GET /bff/emergencias/{id}/resumen`.

## Patrones de diseño implementados

| Patrón | Ubicación | Problema que resuelve |
|--------|-----------|------------------------|
| **Custom Hook** | `src/hooks/useEmergenciaResumen.js` | Reutilizar lógica de carga/errores sin duplicar en cada pantalla |
| **Provider (Context)** | `src/context/AlertContext.jsx` | Alertas globales sin prop drilling |
| **Compound Components** | `src/components/emergencia/EmergenciaPanel.jsx` | Componer UI del panel (`Header`, `Body`, `Footer`) de forma flexible |
| **Facade** | `src/services/emergenciaApi.js` | Un único punto de acceso HTTP al BFF |

## Requisitos

- **Node.js** 18+
- **Backend en ejecución** (orden recomendado):
  1. Eureka (8761)
  2. Microservicios: incidentes, recursos, zonasriesgo
  3. BFF (8085)

El API Gateway (8080) es **opcional** en desarrollo local.

## Instalación

```bash
cd frontend/sre-ui
npm install
```

Copia el archivo de entorno:

```bash
cp .env.example .env
# Windows PowerShell:
copy .env.example .env
```

### Configuración `.env`

**Desarrollo local (recomendado):**

```env
VITE_API_BASE_URL=
```

Con la URL vacía, las peticiones van a `/bff/...` y el **proxy de Vite** las reenvía a `http://localhost:8085`.

**Con API Gateway y JWT:**

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_AUTH_TOKEN=Bearer TU_TOKEN_KEYCLOAK
```

Reinicia `npm run dev` después de modificar `.env`.

## Ejecución en desarrollo

```bash
npm run dev
```

Abre http://localhost:5173

### Probar el panel

1. Asegúrate de tener datos de prueba en el backend (incidente id=1, zona y recurso opcionales). Ver [README raíz](../../README.md) — *Paso 5 — Datos de prueba*.
2. Ingresa `1` en **ID de incidente**.
3. Pulsa **Consultar resumen**.

Deberías ver el incidente, zona de riesgo (si existe para esas coordenadas) y recursos asignados.

## Build y preview

```bash
npm run build
npm run preview
```

## Empaquetar como librería NPM

```bash
npm run build -- --mode lib
```

Exporta desde `src/index.js`: `EmergenciaPanel`, `useEmergenciaResumen`, `AlertProvider`, etc.

## Pruebas automatizadas

```bash
npm test
```

Ejecuta tests con Vitest (hook y servicio API).

## Estructura del código

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

## Solución de problemas

| Síntoma | Solución |
|---------|----------|
| Error 500 al consultar | Verificar que Eureka, microservicios y BFF estén `UP`; crear incidente de prueba |
| Error de red / CORS | Usar `VITE_API_BASE_URL` vacío y proxy a 8085; reiniciar `npm run dev` |
| Sin zona ni recursos | Normal si no cargaste datos; ver README raíz paso 5 |
