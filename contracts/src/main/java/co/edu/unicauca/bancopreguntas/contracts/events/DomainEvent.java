package co.edu.unicauca.bancopreguntas.contracts.events;

import java.time.LocalDateTime;

/**
 * Contrato base para todos los eventos de dominio del sistema distribuido.
 */
public interface DomainEvent {

    /**
     * Identificador único del evento (UUID) para idempotencia y trazabilidad.
     */
    String getEventId();

    /**
     * Tipo canónico del evento (e.g. "PreguntaEnviadaARevision").
     */
    String getEventType();

    /**
     * Identificador del agregado principal relacionado (e.g. ID de pregunta).
     */
    String getAggregateId();

    /**
     * Fecha y hora de generación del evento.
     */
    LocalDateTime getOccurredOn();

    /**
     * Identificador de correlación para trazabilidad distribuida a través del API Gateway y microservicios.
     */
    String getCorrelationId();
}

