# =========================================================================
# Daemon runner para Microservicios del Banco de Preguntas Saber Pro
# =========================================================================
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

$logsDir = Join-Path $PSScriptRoot "logs"
if (-not (Test-Path $logsDir)) {
    New-Item -ItemType Directory -Path $logsDir -Force | Out-Null
}

$childProcesses = [System.Collections.Generic.List[System.Diagnostics.Process]]::new()

Write-Host "Iniciando microservicios..." -ForegroundColor Yellow
foreach ($s in $services) {
    if (Test-PortOpen "localhost" $s.Port) {
        Write-Host " [OK] $($s.Name) ya esta activo en puerto $($s.Port)." -ForegroundColor Green
    } else {
        Write-Host " [->] Levantando $($s.Name) en puerto $($s.Port)..." -ForegroundColor White
        $outLog = Join-Path $logsDir "$($s.Name).log"
        $errLog = Join-Path $logsDir "$($s.Name)-err.log"
        $proc = Start-Process -FilePath "java" -ArgumentList @("-jar", $s.Jar) -WorkingDirectory $PSScriptRoot -RedirectStandardOutput $outLog -RedirectStandardError $errLog -PassThru
        if ($proc) {
            $childProcesses.Add($proc)
        }
    }
}

Write-Host "Esperando estabilizacion de puertos (30s)..." -ForegroundColor Cyan
$timeout = 30
$sw = [System.Diagnostics.Stopwatch]::StartNew()
while ($sw.Elapsed.TotalSeconds -lt $timeout) {
    $allReady = $true
    foreach ($s in $services) {
        if (-not (Test-PortOpen "localhost" $s.Port)) {
            $allReady = $false
            break
        }
    }
    if ($allReady) {
        Write-Host "Todos los microservicios estan respondiendo en sus puertos." -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
}

# Lanzar el cliente Desktop Frontend Swing
$frontendJar = "$PSScriptRoot\frontend\target\frontend-2.0.0-SNAPSHOT.jar"
if (Test-Path $frontendJar) {
    Write-Host "Lanzando Frontend Desktop Swing..." -ForegroundColor Green
    $feProc = Start-Process -FilePath "javaw" -ArgumentList @("-jar", $frontendJar) -WorkingDirectory $PSScriptRoot -PassThru
    if ($feProc) {
        $childProcesses.Add($feProc)
    }
}

Write-Host "Servicios y aplicacion activos. Manteniendo proceso vivo..." -ForegroundColor Green
while ($true) {
    Start-Sleep -Seconds 30
}
