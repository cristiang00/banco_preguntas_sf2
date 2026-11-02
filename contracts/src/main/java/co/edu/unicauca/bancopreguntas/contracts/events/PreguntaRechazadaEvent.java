package co.edu.unicauca.bancopreguntas.contracts.events;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evento emitido cuando una pregunta es rechazada con observaciones (HU5 - SSCRUM-64, SSCRUM-65).
 */
public class PreguntaRechazadaEvent extends BaseDomainEvent {

    public static final String EVENT_TYPE = "PreguntaRechazada";

    private final Long preguntaId;
    private final Long autorId;
    private final String autorNombre;
    private final String autorEmail;
    private final String contexto;
    private final List<String> observaciones;

    public PreguntaRechazadaEvent(Long preguntaId, Long autorId, String autorNombre, String autorEmail,
                                  String contexto, List<String> observaciones, String correlationId) {
        super(EVENT_TYPE, String.valueOf(preguntaId), correlationId);
        this.preguntaId = preguntaId;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.autorEmail = autorEmail;
        this.contexto = contexto;
        this.observaciones = observaciones != null ? new ArrayList<>(observaciones) : Collections.emptyList();
    }

    public PreguntaRechazadaEvent(String eventId, Long preguntaId, Long autorId, String autorNombre, String autorEmail,
                                  String contexto, List<String> observaciones, LocalDateTime occurredOn, String correlationId) {
        super(eventId, EVENT_TYPE, String.valueOf(preguntaId), occurredOn, correlationId);
        this.preguntaId = preguntaId;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.autorEmail = autorEmail;
        this.contexto = contexto;
        this.observaciones = observaciones != null ? new ArrayList<>(observaciones) : Collections.emptyList();
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

    public List<String> getObservaciones() {
        return Collections.unmodifiableList(observaciones);
    }
}

