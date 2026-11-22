package co.edu.unicauca.bancopreguntas.review.application.ports.output;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;

/**
 * Puerto de salida para publicación de eventos de dominio hacia el broker o bus.
 */
public interface EventPublisherPort {

    void publish(DomainEvent event);
}

