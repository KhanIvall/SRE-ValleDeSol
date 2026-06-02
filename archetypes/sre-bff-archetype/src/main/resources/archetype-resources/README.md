# ${artifactId}

BFF generado con **sre-bff-archetype** (patron Facade + WebClient + Resilience4j).

- Paquete: `${package}`
- Puerto: `${bffPort}`
- Eureka: `${serviceName}`

## Ejecución

```bash
mvn -pl infraestructuredomain/${artifactId} spring-boot:run
```

Agregue el modulo en `infraestructuredomain/pom.xml` y una ruta en el API Gateway.
