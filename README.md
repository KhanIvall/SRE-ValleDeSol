# SRE Valle del Sol

Sistema de Respuesta de Emergencias — Municipalidad Valle del Sol.  
Monorepo fullstack: microservicios Java/Spring Boot, BFF, API Gateway y frontend React (NPM).

**Versión:** 1.0-SNAPSHOT (Maven) / 1.0.0 (frontend NPM)

## Estructura

| Carpeta | Contenido |
|---------|-----------|
| `businessdomain/` | Microservicios: incidentes, recursos, zonasriesgo |
| `infraestructuredomain/` | Eureka, API Gateway, BFF, Keycloak, Spring Boot Admin |
| `frontend/sre-ui/` | Panel React (`@valledelsol/sre-ui`) |
| `archetypes/` | Arquetipos Maven para nuevos microservicios y BFF |

## Requisitos

- JDK 17+
- Maven 3.9+
- Node.js 18+ (solo para frontend)

## Orden de arranque (desarrollo)

1. **Eureka** — puerto 8761  
   `mvn -pl infraestructuredomain/eurekaServer spring-boot:run`

2. **Microservicios** (en terminales separadas):  
   `mvn -pl businessdomain/incidentes spring-boot:run`  
   `mvn -pl businessdomain/recursos spring-boot:run`  
   `mvn -pl businessdomain/zonasriesgo spring-boot:run`

3. **BFF** — puerto 8085  
   `mvn -pl infraestructuredomain/bff spring-boot:run`

4. **API Gateway** — puerto 8080  
   `mvn -pl infraestructuredomain/apigateway spring-boot:run`

5. **Frontend**  
   `cd frontend/sre-ui && npm install && npm run dev` → http://localhost:5173

## Pruebas

```bash
# Backend (desde la raíz)
mvn test

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

- [incidentes](businessdomain/incidentes/README.md)
- [recursos](businessdomain/recursos/README.md)
- [zonasriesgo](businessdomain/zonasriesgo/README.md)
- [bff](infraestructuredomain/bff/README.md)
- [apigateway](infraestructuredomain/apigateway/README.md)
- [frontend](frontend/sre-ui/README.md)
- [arquetipos](archetypes/README.md)

## Repositorio

https://github.com/KhanIvall/SRE-ValleDeSol
