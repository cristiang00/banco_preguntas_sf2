# =========================================================================
# Script de Inicio: Banco de Preguntas Saber Pro (Sistema Distribuido)
# =========================================================================
param(
    [switch]$Docker,
    [switch]$FrontendOnly,
    [switch]$NoFrontend,
    [switch]$Console,
    [switch]$Build,
    [switch]$Stop
)

$ErrorActionPreference = "Stop"
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

function Stop-RunningServices {
    Write-Host "Deteniendo microservicios activos en puertos 8080-8087 (y OIDC local en 8089 si aplica)..." -ForegroundColor Yellow
    $ports = 8080..8087
    foreach ($port in $ports) {
        try {
            $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
            if ($conns) {
                $pids = $conns.OwningProcess | Select-Object -Unique
                foreach ($procId in $pids) {
                    if ($procId -gt 0 -and $procId -ne $PID) {
                        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
                        Write-Host " -> Proceso detenido (PID: $procId, Puerto: $port)" -ForegroundColor Green
                    }
                }
            }
        } catch {}
    }
    # Detener servidor OIDC local Java en 8089 (sin afectar Keycloak en Docker)
    try {
        $conns8089 = Get-NetTCPConnection -LocalPort 8089 -State Listen -ErrorAction SilentlyContinue
        foreach ($procId in ($conns8089.OwningProcess | Select-Object -Unique)) {
            $p = Get-Process -Id $procId -ErrorAction SilentlyContinue
            if ($p -and $p.ProcessName -in @("java", "javaw", "node")) {
                Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
                Write-Host " -> Servidor OIDC local detenido (PID: $procId, Puerto: 8089)" -ForegroundColor Green
            }
        }
    } catch {}
}

