package co.edu.unicauca.bancopreguntas.contracts.events;

import java.time.LocalDateTime;

/**
 * Evento emitido cuando una pregunta es aprobada por los revisores (HU5 - SSCRUM-64).
 */
public class PreguntaAprobadaEvent extends BaseDomainEvent {

    public static final String EVENT_TYPE = "PreguntaAprobada";

    private final Long preguntaId;
    private final Long autorId;
    private final String autorNombre;
    private final String autorEmail;
    private final String contexto;
    private final String comentarioGeneral;

    public PreguntaAprobadaEvent(Long preguntaId, Long autorId, String autorNombre, String autorEmail,
                                 String contexto, String comentarioGeneral, String correlationId) {
        super(EVENT_TYPE, String.valueOf(preguntaId), correlationId);
        this.preguntaId = preguntaId;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.autorEmail = autorEmail;
        this.contexto = contexto;
        this.comentarioGeneral = comentarioGeneral;
    }

    public PreguntaAprobadaEvent(String eventId, Long preguntaId, Long autorId, String autorNombre, String autorEmail,
                                 String contexto, String comentarioGeneral, LocalDateTime occurredOn, String correlationId) {
        super(eventId, EVENT_TYPE, String.valueOf(preguntaId), occurredOn, correlationId);
        this.preguntaId = preguntaId;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.autorEmail = autorEmail;
        this.contexto = contexto;
        this.comentarioGeneral = comentarioGeneral;
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

    public String getAutorEmail() {
        return autorEmail;
    }

    public String getContexto() {
        return contexto;
    }

    public String getComentarioGeneral() {
        return comentarioGeneral;
    }
}

