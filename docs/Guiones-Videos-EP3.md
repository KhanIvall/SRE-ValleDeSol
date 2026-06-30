# Guiones de videos — EP3 (DSY1106)

**Proyecto:** SRE Valle del Sol — Sistema de Respuesta de Emergencias  
**Repositorio:** https://github.com/KhanIvall/SRE-ValleDeSol  
**Rama de entrega:** `feature/ep3-entrega`  
**Integrantes:** Ari Araya, Skarlett Tropan  

**Duración sugerida:** Video de Arquitectura ~25–35 min · Video de Uso ~12–18 min.

> Guiones alineados con el checklist EP3 (`CHECKLIST.xlsx`). Grabar con pantalla compartida (IDE + terminal + navegador).

---

## Checklist rápido antes de grabar

### Video de Arquitectura (52 ítems)

Preparar pantallas de: diagrama Mermaid (`Analisis-Patrones-Arquetipos-SRE-EP2.md`), dashboard Eureka, código Factory/Adapter/Circuit Breaker, `application.yml`, filtro JWT, Actuator, reportes JaCoCo/Vitest, estructura de carpetas de los 3 microservicios.

### Video de Uso (9 ítems)

Tener todo el stack levantado antes de grabar, ejecutar `cargar-datos-prueba.ps1`, y practicar el flujo: consulta → focos → mapa → contingencia (opcional).

### Tips de grabación

1. Graba el **Video de Uso** con el stack completo ya corriendo (evita esperas de Maven en cámara).
2. En **Arquitectura**, alterna IDE + navegador + terminal según el ítem.
3. Si preguntan por **TypeScript**: el frontend usa **JavaScript con ES modules y tests Vitest**; es la buena práctica aplicada en este proyecto.
4. Menciona la rama `feature/ep3-entrega` y el enlace GitHub al inicio de ambos videos.

---

# VIDEO 1 — Arquitectura de Componentes y Diseño Global

## Apertura (ítems 1–4)

**[PANTALLA: portada / logo Municipalidad Valle del Sol]**

