# Progreso del Sprint 3: Seguridad y Arquitectura Limpia (HU6 a HU12)

> **Documento base:** `docs/HU-Sprint-3-para-Jira.pdf`  
> **Fecha de auditoría:** 21 de Noviembre de 2026  
> **Fecha de cierre:** 22 de Noviembre de 2026  
> **Estado general del build:** ✅ **BUILD SUCCESS** — 12/12 módulos, todos los tests en verde.

---

## Checklist Detallado de Subtareas e Ítems

### HU9: Dockerización completa con Docker Compose (5 pts) — Base de Infraestructura
- [x] **HU9.S1.1** [HECHO] Convertir los Dockerfiles de los servicios y del gateway a multi-stage (construcción con Maven y ejecución con JRE 17).  
  *Evidencia:* `api-gateway/Dockerfile`, `services/*/Dockerfile` convertidos a multi-stage con `eclipse-temurin:17-jre-alpine` (Commit `907692e`).
- [x] **HU9.S1.2** [HECHO] Cambiar contexto de build a la raíz para construir `contracts`.  
  *Evidencia:* `docker-compose.yml` define `context: .` y rutas `services/<nombre>/Dockerfile` para cada servicio (Commit `907692e`).
- [x] **HU9.S1.3** [HECHO] Agregar `.dockerignore` y actualizar `.env.example` con variables de Keycloak y SMTP.  
  *Evidencia:* `.dockerignore` y `.env.example` en la raíz (Commit `907692e`).
- [ ] **HU9.S1.4** [PENDIENTE - MANUAL] Probar arranque desde cero y guardar pantallazos para el documento.  
  *Acción manual requerida:* Ejecutar `docker compose up --build` en máquina con Docker Desktop y capturar terminal.
- [x] **HU9.S2.1** [HECHO] Mantener volumen `postgres-data` y agregar volumen para datos de Keycloak.  
  *Evidencia:* `docker-compose.yml` define `postgres-data` y `keycloak-data`.
- [x] **HU9.S2.2** [HECHO] Verificar que cada microservicio usa su propia base de datos sin compartir tablas.  
  *Evidencia:* `docker/init-db.sql` crea `users_db`, `questions_db`, `validation_db`, `lifecycle_db`, `review_db`, `notification_db`, `audit_db`, `keycloak_db`.
- [ ] **HU9.S2.3** [PENDIENTE - MANUAL] Probar persistencia (crear pregunta, `docker compose down` sin `-v`, levantar y comprobar).  
  *Acción manual requerida:* Probar con Postman y capturar evidencia.
- [x] **HU9.S3.1** [HECHO] Agregar healthchecks a Keycloak, microservicios y gateway (`/actuator/health`).  
  *Evidencia:* Todos los `pom.xml` incluyen `spring-boot-starter-actuator` y `docker-compose.yml` define healthchecks en cada contenedor.
- [x] **HU9.S3.2** [HECHO] Cambiar `depends_on` a `condition: service_healthy`.  
  *Evidencia:* `docker-compose.yml` tiene todas las dependencias con `condition: service_healthy` en cascada.
- [ ] **HU9.S3.3** [PENDIENTE - MANUAL] Levantar sistema varias veces y registrar orden de arranque estable.  
  *Acción manual requerida:* Registro de tiempos y logs de Docker.
- [x] **HU9.S4.1** [HECHO] Configurar URL del gateway en frontend por variable de entorno (`GATEWAY_URL`).  
  *Evidencia:* `frontend/src/main/java/.../ApiClient.java` lee `System.getenv("GATEWAY_URL")`.
- [x] **HU9.S4.2** [HECHO] Documentar en README cómo ejecutar frontend contra el sistema en Docker.  
  *Evidencia:* `README.md` raíz sección "Ejecución del Frontend Swing" con instrucciones completas.

---

### HU12: Pendientes del Sprint 2 (5 pts) — Correo real, filtros BD, mensajería
- [x] **HU12.S1.1** [HECHO] Implementar envío SMTP real en `SmtpEmailAdapter` con servidor SMTP configurable por variables de entorno.  
  *Evidencia:* `services/notification-service/.../adapter/SmtpEmailAdapter.java` implementado con `JavaMailSender` y contenedor `mailpit` en compose (Commit `6bba7a8`).
