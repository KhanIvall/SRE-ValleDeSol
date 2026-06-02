# Microservicio Incidentes

Núcleo de gestión de incidentes del SRE. Capas **Controller → Service → Repository** y patrón **Factory Method** para transiciones de estado.

- **Eureka:** `incidentes`
- **Base path:** `/incidentes`
- **Puerto:** dinámico (`server.port=0`)

## Patrones

- **Factory Method:** `factory/EstadoIncidenteFactory` — valida reglas al cambiar estado (p. ej. coordenadas obligatorias en `EN_PROGRESO`).
- **Repository:** `IncidenteRepository`

## Ejecución

Requisito: Eureka en `http://localhost:8761`.

```bash
# Desde la raíz del monorepo
mvn -pl businessdomain/incidentes spring-boot:run
```

Swagger UI (puerto asignado al arrancar): `http://localhost:{puerto}/swagger-ui.html`

## API principal

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/incidentes` | Listar |
| GET | `/incidentes/{id}` | Obtener por ID |
| POST | `/incidentes` | Crear incidente |
| PUT | `/incidentes/{id}` | Actualizar datos |
| PUT | `/incidentes/{id}/estado` | Cambiar estado — body: `{"estado":"EN_PROGRESO"}` |
| DELETE | `/incidentes/{id}` | Eliminar |

## Ejemplo

```bash
curl -X POST http://localhost:8080/incidentes \
  -H "Content-Type: application/json" \
  -d '{"tipo":"INCENDIO_FORESTAL","descripcion":"Foco en cerro"}'
```

*(Vía API Gateway en 8080 si está activo; requiere JWT si el filtro de auth está habilitado.)*

## Pruebas

```bash
mvn -pl businessdomain/incidentes test
```

Incluye `EstadoIncidenteFactoryTest` e `IncidenteServiceTest`.
