# =========================================================================
# Script de Inicio: Banco de Preguntas Saber Pro (Sistema Distribuido)
# =========================================================================
param(
    [switch]$Docker,
    [switch]$FrontendOnly
)

$ErrorActionPreference = "Stop"
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Iniciando Banco de Preguntas Saber Pro 2.0 (Microservicios)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

if ($Docker) {
    Write-Host "[1/2] Levantando contenedores con Docker Compose..." -ForegroundColor Yellow
    docker compose up --build -d
    Write-Host "[2/2] Esperando inicialización de servicios (15s)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    Write-Host "Servicios disponibles en Docker:" -ForegroundColor Green
    Write-Host " - API Gateway: http://localhost:8080" -ForegroundColor White
    Write-Host " - Swagger UI:  http://localhost:8080/swagger-ui.html" -ForegroundColor White
    Write-Host " - RabbitMQ UI: http://localhost:15672 (guest/guest)" -ForegroundColor White
    exit 0
}

if ($FrontendOnly) {
    Write-Host "Lanzando Cliente Frontend Desktop Swing..." -ForegroundColor Green
    & .\mvnw.cmd exec:java -pl frontend -Dexec.mainClass="co.edu.unicauca.bancopreguntas.frontend.FrontendMain"
    exit 0
}

Write-Host "[1/3] Compilando proyecto Maven multi-módulo..." -ForegroundColor Yellow
& .\mvnw.cmd package -DskipTests

Write-Host "[2/3] Levantando microservicios en segundo plano..." -ForegroundColor Yellow

$services = @(
    @{ Name = "users-service"; Port = 8081; Dir = "services/users-service" },
    @{ Name = "questions-service"; Port = 8082; Dir = "services/questions-service" },
    @{ Name = "validation-service"; Port = 8083; Dir = "services/validation-service" },
    @{ Name = "lifecycle-service"; Port = 8084; Dir = "services/lifecycle-service" },
    @{ Name = "review-service"; Port = 8085; Dir = "services/review-service" },
    @{ Name = "notification-service"; Port = 8086; Dir = "services/notification-service" },
    @{ Name = "audit-service"; Port = 8087; Dir = "services/audit-service" },
    @{ Name = "api-gateway"; Port = 8080; Dir = "api-gateway" }
)

foreach ($s in $services) {
    Write-Host " -> Iniciando $($s.Name) en puerto $($s.Port)..." -ForegroundColor White
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$($s.Dir)'; ..\..\mvnw.cmd spring-boot:run" -WindowStyle Minimized
}

Write-Host "[3/3] Esperando estabilización de puertos (12s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 12

Write-Host "Todos los microservicios han sido lanzados:" -ForegroundColor Green
Write-Host " - API Gateway:          http://localhost:8080 (Swagger: /swagger-ui.html)" -ForegroundColor Green
Write-Host " - Users Service:        http://localhost:8081" -ForegroundColor Green
Write-Host " - Questions Service:    http://localhost:8082" -ForegroundColor Green
Write-Host " - Validation Service:   http://localhost:8083" -ForegroundColor Green
Write-Host " - Lifecycle Service:    http://localhost:8084" -ForegroundColor Green
Write-Host " - Review Service:       http://localhost:8085" -ForegroundColor Green
Write-Host " - Notification Service: http://localhost:8086" -ForegroundColor Green
Write-Host " - Audit Service:        http://localhost:8087" -ForegroundColor Green

Write-Host "Iniciando cliente de escritorio Swing..." -ForegroundColor Cyan
& .\mvnw.cmd exec:java -pl frontend -Dexec.mainClass="co.edu.unicauca.bancopreguntas.frontend.FrontendMain"
