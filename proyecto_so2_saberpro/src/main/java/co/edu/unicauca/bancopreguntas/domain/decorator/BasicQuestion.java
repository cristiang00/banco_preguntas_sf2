package co.edu.unicauca.bancopreguntas.domain.decorator;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

import java.util.Objects;

/**
 * Componente concreto básico que expone el enunciado y puntaje base de una pregunta.
 */
public class BasicQuestion implements QuestionComponent {

    public static final double BASE_SCORE = 1.0;

    private final String description;

    public BasicQuestion(String description) {
        this.description = Objects.requireNonNull(description, "La descripción es obligatoria");
    }

    public BasicQuestion(Pregunta pregunta) {
        Objects.requireNonNull(pregunta, "La pregunta es obligatoria");
        String tipoEtiqueta = pregunta.getTipo() != null ? pregunta.getTipo().getEtiqueta() : "Pregunta";
        this.description = "[" + tipoEtiqueta + "] " + pregunta.getPreguntaDirecta();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public double getScore() {
        return BASE_SCORE;
    }
}
