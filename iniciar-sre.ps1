# ============================================================
#  SRE Valle del Sol - Script de inicio completo
#  Uso: click derecho -> "Ejecutar con PowerShell"
#       o desde terminal: .\iniciar-sre.ps1
# ============================================================

$JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$MAVEN    = "C:\Users\Skarlett\maven\apache-maven-3.9.6\bin\mvn.cmd"
$ROOT     = $PSScriptRoot

$env:JAVA_HOME = $JAVA_HOME
$env:PATH      = "$JAVA_HOME\bin;$($env:PATH)"

function Iniciar-Servicio {
    param(
        [string]$Nombre,
        [string]$Directorio,
        [string]$Color = "Cyan"
    )
    Write-Host ""
    Write-Host ">>> Iniciando $Nombre ..." -ForegroundColor $Color
    $dir = Join-Path $ROOT $Directorio
    Start-Process powershell -ArgumentList `
        "-NoExit", "-Command", `
        "`$env:JAVA_HOME='$JAVA_HOME'; `$env:PATH='$JAVA_HOME\bin;'+`$env:PATH; Set-Location '$dir'; & '$MAVEN' spring-boot:run" `
        -WindowStyle Normal
}

# 1. Eureka Server (debe ser el primero)
Iniciar-Servicio "Eureka Server" "infraestructuredomain\eurekaServer" "Yellow"
Write-Host "   Esperando 15s para que Eureka levante..." -ForegroundColor Gray
Start-Sleep -Seconds 15

# 2. Microservicios de negocio (en paralelo)
Iniciar-Servicio "Incidentes"   "businessdomain\incidentes"   "Green"
Iniciar-Servicio "Recursos"     "businessdomain\recursos"     "Green"
Iniciar-Servicio "Zonas Riesgo" "businessdomain\zonasriesgo"  "Green"
Write-Host "   Esperando 20s para que los microservicios se registren en Eureka..." -ForegroundColor Gray
Start-Sleep -Seconds 20

# 3. Infraestructura (API Gateway y BFF)
Iniciar-Servicio "API Gateway" "infraestructuredomain\apigateway" "Magenta"
Iniciar-Servicio "BFF"         "infraestructuredomain\bff"        "Magenta"
Write-Host "   Esperando 10s para que el Gateway y BFF levanten..." -ForegroundColor Gray
Start-Sleep -Seconds 10

# 4. Frontend
Write-Host ""
Write-Host ">>> Iniciando Frontend (npm run dev)..." -ForegroundColor Cyan
$frontendDir = Join-Path $ROOT "frontend\sre-ui"
Start-Process powershell -ArgumentList `
    "-NoExit", "-Command", `
    "Set-Location '$frontendDir'; npm run dev" `
    -WindowStyle Normal

# 5. Resumen
Write-Host ""
Write-Host "============================================" -ForegroundColor White
Write-Host "  Todos los servicios iniciados" -ForegroundColor White
Write-Host "============================================" -ForegroundColor White
Write-Host "  Eureka Dashboard : http://localhost:8761"  -ForegroundColor Cyan
Write-Host "  API Gateway      : http://localhost:8080"  -ForegroundColor Cyan
Write-Host "  BFF              : http://localhost:8085"  -ForegroundColor Cyan
Write-Host "  Frontend         : http://localhost:5173"  -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor White
Write-Host ""
Write-Host "  Espera ~1 minuto a que todo termine de cargar" -ForegroundColor Yellow
Write-Host "  luego abre http://localhost:5173 en el navegador" -ForegroundColor Yellow
Write-Host ""
