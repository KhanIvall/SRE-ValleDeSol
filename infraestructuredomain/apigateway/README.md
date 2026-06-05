# API Gateway

Punto de entrada único (Spring Cloud Gateway). Enruta al BFF y microservicios registrados en Eureka. Filtro JWT opcional vía Keycloak adapter.

| Atributo | Valor |
|----------|-------|
| **Puerto** | `8080` |
| **Eureka** | `APIGATEWAY` |

> **Desarrollo local del panel EP2:** el frontend puede conectarse **directamente al BFF (8085)** sin levantar el gateway. Este módulo es necesario para el flujo con autenticación JWT.

## Rutas configuradas

| Path | Servicio Eureka |
|------|-----------------|
| `/bff/**` | BFF |
| `/incidentes/**` | INCIDENTES |
| `/recursos/**` | RECURSOS |
| `/zonas-riesgo/**` | ZONASRIESGO |

Configuración: `src/main/resources/application.yml`

## Requisitos previos

1. **Eureka** (8761)
2. Microservicios y **BFF** registrados en Eureka
3. Para JWT: **Keycloak** (8091) y **keycloakadapter** (8088) en ejecución

## Instalación

```bash
mvn -pl infraestructuredomain/apigateway compile
```

## Ejecución

```bash
mvn -pl infraestructuredomain/apigateway spring-boot:run
```

## Autenticación

Si el filtro `AuthenticationFilterinig` está activo en las rutas, todas las peticiones requieren:

```
Authorization: Bearer {token}
```

Obtener token desde el módulo `keycloakadapter` (puerto 8088) según la configuración de Keycloak en `application.properties`.

## Verificación

```bash
# Sin token — puede responder 401 si el filtro está habilitado
curl http://localhost:8080/bff/emergencias/1/resumen

# Con token
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/bff/emergencias/1/resumen
```

## Integración con el frontend

En `frontend/sre-ui/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_AUTH_TOKEN=Bearer <tu_token>
```

Reinicia `npm run dev` tras cambiar `.env`.

## Pruebas automatizadas

```bash
mvn -pl infraestructuredomain/apigateway test
```
