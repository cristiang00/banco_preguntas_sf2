package co.edu.unicauca.bancopreguntas.domain.evaluation;

import co.edu.unicauca.bancopreguntas.domain.entities.OpcionRespuesta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Evaluador de preguntas de tipo Verdadero/Falso.
 */
public class TrueFalseEvaluator extends QuestionEvaluator {

    private static final Map<String, String> SYNONYMS = Map.of(
            "VERDADERO", OpcionRespuesta.TRUE_KEY,
            "TRUE", OpcionRespuesta.TRUE_KEY,
            "FALSO", OpcionRespuesta.FALSE_KEY,
            "FALSE", OpcionRespuesta.FALSE_KEY);

    @Override
    public TipoPregunta getSupportedType() {
        return TipoPregunta.VERDADERO_FALSO;
    }

    @Override
    protected void validate(Pregunta question, List<String> answers) {
        if (answers.size() != 1) {
            throw new InvalidAnswerException("debe seleccionar solo Verdadero o Falso (una única opción).");
        }
    }

    @Override
    protected boolean compareWithCorrectAnswers(Pregunta question, List<String> selected) {
        return question.getRespuestasCorrectas().contains(selected.get(0));
    }

    @Override
    protected String normalize(String answer) {
        String upper = answer.trim().toUpperCase(Locale.ROOT);
        return SYNONYMS.getOrDefault(upper, upper);
    }
}
