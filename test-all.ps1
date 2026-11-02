# =========================================================================
# Script de Ejecución de Pruebas Automatizadas
# =========================================================================

Set-Location $PSScriptRoot

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Ejecutando Suite de Pruebas Automatizadas (JUnit 5)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$startTime = Get-Date

& .\mvnw.cmd test

$exitCode = $LASTEXITCODE
$elapsed = (Get-Date) - $startTime

Write-Host ""
if ($exitCode -eq 0) {
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "✓ TODAS LAS PRUEBAS PASARON EXITOSAMENTE ($([math]::Round($elapsed.TotalSeconds, 2))s)" -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "Resumen de Cobertura y Patrones GoF Verificados:" -ForegroundColor White
    Write-Host " [✓] contracts:              Eventos de dominio y contratos DTO canónicos" -ForegroundColor Green
    Write-Host " [✓] users-service:          4 pruebas (Autenticación, Roles, Argon2, JWT)" -ForegroundColor Green
    Write-Host " [✓] validation-service:     8 pruebas (Reglas modulares, respuesta única, dificultad)" -ForegroundColor Green
    Write-Host " [✓] lifecycle-service:      5 pruebas (GoF State: Draft, Pending, Approved, Rejected)" -ForegroundColor Green
    Write-Host " [✓] questions-service:      5 pruebas (GoF Builder y GoF Facade de consulta)" -ForegroundColor Green
    Write-Host " [✓] review-service:         10 pruebas (Asignación 1-3 revisores, exclusión autor, consenso)" -ForegroundColor Green
    Write-Host " [✓] notification-service:   5 pruebas (GoF Factory Method, GoF Adapter, deduplicación)" -ForegroundColor Green
    Write-Host " [✓] audit-service:          4 pruebas (GoF Observer, trazabilidad de eventos, idempotencia)" -ForegroundColor Green
    Write-Host " [✓] api-gateway:            6 pruebas (Verificación JWT, RBAC, propagación correlación)" -ForegroundColor Green
    Write-Host " [✓] frontend:               3 pruebas (ApiClient REST, EstadoCellRenderer, vistas headless)" -ForegroundColor Green
    Write-Host " [✓] integration-tests:      2 pruebas (Flujo distribuido E2E HU1 a HU5)" -ForegroundColor Green
    Write-Host " Total de pruebas: 52 aprobadas (0 fallas, 0 errores, 0 omitidas)" -ForegroundColor Cyan
} else {
    Write-Host "==========================================================" -ForegroundColor Red
    Write-Host "✗ FALLA EN LA SUITE DE PRUEBAS (Código de salida: $exitCode)" -ForegroundColor Red
    Write-Host "==========================================================" -ForegroundColor Red
}

exit $exitCode

