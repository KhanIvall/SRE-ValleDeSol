# Eureka Server

Registro de servicios (service discovery) para el monorepo SRE. Los microservicios, BFF y gateway se registran aquí para resolverse por nombre (`INCIDENTES`, `RECURSOS`, `BFF`, etc.).

| Atributo | Valor |
|----------|-------|
| **Puerto** | `8761` |
| **Dashboard** | http://localhost:8761 |
| **Nombre** | `eurekaServer` |

## Rol en el sistema

Eureka es el **primer componente** que debe arrancar en desarrollo local. Sin él, los microservicios y el BFF no pueden descubrirse entre sí.

## Requisitos

- JDK 17+, Maven 3.9+

## Instalación

```bash
mvn -pl infraestructuredomain/eurekaServer compile
```

## Ejecución

Desde la **raíz del monorepo**:

```bash
mvn -pl infraestructuredomain/eurekaServer spring-boot:run
```

El proceso queda activo en la terminal (no vuelve al prompt). Detener con `Ctrl+C`.

## Verificación

Abre http://localhost:8761 y revisa **Instances currently registered with Eureka** conforme levantes cada servicio.

| Servicio | Cuándo aparece |
|----------|----------------|
| `INCIDENTES` | Tras `mvn -pl businessdomain/incidentes spring-boot:run` |
| `RECURSOS` | Tras recursos |
| `ZONASRIESGO` | Tras zonasriesgo |
| `BFF` | Tras BFF (puerto 8085) |
| `APIGATEWAY` | Tras gateway (opcional, puerto 8080) |

Los microservicios de negocio muestran **puerto dinámico** en la columna Port del dashboard.

## Comportamiento esperado

### Solo Eureka levantado

En el dashboard es normal ver:

- **No instances available** — aún no hay clientes registrados.
- Aviso **EMERGENCY! EUREKA MAY BE INCORRECTLY...** — autopreservación cuando no llegan heartbeats. En desarrollo local con un único nodo **no indica fallo** del servidor.

### Stack completo (demo EP2)

Tras levantar incidentes, recursos, zonasriesgo y BFF, las cuatro aplicaciones deben figurar como `UP`.

## Configuración

`src/main/resources/application.properties`:

- `server.port=8761`
- `eureka.client.register-with-eureka=false` — el servidor no se registra a sí mismo como cliente

## Pruebas automatizadas

```bash
mvn -pl infraestructuredomain/eurekaServer test
```

## Siguiente paso

Continúa con el [README raíz](../../README.md) — sección *Guía rápida: levantar el proyecto*.
