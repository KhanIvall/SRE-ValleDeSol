# Microservicio Recursos

Administración logística de brigadas, vehículos y equipos. Persistencia autónoma; la asignación a incidentes usa `incidenteAsignadoId` (sin FK entre microservicios).

| Atributo | Valor |
|----------|-------|
| **Eureka** | `RECURSOS` |
| **Base path** | `/recursos` |
| **Puerto** | Dinámico |
| **Persistencia** | H2 en memoria |

## Patrones de diseño

| Patrón | Ubicación | Problema que resuelve |
|--------|-----------|------------------------|
| **Repository** | `repository/RecursoRepository` | Acceso a datos desacoplado del dominio |
| **Reglas de negocio** | `service/RecursoService` | Solo recursos `DISPONIBLE` pueden asignarse a un incidente |

## Requisitos

- JDK 17+, Maven 3.9+
- **Eureka** en ejecución: http://localhost:8761

## Instalación

```bash
mvn -pl businessdomain/recursos compile
```

## Ejecución

Desde la **raíz del monorepo**:

```bash
mvn -pl businessdomain/recursos spring-boot:run
```

Obtén el puerto en los logs (`Tomcat started on port XXXXX`) o en Eureka (instancia `RECURSOS`).

## Verificación

```bash
curl http://localhost:PUERTO/recursos
curl http://localhost:PUERTO/recursos/disponibles
```

## API principal

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/recursos` | Listar todos |
| GET | `/recursos/disponibles` | Solo disponibles |
| GET | `/recursos/incidente/{incidenteId}` | Asignados a un incidente |
| GET | `/recursos/{id}` | Obtener por ID |
| POST | `/recursos` | Crear recurso |
| PUT | `/recursos/{id}` | Actualizar |
| PUT | `/recursos/{id}/asignar` | Asignar — body: `{"incidenteId":1}` |
| PUT | `/recursos/{id}/liberar` | Devolver a disponible |
| DELETE | `/recursos/{id}` | Eliminar |

### Tipos y estados

- **Tipos:** `BRIGADA`, `VEHICULO`, `EQUIPO`
- **Estados:** `DISPONIBLE`, `DESPLEGADO`, `EN_MANTENIMIENTO`

## Datos de prueba (demo EP2)

Requiere un incidente existente (ver [incidentes/README.md](../incidentes/README.md)).

**PowerShell:**

```powershell
# Crear recurso
$rec = @{ nombre = "Ambulancia 01"; tipo = "VEHICULO"; identificador = "AMB-01" } | ConvertTo-Json
$r = Invoke-RestMethod -Uri "http://localhost:PUERTO/recursos" -Method Post -Body $rec -ContentType "application/json"

# Asignar al incidente 1
$asig = @{ incidenteId = 1 } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:PUERTO/recursos/$($r.id)/asignar" -Method Put -Body $asig -ContentType "application/json"
```

**curl:**

```bash
curl -X POST http://localhost:PUERTO/recursos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ambulancia 01","tipo":"VEHICULO","identificador":"AMB-01"}'

curl -X PUT http://localhost:PUERTO/recursos/1/asignar \
  -H "Content-Type: application/json" \
  -d '{"incidenteId":1}'
```

El **BFF** consulta `GET /recursos/incidente/{id}` al armar el resumen de emergencia.

## Pruebas automatizadas

```bash
mvn -pl businessdomain/recursos test
```
