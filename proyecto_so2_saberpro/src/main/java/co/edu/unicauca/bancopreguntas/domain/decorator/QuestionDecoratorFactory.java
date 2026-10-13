package co.edu.unicauca.bancopreguntas.domain.decorator;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

import java.util.Objects;

/**
 * Fábrica y compositor dinámico de decoradores sobre instancias de {@link Pregunta}.
 */
public final class QuestionDecoratorFactory {

    private QuestionDecoratorFactory() {
    }

    /**
     * Compone dinámicamente los decoradores a partir de los datos reales de la Pregunta:
     * - nivelDificultad ("Alto") -> {@link HighDifficultyQuestion}
     * - contexto -> {@link ContextualizedQuestion}
     * - competencia -> {@link CompetencyQuestion}
     * - justificacion -> {@link FeedbackQuestion}
     * - motivoRechazo -> {@link ReviewFlaggedQuestion}
     */
    public static QuestionComponent decorateFromEntity(Pregunta pregunta) {
        Objects.requireNonNull(pregunta, "La pregunta es obligatoria");
        QuestionComponent comp = new BasicQuestion(pregunta);

        if (pregunta.getNivelDificultad() != null && pregunta.getNivelDificultad().equalsIgnoreCase("Alto")) {
            comp = new HighDifficultyQuestion(comp);
        }
        if (pregunta.getContexto() != null && !pregunta.getContexto().isBlank()) {
            comp = new ContextualizedQuestion(comp, pregunta.getContexto());
        }
        if (pregunta.getCompetencia() != null && !pregunta.getCompetencia().isBlank()) {
            comp = new CompetencyQuestion(comp, pregunta.getCompetencia());
        }
        if (pregunta.getJustificacion() != null && !pregunta.getJustificacion().isBlank()) {
            comp = new FeedbackQuestion(comp, pregunta.getJustificacion());
        }
        if (pregunta.getMotivoRechazo() != null && !pregunta.getMotivoRechazo().isBlank()) {
            comp = new ReviewFlaggedQuestion(comp, pregunta.getMotivoRechazo());
        }
        return comp;
    }

    /**
     * Aplica una característica específica a un componente base.
     */
    public static QuestionComponent applyFeature(QuestionComponent base, Feature feature, String detail) {
        Objects.requireNonNull(base, "El componente base es obligatorio");
        Objects.requireNonNull(feature, "La característica es obligatoria");

        return switch (feature) {
            case HIGH_DIFFICULTY -> new HighDifficultyQuestion(base);
            case CONTEXTUALIZED -> new ContextualizedQuestion(base, detail != null ? detail : "Contextualizada");
            case FEEDBACK -> new FeedbackQuestion(base, detail != null ? detail : "Retroalimentación");
            case COMPETENCY -> new CompetencyQuestion(base, detail != null ? detail : "Competencia");
            case REVIEW_FLAGGED -> new ReviewFlaggedQuestion(base, detail != null ? detail : "Revisión");
        };
    }
}
