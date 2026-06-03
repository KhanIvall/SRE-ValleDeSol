# Análisis de Patrones y Arquetipos

**Sistema de Respuesta de Emergencias (SRE) — Municipalidad Valle del Sol**

| Campo | Detalle |
|-------|---------|
| Asignatura | DSY1106 — Desarrollo Fullstack III |
| Evaluación | Parcial N°2 (EP2) |
| Versión | 1.0.0 |
| Integrantes | Ari Araya, Skarlett Tropan |
| Repositorio | https://github.com/KhanIvall/SRE-ValleDeSol |
| Fecha | _________________ |

---

## 1. Introducción

El **Sistema de Respuesta de Emergencias (SRE)** es una plataforma de misión crítica diseñada para optimizar la gestión de siniestros (incendios forestales y emergencias) en la Municipalidad Valle del Sol. Fue concebido en el **Informe Técnico de Diseño Arquitectónico (EP1)** y materializado en el **Encargo EP2** mediante una arquitectura de microservicios, un Backend For Frontend (BFF), API Gateway y frontend React empaquetado como NPM.

Este documento analiza los **patrones de diseño** y **arquetipos Maven** seleccionados, justificando su aplicación según los problemas que resuelven y su contribución a la mantenibilidad, escalabilidad y resiliencia del sistema.

---

## 2. Contexto y objetivos

### 2.1 Problema

Valle del Sol enfrenta vulnerabilidad ante incendios forestales con infraestructura digital fragmentada y flujos manuales, lo que genera retrasos en la respuesta operativa.

### 2.2 Objetivo técnico

Construir una solución desacoplada donde:

- Cada capacidad operativa (incidentes, recursos, riesgo territorial) evolucione de forma independiente.
- El frontend reciba datos agregados sin conocer la complejidad interna.
- El sistema tolere fallos parciales durante picos de demanda.

### 2.3 Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Backend | Java 17, Spring Boot 4.x, Spring Cloud |
| Persistencia (dev) | H2 + JPA |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| BFF | Spring WebFlux + WebClient |
| Resiliencia | Resilience4j (Circuit Breaker) |
| Frontend | React 19, Vite, NPM |
| Build | Maven (monorepo `sre-parent`) |

---

## 3. Arquitectura general

### 3.1 Diagrama de flujo

```
┌──────────────────┐
│  React (NPM)     │  frontend/sre-ui — puerto 5173
│  @valledelsol/   │
│  sre-ui          │
└────────┬─────────┘
         │ GET /bff/emergencias/{id}/resumen
         ▼
┌──────────────────┐
│  API Gateway     │  :8080 — rutas, JWT
└────────┬─────────┘
         ▼
┌──────────────────┐
│  BFF (Facade)    │  :8085 — agregación paralela
└────────┬─────────┘
         │ vía Eureka (WebClient)
    ┌────┼────────────┐
    ▼    ▼            ▼
 incidentes  recursos  zonasriesgo
    │    │            │
    └── H2 independiente por microservicio
         ▲
    Eureka Server :8761
```

### 3.2 Patrones arquitectónicos aplicados

| Patrón arquitectónico | Implementación | Justificación |
|----------------------|----------------|---------------|
| **Microservicios** | 3 servicios en `businessdomain/` | Single Responsibility; escalado independiente |
| **BFF (Backend for Frontend)** | `infraestructuredomain/bff` | Reduce llamadas HTTP del cliente móvil/web |
| **API Gateway** | `infraestructuredomain/apigateway` | Punto único de entrada, seguridad centralizada |
| **Service Discovery** | Eureka | Registro dinámico; sin IPs fijas |
| **Database per Service** | H2 por microservicio | Autonomía de persistencia |

---

## 4. Patrones de diseño — Backend

### 4.1 Factory Method (Creacional)

**Ubicación:** `businessdomain/incidentes/src/main/java/com/valledelsol/incidentes/factory/`

