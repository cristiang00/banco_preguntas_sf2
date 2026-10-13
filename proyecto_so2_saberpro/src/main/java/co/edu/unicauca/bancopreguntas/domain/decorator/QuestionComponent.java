package co.edu.unicauca.bancopreguntas.domain.decorator;

/**
 * Interfaz base del patrón Decorator para preguntas.
 */
public interface QuestionComponent {

    String getDescription();

    double getScore();
}
