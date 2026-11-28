# =========================================================================
# Daemon runner para Microservicios del Banco de Preguntas Saber Pro
# Soporta tanto Keycloak real en Docker (:8089) como modo local sin Docker
# =========================================================================
param(
    [switch]$NoFrontend
)
$ErrorActionPreference = "Continue"
$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $PSScriptRoot

function Test-PortOpen([string]$HostName, [int]$Port) {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $async = $tcp.BeginConnect($HostName, $Port, $null, $null)
        $wait = $async.AsyncWaitHandle.WaitOne(400, $false)
        if (-not $wait) {
            $tcp.Close()
            return $false
        }
        $tcp.EndConnect($async)
        $tcp.Close()
        return $true
    } catch {
        return $false
    }
}

$services = @(
    @{ Name = "users-service";        Port = 8081; Jar = "$PSScriptRoot\services\users-service\target\users-service-2.0.0-SNAPSHOT.jar" },
    @{ Name = "questions-service";    Port = 8082; Jar = "$PSScriptRoot\services\questions-service\target\questions-service-2.0.0-SNAPSHOT.jar" },
    @{ Name = "validation-service";   Port = 8083; Jar = "$PSScriptRoot\services\validation-service\target\validation-service-2.0.0-SNAPSHOT.jar" },
    @{ Name = "lifecycle-service";    Port = 8084; Jar = "$PSScriptRoot\services\lifecycle-service\target\lifecycle-service-2.0.0-SNAPSHOT.jar" },
    @{ Name = "review-service";       Port = 8085; Jar = "$PSScriptRoot\services\review-service\target\review-service-2.0.0-SNAPSHOT.jar" },
    @{ Name = "notification-service"; Port = 8086; Jar = "$PSScriptRoot\services\notification-service\target\notification-service-2.0.0-SNAPSHOT.jar" },
    @{ Name = "audit-service";        Port = 8087; Jar = "$PSScriptRoot\services\audit-service\target\audit-service-2.0.0-SNAPSHOT.jar" },
    @{ Name = "api-gateway";          Port = 8080; Jar = "$PSScriptRoot\api-gateway\target\api-gateway-2.0.0-SNAPSHOT.jar" }
)
$frontendJar = "$PSScriptRoot\frontend\target\frontend-2.0.0-SNAPSHOT.jar"

# Compilar automaticamente si faltan los JARs (por ejemplo en un git clone limpio)
$needsBuild = -not (Test-Path $frontendJar)
if (-not $needsBuild) {
    foreach ($s in $services) {
        if (-not (Test-Path $s.Jar)) {
            $needsBuild = $true
            break
        }
    }
}
if ($needsBuild) {
    Write-Host "[1/3] Faltan artefactos en target/. Compilando proyecto con Maven..." -ForegroundColor Yellow
    & "$PSScriptRoot\mvnw.cmd" package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Fallo la compilacion Maven." -ForegroundColor Red
        exit 1
    }
}

$logsDir = Join-Path $PSScriptRoot "logs"
if (-not (Test-Path $logsDir)) {
    New-Item -ItemType Directory -Path $logsDir -Force | Out-Null
}

$childProcesses = [System.Collections.Generic.List[System.Diagnostics.Process]]::new()

# Asegurar OIDC/Keycloak en el puerto 8089 (usa Keycloak existente si ya corre en Docker)
$oidcScript = Join-Path $PSScriptRoot "scripts\LocalOidcServer.java"
if (Test-PortOpen "localhost" 8089) {
    Write-Host " [OK] Keycloak / OIDC ya activo en puerto 8089." -ForegroundColor Green
} elseif (Test-Path $oidcScript) {
    Write-Host " [->] Iniciando servidor OIDC/JWKS local en puerto 8089..." -ForegroundColor Cyan
    $oidcOut = Join-Path $logsDir "local-oidc.log"
    $oidcErr = Join-Path $logsDir "local-oidc-err.log"
    $oidcProc = Start-Process -FilePath "java" -ArgumentList @($oidcScript) -WorkingDirectory $PSScriptRoot -RedirectStandardOutput $oidcOut -RedirectStandardError $oidcErr -PassThru
    if ($oidcProc) { $childProcesses.Add($oidcProc) }
    $oidcSw = [System.Diagnostics.Stopwatch]::StartNew()
    while ($oidcSw.Elapsed.TotalSeconds -lt 15 -and -not (Test-PortOpen "localhost" 8089)) {
        Start-Sleep -Milliseconds 500
    }
}

Write-Host "Iniciando microservicios..." -ForegroundColor Yellow
foreach ($s in $services) {
    if (Test-PortOpen "localhost" $s.Port) {
        Write-Host " [OK] $($s.Name) ya esta activo en puerto $($s.Port)." -ForegroundColor Green
    } else {
        Write-Host " [->] Levantando $($s.Name) en puerto $($s.Port)..." -ForegroundColor White
        $outLog = Join-Path $logsDir "$($s.Name).log"
        $errLog = Join-Path $logsDir "$($s.Name)-err.log"
        $proc = Start-Process -FilePath "java" -ArgumentList @("-Xmx256m", "-jar", $s.Jar) -WorkingDirectory $PSScriptRoot -RedirectStandardOutput $outLog -RedirectStandardError $errLog -PassThru
        if ($proc) {
            $childProcesses.Add($proc)
        }
    }
}

Write-Host "Esperando estabilizacion de puertos (hasta 60s)..." -ForegroundColor Cyan
$timeout = 60
$sw = [System.Diagnostics.Stopwatch]::StartNew()
while ($sw.Elapsed.TotalSeconds -lt $timeout) {
    $allReady = (Test-PortOpen "localhost" 8089)
    if ($allReady) {
        foreach ($s in $services) {
            if (-not (Test-PortOpen "localhost" $s.Port)) {
                $allReady = $false
                break
            }
        }
    }
    if ($allReady) {
        Write-Host "Todos los microservicios + OIDC (:8089) estan respondiendo." -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
}

# Lanzar el cliente Desktop Frontend Swing
if (-not $NoFrontend -and (Test-Path $frontendJar)) {
    Write-Host "Lanzando Frontend Desktop Swing..." -ForegroundColor Green
    $feProc = Start-Process -FilePath "javaw" -ArgumentList @("-jar", $frontendJar) -WorkingDirectory $PSScriptRoot -PassThru
    if ($feProc) {
        $childProcesses.Add($feProc)
    }
}

Write-Host "Servicios y aplicacion activos. Presiona Ctrl+C o ejecuta .\stop-all.ps1 para detener." -ForegroundColor Green
try {
    while ($true) {
        Start-Sleep -Seconds 30
    }
} finally {
    foreach ($p in $childProcesses) {
        if ($p -and -not $p.HasExited) {
            Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
        }
    }
}
