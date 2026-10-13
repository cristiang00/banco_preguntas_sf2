package co.edu.unicauca.bancopreguntas.domain.adapter;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalQuestionAdapterTest {

    @Test
    @DisplayName("Adapta correctamente el JSON estándar de la guía con distractores por defecto")
    void adaptStandardQuestion() {
        ExternalQuestionService service = new ExternalQuestionService();
        ExternalQuestionAdapter adapter = new ExternalQuestionAdapter(service);

        Pregunta question = adapter.getQuestion();

        assertNotNull(question);
        assertEquals("¿Cuál es la capital de Colombia?", question.getStatement());
        assertEquals(TipoPregunta.SELECCION_UNICA, question.getTipo());
        assertEquals("Borrador", question.getStateName());
        assertEquals(4, question.getOpciones().size());
        assertEquals("Bogotá", question.buscarOpcion("A").orElseThrow().texto());
        assertEquals(Set.of("A"), question.getRespuestasCorrectas());
        assertTrue(question.isContentComplete());
    }

    @Test
    @DisplayName("Soporta distractores configurables sin acoplarlos al parseo")
    void adaptWithCustomDistractors() {
        ExternalQuestionService service = new ExternalQuestionService();
        List<String> customDistractors = List.of("Cartagena", "Bucaramanga", "Santa Marta");
        ExternalQuestionAdapter adapter = new ExternalQuestionAdapter(service, customDistractors);

        Pregunta question = adapter.getQuestion();

        assertEquals(4, question.getOpciones().size());
        assertEquals("Cartagena", question.buscarOpcion("B").orElseThrow().texto());
        assertEquals("Bucaramanga", question.buscarOpcion("C").orElseThrow().texto());
        assertEquals("Santa Marta", question.buscarOpcion("D").orElseThrow().texto());
    }

    @Test
    @DisplayName("Procesa JSON con campos en orden invertido y espacios/saltos de línea variables")
    void adaptReorderedAndFormattedJson() {
        String json = """
        {
            
            "answer"   :    "París"   ,
            
            "statement" :
               "¿Capital de Francia?"
        }
        """;
        ExternalQuestionService service = new ExternalQuestionService(json);
        ExternalQuestionAdapter adapter = new ExternalQuestionAdapter(service, List.of("Lyon", "Marsella"));

        Pregunta question = adapter.getQuestion();

        assertEquals("¿Capital de Francia?", question.getStatement());
        assertEquals("París", question.buscarOpcion("A").orElseThrow().texto());
        assertEquals(Set.of("A"), question.getRespuestasCorrectas());
        assertEquals(3, question.getOpciones().size());
    }

    @Test
    @DisplayName("Lanza excepción si el JSON no contiene el campo 'answer'")
    void throwsWhenMissingAnswer() {
        String json = """
        {
            "statement": "¿Pregunta sin respuesta?"
        }
        """;
        ExternalQuestionService service = new ExternalQuestionService(json);
        ExternalQuestionAdapter adapter = new ExternalQuestionAdapter(service);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, adapter::getQuestion);
        assertTrue(ex.getMessage().contains("answer"));
    }

    @Test
    @DisplayName("Lanza excepción si el JSON no contiene el campo 'statement'")
    void throwsWhenMissingStatement() {
        String json = """
        {
            "answer": "Respuesta huérfana"
        }
        """;
        ExternalQuestionService service = new ExternalQuestionService(json);
        ExternalQuestionAdapter adapter = new ExternalQuestionAdapter(service);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, adapter::getQuestion);
        assertTrue(ex.getMessage().contains("statement"));
    }

    @Test
    @DisplayName("Valida argumentos nulos o vacíos en el constructor")
    void constructorValidations() {
        assertThrows(NullPointerException.class, () -> new ExternalQuestionAdapter(null));
        ExternalQuestionService service = new ExternalQuestionService();
        assertThrows(NullPointerException.class, () -> new ExternalQuestionAdapter(service, null));
        assertThrows(IllegalArgumentException.class, () -> new ExternalQuestionAdapter(service, List.of()));
    }
}
