package co.edu.unicauca.bancopreguntas.notificationservice.factory;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RevisionNotificacionFactory implements NotificacionFactory {

    @Override
    public Notificacion crearNotificacion(String destinatario, Map<String, Object> data) {
        Long preguntaId = ((Number) data.getOrDefault("preguntaId", 0L)).longValue();
        Integer version = ((Number) data.getOrDefault("version", 1)).intValue();
        String autor = (String) data.getOrDefault("autor", "Autor");
        return new NotificacionRevision(destinatario, preguntaId, version, autor);
    }

    @Override
    public String getTipoSoportado() {
        return "PREGUNTA_ENVIADA_REVISION";
    }
}

