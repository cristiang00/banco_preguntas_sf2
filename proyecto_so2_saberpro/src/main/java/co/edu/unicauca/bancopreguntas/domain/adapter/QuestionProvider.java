package co.edu.unicauca.bancopreguntas.domain.adapter;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

/**
 * Interfaz objetivo (Target) del patrón Adapter para la importación de preguntas.
 */
public interface QuestionProvider {

    /**
     * Obtiene una pregunta adaptada al modelo interno {@link Pregunta}.
     *
     * @return pregunta lista para ser persistida en estado Borrador
     */
    Pregunta getQuestion();
}
