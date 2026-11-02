package co.edu.unicauca.bancopreguntas.lifecycle.messaging;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.observer.DomainEventBus;
import co.edu.unicauca.bancopreguntas.contracts.observer.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class LifecycleEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LifecycleEventPublisher.class);

    private final ApplicationEventPublisher springPublisher;
    private final DomainEventBus domainEventBus = new DomainEventBus();

    public LifecycleEventPublisher(@Autowired(required = false) ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    public DomainEventBus getDomainEventBus() {
        return domainEventBus;
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) return;
        log.info("Publicando evento [{}]: aggregateId={}, correlationId={}", 
                event.getEventType(), event.getAggregateId(), event.getCorrelationId());
        
        // 1. Notificar observadores del bus GoF
        domainEventBus.publish(event);

        // 2. Notificar contexto Spring si está activo
        if (springPublisher != null) {
            springPublisher.publishEvent(event);
        }
    }
}

