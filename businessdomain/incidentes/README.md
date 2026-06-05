# Microservicio Incidentes

Núcleo de gestión de incidentes del SRE. Capas **Controller → Service → Repository** y patrón **Factory Method** para transiciones de estado.

| Atributo | Valor |
|----------|-------|
| **Eureka** | `INCIDENTES` |
| **Base path** | `/incidentes` |
| **Puerto** | Dinámico (`server.port=0`) |
| **Persistencia** | H2 en memoria (se reinicia al detener el proceso) |

## Patrones de diseño

| Patrón | Ubicación | Problema que resuelve |
|--------|-----------|------------------------|
| **Factory Method** | `factory/EstadoIncidenteFactory` | Validar reglas al cambiar estado (p. ej. coordenadas obligatorias en `EN_PROGRESO`) |
| **Repository** | `repository/IncidenteRepository` | Abstraer acceso a datos JPA |

## Requisitos

- JDK 17+, Maven 3.9+
- **Eureka** en ejecución: http://localhost:8761

## Instalación

No requiere pasos adicionales. Las dependencias se resuelven con Maven desde la raíz del monorepo.

```bash
mvn -pl businessdomain/incidentes compile
```

## Ejecución

Desde la **raíz del monorepo**:

```bash
mvn -pl businessdomain/incidentes spring-boot:run
```

El proceso queda activo en la terminal. Detener con `Ctrl+C`.

**Obtener el puerto:** en los logs busca `Tomcat started on port XXXXX` o consulta el dashboard de Eureka (instancia `INCIDENTES`).

## Verificación

```bash
# Listar incidentes (reemplaza PUERTO)
curl http://localhost:PUERTO/incidentes

# Swagger UI
http://localhost:PUERTO/swagger-ui.html
```

## API principal

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/incidentes` | Listar todos |
| GET | `/incidentes/{id}` | Obtener por ID |
| POST | `/incidentes` | Crear incidente |
| PUT | `/incidentes/{id}` | Actualizar datos |
| PUT | `/incidentes/{id}/estado` | Cambiar estado — body: `{"estado":"EN_PROGRESO"}` |
| DELETE | `/incidentes/{id}` | Eliminar |

### Estados válidos

`REPORTADO`, `EN_PROGRESO`, `CONTROLADO`, `CERRADO`

## Datos de prueba (demo EP2)

**PowerShell** (reemplaza `PUERTO`):

```powershell
$body = @{
  tipo = "INCENDIO"
  descripcion = "Prueba panel SRE"
  latitud = -33.45
  longitud = -70.66
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:PUERTO/incidentes" -Method Post -Body $body -ContentType "application/json"
```

**curl** (bash):

```bash
curl -X POST http://localhost:PUERTO/incidentes \
  -H "Content-Type: application/json" \
  -d '{"tipo":"INCENDIO","descripcion":"Prueba panel SRE","latitud":-33.45,"longitud":-70.66}'
```

El incidente creado recibe `id: 1` si la base está vacía. Ese ID se usa en el panel y en el BFF (`/bff/emergencias/1/resumen`).

> **Nota:** Vía API Gateway (`8080`) las mismas rutas requieren header `Authorization: Bearer <token>` si el filtro JWT está activo.

## Pruebas automatizadas

```bash
mvn -pl businessdomain/incidentes test
```

Incluye `EstadoIncidenteFactoryTest` e `IncidenteServiceTest`.

## Relación con otros componentes

- El **BFF** consulta `GET /incidentes/{id}` por Eureka (`INCIDENTES`).
- **Recursos** referencia incidentes por `incidenteAsignadoId` sin FK entre bases de datos.
