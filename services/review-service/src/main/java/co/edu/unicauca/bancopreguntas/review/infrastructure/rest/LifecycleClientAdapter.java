package co.edu.unicauca.bancopreguntas.review.infrastructure.rest;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.LifecycleClientPort;
import co.edu.unicauca.bancopreguntas.review.client.LifecycleServiceClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de infraestructura REST que implementa el puerto de salida {@link LifecycleClientPort}
 * delegando al cliente HTTP existente {@link LifecycleServiceClient} (HU8 - Arquitectura Hexagonal).
 */
@Component
public class LifecycleClientAdapter implements LifecycleClientPort {

    private final LifecycleServiceClient lifecycleServiceClient;

    public LifecycleClientAdapter(LifecycleServiceClient lifecycleServiceClient) {
        this.lifecycleServiceClient = lifecycleServiceClient;
    }

    @Override
    public EstadoPreguntaDTO obtenerEstado(Long preguntaId) {
        return lifecycleServiceClient.obtenerEstado(preguntaId);
    }

    @Override
    public void aplicarDecision(Long preguntaId, String decision, Long userId, String autorEmail,
                                String contexto, List<String> observaciones, String correlationId) {
        lifecycleServiceClient.aplicarDecision(preguntaId, decision, userId, autorEmail,
                contexto, observaciones, correlationId);
    }
}

