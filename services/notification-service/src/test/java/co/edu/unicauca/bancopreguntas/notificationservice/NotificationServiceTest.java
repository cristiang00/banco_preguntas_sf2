package co.edu.unicauca.bancopreguntas.notificationservice;

import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaAprobadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaRechazadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.RevisoresAsignadosEvent;
import co.edu.unicauca.bancopreguntas.notificationservice.adapter.MockEmailAdapter;
import co.edu.unicauca.bancopreguntas.notificationservice.factory.*;
import co.edu.unicauca.bancopreguntas.notificationservice.model.NotificacionEntity;
import co.edu.unicauca.bancopreguntas.notificationservice.repository.NotificacionRepository;
import co.edu.unicauca.bancopreguntas.notificationservice.service.NotificacionService;
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

class NotificationServiceTest {

    private StubNotificacionRepository notificacionRepository;
    private MockEmailAdapter emailAdapter;
    private NotificacionService notificacionService;

    @BeforeEach
    void setUp() {
        notificacionRepository = new StubNotificacionRepository();
        emailAdapter = new MockEmailAdapter();
        emailAdapter.clear();

        List<NotificacionFactory> factories = List.of(
                new AsignacionNotificacionFactory(),
                new AprobacionNotificacionFactory(),
                new RechazoNotificacionFactory()
        );
        NotificacionFactoryProvider provider = new NotificacionFactoryProvider(factories);

        notificacionService = new NotificacionService(notificacionRepository, provider, emailAdapter);
    }

    @Test
    @DisplayName("Factory Method: debe construir notificación de asignación de revisor")
    void testFactoryMethodAsignacion() {
        AsignacionNotificacionFactory factory = new AsignacionNotificacionFactory();
        Map<String, Object> data = Map.of("preguntaId", 42L, "tituloPregunta", "¿Qué es polimorfismo?");

        Notificacion notif = factory.crearNotificacion("revisor1@unicauca.edu.co", data);

        assertNotNull(notif);
        assertEquals("ASIGNACION_REVISION", notif.getTipo());
        assertEquals("revisor1@unicauca.edu.co", notif.getDestinatario());
        assertTrue(notif.getAsunto().contains("42"));
        assertTrue(notif.getCuerpo().contains("polimorfismo"));
    }

    @Test
    @DisplayName("Factory Method: debe construir notificación de aprobación y rechazo")
    void testFactoryMethodAprobacionYRechazo() {
        AprobacionNotificacionFactory aprobFactory = new AprobacionNotificacionFactory();
        Notificacion aprob = aprobFactory.crearNotificacion("autor@unicauca.edu.co", Map.of("preguntaId", 10L, "observaciones", "Excelente pregunta"));
        assertEquals("PREGUNTA_APROBADA", aprob.getTipo());
        assertTrue(aprob.getAsunto().contains("APROBADA"));

        RechazoNotificacionFactory rechFactory = new RechazoNotificacionFactory();
        Notificacion rech = rechFactory.crearNotificacion("autor@unicauca.edu.co", Map.of("preguntaId", 10L, "motivoRechazo", "Faltan opciones coherentes"));
        assertEquals("PREGUNTA_RECHAZADA", rech.getTipo());
        assertTrue(rech.getAsunto().contains("RECHAZADA"));
        assertTrue(rech.getCuerpo().contains("Faltan opciones coherentes"));
    }

    @Test
    @DisplayName("Procesar evento RevisoresAsignadosEvent envía emails vía Adapter a cada revisor")
    void testProcesarRevisoresAsignados() {
        List<RevisoresAsignadosEvent.RevisorInfo> revisores = List.of(
                new RevisoresAsignadosEvent.RevisorInfo(2L, "Revisor Dos", "revisor2@unicauca.edu.co"),
                new RevisoresAsignadosEvent.RevisorInfo(3L, "Revisor Tres", "revisor3@unicauca.edu.co")
        );
        RevisoresAsignadosEvent event = new RevisoresAsignadosEvent(50L, 1L, "Juan Autor", "Pregunta Biología", revisores, "corr-123");

        List<NotificacionEntity> enviadas = notificacionService.procesarEvento(event);

        assertEquals(2, enviadas.size());
        assertEquals(2, emailAdapter.getSentEmails().size());
        assertEquals("ENVIADA", enviadas.get(0).getEstado());
        assertEquals("ENVIADA", enviadas.get(1).getEstado());
        assertEquals("MOCK", emailAdapter.getProviderName());
    }

