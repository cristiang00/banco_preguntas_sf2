package co.edu.unicauca.bancopreguntas.domain.decorator;

/**
 * Decorador de pregunta contextualizada: agrega un bono de 0.5 al puntaje.
 */
public class ContextualizedQuestion extends QuestionDecorator {

    public static final double CONTEXT_BONUS = 0.5;

    private final String context;

    public ContextualizedQuestion(QuestionComponent question, String context) {
        super(question);
        this.context = requireDetail(context, "Contextualizada");
    }

    @Override
    public String getDescription() {
        return question.getDescription() + " [CONTEXTUALIZADA: " + context + "]";
    }

    @Override
    public double getScore() {
        return question.getScore() + CONTEXT_BONUS;
    }
}