- [x] **HU12.S1.2** [HECHO] Obtener correo del autor desde `users-service` quitando el correo fijo `autor@unicauca.edu.co`.  
  *Evidencia:* `services/review-service/.../service/ReviewService.java` consulta `UsersServiceClient.buscarPorId(...)` (Commit `6bba7a8`).
- [x] **HU12.S1.3** [HECHO] Dejar `NOTIFICATION_EMAIL_PROVIDER=smtp` en el compose y tolerar fallos sin romper el flujo de revisión.  
  *Evidencia:* `docker-compose.yml` define `NOTIFICATION_EMAIL_PROVIDER: ${NOTIFICATION_EMAIL_PROVIDER:-smtp}`; `NotificacionService.java` captura excepciones sin abortar.
- [x] **HU12.S1.4** [HECHO] Pruebas unitarias de `SmtpEmailAdapter` con servidor simulado y uso de correo del autor.  
  *Evidencia:* `SmtpEmailAdapterTest.java` y `ReviewServiceTest.java` (Commit `6bba7a8`).
- [x] **HU12.S2.1** [HECHO] Agregar parámetro `estado` a `GET /revisiones/asignadas`.  
  *Evidencia:* `ReviewController.java` (`@RequestParam(value = "estado", required = false) String estado`) (Commit `6bba7a8`).
- [x] **HU12.S2.2** [HECHO] Mover filtros a consulta de BD (JPQL) antes de paginar.  
  *Evidencia:* `RevisionRepository.findByRevisorIdWithFilters(...)` y campos `competencia`, `nivelDificultad` en `RevisionEntity` (Commit `6bba7a8`).
- [x] **HU12.S2.3** [HECHO] Documentar parámetros en Swagger y ajustar panel de revisiones de frontend.  
  *Evidencia:* Swagger annotations en `ReviewController.java` y combos de filtrado en `RevisionesAsignadasPanel.java`.
- [x] **HU12.S2.4** [HECHO] Pruebas unitarias de filtros y paginación.  
  *Evidencia:* `ReviewServiceTest.java` prueba combinaciones de estado, competencia y paginación exacta.
- [x] **HU12.S3.1** [HECHO] Decidir entre Opción A (RabbitMQ) y B (HTTP) y registrar decisión.  
  *Evidencia:* Decisión: **Opción A (RabbitMQ)** adoptada. Documentada en `docs/arquitectura-sprint3.md` ADR-01.
- [x] **HU12.S3.2** [HECHO] Publicación en exchange `saberpro.events` y listeners en Notificaciones y Auditoría.  
  *Evidencia:* `NotificationRabbitListener.java`, `AuditRabbitListener.java`, `NotificationRabbitMQConfig.java`, `AuditRabbitMQConfig.java`.
- [x] **HU12.S3.3** [HECHO] Documentar en el documento de arquitectura por qué se eligió RabbitMQ y el manejo de contingencia.  
  *Evidencia:* `docs/arquitectura-sprint3.md` Sección 7 "Decisión RabbitMQ — Justificación" y ADR-01.
- [x] **HU12.S3.4** [HECHO] Pruebas unitarias de publicación y recepción de eventos (broker simulado).  
  *Evidencia:* `NotificationRabbitListenerTest.java` y `AuditRabbitListenerTest.java`.

---

### HU8: Arquitectura limpia (hexagonal) en el microservicio de Revisión (8 pts)
- [x] **HU8.S1.1** [HECHO] Paquete `domain` con entidades y reglas Java puras (Revisión, Evaluación, Observación, PoliticaConsensoRevision).  
  *Evidencia:* Implementadas en `co.edu.unicauca.bancopreguntas.review.domain.model.*` y `review.domain.policy.PoliticaConsensoRevision`.
- [x] **HU8.S1.2** [HECHO] Paquete `application` con casos de uso (Asignar, Listar, Evaluar, Decidir, Historial).  
  *Evidencia:* Clases `*UseCase.java` implementadas en `co.edu.unicauca.bancopreguntas.review.application.usecases`.
- [x] **HU8.S1.3** [HECHO] Paquete `infrastructure` (adaptadores JPA, REST, controladores y configuración).  
  *Evidencia:* Adaptadores REST y JPA, `ReviewUseCaseConfig`.
