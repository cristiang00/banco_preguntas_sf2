package co.edu.unicauca.bancopreguntas.domain.services;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;

/**
 * Implementación concreta de {@link TransicionEstadoPreguntaTemplate} para el flujo
 * "Enviar a Revisión": transiciona una pregunta de {@code BORRADOR} a {@code PENDIENTE_REVISION}.
 *
 * <p>Precondiciones:</p>
 * <ul>
 *   <li>El {@code usuarioId} debe ser el autor de la pregunta.</li>
 *   <li>La pregunta debe estar en estado {@code BORRADOR}.</li>
 * </ul>
 */
public class EnviarARevisionTransicion extends TransicionEstadoPreguntaTemplate {

    public EnviarARevisionTransicion(PreguntaRepository preguntaRepository) {
        super(preguntaRepository);
    }

    @Override
    protected void validarPrecondiciones(Pregunta pregunta, int usuarioId) {
        if (pregunta.getAutorId() != usuarioId) {
            throw new IllegalArgumentException(
                "No tiene permisos para modificar el estado de esta pregunta");
        }
        if (pregunta.getEstado() != EstadoPregunta.BORRADOR) {
            throw new IllegalArgumentException(
                "Solo las preguntas en estado Borrador pueden enviarse a revisión");
        }
    }

    @Override
    protected void ejecutarTransicion(Pregunta pregunta, int usuarioId) {
        pregunta.setEstado(EstadoPregunta.PENDIENTE_REVISION);
    }
}
