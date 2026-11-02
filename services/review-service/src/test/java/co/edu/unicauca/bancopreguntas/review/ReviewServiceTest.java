package co.edu.unicauca.bancopreguntas.review;

import co.edu.unicauca.bancopreguntas.contracts.dto.DecisionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaAprobadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaRechazadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.RevisoresAsignadosEvent;
import co.edu.unicauca.bancopreguntas.review.client.LifecycleServiceClient;
import co.edu.unicauca.bancopreguntas.review.client.QuestionsServiceClient;
import co.edu.unicauca.bancopreguntas.review.client.UsersServiceClient;
import co.edu.unicauca.bancopreguntas.review.domain.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.EvaluacionRevisorEntity;
import co.edu.unicauca.bancopreguntas.review.domain.ObservacionEntity;
import co.edu.unicauca.bancopreguntas.review.domain.RevisionEntity;
import co.edu.unicauca.bancopreguntas.review.messaging.ReviewEventPublisher;
import co.edu.unicauca.bancopreguntas.review.repository.EvaluacionRevisorRepository;
import co.edu.unicauca.bancopreguntas.review.repository.ObservacionRepository;
import co.edu.unicauca.bancopreguntas.review.repository.RevisionRepository;
import co.edu.unicauca.bancopreguntas.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

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
        @Override public Page<RevisionEntity> findByRevisorId(Long rId, Pageable pageable) { return null; }
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
        @Override public void deleteAll() { map.clear(); }
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
        @Override public List<EvaluacionRevisorEntity> findByRevisionId(Long rId) { return list; }
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
        @Override public void actualizarProyeccionEstado(Long id, String nuevoEstado) {
            if (questions.containsKey(id)) questions.get(id).setEstado(nuevoEstado);
        }
    }

    static class StubLifecycleClient extends LifecycleServiceClient {
        EstadoPreguntaDTO estado = EstadoPreguntaDTO.PENDIENTE_REVISION;
        public StubLifecycleClient() { super((org.springframework.web.client.RestTemplate) null, "http://dummy"); }
        @Override public EstadoPreguntaDTO obtenerEstado(Long id) { return estado; }
        @Override public void aplicarDecision(Long id, String dec, Long u, String e, String c, List<String> obs, String corr) {
            estado = "APROBADA".equalsIgnoreCase(dec) ? EstadoPreguntaDTO.APROBADA : EstadoPreguntaDTO.RECHAZADA;
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
        List<RevisionEntity> creadas = reviewService.asignarRevisores(100L, List.of(6L), 1L, "corr-2");
        assertEquals(1, creadas.size());
        assertEquals(1, capturedEvents.size());
        assertTrue(capturedEvents.get(0) instanceof RevisoresAsignadosEvent);
    }

    @Test
    @DisplayName("HU4 - Regla obligatoria: 3 revisores permitidos")
    void asignarRevisores_tresRevisores_permitido() {
        List<RevisionEntity> creadas = reviewService.asignarRevisores(100L, List.of(6L, 10L, 14L), 1L, "corr-3");
        assertEquals(3, creadas.size());
        assertEquals(1, capturedEvents.size());
    }

    @Test
    @DisplayName("HU4 - Regla obligatoria: 4 revisores da Error 400")
    void asignarRevisores_cuatroRevisores_lanzaError400() {
        assertThrows(IllegalArgumentException.class, () ->
            reviewService.asignarRevisores(100L, List.of(6L, 10L, 14L, 99L), 1L, "corr-4")
        );
    }

    @Test
    @DisplayName("HU4 - Exclusión del autor de su propia pregunta")
    void asignarRevisores_autorAsignado_lanzaError() {
        // Intentar asignar al autor (ID 3) como revisor
        assertThrows(IllegalArgumentException.class, () ->
            reviewService.asignarRevisores(100L, List.of(3L), 1L, "corr-5")
        );
    }

    @Test
    @DisplayName("HU5 - SSCRUM-65: Rechazo exige al menos una observación no vacía")
    void registrarDecision_rechazarSinObservaciones_lanzaError() {
        // Asignar revisor 6
        reviewService.asignarRevisores(100L, List.of(6L), 1L, "corr-6");
        Long revId = revRepo.map.keySet().iterator().next();

        DecisionRequestDTO dec = new DecisionRequestDTO("RECHAZAR", Collections.emptyList(), null);

        assertThrows(IllegalArgumentException.class, () ->
            reviewService.registrarDecision(revId, dec, 6L, "corr-6")
        );
    }

    @Test
    @DisplayName("HU5 - SSCRUM-65: Rechazo con observación exitoso y publicación de evento")
    void registrarDecision_rechazarConObservacion_exitoso() {
        reviewService.asignarRevisores(100L, List.of(6L), 1L, "corr-7");
        Long revId = revRepo.map.keySet().iterator().next();

        DecisionRequestDTO dec = new DecisionRequestDTO("RECHAZAR", List.of("La justificación carece de rigor"), "Rechazo motivado");

        RevisionEntity rev = reviewService.registrarDecision(revId, dec, 6L, "corr-7");

        assertEquals(EstadoRevision.RECHAZADA, rev.getEstado());
        assertTrue(capturedEvents.stream().anyMatch(e -> e instanceof PreguntaRechazadaEvent));
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
}
