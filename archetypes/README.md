# Arquetipos Maven — SRE Valle del Sol

Arquetipos para estandarizar la creación de **microservicios** y módulos **BFF** alineados con el monorepo `sre-parent`. Cumplen el requisito EP2 de componentes backend basados en arquetipos Maven.

## Arquetipos disponibles

| Artefacto | Descripción |
|-----------|-------------|
| `sre-microservice-archetype` | Spring Boot + JPA + Eureka + capas Controller/Service/Repository |
| `sre-bff-archetype` | WebFlux + Eureka + WebClient + Resilience4j (Facade) |

## Requisitos

- JDK 17+, Maven 3.9+
- Proyecto clonado en local (monorepo `SRE-ValleDeSol`)

## Instalación en el repositorio local

Desde la **raíz del monorepo**:

```bash
mvn -pl archetypes install
```

Instala ambos arquetipos en el repositorio Maven local (`~/.m2`). Solo es necesario una vez por máquina (o tras cambios en los arquetipos).

Instalar un arquetipo individual:

```bash
mvn -pl archetypes/sre-microservice-archetype install
mvn -pl archetypes/sre-bff-archetype install
```

## Verificación de la instalación

```bash
mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout
```

Los arquetipos quedan bajo `com/valledelsol/sre/` en ese directorio.

## Generar un microservicio

Ejecutar dentro de `businessdomain/`:

**Windows (cmd/PowerShell):**

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

**Linux/macOS:**

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

Luego agregar `<module>alertas</module>` en `businessdomain/pom.xml` y ejecutar:

```bash
mvn -pl businessdomain/alertas spring-boot:run
```

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
README.md
```

## Pruebas

Los proyectos generados incluyen `*ApplicationTests`. Ejecutar desde el nuevo módulo:

```bash
mvn -pl businessdomain/<nuevo-modulo> test
```
