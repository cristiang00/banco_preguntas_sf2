package co.edu.unicauca.bancopreguntas.notificationservice.factory;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AprobacionNotificacionFactory implements NotificacionFactory {

    @Override
    public Notificacion crearNotificacion(String destinatario, Map<String, Object> data) {
        Long preguntaId = ((Number) data.getOrDefault("preguntaId", 0L)).longValue();
        String observaciones = (String) data.getOrDefault("observaciones", "");
        return new NotificacionAprobacion(destinatario, preguntaId, observaciones);
    }

    @Override
    public String getTipoSoportado() {
        return "PREGUNTA_APROBADA";
    }
}

