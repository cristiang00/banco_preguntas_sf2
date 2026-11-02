package co.edu.unicauca.bancopreguntas.lifecycle.domain;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_transiciones")
public class HistorialTransicionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pregunta_id", nullable = false)
    private Long preguntaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_origen", nullable = false, length = 50)
    private EstadoPreguntaDTO estadoOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_destino", nullable = false, length = 50)
    private EstadoPreguntaDTO estadoDestino;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "motivo", length = 1000)
    private String motivo;

    @Column(name = "fecha_transicion", nullable = false)
    private LocalDateTime fechaTransicion;

    public HistorialTransicionEntity() {}

    public HistorialTransicionEntity(Long preguntaId, EstadoPreguntaDTO estadoOrigen, 
                                     EstadoPreguntaDTO estadoDestino, Long usuarioId, String motivo) {
        this.preguntaId = preguntaId;
        this.estadoOrigen = estadoOrigen;
        this.estadoDestino = estadoDestino;
        this.usuarioId = usuarioId;
        this.motivo = motivo;
        this.fechaTransicion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getPreguntaId() { return preguntaId; }
    public EstadoPreguntaDTO getEstadoOrigen() { return estadoOrigen; }
    public EstadoPreguntaDTO getEstadoDestino() { return estadoDestino; }
    public Long getUsuarioId() { return usuarioId; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFechaTransicion() { return fechaTransicion; }
}
