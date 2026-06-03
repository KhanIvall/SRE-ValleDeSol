# Documentacion formal EP2 — SRE Valle del Sol

Borradores listos para exportar a PDF e incluir en el ZIP de Blackboard.

## Archivos

| Archivo | Uso |
|---------|-----|
| `Analisis-Patrones-Arquetipos-SRE-EP2.md` | Exportar a PDF → entregable "Analisis de Patrones y Arquetipos" |
| `Plan-Branching-SRE-EP2.md` | Exportar a PDF → entregable "Plan de Branching" |
| `Guion-Defensa-Oral-EP2.md` | Guion para presentacion de 15 min (no obligatorio en ZIP) |
| `repositorios.txt` | Enlaces y descripcion del repo (incluir en ZIP) |

## Como exportar a PDF

### Opcion A — VS Code / Cursor
1. Instalar extension "Markdown PDF" o abrir en preview.
2. Exportar / imprimir a PDF.

### Opcion B — Word / Google Docs
1. Copiar el contenido del `.md`.
2. Pegar en Word o Google Docs.
2. Ajustar portada (logo Duoc, integrantes, fecha).
3. Archivo → Descargar como PDF.

### Opcion C — Pandoc (linea de comandos)
```bash
pandoc Analisis-Patrones-Arquetipos-SRE-EP2.md -o Analisis-Patrones-Arquetipos-SRE-EP2.pdf
pandoc Plan-Branching-SRE-EP2.md -o Plan-Branching-SRE-EP2.pdf
```

## Antes de entregar

- [ ] Completar nombres, fechas y capturas de pantalla indicadas con `[INSERTAR CAPTURA]`
- [ ] Mergear PR de readmes-tests si aplica
- [ ] Tag `v1.0.0-ep2` en rama `main`
- [ ] Armar ZIP con codigo + docs PDF + repositorios.txt
