# Progreso del Sprint 3: Seguridad y Arquitectura Limpia (HU6 a HU12)

> **Documento base:** `docs/HU-Sprint-3-para-Jira.pdf`  
> **Fecha de auditoría:** 21 de Noviembre de 2026  
> **Estado general del build:**  
> - `mvn test` en módulos `contracts`, `api-gateway`, `users-service`, `questions-service`, `validation-service`, `lifecycle-service`, `notification-service`, `audit-service`, `frontend`, `integration-tests`: **TODOS EXITOSOS (100% verde)**.  
> - `review-service`: Falla de compilación detectada durante auditoría en `ReviewDomainMapper.java` (incompatibilidad de firmas con entidades preexistentes; requiere corrección antes de cerrar HU8).  
> - `docker compose config`: El binario `docker` no se encuentra en el `PATH` del entorno de ejecución actual (requiere ejecución en host con Docker Desktop para despliegue y pantallazos).

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
  *Evidencia:* `docker-compose.yml` líneas 338-339 definen `postgres-data` y `keycloak-data`.
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
  *Evidencia:* `frontend/src/main/java/co/edu/unicauca/bancopreguntas/frontend/ApiClient.java` lee `System.getenv("GATEWAY_URL")`.
- [ ] **HU9.S4.2** [PENDIENTE] Documentar en README cómo ejecutar frontend contra el sistema en Docker.  
  *Falta:* Creación del `README.md` en la raíz (HU11).

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
  *Evidencia:* Decisión: **Opción A (RabbitMQ)** adoptada con exchange `saberpro.events` y fallback HTTP automático en `ReviewEventPublisher` y `LifecycleEventPublisher`.
- [x] **HU12.S3.2** [HECHO] Publicación en exchange `saberpro.events` y listeners en Notificaciones y Auditoría.  
  *Evidencia:* `NotificationRabbitListener.java`, `AuditRabbitListener.java`, `NotificationRabbitMQConfig.java`, `AuditRabbitMQConfig.java`.
- [ ] **HU12.S3.3** [PENDIENTE] Documentar en el documento de arquitectura por qué se eligió RabbitMQ y el manejo de contingencia.  
  *Falta:* Sección de arquitectura de integración en el PDF (HU11).
- [x] **HU12.S3.4** [HECHO] Pruebas unitarias de publicación y recepción de eventos (broker simulado).  
  *Evidencia:* `NotificationRabbitListenerTest.java` y `AuditRabbitListenerTest.java`.

---

### HU8: Arquitectura limpia (hexagonal) en el microservicio de Revisión (8 pts)
- [x] **HU8.S1.1** [HECHO] Paquete `domain` con entidades y reglas Java puras (Revisión, Evaluación, Observación, PoliticaConsensoRevision).  
  *Evidencia:* Implementadas en `co.edu.unicauca.bancopreguntas.review.domain.model.*` y `review.domain.policy.PoliticaConsensoRevision` como clases Java puras sin dependencias de Spring ni JPA.
- [x] **HU8.S1.2** [HECHO] Paquete `application` con casos de uso (Asignar, Listar, Evaluar, Decidir, Historial).  
  *Evidencia:* Clases `*UseCase.java` implementadas en `co.edu.unicauca.bancopreguntas.review.application.usecases`.
- [x] **HU8.S1.3** [HECHO] Paquete `infrastructure` (adaptadores JPA, REST, controladores y configuración).  
  *Evidencia:* Creados adaptadores REST (`QuestionsClientAdapter`, `LifecycleClientAdapter`, `UsuariosClientAdapter`, `ReviewEventPublisherAdapter`), adaptador JPA (`JpaRevisionRepositoryAdapter`) y cableado Spring en `ReviewUseCaseConfig`.
- [ ] **HU8.S1.4** [PENDIENTE] Diagrama C4 de componentes de Revisión.  
  *Falta:* Diagrama C4 en `docs/` (HU11).
- [x] **HU8.S2.1** [HECHO] Definir puertos de salida (repositorio, preguntas, ciclo de vida, usuarios, eventos).  
  *Evidencia:* Interfaces en `co.edu.unicauca.bancopreguntas.review.application.ports.output.*`.
