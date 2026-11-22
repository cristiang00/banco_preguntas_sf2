package co.edu.unicauca.bancopreguntas.lifecycle;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaEnviadaARevisionEvent;
import co.edu.unicauca.bancopreguntas.contracts.observer.DomainObserver;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.HistorialTransicionEntity;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.PreguntaCicloVidaEntity;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.state.*;
import co.edu.unicauca.bancopreguntas.lifecycle.messaging.LifecycleEventPublisher;
import co.edu.unicauca.bancopreguntas.lifecycle.repository.HistorialTransicionRepository;
import co.edu.unicauca.bancopreguntas.lifecycle.repository.PreguntaCicloVidaRepository;
import co.edu.unicauca.bancopreguntas.lifecycle.service.LifecycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleStateTest {

    private LifecycleService service;
    private InMemoryCicloVidaRepository cicloVidaRepo;
    private InMemoryHistorialRepository historialRepo;
    private LifecycleEventPublisher eventPublisher;

    static class InMemoryCicloVidaRepository implements PreguntaCicloVidaRepository {
        Map<Long, PreguntaCicloVidaEntity> map = new HashMap<>();
        @Override public <S extends PreguntaCicloVidaEntity> S save(S entity) { map.put(entity.getPreguntaId(), entity); return entity; }
        @Override public Optional<PreguntaCicloVidaEntity> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long aLong) { return map.containsKey(aLong); }
        @Override public List<PreguntaCicloVidaEntity> findAll() { return new ArrayList<>(map.values()); }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) { map.remove(aLong); }
        @Override public void delete(PreguntaCicloVidaEntity entity) { map.remove(entity.getPreguntaId()); }
        @Override public void deleteAll() { map.clear(); }
        // Default JpaRepository dummy implementations
        @Override public <S extends PreguntaCicloVidaEntity> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public List<PreguntaCicloVidaEntity> findAllById(Iterable<Long> longs) { return null; }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends PreguntaCicloVidaEntity> entities) {}
        @Override public void flush() {}
        @Override public <S extends PreguntaCicloVidaEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends PreguntaCicloVidaEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<PreguntaCicloVidaEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public PreguntaCicloVidaEntity getOne(Long aLong) { return null; }
        @Override public PreguntaCicloVidaEntity getById(Long aLong) { return null; }
        @Override public PreguntaCicloVidaEntity getReferenceById(Long aLong) { return null; }
        @Override public <S extends PreguntaCicloVidaEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends PreguntaCicloVidaEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends PreguntaCicloVidaEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends PreguntaCicloVidaEntity> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends PreguntaCicloVidaEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends PreguntaCicloVidaEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends PreguntaCicloVidaEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<PreguntaCicloVidaEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public org.springframework.data.domain.Page<PreguntaCicloVidaEntity> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
    }

    static class InMemoryHistorialRepository implements HistorialTransicionRepository {
        List<HistorialTransicionEntity> list = new ArrayList<>();
        @Override public <S extends HistorialTransicionEntity> S save(S entity) { list.add(entity); return entity; }
        @Override public List<HistorialTransicionEntity> findByPreguntaIdOrderByFechaTransicionAsc(Long preguntaId) { return list; }
        @Override public <S extends HistorialTransicionEntity> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<HistorialTransicionEntity> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<HistorialTransicionEntity> findAll() { return list; }
        @Override public List<HistorialTransicionEntity> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return list.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(HistorialTransicionEntity entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends HistorialTransicionEntity> entities) {}
        @Override public void deleteAll() {}
        @Override public void flush() {}
        @Override public <S extends HistorialTransicionEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends HistorialTransicionEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<HistorialTransicionEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public HistorialTransicionEntity getOne(Long aLong) { return null; }
        @Override public HistorialTransicionEntity getById(Long aLong) { return null; }
        @Override public HistorialTransicionEntity getReferenceById(Long aLong) { return null; }
        @Override public <S extends HistorialTransicionEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends HistorialTransicionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends HistorialTransicionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends HistorialTransicionEntity> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends HistorialTransicionEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends HistorialTransicionEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends HistorialTransicionEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<HistorialTransicionEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public org.springframework.data.domain.Page<HistorialTransicionEntity> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
    }

    @BeforeEach
    void setUp() {
        cicloVidaRepo = new InMemoryCicloVidaRepository();
        historialRepo = new InMemoryHistorialRepository();
        eventPublisher = new LifecycleEventPublisher(null);
        service = new LifecycleService(cicloVidaRepo, historialRepo, eventPublisher);
    }

    @Test
    @DisplayName("SSCRUM-42: Transición exitosa Borrador -> Pendiente de revisión y publicación de evento")
    void enviarARevision_exitoso() {
        Long preguntaId = 101L;
        Long autorId = 5L;
        service.registrarPreguntaInicial(preguntaId, autorId);

        List<DomainEvent> publishedEvents = new ArrayList<>();
        eventPublisher.getDomainEventBus().registerObserver(publishedEvents::add);

        EstadoPreguntaDTO estado = service.enviarARevision(
                preguntaId, autorId, "Juan Pérez", "Contexto de prueba", "¿Pregunta?", "corr-123"
        );

        assertEquals(EstadoPreguntaDTO.PENDIENTE_REVISION, estado);
        assertEquals(1, historialRepo.list.size());
        assertEquals(1, publishedEvents.size());
        assertTrue(publishedEvents.get(0) instanceof PreguntaEnviadaARevisionEvent);
        PreguntaEnviadaARevisionEvent event = (PreguntaEnviadaARevisionEvent) publishedEvents.get(0);
        assertEquals(preguntaId, event.getPreguntaId());
        assertEquals(autorId, event.getAutorId());
        assertEquals("corr-123", event.getCorrelationId());
        assertEquals(1, event.getVersion());
    }

    @Test
    @DisplayName("SSCRUM-43: Rechazo de transición cuando el estado no es Borrador (patrón State)")
    void enviarARevision_estadoInvalido_lanzaExcepcion() {
        Long preguntaId = 102L;
        Long autorId = 5L;
        PreguntaCicloVidaEntity entity = new PreguntaCicloVidaEntity(preguntaId, autorId, EstadoPreguntaDTO.PENDIENTE_REVISION);
        cicloVidaRepo.save(entity);

        assertThrows(TransicionInvalidaException.class, () ->
            service.enviarARevision(preguntaId, autorId, "Juan", "Ctx", "¿?", "corr-1")
        );
    }

    @Test
    @DisplayName("SSCRUM-45: Rechazo por falta de permisos si el usuario no es el autor")
    void enviarARevision_noEsAutor_lanzaAccesoDenegado() {
        Long preguntaId = 103L;
        Long autorOriginalId = 5L;
        Long otroUsuarioId = 99L;
        service.registrarPreguntaInicial(preguntaId, autorOriginalId);

        assertThrows(AccesoDenegadoException.class, () ->
            service.enviarARevision(preguntaId, otroUsuarioId, "Otro", "Ctx", "¿?", "corr-2")
        );
    }

    @Test
    @DisplayName("SSCRUM-46: Solo se pueden editar preguntas en estado Borrador")
    void ensureEditable_bloqueaFueraDeBorrador() {
        QuestionState draft = new DraftState();
        assertTrue(draft.isEditable());
        assertDoesNotThrow(draft::ensureEditable);

        QuestionState pending = new PendingReviewState();
        assertFalse(pending.isEditable());
        assertThrows(TransicionInvalidaException.class, pending::ensureEditable);

        QuestionState approved = new ApprovedState();
        assertFalse(approved.isEditable());
        assertThrows(TransicionInvalidaException.class, approved::ensureEditable);

        QuestionState rejected = new RejectedState();
        assertFalse(rejected.isEditable());
        assertThrows(TransicionInvalidaException.class, rejected::ensureEditable);
    }

    @Test
    @DisplayName("SSCRUM-44: Cada estado tiene color distintivo y etiqueta")
    void estadoPreguntaDTO_coloresYEtiquetas() {
        assertEquals("#6C757D", EstadoPreguntaDTO.BORRADOR.getColorHex());
        assertEquals("Borrador", EstadoPreguntaDTO.BORRADOR.getLabel());

        assertEquals("#FFC107", EstadoPreguntaDTO.PENDIENTE_REVISION.getColorHex());
        assertEquals("Pendiente de revisión", EstadoPreguntaDTO.PENDIENTE_REVISION.getLabel());

        assertEquals("#198754", EstadoPreguntaDTO.APROBADA.getColorHex());
        assertEquals("Aprobada", EstadoPreguntaDTO.APROBADA.getLabel());

        assertEquals("#DC3545", EstadoPreguntaDTO.RECHAZADA.getColorHex());
        assertEquals("Rechazada", EstadoPreguntaDTO.RECHAZADA.getLabel());
    }

    @Test
    @DisplayName("HU6.S3 / HU6.S3.5: Transición exitosa Rechazada -> Borrador al crear nueva versión y registro en historial")
    void crearNuevaVersion_rechazadaABorrador_exitoso() {
        Long preguntaId = 201L;
        Long autorId = 7L;
        PreguntaCicloVidaEntity entity = new PreguntaCicloVidaEntity(preguntaId, autorId, EstadoPreguntaDTO.RECHAZADA);
        cicloVidaRepo.save(entity);

        EstadoPreguntaDTO nuevoEstado = service.crearNuevaVersion(preguntaId, autorId, "corr-nueva-version");

        assertEquals(EstadoPreguntaDTO.BORRADOR, nuevoEstado);
        assertEquals(EstadoPreguntaDTO.BORRADOR, entity.getEstado());
        assertEquals(1, historialRepo.list.size());
        HistorialTransicionEntity log = historialRepo.list.get(0);
        assertEquals(EstadoPreguntaDTO.RECHAZADA, log.getEstadoOrigen());
        assertEquals(EstadoPreguntaDTO.BORRADOR, log.getEstadoDestino());
        assertEquals(autorId, log.getUsuarioId());
        assertTrue(log.getMotivo().contains("nueva versión"));
    }

    @Test
    @DisplayName("HU6.S4.4: Transición no permitida a nueva versión desde estados que no sean Rechazada (409)")
    void crearNuevaVersion_desdeEstadosNoPermitidos_lanzaTransicionInvalidaException() {
        Long autorId = 7L;

        // Desde Borrador
        PreguntaCicloVidaEntity enBorrador = new PreguntaCicloVidaEntity(202L, autorId, EstadoPreguntaDTO.BORRADOR);
        cicloVidaRepo.save(enBorrador);
        assertThrows(TransicionInvalidaException.class, () ->
            service.crearNuevaVersion(202L, autorId, "corr-1")
        );

        // Desde Pendiente de Revisión
        PreguntaCicloVidaEntity enPendiente = new PreguntaCicloVidaEntity(203L, autorId, EstadoPreguntaDTO.PENDIENTE_REVISION);
        cicloVidaRepo.save(enPendiente);
        assertThrows(TransicionInvalidaException.class, () ->
            service.crearNuevaVersion(203L, autorId, "corr-2")
        );

        // Desde Aprobada
        PreguntaCicloVidaEntity enAprobada = new PreguntaCicloVidaEntity(204L, autorId, EstadoPreguntaDTO.APROBADA);
        cicloVidaRepo.save(enAprobada);
        assertThrows(TransicionInvalidaException.class, () ->
            service.crearNuevaVersion(204L, autorId, "corr-3")
        );
    }

    @Test
    @DisplayName("HU6.S2 / HU6.S4: Rechazo por falta de permisos si el usuario no es el autor al crear nueva versión (403)")
    void crearNuevaVersion_noEsAutor_lanzaAccesoDenegado() {
        Long preguntaId = 205L;
        Long autorOriginalId = 7L;
        Long otroUsuarioId = 99L;
        PreguntaCicloVidaEntity entity = new PreguntaCicloVidaEntity(preguntaId, autorOriginalId, EstadoPreguntaDTO.RECHAZADA);
        cicloVidaRepo.save(entity);

        assertThrows(AccesoDenegadoException.class, () ->
            service.crearNuevaVersion(preguntaId, otroUsuarioId, "corr-4")
        );
    }

    // ─── HU10.S2.1: QuestionStateFactory y patrones State ────────────────────

    @Test
    @DisplayName("HU10.S2.1 - QuestionStateFactory mapea correctamente cada EstadoPreguntaDTO")
    void stateFactory_mapeoCorrectoDesdeDTOs() {
        assertTrue(QuestionStateFactory.fromDTO(EstadoPreguntaDTO.BORRADOR) instanceof DraftState);
        assertTrue(QuestionStateFactory.fromDTO(EstadoPreguntaDTO.PENDIENTE_REVISION) instanceof PendingReviewState);
        assertTrue(QuestionStateFactory.fromDTO(EstadoPreguntaDTO.APROBADA) instanceof ApprovedState);
        assertTrue(QuestionStateFactory.fromDTO(EstadoPreguntaDTO.RECHAZADA) instanceof RejectedState);
        assertTrue(QuestionStateFactory.fromDTO(null) instanceof DraftState, "null debe producir DraftState");
    }

    @Test
    @DisplayName("HU10.S2.1 - QuestionStateFactory.fromString mapea desde cadena de texto")
    void stateFactory_mapeoDesdeString() {
        assertTrue(QuestionStateFactory.fromString("BORRADOR") instanceof DraftState);
        assertTrue(QuestionStateFactory.fromString("PENDIENTE_REVISION") instanceof PendingReviewState);
        assertTrue(QuestionStateFactory.fromString("APROBADA") instanceof ApprovedState);
        assertTrue(QuestionStateFactory.fromString("RECHAZADA") instanceof RejectedState);
        assertTrue(QuestionStateFactory.fromString("INVALIDO") instanceof DraftState, "Estado invalido debe devolver DraftState");
        assertTrue(QuestionStateFactory.fromString(null) instanceof DraftState, "null debe devolver DraftState");
    }

    @Test
    @DisplayName("HU10.S2.1 - Transicion Pendiente -> Aprobada (aplicarDecisionRevisor APROBAR)")
    void aplicarDecision_aprobar_cambiaAAprobada() {
        Long preguntaId = 300L;
        Long adminId = 1L;
        PreguntaCicloVidaEntity entity = new PreguntaCicloVidaEntity(preguntaId, 5L, EstadoPreguntaDTO.PENDIENTE_REVISION);
        cicloVidaRepo.save(entity);

        EstadoPreguntaDTO resultado = service.aplicarDecisionRevisor(
                preguntaId, "APROBAR", adminId, "autor@unicauca.edu.co",
                "Contexto de aprobacion", null, "corr-aprobar"
        );

        assertEquals(EstadoPreguntaDTO.APROBADA, resultado);
        assertEquals(EstadoPreguntaDTO.APROBADA, entity.getEstado());
        assertFalse(historialRepo.list.isEmpty());
    }

    @Test
    @DisplayName("HU10.S2.1 - Transicion Pendiente -> Rechazada (aplicarDecisionRevisor RECHAZAR)")
    void aplicarDecision_rechazar_cambiaArechazada() {
        Long preguntaId = 301L;
        Long adminId = 1L;
        PreguntaCicloVidaEntity entity = new PreguntaCicloVidaEntity(preguntaId, 5L, EstadoPreguntaDTO.PENDIENTE_REVISION);
        cicloVidaRepo.save(entity);

        EstadoPreguntaDTO resultado = service.aplicarDecisionRevisor(
                preguntaId, "RECHAZAR", adminId, "autor@unicauca.edu.co",
                "Contexto de rechazo", List.of("Falta bibliografía"), "corr-rechazar"
        );

        assertEquals(EstadoPreguntaDTO.RECHAZADA, resultado);
        assertEquals(EstadoPreguntaDTO.RECHAZADA, entity.getEstado());
    }

    @Test
    @DisplayName("HU10.S2.1 - puedeEditar es true solo en estado BORRADOR")
    void puedeEditar_soloEnBorrador() {
        // Pregunta en Borrador
        Long p1 = 310L;
        service.registrarPreguntaInicial(p1, 5L);
        assertTrue(service.puedeEditar(p1));

        // Pregunta en Pendiente
        Long p2 = 311L;
        PreguntaCicloVidaEntity enPendiente = new PreguntaCicloVidaEntity(p2, 5L, EstadoPreguntaDTO.PENDIENTE_REVISION);
        cicloVidaRepo.save(enPendiente);
        assertFalse(service.puedeEditar(p2));

        // Pregunta Aprobada
        Long p3 = 312L;
        PreguntaCicloVidaEntity enAprobada = new PreguntaCicloVidaEntity(p3, 5L, EstadoPreguntaDTO.APROBADA);
        cicloVidaRepo.save(enAprobada);
        assertFalse(service.puedeEditar(p3));
    }
}
