package co.edu.unicauca.bancopreguntas.contracts.observer;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bus de eventos que implementa el Sujeto del patrón Observer (GoF).
 * Permite registrar múltiples observadores que reaccionan a los eventos publicados.
 */
public class DomainEventBus implements EventPublisher {

    private static final DomainEventBus INSTANCE = new DomainEventBus();

    public static DomainEventBus getInstance() {
        return INSTANCE;
    }

    private final List<DomainObserver> observers = new CopyOnWriteArrayList<>();

    public void registerObserver(DomainObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(DomainObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            return;
        }
        for (DomainObserver observer : observers) {
            String supportedType = observer.getSupportedEventType();
            if ("*".equals(supportedType) || supportedType.equalsIgnoreCase(event.getEventType())) {
                try {
                    observer.onEvent(event);
                } catch (Exception e) {
                    System.err.println("Error procesando evento en observador " + observer.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}

