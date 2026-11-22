# Banco de Preguntas Saber Pro — SF2

[![CI](https://github.com/cristiang00/banco_preguntas_sf2/actions/workflows/ci.yml/badge.svg)](https://github.com/cristiang00/banco_preguntas_sf2/actions/workflows/ci.yml)
[![Java 17](https://img.shields.io/badge/Java-17-blue?logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://docs.docker.com/compose/)

Sistema distribuido de gestión y revisión de preguntas para el examen Saber Pro de la Universidad del Cauca, desarrollado con microservicios Spring Boot, arquitectura hexagonal, Keycloak JWT y mensajería RabbitMQ.

---

## Tabla de Contenidos

- [Arquitectura](#arquitectura)
- [Requisitos](#requisitos)
- [Inicio Rápido con Docker](#inicio-rápido-con-docker)
- [Ejecución del Frontend Swing](#ejecución-del-frontend-swing)
- [Usuarios de Prueba](#usuarios-de-prueba)
- [URLs y Puertos](#urls-y-puertos)
- [Desarrollo Local sin Docker](#desarrollo-local-sin-docker)
- [Pruebas](#pruebas)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Sprint 3 — Estado del Cierre](#sprint-3--estado-del-cierre)

---

## Arquitectura

El sistema sigue una **arquitectura de microservicios** con un API Gateway central:

```
Frontend (Swing)
      │  JWT Bearer
      ▼
 API Gateway :8080  ◄─── Keycloak :8089 (RS256 JWKS)
      │  X-User-* headers
      ├──► users-service      (gestión de usuarios)
      ├──► questions-service  (preguntas + Builder/Facade)
      ├──► validation-service (reglas de validación)
      ├──► lifecycle-service  (máquina de estados / patrón State)
      ├──► review-service     (arquitectura hexagonal)
      ├──► notification-service (SMTP + RabbitMQ)
      └──► audit-service      (auditoría / Observer)

Mensajería: RabbitMQ exchange saberpro.events
BD:         PostgreSQL (una base por servicio)
Correo:     Mailpit (desarrollo) / SMTP real (producción)
```

Consulta [`docs/arquitectura-sprint3.md`](docs/arquitectura-sprint3.md) para los diagramas C4 completos.

---

## Requisitos

| Herramienta | Versión mínima |
|-------------|---------------|
| Docker Desktop | 4.x |
| Java JDK | 17 (Temurin) |
| Maven Wrapper | incluido (`mvnw.cmd`) |

---

## Inicio Rápido con Docker

```powershell
# 1. Copiar y ajustar variables de entorno
Copy-Item .env.example .env

# 2. Construir imágenes y levantar todos los servicios
docker compose up --build -d

# 3. Verificar que todos están healthy
docker compose ps
```

> **Nota:** El primer arranque puede tardar 2-3 minutos mientras Keycloak y PostgreSQL inicializan.

### Parar y limpiar

```powershell
# Parar sin borrar datos
docker compose down

# Parar y borrar volúmenes (¡borra la base de datos!)
docker compose down -v
```

---

## Ejecución del Frontend Swing

### Con Docker activo (recomendado)

```powershell
# Desde la raíz del proyecto
cd frontend
..\mvnw.cmd package -DskipTests
java -jar target\frontend-2.0.0-SNAPSHOT.jar
```

La variable `GATEWAY_URL` apunta por defecto a `http://localhost:8080`. Para cambiarla:

```powershell
$env:GATEWAY_URL = "http://mi-servidor:8080"
java -jar target\frontend-2.0.0-SNAPSHOT.jar
```

### Sin Docker (todos los servicios en localhost)

Ejecutar en este orden: PostgreSQL → RabbitMQ → Keycloak → microservicios → frontend.  
Usar `start-all.ps1` para automatizar el arranque local.

---

## Usuarios de Prueba

Los siguientes usuarios vienen preconfigurados en el realm `saberpro` de Keycloak:

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `autor1` | `Autor1234!` | AUTOR |
| `revisor1` | `Revisor1234!` | REVISOR |
| `admin` | `Admin1234!` | ADMINISTRADOR |

Para obtener un token JWT:

```bash
curl -X POST http://localhost:8089/realms/saberpro/protocol/openid-connect/token \
  -d "client_id=gateway-client" \
  -d "client_secret=saberpro-client-secret-2026" \
  -d "grant_type=password" \
  -d "username=autor1" \
  -d "password=Autor1234!"
```

---

## URLs y Puertos

| Servicio | URL |
|----------|-----|
| API Gateway | http://localhost:8080 |
| Swagger UI (gateway) | http://localhost:8080/swagger-ui.html |
| Keycloak Admin | http://localhost:8089 |
| Mailpit (correos dev) | http://localhost:8025 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |

> Los microservicios **no exponen puertos directos**; todo el tráfico entra por el gateway.

---

## Desarrollo Local sin Docker

```powershell
# Compilar todo el monorepo
.\mvnw.cmd package -DskipTests

# Ejecutar un servicio específico
cd services\questions-service
..\..\mvnw.cmd spring-boot:run
```

---

## Pruebas

```powershell
# Ejecutar TODOS los tests del monorepo
.\mvnw.cmd test

# Ejecutar tests de un módulo específico
.\mvnw.cmd test -pl api-gateway
.\mvnw.cmd test -pl services/review-service
```

### Cobertura por módulo (Sprint 3)

| Módulo | Tests | Estado |
|--------|-------|--------|
| `api-gateway` | 16 | ✅ |
| `questions-service` | 7 | ✅ |
| `validation-service` | ~15 | ✅ |
| `lifecycle-service` | 8 | ✅ |
| `review-service` | ~10 | ✅ |
| `notification-service` | ~8 | ✅ |
| `audit-service` | ~5 | ✅ |
| `frontend` | 4 | ✅ |

---

## Estructura del Proyecto

```
banco_preguntas_sf2/
├── .github/workflows/ci.yml     # Pipeline CI GitHub Actions
├── api-gateway/                  # Gateway centralizado (JWT, enrutamiento, Swagger)
├── contracts/                    # DTOs y eventos compartidos entre servicios
├── services/
│   ├── users-service/            # Gestión de usuarios (CRUD, roles)
│   ├── questions-service/        # Preguntas (Builder, Facade, versiones)
│   ├── validation-service/       # Motor de validación (Strategy/Chain)
│   ├── lifecycle-service/        # Ciclo de vida (patrón State)
│   ├── review-service/           # Revisión (Arquitectura Hexagonal)
│   ├── notification-service/     # Notificaciones (Observer, SMTP, RabbitMQ)
│   └── audit-service/            # Auditoría (Observer, RabbitMQ)
├── frontend/                     # Cliente de escritorio Swing
├── docker/
│   ├── init-db.sql               # Inicialización de bases de datos
│   └── keycloak/
│       └── saberpro-realm.json   # Realm Keycloak con roles y cliente
├── docs/
│   ├── HU-Sprint-3-para-Jira.pdf
│   ├── sprint3-progreso.md       # Checklist de avance
│   └── arquitectura-sprint3.md   # Documento de arquitectura C4
├── docker-compose.yml
├── .env.example
└── pom.xml                       # POM raíz del monorepo
```

---

## Sprint 3 — Estado del Cierre

| HU | Título | Estado |
|----|--------|--------|
| HU9 | Dockerización completa | ✅ Completada |
| HU12 | Pendientes Sprint 2 (SMTP, filtros BD, RabbitMQ) | ✅ Completada |
| HU8 | Arquitectura hexagonal en Review | ✅ Completada |
| HU6 | Historial de revisiones y versión mejorada | ✅ Completada |
| HU7 | Autenticación y autorización Keycloak + JWT | ✅ Completada |
| HU10 | Pruebas unitarias y pipeline CI | ✅ Completada |
| HU11 | Documento de arquitectura y README | ✅ Completada |

Ver progreso detallado en [`docs/sprint3-progreso.md`](docs/sprint3-progreso.md).

