package co.edu.unicauca.bancopreguntas.domain.services;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.AsignacionRevisorRepository;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;
import com.unicauca.taller2.usuarios.access.UsuarioRepository;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de dominio para la asignación de revisores a preguntas.
 * Implementa el patrón <strong>Observer</strong> (Sujeto/Publisher):
 * mantiene una lista de {@link NotificadorAsignacion} observadores que se
 * notifican tras cada asignación exitosa, sin ningún acoplamiento con la UI
 * ni con la infraestructura de correo.
 */
public class AsignacionRevisorService {

    private final AsignacionRevisorRepository asignacionRepository;
    private final PreguntaRepository preguntaRepository;
    private final UsuarioRepository usuarioRepository;
    private final List<NotificadorAsignacion> observadores;

    /**
     * Constructor original — compatible con los tests existentes.
     * Inicia con lista vacía de observadores.
     */
    public AsignacionRevisorService(AsignacionRevisorRepository asignacionRepository,
                                     PreguntaRepository preguntaRepository,
                                     UsuarioRepository usuarioRepository) {
        this(asignacionRepository, preguntaRepository, usuarioRepository, new ArrayList<>());
    }

    /**
     * Constructor con lista inicial de observadores (para inyección por constructor).
     */
    public AsignacionRevisorService(AsignacionRevisorRepository asignacionRepository,
                                     PreguntaRepository preguntaRepository,
                                     UsuarioRepository usuarioRepository,
                                     List<NotificadorAsignacion> observadores) {
        this.asignacionRepository = asignacionRepository;
        this.preguntaRepository = preguntaRepository;
        this.usuarioRepository = usuarioRepository;
        this.observadores = new ArrayList<>(observadores);
    }

    // -------------------------------------------------------------------------
    // API de observadores (patrón Observer)
    // -------------------------------------------------------------------------

    public void agregarObservador(NotificadorAsignacion observador) {
        observadores.add(observador);
    }

    public void removerObservador(NotificadorAsignacion observador) {
        observadores.remove(observador);
    }

    // -------------------------------------------------------------------------
    // Lógica de negocio
    // -------------------------------------------------------------------------

    public void asignarRevisores(int preguntaId, List<Integer> revisorIds) {
        Optional<Pregunta> opt = preguntaRepository.buscarPorId(preguntaId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("La pregunta no existe");
        }

        Pregunta pregunta = opt.get();
        if (pregunta.getEstado() != EstadoPregunta.PENDIENTE_REVISION) {
            throw new IllegalArgumentException(
                "Solo las preguntas en estado Pendiente de revisión pueden tener revisores asignados");
        }

        if (revisorIds == null || revisorIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un revisor");
        }

        // Validar que todos los IDs corresponden a usuarios con rol REVISOR antes de asignar
        List<Usuario> revisoresValidados = new ArrayList<>();
        for (Integer revisorId : revisorIds) {
            Usuario revisor = resolverRevisor(revisorId);
            if (revisor.getRol() != Rol.REVISOR) {
                throw new IllegalArgumentException(
                    "El usuario " + revisor.getNombreUsuario() + " no puede ser asignado porque no tiene rol de Revisor");
            }
            revisoresValidados.add(revisor);
        }

        // Asignar y notificar
        boolean primerAsignado = false;
        for (Usuario revisor : revisoresValidados) {
            if (!asignacionRepository.existeAsignacion(preguntaId, revisor.getId())) {
                asignacionRepository.asignar(preguntaId, revisor.getId());
                primerAsignado = true;
                // Notificar a todos los observadores registrados
                for (NotificadorAsignacion obs : observadores) {
                    obs.notificarAsignacion(pregunta, revisor);
                }
            }
        }

        // HU04: transicionar a EN_REVISION tras la primera asignación exitosa
        if (primerAsignado && pregunta.getEstado() == EstadoPregunta.PENDIENTE_REVISION) {
            preguntaRepository.actualizarEstado(preguntaId, EstadoPregunta.EN_REVISION);
        }
    }

    // -------------------------------------------------------------------------
    // Métodos auxiliares privados
    // -------------------------------------------------------------------------

    private Usuario resolverRevisor(int revisorId) {
        return usuarioRepository.listarTodos().stream()
            .filter(u -> u.getId() == revisorId)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "El usuario con ID " + revisorId + " no existe"));
    }
}
