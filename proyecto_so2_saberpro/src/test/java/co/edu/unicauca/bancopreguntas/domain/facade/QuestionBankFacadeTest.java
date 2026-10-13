package co.edu.unicauca.bancopreguntas.domain.facade;

import co.edu.unicauca.bancopreguntas.domain.decorator.DecoratedQuestionView;
import co.edu.unicauca.bancopreguntas.domain.decorator.Feature;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;
import co.edu.unicauca.bancopreguntas.domain.evaluation.EvaluationResult;
import co.edu.unicauca.bancopreguntas.domain.services.PreguntaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class QuestionBankFacadeTest {

    @Test
    @DisplayName("Mockito: createQuestion delega en validator y questionService en orden")
    void createQuestionDelegatesInOrder() {
        PreguntaService service = mock(PreguntaService.class);
        QuestionValidator validator = mock(QuestionValidator.class);
        QuestionPublisher publisher = mock(QuestionPublisher.class);

        QuestionBankFacade facade = new QuestionBankFacade(service, validator, publisher);
        Pregunta question = new Pregunta("¿Pregunta de prueba?", TipoPregunta.VERDADERO_FALSO);

        facade.createQuestion(question);

        InOrder inOrder = inOrder(validator, service);
        inOrder.verify(validator).validate(question);
        inOrder.verify(service).save(question);
    }

    @Test
    @DisplayName("Mockito: publishQuestion delega en validator, questionService.approve y publisher.publish")
    void publishQuestionDelegatesInOrder() {
        PreguntaService service = mock(PreguntaService.class);
        QuestionValidator validator = mock(QuestionValidator.class);
        QuestionPublisher publisher = mock(QuestionPublisher.class);

        QuestionBankFacade facade = new QuestionBankFacade(service, validator, publisher);
        Pregunta question = new Pregunta("¿Pregunta aprobada?", TipoPregunta.VERDADERO_FALSO);

        facade.publishQuestion(question);

        InOrder inOrder = inOrder(validator, service, publisher);
        inOrder.verify(validator).validate(question);
        inOrder.verify(service).approve(question);
        inOrder.verify(publisher).publish(question);
    }

    @Test
    @DisplayName("Flujo completo a través de Facade.createDefault() sin acoplamiento a subsistemas")
    void fullFlowWithDefaultFacade() {
        QuestionBankFacade facade = QuestionBankFacade.createDefault();

        // 1. Crear pregunta
        int id = facade.createQuestion("¿Cuál es un patrón estructural?", TipoPregunta.SELECCION_UNICA);
        assertTrue(id > 0);

        // 2. Agregar opciones
        facade.addOption(id, "A", "Decorator");
        facade.addOption(id, "B", "State");
        facade.addOption(id, "C", "Observer");

        // 3. Definir correctas
        facade.defineCorrectAnswers(id, "A");

        // 4. Intentar evaluar en Borrador -> DEBE FALLAR (solo publicada puede usarse en prueba)
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> facade.evaluateAnswer(id, List.of("A")));
        assertTrue(ex.getMessage().contains("prueba") || ex.getMessage().contains("Publicada"));
        assertTrue(ex.getMessage().contains("Borrador"));

        // 5. Transición por ciclo de vida
        facade.submitForReview(id);
        facade.assignReviewer(id, "Profesor Revisor");
        facade.approveQuestion(id);
        facade.publishQuestion(id);

        // 6. Evaluar con éxito una vez publicada
        EvaluationResult eval = facade.evaluateAnswer(id, List.of("A"));
        assertTrue(eval.isValid());
        assertTrue(eval.isCorrect());
        assertEquals(1.0, eval.getScore());

        // 7. Decorar acumulando 3 características y consultar
        facade.decorateQuestion(id, Feature.HIGH_DIFFICULTY);
        facade.decorateQuestion(id, Feature.CONTEXTUALIZED, "Examen Final");
        facade.decorateQuestion(id, Feature.FEEDBACK, "Revise patrones GoF");

        DecoratedQuestionView view = facade.describeQuestion(id);
        assertTrue(view.description().contains("[ALTA DIFICULTAD]"));
        assertTrue(view.description().contains("[CONTEXTUALIZADA: Examen Final]"));
        assertTrue(view.description().contains("[CON RETROALIMENTACIÓN: Revise patrones GoF]"));
        assertEquals(2.0, view.score(), 1e-9); // (1.0 * 1.5) + 0.5 = 2.0

        // 8. Importar pregunta externa
        Pregunta imported = facade.importDefaultExternalQuestion();
        assertNotNull(imported);
        assertEquals("Borrador", imported.getStateName());
        assertEquals("¿Cuál es la capital de Colombia?", imported.getStatement());

        // 9. Consultar preguntas por estado
        List<Pregunta> published = facade.listQuestionsByState("Publicada");
        assertEquals(1, published.size());
        assertEquals(id, published.get(0).getId());

        List<Pregunta> drafts = facade.listQuestionsByState("Borrador");
        assertTrue(drafts.stream().anyMatch(q -> q.getStatement().equals(imported.getStatement())));
    }
}
