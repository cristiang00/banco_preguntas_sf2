package co.edu.unicauca.bancopreguntas.review.application.usecases;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.contracts.events.RevisoresAsignadosEvent;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.EventPublisherPort;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.LifecycleClientPort;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.PreguntasClientPort;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.RevisionRepositoryPort;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.UsuariosClientPort;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;
import co.edu.unicauca.bancopreguntas.review.domain.policy.PoliticaConsensoRevision;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Caso de uso: Asignar revisores a una pregunta en revisión (HU4 / HU8).
 */
public class AsignarRevisoresUseCase {

    private final RevisionRepositoryPort repositoryPort;
    private final PreguntasClientPort preguntasClientPort;
    private final LifecycleClientPort lifecycleClientPort;
    private final UsuariosClientPort usuariosClientPort;
    private final EventPublisherPort eventPublisherPort;
    private final PoliticaConsensoRevision politicaConsenso;

    public AsignarRevisoresUseCase(RevisionRepositoryPort repositoryPort,
                                  PreguntasClientPort preguntasClientPort,
                                  LifecycleClientPort lifecycleClientPort,
                                  UsuariosClientPort usuariosClientPort,
                                  EventPublisherPort eventPublisherPort) {
        this.repositoryPort = repositoryPort;
        this.preguntasClientPort = preguntasClientPort;
        this.lifecycleClientPort = lifecycleClientPort;
        this.usuariosClientPort = usuariosClientPort;
        this.eventPublisherPort = eventPublisherPort;
        this.politicaConsenso = new PoliticaConsensoRevision();
    }

    public List<Revision> execute(Long preguntaId, List<Long> revisorIds, Long adminId, String correlationId) {
        politicaConsenso.validarCantidadRevisores(revisorIds);

        PreguntaDTO pregunta = preguntasClientPort.obtenerPregunta(preguntaId)
                .orElseThrow(() -> new IllegalArgumentException("La pregunta con ID " + preguntaId + " no existe"));

        politicaConsenso.validarExclusionAutor(pregunta.getAutorId(), revisorIds);

        EstadoPreguntaDTO estado = lifecycleClientPort.obtenerEstado(preguntaId);
        if (estado != EstadoPreguntaDTO.PENDIENTE_REVISION) {
            throw new IllegalStateException("Solo las preguntas en estado Pendiente de revisión pueden tener revisores asignados");
        }

        List<RevisoresAsignadosEvent.RevisorInfo> revisoresInfo = new ArrayList<>();
        List<Revision> asignacionesCreadas = new ArrayList<>();

        for (Long revId : revisorIds) {
            UserSummaryDTO user = usuariosClientPort.buscarPorId(revId)
                    .orElseThrow(() -> new IllegalArgumentException("El usuario revisor con ID " + revId + " no existe"));

            if (!"REVISOR".equalsIgnoreCase(user.getRol())) {
                throw new IllegalArgumentException("El usuario " + user.getNombreUsuario() + " no tiene rol de Revisor");
            }

            if (!"ACTIVO".equalsIgnoreCase(user.getEstado())) {
                throw new IllegalArgumentException("El revisor " + user.getNombreUsuario() + " está inactivo");
            }

            Optional<Revision> existente = repositoryPort.findByPreguntaIdAndRevisorId(preguntaId, revId);
            Revision rev;
            if (existente.isEmpty()) {
                rev = Revision.crearNueva(preguntaId, revId, user.getNombreCompleto(), user.getEmail(),
                        pregunta.getCompetencia(), pregunta.getNivelDificultad());
            } else {
                rev = existente.get();
                rev.reactivar();
                rev.setCompetencia(pregunta.getCompetencia());
                rev.setNivelDificultad(pregunta.getNivelDificultad());
            }
            Revision guardada = repositoryPort.save(rev);
            asignacionesCreadas.add(guardada);

            revisoresInfo.add(new RevisoresAsignadosEvent.RevisorInfo(user.getId(), user.getNombreCompleto(), user.getEmail()));
        }

        RevisoresAsignadosEvent event = new RevisoresAsignadosEvent(
                preguntaId, pregunta.getAutorId(), pregunta.getAutorNombre(), pregunta.getContexto(),
                revisoresInfo, correlationId
        );
        eventPublisherPort.publish(event);

        return asignacionesCreadas;
    }
}

