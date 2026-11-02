package co.edu.unicauca.bancopreguntas.contracts.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DecisionRequestDTO implements Serializable {
    private String decision; // "APROBAR" o "RECHAZAR"
    private List<String> observaciones = new ArrayList<>();
    private String comentario;

    public DecisionRequestDTO() {}

    public DecisionRequestDTO(String decision, List<String> observaciones, String comentario) {
        this.decision = decision;
        if (observaciones != null) {
            this.observaciones = observaciones;
        }
        this.comentario = comentario;
    }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public List<String> getObservaciones() { return observaciones; }
    public void setObservaciones(List<String> observaciones) { this.observaciones = observaciones; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}