**Clases principales:**
- `EstadoIncidenteFactory`
- `EstadoIncidenteHandler` (interfaz)
- `ReportadoEstadoHandler`, `EnProgresoEstadoHandler`, `ControladoEstadoHandler`, `CerradoEstadoHandler`

**Problema que resuelve:**  
En emergencias, las reglas de negocio varían según la fase del siniestro. Dispersar validaciones en controllers o entidades genera código difícil de mantener y propenso a errores.

**Solución:**  
La factory centraliza la creación y validación de transiciones de estado. Cada handler implementa reglas específicas. Ejemplo crítico del informe técnico: al pasar a `EN_PROGRESO`, el handler exige **latitud y longitud** antes de permitir despacho de brigadas.

**Beneficios:**
- Extensibilidad: nuevos estados = nuevo handler sin modificar el núcleo.
- Coherencia: una sola vía para cambiar estado (`IncidenteService.cambiarEstado`).
- Testeable: `EstadoIncidenteFactoryTest` valida reglas aisladas.

**Prueba unitaria:** `EstadoIncidenteFactoryTest`, `IncidenteServiceTest`

---

### 4.2 Adapter (Estructural)

**Ubicación:** `businessdomain/zonasriesgo/src/main/java/com/valledelsol/zonasriesgo/adapter/`

**Clases principales:**
- `WeatherDataPort` (puerto / interfaz)
- `FakeWeatherAdapter` (adaptador de simulación)
- `WeatherSnapshot` (DTO de condiciones)

**Problema que resuelve:**  
El microservicio de zonas de riesgo necesita datos climáticos, pero en desarrollo no hay API meteorológica contratada. Acoplar el servicio a un proveedor concreto generaría deuda técnica al migrar a sensores IoT o APIs reales.

**Solución:**  
El servicio (`ZonaRiesgoService`) solo depende de `WeatherDataPort`. `FakeWeatherAdapter` simula temperatura, humedad y viento. En producción futura se implementaría `OpenWeatherAdapter` sin cambiar la lógica de negocio.

**Beneficios:**
- Principio de inversión de dependencias.
- Cambio de proveedor sin modificar `ZonaRiesgoService`.
- Alineado con el informe técnico (FakeWeatherAdapter en fase actual).

**Prueba unitaria:** `FakeWeatherAdapterTest`, `ZonaRiesgoServiceTest`, `ZonaRiesgoRecalibrarTest`

---

### 4.3 Facade (Estructural) — BFF

**Ubicación:** `infraestructuredomain/bff/src/main/java/com/valledelsol/bff/facade/EmergenciaFacadeService.java`

**Problema que resuelve:**  
El panel React necesita incidente + recursos asignados + zona de riesgo en una sola pantalla. Obligar al frontend a realizar 3+ peticiones aumenta latencia, consumo de batería en dispositivos de brigadistas y complejidad del cliente.

**Solución:**  
`EmergenciaFacadeService.obtenerResumen(incidenteId)` consulta en paralelo vía WebClient:
1. `http://INCIDENTES/incidentes/{id}`
2. `http://RECURSOS/recursos/incidente/{id}`
3. `http://ZONASRIESGO/zonas-riesgo/coordenadas` (si hay lat/long)

Devuelve `EmergenciaResumenDto` unificado.

**Endpoint:** `GET /bff/emergencias/{incidenteId}/resumen`

**Beneficios:**
- Interfaz simple para el frontend.
- Menor tráfico de red en el cliente.
- Coherente con el patrón Facade del informe técnico.

**Prueba unitaria:** `EmergenciaFacadeServiceTest` (fallback de contingencia)

---

### 4.4 Repository (Persistencia)

**Ubicación:** Paquetes `repository/` en cada microservicio:
- `IncidenteRepository`
- `RecursoRepository`
- `ZonaRiesgoRepository`

**Problema que resuelve:**  
Mezclar SQL/JPA en controllers o services dificulta pruebas y cambios de persistencia.

**Solución:**  
Capa Repository con Spring Data JPA. Services y controllers no conocen detalles de acceso a datos.

**Beneficios:**
- Separación Controller → Service → Repository (informe técnico).
- Repositorios mockeables en tests unitarios.