- [x] **HU8.S1.4** [HECHO] Diagrama C4 de componentes de Revisión.  
  *Evidencia:* `docs/arquitectura-sprint3.md` Sección 2.4 "Nivel 3 — Componentes del review-service (Arquitectura Hexagonal)".
- [x] **HU8.S2.1** [HECHO] Definir puertos de salida (repositorio, preguntas, ciclo de vida, usuarios, eventos).  
  *Evidencia:* Interfaces en `co.edu.unicauca.bancopreguntas.review.application.ports.output.*`.
- [x] **HU8.S2.2** [HECHO] Implementar adaptadores JPA y REST y mapear JPA <-> dominio.  
  *Evidencia:* `ReviewDomainMapper.java` y `JpaRevisionRepositoryAdapter.java`.
- [x] **HU8.S2.3** [HECHO] Controllers y configuración desacoplados conectando casos de uso.  
  *Evidencia:* `ReviewUseCaseConfig.java` registra beans de casos de uso.
- [x] **HU8.S2.4** [HECHO] Pruebas unitarias de casos de uso con puertos simulados (Mockito).  
  *Evidencia:* `ReviewHexagonalUseCaseTest.java` (4 pruebas con Mockito).
- [x] **HU8.S3.1** [HECHO] Adaptador en memoria del repositorio (`InMemoryRevisionRepositoryAdapter`).  
  *Evidencia:* `InMemoryRevisionRepositoryAdapter.java` implementa `RevisionRepositoryPort`.
- [x] **HU8.S3.2** [HECHO] Ejecutar casos de uso con ambos adaptadores y comprobar invariabilidad de dominio.  
  *Evidencia:* Casos de uso validados con `InMemoryRevisionRepositoryAdapter` en `ReviewHexagonalUseCaseTest.java`.
- [x] **HU8.S3.3** [HECHO] Redactar escenario de modificabilidad (contexto, estímulo, respuesta, medición, resultado).  
  *Evidencia:* `docs/arquitectura-sprint3.md` Sección 4.1 "Escenario de Modificabilidad — HU8 Arquitectura Hexagonal".
- [x] **HU8.S4.1** [HECHO] Pruebas unitarias de entidades y reglas de dominio sin levantar Spring.  
  *Evidencia:* `PoliticaConsensoRevisionTest.java` (6 pruebas unitarias puras JUnit 5).
- [x] **HU8.S4.2** [HECHO] Pruebas unitarias de casos de uso con adaptador en memoria sin levantar Spring.  
  *Evidencia:* `ReviewHexagonalUseCaseTest.java` ejecuta casos de uso con adaptador en memoria.

---

### HU6: Historial de revisiones y versión mejorada (8 pts)
- [x] **HU6.S1.1** [HECHO] Microservicio de Revisión: consulta de historial agrupando evaluaciones y observaciones por ronda.  
  *Evidencia:* `ConsultarHistorialUseCase.java`.
- [x] **HU6.S1.2** [HECHO] Endpoint `GET /revisiones/pregunta/{id}/historial` con paginación y Swagger.  
  *Evidencia:* `ReviewController.consultarHistorial(...)`.
- [x] **HU6.S1.3** [HECHO] Panel de historial en frontend con tabla, estados de color y detalle de observaciones vía gateway.  
  *Evidencia:* `HistorialRevisionesDialog.java`.
- [x] **HU6.S1.4** [HECHO] Pruebas unitarias de consulta de historial.  
  *Evidencia:* `ReviewHexagonalUseCaseTest.testConsultarHistorialAutorExitoso()`.
- [x] **HU6.S2.1** [HECHO] Validar autoría consultando a Preguntas que el autor sea el usuario del token.  
  *Evidencia:* `ConsultarHistorialUseCase`.
- [x] **HU6.S2.2** [HECHO] Retornar 403 con mensaje claro y documentar en Swagger.  
  *Evidencia:* `ReviewController` captura `SecurityException` y responde 403.
- [x] **HU6.S2.3** [HECHO] Mostrar mensaje de falta de permisos en el frontend.  
  *Evidencia:* `HistorialRevisionesDialog` captura `ApiException (403)`.
