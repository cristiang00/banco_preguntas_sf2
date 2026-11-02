package co.edu.unicauca.bancopreguntas.contracts.observer;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;

/**
 * Interfaz Observador (GoF Observer Pattern).
 * Permite que los suscriptores reaccionen a eventos de dominio de forma desacoplada
 * respecto al componente emisor o a la infraestructura subyacente.
 */
public interface DomainObserver {

    /**
     * Notificación de la ocurrencia de un evento de dominio.
     *
     * @param event evento de dominio emitido
     */
    void onEvent(DomainEvent event);

    /**
     * Tipo de evento al cual este observador se suscribe (o "*" para todos).
     */
    default String getSupportedEventType() {
        return "*";
    }
}

