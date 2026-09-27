package co.edu.unicauca.bancopreguntas.presentation.controllers;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.services.PreguntaService;
import java.util.List;

public class PreguntaController {
    private final PreguntaService preguntaService;
    private final int autorId;

    public PreguntaController(PreguntaService preguntaService, int autorId) {
        this.preguntaService = preguntaService;
        this.autorId = autorId;
    }

    public void crearPregunta(Pregunta pregunta) {
        preguntaService.crearPregunta(pregunta, autorId);
    }

    public void enviarARevision(int preguntaId) {
        preguntaService.enviarARevision(preguntaId, autorId);
    }

    public List<Pregunta> listarMisPreguntas(List<EstadoPregunta> estadosFiltro, String nivelDificultad, int offset, int limit) {
        return preguntaService.listarMisPreguntas(autorId, estadosFiltro, nivelDificultad, offset, limit);
    }
    
    public int contarMisPreguntas(List<EstadoPregunta> estadosFiltro, String nivelDificultad) {
        return preguntaService.contarMisPreguntas(autorId, estadosFiltro, nivelDificultad);
    }

    public Pregunta obtenerDetalle(int preguntaId) {
        return preguntaService.obtenerPregunta(preguntaId);
    }
}
