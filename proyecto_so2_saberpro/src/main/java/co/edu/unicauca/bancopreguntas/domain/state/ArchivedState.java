package co.edu.unicauca.bancopreguntas.domain.state;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;

/**
 * Archivada: la pregunta ya no se utiliza activamente y no admite transiciones salientes.
 */
public class ArchivedState extends AbstractQuestionState {

    public static final String NAME = "Archivada";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EstadoPregunta getEstado() {
        return EstadoPregunta.ARCHIVADA;
    }
}
