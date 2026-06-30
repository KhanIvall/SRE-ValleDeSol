# BFF — Backend For Frontend

Agregador para el panel React. Patrón **Facade**: consulta en paralelo incidentes, recursos y zonas de riesgo y devuelve un DTO único al frontend.

| Atributo | Valor |
|----------|-------|
| **Eureka** | `BFF` |
| **Puerto** | `8085` (fijo) |
| **Base path** | `/bff` |
| **Resiliencia** | Resilience4j Circuit Breaker con fallback de contingencia |
| **Stack** | Spring Boot 4.0.3 (WebFlux + Eureka Client) |

## Patrones de diseño

| Patrón | Ubicación | Problema que resuelve |
|--------|-----------|------------------------|
| **Facade** | `facade/EmergenciaFacadeService` | Un único punto de agregación para el cliente |
| **Circuit Breaker** | Resilience4j en `obtenerResumen` | Degradación controlada si un microservicio falla |

## Requisitos previos

Antes de iniciar el BFF deben estar en ejecución:

1. **Eureka** (8761)
2. Microservicios registrados: `INCIDENTES`, `RECURSOS`, `ZONASRIESGO`

## Instalación

Desde la raíz del monorepo:

```bash
mvn -pl infraestructuredomain/bff compile
```

## Ejecución

```bash
mvn -pl infraestructuredomain/bff spring-boot:run
```

El proceso escucha en **http://localhost:8085**. Detener con `Ctrl+C`.

> La raíz `http://localhost:8085/` no expone una página; es normal ver un error 404 ahí. Usa el endpoint documentado abajo.

## Endpoint principal

```
GET /bff/emergencias/{incidenteId}/resumen
```

**Respuesta** (`EmergenciaResumenDto`):

- `incidente` — datos del microservicio incidentes
- `recursosAsignados` — lista de recursos del incidente
- `zonaRiesgo` — zona que coincide con coordenadas del incidente (si existe)
- `datosContingencia` — `true` si se activó el fallback del Circuit Breaker

## Verificación

```bash
# Requiere incidente con id=1 (ver README raíz o incidentes)
curl http://localhost:8085/bff/emergencias/1/resumen
```

Respuesta esperada: JSON con `incidente`, `recursosAsignados` y opcionalmente `zonaRiesgo`.

## Integración con el frontend

| Modo | Configuración en `frontend/sre-ui/.env` |
|------|----------------------------------------|
| **Desarrollo (recomendado)** | `VITE_API_BASE_URL=` vacío; proxy Vite → `http://localhost:8085` |
| **Con API Gateway** | `VITE_API_BASE_URL=http://localhost:8080` + `VITE_AUTH_TOKEN=Bearer <token>` |

El BFF resuelve microservicios por nombre en Eureka (`http://INCIDENTES/...`, `http://RECURSOS/...`, `http://ZONASRIESGO/...`).

## Vía API Gateway (opcional)

```bash
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/bff/emergencias/1/resumen
```

Requiere gateway (8080) y token JWT válido.

## Pruebas automatizadas

```bash
mvn -pl infraestructuredomain/bff test
```

Incluye:

| Test | Qué valida |
|------|------------|
| `EmergenciaFacadeServiceTest` | Fallback de contingencia del Circuit Breaker |
| `EmergenciaFacadeIntegrationTest` | Integración del facade con clientes simulados |
| `BffEmergenciaE2ETest` | Endpoint REST `GET /bff/emergencias/{id}/resumen` con `WebTestClient` |
| `BffApplicationTests` | Contexto Spring carga correctamente |

### Spring Boot 4 — dependencia de test

En Spring Boot 4, `@AutoConfigureWebTestClient` vive en el módulo `spring-boot-webtestclient` (paquete `org.springframework.boot.webtestclient.autoconfigure`). El `pom.xml` del BFF ya incluye:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-webtestclient</artifactId>
    <scope>test</scope>
</dependency>
```

Sin esta dependencia, `mvn spring-boot:run` falla al compilar tests con error *package org.springframework.boot.test.autoconfigure.web.reactive does not exist*.

### Cobertura JaCoCo

Tras `mvn test`, revisa `target/site/jacoco/index.html`. Cobertura de referencia (EP3): **~97% líneas**.
