package co.edu.unicauca.bancopreguntas.review;

import co.edu.unicauca.bancopreguntas.contracts.dto.DecisionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaAprobadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaRechazadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.RabbitMQConstants;
import co.edu.unicauca.bancopreguntas.contracts.events.RevisoresAsignadosEvent;
import co.edu.unicauca.bancopreguntas.review.client.LifecycleServiceClient;
import co.edu.unicauca.bancopreguntas.review.client.QuestionsServiceClient;
import co.edu.unicauca.bancopreguntas.review.client.UsersServiceClient;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.EvaluacionRevisorEntity;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.ObservacionEntity;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.RevisionEntity;
import co.edu.unicauca.bancopreguntas.review.messaging.ReviewEventPublisher;
import co.edu.unicauca.bancopreguntas.review.repository.EvaluacionRevisorRepository;
import co.edu.unicauca.bancopreguntas.review.repository.ObservacionRepository;
import co.edu.unicauca.bancopreguntas.review.repository.RevisionRepository;
import co.edu.unicauca.bancopreguntas.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    static class StubRevisionRepository implements RevisionRepository {
        Map<Long, RevisionEntity> map = new HashMap<>();
        long seq = 1;

        @Override
        public <S extends RevisionEntity> S save(S entity) {
            if (entity.getId() == null) entity.setId(seq++);
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override public Optional<RevisionEntity> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public List<RevisionEntity> findByPreguntaId(Long pId) {
            return map.values().stream().filter(r -> r.getPreguntaId().equals(pId)).collect(Collectors.toList());
        }
        @Override public Optional<RevisionEntity> findByPreguntaIdAndRevisorId(Long pId, Long rId) {
            return map.values().stream().filter(r -> r.getPreguntaId().equals(pId) && r.getRevisorId().equals(rId)).findFirst();
        }
        @Override public Optional<RevisionEntity> findByPreguntaIdAndRevisorIdAndRonda(Long pId, Long rId, Integer ronda) {
            int target = (ronda != null && ronda >= 1) ? ronda : 1;
            return map.values().stream()
                    .filter(r -> r.getPreguntaId().equals(pId) && r.getRevisorId().equals(rId) && r.getRonda() == target)
                    .findFirst();
        }
        @Override public Page<RevisionEntity> findByRevisorId(Long rId, Pageable pageable) { return null; }

        @Override
        public Page<RevisionEntity> findByRevisorIdWithFilters(Long rId, EstadoRevision estado, String competencia, String dificultad, Pageable pageable) {
            List<RevisionEntity> filtered = map.values().stream()
                    .filter(r -> r.getRevisorId().equals(rId))
                    .filter(r -> estado == null || r.getEstado() == estado)
                    .filter(r -> competencia == null || (r.getCompetencia() != null && r.getCompetencia().equalsIgnoreCase(competencia)))
                    .filter(r -> dificultad == null || (r.getNivelDificultad() != null && r.getNivelDificultad().equalsIgnoreCase(dificultad)))
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filtered.size());
            List<RevisionEntity> sub = (start <= end && start < filtered.size()) ? filtered.subList(start, end) : Collections.emptyList();
            return new PageImpl<>(sub, pageable, filtered.size());
        }

        @Override public List<RevisionEntity> findByRevisorId(Long rId) {
            return map.values().stream().filter(r -> r.getRevisorId().equals(rId)).collect(Collectors.toList());
        }
        @Override public boolean existsByPreguntaIdAndRevisorId(Long pId, Long rId) {
            return map.values().stream().anyMatch(r -> r.getPreguntaId().equals(pId) && r.getRevisorId().equals(rId));
        }

        @Override public <S extends RevisionEntity> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public boolean existsById(Long aLong) { return map.containsKey(aLong); }
        @Override public List<RevisionEntity> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<RevisionEntity> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) { map.remove(aLong); }
        @Override public void delete(RevisionEntity entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends RevisionEntity> entities) {}
        @Override public void deleteAll() {}
        @Override public void flush() {}
        @Override public <S extends RevisionEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends RevisionEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<RevisionEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public RevisionEntity getOne(Long aLong) { return null; }
        @Override public RevisionEntity getById(Long aLong) { return null; }
        @Override public RevisionEntity getReferenceById(Long aLong) { return null; }
        @Override public <S extends RevisionEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends RevisionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends RevisionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends RevisionEntity> Page<S> findAll(org.springframework.data.domain.Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends RevisionEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends RevisionEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends RevisionEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<RevisionEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public Page<RevisionEntity> findAll(Pageable pageable) { return null; }
    }

    static class StubEvaluacionRepository implements EvaluacionRevisorRepository {
        List<EvaluacionRevisorEntity> list = new ArrayList<>();
        @Override public <S extends EvaluacionRevisorEntity> S save(S entity) { list.add(entity); return entity; }
        @Override public List<EvaluacionRevisorEntity> findByRevisionId(Long revisionId) {
            return list.stream().filter(e -> e.getRevisionId().equals(revisionId)).collect(Collectors.toList());
        }
        @Override public <S extends EvaluacionRevisorEntity> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<EvaluacionRevisorEntity> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<EvaluacionRevisorEntity> findAll() { return list; }
        @Override public List<EvaluacionRevisorEntity> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return list.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(EvaluacionRevisorEntity entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends EvaluacionRevisorEntity> entities) {}
        @Override public void deleteAll() {}
        @Override public void flush() {}
        @Override public <S extends EvaluacionRevisorEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends EvaluacionRevisorEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<EvaluacionRevisorEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public EvaluacionRevisorEntity getOne(Long aLong) { return null; }
        @Override public EvaluacionRevisorEntity getById(Long aLong) { return null; }
        @Override public EvaluacionRevisorEntity getReferenceById(Long aLong) { return null; }
        @Override public <S extends EvaluacionRevisorEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends EvaluacionRevisorEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends EvaluacionRevisorEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends EvaluacionRevisorEntity> Page<S> findAll(org.springframework.data.domain.Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends EvaluacionRevisorEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends EvaluacionRevisorEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends EvaluacionRevisorEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<EvaluacionRevisorEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public Page<EvaluacionRevisorEntity> findAll(Pageable pageable) { return null; }
    }

    static class StubObservacionRepository implements ObservacionRepository {
        List<ObservacionEntity> list = new ArrayList<>();
        @Override public <S extends ObservacionEntity> S save(S entity) { list.add(entity); return entity; }
        @Override public List<ObservacionEntity> findByRevisionId(Long rId) {
            return list.stream().filter(o -> o.getRevisionId().equals(rId)).collect(Collectors.toList());
        }
        @Override public List<ObservacionEntity> findByPreguntaIdAndVisibleAutorTrue(Long pId) {
            return list.stream().filter(o -> o.getPreguntaId().equals(pId)).collect(Collectors.toList());
        }
        @Override public <S extends ObservacionEntity> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<ObservacionEntity> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<ObservacionEntity> findAll() { return list; }
        @Override public List<ObservacionEntity> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return list.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(ObservacionEntity entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends ObservacionEntity> entities) {}
        @Override public void deleteAll() {}
        @Override public void flush() {}
        @Override public <S extends ObservacionEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ObservacionEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<ObservacionEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public ObservacionEntity getOne(Long aLong) { return null; }
        @Override public ObservacionEntity getById(Long aLong) { return null; }
        @Override public ObservacionEntity getReferenceById(Long aLong) { return null; }
        @Override public <S extends ObservacionEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends ObservacionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends ObservacionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends ObservacionEntity> Page<S> findAll(org.springframework.data.domain.Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends ObservacionEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends ObservacionEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends ObservacionEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<ObservacionEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public Page<ObservacionEntity> findAll(Pageable pageable) { return null; }
    }

    static class StubUsersClient extends UsersServiceClient {
        Map<Long, UserSummaryDTO> users = new HashMap<>();
        public StubUsersClient() { super((org.springframework.web.client.RestTemplate) null, "http://dummy"); }
        @Override public Optional<UserSummaryDTO> buscarPorId(Long id) { return Optional.ofNullable(users.get(id)); }
    }

    static class StubQuestionsClient extends QuestionsServiceClient {
        Map<Long, PreguntaDTO> questions = new HashMap<>();
        public StubQuestionsClient() { super((org.springframework.web.client.RestTemplate) null, "http://dummy"); }
        @Override public Optional<PreguntaDTO> obtenerPregunta(Long id) { return Optional.ofNullable(questions.get(id)); }
        @Override public void actualizarProyeccionEstado(Long id, String estado) {}
    }

    static class StubLifecycleClient extends LifecycleServiceClient {
        EstadoPreguntaDTO estado = EstadoPreguntaDTO.PENDIENTE_REVISION;
        String ultimoEstadoAplicado = null;
        String ultimoEmailAutor = null;

        public StubLifecycleClient() { super((org.springframework.web.client.RestTemplate) null, "http://dummy"); }
        @Override public EstadoPreguntaDTO obtenerEstado(Long id) { return estado; }
        @Override public void aplicarDecision(Long preguntaId, String decision, Long revisorId, String emailAutor, String contexto, List<String> observaciones, String correlationId) {
            this.ultimoEstadoAplicado = decision;
            this.ultimoEmailAutor = emailAutor;
        }
    }

    private StubRevisionRepository revRepo;
    private StubEvaluacionRepository evalRepo;
    private StubObservacionRepository obsRepo;
    private StubUsersClient usersClient;
    private StubQuestionsClient questionsClient;
    private StubLifecycleClient lifecycleClient;
    private ReviewEventPublisher eventPublisher;
    private ReviewService reviewService;
    private List<DomainEvent> capturedEvents;

    @BeforeEach
    void setUp() {
        revRepo = new StubRevisionRepository();
        evalRepo = new StubEvaluacionRepository();
        obsRepo = new StubObservacionRepository();
        usersClient = new StubUsersClient();
        questionsClient = new StubQuestionsClient();
        lifecycleClient = new StubLifecycleClient();
        eventPublisher = new ReviewEventPublisher(null);
        capturedEvents = new ArrayList<>();
        eventPublisher.getDomainEventBus().registerObserver(capturedEvents::add);

        reviewService = new ReviewService(revRepo, evalRepo, obsRepo, usersClient, questionsClient, lifecycleClient, eventPublisher);

        // Pre-cargar usuarios
        usersClient.users.put(3L, new UserSummaryDTO(3L, "autor12", "Juan Pérez", "AUTOR", "ACTIVO", "autor.real@unicauca.edu.co"));
        usersClient.users.put(6L, new UserSummaryDTO(6L, "Juanita", "Juana Pérez", "REVISOR", "ACTIVO", "juanita@unicauca.edu.co"));
        usersClient.users.put(10L, new UserSummaryDTO(10L, "Juan6", "Juan García", "REVISOR", "ACTIVO", "juan6@unicauca.edu.co"));
        usersClient.users.put(14L, new UserSummaryDTO(14L, "Juan13", "Tercer Revisor", "REVISOR", "ACTIVO", "juan13@unicauca.edu.co"));
        usersClient.users.put(99L, new UserSummaryDTO(99L, "Cuarto", "Cuarto Revisor", "REVISOR", "ACTIVO", "cuarto@unicauca.edu.co"));

        // Pre-cargar pregunta
        PreguntaDTO p = new PreguntaDTO();
        p.setId(100L);
        p.setAutorId(3L); // Autor es juan12
        p.setAutorNombre("Juan Pérez");
        p.setContexto("Contexto pregunta 100");
        p.setCompetencia("Lectura Crítica");
        p.setNivelDificultad("Medio");
        questionsClient.questions.put(100L, p);
    }

    @Test
    @DisplayName("HU4 - Regla obligatoria de cantidad de revisores: 0 revisores da Error 400")
    void asignarRevisores_ceroRevisores_lanzaError400() {
        assertThrows(IllegalArgumentException.class, () ->
            reviewService.asignarRevisores(100L, Collections.emptyList(), 1L, "corr-1")
        );
    }

    @Test
    @DisplayName("HU4 - Regla obligatoria: 1 revisor permitido")
    void asignarRevisores_unRevisor_permitido() {
        List<RevisionEntity> revs = reviewService.asignarRevisores(100L, List.of(6L), 1L, "corr-2");
        assertEquals(1, revs.size());
        assertEquals(EstadoRevision.ASIGNADA, revs.get(0).getEstado());
        assertEquals("Lectura Crítica", revs.get(0).getCompetencia());
        assertEquals("Medio", revs.get(0).getNivelDificultad());
    }

    @Test
    @DisplayName("HU4 - Regla obligatoria: 3 revisores permitidos (máximo)")
    void asignarRevisores_tresRevisores_permitido() {
        List<RevisionEntity> revs = reviewService.asignarRevisores(100L, List.of(6L, 10L, 14L), 1L, "corr-3");
        assertEquals(3, revs.size());
    }

    @Test
    @DisplayName("HU4 - Regla obligatoria: más de 3 revisores da Error 400")
    void asignarRevisores_cuatroRevisores_lanzaError400() {
        assertThrows(IllegalArgumentException.class, () ->
            reviewService.asignarRevisores(100L, List.of(6L, 10L, 14L, 99L), 1L, "corr-4")
        );
    }

    @Test
    @DisplayName("HU4 - Exclusión del autor: el autor no puede ser asignado como revisor (Error 400)")
    void asignarRevisores_autorComoRevisor_lanzaError400() {
        assertThrows(IllegalArgumentException.class, () ->
            reviewService.asignarRevisores(100L, List.of(3L), 1L, "corr-5") // ID 3 es el autor
        );
    }

    @Test
    @DisplayName("HU4 - Publicación de evento RevisoresAsignados al completar la asignación")
    void asignarRevisores_publicaEvento() {
        reviewService.asignarRevisores(100L, List.of(6L), 1L, "corr-6");
        assertEquals(1, capturedEvents.size());
        assertTrue(capturedEvents.get(0) instanceof RevisoresAsignadosEvent);
        RevisoresAsignadosEvent ev = (RevisoresAsignadosEvent) capturedEvents.get(0);
        assertEquals(100L, ev.getPreguntaId());
        assertEquals(1, ev.getRevisores().size());
        assertEquals("juanita@unicauca.edu.co", ev.getRevisores().get(0).getEmail());
    }

    @Test
    @DisplayName("HU5 - SSCRUM-65: Rechazo sin observaciones lanza IllegalArgumentException (400)")
    void registrarDecision_rechazoSinObservaciones_lanzaIllegalArgumentException() {
        reviewService.asignarRevisores(100L, List.of(6L), 1L, "corr-7");
        Long revId = revRepo.map.keySet().iterator().next();

        DecisionRequestDTO dec = new DecisionRequestDTO("RECHAZAR", Collections.emptyList(), "");

        assertThrows(IllegalArgumentException.class, () ->
            reviewService.registrarDecision(revId, dec, 6L, "corr-7")
        );
    }

    @Test
    @DisplayName("HU5 - SSCRUM-66: Revisor no asignado intentando decidir genera excepción de seguridad (403)")
    void registrarDecision_revisorNoAsignado_lanzaSecurityException() {
        reviewService.asignarRevisores(100L, List.of(6L), 1L, "corr-8");
        Long revId = revRepo.map.keySet().iterator().next();

        DecisionRequestDTO dec = new DecisionRequestDTO("APROBAR", null, "OK");

        assertThrows(SecurityException.class, () ->
            reviewService.registrarDecision(revId, dec, 999L, "corr-8") // Revisor no asignado
        );
    }

    @Test
    @DisplayName("HU5 - SSCRUM-66: No se puede decidir dos veces una revisión ya finalizada (409)")
    void registrarDecision_yaDecidida_lanzaIllegalStateException() {
        reviewService.asignarRevisores(100L, List.of(6L), 1L, "corr-9");
        Long revId = revRepo.map.keySet().iterator().next();

        DecisionRequestDTO dec = new DecisionRequestDTO("APROBAR", null, "Aprobado 1");
        reviewService.registrarDecision(revId, dec, 6L, "corr-9");

        // Intentar decidir nuevamente
        assertThrows(IllegalStateException.class, () ->
            reviewService.registrarDecision(revId, dec, 6L, "corr-9")
        );
    }

    @Test
    @DisplayName("HU5 - Consenso con múltiples revisores: pasa a Aprobada solo cuando todos aprueban")
    void registrarDecision_consensoMultiplesRevisores() {
        reviewService.asignarRevisores(100L, List.of(6L, 10L), 1L, "corr-10");

        List<RevisionEntity> revs = revRepo.findByPreguntaId(100L);
        assertEquals(2, revs.size());

        RevisionEntity rev1 = revs.get(0);
        RevisionEntity rev2 = revs.get(1);

        // Primer revisor aprueba
        reviewService.registrarDecision(rev1.getId(), new DecisionRequestDTO("APROBAR", null, "Bien"), rev1.getRevisorId(), "corr-10");
        // Aún falta el segundo revisor: no debe emitir PreguntaAprobadaEvent todavía
        assertFalse(capturedEvents.stream().anyMatch(e -> e instanceof PreguntaAprobadaEvent));

        // Segundo revisor aprueba
        reviewService.registrarDecision(rev2.getId(), new DecisionRequestDTO("APROBAR", null, "Muy bien"), rev2.getRevisorId(), "corr-10");
        // Ahora ambos aprobaron: debe emitirse PreguntaAprobadaEvent
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof PreguntaAprobadaEvent));
    }

    @Test
    @DisplayName("HU12 - Subtarea 1: Correo del autor se obtiene desde UsersServiceClient y no usa valor fijo")
    void testObtenerCorreoAutorDesdeUsersService() {
        reviewService.asignarRevisores(100L, List.of(6L), 1L, "corr-email-test");
        Long revId = revRepo.map.keySet().iterator().next();

        DecisionRequestDTO dec = new DecisionRequestDTO("RECHAZAR", List.of("Corrección en enunciado"), "Observación");
        reviewService.registrarDecision(revId, dec, 6L, "corr-email-test");

        assertEquals("autor.real@unicauca.edu.co", lifecycleClient.ultimoEmailAutor);

        Optional<DomainEvent> rechazoEventOpt = capturedEvents.stream()
                .filter(e -> e instanceof PreguntaRechazadaEvent)
                .findFirst();
        assertTrue(rechazoEventOpt.isPresent());
        PreguntaRechazadaEvent rechazoEvent = (PreguntaRechazadaEvent) rechazoEventOpt.get();
        assertEquals("autor.real@unicauca.edu.co", rechazoEvent.getAutorEmail());
    }

    @Test
    @DisplayName("HU12 - Subtarea 2: Listar asignadas con filtros de estado, dificultad y competencia antes de paginar")
    void testListarAsignadasConFiltrosYPaginacion() {
        // Crear 4 revisiones para el revisor 6L
        RevisionEntity r1 = new RevisionEntity(101L, 6L, "Juana", "juanita@unicauca.edu.co", "Matemáticas", "Medio");
        r1.setEstado(EstadoRevision.ASIGNADA);
        revRepo.save(r1);

        RevisionEntity r2 = new RevisionEntity(102L, 6L, "Juana", "juanita@unicauca.edu.co", "Matemáticas", "Alto");
        r2.setEstado(EstadoRevision.EVALUADA);
        revRepo.save(r2);

        RevisionEntity r3 = new RevisionEntity(103L, 6L, "Juana", "juanita@unicauca.edu.co", "Lectura", "Medio");
        r3.setEstado(EstadoRevision.ASIGNADA);
        revRepo.save(r3);

        RevisionEntity r4 = new RevisionEntity(104L, 6L, "Juana", "juanita@unicauca.edu.co", "Matemáticas", "Bajo");
        r4.setEstado(EstadoRevision.APROBADA);
        revRepo.save(r4);

        PreguntaDTO pSample = new PreguntaDTO();
        pSample.setContexto("Contexto demo");
        questionsClient.questions.put(101L, pSample);
        questionsClient.questions.put(102L, pSample);
        questionsClient.questions.put(103L, pSample);
        questionsClient.questions.put(104L, pSample);

        // Filtro por Estado = ASIGNADA
        Map<String, Object> resEstado = reviewService.listarAsignadas(6L, 0, 10, "ASIGNADA", null, null);
        assertEquals(2L, resEstado.get("totalElements"));

        // Filtro por Competencia = Matemáticas y Dificultad = Medio
        Map<String, Object> resCompDif = reviewService.listarAsignadas(6L, 0, 10, null, "Medio", "Matemáticas");
        assertEquals(1L, resCompDif.get("totalElements"));

        // Paginación con page=0, size=1
        Map<String, Object> resPaginado = reviewService.listarAsignadas(6L, 0, 1, "ASIGNADA", null, null);
        List<?> content = (List<?>) resPaginado.get("content");
        assertEquals(1, content.size());
        assertEquals(2L, resPaginado.get("totalElements"));
        assertEquals(2, resPaginado.get("totalPages"));
    }

    @Test
    @DisplayName("HU12 - Subtarea 3: Publicación en broker RabbitMQ mediante RabbitTemplate en exchange saberpro.events")
    void testPublicacionRabbitMQ() {
        RabbitTemplate mockRabbitTemplate = mock(RabbitTemplate.class);
        ReviewEventPublisher publisherWithRabbit = new ReviewEventPublisher(null, mockRabbitTemplate, null, "http://localhost:8086", "http://localhost:8087");

        PreguntaAprobadaEvent event = new PreguntaAprobadaEvent(
                999L, 3L, "Autor", "autor.real@unicauca.edu.co", "Contexto", "Aprobada", "corr-rmq"
        );

        publisherWithRabbit.publish(event);

        verify(mockRabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConstants.EXCHANGE_NAME),
                eq("saberpro.PreguntaAprobada"),
                anyString()
        );
    }
}
