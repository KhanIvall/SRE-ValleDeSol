# SRE Valle del Sol

Sistema de Respuesta de Emergencias — Municipalidad Valle del Sol.  
Monorepo fullstack: microservicios Java/Spring Boot, BFF, API Gateway y frontend React (NPM).

**Versión:** 1.0-SNAPSHOT (Maven) / 1.0.0 (frontend NPM)  
**Repositorio:** https://github.com/KhanIvall/SRE-ValleDeSol

## Estructura del proyecto

| Carpeta | Contenido |
|---------|-----------|
| `businessdomain/` | Microservicios: **incidentes**, **recursos**, **zonasriesgo** |
| `infraestructuredomain/` | Eureka, API Gateway, **BFF**, Keycloak adapter, Spring Boot Admin |
| `frontend/sre-ui/` | Panel React NPM (`@valledelsol/sre-ui`) |

## Requisitos previos

| Herramienta | Versión mínima | Uso |
|-------------|----------------|-----|
| **JDK** | 17+ | Backend Spring Boot |
| **Maven** | 3.9+ | Compilar y ejecutar módulos Java |
| **Node.js** | 18+ | Frontend (`npm install`, `npm run dev`) |

Todos los comandos Maven de este documento se ejecutan **desde la raíz del monorepo**.

## Guía rápida: levantar el proyecto (desarrollo local)

Modo recomendado para el panel EP2: **sin API Gateway ni JWT**. El frontend habla con el BFF vía proxy de Vite.

### Paso 1 — Eureka (obligatorio, primero)

Terminal 1:

```bash
mvn -pl infraestructuredomain/eurekaServer spring-boot:run
```

- Dashboard: http://localhost:8761  
- Debe quedar en ejecución antes de los demás servicios.

### Paso 2 — Microservicios (3 terminales)

Terminal 2:

```bash
mvn -pl businessdomain/incidentes spring-boot:run
```

Terminal 3:

```bash
mvn -pl businessdomain/recursos spring-boot:run
```

Terminal 4:

```bash
mvn -pl businessdomain/zonasriesgo spring-boot:run
```

Los tres usan **puerto dinámico** (`server.port=0`). El puerto asignado aparece en los logs (`Tomcat started on port XXXXX`) o en el dashboard de Eureka.

### Paso 3 — BFF

Terminal 5:

```bash
mvn -pl infraestructuredomain/bff spring-boot:run
```

- Puerto fijo: **8085**  
- Endpoint principal: `GET http://localhost:8085/bff/emergencias/{id}/resumen`

### Paso 4 — Frontend

Terminal 6:

```bash
cd frontend/sre-ui
npm install
cp .env.example .env    # Windows: copy .env.example .env
npm run dev
```

- Panel: http://localhost:5173  
- Dejar `VITE_API_BASE_URL` **vacío** en `.env` para que Vite envíe `/bff` al puerto 8085.

### Paso 5 — Datos de prueba (demo del panel)

Los microservicios usan **H2 en memoria**: al reiniciar un servicio se pierden los datos. Crea el escenario de demo con los puertos que muestre Eureka.

**PowerShell** (reemplaza `PUERTO_INCIDENTES`, `PUERTO_RECURSOS`, `PUERTO_ZONAS`):

```powershell
# 1. Incidente
$inc = @{ tipo = "INCENDIO"; descripcion = "Prueba panel SRE"; latitud = -33.45; longitud = -70.66 } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:PUERTO_INCIDENTES/incidentes" -Method Post -Body $inc -ContentType "application/json"

# 2. Zona de riesgo (mismas coordenadas)
$zona = @{ nombre = "Cerro Alto"; latitud = -33.45; longitud = -70.66 } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:PUERTO_ZONAS/zonas-riesgo" -Method Post -Body $zona -ContentType "application/json"

# 3. Recurso y asignación al incidente 1
$rec = @{ nombre = "Ambulancia 01"; tipo = "VEHICULO"; identificador = "AMB-01" } | ConvertTo-Json
$r = Invoke-RestMethod -Uri "http://localhost:PUERTO_RECURSOS/recursos" -Method Post -Body $rec -ContentType "application/json"
$asig = @{ incidenteId = 1 } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:PUERTO_RECURSOS/recursos/$($r.id)/asignar" -Method Put -Body $asig -ContentType "application/json"
```

