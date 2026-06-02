# ${artifactId}

Microservicio SRE generado con **sre-microservice-archetype**.

- Paquete: `${package}`
- Eureka: `${serviceName}`
- Capas: Controller → Service → Repository

## Ejecución

Desde la raíz del monorepo (con Eureka activo):

```bash
mvn -pl businessdomain/${artifactId} spring-boot:run
```

Registre el módulo en `businessdomain/pom.xml` si aún no está listado.
