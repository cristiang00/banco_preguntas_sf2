package co.edu.unicauca.bancopreguntas.notificationservice.factory;

public class NotificacionAprobacion implements Notificacion {

    private final String destinatario;
    private final Long preguntaId;
    private final String observaciones;

    public NotificacionAprobacion(String destinatario, Long preguntaId, String observaciones) {
        this.destinatario = destinatario;
        this.preguntaId = preguntaId;
        this.observaciones = observaciones;
    }

    @Override
    public String getDestinatario() {
        return destinatario;
    }

    @Override
    public String getAsunto() {
        return "Pregunta #" + preguntaId + " ha sido APROBADA";
    }

    @Override
    public String getCuerpo() {
        return "Estimado(a) autor(a),\n\n"
                + "Nos complace informarle que su pregunta #" + preguntaId + " ha completado satisfactoriamente el proceso de revisión y ha sido APROBADA.\n"
                + (observaciones != null && !observaciones.isBlank() ? "Observaciones finales de los revisores:\n" + observaciones + "\n\n" : "\n")
                + "La pregunta ahora se encuentra disponible en el banco oficial de preguntas.\n\n"
                + "Cordialmente,\nComité Editorial Saber Pro";
    }

    @Override
    public String getTipo() {
        return "PREGUNTA_APROBADA";
    }
}

