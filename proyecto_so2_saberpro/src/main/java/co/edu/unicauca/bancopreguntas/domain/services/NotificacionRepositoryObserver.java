package co.edu.unicauca.bancopreguntas.domain.services;

import co.edu.unicauca.bancopreguntas.domain.entities.Notificacion;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.NotificacionRepository;
import com.unicauca.taller2.usuarios.model.Usuario;

import java.time.LocalDateTime;

/**
 * Observer 1 — persiste cada notificación en la base de datos SQLite.
 * Vive en el paquete de servicios de dominio (o puede moverse a dataaccess).
 */
public class NotificacionRepositoryObserver implements NotificadorAsignacion {

    private final NotificacionRepository notificacionRepository;

    public NotificacionRepositoryObserver(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    public void notificarAsignacion(Pregunta pregunta, Usuario revisor) {
        Notificacion n = new Notificacion();
        n.setPreguntaId(pregunta.getId());
        n.setRevisorId(revisor.getId());
        n.setAsunto("Revisión asignada: Pregunta #" + pregunta.getId());
        n.setCuerpo("Estimado " + revisor.getNombreCompleto()
                + ", se le ha asignado la revisión de la siguiente pregunta: "
                + pregunta.getPreguntaDirecta());
        n.setFecha(LocalDateTime.now());
        notificacionRepository.guardar(n);
    }
}
