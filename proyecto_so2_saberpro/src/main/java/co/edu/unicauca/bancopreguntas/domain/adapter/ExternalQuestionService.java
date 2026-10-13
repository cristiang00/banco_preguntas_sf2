package co.edu.unicauca.bancopreguntas.domain.adapter;

/**
 * Servicio externo (Adaptee) que entrega preguntas en un formato JSON plano,
 * incompatible directamente con el modelo {@code Pregunta}.
 */
public class ExternalQuestionService {

    private final String payload;

    public ExternalQuestionService() {
        this("""
        {
            "statement": "¿Cuál es la capital de Colombia?",
            "answer": "Bogotá"
        }
        """);
    }

    public ExternalQuestionService(String payload) {
        this.payload = payload;
    }

    public String getQuestion() {
        return payload;
    }
}
