# Plan de Branching — GitFlow

**Sistema de Respuesta de Emergencias (SRE) — Municipalidad Valle del Sol**

| Campo | Detalle |
|-------|---------|
| Asignatura | DSY1106 — Desarrollo Fullstack III |
| Evaluación | Parcial N°2 (EP2) |
| Versión | 1.0.0 |
| Integrantes | Ari Araya, Skarlett Tropan |
| Repositorio | https://github.com/KhanIvall/SRE-ValleDeSol |
| Fecha | _________________ |

---

## 1. Objetivo del documento

Describir la **estrategia de branching** utilizada por el equipo durante el desarrollo del encargo EP2, evidenciando cómo la estructura de ramas favoreció la colaboración, el control de versiones y la integración incremental de componentes frontend, backend y arquetipos Maven.

---

## 2. Estrategia seleccionada: GitFlow simplificado

Se adoptó una variante de **GitFlow** adaptada al tamaño del equipo (2 integrantes) y al monorepo del proyecto:

```
                    feature/ep2-*
                   /     |     \
                  /      |      \
         develop ────────┼──────── (integración)
                \        |        /
                 \       |       /
                  main ──┴────── (entrega / release)
```

### 2.1 Roles de cada rama

| Rama | Propósito | Quién mergea | Protección |
|------|-----------|--------------|------------|
| **`main`** | Código estable para entrega EP2 y releases | Solo vía Pull Request | Rama de producción |
| **`develop`** | Integración continua de features completadas | Merge desde `feature/*` | Rama de desarrollo |
| **`feature/*`** | Una funcionalidad o entregable EP2 por rama | PR hacia `develop` o `main` | Vida corta; se elimina tras merge |

### 2.2 Convención de nombres

```
feature/ep2-<descripcion-corta>
```

Ejemplos usados en el proyecto:
- `feature/ep2-incidentes-recursos`
- `feature/ep2-zonas-bff`
- `feature/ep2-frontend-npm`
- `feature/ep2-arquetipos-maven`
- `feature/ep2-readmes-tests`

---

## 3. Flujo de trabajo del equipo

### Paso a paso por feature

1. **Actualizar** rama base (`develop` o `main` según el hito).
2. **Crear** rama feature: `git checkout -b feature/ep2-nombre`.
3. **Desarrollar** con commits atómicos y mensajes descriptivos (`feat(ep2): ...`, `docs(ep2): ...`).
4. **Push** a origin: `git push -u origin feature/ep2-nombre`.
5. **Abrir Pull Request** en GitHub hacia `develop` o `main`.
6. **Revisar** (el otro integrante) y **mergear**.
7. **Sincronizar** local: `git checkout develop && git pull`.

### Convención de commits

| Prefijo | Uso |
|---------|-----|
| `feat(ep2):` | Nueva funcionalidad |
| `chore(develop):` | Configuración, baseline |
| `docs(ep2):` | README, documentación |
| `fix(ep2):` | Corrección de bugs |

---

## 4. Historial de ramas y merges del proyecto

### 4.1 Línea de tiempo

| Orden | Rama feature | Contenido | Integración |
|-------|--------------|-----------|-------------|
| 1 | — | `main`: commit inicial | Base |
| 2 | — | `develop`: baseline Payment Chain → SRE | `chore(develop): baseline multi-modulo` |
| 3 | `feature/ep2-incidentes-recursos` | MS incidentes + recursos; elimina customer/product | Merge → `develop` |
| 4 | `feature/ep2-zonas-bff` | MS zonasriesgo + BFF Facade | Merge → `develop` |
| 5 | `feature/ep2-frontend-npm` | `@valledelsol/sre-ui` React/Vite | Merge → `develop` |
| 6 | — | **PR #1** `develop` → `main` | Backend + frontend en producción |
| 7 | `feature/ep2-arquetipos-maven` | Arquetipos MS + BFF | **PR #2** → `main` |
| 8 | `feature/ep2-readmes-tests` | READMEs + tests ampliados | PR pendiente → `main` |

### 4.2 Commits principales en `main`

```
41ff18f Merge pull request #2 from KhanIvall/feature/ep2-arquetipos-maven
86a29e2 feat(ep2): arquetipos Maven sre-microservice y sre-bff
f1a2d07 feat(ep2): frontend NPM React (@valledelsol/sre-ui)
201fd41 Merge pull request #1 from KhanIvall/develop
2076327 feat(ep2): zonas de riesgo (Adapter) y BFF agregador (Facade)
1ae606f feat(ep2): microservicios incidentes y recursos con capas SRE
c02b0a1 chore(develop): baseline multi-modulo businessdomain e infraestructuredomain
56a34b5 first commit
```

---

## 5. Pull Requests en GitHub (evidencia)

### PR #1 — Integración backend y frontend

| Campo | Valor |
|-------|-------|
| Origen | `develop` |
| Destino | `main` |
| Contenido | Microservicios SRE, BFF, infraestructura, frontend NPM |
| Merge commit | `201fd41` |