---

### 4.5 Circuit Breaker (Resiliencia)

**Ubicación:** `EmergenciaFacadeService` — anotación `@CircuitBreaker(name = "bffEmergencia", fallbackMethod = "resumenFallback")`

**Problema que resuelve:**  
Durante una catástrofe, un microservicio puede saturarse o caer. Sin protección, el BFF propagaría errores al frontend y colapsaría la experiencia operativa.

**Solución:**  
Resilience4j abre el circuito tras umbral de fallos y ejecuta `resumenFallback`, devolviendo datos de contingencia (`datosContingencia: true`) para mantener operatividad básica.

**Beneficios:**
- Evita cascadas de fallos.
- Alineado con atributo de disponibilidad del informe (ISO 25010).

---

### 4.6 Gateway Filter (Seguridad perimetral)

**Ubicación:** `infraestructuredomain/apigateway/.../AuthenticationFilterinig.java`

**Problema que resuelve:**  
Cada microservicio no debe reimplementar validación JWT.

**Solución:**  
Filtro centralizado en API Gateway que valida header `Authorization` antes de enrutar al BFF o microservicios.

---

## 5. Patrones de diseño — Frontend (NPM)

**Paquete:** `@valledelsol/sre-ui` — `frontend/sre-ui/`

### 5.1 Custom Hook

**Ubicación:** `src/hooks/useEmergenciaResumen.js`

**Problema:** Encapsular estado (`data`, `loading`, `error`) y acciones (`cargar`, `limpiar`) sin duplicar lógica en cada componente.

**Beneficio:** Reutilización y separación UI / lógica de datos.

**Prueba:** `useEmergenciaResumen.test.js`

---

### 5.2 Provider (Context)

**Ubicación:** `src/context/AlertContext.jsx`

**Problema:** Mostrar alertas operativas (éxito, error, contingencia) en toda la app sin pasar props por múltiples niveles (prop drilling).

**Beneficio:** Estado global de alertas con API `publicar` / `descartar`.

---

### 5.3 Compound Components

**Ubicación:** `src/components/emergencia/EmergenciaPanel.jsx`

**Problema:** El panel de emergencia tiene secciones (cabecera, cuerpo, pie) que deben componerse flexiblemente.

**Solución:** `EmergenciaPanel.Header`, `.Body`, `.Footer` comparten contexto interno del panel.

**Beneficio:** UI declarativa y extensible.

---

### 5.4 Facade HTTP

**Ubicación:** `src/services/emergenciaApi.js`

**Problema:** Los componentes no deben conocer URLs del BFF, headers JWT ni manejo de errores HTTP.

**Solución:** Función `fetchEmergenciaResumen(incidenteId)` como único punto de acceso.

**Prueba:** `emergenciaApi.test.js`

---

## 6. Resumen de patrones (tabla consolidada)

| # | Patrón | Capa | Módulo | Problema resuelto |
|---|--------|------|--------|-------------------|
| 1 | Factory Method | Backend | incidentes | Reglas de estado del incidente |
| 2 | Adapter | Backend | zonasriesgo | Proveedor climático intercambiable |
| 3 | Facade | Backend | bff | Agregación para el frontend |
| 4 | Repository | Backend | todos MS | Desacoplar persistencia |
| 5 | Circuit Breaker | Backend | bff | Resiliencia ante fallos |
| 6 | Custom Hook | Frontend | sre-ui | Lógica de carga reutilizable |
| 7 | Context Provider | Frontend | sre-ui | Alertas globales |
| 8 | Compound Components | Frontend | sre-ui | Composición de UI |
| 9 | Facade HTTP | Frontend | sre-ui | Acceso unificado al BFF |

Se supera ampliamente el mínimo de **3 patrones** exigido en frontend y backend.

---

## 7. Arquetipos Maven

### 7.1 Justificación

El informe técnico y la pauta EP2 exigen **estandarización** en la creación de nuevos módulos backend. Los arquetipos garantizan que todo microservicio o BFF futuro comparta la misma estructura, dependencias y convenciones del monorepo `sre-parent`.

