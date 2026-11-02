package co.edu.unicauca.bancopreguntas.auditservice.service;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.observer.DomainEventBus;
import co.edu.unicauca.bancopreguntas.contracts.observer.DomainObserver;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador de Auditoría (GoF Observer Pattern).
 * Escucha todos los eventos publicados en el bus desacoplado y los registra en la bitácora.
 */
@Component
public class AuditObserver implements DomainObserver {

    private static final Logger log = LoggerFactory.getLogger(AuditObserver.class);

    private final AuditService auditService;

    public AuditObserver(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostConstruct
    public void registrarEnBus() {
        DomainEventBus.getInstance().registerObserver(this);
        log.info("[AuditObserver] Registrado exitosamente en DomainEventBus para auditar todos los eventos.");
    }

    @Override
    public void onEvent(DomainEvent event) {
        try {
            auditService.registrarEvento(event);
        } catch (Exception e) {
            log.error("[AuditObserver] Error registrando auditoría para evento {}: {}",
                    event.getEventId(), e.getMessage(), e);
        }
    }

    @Override
    public String getSupportedEventType() {
        return "*"; // Observa todos los eventos de dominio
    }
}
