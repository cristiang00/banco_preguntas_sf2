package co.edu.unicauca.bancopreguntas.contracts.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AsignarRevisoresRequestDTO implements Serializable {
    private Long preguntaId;
    private List<Long> revisorIds = new ArrayList<>();

    public AsignarRevisoresRequestDTO() {}

    public AsignarRevisoresRequestDTO(Long preguntaId, List<Long> revisorIds) {
        this.preguntaId = preguntaId;
        if (revisorIds != null) {
            this.revisorIds = revisorIds;
        }
    }

    public Long getPreguntaId() { return preguntaId; }
    public void setPreguntaId(Long preguntaId) { this.preguntaId = preguntaId; }

    public List<Long> getRevisorIds() { return revisorIds; }
    public void setRevisorIds(List<Long> revisorIds) { this.revisorIds = revisorIds; }
}

