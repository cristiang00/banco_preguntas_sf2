package co.edu.unicauca.bancopreguntas.domain.repositories;

import co.edu.unicauca.bancopreguntas.domain.entities.Notificacion;
import java.util.List;

/**
 * Contrato de persistencia para el historial de notificaciones de asignación de revisores.
 */
public interface NotificacionRepository {
    /**
     * Persiste una nueva notificación.
     *
     * @param notificacion la notificación a guardar
     */
    void guardar(Notificacion notificacion);

    /**
     * Retorna todas las notificaciones ordenadas por fecha descendente.
     *
     * @return lista de notificaciones
     */
    List<Notificacion> listarTodas();

    /**
     * Retorna las notificaciones de un revisor específico, ordenadas por fecha descendente.
     *
     * @param revisorId identificador del revisor
     * @return lista de notificaciones del revisor
     */
    List<Notificacion> listarPorRevisor(int revisorId);
}
