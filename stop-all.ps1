# =========================================================================
# Script de Parada: Banco de Preguntas Saber Pro (Sistema Distribuido)
# =========================================================================
param(
    [switch]$Docker
)

$ErrorActionPreference = "SilentlyContinue"
Set-Location $PSScriptRoot

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Deteniendo Microservicios del Banco de Preguntas Saber Pro" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

if ($Docker) {
    Write-Host "Deteniendo contenedores Docker Compose..." -ForegroundColor Yellow
    docker compose down
    Write-Host "Contenedores Docker detenidos." -ForegroundColor Green
    exit 0
}

$ports = 8080..8087
$stoppedAny = $false

foreach ($port in $ports) {
    try {
        $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($conns) {
            $pids = $conns.OwningProcess | Select-Object -Unique
            foreach ($procId in $pids) {
                if ($procId -gt 0 -and $procId -ne $PID) {
                    Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
                    Write-Host " -> Proceso PID $procId (puerto $port) detenido." -ForegroundColor Green
                    $stoppedAny = $true
                }
            }
        }
    } catch {}
}

# Detener servidor OIDC local Java/Node en 8089 (sin afectar Keycloak en Docker)
try {
    $conns8089 = Get-NetTCPConnection -LocalPort 8089 -State Listen -ErrorAction SilentlyContinue
    foreach ($procId in ($conns8089.OwningProcess | Select-Object -Unique)) {
        $p = Get-Process -Id $procId -ErrorAction SilentlyContinue
        if ($p -and $p.ProcessName -in @("java", "javaw", "node")) {
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
            Write-Host " -> Servidor OIDC local PID $procId (puerto 8089) detenido." -ForegroundColor Green
            $stoppedAny = $true
        }
    }
} catch {}

if (-not $stoppedAny) {
    Write-Host "No se encontraron microservicios activos en los puertos 8080-8087 / 8089." -ForegroundColor DarkGray
} else {
    Write-Host "Todos los microservicios han sido detenidos exitosamente." -ForegroundColor Green
}