    @Test
    @DisplayName("Deduplicación / Idempotencia: no reenvía correos para el mismo evento y destinatario")
    void testIdempotenciaYDeduplicacion() {
        PreguntaAprobadaEvent event = new PreguntaAprobadaEvent(77L, 5L, "Juan Autor", "autor@unicauca.edu.co", "Pregunta Matemáticas", "Aprobada por consenso", "corr-456");

        // Primer envío
        List<NotificacionEntity> primerEnvio = notificacionService.procesarEvento(event);
        assertEquals(1, primerEnvio.size());
        assertEquals("ENVIADA", primerEnvio.get(0).getEstado());
        assertEquals(1, emailAdapter.getSentEmails().size());

        // Segundo envío idéntico (mismo eventId y destinatario)
        List<NotificacionEntity> segundoEnvio = notificacionService.procesarEvento(event);
        assertEquals(1, segundoEnvio.size());
        assertEquals("DUPLICADA_IGNORADA", segundoEnvio.get(0).getEstado());
        // El número de emails reales enviados no debe aumentar
        assertEquals(1, emailAdapter.getSentEmails().size());
    }

    @Test
    @DisplayName("Reintentos ante fallas: intenta hasta el máximo y registra estado FALLIDA")
    void testReintentosAnteFalla() {
        emailAdapter.setSimulateFailure(true);
        PreguntaRechazadaEvent event = new PreguntaRechazadaEvent(88L, 9L, "Juan Autor", "autor@unicauca.edu.co", "Pregunta Historia", List.of("Rechazo por bibliografía inválida"), "corr-789");

        List<NotificacionEntity> resultado = notificacionService.procesarEvento(event);

        assertEquals(1, resultado.size());
        NotificacionEntity notif = resultado.get(0);
        assertEquals("FALLIDA", notif.getEstado());
        assertEquals(3, notif.getIntentos());
        assertNotNull(notif.getErrorDetalle());
    }

    // --- Stub in-memory repository ---
    private static class StubNotificacionRepository implements NotificacionRepository {
        private final Map<Long, NotificacionEntity> store = new ConcurrentHashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public boolean existsByEventoIdAndDestinatario(String eventoId, String destinatario) {
            return store.values().stream()
                    .anyMatch(n -> eventoId.equals(n.getEventoId()) && destinatario.equals(n.getDestinatario()));
        }

        @Override
        public List<NotificacionEntity> findByDestinatarioOrderByFechaCreacionDesc(String destinatario) {
            return store.values().stream()
                    .filter(n -> destinatario.equals(n.getDestinatario()))
                    .sorted((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()))
                    .toList();
        }

        @Override
        public List<NotificacionEntity> findAllByOrderByFechaCreacionDesc() {
            return store.values().stream()
                    .sorted((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()))
                    .toList();
        }

        @Override
        public <S extends NotificacionEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(seq.getAndIncrement());
            }
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<NotificacionEntity> findById(Long aLong) {
            return Optional.ofNullable(store.get(aLong));
        }

        @Override public boolean existsById(Long aLong) { return store.containsKey(aLong); }
        @Override public List<NotificacionEntity> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<NotificacionEntity> findAllById(Iterable<Long> longs) { return Collections.emptyList(); }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(NotificacionEntity entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends NotificacionEntity> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public <S extends NotificacionEntity> List<S> saveAll(Iterable<S> entities) { return Collections.emptyList(); }
        @Override public void flush() {}
        @Override public <S extends NotificacionEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends NotificacionEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return Collections.emptyList(); }
        @Override public void deleteAllInBatch(Iterable<NotificacionEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public NotificacionEntity getOne(Long aLong) { return store.get(aLong); }
        @Override public NotificacionEntity getById(Long aLong) { return store.get(aLong); }
        @Override public NotificacionEntity getReferenceById(Long aLong) { return store.get(aLong); }
        @Override public <S extends NotificacionEntity> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends NotificacionEntity> List<S> findAll(Example<S> example) { return Collections.emptyList(); }
        @Override public <S extends NotificacionEntity> List<S> findAll(Example<S> example, Sort sort) { return Collections.emptyList(); }
        @Override public <S extends NotificacionEntity> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends NotificacionEntity> long count(Example<S> example) { return 0; }
        @Override public <S extends NotificacionEntity> boolean exists(Example<S> example) { return false; }
        @Override public <S extends NotificacionEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<NotificacionEntity> findAll(Sort sort) { return new ArrayList<>(store.values()); }
        @Override public Page<NotificacionEntity> findAll(Pageable pageable) { return Page.empty(); }
    }
}
