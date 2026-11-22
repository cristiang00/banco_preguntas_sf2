package co.edu.unicauca.bancopreguntas.notificationservice.factory;

public class NotificacionRevision implements Notificacion {

    private final String destinatario;
    private final Long preguntaId;
    private final Integer version;
    private final String autor;

    public NotificacionRevision(String destinatario, Long preguntaId, Integer version, String autor) {
        this.destinatario = destinatario;
        this.preguntaId = preguntaId;
        this.version = version != null ? version : 1;
        this.autor = autor != null ? autor : "Autor";
    }

    @Override
    public String getDestinatario() {
        return destinatario;
    }

    @Override
    public String getAsunto() {
        return "Nueva ronda de revisión: Pregunta #" + preguntaId + " (v" + version + ")";
    }

    @Override
    public String getCuerpo() {
        return "Estimado(a),\n\n"
                + "Se ha enviado la pregunta #" + preguntaId + " a revisión para una nueva ronda (Versión " + version + ") por el autor " + autor + ".\n"
                + "Por favor ingrese al sistema Saber Pro para proceder con la revisión o asignación correspondiente.\n\n"
                + "Cordialmente,\nSistema Banco de Preguntas Saber Pro";
    }

    @Override
    public String getTipo() {
        return "PREGUNTA_ENVIADA_REVISION";
    }
}

