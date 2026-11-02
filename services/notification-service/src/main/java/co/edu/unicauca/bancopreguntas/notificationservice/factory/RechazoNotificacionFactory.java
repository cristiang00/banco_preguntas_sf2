package co.edu.unicauca.bancopreguntas.notificationservice.factory;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class RechazoNotificacionFactory implements NotificacionFactory {

    @Override
    public Notificacion crearNotificacion(String destinatario, Map<String, Object> data) {
        Long preguntaId = ((Number) data.getOrDefault("preguntaId", 0L)).longValue();
        String motivo = (String) data.getOrDefault("motivoRechazo", "Observaciones en revisión");
        return new NotificacionRechazo(destinatario, preguntaId, motivo);
    }

    @Override
    public String getTipoSoportado() {
        return "PREGUNTA_RECHAZADA";
    }
}
