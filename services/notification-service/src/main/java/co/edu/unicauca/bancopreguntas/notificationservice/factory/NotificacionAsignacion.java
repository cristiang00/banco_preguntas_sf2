package co.edu.unicauca.bancopreguntas.notificationservice.factory;

public class NotificacionAsignacion implements Notificacion {

    private final String destinatario;
    private final Long preguntaId;
    private final String tituloPregunta;

    public NotificacionAsignacion(String destinatario, Long preguntaId, String tituloPregunta) {
        this.destinatario = destinatario;
        this.preguntaId = preguntaId;
        this.tituloPregunta = tituloPregunta != null ? tituloPregunta : ("ID: " + preguntaId);
    }

    @Override
    public String getDestinatario() {
        return destinatario;
    }

    @Override
    public String getAsunto() {
        return "Nueva pregunta asignada para revisión (Pregunta #" + preguntaId + ")";
    }

    @Override
    public String getCuerpo() {
        return "Estimado(a) revisor(a),\n\n"
                + "Se le ha asignado la pregunta #" + preguntaId + " (" + tituloPregunta + ") para evaluación técnica y pedagógica.\n"
                + "Por favor ingrese al sistema Banco de Preguntas Saber Pro para registrar sus observaciones y emitir su veredicto.\n\n"
                + "Cordialmente,\nSistema de Evaluación Saber Pro";
    }

    @Override
    public String getTipo() {
        return "ASIGNACION_REVISION";
    }
}

