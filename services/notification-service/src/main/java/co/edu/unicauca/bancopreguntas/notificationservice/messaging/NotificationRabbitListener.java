package co.edu.unicauca.bancopreguntas.notificationservice.messaging;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.EventParser;
import co.edu.unicauca.bancopreguntas.contracts.events.RabbitMQConstants;
import co.edu.unicauca.bancopreguntas.notificationservice.service.NotificacionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Listener AMQP para recepción asíncrona de eventos de dominio en Notification Service (HU12).
 */
@Component
@ConditionalOnClass(RabbitListener.class)
public class NotificationRabbitListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationRabbitListener.class);

    private final NotificacionService notificacionService;
    private final ObjectMapper objectMapper;

    public NotificationRabbitListener(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @RabbitListener(queues = RabbitMQConstants.NOTIFICATIONS_QUEUE)
    public void receiveMessage(String messageJson) {
        try {
            log.info("[NotificationRabbitListener] Evento recibido desde RabbitMQ: {}", messageJson);
            Map<String, Object> map = objectMapper.readValue(messageJson, new TypeReference<Map<String, Object>>() {});
            DomainEvent event = EventParser.parse(map);
            if (event != null) {
                notificacionService.procesarEvento(event);
                log.info("[NotificationRabbitListener] Evento {} procesado exitosamente", event.getEventType());
            } else {
                log.warn("[NotificationRabbitListener] No se pudo deserializar evento: {}", messageJson);
            }
        } catch (Exception e) {
            log.error("[NotificationRabbitListener] Error procesando mensaje de RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
