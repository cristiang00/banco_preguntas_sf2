package co.edu.unicauca.bancopreguntas.contracts.events;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evento emitido cuando un administrador asigna revisores a una pregunta (HU4 - SSCRUM-56, SSCRUM-60).
 */
public class RevisoresAsignadosEvent extends BaseDomainEvent {

    public static final String EVENT_TYPE = "RevisoresAsignados";

    private final Long preguntaId;
    private final Long autorId;
    private final String autorNombre;
    private final String contexto;
    private final List<RevisorInfo> revisores;

    public static class RevisorInfo {
        private final Long revisorId;
        private final String nombreCompleto;
        private final String email;

        public RevisorInfo(Long revisorId, String nombreCompleto, String email) {
            this.revisorId = revisorId;
            this.nombreCompleto = nombreCompleto;
            this.email = email;
        }

        public Long getRevisorId() {
            return revisorId;
        }

        public String getNombreCompleto() {
            return nombreCompleto;
        }

        public String getEmail() {
            return email;
        }
    }

    public RevisoresAsignadosEvent(Long preguntaId, Long autorId, String autorNombre,
                                   String contexto, List<RevisorInfo> revisores, String correlationId) {
        super(EVENT_TYPE, String.valueOf(preguntaId), correlationId);
        this.preguntaId = preguntaId;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.contexto = contexto;
        this.revisores = revisores != null ? new ArrayList<>(revisores) : Collections.emptyList();
    }

    public RevisoresAsignadosEvent(String eventId, Long preguntaId, Long autorId, String autorNombre,
                                   String contexto, List<RevisorInfo> revisores, LocalDateTime occurredOn, String correlationId) {
        super(eventId, EVENT_TYPE, String.valueOf(preguntaId), occurredOn, correlationId);
        this.preguntaId = preguntaId;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.contexto = contexto;
        this.revisores = revisores != null ? new ArrayList<>(revisores) : Collections.emptyList();
    }

    public Long getPreguntaId() {
        return preguntaId;
    }

    public Long getAutorId() {
        return autorId;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public String getContexto() {
        return contexto;
    }

    public List<RevisorInfo> getRevisores() {
        return Collections.unmodifiableList(revisores);
    }
}