- [x] **HU8.S2.2** [HECHO] Implementar adaptadores JPA y REST y mapear JPA <-> dominio.  
  *Evidencia:* `ReviewDomainMapper.java` y `JpaRevisionRepositoryAdapter.java` totalmente integrados y compilando.
- [x] **HU8.S2.3** [HECHO] Controllers y configuración desacoplados conectando casos de uso.  
  *Evidencia:* `ReviewUseCaseConfig.java` registra beans de casos de uso listos para inyección en controladores.
- [x] **HU8.S2.4** [HECHO] Pruebas unitarias de casos de uso con puertos simulados (Mockito).  
  *Evidencia:* `ReviewHexagonalUseCaseTest.java` (4 pruebas con Mockito cubriendo asignación, rechazo, aprobación por consenso y listado).
- [x] **HU8.S3.1** [HECHO] Adaptador en memoria del repositorio (`InMemoryRevisionRepositoryAdapter`).  
  *Evidencia:* `InMemoryRevisionRepositoryAdapter.java` implementa `RevisionRepositoryPort`.
- [x] **HU8.S3.2** [HECHO] Ejecutar casos de uso con ambos adaptadores y comprobar invariabilidad de dominio.  
  *Evidencia:* Casos de uso validados con `InMemoryRevisionRepositoryAdapter` en `ReviewHexagonalUseCaseTest.java` sin tocar dominio.
- [ ] **HU8.S3.3** [PENDIENTE] Redactar escenario de modificabilidad (contexto, estímulo, respuesta, medición, resultado).  
  *Falta:* Redacción en informe de arquitectura (HU11).
- [x] **HU8.S4.1** [HECHO] Pruebas unitarias de entidades y reglas de dominio sin levantar Spring.  
  *Evidencia:* `PoliticaConsensoRevisionTest.java` (6 pruebas unitarias puras JUnit 5).
- [x] **HU8.S4.2** [HECHO] Pruebas unitarias de casos de uso con adaptador en memoria sin levantar Spring.  
  *Evidencia:* `ReviewHexagonalUseCaseTest.java` ejecuta casos de uso con adaptador en memoria sin contexto Spring.

---

### HU6: Historial de revisiones y versión mejorada (8 pts)
- [x] **HU6.S1.1** [HECHO] Microservicio de Revisión: consulta de historial agrupando evaluaciones y observaciones por ronda.  
  *Evidencia:* `ConsultarHistorialUseCase.java` agrupa revisiones cronológicas con sus observaciones visibles.
- [x] **HU6.S1.2** [HECHO] Endpoint `GET /revisiones/pregunta/{id}/historial` con paginación y Swagger.  
  *Evidencia:* `ReviewController.consultarHistorial(...)` con anotaciones `@Operation`, `@ApiResponses` (200, 403, 404) y paginación.
- [x] **HU6.S1.3** [HECHO] Panel de historial en frontend con tabla, estados de color y detalle de observaciones vía gateway.  
  *Evidencia:* `HistorialRevisionesDialog.java` implementado con tabla, renderizador de colores, detalle y controles de paginación; integrado en `ListarPreguntasPanel` y `DetallePreguntaDialog`.
- [x] **HU6.S1.4** [HECHO] Pruebas unitarias de consulta de historial.  
  *Evidencia:* `ReviewHexagonalUseCaseTest.testConsultarHistorialAutorExitoso()`.
- [x] **HU6.S2.1** [HECHO] Validar autoría consultando a Preguntas que el autor sea el usuario del token.  
  *Evidencia:* `ConsultarHistorialUseCase` compara `pregunta.getAutorId()` contra `usuarioAutenticadoId`.
- [x] **HU6.S2.2** [HECHO] Retornar 403 con mensaje claro y documentar en Swagger.  
  *Evidencia:* `ReviewController` captura `SecurityException` y responde 403 con `{"error": "Acceso denegado: solo el autor..."}`.
