package co.edu.unicauca.bancopreguntas.domain.decorator;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuestionDecoratorTest {

    private static final String STATEMENT = "¿Qué es arquitectura de software?";

    @Test
    @DisplayName("Ejemplo de la guía: HighDifficultyQuestion sobre BasicQuestion")
    void guideExample() {
        QuestionComponent question = new BasicQuestion(STATEMENT);
        question = new HighDifficultyQuestion(question);
        assertEquals(STATEMENT + " [ALTA DIFICULTAD]", question.getDescription());
        assertEquals(1.5, question.getScore());
    }

    @Test
    @DisplayName("Los cinco decoradores se apilan dinámicamente sin modificar BasicQuestion")
    void stackAllDecorators() {
        BasicQuestion basic = new BasicQuestion(STATEMENT);
        QuestionComponent decorated = new ReviewFlaggedQuestion(
                new CompetencyQuestion(
                        new FeedbackQuestion(
                                new ContextualizedQuestion(
                                        new HighDifficultyQuestion(basic), "Caso empresa X"),
                                "Repase vistas arquitectónicas"),
                        "Diseño de software"),
                "Verificar redacción");

        assertEquals(STATEMENT
                + " [ALTA DIFICULTAD]"
                + " [CONTEXTUALIZADA: Caso empresa X]"
                + " [CON RETROALIMENTACIÓN: Repase vistas arquitectónicas]"
                + " [COMPETENCIA: Diseño de software]"
                + " [MARCADA PARA REVISIÓN: Verificar redacción]", decorated.getDescription());
        assertEquals(2.0, decorated.getScore(), 1e-9); // (1.0 * 1.5) + 0.5
        assertEquals(STATEMENT, basic.getDescription());
        assertEquals(1.0, basic.getScore());
    }

    @Test
    @DisplayName("El orden de apilamiento influye en el puntaje")
    void orderAffectsScore() {
        QuestionComponent contextThenHard = new HighDifficultyQuestion(
                new ContextualizedQuestion(new BasicQuestion(STATEMENT), "ctx"));
        assertEquals(2.25, contextThenHard.getScore(), 1e-9); // (1.0 + 0.5) * 1.5
    }

    @Test
    @DisplayName("BasicQuestion se construye a partir de una Pregunta del dominio")
    void bridgeFromDomainQuestion() {
        Pregunta q = new Pregunta("El patrón Adapter convierte interfaces", TipoPregunta.VERDADERO_FALSO);
        QuestionComponent component = new FeedbackQuestion(new BasicQuestion(q), "Ver GoF");
        assertEquals("[Verdadero/Falso] El patrón Adapter convierte interfaces [CON RETROALIMENTACIÓN: Ver GoF]",
                component.getDescription());
    }

    @Test
    @DisplayName("Los decoradores con detalle exigen un texto válido")
    void detailRequired() {
        BasicQuestion basic = new BasicQuestion(STATEMENT);
        assertThrows(IllegalArgumentException.class, () -> new ContextualizedQuestion(basic, " "));
        assertThrows(IllegalArgumentException.class, () -> new CompetencyQuestion(basic, null));
        assertThrows(NullPointerException.class, () -> new HighDifficultyQuestion(null));
    }
}
