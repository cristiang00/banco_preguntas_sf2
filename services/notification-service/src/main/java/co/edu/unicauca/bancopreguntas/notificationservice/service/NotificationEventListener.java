package co.edu.unicauca.bancopreguntas.notificationservice.service;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.observer.DomainEventBus;
import co.edu.unicauca.bancopreguntas.contracts.observer.DomainObserver;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Suscriptor / Observador (GoF Observer Pattern) que escucha eventos de dominio
 * y dispara el procesamiento de notificaciones.
 */
@Component
public class NotificationEventListener implements DomainObserver {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificacionService notificacionService;

    public NotificationEventListener(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PostConstruct
    public void registrarEnBus() {
        DomainEventBus.getInstance().registerObserver(this);
        log.info("[NotificationEventListener] Registrado en DomainEventBus para observar eventos de dominio.");
    }

    @Override
    public void onEvent(DomainEvent event) {
        try {
            notificacionService.procesarEvento(event);
        } catch (Exception e) {
            log.error("[NotificationEventListener] Error procesando evento {}: {}", event.getEventId(), e.getMessage(), e);
        }
    }

    @Override
    public String getSupportedEventType() {
        return "*";
    }
}
