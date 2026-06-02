# API Gateway

Punto de entrada único (Spring Cloud Gateway). Enruta al BFF y microservicios registrados en Eureka. Filtro JWT opcional vía Keycloak adapter.

- **Puerto:** `8080`
- **Eureka:** `apigateway`

## Rutas configuradas

| Path | Servicio Eureka |
|------|-----------------|
| `/bff/**` | BFF |
| `/incidentes/**` | INCIDENTES |
| `/recursos/**` | RECURSOS |
| `/zonas-riesgo/**` | ZONASRIESGO |

Configuración: `src/main/resources/application.yml`

## Ejecución

1. Eureka activo.
2. Microservicios y BFF registrados en Eureka.

```bash
mvn -pl infraestructuredomain/apigateway spring-boot:run
```

## Autenticación

Si `AuthenticationFilterinig` está activo, incluir header:

```
Authorization: Bearer {token}
```

Obtener token desde el módulo `keycloakadapter` según su configuración.

## Pruebas

```bash
mvn -pl infraestructuredomain/apigateway test
```
