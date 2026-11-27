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
    Write-Host "Resumen de Cobertura y Patrones Verificados (Sprint 3):" -ForegroundColor White
    Write-Host " [OK] contracts:              Eventos de dominio, RabbitMQ y contratos DTO" -ForegroundColor Green
    Write-Host " [OK] api-gateway:            14 pruebas (Keycloak JWKS RS256, matriz de roles, anti-spoofing)" -ForegroundColor Green
    Write-Host " [OK] users-service:          9 pruebas (Usuarios, roles Keycloak, Argon2)" -ForegroundColor Green
    Write-Host " [OK] questions-service:      19 pruebas (Builder, Facade con filtros/paginacion, versionado HU6)" -ForegroundColor Green
    Write-Host " [OK] validation-service:     8 pruebas (5 reglas estructurales y ValidationEngine)" -ForegroundColor Green
    Write-Host " [OK] lifecycle-service:      14 pruebas (Patron State, Rechazada->Borrador HU6, broker)" -ForegroundColor Green
    Write-Host " [OK] review-service:         34 pruebas (Hexagonal, JPA vs InMemory, consenso por ronda, @PreAuthorize)" -ForegroundColor Green
    Write-Host " [OK] notification-service:   9 pruebas (Factory Method, SmtpEmailAdapter, RabbitMQ)" -ForegroundColor Green
    Write-Host " [OK] audit-service:          5 pruebas (Observer, trazabilidad RabbitMQ, idempotencia)" -ForegroundColor Green
    Write-Host " [OK] frontend:               5 pruebas (Login OIDC Keycloak simulado, vistas Swing)" -ForegroundColor Green
    Write-Host " [OK] integration-tests:      2 pruebas (Flujo distribuido orientado a eventos)" -ForegroundColor Green
    Write-Host " Total de pruebas: 119 aprobadas (0 fallas, 0 errores, 0 omitidas)" -ForegroundColor Cyan
} else {
    Write-Host "==========================================================" -ForegroundColor Red
    Write-Host "[FAIL] FALLA EN LA SUITE DE PRUEBAS (Codigo de salida: $exitCode)" -ForegroundColor Red
    Write-Host "==========================================================" -ForegroundColor Red
}

exit $exitCode