**Verificación:**

```bash
curl http://localhost:8085/bff/emergencias/1/resumen
```

En el panel, ingresa ID `1` y pulsa **Consultar resumen**.

## Modo alternativo: API Gateway + JWT (opcional)

Para probar el flujo con autenticación:

1. Eureka + microservicios + BFF (pasos 1–3).
2. Keycloak en `8091` (externo al repo).
3. `mvn -pl infraestructuredomain/keycloakadapter spring-boot:run` (puerto 8088).
4. `mvn -pl infraestructuredomain/apigateway spring-boot:run` (puerto 8080).
5. En `frontend/sre-ui/.env`: `VITE_API_BASE_URL=http://localhost:8080` y `VITE_AUTH_TOKEN=Bearer <token>`.

Ver [apigateway](infraestructuredomain/apigateway/README.md) para rutas y JWT.

## Patrones de diseño (EP2)

| Componente | Patrones |
|------------|----------|
| **incidentes** | Factory Method, Repository, capas Controller → Service → Repository |
| **recursos** | Repository, reglas de negocio en Service |
| **zonasriesgo** | Adapter (`FakeWeatherAdapter`), Repository |
| **bff** | Facade, Circuit Breaker (Resilience4j) |
| **frontend** | Custom Hook, Context Provider, Compound Components, Facade HTTP |

La justificación detallada va en el PDF de análisis de patrones (entregable EP2).

## Pruebas automatizadas

```bash
# Todo el backend
mvn test

# Por módulo
mvn -pl businessdomain/incidentes test
mvn -pl infraestructuredomain/bff test

# Frontend
cd frontend/sre-ui && npm test
```

## Documentación de Arquitectura SRE

Documentación formal del encargo EV2 (Evaluación Parcial 2 — DSY1106):

| Documento | Descripción |
|-----------|-------------|
| [Análisis de Patrones y Arquetipos](Analisis-Patrones-Arquetipos-SRE-EP2.md) | Patrones de diseño, arquetipos Maven, arquitectura BFF/microservicios y diagramas Mermaid |
| [Plan de Branching](Plan-Branching-SRE-EP2.md) | Estrategia de ramas, convención de commits y políticas de merge (Pull Requests) |

> Para la entrega en Blackboard, exportar cada documento a PDF según las instrucciones del encargo.

## Documentación por componente

| Componente | README |
|------------|--------|
| Eureka | [eurekaServer/README.md](infraestructuredomain/eurekaServer/README.md) |
| Incidentes | [incidentes/README.md](businessdomain/incidentes/README.md) |
| Recursos | [recursos/README.md](businessdomain/recursos/README.md) |
| Zonas de riesgo | [zonasriesgo/README.md](businessdomain/zonasriesgo/README.md) |
| BFF | [bff/README.md](infraestructuredomain/bff/README.md) |
| API Gateway | [apigateway/README.md](infraestructuredomain/apigateway/README.md) |
| Frontend NPM | [frontend/sre-ui/README.md](frontend/sre-ui/README.md) |

## Solución de problemas frecuentes

| Síntoma | Causa probable | Qué hacer |
|---------|----------------|-----------|
| `mvn` no reconocido | Maven no está en el PATH | Instalar Maven 3.9+ y abrir terminal nueva |
| Panel con error 500 | Microservicio caído o sin datos | Revisar Eureka; crear incidente de prueba |
| BFF 404 en incidente | No existe el ID en H2 | Ejecutar POST de incidente (paso 5) |
| `localhost:8085` muestra 404 | No hay ruta en la raíz del BFF | Usar `/bff/emergencias/1/resumen` |
| Sin zona/recursos en panel | No hay datos en esos MS | Ejecutar scripts del paso 5 |
