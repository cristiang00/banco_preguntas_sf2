package co.edu.unicauca.bancopreguntas.notificationservice.factory;

import java.util.Map;

/**
 * Creador abstracto del patrón Factory Method (GoF).
 */
public interface NotificacionFactory {

    /**
     * Factory Method (GoF) para instanciar la notificación concreta.
     *
     * @param destinatario correo o identificador del destinatario
     * @param data parámetros contextuales del evento
     * @return instancia de Notificacion
     */
    Notificacion crearNotificacion(String destinatario, Map<String, Object> data);

    String getTipoSoportado();
}
