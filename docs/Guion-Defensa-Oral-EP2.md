# Guion de Defensa Oral — EP2 (15 minutos)

**SRE Valle del Sol — DSY1106**

Integrantes: Ari Araya, Skarlett Tropan

> Distribución sugerida: ~7 min presentación grupal + ~8 min preguntas individuales del docente.

---

## Estructura de la presentación (7 min)

### 1. Contexto (1 min) — Integrante A

- Municipalidad Valle del Sol, emergencias (incendios).
- Problema: sistemas fragmentados, respuesta lenta.
- Solución: SRE con microservicios + BFF + React.

### 2. Arquitectura (1,5 min) — Integrante B

Mostrar diagrama:

```
React → Gateway :8080 → BFF :8085 → [incidentes | recursos | zonasriesgo]
                              ↑
                           Eureka
```

- **Gateway:** seguridad y rutas.
- **BFF:** agrega datos (Facade); el UI no llama 3 servicios.
- **Microservicios:** dominio independiente, BD propia.

### 3. Patrones backend (2 min) — Integrante A

| Patrón | Dónde | Una frase |
|--------|-------|-----------|
| Factory Method | incidentes | Estados con reglas; coordenadas antes de EN_PROGRESO |
| Adapter | zonasriesgo | Clima simulado; cambiar proveedor sin tocar servicio |
| Facade | BFF | Un endpoint `/bff/emergencias/{id}/resumen` |
| Circuit Breaker | BFF | Fallback si un MS cae |

### 4. Frontend NPM (1 min) — Integrante B

- Paquete `@valledelsol/sre-ui`.
- Patrones: Custom Hook, Context, Compound Components, Facade HTTP.
- Demo: consultar incidente ID 1 en el panel.

### 5. GitFlow + entregables (1 min) — Integrante A

- Ramas `feature/ep2-*`, PR #1 y #2 en GitHub.
- Arquetipos Maven para estandarizar nuevos módulos.

### 6. Pruebas (0,5 min) — Integrante B

- `mvn test` backend, `npm test` frontend (4 tests).
- Mencionar clases: `EstadoIncidenteFactoryTest`, `EmergenciaFacadeServiceTest`.

---

## Preguntas frecuentes — respuestas cortas

### ¿Por qué BFF y no solo API Gateway?

El Gateway **enruta y protege**. El BFF **agrega y adapta** datos al formato que necesita React. Son responsabilidades distintas.

### ¿Por qué Factory Method en incidentes?

Cada estado tiene reglas distintas. Centralizar en factory evita `if/else` dispersos y facilita agregar nuevos tipos de emergencia.

### ¿Por qué Adapter en zonas de riesgo?

Hoy simulamos clima; mañana puede ser API real o IoT. El servicio solo conoce la interfaz `WeatherDataPort`.

### ¿Qué pasa si un microservicio falla?

El BFF activa Circuit Breaker y devuelve `datosContingencia: true` con información mínima.

### ¿Cómo trabajaron en Git?

GitFlow: features → develop/main vía Pull Requests. Evidencia en GitHub PR #1 y #2.

### ¿Qué son los arquetipos?

Plantillas Maven para generar nuevos microservicios o BFF con la misma estructura del proyecto.

---

## Demo rápida (opcional, 2 min)

1. Eureka en 8761.
2. Microservicios + BFF + Gateway.
3. `npm run dev` en frontend.
4. Consultar resumen incidente #1.
5. Mostrar respuesta JSON agregada.

---

## Reparto individual sugerido

| Integrante | Temas fuertes para preguntas |
|------------|------------------------------|
| Ari | GitFlow, incidentes, Factory, arquetipos, branching PDF |
| Skarlett | BFF, Adapter, frontend, tests, arquitectura |

---

*Ajustar reparto según participación real del equipo.*
