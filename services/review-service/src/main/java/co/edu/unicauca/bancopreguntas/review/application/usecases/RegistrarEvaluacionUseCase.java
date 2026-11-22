package co.edu.unicauca.bancopreguntas.review.application.usecases;

import co.edu.unicauca.bancopreguntas.contracts.dto.EvaluacionRequestDTO;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.RevisionRepositoryPort;
import co.edu.unicauca.bancopreguntas.review.domain.model.Evaluacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Observacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;
import co.edu.unicauca.bancopreguntas.review.domain.policy.PoliticaConsensoRevision;

/**
 * Caso de uso: Registrar evaluación y observaciones técnicas/pedagógicas (HU5 / HU8).
 */
public class RegistrarEvaluacionUseCase {

    private final RevisionRepositoryPort repositoryPort;
    private final PoliticaConsensoRevision politicaConsenso;

    public RegistrarEvaluacionUseCase(RevisionRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
        this.politicaConsenso = new PoliticaConsensoRevision();
    }

    public Evaluacion execute(Long revisionId, EvaluacionRequestDTO req, Long revisorId) {
        Revision rev = repositoryPort.findById(revisionId)
                .orElseThrow(() -> new IllegalArgumentException("La revisión con ID " + revisionId + " no existe"));

        politicaConsenso.validarAsignacionRevisor(rev, revisorId);
        politicaConsenso.validarNoReDecision(rev);

        Evaluacion eval = Evaluacion.crear(
                revisionId,
                req.getCriterio() != null ? req.getCriterio() : "General",
                req.getCalificacion() != null ? req.getCalificacion() : 5.0,
                req.getComentario()
        );
        Evaluacion guardada = repositoryPort.saveEvaluacion(eval);

        if (req.getObservaciones() != null) {
            for (String obsText : req.getObservaciones()) {
                if (obsText != null && !obsText.trim().isEmpty()) {
                    repositoryPort.saveObservacion(Observacion.crear(revisionId, rev.getPreguntaId(), obsText.trim()));
                }
            }
        }

        rev.evaluar();
        repositoryPort.save(rev);

        return guardada;
    }
}

