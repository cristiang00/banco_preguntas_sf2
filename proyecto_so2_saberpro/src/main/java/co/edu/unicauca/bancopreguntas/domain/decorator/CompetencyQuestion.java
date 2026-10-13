package co.edu.unicauca.bancopreguntas.domain.decorator;

/**
 * Decorador para preguntas asociadas a una competencia de evaluación.
 */
public class CompetencyQuestion extends QuestionDecorator {

    private final String competency;

    public CompetencyQuestion(QuestionComponent question, String competency) {
        super(question);
        this.competency = requireDetail(competency, "Asociada a competencia");
    }

    @Override
    public String getDescription() {
        return question.getDescription() + " [COMPETENCIA: " + competency + "]";
    }
}
