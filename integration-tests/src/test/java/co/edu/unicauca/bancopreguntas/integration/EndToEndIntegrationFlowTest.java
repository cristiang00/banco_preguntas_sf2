package co.edu.unicauca.bancopreguntas.integration;

import co.edu.unicauca.bancopreguntas.contracts.dto.*;
import co.edu.unicauca.bancopreguntas.contracts.events.*;
import co.edu.unicauca.bancopreguntas.contracts.observer.DomainEventBus;
import co.edu.unicauca.bancopreguntas.contracts.observer.DomainObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EndToEndIntegrationFlowTest {

    private DomainEventBus eventBus;
    private TrackingAuditObserver auditObserver;
    private TrackingNotificationObserver notificationObserver;

    @BeforeEach
    void setUp() {
        eventBus = DomainEventBus.getInstance();
        auditObserver = new TrackingAuditObserver();
        notificationObserver = new TrackingNotificationObserver();

        eventBus.registerObserver(auditObserver);
        eventBus.registerObserver(notificationObserver);
    }

    @Test
    @DisplayName("Flujo Completo de Integración Distribuida (HU1 a HU5) con Contratos y Patrón Observer")
    void testCompleteEventDrivenFlow() {
        // =====================================================================
        // HU1: Creación de Pregunta y Verificación Estructural de DTOs
        // =====================================================================
        List<OpcionDTO> opciones = List.of(
                new OpcionDTO("A", "Sección Crítica con Mutex"),
                new OpcionDTO("B", "Sobrecarga de Métodos"),
                new OpcionDTO("C", "Polimorfismo Paramétrico"),
                new OpcionDTO("D", "Recolección de Basura")
        );

        PreguntaDTO preguntaDto = new PreguntaDTO();
        preguntaDto.setId(5001L);
        preguntaDto.setAutorId(2L);
        preguntaDto.setAutorNombre("Ing. Juan Pérez");
        preguntaDto.setContexto("En sistemas concurrentes, dos hilos intentan modificar una variable compartida simultáneamente sin exclusión mutua.");
        preguntaDto.setPreguntaDirecta("¿Qué problema se produce?");
        preguntaDto.setOpciones(opciones);
        preguntaDto.setRespuestaCorrecta("A");
        preguntaDto.setJustificacion("Se produce una condición de carrera corregible mediante exclusión mutua.");
        preguntaDto.setBibliografia("Operating System Concepts, 10th Edition.");
        preguntaDto.setCompetencia("Diseño de Software");
        preguntaDto.setTema("Concurrencia");
        preguntaDto.setSubtema("Exclusión Mutua");
        preguntaDto.setNivelDificultad("Medio");
        preguntaDto.setEstado("BORRADOR");

        assertEquals(4, preguntaDto.getOpciones().size());
        assertEquals("BORRADOR", preguntaDto.getEstado());
        assertEquals("A", preguntaDto.getRespuestaCorrecta());

        // =====================================================================
        // HU2: Envío de Pregunta a Revisión y Emisión de Evento
        // =====================================================================
        EstadoPreguntaDTO estadoEnviado = EstadoPreguntaDTO.PENDIENTE_REVISION;
        assertEquals("PENDIENTE_REVISION", estadoEnviado.name());
        assertEquals("#FFC107", estadoEnviado.getColorHex());
        assertEquals("Pendiente de revisión", estadoEnviado.getLabel());

        PreguntaEnviadaARevisionEvent eventoEnvio = new PreguntaEnviadaARevisionEvent(
                preguntaDto.getId(),
                preguntaDto.getAutorId(),
                preguntaDto.getAutorNombre(),
                preguntaDto.getContexto(),
                preguntaDto.getPreguntaDirecta(),
                "corr-flow-hu2"
        );

        eventBus.publish(eventoEnvio);

        // Verificación de recepción en Observador de Auditoría
        assertTrue(auditObserver.getReceivedEvents().stream()
                .anyMatch(e -> e instanceof PreguntaEnviadaARevisionEvent pe && pe.getPreguntaId().equals(5001L)));

        // =====================================================================
        // HU3: Proyección de Estados y Colores en el Gateway
        // =====================================================================
        assertEquals("#FFC107", estadoEnviado.getColorHex());
        assertEquals("Pendiente de revisión", estadoEnviado.getLabel());

        // =====================================================================
        // HU4: Asignación de Revisores y Notificaciones Desacopladas
        // =====================================================================
        List<RevisoresAsignadosEvent.RevisorInfo> revisoresAsignados = List.of(
                new RevisoresAsignadosEvent.RevisorInfo(10L, "Dra. María Revisor", "maria@saberpro.unicauca.edu.co"),
                new RevisoresAsignadosEvent.RevisorInfo(11L, "Dr. Pedro Revisor", "pedro@saberpro.unicauca.edu.co")
        );

        // Validar regla de negocio: autor no puede ser revisor
        boolean autorEsRevisor = revisoresAsignados.stream()
                .anyMatch(r -> r.getRevisorId().equals(preguntaDto.getAutorId()));
        assertFalse(autorEsRevisor, "Conflicto de interés: el autor no debe asignarse a sí mismo");

        RevisoresAsignadosEvent eventoAsignacion = new RevisoresAsignadosEvent(
                preguntaDto.getId(),
                preguntaDto.getAutorId(),
                preguntaDto.getAutorNombre(),
                preguntaDto.getContexto(),
                revisoresAsignados,
                "corr-flow-hu4"
        );

        eventBus.publish(eventoAsignacion);

        // Verificación en Notificaciones
        assertTrue(notificationObserver.getReceivedEvents().stream()
                .anyMatch(e -> e instanceof RevisoresAsignadosEvent ra && ra.getPreguntaId().equals(5001L)));

        // =====================================================================
        // HU5: Decisión de Revisión y Transición Final (Aprobación)
        // =====================================================================
        PreguntaAprobadaEvent eventoAprobada = new PreguntaAprobadaEvent(
                preguntaDto.getId(),
                preguntaDto.getAutorId(),
                preguntaDto.getAutorNombre(),
                "autor@saberpro.unicauca.edu.co",
                preguntaDto.getContexto(),
                "Aprobada por unanimidad pedagógica",
                "corr-flow-hu5"
        );

        eventBus.publish(eventoAprobada);

        // Verificación de trazabilidad final completa
        assertTrue(auditObserver.getReceivedEvents().stream().anyMatch(e -> e instanceof PreguntaAprobadaEvent));
        assertTrue(notificationObserver.getReceivedEvents().stream().anyMatch(e -> e instanceof PreguntaAprobadaEvent));
    }

    @Test
    @DisplayName("Flujo de Rechazo (HU5): Valida que el rechazo con observaciones emite PreguntaRechazadaEvent")
    void testRejectionFlow() {
        PreguntaRechazadaEvent eventoRechazo = new PreguntaRechazadaEvent(
                6001L, 2L, "Ing. Juan Pérez", "autor@saberpro.unicauca.edu.co",
                "Contexto de prueba", List.of("Opción C es ambigua", "Bibliografía incompleta"), "corr-rechazo"
        );

        eventBus.publish(eventoRechazo);

        assertTrue(auditObserver.getReceivedEvents().stream()
                .anyMatch(e -> e instanceof PreguntaRechazadaEvent pr && pr.getPreguntaId().equals(6001L)));
        assertTrue(notificationObserver.getReceivedEvents().stream()
                .anyMatch(e -> e instanceof PreguntaRechazadaEvent pr && pr.getPreguntaId().equals(6001L)));
    }

    // --- Clases Observadoras de Prueba ---
    private static class TrackingAuditObserver implements DomainObserver {
        private final List<DomainEvent> receivedEvents = new CopyOnWriteArrayList<>();

        @Override
        public void onEvent(DomainEvent event) {
            receivedEvents.add(event);
        }

        public List<DomainEvent> getReceivedEvents() {
            return Collections.unmodifiableList(receivedEvents);
        }
    }

    private static class TrackingNotificationObserver implements DomainObserver {
        private final List<DomainEvent> receivedEvents = new CopyOnWriteArrayList<>();

        @Override
        public void onEvent(DomainEvent event) {
            receivedEvents.add(event);
        }

        public List<DomainEvent> getReceivedEvents() {
            return Collections.unmodifiableList(receivedEvents);
        }
    }
}
