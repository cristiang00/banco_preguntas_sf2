package co.edu.unicauca.bancopreguntas.dataaccess;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Doble de prueba en memoria de {@link PreguntaRepository} para tests rápidos e independientes.
 */
public class InMemoryQuestionRepository implements PreguntaRepository {

    private final Map<Integer, Pregunta> storage = new LinkedHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1);

    @Override
    public void guardar(Pregunta pregunta) {
        if (pregunta.getId() <= 0) {
            pregunta.setId(sequence.getAndIncrement());
        }
        storage.put(pregunta.getId(), pregunta);
    }

    @Override
    public void actualizar(Pregunta pregunta) {
        storage.put(pregunta.getId(), pregunta);
    }

    @Override
    public void actualizarEstado(int preguntaId, EstadoPregunta nuevoEstado) {
        Pregunta p = storage.get(preguntaId);
        if (p != null) {
            p.setEstado(nuevoEstado);
        }
    }

    @Override
    public Optional<Pregunta> buscarPorId(int id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Pregunta> listarPorAutor(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad,
                                         int offset, int limit) {
        return storage.values().stream()
                .filter(p -> autorId == -1 || p.getAutorId() == autorId)
                .filter(p -> estadosFiltro == null || estadosFiltro.isEmpty() || estadosFiltro.contains(p.getEstado()))
                .filter(p -> nivelDificultad == null || nivelDificultad.equals("Todos") || nivelDificultad.equalsIgnoreCase(p.getNivelDificultad()))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public int contarPorAutor(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad) {
        return (int) storage.values().stream()
                .filter(p -> autorId == -1 || p.getAutorId() == autorId)
                .filter(p -> estadosFiltro == null || estadosFiltro.isEmpty() || estadosFiltro.contains(p.getEstado()))
                .filter(p -> nivelDificultad == null || nivelDificultad.equals("Todos") || nivelDificultad.equalsIgnoreCase(p.getNivelDificultad()))
                .count();
    }

    @Override
    public List<Pregunta> listarPorRevisor(int revisorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad, int offset, int limit) {
        return storage.values().stream()
                .filter(p -> estadosFiltro == null || estadosFiltro.isEmpty() || estadosFiltro.contains(p.getEstado()))
                .filter(p -> nivelDificultad == null || nivelDificultad.equals("Todos") || nivelDificultad.equalsIgnoreCase(p.getNivelDificultad()))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public int contarPorRevisor(int revisorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad) {
        return (int) storage.values().stream()
                .filter(p -> estadosFiltro == null || estadosFiltro.isEmpty() || estadosFiltro.contains(p.getEstado()))
                .filter(p -> nivelDificultad == null || nivelDificultad.equals("Todos") || nivelDificultad.equalsIgnoreCase(p.getNivelDificultad()))
                .count();
    }

    @Override
    public List<Pregunta> listarPorEstado(EstadoPregunta estado, int offset, int limit) {
        return storage.values().stream()
                .filter(p -> p.getEstado() == estado)
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public int contarPorEstado(EstadoPregunta estado) {
        return (int) storage.values().stream()
                .filter(p -> p.getEstado() == estado)
                .count();
    }

    @Override
    public List<Pregunta> listarTodas() {
        return new ArrayList<>(storage.values());
    }
}