if ($Stop) {
    Stop-RunningServices
    if ($Docker) {
        docker compose down
    }
    Write-Host "Microservicios detenidos correctamente." -ForegroundColor Green
    exit 0
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Iniciando Banco de Preguntas Saber Pro 2.0 (Microservicios)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

if ($Docker) {
    Write-Host "[1/2] Levantando contenedores con Docker Compose..." -ForegroundColor Yellow
    docker compose up --build -d
    Write-Host "[2/2] Esperando inicializacion de servicios (15s)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    Write-Host "Servicios disponibles en Docker:" -ForegroundColor Green
    Write-Host " - API Gateway: http://localhost:8080" -ForegroundColor White
    Write-Host " - Swagger UI:  http://localhost:8080/swagger-ui.html" -ForegroundColor White
    Write-Host " - RabbitMQ UI: http://localhost:15672 (guest/guest)" -ForegroundColor White
    if (-not $NoFrontend) {
        Write-Host "Lanzando Cliente Frontend Desktop Swing..." -ForegroundColor Cyan
        & java -jar "$PSScriptRoot\frontend\target\frontend-2.0.0-SNAPSHOT.jar"
    }
    exit 0
}

$frontendJar = "$PSScriptRoot\frontend\target\frontend-2.0.0-SNAPSHOT.jar"

if ($FrontendOnly) {
    if (-not (Test-Path $frontendJar)) {
        Write-Host "Compilando frontend JAR..." -ForegroundColor Yellow
        & "$PSScriptRoot\mvnw.cmd" package -pl frontend -am -DskipTests
    }
    Write-Host "Lanzando Cliente Frontend Desktop Swing..." -ForegroundColor Green
    & java -jar "$frontendJar"
    exit 0
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

$needsBuild = $Build
if (-not $needsBuild) {
    foreach ($s in $services) {
        if (-not (Test-Path $s.Jar)) {
            $needsBuild = $true
            break
        }
    }
    if (-not (Test-Path $frontendJar)) {
        $needsBuild = $true
    }
}

if ($needsBuild) {
    Write-Host "[1/3] Compilando y empaquetando modulos con Maven..." -ForegroundColor Yellow
    & "$PSScriptRoot\mvnw.cmd" package -DskipTests
} else {
    Write-Host "[1/3] Artefactos JAR verificados en target/." -ForegroundColor Green
}

$logsDir = Join-Path $PSScriptRoot "logs"
if (-not (Test-Path $logsDir)) {
    New-Item -ItemType Directory -Path $logsDir -Force | Out-Null
}

Write-Host "[2/3] Verificando autenticacion OIDC/Keycloak (:8089) y levantando microservicios..." -ForegroundColor Yellow

$oidcScript = Join-Path $PSScriptRoot "scripts\LocalOidcServer.java"
if (Test-PortOpen "localhost" 8089) {
    Write-Host " [OK] Keycloak / OIDC ya activo en puerto 8089 (se usara esa instancia)." -ForegroundColor Green
} elseif (Test-Path $oidcScript) {
    Write-Host " [->] No hay Keycloak en :8089; iniciando servidor OIDC/JWKS local (scripts\LocalOidcServer.java)..." -ForegroundColor Cyan
    $oidcOut = Join-Path $logsDir "local-oidc.log"
    $oidcErr = Join-Path $logsDir "local-oidc-err.log"
    if ($Console) {
        Start-Process cmd.exe -ArgumentList @("/k", "title LOCAL-OIDC [8089] & java `"$oidcScript`"") -WorkingDirectory $PSScriptRoot
    } else {
        Start-Process -FilePath "java" -ArgumentList @($oidcScript) -WorkingDirectory $PSScriptRoot -RedirectStandardOutput $oidcOut -RedirectStandardError $oidcErr
    }
    $oidcSw = [System.Diagnostics.Stopwatch]::StartNew()
    while ($oidcSw.Elapsed.TotalSeconds -lt 15 -and -not (Test-PortOpen "localhost" 8089)) {
        Start-Sleep -Milliseconds 500
    }
    if (Test-PortOpen "localhost" 8089) {
        Write-Host " [OK] Servidor OIDC/JWKS local activo en puerto 8089." -ForegroundColor Green
    } else {
        Write-Host " [WARN] El servidor OIDC local aun esta iniciando en :8089 (revisa logs\local-oidc-err.log)." -ForegroundColor Yellow
    }
} else {
    Write-Host " [WARN] Puerto 8089 no disponible y falta scripts\LocalOidcServer.java. El login requiere Keycloak en :8089." -ForegroundColor Red
}

foreach ($s in $services) {
    $isOpen = Test-PortOpen "localhost" $s.Port
    if ($isOpen) {
        Write-Host " [OK] $($s.Name) ya activo en puerto $($s.Port)." -ForegroundColor DarkGray
    } else {
        Write-Host " [->] Iniciando $($s.Name) en puerto $($s.Port)..." -ForegroundColor White
        $outLog = Join-Path $logsDir "$($s.Name).log"
        $errLog = Join-Path $logsDir "$($s.Name)-err.log"
        if ($Console) {
            Start-Process cmd.exe -ArgumentList @("/k", "title $($s.Name) [$($s.Port)] & java -Xmx256m -jar `"$($s.Jar)`"") -WorkingDirectory $PSScriptRoot
        } else {
            Start-Process -FilePath "java" -ArgumentList @("-Xmx256m", "-jar", $s.Jar) -WorkingDirectory $PSScriptRoot -RedirectStandardOutput $outLog -RedirectStandardError $errLog
        }
    }
}

Write-Host "[3/3] Verificando disponibilidad de los 8 microservicios + OIDC (logs en $logsDir)..." -ForegroundColor Yellow
$timeoutSeconds = 60
$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$allReady = $false

while ($stopwatch.Elapsed.TotalSeconds -lt $timeoutSeconds) {
    $allReady = (Test-PortOpen "localhost" 8089)
    if ($allReady) {
        foreach ($s in $services) {
            if (-not (Test-PortOpen "localhost" $s.Port)) {
                $allReady = $false
                break
            }
        }
    }
    if ($allReady) { break }
    Write-Host "." -NoNewline -ForegroundColor Yellow
    Start-Sleep -Seconds 2
}
Write-Host ""

if ($allReady) {
    Write-Host "[OK] Todos los 8 microservicios + OIDC (:8089) activos y listos." -ForegroundColor Green
} else {
    Write-Host "[INFO] Algunos servicios siguen iniciando. Consulta la carpeta 'logs' para mas detalle." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Microservicios disponibles:" -ForegroundColor Green
Write-Host " - API Gateway:          http://localhost:8080" -ForegroundColor White
Write-Host "   -> OpenAPI / Swagger: http://localhost:8080/swagger-ui.html" -ForegroundColor Yellow
Write-Host " - Users Service:        http://localhost:8081" -ForegroundColor White
Write-Host " - Questions Service:    http://localhost:8082" -ForegroundColor White
Write-Host " - Validation Service:   http://localhost:8083" -ForegroundColor White
Write-Host " - Lifecycle Service:    http://localhost:8084" -ForegroundColor White
Write-Host " - Review Service:       http://localhost:8085" -ForegroundColor White
Write-Host " - Notification Service: http://localhost:8086" -ForegroundColor White
Write-Host " - Audit Service:        http://localhost:8087" -ForegroundColor White
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Para detener todos los servicios ejecute: .\stop-all.ps1" -ForegroundColor DarkGray
Write-Host ""

if (-not $NoFrontend) {
    Write-Host "Iniciando cliente de escritorio Java Swing..." -ForegroundColor Cyan
    & java -jar "$frontendJar"
}
