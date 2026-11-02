package co.edu.unicauca.bancopreguntas.notificationservice.factory;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AsignacionNotificacionFactory implements NotificacionFactory {

    @Override
    public Notificacion crearNotificacion(String destinatario, Map<String, Object> data) {
        Long preguntaId = ((Number) data.getOrDefault("preguntaId", 0L)).longValue();
        String titulo = (String) data.getOrDefault("tituloPregunta", "Pregunta " + preguntaId);
        return new NotificacionAsignacion(destinatario, preguntaId, titulo);
    }

    @Override
    public String getTipoSoportado() {
        return "ASIGNACION_REVISION";
    }
}

