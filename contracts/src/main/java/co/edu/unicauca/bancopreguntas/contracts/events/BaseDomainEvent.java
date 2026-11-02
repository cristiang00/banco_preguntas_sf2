package co.edu.unicauca.bancopreguntas.contracts.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementación base reutilizable para eventos de dominio.
 */
public abstract class BaseDomainEvent implements DomainEvent {

    private final String eventId;
    private final String eventType;
    private final String aggregateId;
    private final LocalDateTime occurredOn;
    private final String correlationId;

    protected BaseDomainEvent(String eventType, String aggregateId, String correlationId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.occurredOn = LocalDateTime.now();
        this.correlationId = (correlationId != null && !correlationId.isBlank()) 
                ? correlationId 
                : UUID.randomUUID().toString();
    }

    protected BaseDomainEvent(String eventId, String eventType, String aggregateId, LocalDateTime occurredOn, String correlationId) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.occurredOn = occurredOn;
        this.correlationId = correlationId;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }
}

