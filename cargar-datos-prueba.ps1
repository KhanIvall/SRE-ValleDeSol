# ============================================================
#  SRE Valle del Sol - Carga de datos de prueba
#  Uso: .\cargar-datos-prueba.ps1
#  Requiere que todos los servicios esten corriendo
# ============================================================

$BASE = "http://localhost:8080"

function Post-Incidente {
    param($tipo, $descripcion, $lat, $lon)
    $body = @{
        tipo        = $tipo
        descripcion = $descripcion
        latitud     = $lat
        longitud    = $lon
    } | ConvertTo-Json

    try {
        $resp = Invoke-RestMethod -Uri "$BASE/incidentes" -Method POST -ContentType "application/json" -Body $body
        Write-Host "  [OK] Creado ID=$($resp.id) - $descripcion" -ForegroundColor Green
        return $resp.id
    } catch {
        Write-Host "  [ERROR] $descripcion - $_" -ForegroundColor Red
        return $null
    }
}

function Put-Estado {
    param($id, $estado)
    try {
        $body = @{ estado = $estado } | ConvertTo-Json
        Invoke-RestMethod -Uri "$BASE/incidentes/$id/estado" -Method PUT -ContentType "application/json" -Body $body | Out-Null
        Write-Host "  [OK] Incidente $id -> $estado" -ForegroundColor Cyan
    } catch {
        Write-Host "  [ERROR] Cambiar estado ID=$id - $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Yellow
Write-Host "  Cargando incidentes de prueba..." -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow

# --- INCENDIOS FORESTALES ---
Write-Host ""
Write-Host "Incendios forestales:" -ForegroundColor White

$id1 = Post-Incidente "INCENDIO_FORESTAL" "Foco activo en Cerro San Cristobal" -33.4372 -70.6506
$id2 = Post-Incidente "INCENDIO_FORESTAL" "Incendio en Parque Metropolitano sector norte" -33.4050 -70.6200
$id3 = Post-Incidente "INCENDIO_FORESTAL" "Foco en quebrada Macul" -33.5100 -70.5600
$id4 = Post-Incidente "INCENDIO_FORESTAL" "Incendio forestal Lo Barnechea" -33.3500 -70.5200

# --- DERRUMBES ---
Write-Host ""
Write-Host "Derrumbes:" -ForegroundColor White

$id5 = Post-Incidente "DERRUMBE" "Derrumbe en ruta G-21 sector cordillera" -33.5800 -70.4800
$id6 = Post-Incidente "DERRUMBE" "Deslizamiento de tierra en Puente Alto" -33.6100 -70.5700

# --- INUNDACIONES ---
Write-Host ""
Write-Host "Inundaciones:" -ForegroundColor White

$id7 = Post-Incidente "INUNDACION" "Inundacion en zanjón de la Aguada" -33.4700 -70.6400
$id8 = Post-Incidente "INUNDACION" "Desborde rio Mapocho sector Pudahuel" -33.4300 -70.7800

# --- CAMBIAR ESTADOS ---
Write-Host ""
Write-Host "Cambiando estados (EN_PROGRESO = aparece en mapa con cono):" -ForegroundColor White

Start-Sleep -Seconds 1

if ($id1) { Put-Estado $id1 "EN_PROGRESO" }
if ($id2) { Put-Estado $id2 "EN_PROGRESO" }
if ($id3) { Put-Estado $id3 "EN_PROGRESO" }
if ($id5) { Put-Estado $id5 "EN_PROGRESO" }
if ($id7) { Put-Estado $id7 "EN_PROGRESO" }

# Dejar algunos en REPORTADO y uno CONTROLADO
if ($id4) { Put-Estado $id4 "EN_PROGRESO" ; Start-Sleep 1 ; Put-Estado $id4 "CONTROLADO" }

# --- RESUMEN ---
Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "  Datos cargados exitosamente" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host "  EN_PROGRESO : 5 incidentes (con cono de propagacion)" -ForegroundColor Cyan
Write-Host "  REPORTADO   : 2 incidentes" -ForegroundColor Yellow
Write-Host "  CONTROLADO  : 1 incidente" -ForegroundColor Gray
Write-Host ""
Write-Host "  Abre http://localhost:5173 para verlos en el mapa" -ForegroundColor White
Write-Host ""
