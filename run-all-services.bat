@echo off
cd /d "%~dp0"

echo ==========================================================
echo Verificando artefactos y autenticacion OIDC (:8089)...
echo ==========================================================

if not exist "api-gateway\target\api-gateway-2.0.0-SNAPSHOT.jar" (
    echo Compilando microservicios con Maven...
    call mvnw.cmd package -DskipTests
    if errorlevel 1 (
        echo Error compilando el proyecto.
        exit /b 1
    )
)

netstat -ano | findstr ":8089 " | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    echo Iniciando servidor OIDC/JWKS local en puerto 8089...
    start "local-oidc (8089)" cmd /k "title LOCAL-OIDC [8089] && java scripts\LocalOidcServer.java"
    timeout /t 3 /nobreak >nul
) else (
    echo [OK] Keycloak / OIDC ya activo en puerto 8089.
)

echo ==========================================================
echo Iniciando los 8 Microservicios en ventanas de CMD...
echo ==========================================================

start "users-service (8081)" cmd /k "title USERS-SERVICE [8081] && java -Xmx256m -jar services\users-service\target\users-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "questions-service (8082)" cmd /k "title QUESTIONS-SERVICE [8082] && java -Xmx256m -jar services\questions-service\target\questions-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "validation-service (8083)" cmd /k "title VALIDATION-SERVICE [8083] && java -Xmx256m -jar services\validation-service\target\validation-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "lifecycle-service (8084)" cmd /k "title LIFECYCLE-SERVICE [8084] && java -Xmx256m -jar services\lifecycle-service\target\lifecycle-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "review-service (8085)" cmd /k "title REVIEW-SERVICE [8085] && java -Xmx256m -jar services\review-service\target\review-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "notification-service (8086)" cmd /k "title NOTIFICATION-SERVICE [8086] && java -Xmx256m -jar services\notification-service\target\notification-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "audit-service (8087)" cmd /k "title AUDIT-SERVICE [8087] && java -Xmx256m -jar services\audit-service\target\audit-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "api-gateway (8080)" cmd /k "title API-GATEWAY [8080] && java -Xmx256m -jar api-gateway\target\api-gateway-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

echo ==========================================================
echo Todos los 8 microservicios + OIDC (:8089) fueron lanzados.
echo Para abrir el frontend ejecute:
echo   java -jar frontend\target\frontend-2.0.0-SNAPSHOT.jar
echo ==========================================================
