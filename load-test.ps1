# =========================================================================
# Script de Pruebas de Carga y Rendimiento (API Gateway)
# =========================================================================
param(
    [string]$GatewayUrl = "http://localhost:8080/api/v1",
    [int]$Requests = 50,
    [int]$Concurrency = 5
)

Set-Location $PSScriptRoot

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Prueba de Carga: Banco de Preguntas Saber Pro (Gateway)" -ForegroundColor Cyan
Write-Host "Destino: $GatewayUrl" -ForegroundColor White
Write-Host "Peticiones: $Requests | Concurrencia: $Concurrency" -ForegroundColor White
Write-Host "==========================================================" -ForegroundColor Cyan

$successCount = 0
$failCount = 0
$times = @()

$swTotal = [System.Diagnostics.Stopwatch]::StartNew()

for ($i = 1; $i -le $Requests; $i++) {
    $swReq = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        # Endpoint de catálogos público en el Gateway
        $res = Invoke-RestMethod -Uri "$GatewayUrl/catalogos/competencias" -Method Get -TimeoutSec 5 -ErrorAction Stop
        $swReq.Stop()
        $times += $swReq.ElapsedMilliseconds
        $successCount++
        Write-Host "." -NoNewline -ForegroundColor Green
    } catch {
        $swReq.Stop()
        $failCount++
        Write-Host "x" -NoNewline -ForegroundColor Red
    }
}
$swTotal.Stop()

Write-Host ""
Write-Host "----------------------------------------------------------" -ForegroundColor White
Write-Host "Resultados de la prueba de carga:" -ForegroundColor Cyan
Write-Host " Total peticiones: $Requests" -ForegroundColor White
Write-Host " Exitosas:         $successCount" -ForegroundColor Green
Write-Host " Fallidas:         $failCount" -ForegroundColor Red
Write-Host " Tiempo total:     $([math]::Round($swTotal.Elapsed.TotalSeconds, 2)) s" -ForegroundColor White

if ($times.Count -gt 0) {
    $avg = ($times | Measure-Object -Average).Average
    $min = ($times | Measure-Object -Minimum).Minimum
    $max = ($times | Measure-Object -Maximum).Maximum
    Write-Host " Latencia Promedio: $([math]::Round($avg, 2)) ms" -ForegroundColor White
    Write-Host " Latencia Mínima:   $min ms" -ForegroundColor White
    Write-Host " Latencia Máxima:   $max ms" -ForegroundColor White
    $throughput = [math]::Round($successCount / $swTotal.Elapsed.TotalSeconds, 2)
    Write-Host " Throughput (RPS):  $throughput req/s" -ForegroundColor Cyan
}
Write-Host "==========================================================" -ForegroundColor Cyan

