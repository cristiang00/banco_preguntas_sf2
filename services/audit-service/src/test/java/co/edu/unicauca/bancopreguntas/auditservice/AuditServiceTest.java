package co.edu.unicauca.bancopreguntas.auditservice;

import co.edu.unicauca.bancopreguntas.auditservice.model.RegistroAuditoriaEntity;
import co.edu.unicauca.bancopreguntas.auditservice.repository.AuditRepository;
import co.edu.unicauca.bancopreguntas.auditservice.service.AuditObserver;
import co.edu.unicauca.bancopreguntas.auditservice.service.AuditService;
import co.edu.unicauca.bancopreguntas.contracts.events.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class AuditServiceTest {

    private StubAuditRepository auditRepository;
    private AuditService auditService;
    private AuditObserver auditObserver;

    @BeforeEach
    void setUp() {
        auditRepository = new StubAuditRepository();
        auditService = new AuditService(auditRepository);
        auditObserver = new AuditObserver(auditService);
    }

    @Test
    @DisplayName("Observer: debe auditar evento PreguntaEnviadaARevisionEvent")
    void testAuditarPreguntaEnviada() {
        PreguntaEnviadaARevisionEvent event = new PreguntaEnviadaARevisionEvent(
                101L, 2L, "Juan Autor", "Pregunta de cálculo integral", "¿Cuál es el valor de la integral?", "corr-101"
        );

        auditObserver.onEvent(event);

        List<RegistroAuditoriaEntity> registros = auditService.listarPorPregunta(101L);
        assertEquals(1, registros.size());
        RegistroAuditoriaEntity reg = registros.get(0);
        assertEquals("ENVIO_A_REVISION", reg.getAccion());
        assertEquals("101", reg.getAggregateId());
        assertEquals(2L, reg.getUsuarioId());
        assertEquals("Juan Autor", reg.getUsuarioNombre());
        assertTrue(reg.getDetalles().contains("cálculo integral"));
    }

    @Test
    @DisplayName("Observer: debe auditar evento RevisoresAsignadosEvent con detalle de revisores")
    void testAuditarRevisoresAsignados() {
        List<RevisoresAsignadosEvent.RevisorInfo> revisores = List.of(
                new RevisoresAsignadosEvent.RevisorInfo(4L, "Dr. Revisor 1", "r1@unicauca.edu.co"),
                new RevisoresAsignadosEvent.RevisorInfo(5L, "Dra. Revisor 2", "r2@unicauca.edu.co")
        );
        RevisoresAsignadosEvent event = new RevisoresAsignadosEvent(
                102L, 1L, "Admin General", "Pregunta Física", revisores, "corr-102"
        );

        auditObserver.onEvent(event);

        List<RegistroAuditoriaEntity> registros = auditService.listarPorPregunta(102L);
        assertEquals(1, registros.size());
        RegistroAuditoriaEntity reg = registros.get(0);
        assertEquals("ASIGNACION_REVISORES", reg.getAccion());
        assertTrue(reg.getDetalles().contains("Dr. Revisor 1"));
        assertTrue(reg.getDetalles().contains("Dra. Revisor 2"));
    }

    @Test
    @DisplayName("Observer: debe auditar eventos de decisión (Aprobación y Rechazo)")
    void testAuditarDecisiones() {
        PreguntaAprobadaEvent aprob = new PreguntaAprobadaEvent(
                200L, 3L, "Carlos", "carlos@unicauca.edu.co", "Ingeniería de Software", "Cumple todos los criterios", "corr-aprob"
        );
        PreguntaRechazadaEvent rech = new PreguntaRechazadaEvent(
                201L, 3L, "Carlos", "carlos@unicauca.edu.co", "Estructuras de Datos", List.of("Opción B es ambigua", "Falta bibliografía"), "corr-rech"
        );

        auditObserver.onEvent(aprob);
        auditObserver.onEvent(rech);

        assertEquals(1, auditService.listarPorPregunta(200L).size());
        assertEquals("APROBACION_PREGUNTA", auditService.listarPorPregunta(200L).get(0).getAccion());

        assertEquals(1, auditService.listarPorPregunta(201L).size());
        assertEquals("RECHAZO_PREGUNTA", auditService.listarPorPregunta(201L).get(0).getAccion());
        assertTrue(auditService.listarPorPregunta(201L).get(0).getDetalles().contains("Opción B es ambigua"));
    }

    @Test
    @DisplayName("Idempotencia: no registra eventos con el mismo eventoId dos veces")
    void testIdempotenciaAudit() {
        PreguntaAprobadaEvent event = new PreguntaAprobadaEvent(
                300L, 3L, "Carlos", "carlos@unicauca.edu.co", "Algoritmos", "Aprobada", "corr-idem"
        );

        auditObserver.onEvent(event);
        auditObserver.onEvent(event); // Reintento / duplicado

        List<RegistroAuditoriaEntity> registros = auditService.listarPorPregunta(300L);
        assertEquals(1, registros.size(), "No debe duplicar registros para el mismo evento");
    }

    // --- In-memory stub repository ---
    private static class StubAuditRepository implements AuditRepository {
        private final Map<Long, RegistroAuditoriaEntity> store = new ConcurrentHashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public boolean existsByEventoId(String eventoId) {
            return store.values().stream().anyMatch(r -> eventoId.equals(r.getEventoId()));
        }

        @Override
        public List<RegistroAuditoriaEntity> findByAggregateIdOrderByFechaRegistroDesc(String aggregateId) {
            return store.values().stream()
                    .filter(r -> aggregateId.equals(r.getAggregateId()))
                    .sorted((a, b) -> b.getFechaRegistro().compareTo(a.getFechaRegistro()))
                    .toList();
        }

        @Override
        public List<RegistroAuditoriaEntity> findByEventTypeOrderByFechaRegistroDesc(String eventType) {
            return store.values().stream()
                    .filter(r -> eventType.equalsIgnoreCase(r.getEventType()))
                    .sorted((a, b) -> b.getFechaRegistro().compareTo(a.getFechaRegistro()))
                    .toList();
        }

        @Override
        public List<RegistroAuditoriaEntity> findAllByOrderByFechaRegistroDesc() {
            return store.values().stream()
                    .sorted((a, b) -> b.getFechaRegistro().compareTo(a.getFechaRegistro()))
                    .toList();
        }

        @Override
        public <S extends RegistroAuditoriaEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(seq.getAndIncrement());
            }
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<RegistroAuditoriaEntity> findById(Long aLong) {
            return Optional.ofNullable(store.get(aLong));
        }

        @Override public boolean existsById(Long aLong) { return store.containsKey(aLong); }
        @Override public List<RegistroAuditoriaEntity> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<RegistroAuditoriaEntity> findAllById(Iterable<Long> longs) { return Collections.emptyList(); }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(RegistroAuditoriaEntity entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends RegistroAuditoriaEntity> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public <S extends RegistroAuditoriaEntity> List<S> saveAll(Iterable<S> entities) { return Collections.emptyList(); }
        @Override public void flush() {}
        @Override public <S extends RegistroAuditoriaEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends RegistroAuditoriaEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return Collections.emptyList(); }
        @Override public void deleteAllInBatch(Iterable<RegistroAuditoriaEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public RegistroAuditoriaEntity getOne(Long aLong) { return store.get(aLong); }
        @Override public RegistroAuditoriaEntity getById(Long aLong) { return store.get(aLong); }
        @Override public RegistroAuditoriaEntity getReferenceById(Long aLong) { return store.get(aLong); }
        @Override public <S extends RegistroAuditoriaEntity> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends RegistroAuditoriaEntity> List<S> findAll(Example<S> example) { return Collections.emptyList(); }
        @Override public <S extends RegistroAuditoriaEntity> List<S> findAll(Example<S> example, Sort sort) { return Collections.emptyList(); }
        @Override public <S extends RegistroAuditoriaEntity> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends RegistroAuditoriaEntity> long count(Example<S> example) { return 0; }
        @Override public <S extends RegistroAuditoriaEntity> boolean exists(Example<S> example) { return false; }
        @Override public <S extends RegistroAuditoriaEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<RegistroAuditoriaEntity> findAll(Sort sort) { return new ArrayList<>(store.values()); }
        @Override public Page<RegistroAuditoriaEntity> findAll(Pageable pageable) { return Page.empty(); }
    }
}