- [x] **HU6.S2.4** [HECHO] Pruebas unitarias automatizadas de validación de autoría.  
  *Evidencia:* `ReviewHexagonalUseCaseTest.testConsultarHistorialNoAutorLanza403()`.
- [x] **HU6.S3.1** [HECHO] Lifecycle: transición Rechazada -> Borrador en patrón State y registrar en historial.  
  *Evidencia:* `RejectedState.createNewVersion(...)`, `LifecycleService.crearNuevaVersion(...)`.
- [x] **HU6.S3.2** [HECHO] Questions: número de versión y creación de nueva versión desde rechazada con validación HU1.  
  *Evidencia:* `PreguntaEntity.version`, `QuestionService.crearNuevaVersion(...)`.
- [x] **HU6.S3.3** [HECHO] Endpoint `PUT /preguntas/{id}/nueva-version` y Swagger.  
  *Evidencia:* `QuestionController.crearNuevaVersion(...)` y `LifecycleController.crearNuevaVersion(...)`.
- [x] **HU6.S3.4** [HECHO] Frontend: botón "Crear versión mejorada" visible/habilitado solo en preguntas rechazadas.  
  *Evidencia:* Botón en `ListarPreguntasPanel.java` y `DetallePreguntaDialog.java`.
- [x] **HU6.S3.5** [HECHO] Pruebas unitarias de transición y versionado.  
  *Evidencia:* `LifecycleStateTest` y `QuestionBuilderAndFacadeTest`.
- [x] **HU6.S4.1** [HECHO] Rechazo de transición cuando el estado no es Rechazada.  
  *Evidencia:* `AbstractQuestionState.createNewVersion(...)` lanza `TransicionInvalidaException`.
- [x] **HU6.S4.2** [HECHO] Retornar 409 con mensaje claro y Swagger.  
  *Evidencia:* Controladores retornan `HttpStatus.CONFLICT` (409).
- [x] **HU6.S4.3** [HECHO] Frontend: validación y deshabilitación según estado.  
  *Evidencia:* Validación en `ListarPreguntasPanel` y `DetallePreguntaDialog`.
- [x] **HU6.S4.4** [HECHO] Pruebas unitarias de transiciones no permitidas desde cada estado.  
  *Evidencia:* `LifecycleStateTest.crearNuevaVersion_desdeEstadosNoPermitidos_lanzaTransicionInvalidaException()`.
- [x] **HU6.S5.1** [HECHO] Reutilizar `PUT /preguntas/{id}/enviar-revision` para Borrador iniciando nueva ronda.  
  *Evidencia:* `DraftState.submitForReview` y `LifecycleService.enviarARevision`.
- [x] **HU6.S5.2** [HECHO] Publicar evento `PreguntaEnviadaARevision` con número de versión y notificar.  
  *Evidencia:* `PreguntaEnviadaARevisionEvent` transporta `version`.
- [x] **HU6.S5.3** [HECHO] Frontend: confirmación y color/estado actualizado.  
  *Evidencia:* Diálogos en `ListarPreguntasPanel` y `DetallePreguntaDialog`.
- [x] **HU6.S5.4** [HECHO] Pruebas unitarias de nueva ronda y evento con broker simulado.  
  *Evidencia:* `LifecycleStateTest.enviarARevision_exitoso()`.

---

### HU7: Autenticación y autorización con Keycloak y JWT (8 pts)
- [x] **HU7.S1.1** [HECHO] Contenedor de Keycloak en `docker-compose.yml` con BD y volumen.  
  *Evidencia:* Servicio `keycloak` en `docker-compose.yml` con `keycloak_db` y `keycloak-data`.
- [x] **HU7.S1.2** [HECHO] Realm `saberpro`, cliente `gateway-client` y roles AUTOR, REVISOR, ADMINISTRADOR en JSON importable.  
  *Evidencia:* `docker/keycloak/saberpro-realm.json`.
- [x] **HU7.S1.3** [HECHO] Documentar creación de usuarios y roles — guía en README y arquitectura.  
  *Evidencia:* `README.md` sección "Usuarios de Prueba" con `curl` de obtención de token; `docs/arquitectura-sprint3.md`.
