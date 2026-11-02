package co.edu.unicauca.bancopreguntas.review.messaging;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.observer.DomainEventBus;
import co.edu.unicauca.bancopreguntas.contracts.observer.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ReviewEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReviewEventPublisher.class);

    private final ApplicationEventPublisher springPublisher;
    private final DomainEventBus domainEventBus = new DomainEventBus();

    public ReviewEventPublisher(@Autowired(required = false) ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    public DomainEventBus getDomainEventBus() {
        return domainEventBus;
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) return;
        log.info("Publicando evento desde ReviewService [{}]: aggregateId={}, correlationId={}",
                event.getEventType(), event.getAggregateId(), event.getCorrelationId());

        domainEventBus.publish(event);

        if (springPublisher != null) {
            springPublisher.publishEvent(event);
        }
    }
}