> Hola, somos [nombres]. En este video presentamos la **arquitectura técnica** del **Sistema de Respuesta de Emergencias (SRE)** de la Municipalidad Valle del Sol, desarrollado en el monorepo [SRE-ValleDeSol](https://github.com/KhanIvall/SRE-ValleDeSol), rama `feature/ep3-entrega`.

**[PANTALLA: diagrama del informe `Analisis-Patrones-Arquetipos-SRE-EP2.md`, sección 2.2]**

> **Problema (ítem 1):** La municipalidad necesita centralizar emergencias: registrar incidentes con ciclo de vida, asignar recursos logísticos, evaluar zonas de riesgo según ubicación y clima, y mostrar todo en un panel único para operadores. Un sistema monolítico generaría acoplamiento y dificultaría escalar dominios independientes.

> **Descomposición DDD / microservicios (ítem 2):** Descompusimos por **subdominios de negocio**:
>
> - `incidentes` — gestión operativa de emergencias
> - `recursos` — brigadas, vehículos y equipos
> - `zonasriesgo` — análisis territorial y clima
>
> Cada uno tiene su propia base H2, se despliega de forma independiente y escala por dominio. El **BFF** agrega datos para el frontend y el **API Gateway** unifica el acceso externo. Esto reduce acoplamiento y permite escalar solo el servicio que lo necesite.

**[PANTALLA: estructura de carpetas del monorepo]**

> **Componentes y justificación (ítems 3–4):**

| Componente | Carpeta | Justificación |
|------------|---------|---------------|
| Eureka | `infraestructuredomain/eurekaServer` | Service Discovery |
| Microservicios | `businessdomain/` | Dominios autónomos |
| BFF | `infraestructuredomain/bff` | Agregación orientada al panel |
| API Gateway | `infraestructuredomain/apigateway` | Punto de entrada único |
| Keycloak Adapter | `infraestructuredomain/keycloakadapter` | JWT opcional |
| Spring Boot Admin | `infraestructuredomain/springBootAdmin` | Monitoreo centralizado |
| Frontend NPM | `frontend/sre-ui` | Panel centro de operaciones |

---

## Service Discovery — Eureka (ítems 5–11)

**[PANTALLA: `eurekaServer/README.md` + dashboard http://localhost:8761]**

> **Función (ítem 5):** Eureka actúa como **base de datos dinámica de ubicaciones de red**. Los servicios se auto-registran y el resto los resuelve por nombre (`INCIDENTES`, `RECURSOS`, `BFF`, etc.) sin conocer puertos fijos.

> **Justificación (ítem 6):** Nuestros microservicios usan **puerto dinámico** (`server.port=0`). Sin Eureka, el BFF no sabría dónde están. Eureka permite balanceo `lb://` desde el Gateway.

> **Cómo funciona — lo bueno y lo malo (ítem 7):**
>
> - **Bueno:** descubrimiento automático, health checks, integración nativa con Spring Cloud.
> - **Malo:** en desarrollo local con un solo nodo puede mostrar avisos de autopreservación; agrega complejidad operativa (Eureka debe levantarse primero).

> **Configuración (ítem 8):** En `application.properties` de cada cliente: `eureka.client.serviceUrl.defaultZone`, `register-with-eureka=true`, `fetch-registry=true`. Para cambiar zona: editar `defaultZone`.

> **Self-Registration (ítem 9):** Sí. Cada microservicio y el BFF incluyen `spring-cloud-starter-netflix-eureka-client` y se registran automáticamente al arrancar.

**[PANTALLA: terminal]**

> **Ubicación y descarga (ítem 10):** El código está en `infraestructuredomain/eurekaServer` del monorepo GitHub. No requiere descarga aparte.

> **Cómo levantarlo (ítem 11):**

```bash
# Desde la raíz del monorepo
mvn -pl infraestructuredomain/eurekaServer spring-boot:run
```

**[PANTALLA: Eureka dashboard con instancias UP]**

> Verificamos en http://localhost:8761 que aparezcan `INCIDENTES`, `RECURSOS`, `ZONASRIESGO` y `BFF` conforme los levantamos.

---

## Microservicios (ítems 12–30)

### Visión general de los 3 MS

**[PANTALLA: README de incidentes, recursos, zonasriesgo]**

> **Dominio de cada microservicio (ítem 12):**
>
> - **incidentes:** ciclo de vida `REPORTADO → EN_PROGRESO → CONTROLADO → CERRADO`
> - **recursos:** logística; asignación por `incidenteAsignadoId` sin FK entre BD
> - **zonasriesgo:** nivel de riesgo territorial con datos climáticos simulados

> **Reglas de negocio (ítem 13):**
>
> - Incidentes: coordenadas obligatorias para pasar a `EN_PROGRESO` (`EstadoIncidenteFactory`)
> - Recursos: solo `DISPONIBLE` puede asignarse (`RecursoService`)
> - Zonas: nombre y coordenadas obligatorias; nivel calculado por clima (`ZonaRiesgoService`)

> **Procesamiento de datos (ítem 14):** JPA/Hibernate con H2 en memoria. Cada MS persiste en su propia BD. El BFF consulta vía REST y agrega en `EmergenciaResumenDto`.

> **Validaciones (ítem 15):** `BusinessRulesException` con códigos (`INC-404`, `ZON-001`, etc.) y `ApiExceptionHandler` global que devuelve HTTP semántico.

> **Casos de uso (ítem 16):**
>
> - Crear/listar/actualizar incidentes y cambiar estado
> - Crear recursos, asignar/liberar
> - Crear zonas, buscar por coordenadas, recalibrar riesgo

### Estructura y código (ítems 17–25)

**[PANTALLA: árbol de carpetas `incidentes/src/main/java/...`]**

> **Estructura de carpetas (ítem 17):** Patrón homogéneo desde arquetipo Maven: `controller/`, `service/`, `repository/`, `entities/`, `common/`, `exception/`. En `zonasriesgo` hay además `adapter/`.

> **Dependencias (ítem 18):** En cada `pom.xml`: Spring Boot Web, Spring Data JPA, H2, Eureka Client, Actuator, JaCoCo para cobertura.

> **Controladores (ítem 19):** Por ejemplo `IncidenteRestController`: expone REST, delega al Service, retorna `ResponseEntity` con códigos HTTP correctos (`201 CREATED` en POST).

> **Seguridad en microservicios (ítem 20):** La seguridad perimetral está en el **API Gateway** (filtro JWT). Los microservicios confían en la red interna vía Eureka; no duplican autenticación.

> **Patrones en código (ítem 21):**
>
> - **Factory Method:** `incidentes/factory/EstadoIncidenteFactory`
> - **Repository:** los tres MS en `repository/`
> - **Adapter:** `zonasriesgo/adapter/FakeWeatherAdapter`

> **Archivos de configuración (ítem 22):** `application.properties`: nombre Eureka, puerto, JPA, Actuator expuesto.

**[PANTALLA: terminal — levantar un MS]**

> **Levantar y comprobar (ítem 23):**

```bash
mvn -pl businessdomain/incidentes spring-boot:run
curl http://localhost:PUERTO/incidentes
```

> **Manejo de excepciones (ítem 24):** `BusinessRulesException` + `@ControllerAdvice` (`ApiExceptionHandler`) mapea a JSON con código, mensaje y status HTTP.

> **Buenas prácticas (ítem 25):** Capas separadas, DTOs en BFF, sin FK entre microservicios, Swagger en incidentes, tests automatizados.

### Circuit Breaker, logs, métricas, pruebas (ítems 26–30)

**[PANTALLA: `EmergenciaFacadeService.java` + `application.properties` del BFF]**

> **Circuit Breaker (ítem 26):** En el BFF con Resilience4j. Anotación `@CircuitBreaker(name = "bffEmergencia", fallbackMethod = "resumenFallback")`. Config en `application.properties`: ventana deslizante 10, umbral 50%, estado abierto 5s.

> **HttpStatus (ítem 27):** `201` al crear recurso/zona, `404` incidente no encontrado, `412 PRECONDITION_FAILED` en transiciones inválidas, `401/400` en Gateway sin token.

> **Logs (ítem 28):** Gateway con `logging.level.org.springframework.cloud.gateway: DEBUG`. Filtro JWT registra autorización. Frontend: `AlertContext` para errores de negocio.

> **Métricas (ítem 29):** Actuator en BFF y microservicios (`management.endpoints.web.exposure.include=*`). Circuit Breaker expone health indicator. Spring Boot Admin en puerto `8099` centraliza monitoreo.

**[PANTALLA: reportes JaCoCo y Vitest]**

> **Pruebas (ítem 30):**
>
> - **Unitarias:** `EstadoIncidenteFactoryTest`, `RecursoServiceTest`, `FakeWeatherAdapterTest`
> - **Integración:** `IncidenteIntegrationTest`, `RecursoIntegrationTest`
> - **E2E:** `IncidenteE2ETest`, `BffEmergenciaE2ETest`
> - **Frontend:** `emergenciaApi.test.js`, `useEmergenciaResumen.test.js`
> - Cobertura: todos ≥ 60% (incidentes 74%, BFF 97%, frontend 80%)

```bash
mvn test -pl businessdomain/incidentes,businessdomain/recursos,businessdomain/zonasriesgo,infraestructuredomain/bff -am
cd frontend/sre-ui && npm run test:coverage
```

---

## Seguridad / JWT (ítems 31–35)

**[PANTALLA: `keycloakadapter` + `AuthenticationFilterinig.java`]**

> **Justificación (ítem 31):** El SRE maneja datos operativos sensibles. El Gateway centraliza autenticación sin modificar cada microservicio.

> **Generación JWT (ítem 32):** `KeycloakRestService.login()` envía credenciales al endpoint OpenID de Keycloak (`/protocol/openid-connect/token`) con `grant_type=password` y devuelve el access token.

> **Configuración JWT (ítem 33):** En `keycloakadapter/application.properties`: `keycloak.token-uri`, `client-id`, `client-secret`, `jwk-set-uri`.

> **Validación (ítem 34):** El filtro `AuthenticationFilterinig` en el Gateway verifica header `Authorization: Bearer`, consulta `http://KEYCLOAKADAPTER/roles` y valida rol `Partners`.

> **Enrutamiento seguro (ítem 35):** Rutas en `application.yml` usan `lb://BFF`, `lb://INCIDENTES`, etc. Con filtro activo, peticiones sin token reciben `401 UNAUTHORIZED`. El frontend envía token vía `VITE_AUTH_TOKEN` en `emergenciaApi.js`.

---

## API Gateway (ítems 36–38)

**[PANTALLA: `application.yml` + README apigateway]**

> **Funcionamiento (ítem 36):** Spring Cloud Gateway en puerto `8080`. Punto de entrada único: `/bff/**`, `/incidentes/**`, `/recursos/**`, `/zonas-riesgo/**`.

> **Componentes (ítem 37):** `application.yml` (rutas), `AuthenticationFilterinig` (filtro JWT), `GlobalPostFiltering` (post-procesamiento), registro Eureka como `APIGATEWAY`.

> **Filtrado (ítem 38):** Predicados `Path` enrutan al servicio Eureka correspondiente. Filtro global valida JWT antes de reenviar. Sin filtro activo (modo demo local), las rutas son abiertas.

```bash
mvn -pl infraestructuredomain/apigateway spring-boot:run
curl http://localhost:8080/incidentes
```

---

## Monitoreo (ítems 39–42)

**[PANTALLA: Eureka + Spring Boot Admin http://localhost:8099]**

> **Justificación (ítem 39):** En emergencias no podemos operar a ciegas. Actuator + Eureka health + Spring Boot Admin permiten ver estado de cada instancia.

> **Si un MS falla (ítem 40):** El BFF activa `resumenFallback` con `datosContingencia: true`. El panel muestra badge de contingencia. El operador sigue viendo datos parciales.

> **Cómo detectamos fallas (ítem 41):** Eureka marca instancia `DOWN`. Actuator `/actuator/health`. Frontend: `useIncidentesActivos` muestra "Backend sin conexión" si el polling falla.

> **Seguimiento para mejoras (ítem 42):** Logs del Gateway, health indicators de Circuit Breaker, dashboard Spring Boot Admin y reportes JaCoCo para priorizar deuda técnica.

---

## Frontend (ítems 43–52)

**[PANTALLA: panel http://localhost:5173 + código `frontend/sre-ui`]**

> **Tecnología (ítem 43):** React 19 + Vite 6 como paquete NPM `@valledelsol/sre-ui`. Elegido por ecosistema maduro, hot reload y publicación como librería reutilizable.

> **Reglas de negocio en UI (ítem 44):** Solo incidentes no `CERRADO` aparecen en focos activos. Prioridad visual: `EN_PROGRESO` primero. Historial limitado a 10 consultas.

> **Buenas prácticas (ítem 45):** Usamos **JavaScript ES modules** (no TypeScript en esta entrega). Patrones: Custom Hooks, Context Provider, Compound Components, Facade HTTP. Tests con Vitest.

> **Seguridad y cambios (ítem 46):** Token en `.env` (`VITE_AUTH_TOKEN`). Para cambiar funcionalidad: modificar hooks/services, actualizar `.env` y reiniciar `npm run dev`.

> **Sin respuesta del backend (ítem 47):** `useEmergenciaResumen` captura error y muestra mensaje. `useIncidentesActivos` usa `AbortSignal.timeout(5000)` y marca `online: false`. `AlertContext` publica alertas globales.

> **Tiempos de respuesta (ítem 48):** Polling de focos cada 30 s. Timeout de 5 s en fetch de incidentes. Indicador `loading` en consulta de resumen.

> **Estándares de rendimiento (ítem 49):** Panel debe responder consulta BFF en < 3 s en red local. Focos actualizados cada 30 s. Modo contingencia garantiza disponibilidad degradada pero operativa.

> **Logs frontend (ítem 50):** Errores visibles al operador vía `AlertContext` e `inline-error` en Dashboard; tests validan flujos de error.

> **Mensajes de negocio ante excepción (ítem 51):** Badge "Contingencia" cuando `datosContingencia: true`. Mensajes como "Ingrese un ID válido" o detalle HTTP del BFF.

> **Manejo de excepciones (ítem 52):** try/catch en hooks, propagación a UI, estados `error`/`loading`/`data` separados en `useEmergenciaResumen`.

### Cierre Video 1

> En resumen: arquitectura de microservicios con Eureka, BFF resiliente, Gateway como perímetro, persistencia JPA desacoplada y panel React orientado al operador municipal. Repositorio y documentación en el README del monorepo.

---

# VIDEO 2 — Video de Uso (demostración funcional)

## 1. Problemática y solución (ítem 1)

**[PANTALLA: mapa del panel con incidentes activos]**

> La Municipalidad Valle del Sol necesita coordinar emergencias en tiempo real: incendios, derrumbes, inundaciones. Antes, la información estaba dispersa. **SRE** centraliza incidentes, recursos y riesgo en un **panel único** con mapa, focos activos y resumen agregado del BFF.

---

## 2. Introducción (ítem 2)

> Presentamos el **Sistema de Respuesta de Emergencias (SRE)**, versión 1.0, desarrollado por [nombres] para la Municipalidad Valle del Sol. Es un monorepo con microservicios Java, BFF, API Gateway y frontend React.

**[PANTALLA: diagrama simplificado UI → Gateway → BFF → MS]**

---

## 3. Requisitos del sistema (ítem 3)

**[PANTALLA: tabla de requisitos]**

| Requisito | Detalle |
|-----------|---------|
| JDK | 17 o superior |
| Maven | 3.9+ |
| Node.js | 18+ |
| Navegador | Chrome/Edge/Firefox actual |
| Internet | Para barra meteorológica (Open-Meteo) |
| Opcional | Keycloak en 8091 (modo JWT) |

**Funcionales:**

- Registrar y gestionar incidentes con estados
- Asignar recursos logísticos
- Evaluar zonas de riesgo
- Consultar resumen agregado por ID
- Visualizar focos activos en mapa en tiempo real

---

## 4. Instalación y configuración (ítem 4)

**[PANTALLA: terminal — clonar y levantar]**

```bash
git clone https://github.com/KhanIvall/SRE-ValleDeSol.git
cd SRE-ValleDeSol
git checkout feature/ep3-entrega
```

Orden de arranque:

```bash
# 1. Eureka (obligatorio primero)
mvn -pl infraestructuredomain/eurekaServer spring-boot:run

# 2. Microservicios (3 terminales)
mvn -pl businessdomain/incidentes spring-boot:run
mvn -pl businessdomain/recursos spring-boot:run
mvn -pl businessdomain/zonasriesgo spring-boot:run

# 3. BFF
mvn -pl infraestructuredomain/bff spring-boot:run

# 4. API Gateway (demo completa)
mvn -pl infraestructuredomain/apigateway spring-boot:run

# 5. Frontend
cd frontend/sre-ui
npm install --legacy-peer-deps
copy .env.example .env
npm run dev
```

Para demo completa con 8 incidentes de prueba:

```powershell
.\cargar-datos-prueba.ps1
```

---

## 5. Cómo acceder (ítem 5)

**[PANTALLA: URLs en navegador]**

| Servicio | URL |
|----------|-----|
| Panel SRE | http://localhost:5173 |
| Eureka | http://localhost:8761 |
| BFF (directo) | http://localhost:8085/bff/emergencias/1/resumen |
| API Gateway | http://localhost:8080 |
| Spring Boot Admin | http://localhost:8099 |

---

## 6. Descripción de la interfaz (ítem 6)

**[PANTALLA: recorrido visual del panel]**

La interfaz tiene estas zonas:

1. **Barra meteorológica** (arriba): temperatura, viento, calidad del aire, reloj
2. **Focos activos** (lateral): incidentes no cerrados, actualización cada 30 s
3. **Mapa Leaflet** (centro): marcadores por estado, círculos de riesgo, conos de propagación según viento
4. **Búsqueda por ID**: campo + botón Consultar
5. **Detalle de emergencia**: incidente, zona, recursos asignados
6. **Estadísticas**: contadores por estado del historial de sesión

---

## 7. Funcionalidades principales (ítem 7)

- **Consultar resumen** de un incidente por ID (agrega incidente + recursos + zona)
- **Focos activos** con alerta sonora en nuevos `EN_PROGRESO`
- **Mapa interactivo** con clic en foco para ver detalle
- **Modo contingencia** si un microservicio falla (badge visible)
- **Historial** de hasta 10 consultas en la sesión

---

## 8. Demostración completa (ítem 8)

**[DEMO en vivo — ~5–7 min]**

### Escenario A — Consulta por ID

1. Abrir http://localhost:5173
2. Ingresar ID `1` → **Consultar**
3. Mostrar detalle: tipo INCENDIO, zona Cerro Alto, ambulancia asignada

### Escenario B — Focos y mapa

1. Ejecutar `cargar-datos-prueba.ps1`
2. Mostrar panel lateral con 8 incidentes
3. Clic en un foco `EN_PROGRESO` → detalle en mapa
4. Señalar conos de propagación según viento

### Escenario C — Resiliencia (opcional)

1. Detener microservicio `zonasriesgo` (Ctrl+C)
2. Consultar resumen → badge **Contingencia**
3. Relanzar servicio → consulta normal

### Escenario D — API directa (opcional)

```bash
curl http://localhost:8085/bff/emergencias/1/resumen
```

---

## 9. Conclusión y escalabilidad (ítem 9)

**[PANTALLA: diagrama de arquitectura]**

> **Conclusión:** SRE entrega una solución fullstack que resuelve la problemática municipal con microservicios desacoplados, panel operativo en tiempo real y resiliencia ante fallas parciales.

> **Escalabilidad:**
>
> - Agregar nuevos dominios con arquetipo `sre-microservice-archetype`
> - Escalar horizontalmente instancias en Eureka
> - Cambiar H2 por PostgreSQL en producción
> - Activar JWT con Keycloak en el Gateway
> - Publicar frontend como paquete NPM (`npm run build:lib`)
> - Integrar proveedor climático real cambiando el Adapter en zonasriesgo

> Repositorio: https://github.com/KhanIvall/SRE-ValleDeSol — rama `feature/ep3-entrega`. Gracias.

---

## Referencias del proyecto

| Documento | Ruta |
|-----------|------|
| Guía de ejecución | `README.md` |
| Análisis de patrones y arquitectura | `Analisis-Patrones-Arquetipos-SRE-EP2.md` |
| Persistencia EP3 | `docs/Persistencia-SRE-EP3.md` |
| Informe de pruebas | `docs/Informe-Pruebas-Unitarias-EP3.md` |
| Repositorios y ramas | `repositorios.txt` |
