package co.edu.unicauca.bancopreguntas.domain.services;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import com.unicauca.taller2.usuarios.model.Usuario;

/**
 * Interfaz Observer para notificaciones de asignación de revisores.
 * El dominio define el contrato; las implementaciones concretas pueden vivir
 * en dataaccess (persistencia) o en presentation (UI) según el principio DIP.
 */
public interface NotificadorAsignacion {
    /**
     * Se invoca cuando un revisor es asignado exitosamente a una pregunta.
     *
     * @param pregunta la pregunta a revisar
     * @param revisor  el usuario asignado como revisor
     */
    void notificarAsignacion(Pregunta pregunta, Usuario revisor);
}