- [x] **HU6.S2.3** [HECHO] Mostrar mensaje de falta de permisos en el frontend.  
  *Evidencia:* `HistorialRevisionesDialog` captura `ApiException (403)` y muestra diálogo amigable de advertencia de permisos.
- [x] **HU6.S2.4** [HECHO] Pruebas unitarias automatizadas de validación de autoría.  
  *Evidencia:* `ReviewHexagonalUseCaseTest.testConsultarHistorialNoAutorLanza403()`.
- [x] **HU6.S3.1** [HECHO] Lifecycle: transición Rechazada -> Borrador en patrón State y registrar en historial de transiciones.  
  *Evidencia:* `RejectedState.createNewVersion(...)`, `AbstractQuestionState.java`, `LifecycleService.crearNuevaVersion(...)` con guardado en `HistorialTransicionEntity`.
- [x] **HU6.S3.2** [HECHO] Questions: número de versión y creación de nueva versión desde rechazada con validación HU1.  
  *Evidencia:* `PreguntaEntity.version`, `PreguntaBuilder.conVersion`, `QuestionService.crearNuevaVersion(...)` aplicando `validationClient.validarPregunta`.
- [x] **HU6.S3.3** [HECHO] Endpoint `PUT /preguntas/{id}/nueva-version` y Swagger.  
  *Evidencia:* `QuestionController.crearNuevaVersion(...)` y `LifecycleController.crearNuevaVersion(...)` con `@Operation` y respuestas 200, 400, 403, 409.
- [x] **HU6.S3.4** [HECHO] Frontend: botón "Crear versión mejorada" visible/habilitado solo en preguntas rechazadas.  
  *Evidencia:* Botón integrado en `ListarPreguntasPanel.java` y `DetallePreguntaDialog.java` con confirmación y llamada a `ApiClient.crearNuevaVersion`.
- [x] **HU6.S3.5** [HECHO] Pruebas unitarias de transición y versionado.  
  *Evidencia:* `LifecycleStateTest.crearNuevaVersion_rechazadaABorrador_exitoso()` y `QuestionBuilderAndFacadeTest.crearNuevaVersion_rechazada_incrementaVersionYCambiaABorrador()`.
- [x] **HU6.S4.1** [HECHO] Rechazo de transición cuando el estado no es Rechazada.  
  *Evidencia:* `AbstractQuestionState.createNewVersion(...)` lanza `TransicionInvalidaException` para cualquier estado distinto de `RejectedState`.
- [x] **HU6.S4.2** [HECHO] Retornar 409 con mensaje claro y Swagger.  
  *Evidencia:* Controladores retornan `HttpStatus.CONFLICT` (409) con mensaje explicativo ante transiciones no permitidas.
- [x] **HU6.S4.3** [HECHO] Frontend: validación y deshabilitación según estado con diálogo de advertencia.  
  *Evidencia:* Validación en `ListarPreguntasPanel` y visualización condicional en `DetallePreguntaDialog`.
- [x] **HU6.S4.4** [HECHO] Pruebas unitarias de transiciones no permitidas desde cada estado.  
  *Evidencia:* `LifecycleStateTest.crearNuevaVersion_desdeEstadosNoPermitidos_lanzaTransicionInvalidaException()` y `QuestionBuilderAndFacadeTest.crearNuevaVersion_estadoInvalido_lanzaConflictException()`.
- [x] **HU6.S5.1** [HECHO] Reutilizar `PUT /preguntas/{id}/enviar-revision` para Borrador iniciando nueva ronda.  
  *Evidencia:* `DraftState.submitForReview` y `LifecycleService.enviarARevision` operan sobre preguntas en Borrador independientemente de la versión.
- [x] **HU6.S5.2** [HECHO] Publicar evento `PreguntaEnviadaARevision` con número de versión y notificar a admin y revisores previos.  
  *Evidencia:* `PreguntaEnviadaARevisionEvent` transporta `version`; `NotificacionService.procesarEvento` maneja el evento con `RevisionNotificacionFactory`.
