package co.edu.unicauca.bancopreguntas.contracts.events;

import java.time.LocalDateTime;

/**
 * Evento emitido cuando un autor envía una pregunta en borrador a revisión (HU2 - SSCRUM-42 / HU6.S5).
 */
public class PreguntaEnviadaARevisionEvent extends BaseDomainEvent {

    public static final String EVENT_TYPE = "PreguntaEnviadaARevision";

    private final Long preguntaId;
    private final Long autorId;
    private final String autorNombre;
    private final String contexto;
    private final String preguntaDirecta;
    private final Integer version;

    public PreguntaEnviadaARevisionEvent(Long preguntaId, Long autorId, String autorNombre, 
                                         String contexto, String preguntaDirecta, String correlationId) {
        this(preguntaId, autorId, autorNombre, contexto, preguntaDirecta, 1, correlationId);
    }

    public PreguntaEnviadaARevisionEvent(Long preguntaId, Long autorId, String autorNombre, 
                                         String contexto, String preguntaDirecta, Integer version, String correlationId) {
        super(EVENT_TYPE, String.valueOf(preguntaId), correlationId);
        this.preguntaId = preguntaId;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.contexto = contexto;
        this.preguntaDirecta = preguntaDirecta;
        this.version = version != null ? version : 1;
    }

    public PreguntaEnviadaARevisionEvent(String eventId, Long preguntaId, Long autorId, String autorNombre,
                                         String contexto, String preguntaDirecta, LocalDateTime occurredOn, String correlationId) {
        this(eventId, preguntaId, autorId, autorNombre, contexto, preguntaDirecta, 1, occurredOn, correlationId);
    }

    public PreguntaEnviadaARevisionEvent(String eventId, Long preguntaId, Long autorId, String autorNombre,
                                         String contexto, String preguntaDirecta, Integer version,
                                         LocalDateTime occurredOn, String correlationId) {
        super(eventId, EVENT_TYPE, String.valueOf(preguntaId), occurredOn, correlationId);
        this.preguntaId = preguntaId;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.contexto = contexto;
        this.preguntaDirecta = preguntaDirecta;
        this.version = version != null ? version : 1;
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

    public String getPreguntaDirecta() {
        return preguntaDirecta;
    }

    public Integer getVersion() {
        return version != null ? version : 1;
    }
}