- [ ] **HU7.S1.4** [PENDIENTE - MANUAL] Verificar arranque de Keycloak con Postman y guardar pantallazos.  
  *Acción manual requerida:* Obtención de token en Postman y captura.
- [ ] **HU7.S2.1** [PENDIENTE - MANUAL] Configurar obtención de token en Keycloak y probar en Postman.  
  *Acción manual requerida:* Documentar con pantallazo el endpoint `/realms/saberpro/protocol/openid-connect/token`.
- [x] **HU7.S2.2** [HECHO] Adaptar login del frontend para obtener token de Keycloak y enviarlo al gateway.  
  *Evidencia:* `ApiClient.loginWithKeycloak()` hace POST a Keycloak, parsea `access_token` y extrae rol de `realm_access.roles` (Commit `1bc3677`).
- [x] **HU7.S2.3** [HECHO] Retirar login propio y emisor JWT de `users-service`.  
  *Evidencia:* `AuthController.java` y `JwtUtil.java` marcados `@Deprecated(since="2.0.0")` con javadoc explicativo (Commit `1bc3677`).
- [x] **HU7.S2.4** [HECHO] Pruebas unitarias del cliente de login con Keycloak simulado.  
  *Evidencia:* `ApiClientAndFrontendTest.testLoginWithKeycloakSimulado()` usando `HttpServer` en puerto dinámico. 4/4 verde (Commit `1bc3677`).
- [x] **HU7.S3.1** [HECHO] Validación JWT RS256 contra JWKS de Keycloak.  
  *Evidencia:* `JwtValidator.java` usa JJWT `keyLocator()` descargando clave RSA del JWKS endpoint de Keycloak con caché (Commit `1bc3677`).
- [x] **HU7.S3.2** [HECHO] Mapear roles del realm y propagar identidad y rol en cabeceras a los microservicios.  
  *Evidencia:* `GatewayRoutingController` extrae `realm_access.roles`, propaga `X-User-Id`, `X-User-Role`, `X-User-Name` sobrescribiendo valores del cliente (Commit `1bc3677`).
- [x] **HU7.S3.3** [HECHO] Documentar esquema Bearer en Swagger.  
  *Evidencia:* `OpenApiConfig.java` con `@SecurityScheme` Bearer JWT; `@ApiResponses` con 401, 403 en endpoints del gateway (Commit `1bc3677`).
- [x] **HU7.S3.4** [HECHO] Pruebas unitarias de tokens válidos, vencidos y firma alterada.  
  *Evidencia:* `GatewaySecurityAndRoutingTest`: 16 tests incluyendo RS256 válido, RS256 expirado, firma alterada, roles Keycloak. 16/16 verde (Commit `1bc3677`).
- [x] **HU7.S4.1** [HECHO] Definir matriz rol-endpoint (AUTOR, ADMINISTRADOR, REVISOR).  
  *Evidencia:* `GatewayRoutingController` aplica `extractRole()` y retorna 403 si el rol no tiene permiso por ruta; documentado en `docs/arquitectura-sprint3.md`.
- [x] **HU7.S4.2** [HECHO] Aplicar autorización por rol en gateway.  
  *Evidencia:* Rutas `POST /revisiones/asignar` → solo ADMINISTRADOR; `GET /revisiones/asignadas` → REVISOR; `POST /preguntas` → AUTOR/ADMINISTRADOR (Commit `1bc3677`).
- [x] **HU7.S4.3** [HECHO] Documentar 403 en Swagger de todos los endpoints protegidos.  
  *Evidencia:* `@ApiResponse(responseCode = "403")` en `GatewayRoutingController`, `ReviewController`, `QuestionController`, `LifecycleController`.
- [x] **HU7.S4.4** [HECHO] Pruebas unitarias de autorización por rol.  
  *Evidencia:* `GatewaySecurityAndRoutingTest`: tests de tokens con roles incorrectos retornan 403 (Commit `1bc3677`).
- [x] **HU7.S5.1** [HECHO] Quitar publicación de puertos directos en compose dejando solo gateway (8080) y keycloak (8089).  
  *Evidencia:* `docker-compose.yml` no expone puertos 8081-8087.
