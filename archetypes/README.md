# Arquetipos Maven — SRE Valle del Sol

Arquetipos para estandarizar la creación de **microservicios** y módulos **BFF** alineados con el monorepo `sre-parent`.

## Arquetipos disponibles

| Artefacto | Descripción |
|-----------|-------------|
| `sre-microservice-archetype` | Spring Boot + JPA + Eureka + capas Controller/Service/Repository |
| `sre-bff-archetype` | WebFlux + Eureka + WebClient + Resilience4j (Facade) |

## Instalación en el repositorio local

Desde la raíz del proyecto:

```bash
mvn -pl archetypes install
```

O solo un arquetipo:

```bash
mvn -pl archetypes/sre-microservice-archetype install
mvn -pl archetypes/sre-bff-archetype install
```

## Generar un microservicio

Ejecutar dentro de `businessdomain/`:

```bash
cd businessdomain
mvn archetype:generate ^
  -DarchetypeGroupId=com.valledelsol.sre ^
  -DarchetypeArtifactId=sre-microservice-archetype ^
  -DarchetypeVersion=1.0-SNAPSHOT ^
  -DgroupId=com.valledelsol.sre ^
  -DartifactId=alertas ^
  -Dversion=0.0.1-SNAPSHOT ^
  -Dpackage=com.valledelsol.alertas ^
  -DserviceName=alertas ^
  -DapplicationClass=AlertasApplication ^
  -DentityName=Alerta
```

Luego agregar `<module>alertas</module>` en `businessdomain/pom.xml`.

## Generar un BFF

Ejecutar dentro de `infraestructuredomain/`:

```bash
cd infraestructuredomain
mvn archetype:generate ^
  -DarchetypeGroupId=com.valledelsol.sre ^
  -DarchetypeArtifactId=sre-bff-archetype ^
  -DarchetypeVersion=1.0-SNAPSHOT ^
  -DgroupId=com.valledelsol.sre ^
  -DartifactId=operaciones-bff ^
  -Dversion=0.0.1-SNAPSHOT ^
  -Dpackage=com.valledelsol.operaciones.bff ^
  -DserviceName=operaciones-bff ^
  -DapplicationClass=OperacionesBffApplication ^
  -DbffPort=8086
```

Agregar el módulo en `infraestructuredomain/pom.xml` y configurar ruta en `apigateway`.

## Propiedades del arquetipo microservicio

| Propiedad | Descripción |
|-----------|-------------|
| `package` | Paquete Java base |
| `serviceName` | `spring.application.name` / registro Eureka |
| `applicationClass` | Clase principal Spring Boot |
| `entityName` | Nombre de la entidad JPA de plantilla |

## Propiedades del arquetipo BFF

| Propiedad | Descripción |
|-----------|-------------|
| `bffPort` | Puerto HTTP del BFF |
| `applicationClass` | Clase principal |
| `serviceName` | Nombre en Eureka |

## Estructura generada (microservicio)

```text
src/main/java/.../
  __applicationClass__.java
  controller/
  service/
  repository/
  entities/
  common/
  exception/
src/main/resources/application.properties
pom.xml
```
