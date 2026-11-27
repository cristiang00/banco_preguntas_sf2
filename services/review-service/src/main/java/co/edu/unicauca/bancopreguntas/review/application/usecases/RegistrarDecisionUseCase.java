package co.edu.unicauca.bancopreguntas.review.application.usecases;

import co.edu.unicauca.bancopreguntas.contracts.dto.DecisionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaAprobadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaRechazadaEvent;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.*;
import co.edu.unicauca.bancopreguntas.review.domain.model.Observacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;
import co.edu.unicauca.bancopreguntas.review.domain.policy.PoliticaConsensoRevision;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso: Registrar veredicto final (Aprobar / Rechazar) con política de consenso (HU5 / HU12 / HU8).
 */
public class RegistrarDecisionUseCase {

    private final RevisionRepositoryPort repositoryPort;
    private final PreguntasClientPort preguntasClientPort;
    private final LifecycleClientPort lifecycleClientPort;
    private final UsuariosClientPort usuariosClientPort;
    private final EventPublisherPort eventPublisherPort;
    private final PoliticaConsensoRevision politicaConsenso;

    public RegistrarDecisionUseCase(RevisionRepositoryPort repositoryPort,
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

    public Revision execute(Long revisionId, DecisionRequestDTO req, Long revisorId, String correlationId) {
        Revision rev = repositoryPort.findById(revisionId)
                .orElseThrow(() -> new IllegalArgumentException("La revisión con ID " + revisionId + " no existe"));

        politicaConsenso.validarAsignacionRevisor(rev, revisorId);
        politicaConsenso.validarNoReDecision(rev);

        String decision = req.getDecision() != null ? req.getDecision().trim().toUpperCase() : "";
        Long preguntaId = rev.getPreguntaId();
        int rondaActual = rev.getRonda();

        PreguntaDTO pregunta = preguntasClientPort.obtenerPregunta(preguntaId)
                .orElseThrow(() -> new IllegalArgumentException("Pregunta no encontrada"));

        // Obtener correo real del autor (HU12)
        String autorEmail = usuariosClientPort.buscarPorId(pregunta.getAutorId())
                .map(UserSummaryDTO::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .orElse("autor@unicauca.edu.co");

        if ("RECHAZAR".equals(decision) || "RECHAZADA".equals(decision)) {
            List<Observacion> obsGuardadas = repositoryPort.findObservacionesByRevisionId(revisionId);
            politicaConsenso.validarRechazoConObservaciones(req.getObservaciones(), !obsGuardadas.isEmpty());

            if (req.getObservaciones() != null) {
                for (String txt : req.getObservaciones()) {
                    if (txt != null && !txt.trim().isEmpty()) {
                        repositoryPort.saveObservacion(Observacion.crear(revisionId, preguntaId, txt.trim()));
                    }
                }
            }

            rev.rechazar();
            repositoryPort.save(rev);

            // Política de rechazo: Si un revisor rechaza, cerrar demás revisiones pendientes de la ronda actual
            List<Revision> revisionesRonda = politicaConsenso.filtrarRevisionesDeRonda(
                    repositoryPort.findByPreguntaId(preguntaId), rondaActual
            );
            for (Revision otra : revisionesRonda) {
                if (!otra.getId().equals(revisionId) && !otra.getEstado().estaFinalizada()) {
                    otra.cerrarPorRechazo();
                    repositoryPort.save(otra);
                }
            }

            List<String> obsRonda = revisionesRonda.stream()
                    .flatMap(r -> repositoryPort.findObservacionesByRevisionId(r.getId()).stream())
                    .filter(Observacion::isVisibleAutor)
                    .map(Observacion::getTexto)
                    .collect(Collectors.toList());
            if (obsRonda.isEmpty()) {
                obsRonda = repositoryPort.findObservacionesByPreguntaId(preguntaId)
                        .stream().filter(Observacion::isVisibleAutor).map(Observacion::getTexto).collect(Collectors.toList());
            }

            lifecycleClientPort.aplicarDecision(preguntaId, "RECHAZADA", revisorId, autorEmail,
                    pregunta.getContexto(), obsRonda, correlationId);

            preguntasClientPort.actualizarProyeccionEstado(preguntaId, "RECHAZADA");

            PreguntaRechazadaEvent rechazoEvent = new PreguntaRechazadaEvent(
                    preguntaId, pregunta.getAutorId(), pregunta.getAutorNombre(), autorEmail,
                    pregunta.getContexto(), obsRonda, correlationId
            );
            eventPublisherPort.publish(rechazoEvent);

        } else if ("APROBAR".equals(decision) || "APROBADA".equals(decision)) {
            rev.aprobar();
            repositoryPort.save(rev);

            List<Revision> todas = repositoryPort.findByPreguntaId(preguntaId);
            boolean todosAprobaron = politicaConsenso.determinarConsensoAprobacionRonda(todas, rondaActual);

            if (todosAprobaron) {
                lifecycleClientPort.aplicarDecision(preguntaId, "APROBADA", revisorId, autorEmail,
                        pregunta.getContexto(), Collections.emptyList(), correlationId);

                preguntasClientPort.actualizarProyeccionEstado(preguntaId, "APROBADA");

                PreguntaAprobadaEvent aprobadaEvent = new PreguntaAprobadaEvent(
                        preguntaId, pregunta.getAutorId(), pregunta.getAutorNombre(), autorEmail,
                        pregunta.getContexto(), "Pregunta aprobada por todos los revisores", correlationId
                );
                eventPublisherPort.publish(aprobadaEvent);
            }
        } else {
            throw new IllegalArgumentException("Decisión inválida. Valores permitidos: APROBAR o RECHAZAR");
        }

        return rev;
    }
}