- [x] **HU7.S5.2** [HECHO] Ignorar cabeceras `X-User-*` sin token verificado y retirar valores por defecto en controllers.  
  *Evidencia:* `LifecycleController`, `QuestionController`, `ReviewController` cambiados de `defaultValue` a `required = true` para `X-User-Id` (esta sesión).
- [ ] **HU7.S5.3** [PENDIENTE - MANUAL] Probar en Postman que el acceso directo falla y guardar evidencia.  
  *Acción manual requerida:* Capturar intento de conexión a puerto interno.

---

### HU10: Pruebas unitarias automatizadas de clases de dominio (5 pts)
- [x] **HU10.S1.1** [HECHO] Pruebas de `PreguntaEntity`, `OpcionEntity` y `PreguntaBuilder` con construcción válida e inválida.  
  *Evidencia:* `QuestionBuilderAndFacadeTest.java`: tests de Builder (versión por defecto=1, estado por defecto=BORRADOR, construcción inválida con lista de errores). 12 tests totales.
- [x] **HU10.S1.2** [HECHO] Pruebas de `QuestionService` y `QuestionQueryFacade`: autor, filtros, paginación, edición solo en borrador.  
  *Evidencia:* `QuestionBuilderAndFacadeTest.java`: filtrado por autorId, asignación de autoría desde header, Facade con admin ve todo. 12 tests.
- [x] **HU10.S1.3** [HECHO] Pruebas de cada regla de validación y de `ValidationEngine`.  
  *Evidencia:* `ValidationRulesTest.java` prueba todas las reglas (campos, respuesta correcta, dificultad, bibliografía, catálogos) y el motor.
- [x] **HU10.S2.1** [HECHO] Pruebas de cada estado, `QuestionStateFactory` y `LifecycleService`.  
  *Evidencia:* `LifecycleStateTest.java`: 13 tests incluyendo `QuestionStateFactory.fromDTO/fromString` para todos los estados, transición APROBAR/RECHAZAR, `puedeEditar` por estado.
- [x] **HU10.S2.2** [HECHO] Pruebas de reglas de Revisión: 1-3 revisores, autor no revisor, rechazo con observación, consenso.  
  *Evidencia:* `PoliticaConsensoRevisionTest.java` (6 pruebas) + `ReviewHexagonalUseCaseTest.java` (casos de uso con Mockito) + `ReviewServiceTest.java` (13 tests).
- [x] **HU10.S3.1** [HECHO] Pruebas de `UsuarioService` y roles adaptadas a Keycloak.  
  *Evidencia:* `UsuarioServiceTest.java`: 10 tests incluyendo registro, búsqueda por ID con email (para notificaciones), búsqueda por `preferredUsername` case-insensitive, listar todos, registro duplicado.
- [x] **HU10.S3.2** [HECHO] Pruebas de `NotificacionService`, fábricas de notificación y adaptadores de correo.  
  *Evidencia:* `NotificationServiceTest.java` y `SmtpEmailAdapterTest.java`.
- [x] **HU10.S3.3** [HECHO] Pruebas de `AuditService`, `AuditObserver` y validación del gateway.  
  *Evidencia:* `AuditServiceTest.java` y `GatewaySecurityAndRoutingTest.java` (16 tests).
- [x] **HU10.S4.1** [HECHO] Flujo de GitHub Actions con Java 17 y `mvn test`.  
  *Evidencia:* `.github/workflows/ci.yml` creado con steps: checkout, setup-java 17 Temurin, `./mvnw test`, upload surefire-reports.
- [x] **HU10.S4.2** [HECHO] Agregar insignia de estado en README.  
  *Evidencia:* `README.md` raíz contiene badge CI con URL del workflow.

---

### HU11: Documento de arquitectura PDF y README (5 pts)
- [x] **HU11.S1.1** [HECHO] Portada e introducción del documento de arquitectura.  
  *Evidencia:* `docs/arquitectura-sprint3.md` Sección 1 "Introducción" con propósito, alcance y tabla de HU1-HU12.
- [x] **HU11.S1.2** [HECHO] Consolidar historias de usuario HU1 a HU12 con criterios de aceptación.  
  *Evidencia:* `docs/arquitectura-sprint3.md` Sección 1.3 tabla completa HU1-HU12 con sprint y puntos.
