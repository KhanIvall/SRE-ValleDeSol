# Eureka Server

Registro de servicios (service discovery) para el monorepo SRE. Los microservicios, BFF y gateway se registran aquí para resolverse por nombre (`INCIDENTES`, `RECURSOS`, `BFF`, etc.).

- **Puerto:** `8761`
- **Dashboard:** http://localhost:8761
- **Nombre en Eureka:** `eurekaServer`

## Ejecución

Debe arrancar **antes** que el resto de servicios backend.

```bash
mvn -pl infraestructuredomain/eurekaServer spring-boot:run
```

El proceso queda activo en la terminal (no vuelve al prompt). Detener con `Ctrl+C`.

## Comportamiento esperado

### Solo Eureka levantado

En el dashboard es normal ver:

- **No instances available** — aún no hay microservicios registrados.
- Aviso **EMERGENCY! EUREKA MAY BE INCORRECTLY...** — modo de autopreservación cuando no llegan renovaciones (heartbeats) de clientes. En desarrollo local, con un único nodo, **no indica fallo** del servidor.

Logs periódicos (`DiscoveryClient`, `response status is 200`, `evict task`) confirman que el servidor está en ejecución.

### Con el stack completo

Tras levantar incidentes, recursos, zonasriesgo y BFF, en **Instances currently registered with Eureka** deberían aparecer esas aplicaciones (puertos dinámicos en los microservicios).

## Configuración

`src/main/resources/application.properties`:

- `server.port=8761`
- `eureka.client.register-with-eureka=false` — el servidor no se registra a sí mismo como cliente

## Pruebas

```bash
mvn -pl infraestructuredomain/eurekaServer test
```