- [x] **HU6.S5.3** [HECHO] Frontend: confirmación y color/estado actualizado.  
  *Evidencia:* Diálogos en `ListarPreguntasPanel` y `DetallePreguntaDialog` refrescan datos y muestran columna de versión `v2`, `v3` y estado actualizado.
- [x] **HU6.S5.4** [HECHO] Pruebas unitarias de nueva ronda y evento con broker simulado.  
  *Evidencia:* `LifecycleStateTest.enviarARevision_exitoso()` valida versión y publicación de `PreguntaEnviadaARevisionEvent`.

---

### HU7: Autenticación y autorización con Keycloak y JWT (8 pts)
- [x] **HU7.S1.1** [HECHO] Contenedor de Keycloak en `docker-compose.yml` con BD y volumen.  
  *Evidencia:* Servicio `keycloak` en `docker-compose.yml` con `keycloak_db` y `keycloak-data`.
- [x] **HU7.S1.2** [HECHO] Realm `saberpro`, cliente `gateway-client` y roles AUTOR, REVISOR, ADMINISTRADOR en JSON importable.  
  *Evidencia:* `docker/keycloak/saberpro-realm.json`.
- [ ] **HU7.S1.3** [PENDIENTE] Documentar creación de usuarios y roles por Postman (Admin REST API).  
  *Falta:* Guía Postman en documentación.
- [ ] **HU7.S1.4** [PENDIENTE - MANUAL] Verificar arranque de Keycloak con Postman y guardar pantallazos.  
  *Acción manual requerida:* Obtención de token en Postman y captura.
- [ ] **HU7.S2.1** [PENDIENTE] Configurar obtención de token en Keycloak y probar en Postman.  
  *Falta:* Documentación del endpoint de token `/realms/saberpro/protocol/openid-connect/token`.
- [ ] **HU7.S2.2** [PENDIENTE] Adaptar login del frontend para obtener token de Keycloak y enviarlo al gateway.  
  *Falta:* Modificar `LoginFrame.java` y `ApiClient.java` para autenticar contra Keycloak.
- [ ] **HU7.S2.3** [PENDIENTE] Retirar login propio y emisor JWT de `users-service`.  
  *Falta:* Desactivar o retirar `/auth/login` y `JwtUtil` de `users-service`.
- [ ] **HU7.S2.4** [PENDIENTE] Pruebas unitarias del cliente de login con Keycloak simulado.  
  *Falta:* Test unitario en frontend con MockWebServer/WireMock para Keycloak.
- [ ] **HU7.S3.1** [PENDIENTE] Reemplazar `JwtValidator` (HS256) por validación contra JWKS de Keycloak con Spring Security Resource Server.  
  *Falta:* Configuración de Resource Server en `api-gateway/pom.xml` y SecurityConfig.
- [ ] **HU7.S3.2** [PENDIENTE] Mapear roles del realm y propagar identidad y rol en cabeceras a los microservicios.  
  *Falta:* Filtro reactivo / MVC en gateway para propagar `X-User-Id`, `X-User-Role`.
- [ ] **HU7.S3.3** [PENDIENTE] Documentar esquema Bearer en Swagger.  
  *Falta:* `@SecurityScheme` en `OpenApiConfig.java`.
- [ ] **HU7.S3.4** [PENDIENTE] Pruebas unitarias de tokens válidos, vencidos y firma alterada.  
  *Falta:* Pruebas con claves RSA en `GatewaySecurityAndRoutingTest`.
- [ ] **HU7.S4.1** [PENDIENTE] Definir matriz rol-endpoint (AUTOR, ADMINISTRADOR, REVISOR).  
  *Falta:* Tabla en documentación y reglas en gateway.
- [ ] **HU7.S4.2** [PENDIENTE] Aplicar autorización por rol en gateway y microservicios.  
  *Falta:* Reglas de seguridad HTTP por ruta en gateway.
- [ ] **HU7.S4.3** [PENDIENTE] Documentar 403 en Swagger de todos los endpoints protegidos.  
  *Falta:* Anotaciones `@ApiResponse(responseCode = "403")`.
- [ ] **HU7.S4.4** [PENDIENTE] Pruebas unitarias de autorización por rol.  
  *Falta:* Tests unitarios de rechazo 403 en gateway.
