# BFF — Backend For Frontend

Agregador para el panel React. Patrón **Facade**: consulta en paralelo incidentes, recursos y zonas de riesgo y devuelve un DTO único.

- **Eureka:** `bff`
- **Puerto:** `8085`
- **Resiliencia:** Resilience4j Circuit Breaker con fallback de contingencia

## Endpoint principal

```
GET /bff/emergencias/{incidenteId}/resumen
```

Respuesta: incidente + recursos asignados + zona de riesgo (si hay coordenadas) + flag `datosContingencia`.

## Ejecución

1. Eureka + microservicios `incidentes`, `recursos`, `zonasriesgo` en ejecución.
2. Desde la raíz:

```bash
mvn -pl infraestructuredomain/bff spring-boot:run
```

3. Prueba directa (sin gateway):

```bash
curl http://localhost:8085/bff/emergencias/1/resumen
```

4. Vía API Gateway (requiere JWT):

```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/bff/emergencias/1/resumen
```

## Integración con el frontend

| Modo | Configuración |
|------|----------------|
| **Desarrollo (recomendado)** | `VITE_API_BASE_URL` vacío en `.env`; proxy Vite → `http://localhost:8085` |
| **Con gateway** | `VITE_API_BASE_URL=http://localhost:8080` + `VITE_AUTH_TOKEN` |

El BFF llama por Eureka a `INCIDENTES`, `RECURSOS` y `ZONASRIESGO`. Debe existir el incidente (crear con `POST /incidentes`).

## Pruebas

```bash
mvn -pl infraestructuredomain/bff test
```

Incluye `EmergenciaFacadeServiceTest` (fallback de contingencia).
