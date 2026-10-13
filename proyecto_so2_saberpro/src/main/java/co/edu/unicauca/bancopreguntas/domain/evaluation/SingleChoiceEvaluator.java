package co.edu.unicauca.bancopreguntas.domain.evaluation;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;

import java.util.List;

/**
 * Evaluador de selección múltiple con única respuesta: el estudiante debe seleccionar exactamente una opción.
 */
public class SingleChoiceEvaluator extends QuestionEvaluator {

    @Override
    public TipoPregunta getSupportedType() {
        return TipoPregunta.SELECCION_UNICA;
    }

    @Override
    protected void validate(Pregunta question, List<String> answers) {
        if (answers.size() != 1) {
            throw new InvalidAnswerException("una pregunta de selección única admite exactamente una respuesta; "
                    + "se recibieron " + answers.size() + ".");
        }
    }

    @Override
    protected boolean compareWithCorrectAnswers(Pregunta question, List<String> selected) {
        return question.getRespuestasCorrectas().contains(selected.get(0));
    }
}