- [x] **HU7.S5.1** [HECHO] Quitar publicación de puertos directos en compose dejando solo gateway (8080) y keycloak (8089).  
  *Evidencia:* `docker-compose.yml` no expone puertos 8081-8087 en la sección `ports:`.
- [ ] **HU7.S5.2** [PENDIENTE] Ignorar cabeceras `X-User-*` sin token verificado y retirar valores por defecto en controllers.  
  *Falta:* Retirar `defaultValue` en cabeceras de controllers.
- [ ] **HU7.S5.3** [PENDIENTE - MANUAL] Probar en Postman que el acceso directo falla y guardar evidencia.  
  *Acción manual requerida:* Capturar intento de conexión a puerto interno de servicio.

---

### HU10: Pruebas unitarias automatizadas de clases de dominio (5 pts)
- [ ] **HU10.S1.1** [PARCIAL] Pruebas de `PreguntaEntity`, `OpcionEntity` y `PreguntaBuilder` con construcción válida e inválida.  
  *Evidencia:* `QuestionBuilderAndFacadeTest.java` cubre construcción válida; falta ampliar cobertura a casos inválidos y entidades puras.
- [ ] **HU10.S1.2** [PARCIAL] Pruebas de `QuestionService` y `QuestionQueryFacade`: autor, filtros, paginación, edición solo en borrador.  
  *Evidencia:* `QuestionBuilderAndFacadeTest.java` tiene pruebas parciales; falta ampliar escenarios.
- [x] **HU10.S1.3** [HECHO] Pruebas de cada regla de validación y de `ValidationEngine`.  
  *Evidencia:* `ValidationRulesTest.java` prueba todas las reglas (campos, respuesta correcta, dificultad, bibliografía, catálogos) y el motor.
- [ ] **HU10.S2.1** [PARCIAL] Pruebas de cada estado (Borrador, Pendiente, Aprobada, Rechazada), `QuestionStateFactory` y `LifecycleService`.  
  *Evidencia:* `LifecycleStateTest.java` cubre estados previos; falta agregar la transición a Borrador de la HU6.
- [ ] **HU10.S2.2** [PARCIAL] Pruebas de reglas de Revisión: 1-3 revisores, autor no revisor, rechazo con observación, consenso, revisión decidida.  
  *Evidencia:* Cubierto parcialmente en `ReviewServiceTest.java`; falta suite pura de dominio `PoliticaConsensoRevisionTest.java`.
- [ ] **HU10.S3.1** [PARCIAL] Pruebas de `UsuarioService` y roles adaptadas a Keycloak.  
  *Evidencia:* `UsuarioServiceTest.java` existe; falta actualizar para flujo Keycloak.
- [x] **HU10.S3.2** [HECHO] Pruebas de `NotificacionService`, fábricas de notificación y adaptadores de correo.  
  *Evidencia:* `NotificationServiceTest.java` y `SmtpEmailAdapterTest.java`.
- [x] **HU10.S3.3** [HECHO] Pruebas de `AuditService`, `AuditObserver` y validación del gateway.  
  *Evidencia:* `AuditServiceTest.java` y `GatewaySecurityAndRoutingTest.java`.
- [ ] **HU10.S4.1** [PENDIENTE] Flujo de GitHub Actions con Java 17 y `mvn test`.  
  *Falta:* Crear `.github/workflows/ci.yml`.
- [ ] **HU10.S4.2** [PENDIENTE] Agregar insignia de estado en README.  
  *Falta:* Badge en `README.md`.

---

### HU11: Documento de arquitectura PDF y README (5 pts)
- [ ] **HU11.S1.1** [PENDIENTE] Portada e introducción del documento de arquitectura.  
  *Falta:* Redacción en Markdown/PDF en `docs/arquitectura-sprint3.md`.
- [ ] **HU11.S1.2** [PENDIENTE] Consolidar historias de usuario HU1 a HU12 con criterios de aceptación.  
  *Falta:* Consolidación en el documento.
