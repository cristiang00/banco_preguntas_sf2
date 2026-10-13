package co.edu.unicauca.bancopreguntas.domain.decorator;

/**
 * Vista con la descripción compuesta y el puntaje calculado tras aplicar decoradores.
 */
public record DecoratedQuestionView(String description, double score) {
}
