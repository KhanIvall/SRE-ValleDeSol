# Microservicio Zonas de Riesgo

Motor analítico territorial. Integra datos climáticos vía patrón **Adapter** y calcula nivel de riesgo por zona.

- **Eureka:** `zonasriesgo`
- **Base path:** `/zonas-riesgo`
- **Puerto:** dinámico
- **Adapter activo:** `FakeWeatherAdapter` (`sre.weather.adapter=fake`)

## Patrones

- **Adapter:** `WeatherDataPort` + `FakeWeatherAdapter` — permite cambiar proveedor climático sin tocar el servicio.
- **Repository:** `ZonaRiesgoRepository`

## Ejecución

```bash
mvn -pl businessdomain/zonasriesgo spring-boot:run
```

## API principal

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/zonas-riesgo` | Listar zonas |
| GET | `/zonas-riesgo/coordenadas?latitud=&longitud=` | Buscar por coordenadas |
| POST | `/zonas-riesgo` | Crear (enriquece con clima simulado) |
| PUT | `/zonas-riesgo/{id}/recalibrar` | Recalcular riesgo |

## Ejemplo

```bash
curl -X POST http://localhost:8080/zonas-riesgo \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Cerro Alto","latitud":-33.45,"longitud":-70.66}'
```

## Pruebas

```bash
mvn -pl businessdomain/zonasriesgo test
```

Incluye `FakeWeatherAdapterTest` y `ZonaRiesgoServiceTest`.
