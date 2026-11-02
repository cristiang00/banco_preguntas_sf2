package co.edu.unicauca.bancopreguntas.contracts.observer;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;

/**
 * Publicador de eventos de dominio (GoF Observer Pattern / Sujeto).
 * Desacopla la lógica de negocio de la infraestructura de mensajería (RabbitMQ / In-Memory).
 */
public interface EventPublisher {

    /**
     * Publica un evento de dominio hacia los observadores o el broker de mensajes.
     *
     * @param event evento de dominio a publicar
     */
    void publish(DomainEvent event);
}

