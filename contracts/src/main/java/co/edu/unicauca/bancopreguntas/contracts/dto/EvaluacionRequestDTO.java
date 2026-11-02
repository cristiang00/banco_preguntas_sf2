package co.edu.unicauca.bancopreguntas.contracts.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EvaluacionRequestDTO implements Serializable {
    private String criterio;
    private Double calificacion;
    private String comentario;
    private List<String> observaciones = new ArrayList<>();

    public EvaluacionRequestDTO() {}

    public EvaluacionRequestDTO(String criterio, Double calificacion, String comentario, List<String> observaciones) {
        this.criterio = criterio;
        this.calificacion = calificacion;
        this.comentario = comentario;
        if (observaciones != null) {
            this.observaciones = observaciones;
        }
    }

    public String getCriterio() { return criterio; }
    public void setCriterio(String criterio) { this.criterio = criterio; }

    public Double getCalificacion() { return calificacion; }
    public void setCalificacion(Double calificacion) { this.calificacion = calificacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public List<String> getObservaciones() { return observaciones; }
    public void setObservaciones(List<String> observaciones) { this.observaciones = observaciones; }
}

