package co.edu.unicauca.bancopreguntas.lifecycle.service;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaAprobadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaEnviadaARevisionEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaRechazadaEvent;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.HistorialTransicionEntity;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.PreguntaCicloVidaEntity;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.state.AccesoDenegadoException;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.state.QuestionState;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.state.QuestionStateFactory;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.state.TransicionInvalidaException;
import co.edu.unicauca.bancopreguntas.lifecycle.messaging.LifecycleEventPublisher;
import co.edu.unicauca.bancopreguntas.lifecycle.repository.HistorialTransicionRepository;
import co.edu.unicauca.bancopreguntas.lifecycle.repository.PreguntaCicloVidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LifecycleService {

    private final PreguntaCicloVidaRepository cicloVidaRepository;
    private final HistorialTransicionRepository historialRepository;
    private final LifecycleEventPublisher eventPublisher;

    public LifecycleService(PreguntaCicloVidaRepository cicloVidaRepository,
                            HistorialTransicionRepository historialRepository,
                            LifecycleEventPublisher eventPublisher) {
        this.cicloVidaRepository = cicloVidaRepository;
        this.historialRepository = historialRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PreguntaCicloVidaEntity registrarPreguntaInicial(Long preguntaId, Long autorId) {
        return cicloVidaRepository.findById(preguntaId).orElseGet(() -> {
            PreguntaCicloVidaEntity entity = new PreguntaCicloVidaEntity(preguntaId, autorId, EstadoPreguntaDTO.BORRADOR);
            return cicloVidaRepository.save(entity);
        });
    }

    @Transactional(readOnly = true)
    public EstadoPreguntaDTO obtenerEstado(Long preguntaId) {
        return cicloVidaRepository.findById(preguntaId)
                .map(PreguntaCicloVidaEntity::getEstado)
                .orElse(EstadoPreguntaDTO.BORRADOR);
    }

    @Transactional(readOnly = true)
    public boolean puedeEditar(Long preguntaId) {
        EstadoPreguntaDTO estado = obtenerEstado(preguntaId);
        QuestionState state = QuestionStateFactory.fromDTO(estado);
        return state.isEditable();
    }

    @Transactional
    public EstadoPreguntaDTO enviarARevision(Long preguntaId, Long usuarioAutenticadoId, String autorNombre,
                                             String contexto, String preguntaDirecta, String correlationId) {
        PreguntaCicloVidaEntity entity = cicloVidaRepository.findById(preguntaId)
                .orElseGet(() -> registrarPreguntaInicial(preguntaId, usuarioAutenticadoId));

        // SSCRUM-45: Validar autoría
        if (entity.getAutorId() != null && !entity.getAutorId().equals(usuarioAutenticadoId)) {
            throw new AccesoDenegadoException("No tiene permisos para enviar a revisión una pregunta creada por otro autor");
        }

        EstadoPreguntaDTO estadoAnterior = entity.getEstado();
        QuestionState estadoActual = entity.getEstadoActual();

        // SSCRUM-43: Aplicación del patrón State (lanza TransicionInvalidaException si no es Borrador)
        estadoActual.submitForReview(entity);

        cicloVidaRepository.save(entity);

        historialRepository.save(new HistorialTransicionEntity(
                preguntaId,
                estadoAnterior,
                entity.getEstado(),
                usuarioAutenticadoId,
                "Envío inicial a revisión por el autor"
        ));

        // SSCRUM-42: Publicación del evento en el broker
        PreguntaEnviadaARevisionEvent event = new PreguntaEnviadaARevisionEvent(
                preguntaId,
                entity.getAutorId(),
                autorNombre,
                contexto,
                preguntaDirecta,
                correlationId
        );
        eventPublisher.publish(event);

        return entity.getEstado();
    }

    @Transactional
    public EstadoPreguntaDTO aplicarDecisionRevisor(Long preguntaId, String decision, Long usuarioId,
                                                    String autorEmail, String contexto, List<String> observaciones,
                                                    String correlationId) {
        PreguntaCicloVidaEntity entity = cicloVidaRepository.findById(preguntaId)
                .orElseThrow(() -> new IllegalArgumentException("La pregunta con ID " + preguntaId + " no tiene registro de ciclo de vida"));

        EstadoPreguntaDTO estadoAnterior = entity.getEstado();
        QuestionState estadoActual = entity.getEstadoActual();

        if ("APROBADA".equalsIgnoreCase(decision) || "APROBAR".equalsIgnoreCase(decision)) {
            estadoActual.approve(entity);
            cicloVidaRepository.save(entity);
            historialRepository.save(new HistorialTransicionEntity(
                    preguntaId, estadoAnterior, entity.getEstado(), usuarioId, "Aprobada por consenso de revisores"
            ));

            eventPublisher.publish(new PreguntaAprobadaEvent(
                    preguntaId, entity.getAutorId(), "Autor", autorEmail, contexto, "Aprobada", correlationId
            ));

        } else if ("RECHAZADA".equalsIgnoreCase(decision) || "RECHAZAR".equalsIgnoreCase(decision)) {
            String motivo = (observaciones != null && !observaciones.isEmpty()) ? String.join("; ", observaciones) : "Rechazada por revisor";
            estadoActual.reject(entity, motivo);
            cicloVidaRepository.save(entity);
            historialRepository.save(new HistorialTransicionEntity(
                    preguntaId, estadoAnterior, entity.getEstado(), usuarioId, motivo
            ));

            eventPublisher.publish(new PreguntaRechazadaEvent(
                    preguntaId, entity.getAutorId(), "Autor", autorEmail, contexto, observaciones, correlationId
            ));
        } else {
            throw new IllegalArgumentException("Decisión inválida: " + decision);
        }

        return entity.getEstado();
    }
}