**Ubicación:** `archetypes/`

### 7.2 Arquetipos disponibles

| Artefacto | groupId | Descripción |
|-----------|---------|-------------|
| `sre-microservice-archetype` | `com.valledelsol.sre` | MS con Controller, Service, Repository, Eureka, H2, excepciones |
| `sre-bff-archetype` | `com.valledelsol.sre` | BFF WebFlux + WebClient + Resilience4j + endpoint de ejemplo |

### 7.3 Instalación local

```bash
mvn -pl archetypes install
```

### 7.4 Ejemplo — generar microservicio

```bash
cd businessdomain
mvn archetype:generate \
  -DarchetypeGroupId=com.valledelsol.sre \
  -DarchetypeArtifactId=sre-microservice-archetype \
  -DarchetypeVersion=1.0-SNAPSHOT \
  -DgroupId=com.valledelsol.sre \
  -DartifactId=alertas \
  -Dversion=0.0.1-SNAPSHOT \
  -Dpackage=com.valledelsol.alertas \
  -DserviceName=alertas \
  -DapplicationClass=AlertasApplication \
  -DentityName=Alerta
```

### 7.5 Relación arquetipo ↔ patrones

El arquetipo de microservicio **incorpora por defecto** la estructura en capas (Repository) y plantillas para reglas de negocio. El arquetipo BFF incluye un **Facade** de ejemplo con **Circuit Breaker**, replicando las decisiones del BFF principal del proyecto.

---

## 8. Pruebas unitarias (evidencia)

### 8.1 Backend (JUnit + Mockito)

| Módulo | Clase de test | Qué valida |
|--------|---------------|------------|
| incidentes | `EstadoIncidenteFactoryTest` | Reglas Factory (coordenadas en EN_PROGRESO) |
| incidentes | `IncidenteServiceTest` | Crear, cambiar estado, errores 404 |
| recursos | `RecursoServiceTest` | Asignación, validación, liberación |
| zonasriesgo | `FakeWeatherAdapterTest` | Snapshot climático simulado |
| zonasriesgo | `ZonaRiesgoServiceTest` / `ZonaRiesgoRecalibrarTest` | Cálculo de nivel de riesgo |
| bff | `EmergenciaFacadeServiceTest` | Fallback de contingencia |

**Comando:**
```bash
mvn test
```

### 8.2 Frontend (Vitest)

| Archivo | Tests |
|---------|-------|
| `useEmergenciaResumen.test.js` | Carga exitosa y manejo de error |
| `emergenciaApi.test.js` | Facade HTTP OK y error 401 |

**Comando:**
```bash
cd frontend/sre-ui && npm test
```

### 8.3 Capturas para anexo

> **[INSERTAR CAPTURA]** — Salida de `mvn test`  
> **[INSERTAR CAPTURA]** — Salida de `npm test` (4 tests passed)

---

## 9. Conclusión

La implementación del SRE Valle del Sol aplica patrones de diseño de forma consciente y alineada al informe técnico EP1 y a los requisitos EP2. Los patrones **Factory Method**, **Adapter** y **Facade** resuelven complejidad de dominio, integración externa y experiencia del frontend. **Repository** y **Circuit Breaker** refuerzan mantenibilidad y disponibilidad. En el frontend, **Hook**, **Context**, **Compound Components** y **Facade HTTP** mantienen el código ordenado y testeable.

Los **arquetipos Maven** cierran el ciclo de estandarización, permitiendo escalar el ecosistema SRE sin desorden arquitectónico.

---

## Referencias

- Informe Técnico de Diseño Arquitectónico — EP1 DSY1106
- Pauta Evaluación Parcial N°2 — EP2 DSY1106
- Repositorio: https://github.com/KhanIvall/SRE-ValleDeSol
- Documentación interna: `README.md`, READMEs por módulo, `archetypes/README.md`

---

*Documento generado como borrador Markdown — exportar a PDF para entrega formal.*