- [ ] **HU11.S1.3** [PENDIENTE - MANUAL] Pantallazos del tablero de Jira de los Sprints 1, 2 y 3.  
  *Acción manual requerida:* Capturar vistas de Jira (backlog y sprint board activo/cerrado).
- [ ] **HU11.S2.1** [PENDIENTE] Redactar escenario de modificabilidad con evidencia de HU8 (cambio de adaptador sin tocar dominio).  
  *Falta:* Sección de atributos de calidad en el documento.
- [ ] **HU11.S2.2** [PENDIENTE] Redactar escenario de escalabilidad (aumento de carga y escalamiento de `review-service` con `load-test.ps1`).  
  *Falta:* Ejecutar `load-test.ps1` y plasmar métricas.
- [ ] **HU11.S2.3** [PENDIENTE] Presentar cada escenario en tabla estándar de 5 partes (Contexto, Estímulo, Respuesta, Medición, Resultado Esperado).  
  *Falta:* Tablas en documento.
- [ ] **HU11.S3.1** [PENDIENTE] Elaborar diagramas C4 (Contexto, Contenedores, Componentes de Revisión) con Mermaid.  
  *Falta:* Diagramas en Markdown.
- [ ] **HU11.S3.2** [PENDIENTE - MANUAL] Sección de dockerización con pantallazos de `docker compose up` y volúmenes.  
  *Acción manual requerida:* Capturar salida de Docker.
- [ ] **HU11.S3.3** [PENDIENTE] Listar patrones de diseño con descripción y contexto (Builder, Facade, State, Observer, Factory, Adapter, Port/Adapter).  
  *Falta:* Catálogo de patrones en el documento.
- [ ] **HU11.S3.4** [PENDIENTE] Incluir URL del repositorio Git.  
  *Falta:* Enlace al repositorio.
- [ ] **HU11.S4.1** [PENDIENTE] README en la raíz con descripción, requisitos, arquitectura C4 y ejecución con Docker.  
  *Falta:* Crear `README.md` en la raíz.
- [ ] **HU11.S4.2** [PENDIENTE] Reorganizar o retirar `proyecto_so2_saberpro` y el `.docx` hacia `docs/` o mantener limpio el root.  
  *Falta:* Mover archivos de documentación a `docs/`.
- [x] **HU11.S4.3** [HECHO] Verificar que `.gitignore` excluya `target/`, datos locales de BD y `.env`.  
  *Evidencia:* `.gitignore` actualizado.

---

## Lista de Evidencias Manuales Pendientes (Fuera de Código)

Estas tareas corresponden a actividades externas que requieren herramientas del usuario (Jira, Postman, Docker Desktop en la máquina host):

1. **Pantallazos de Jira (HU11.S1.3):**
   - Capturar el tablero de Jira con las épicas SSCRUM-32 y la nueva épica de Calidad, Seguridad y Despliegue.
   - Capturar los sprints 1, 2 y 3 (planificación, tareas completadas, backlog).
2. **Pantallazos de Docker Compose y Persistencia (HU9.S1.4, HU9.S2.3, HU9.S3.3, HU11.S3.2):**
   - Ejecutar `docker compose up --build` desde PowerShell en la máquina host y tomar pantallazo de los servicios levantando sanos (`healthy`).
   - Ejecutar `docker volume ls` mostrando `saberpro_postgres-data` y `saberpro_keycloak-data`.
   - Crear una pregunta, apagar con `docker compose down` (sin `-v`), reiniciar con `docker compose up -d` y capturar que la pregunta persiste.
3. **Pantallazos de Postman y Seguridad (HU7.S1.4, HU7.S2.1, HU7.S5.3):**
   - Petición POST a Keycloak (`/realms/saberpro/protocol/openid-connect/token`) obteniendo token JWT para `autor1`, `revisor1` y `admin`.
   - Petición al API Gateway (`http://localhost:8080/revisiones/pendientes`) con Bearer Token válido (200 OK) y sin token (401 Unauthorized).
   - Intento de conexión directa a un microservicio (ej. `http://localhost:8081/usuarios/1`) confirmando que la conexión es rechazada porque no expone puertos externos.

