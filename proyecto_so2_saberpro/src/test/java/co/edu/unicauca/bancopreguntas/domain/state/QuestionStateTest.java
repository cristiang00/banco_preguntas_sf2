package co.edu.unicauca.bancopreguntas.domain.state;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionStateTest {

    private Pregunta question;

    @BeforeEach
    void setUp() {
        question = completeQuestion();
    }

    static Pregunta completeQuestion() {
        Pregunta q = new Pregunta("¿Qué patrón encapsula estados en objetos?", TipoPregunta.SELECCION_UNICA);
        q.addOption("A", "State");
        q.addOption("B", "Adapter");
        q.defineCorrectAnswers("A");
        return q;
    }

    @Test
    @DisplayName("Una pregunta nueva inicia en Borrador")
    void initialStateIsDraft() {
        assertInstanceOf(DraftState.class, question.getQuestionState());
        assertEquals("Borrador", question.getStateName());
        assertEquals(EstadoPregunta.BORRADOR, question.getEstado());
    }

    @Test
    @DisplayName("Flujo feliz: Borrador → Pendiente → En revisión → Aprobada → Publicada → Archivada")
    void happyPath() {
        question.submitForReview();
        assertEquals("Pendiente de revisión", question.getStateName());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, question.getEstado());

        question.assignReviewer("Ana Revisora");
        assertEquals("En revisión", question.getStateName());
        assertEquals(EstadoPregunta.EN_REVISION, question.getEstado());
        assertEquals("Ana Revisora", question.getReviewer());

        question.approve();
        assertEquals("Aprobada", question.getStateName());
        assertEquals(EstadoPregunta.APROBADA, question.getEstado());

        question.publish();
        assertEquals("Publicada", question.getStateName());
        assertEquals(EstadoPregunta.PUBLICADA, question.getEstado());

        question.archive();
        assertEquals("Archivada", question.getStateName());
        assertEquals(EstadoPregunta.ARCHIVADA, question.getEstado());
    }

    @Test
    @DisplayName("Flujo de rechazo: En revisión → Rechazada → Borrador")
    void rejectionLoop() {
        question.submitForReview();
        question.assignReviewer("Ana");
        question.reject("Distractores poco plausibles");
        assertInstanceOf(RejectedState.class, question.getQuestionState());
        assertEquals("Rechazada", question.getStateName());
        assertEquals(EstadoPregunta.RECHAZADA, question.getEstado());
        assertEquals("Distractores poco plausibles", question.getRejectionReason());

        question.returnToDraft();
        assertInstanceOf(DraftState.class, question.getQuestionState());
        assertEquals(EstadoPregunta.BORRADOR, question.getEstado());
        assertDoesNotThrow(() -> question.addOption("C", "Facade"));
    }

    @Nested
    @DisplayName("Tabla 2: permisos para operaciones")
    class Table2 {

        @Test
        @DisplayName("1. Borrador → En revisión: Permitido")
        void draftToReviewAllowed() {
            question.submitForReview();
            question.assignReviewer("Revisor");
            assertInstanceOf(ReviewState.class, question.getQuestionState());
        }

        @Test
        @DisplayName("2. En revisión → Aprobada: Permitido")
        void reviewToApprovedAllowed() {
            question.setQuestionState(new ReviewState());
            question.approve();
            assertInstanceOf(ApprovedState.class, question.getQuestionState());
        }

        @Test
        @DisplayName("3. Aprobada → Publicada: Permitido")
        void approvedToPublishedAllowed() {
            question.setQuestionState(new ApprovedState());
            question.publish();
            assertInstanceOf(PublishedState.class, question.getQuestionState());
        }

        @Test
        @DisplayName("4. Borrador → Publicada: No permitido")
        void draftToPublishedDenied() {
            IllegalStateException ex = assertThrows(IllegalStateException.class, question::publish);
            assertTrue(ex.getMessage().contains("publicar"));
            assertTrue(ex.getMessage().contains("Borrador"));
            assertInstanceOf(DraftState.class, question.getQuestionState());
        }

        @Test
        @DisplayName("5. Publicada → Borrador: No permitido")
        void publishedToDraftDenied() {
            question.setQuestionState(new PublishedState());
            IllegalStateException ex = assertThrows(IllegalStateException.class, question::returnToDraft);
            assertTrue(ex.getMessage().contains("Publicada"));
            assertInstanceOf(PublishedState.class, question.getQuestionState());
        }
    }

    @Test
    @DisplayName("Transiciones inválidas en cada estado lanzan IllegalStateException")
    void invalidTransitionsPerState() {
        assertThrows(IllegalStateException.class, question::approve);
        assertThrows(IllegalStateException.class, () -> question.assignReviewer("x"));

        question.submitForReview();
        assertThrows(IllegalStateException.class, question::submitForReview);
        assertThrows(IllegalStateException.class, question::approve);

        question.assignReviewer("Revisor");
        assertThrows(IllegalStateException.class, question::publish);
        assertThrows(IllegalStateException.class, question::archive);

        question.reject("motivo");
        assertThrows(IllegalStateException.class, question::approve);
        assertThrows(IllegalStateException.class, question::publish);

        question.returnToDraft();
        question.submitForReview();
        question.assignReviewer("Revisor");
        question.approve();
        assertThrows(IllegalStateException.class, () -> question.reject("tarde"));
        assertThrows(IllegalStateException.class, question::archive);

        question.publish();
        assertThrows(IllegalStateException.class, question::approve);

        question.archive();
        assertThrows(IllegalStateException.class, question::publish);
        assertThrows(IllegalStateException.class, question::returnToDraft);
        assertThrows(IllegalStateException.class, question::submitForReview);
    }

    @Test
    @DisplayName("No se puede enviar a revisión si el contenido está incompleto")
    void incompleteContentCannotBeSubmitted() {
        Pregunta incomplete = new Pregunta("Pregunta sin opciones", TipoPregunta.SELECCION_UNICA);
        IllegalStateException ex = assertThrows(IllegalStateException.class, incomplete::submitForReview);
        assertTrue(ex.getMessage().contains("contenido"));
    }

    @Test
    @DisplayName("Solo se puede editar en Borrador")
    void onlyDraftIsEditable() {
        question.submitForReview();
        assertThrows(IllegalStateException.class, () -> question.addOption("C", "Facade"));
        assertThrows(IllegalStateException.class, () -> question.defineCorrectAnswers("B"));
    }

    @Test
    @DisplayName("Solo una pregunta publicada puede usarse en una prueba")
    void onlyPublishedIsUsableInTest() {
        assertFalse(question.isUsableInTest());
        assertThrows(IllegalStateException.class, question::ensureUsableInTest);
        question.setQuestionState(new ApprovedState());
        assertThrows(IllegalStateException.class, question::ensureUsableInTest);
        question.setQuestionState(new PublishedState());
        assertTrue(question.isUsableInTest());
        assertDoesNotThrow(question::ensureUsableInTest);
        question.setQuestionState(new ArchivedState());
        assertThrows(IllegalStateException.class, question::ensureUsableInTest);
    }

    @Test
    @DisplayName("Los nombres de estado corresponden a la Figura 1")
    void spanishNames() {
        assertEquals("Borrador", new DraftState().getName());
        assertEquals("Pendiente de revisión", new PendingReviewState().getName());
        assertEquals("En revisión", new ReviewState().getName());
        assertEquals("Aprobada", new ApprovedState().getName());
        assertEquals("Publicada", new PublishedState().getName());
        assertEquals("Archivada", new ArchivedState().getName());
        assertEquals("Rechazada", new RejectedState().getName());
    }

    @Test
    @DisplayName("Rechazar o asignar revisor exige un texto válido")
    void reasonAndReviewerAreRequired() {
        question.submitForReview();
        assertThrows(IllegalArgumentException.class, () -> question.assignReviewer(" "));
        question.assignReviewer("Revisor");
        assertThrows(IllegalArgumentException.class, () -> question.reject(""));
        assertInstanceOf(ReviewState.class, question.getQuestionState());
    }
}
