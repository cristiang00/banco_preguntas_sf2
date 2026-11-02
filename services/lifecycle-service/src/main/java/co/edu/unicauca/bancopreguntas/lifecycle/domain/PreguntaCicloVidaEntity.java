package co.edu.unicauca.bancopreguntas.lifecycle.domain;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.state.PreguntaCicloVidaContext;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.state.QuestionState;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.state.QuestionStateFactory;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "preguntas_ciclo_vida")
public class PreguntaCicloVidaEntity implements PreguntaCicloVidaContext {

    @Id
    @Column(name = "pregunta_id")
    private Long preguntaId;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoPreguntaDTO estado;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public PreguntaCicloVidaEntity() {}

    public PreguntaCicloVidaEntity(Long preguntaId, Long autorId, EstadoPreguntaDTO estado) {
        this.preguntaId = preguntaId;
        this.autorId = autorId;
        this.estado = estado != null ? estado : EstadoPreguntaDTO.BORRADOR;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public QuestionState getEstadoActual() {
        return QuestionStateFactory.fromDTO(estado);
    }

    @Override
    public void cambiarEstado(QuestionState nuevoEstado) {
        this.estado = nuevoEstado.getEstadoDTO();
        this.fechaActualizacion = LocalDateTime.now();
    }

    @Override
    public Long getPreguntaId() {
        return preguntaId;
    }

    @Override
    public Long getAutorId() {
        return autorId;
    }

    public EstadoPreguntaDTO getEstado() {
        return estado;
    }

    public void setEstado(EstadoPreguntaDTO estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
