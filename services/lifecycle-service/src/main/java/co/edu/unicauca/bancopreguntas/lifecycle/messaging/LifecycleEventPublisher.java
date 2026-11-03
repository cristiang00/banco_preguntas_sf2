package co.edu.unicauca.bancopreguntas.lifecycle.messaging;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.RemoteEventDispatcher;
import co.edu.unicauca.bancopreguntas.contracts.observer.DomainEventBus;
import co.edu.unicauca.bancopreguntas.contracts.observer.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class LifecycleEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LifecycleEventPublisher.class);

    private final ApplicationEventPublisher springPublisher;
    private final DomainEventBus domainEventBus = new DomainEventBus();
    private final ObjectMapper objectMapper;
    private final String notificationServiceUrl;
    private final String auditServiceUrl;

    public LifecycleEventPublisher() {
        this(null, createDefaultMapper(), "http://localhost:8086", "http://localhost:8087");
    }

    public LifecycleEventPublisher(ApplicationEventPublisher springPublisher) {
        this(springPublisher, createDefaultMapper(), "http://localhost:8086", "http://localhost:8087");
    }

    @Autowired
    public LifecycleEventPublisher(@Autowired(required = false) ApplicationEventPublisher springPublisher,
                                   @Autowired(required = false) ObjectMapper objectMapper,
                                   @Value("${services.notification.url:http://localhost:8086}") String notificationServiceUrl,
                                   @Value("${services.audit.url:http://localhost:8087}") String auditServiceUrl) {
        this.springPublisher = springPublisher;
        this.objectMapper = objectMapper != null ? objectMapper : createDefaultMapper();
        this.notificationServiceUrl = notificationServiceUrl;
        this.auditServiceUrl = auditServiceUrl;
    }

    private static ObjectMapper createDefaultMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
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

        // 3. Despachar a microservicios distribuidos (Notification y Audit)
        try {
            String json = objectMapper.writeValueAsString(event);
            RemoteEventDispatcher.dispatchAsync(json, notificationServiceUrl, auditServiceUrl);
        } catch (Exception e) {
            log.warn("Error serializando evento para despacho remoto: {}", e.getMessage());
        }
    }
}
