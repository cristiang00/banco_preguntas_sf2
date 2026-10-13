package co.edu.unicauca.bancopreguntas.domain.services;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;



public abstract class TransicionEstadoPreguntaTemplate {

    protected final PreguntaRepository preguntaRepository;

    protected TransicionEstadoPreguntaTemplate(PreguntaRepository preguntaRepository) {
        this.preguntaRepository = preguntaRepository;
    }

    public final void ejecutar(int preguntaId, int usuarioId) {
        Pregunta pregunta = buscarPregunta(preguntaId);
        validarPrecondiciones(pregunta, usuarioId);
        ejecutarTransicion(pregunta, usuarioId);
        persistir(pregunta);
    }

  
    private Pregunta buscarPregunta(int id) {
        return preguntaRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("La pregunta no existe"));
    }

  
    protected abstract void validarPrecondiciones(Pregunta pregunta, int usuarioId);
  
    protected abstract void ejecutarTransicion(Pregunta pregunta, int usuarioId);

    
    protected void persistir(Pregunta pregunta) {
        preguntaRepository.actualizarEstado(pregunta.getId(), pregunta.getEstado());
    }
}
