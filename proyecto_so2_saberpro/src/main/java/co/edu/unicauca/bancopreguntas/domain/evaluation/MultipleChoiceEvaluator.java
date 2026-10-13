package co.edu.unicauca.bancopreguntas.domain.evaluation;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;

import java.util.List;
import java.util.Set;

/**
 * Evaluador de selección múltiple con múltiples respuestas: es correcta si coincide el conjunto de respuestas.
 */
public class MultipleChoiceEvaluator extends QuestionEvaluator {

    @Override
    public TipoPregunta getSupportedType() {
        return TipoPregunta.SELECCION_MULTIPLE;
    }

    @Override
    protected void validate(Pregunta question, List<String> answers) {
        long distinct = answers.stream().map(this::normalize).distinct().count();
        if (distinct != answers.size()) {
            throw new InvalidAnswerException("hay opciones seleccionadas más de una vez.");
        }
    }

    @Override
    protected boolean compareWithCorrectAnswers(Pregunta question, List<String> selected) {
        return Set.copyOf(selected).equals(question.getRespuestasCorrectas());
    }

    @Override
    protected String generateFeedback(Pregunta question, List<String> selected, boolean correct) {
        String base = super.generateFeedback(question, selected, correct);
        if (correct) {
            return base;
        }
        List<String> missing = question.getRespuestasCorrectas().stream().filter(k -> !selected.contains(k)).toList();
        List<String> extra = selected.stream().filter(k -> !question.getRespuestasCorrectas().contains(k)).toList();
        StringBuilder detail = new StringBuilder(base);
        if (!missing.isEmpty()) {
            detail.append(" Faltó seleccionar: ").append(describe(question, missing)).append('.');
        }
        if (!extra.isEmpty()) {
            detail.append(" Seleccionó de más: ").append(describe(question, extra)).append('.');
        }
        return detail.toString();
    }
}
