package co.edu.unicauca.bancopreguntas.presentation.controllers;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.services.AsignacionRevisorService;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;
import com.unicauca.taller2.usuarios.access.UsuarioRepository;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;
import java.util.List;
import java.util.Collections;

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
        // En una app real, esto podría ir en PreguntaService,
        // pero aquí podemos consultar el repo filtrando por el autor y estado.
        // Dado que un administrador ve TODAS las preguntas pendientes (no sólo las suyas),
        // pasamos un query especial o usamos una solución general.
        // Como PreguntaRepository.listarPorAutor necesita un autorId, tendríamos que agregar
        // un listarPorEstado a PreguntaRepository. Para este proyecto:
        // Como el repo solo tiene listarPorAutor, lo vamos a modificar rápido o buscar de todas?
        // En el plan, listarPorAutor es específico. Haremos que un autorId = -1 signifique "todos"
        // o llamaremos un método especial. Modificaremos el Repo si es necesario.
        return preguntaRepository.listarPorAutor(-1, Collections.singletonList(EstadoPregunta.PENDIENTE_REVISION), null, 0, 1000);
    }

    public List<Usuario> listarRevisores() {
        return usuarioRepository.listarPorRol(Rol.REVISOR);
    }
}
