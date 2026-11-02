package co.edu.unicauca.bancopreguntas.notificationservice.factory;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificacionFactoryProvider {

    private final Map<String, NotificacionFactory> factoryMap = new ConcurrentHashMap<>();

    public NotificacionFactoryProvider(List<NotificacionFactory> factories) {
        if (factories != null) {
            for (NotificacionFactory f : factories) {
                factoryMap.put(f.getTipoSoportado().toUpperCase(), f);
            }
        }
    }

    public NotificacionFactory getFactory(String tipo) {
        if (tipo == null) return null;
        return factoryMap.get(tipo.toUpperCase());
    }
}

