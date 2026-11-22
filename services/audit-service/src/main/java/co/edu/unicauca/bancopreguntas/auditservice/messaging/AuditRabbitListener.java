package co.edu.unicauca.bancopreguntas.auditservice.messaging;

import co.edu.unicauca.bancopreguntas.auditservice.service.AuditService;
import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.EventParser;
import co.edu.unicauca.bancopreguntas.contracts.events.RabbitMQConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Listener AMQP para registro automático en la bitácora de auditoría (HU12).
 */
@Component
@ConditionalOnClass(RabbitListener.class)
public class AuditRabbitListener {

    private static final Logger log = LoggerFactory.getLogger(AuditRabbitListener.class);

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public AuditRabbitListener(AuditService auditService) {
        this.auditService = auditService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @RabbitListener(queues = RabbitMQConstants.AUDIT_QUEUE)
    public void receiveMessage(String messageJson) {
        try {
            log.info("[AuditRabbitListener] Evento recibido desde RabbitMQ: {}", messageJson);
            Map<String, Object> map = objectMapper.readValue(messageJson, new TypeReference<Map<String, Object>>() {});
            DomainEvent event = EventParser.parse(map);
            if (event != null) {
                auditService.registrarEvento(event);
                log.info("[AuditRabbitListener] Evento {} registrado en bitacora de auditoria", event.getEventType());
            } else {
                log.warn("[AuditRabbitListener] No se pudo deserializar evento: {}", messageJson);
            }
        } catch (Exception e) {
            log.error("[AuditRabbitListener] Error registrando auditoría desde RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
