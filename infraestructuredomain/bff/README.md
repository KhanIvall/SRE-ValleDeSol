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

4. Vía API Gateway:

```bash
curl http://localhost:8080/bff/emergencias/1/resumen
```

## Pruebas

```bash
mvn -pl infraestructuredomain/bff test
```

Incluye `EmergenciaFacadeServiceTest` (fallback de contingencia).
