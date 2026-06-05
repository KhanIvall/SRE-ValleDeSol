# Microservicio Zonas de Riesgo

Motor analítico territorial. Integra datos climáticos vía patrón **Adapter** y calcula nivel de riesgo por zona.

| Atributo | Valor |
|----------|-------|
| **Eureka** | `ZONASRIESGO` |
| **Base path** | `/zonas-riesgo` |
| **Puerto** | Dinámico |
| **Adapter activo** | `FakeWeatherAdapter` (`sre.weather.adapter=fake`) |
| **Persistencia** | H2 en memoria |

## Patrones de diseño

| Patrón | Ubicación | Problema que resuelve |
|--------|-----------|------------------------|
| **Adapter** | `adapter/WeatherDataPort` + `FakeWeatherAdapter` | Cambiar proveedor climático sin modificar el servicio de dominio |
| **Repository** | `repository/ZonaRiesgoRepository` | Persistencia JPA |

## Requisitos

- JDK 17+, Maven 3.9+
- **Eureka** en ejecución: http://localhost:8761

## Instalación

```bash
mvn -pl businessdomain/zonasriesgo compile
```

## Ejecución

Desde la **raíz del monorepo**:

```bash
mvn -pl businessdomain/zonasriesgo spring-boot:run
```

Obtén el puerto en los logs o en Eureka (instancia `ZONASRIESGO`).

## Verificación

```bash
curl http://localhost:PUERTO/zonas-riesgo
curl "http://localhost:PUERTO/zonas-riesgo/coordenadas?latitud=-33.45&longitud=-70.66"
```

## API principal

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/zonas-riesgo` | Listar zonas |
| GET | `/zonas-riesgo/{id}` | Obtener por ID |
| GET | `/zonas-riesgo/coordenadas?latitud=&longitud=` | Buscar por coordenadas (tolerancia ±0.01°) |
| POST | `/zonas-riesgo` | Crear (enriquece con clima simulado) |
| PUT | `/zonas-riesgo/{id}` | Actualizar |
| PUT | `/zonas-riesgo/{id}/recalibrar` | Recalcular riesgo con clima actual |
| DELETE | `/zonas-riesgo/{id}` | Eliminar |

### Niveles de riesgo

`BAJO`, `MEDIO`, `ALTO`, `CRITICO` (asignados según condiciones climáticas simuladas).

## Datos de prueba (demo EP2)

Usa las **mismas coordenadas** que el incidente de prueba (`-33.45`, `-70.66`) para que el BFF encuentre la zona.

**PowerShell:**

```powershell
$body = @{ nombre = "Cerro Alto"; latitud = -33.45; longitud = -70.66 } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:PUERTO/zonas-riesgo" -Method Post -Body $body -ContentType "application/json"
```

**curl:**

```bash
curl -X POST http://localhost:PUERTO/zonas-riesgo \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Cerro Alto","latitud":-33.45,"longitud":-70.66}'
```

Al crear, el servicio consulta el adapter de clima y persiste `nivelRiesgo`, `condicionClimatica`, temperatura y humedad.

El **BFF** busca la zona con `GET /zonas-riesgo/coordenadas` usando latitud/longitud del incidente.

## Pruebas automatizadas

```bash
mvn -pl businessdomain/zonasriesgo test
```

Incluye `FakeWeatherAdapterTest` y `ZonaRiesgoServiceTest`.
