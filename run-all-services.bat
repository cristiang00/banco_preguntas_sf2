@echo off
cd /d "C:\Users\mixmi\OneDrive\Desktop\microServicios\banco_preguntas_sf2"

echo Iniciando microservicios de fondo...
start "validation-service (8083)" cmd /k java -jar "services\validation-service\target\validation-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "lifecycle-service (8084)" cmd /k java -jar "services\lifecycle-service\target\lifecycle-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "review-service (8085)" cmd /k java -jar "services\review-service\target\review-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "notification-service (8086)" cmd /k java -jar "services\notification-service\target\notification-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

start "audit-service (8087)" cmd /k java -jar "services\audit-service\target\audit-service-2.0.0-SNAPSHOT.jar"
timeout /t 2 /nobreak >nul

echo Todos los microservicios fueron lanzados.

