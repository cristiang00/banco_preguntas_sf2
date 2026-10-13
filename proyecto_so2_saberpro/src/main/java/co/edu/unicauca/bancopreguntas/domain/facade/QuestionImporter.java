package co.edu.unicauca.bancopreguntas.domain.facade;

import co.edu.unicauca.bancopreguntas.domain.adapter.QuestionProvider;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

import java.util.Objects;

/**
 * Subsistema de importación que utiliza un {@link QuestionProvider} para obtener
 * una pregunta externa adaptada.
 */
public class QuestionImporter {

    public Pregunta importQuestion(QuestionProvider provider) {
        Objects.requireNonNull(provider, "El proveedor de preguntas es obligatorio");
        return provider.getQuestion();
    }
}
