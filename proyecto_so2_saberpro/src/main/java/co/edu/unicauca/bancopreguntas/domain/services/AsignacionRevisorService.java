package co.edu.unicauca.bancopreguntas.domain.services;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.AsignacionRevisorRepository;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;
import com.unicauca.taller2.usuarios.access.UsuarioRepository;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;
import java.util.List;
import java.util.Optional;

public class AsignacionRevisorService {
    private final AsignacionRevisorRepository asignacionRepository;
    private final PreguntaRepository preguntaRepository;
    private final UsuarioRepository usuarioRepository;

    public AsignacionRevisorService(AsignacionRevisorRepository asignacionRepository, PreguntaRepository preguntaRepository, UsuarioRepository usuarioRepository) {
        this.asignacionRepository = asignacionRepository;
        this.preguntaRepository = preguntaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void asignarRevisores(int preguntaId, List<Integer> revisorIds) {
        Optional<Pregunta> opt = preguntaRepository.buscarPorId(preguntaId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("La pregunta no existe");
        }
        
        Pregunta pregunta = opt.get();
        if (pregunta.getEstado() != EstadoPregunta.PENDIENTE_REVISION) {
            throw new IllegalArgumentException("Solo las preguntas en estado Pendiente de revisión pueden tener revisores asignados");
        }

        if (revisorIds == null || revisorIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un revisor");
        }

        for (Integer revisorId : revisorIds) {
            // Verificar si el usuario existe (deberíamos tener un buscarPorId, pero el UsuarioRepository 
            // no lo tiene en su interfaz original por id, lo buscaremos usando la lista de todos por ahora
            // o idealmente el repo SQLite debería tener buscarPorId, 
            // por ahora validamos sobre la lista de revisores
            List<Usuario> revisores = usuarioRepository.listarTodos(); // o agregar buscarPorId
            Usuario revisor = revisores.stream()
                .filter(u -> u.getId() == revisorId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El usuario con ID " + revisorId + " no existe"));
                
            if (revisor.getRol() != Rol.REVISOR) {
                throw new IllegalArgumentException("El usuario " + revisor.getNombreUsuario() + " no puede ser asignado porque no tiene rol de Revisor");
            }
        }

        for (Integer revisorId : revisorIds) {
            if (!asignacionRepository.existeAsignacion(preguntaId, revisorId)) {
                asignacionRepository.asignar(preguntaId, revisorId);
                // Simular envío de correo
                System.out.println("[NOTIFICACIÓN EMAIL] Se ha asignado la pregunta " + preguntaId + " al revisor con ID: " + revisorId);
            }
        }
    }
}
