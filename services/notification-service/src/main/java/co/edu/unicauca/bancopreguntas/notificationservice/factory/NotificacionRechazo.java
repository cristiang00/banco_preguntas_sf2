package co.edu.unicauca.bancopreguntas.notificationservice.factory;

public class NotificacionRechazo implements Notificacion {

    private final String destinatario;
    private final Long preguntaId;
    private final String motivoRechazo;

    public NotificacionRechazo(String destinatario, Long preguntaId, String motivoRechazo) {
        this.destinatario = destinatario;
        this.preguntaId = preguntaId;
        this.motivoRechazo = motivoRechazo != null ? motivoRechazo : "No se especificó motivo detallado.";
    }

    @Override
    public String getDestinatario() {
        return destinatario;
    }

    @Override
    public String getAsunto() {
        return "Pregunta #" + preguntaId + " requiere ajustes / RECHAZADA";
    }

    @Override
    public String getCuerpo() {
        return "Estimado(a) autor(a),\n\n"
                + "Le informamos que la pregunta #" + preguntaId + " ha sido evaluada y marcada como RECHAZADA durante la revisión técnica.\n\n"
                + "Motivo / Observaciones del revisor:\n"
                + motivoRechazo + "\n\n"
                + "Puede consultar los detalles y observaciones en el sistema para realizar los ajustes necesarios.\n\n"
                + "Cordialmente,\nComité de Revisión Saber Pro";
    }

    @Override
    public String getTipo() {
        return "PREGUNTA_RECHAZADA";
    }
}
