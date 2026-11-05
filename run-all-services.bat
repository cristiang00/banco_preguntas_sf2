@echo off
cd /d "C:\Users\mixmi\OneDrive\Desktop\microServicios\banco_preguntas_sf2"

echo ==========================================================
echo Iniciando los 8 Microservicios en ventanas de CMD...
echo ==========================================================

start "api-gateway (8080)" cmd /k "title API-GATEWAY [8080] && java -jar api-gateway\target\api-gateway-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "users-service (8081)" cmd /k "title USERS-SERVICE [8081] && java -jar services\users-service\target\users-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "questions-service (8082)" cmd /k "title QUESTIONS-SERVICE [8082] && java -jar services\questions-service\target\questions-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "validation-service (8083)" cmd /k "title VALIDATION-SERVICE [8083] && java -jar services\validation-service\target\validation-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "lifecycle-service (8084)" cmd /k "title LIFECYCLE-SERVICE [8084] && java -jar services\lifecycle-service\target\lifecycle-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "review-service (8085)" cmd /k "title REVIEW-SERVICE [8085] && java -jar services\review-service\target\review-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "notification-service (8086)" cmd /k "title NOTIFICATION-SERVICE [8086] && java -jar services\notification-service\target\notification-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "audit-service (8087)" cmd /k "title AUDIT-SERVICE [8087] && java -jar services\audit-service\target\audit-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

echo ==========================================================
echo Todos los 8 microservicios fueron lanzados en sus CMDs.
echo ==========================================================