> **[INSERTAR CAPTURA]** — Página del PR #1 en GitHub (merged)

### PR #2 — Arquetipos Maven

| Campo | Valor |
|-------|-------|
| Origen | `feature/ep2-arquetipos-maven` |
| Destino | `main` |
| Contenido | `archetypes/sre-microservice-archetype`, `sre-bff-archetype` |
| Merge commit | `41ff18f` |

> **[INSERTAR CAPTURA]** — Página del PR #2 en GitHub (merged)

### PR #3 — Documentación y tests (pendiente al momento del borrador)

| Campo | Valor |
|-------|-------|
| Origen | `feature/ep2-readmes-tests` |
| Destino | `main` |
| Contenido | README raíz, READMEs por módulo, tests unitarios ampliados |

> **[INSERTAR CAPTURA]** — PR #3 (merged o abierto)

---

## 6. Colaboración y reparto de trabajo

| Integrante | Responsabilidades sugeridas para la defensa |
|------------|---------------------------------------------|
| **Ari Araya** | GitFlow, merges, microservicios incidentes/recursos, documentación |
| **Skarlett Tropan** | BFF, zonasriesgo, frontend NPM, arquetipos, pruebas |

> *Ajustar según reparto real del equipo.*

### Ventajas observadas del branching por feature

- **Paralelismo:** Un integrante pudo trabajar en frontend mientras el otro avanzaba backend.
- **Aislamiento:** Un error en una feature no afectaba `main` hasta el PR.
- **Trazabilidad:** Cada entregable EP2 mapea a una rama identificable.
- **Revisión:** Los PR en GitHub documentan qué código entró y cuándo.

---

## 7. Gestión de conflictos

### 7.1 Estrategia preventiva

- Ramas **cortas** por funcionalidad (1–2 días de trabajo).
- Módulos en **carpetas separadas** (`incidentes/`, `recursos/`, `bff/`, `frontend/`) para reducir solapamiento.
- **`pom.xml` raíz:** único punto de conflicto frecuente; se resolvió agregando `<module>` en commits dedicados.

### 7.2 Conflictos encontrados

| Situación | Resolución |
|-----------|------------|
| Merge `develop` → `main` (PR #1) | Fast-forward / merge limpio — módulos nuevos, sin solapamiento con `customer`/`product` ya eliminados |
| PR #2 arquetipos → `main` | Merge limpio — carpeta `archetypes/` nueva + una línea en `pom.xml` padre |
| `develop` vs `main` desincronizados tras PR #2 directo a `main` | Pendiente: `git merge main` en `develop` para realinear |

> Si hubo conflictos manuales en algún PR, **[INSERTAR CAPTURA]** del diff/conflicto resuelto en GitHub o IDE.

### 7.3 Comandos útiles usados

```bash
git fetch origin
git checkout develop
git pull origin develop
git checkout -b feature/ep2-nombre
# ... trabajo ...
git add .
git commit -m "feat(ep2): descripcion"
git push -u origin feature/ep2-nombre
# Crear PR en GitHub
```

---

## 8. Diagrama GitFlow del proyecto

```
main:     ●───●────────────●──────────●  (PR#1) ────● (PR#2) ─── ● (PR#3?)
          │   │            │          │            │
develop:  │   ●──●──●──●──●┘          │            │
          │   │  │  │  │             │            │
features: │   │  │  │  └─ ep2-frontend-npm       │
          │   │  │  └──── ep2-zonas-bff           │
          │   │  └─────── ep2-incidentes-recursos │
          │   └────────── baseline                 │
          └──────────── first commit              │
                                                    │
                    ep2-arquetipos-maven ──────────┘ (PR directo a main)
                    ep2-readmes-tests ────────────── (PR a main)
```

---

## 9. Tag de release (recomendado)

Tras merge final en `main`:

```bash
git tag -a v1.0.0-ep2 -m "Entrega EP2 SRE Valle del Sol"
git push origin v1.0.0-ep2
```

Esto marca la versión de entrega referenciada en `repositorios.txt` y en los PDFs.

---

## 10. Conclusión

La estrategia GitFlow simplificada permitió al equipo desarrollar de forma **incremental** y **colaborativa** todos los componentes exigidos por EP2 (microservicios, BFF, frontend NPM, arquetipos), manteniendo `main` como rama estable de entrega. Los **Pull Requests** en GitHub constituyen evidencia auditable de merges, revisiones y evolución del proyecto.

---

## Anexo — Checklist de evidencias para la defensa oral

- [ ] Captura: lista de ramas en GitHub
- [ ] Captura: PR #1 merged
- [ ] Captura: PR #2 merged
- [ ] Captura: PR readmes-tests merged
- [ ] Captura: historial de commits en `main`
- [ ] Explicación oral: diferencia entre `develop` y `main`
- [ ] Explicación oral: por qué PR #2 fue directo a `main` y cómo se realineó `develop`

---

*Documento generado como borrador Markdown — exportar a PDF para entrega formal.*
