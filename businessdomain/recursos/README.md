# Microservicio Recursos

Administración logística de brigadas, vehículos y equipos. Persistencia autónoma; la asignación a incidentes usa `incidenteAsignadoId` (sin FK entre microservicios).

- **Eureka:** `recursos`
- **Base path:** `/recursos`
- **Puerto:** dinámico

## Patrones

- **Repository:** `RecursoRepository`
- Reglas de negocio en `RecursoService` (solo recursos `DISPONIBLE` pueden asignarse)

## Ejecución

```bash
mvn -pl businessdomain/recursos spring-boot:run
```

## API principal

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/recursos` | Listar todos |
| GET | `/recursos/disponibles` | Solo disponibles |
| GET | `/recursos/incidente/{incidenteId}` | Asignados a un incidente |
| POST | `/recursos` | Crear recurso |
| PUT | `/recursos/{id}/asignar` | Body: `{"incidenteId":1}` |
| PUT | `/recursos/{id}/liberar` | Devolver a disponible |
| DELETE | `/recursos/{id}` | Eliminar |

## Ejemplo

```bash
curl -X POST http://localhost:8080/recursos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Brigada Norte","tipo":"BRIGADA","estado":"DISPONIBLE"}'
```

## Pruebas

```bash
mvn -pl businessdomain/recursos test
```
