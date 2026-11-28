package co.edu.unicauca.bancopreguntas.review.messaging;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.RabbitMQConstants;
import co.edu.unicauca.bancopreguntas.contracts.events.RemoteEventDispatcher;
import co.edu.unicauca.bancopreguntas.contracts.observer.DomainEventBus;
import co.edu.unicauca.bancopreguntas.contracts.observer.EventPublisher;
import co.edu.unicauca.bancopreguntas.review.infrastructure.security.ResourceServerSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ReviewEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReviewEventPublisher.class);

    private final ApplicationEventPublisher springPublisher;
    private final DomainEventBus domainEventBus = new DomainEventBus();
    private final ObjectMapper objectMapper;
    private final String notificationServiceUrl;
    private final String auditServiceUrl;
    private final RabbitTemplate rabbitTemplate;

    public ReviewEventPublisher() {
        this(null, null, createDefaultMapper(), "http://localhost:8086", "http://localhost:8087");
    }

    public ReviewEventPublisher(ApplicationEventPublisher springPublisher) {
        this(springPublisher, null, createDefaultMapper(), "http://localhost:8086", "http://localhost:8087");
    }

    public ReviewEventPublisher(ApplicationEventPublisher springPublisher, RabbitTemplate rabbitTemplate) {
        this(springPublisher, rabbitTemplate, createDefaultMapper(), "http://localhost:8086", "http://localhost:8087");
    }

    @Autowired
    public ReviewEventPublisher(@Autowired(required = false) ApplicationEventPublisher springPublisher,
                                @Autowired(required = false) RabbitTemplate rabbitTemplate,
                                @Autowired(required = false) ObjectMapper objectMapper,
                                @Value("${services.notification.url:http://localhost:8086}") String notificationServiceUrl,
                                @Value("${services.audit.url:http://localhost:8087}") String auditServiceUrl) {
        this.springPublisher = springPublisher;
        this.rabbitTemplate = rabbitTemplate;
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

    public RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) return;
        log.info("Publicando evento desde review-service [{}]: aggregateId={}, correlationId={}",
                event.getEventType(), event.getAggregateId(), event.getCorrelationId());

        domainEventBus.publish(event);

        if (springPublisher != null) {
            springPublisher.publishEvent(event);
        }

        String bearerToken = ResourceServerSecurityConfig.currentBearerToken();
        try {
            String json = objectMapper.writeValueAsString(event);
            if (rabbitTemplate != null) {
                String routingKey = RabbitMQConstants.ROUTING_KEY_PREFIX + event.getEventType();
                rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE_NAME, routingKey, json);
                log.info("[ReviewEventPublisher] Evento publicado en RabbitMQ exchange '{}' (routingKey={})",
                        RabbitMQConstants.EXCHANGE_NAME, routingKey);
            } else {
                RemoteEventDispatcher.dispatchAsync(json, notificationServiceUrl, auditServiceUrl, bearerToken);
            }
        } catch (Exception e) {
            log.warn("Error enviando evento distribuido, intentando fallback HTTP: {}", e.getMessage());
            try {
                String json = objectMapper.writeValueAsString(event);
                RemoteEventDispatcher.dispatchAsync(json, notificationServiceUrl, auditServiceUrl, bearerToken);
            } catch (Exception ex) {
                log.error("Fallo crítico serializando evento: {}", ex.getMessage());
            }
        }
    }
}
