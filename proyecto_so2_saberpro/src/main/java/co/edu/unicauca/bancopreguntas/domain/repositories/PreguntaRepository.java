package co.edu.unicauca.bancopreguntas.domain.repositories;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import java.util.List;
import java.util.Optional;

public interface PreguntaRepository {
    void guardar(Pregunta pregunta);
    void actualizar(Pregunta pregunta);
    void actualizarEstado(int preguntaId, EstadoPregunta nuevoEstado);
    Optional<Pregunta> buscarPorId(int id);
    List<Pregunta> listarPorAutor(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad, int offset, int limit);
    int contarPorAutor(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad);
}
