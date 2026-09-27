package co.edu.unicauca.bancopreguntas.domain.services;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;

import java.util.Optional;

/**
 * Template Method (GoF) para el flujo de transición de estado de una pregunta.
 * <p>
 * Define el <em>esqueleto del algoritmo</em> con un método final {@link #ejecutar},
 * dejando que las subclases concretas implementen los pasos variables
 * ({@link #validarPrecondiciones} y {@link #ejecutarTransicion}).
 * </p>
 * <p>
 * El paso invariante {@link #persistir} puede sobreescribirse si una transición
 * concreta requiere lógica adicional de persistencia.
 * </p>
 *
 * <pre>
 * // Ejemplo de uso:
 * TransicionEstadoPreguntaTemplate t = new EnviarARevisionTransicion(preguntaRepository);
 * t.ejecutar(preguntaId, autorId);
 * </pre>
 */
public abstract class TransicionEstadoPreguntaTemplate {

    protected final PreguntaRepository preguntaRepository;

    protected TransicionEstadoPreguntaTemplate(PreguntaRepository preguntaRepository) {
        this.preguntaRepository = preguntaRepository;
    }

    /**
     * Ejecuta la transición de estado. Método <em>final</em> — no puede sobreescribirse.
     * Sigue el orden: buscar → validar → ejecutar → persistir.
     *
     * @param preguntaId el ID de la pregunta a transicionar
     * @param usuarioId  el ID del usuario que ejecuta la acción
     */
    public final void ejecutar(int preguntaId, int usuarioId) {
        Pregunta pregunta = buscarPregunta(preguntaId);
        validarPrecondiciones(pregunta, usuarioId);
        ejecutarTransicion(pregunta, usuarioId);
        persistir(pregunta);
    }

    /**
     * Paso invariante: busca la pregunta o lanza excepción si no existe.
     */
    private Pregunta buscarPregunta(int id) {
        return preguntaRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("La pregunta no existe"));
    }

    /**
     * Paso variable — valida que la transición es permitida para este usuario y estado actual.
     *
     * @param pregunta   la pregunta a transicionar
     * @param usuarioId  el usuario que ejecuta la acción
     * @throws IllegalArgumentException si las precondiciones no se cumplen
     */
    protected abstract void validarPrecondiciones(Pregunta pregunta, int usuarioId);

    /**
     * Paso variable — aplica el cambio de estado a la entidad en memoria.
     *
     * @param pregunta   la pregunta a transicionar
     * @param usuarioId  el usuario que ejecuta la acción
     */
    protected abstract void ejecutarTransicion(Pregunta pregunta, int usuarioId);

    /**
     * Paso invariante por defecto — persiste el nuevo estado de la pregunta.
     * Puede sobreescribirse si la transición requiere lógica adicional.
     *
     * @param pregunta la pregunta con el estado ya actualizado en memoria
     */
    protected void persistir(Pregunta pregunta) {
        preguntaRepository.actualizarEstado(pregunta.getId(), pregunta.getEstado());
    }
}
