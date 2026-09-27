package co.edu.unicauca.bancopreguntas.presentation.controllers;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.services.AsignacionRevisorService;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;
import com.unicauca.taller2.usuarios.access.UsuarioRepository;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;
import java.util.List;


public class AsignacionRevisorController {
    private final AsignacionRevisorService asignacionService;
    private final PreguntaRepository preguntaRepository;
    private final UsuarioRepository usuarioRepository;

    public AsignacionRevisorController(AsignacionRevisorService asignacionService, PreguntaRepository preguntaRepository, UsuarioRepository usuarioRepository) {
        this.asignacionService = asignacionService;
        this.preguntaRepository = preguntaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void asignarRevisores(int preguntaId, List<Integer> revisorIds) {
        asignacionService.asignarRevisores(preguntaId, revisorIds);
    }

    public List<Pregunta> listarPreguntasPendientes() {
        return preguntaRepository.listarPorEstado(EstadoPregunta.PENDIENTE_REVISION, 0, 1000);
    }


    public List<Usuario> listarRevisores() {
        return usuarioRepository.listarPorRol(Rol.REVISOR);
    }
}
