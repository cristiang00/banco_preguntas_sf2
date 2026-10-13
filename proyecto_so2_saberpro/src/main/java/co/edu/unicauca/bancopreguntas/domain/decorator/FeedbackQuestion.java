package co.edu.unicauca.bancopreguntas.domain.decorator;

/**
 * Decorador para preguntas con retroalimentación explícita.
 */
public class FeedbackQuestion extends QuestionDecorator {

    private final String feedback;

    public FeedbackQuestion(QuestionComponent question, String feedback) {
        super(question);
        this.feedback = requireDetail(feedback, "Con retroalimentación");
    }

    @Override
    public String getDescription() {
        return question.getDescription() + " [CON RETROALIMENTACIÓN: " + feedback + "]";
    }
}
