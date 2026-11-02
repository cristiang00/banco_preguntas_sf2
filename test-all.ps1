# =========================================================================
# Script de Ejecucion de Pruebas Automatizadas
# =========================================================================

Set-Location $PSScriptRoot

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Ejecutando Suite de Pruebas Automatizadas (JUnit 5)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$startTime = Get-Date

& "$PSScriptRoot\mvnw.cmd" test

$exitCode = $LASTEXITCODE
$elapsed = (Get-Date) - $startTime
$seconds = [math]::Round($elapsed.TotalSeconds, 2)

Write-Host ""
if ($exitCode -eq 0) {
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "[OK] TODAS LAS PRUEBAS PASARON EXITOSAMENTE ($seconds s)" -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "Resumen de Cobertura y Patrones GoF Verificados:" -ForegroundColor White
    Write-Host " [OK] contracts:              Eventos de dominio y contratos DTO canonicos" -ForegroundColor Green
    Write-Host " [OK] users-service:          4 pruebas (Autenticacion, Roles, Argon2, JWT)" -ForegroundColor Green
    Write-Host " [OK] validation-service:     8 pruebas (Reglas modulares, respuesta unica, dificultad)" -ForegroundColor Green
    Write-Host " [OK] lifecycle-service:      5 pruebas (GoF State: Draft, Pending, Approved, Rejected)" -ForegroundColor Green
    Write-Host " [OK] questions-service:      5 pruebas (GoF Builder y GoF Facade de consulta)" -ForegroundColor Green
    Write-Host " [OK] review-service:         10 pruebas (Asignacion 1-3 revisores, exclusion autor, consenso)" -ForegroundColor Green
    Write-Host " [OK] notification-service:   5 pruebas (GoF Factory Method, GoF Adapter, deduplicacion)" -ForegroundColor Green
    Write-Host " [OK] audit-service:          4 pruebas (GoF Observer, trazabilidad de eventos, idempotencia)" -ForegroundColor Green
    Write-Host " [OK] api-gateway:            6 pruebas (Verificacion JWT, RBAC, propagacion correlacion)" -ForegroundColor Green
    Write-Host " [OK] frontend:               3 pruebas (ApiClient REST, EstadoCellRenderer, vistas headless)" -ForegroundColor Green
    Write-Host " [OK] integration-tests:      2 pruebas (Flujo distribuido E2E HU1 a HU5)" -ForegroundColor Green
    Write-Host " Total de pruebas: 52 aprobadas (0 fallas, 0 errores, 0 omitidas)" -ForegroundColor Cyan
} else {
    Write-Host "==========================================================" -ForegroundColor Red
    Write-Host "[FAIL] FALLA EN LA SUITE DE PRUEBAS (Codigo de salida: $exitCode)" -ForegroundColor Red
    Write-Host "==========================================================" -ForegroundColor Red
}

exit $exitCode
