package co.edu.unicauca.bancopreguntas.contracts.events;

/**
 * Constantes de mensajería asíncrona mediante broker RabbitMQ (HU12).
 */
public final class RabbitMQConstants {

    private RabbitMQConstants() {}

    public static final String EXCHANGE_NAME = "saberpro.events";
    public static final String NOTIFICATIONS_QUEUE = "saberpro.notifications.queue";
    public static final String AUDIT_QUEUE = "saberpro.audit.queue";
    public static final String ROUTING_KEY_PREFIX = "saberpro.";
    public static final String ALL_EVENTS_ROUTING_KEY = "saberpro.#";
}

