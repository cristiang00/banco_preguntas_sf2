package co.edu.unicauca.bancopreguntas.domain.evaluation;

import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registro de evaluadores por tipo de pregunta (principio Open/Closed).
 */
public class QuestionEvaluatorRegistry {

    private final Map<TipoPregunta, QuestionEvaluator> evaluators = new EnumMap<>(TipoPregunta.class);

    /** @return registro por defecto con los tres evaluadores configurados. */
    public static QuestionEvaluatorRegistry withDefaults() {
        return new QuestionEvaluatorRegistry()
                .register(new SingleChoiceEvaluator())
                .register(new MultipleChoiceEvaluator())
                .register(new TrueFalseEvaluator());
    }

    public QuestionEvaluatorRegistry register(QuestionEvaluator evaluator) {
        Objects.requireNonNull(evaluator, "El evaluador es obligatorio");
        evaluators.put(evaluator.getSupportedType(), evaluator);
        return this;
    }

    /**
     * @throws IllegalArgumentException si no hay evaluador para el tipo
     */
    public QuestionEvaluator forType(TipoPregunta type) {
        QuestionEvaluator evaluator = evaluators.get(type);
        if (evaluator == null) {
            throw new IllegalArgumentException("No hay un evaluador registrado para el tipo '" + type + "'");
        }
        return evaluator;
    }
}