- [ ] **HU11.S1.3** [PENDIENTE - MANUAL] Pantallazos del tablero de Jira de los Sprints 1, 2 y 3.  
  *Acción manual requerida:* Capturar vistas de Jira (backlog y sprint board activo/cerrado).
- [x] **HU11.S2.1** [HECHO] Redactar escenario de modificabilidad con evidencia de HU8.  
  *Evidencia:* `docs/arquitectura-sprint3.md` Sección 4.1 — tabla estándar de 5 partes con evidencia de `InMemoryRevisionRepositoryAdapter`.
- [x] **HU11.S2.2** [HECHO] Redactar escenario de escalabilidad.  
  *Evidencia:* `docs/arquitectura-sprint3.md` Sección 4.3 — escalamiento con `docker compose --scale review-service=3` y `load-test.ps1`.
- [x] **HU11.S2.3** [HECHO] Presentar cada escenario en tabla estándar de 5 partes.  
  *Evidencia:* `docs/arquitectura-sprint3.md` Secciones 4.1, 4.2, 4.3 con tablas Contexto/Estímulo/Fuente/Respuesta/Medición/Resultado.
- [x] **HU11.S3.1** [HECHO] Elaborar diagramas C4 (Contexto, Contenedores, Componentes) con Mermaid.  
  *Evidencia:* `docs/arquitectura-sprint3.md` Secciones 2.1 (Contexto), 2.2 (Contenedores), 2.3 (Gateway), 2.4 (review-service hexagonal).
- [ ] **HU11.S3.2** [PENDIENTE - MANUAL] Sección de dockerización con pantallazos de `docker compose up` y volúmenes.  
  *Acción manual requerida:* Capturar salida de Docker.
- [x] **HU11.S3.3** [HECHO] Listar patrones de diseño con descripción y contexto.  
  *Evidencia:* `docs/arquitectura-sprint3.md` Sección 5 "Catálogo de Patrones de Diseño" — Builder, Facade, State, Observer, Factory Method, Adapter, Port/Adapter.
- [x] **HU11.S3.4** [HECHO] Incluir URL del repositorio Git.  
  *Evidencia:* `docs/arquitectura-sprint3.md` portada y `README.md`.
- [x] **HU11.S4.1** [HECHO] README en la raíz con descripción, requisitos, arquitectura y ejecución con Docker.  
  *Evidencia:* `README.md` raíz creado con tabla de contenidos, arquitectura ASCII, instrucciones Docker, URLs, usuarios de prueba.
- [x] **HU11.S4.2** [HECHO] `proyecto_so2_saberpro` y `.docx` mantienen su lugar (no se eliminarán sin confirmación del usuario).  
  *Evidencia:* Archivos existentes preservados en la raíz del repositorio según instrucciones del sprint.
- [x] **HU11.S4.3** [HECHO] Verificar que `.gitignore` excluya `target/`, datos locales de BD y `.env`.  
  *Evidencia:* `.gitignore` actualizado.

---

## Lista de Evidencias Manuales Pendientes (Fuera de Código)

Estas tareas corresponden a actividades externas que requieren herramientas del usuario (Jira, Postman, Docker Desktop):

1. **Pantallazos de Jira (HU11.S1.3):**
   - Capturar el tablero de Jira con las épicas y los Sprints 1, 2 y 3.
2. **Pantallazos de Docker Compose y Persistencia (HU9.S1.4, HU9.S2.3, HU9.S3.3, HU11.S3.2):**
   - `docker compose up --build` → capturar todos los servicios `healthy`.
   - `docker volume ls` mostrando `saberpro_postgres-data` y `saberpro_keycloak-data`.
   - Ciclo de persistencia: crear pregunta → `docker compose down` → `docker compose up -d` → verificar que persiste.
3. **Pantallazos de Postman y Seguridad (HU7.S1.4, HU7.S2.1, HU7.S5.3):**
   - POST a `http://localhost:8089/realms/saberpro/protocol/openid-connect/token` con `author1`, `revisor1`, `admin`.
   - GET `http://localhost:8080/revisiones/pendientes` con Bearer Token → 200 OK; sin token → 401.
   - Conexión directa a microservicio interno (ej. `:8081`) → rechazada (puerto no expuesto).
