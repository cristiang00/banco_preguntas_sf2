package co.edu.unicauca.bancopreguntas.review.infrastructure.rest;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.EventPublisherPort;
import co.edu.unicauca.bancopreguntas.review.messaging.ReviewEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura que implementa el puerto de salida {@link EventPublisherPort}
 * delegando al publicador de eventos {@link ReviewEventPublisher} (RabbitMQ o HTTP fallback).
 * (HU8 - Arquitectura Hexagonal)
 */
@Component
public class ReviewEventPublisherAdapter implements EventPublisherPort {

    private final ReviewEventPublisher reviewEventPublisher;

    public ReviewEventPublisherAdapter(ReviewEventPublisher reviewEventPublisher) {
        this.reviewEventPublisher = reviewEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        reviewEventPublisher.publish(event);
    }
}

