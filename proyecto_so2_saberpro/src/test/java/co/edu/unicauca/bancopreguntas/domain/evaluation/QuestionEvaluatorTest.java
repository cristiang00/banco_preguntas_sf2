package co.edu.unicauca.bancopreguntas.domain.evaluation;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionEvaluatorTest {

    static Pregunta singleChoice() {
        Pregunta q = new Pregunta("¿Cuál es la capital de Colombia?", TipoPregunta.SELECCION_UNICA);
        q.addOption("A", "Medellín");
        q.addOption("B", "Bogotá");
        q.addOption("C", "Cali");
        q.defineCorrectAnswers("B");
        return q;
    }

    static Pregunta multipleChoice() {
        Pregunta q = new Pregunta("¿Cuáles son patrones estructurales?", TipoPregunta.SELECCION_MULTIPLE);
        q.addOption("A", "Decorator");
        q.addOption("B", "State");
        q.addOption("C", "Adapter");
        q.addOption("D", "Template Method");
        q.defineCorrectAnswers("A", "C");
        return q;
    }

    static Pregunta trueFalse() {
        Pregunta q = new Pregunta("Facade simplifica la interacción con subsistemas", TipoPregunta.VERDADERO_FALSO);
        q.defineCorrectAnswers("V");
        return q;
    }

    @Test
    @DisplayName("evaluate() es el método plantilla y está declarado final")
    void templateMethodIsFinal() throws NoSuchMethodException {
        int modifiers = QuestionEvaluator.class.getMethod("evaluate", Pregunta.class, List.class).getModifiers();
        assertTrue(Modifier.isFinal(modifiers));
        assertTrue(Modifier.isAbstract(QuestionEvaluator.class.getModifiers()));
    }

    @Nested
    @DisplayName("Selección única")
    class Single {
        private final QuestionEvaluator evaluator = new SingleChoiceEvaluator();

        @Test
        void correctAnswer() {
            EvaluationResult r = evaluator.evaluate(singleChoice(), List.of(" b "));
            assertTrue(r.isValid());
            assertTrue(r.isCorrect());
            assertEquals(1.0, r.getScore());
            assertTrue(r.getFeedback().startsWith("¡Correcto!"));
        }

        @Test
        void incorrectAnswer() {
            EvaluationResult r = evaluator.evaluate(singleChoice(), List.of("A"));
            assertTrue(r.isValid());
            assertFalse(r.isCorrect());
            assertEquals(0.0, r.getScore());
            assertTrue(r.getFeedback().contains("B) Bogotá"));
        }

        @Test
        @DisplayName("Rama 'No': más de una respuesta, vacía o inexistente es inválida")
        void invalidAnswers() {
            assertInvalid(evaluator.evaluate(singleChoice(), List.of("A", "B")), "exactamente una");
            assertInvalid(evaluator.evaluate(singleChoice(), List.of()), "al menos una");
            assertInvalid(evaluator.evaluate(singleChoice(), null), "al menos una");
            assertInvalid(evaluator.evaluate(singleChoice(), List.of("Z")), "no existe");
            assertInvalid(evaluator.evaluate(singleChoice(), Arrays.asList(" ")), "vacías");
        }
    }

    @Nested
    @DisplayName("Selección múltiple")
    class Multiple {
        private final QuestionEvaluator evaluator = new MultipleChoiceEvaluator();

        @Test
        void correctAnswerInAnyOrder() {
            EvaluationResult r = evaluator.evaluate(multipleChoice(), List.of("c", "A"));
            assertTrue(r.isCorrect());
        }

        @Test
        @DisplayName("Respuesta parcial o con opciones de más es incorrecta y explica el detalle")
        void incorrectAnswer() {
            EvaluationResult partial = evaluator.evaluate(multipleChoice(), List.of("A"));
            assertFalse(partial.isCorrect());
            assertTrue(partial.getFeedback().contains("Faltó seleccionar: C) Adapter"));

            EvaluationResult extra = evaluator.evaluate(multipleChoice(), List.of("A", "B", "C"));
            assertFalse(extra.isCorrect());
            assertTrue(extra.getFeedback().contains("Seleccionó de más: B) State"));
        }

        @Test
        @DisplayName("Rama 'No': opciones repetidas o inexistentes son inválidas")
        void invalidAnswers() {
            assertInvalid(evaluator.evaluate(multipleChoice(), List.of("A", "a")), "más de una vez");
            assertInvalid(evaluator.evaluate(multipleChoice(), List.of("A", "X")), "no existe");
        }
    }

    @Nested
    @DisplayName("Verdadero/Falso")
    class TrueFalse {
        private final QuestionEvaluator evaluator = new TrueFalseEvaluator();

        @Test
        void correctAnswerWithSynonyms() {
            assertTrue(evaluator.evaluate(trueFalse(), List.of("V")).isCorrect());
            assertTrue(evaluator.evaluate(trueFalse(), List.of("Verdadero")).isCorrect());
            assertTrue(evaluator.evaluate(trueFalse(), List.of("true")).isCorrect());
        }

        @Test
        void incorrectAnswer() {
            EvaluationResult r = evaluator.evaluate(trueFalse(), List.of("Falso"));
            assertTrue(r.isValid());
            assertFalse(r.isCorrect());
            assertTrue(r.getFeedback().contains("V) Verdadero"));
        }

        @Test
        @DisplayName("Rama 'No': valor distinto de V/F o ambas opciones es inválido")
        void invalidAnswers() {
            assertInvalid(evaluator.evaluate(trueFalse(), List.of("Quizás")), "no existe");
            assertInvalid(evaluator.evaluate(trueFalse(), List.of("V", "F")), "Verdadero o Falso");
        }
    }

    @Test
    @DisplayName("Un evaluador rechaza preguntas de otro tipo")
    void rejectsOtherTypes() {
        assertThrows(IllegalArgumentException.class,
                () -> new SingleChoiceEvaluator().evaluate(trueFalse(), List.of("V")));
    }

    @Test
    @DisplayName("El registro entrega el evaluador correcto por tipo")
    void registryResolvesByType() {
        QuestionEvaluatorRegistry registry = QuestionEvaluatorRegistry.withDefaults();
        assertInstanceOf(SingleChoiceEvaluator.class, registry.forType(TipoPregunta.SELECCION_UNICA));
        assertInstanceOf(MultipleChoiceEvaluator.class, registry.forType(TipoPregunta.SELECCION_MULTIPLE));
        assertInstanceOf(TrueFalseEvaluator.class, registry.forType(TipoPregunta.VERDADERO_FALSO));
        assertThrows(IllegalArgumentException.class,
                () -> new QuestionEvaluatorRegistry().forType(TipoPregunta.VERDADERO_FALSO));
    }

    private static void assertInvalid(EvaluationResult result, String expectedMessagePart) {
        assertFalse(result.isValid());
        assertFalse(result.isCorrect());
        assertEquals(0.0, result.getScore());
        assertTrue(result.getFeedback().startsWith("Respuesta no válida"), result.getFeedback());
        assertTrue(result.getFeedback().contains(expectedMessagePart), result.getFeedback());
    }
}
