package co.edu.unicauca.bancopreguntas.validation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultados_validacion")
public class ResultadoValidacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pregunta_id")
    private Long preguntaId;

    @Column(name = "es_valido", nullable = false)
    private boolean esValido;

    @Column(name = "total_errores", nullable = false)
    private int totalErrores;

    @Column(name = "detalle_errores", length = 2000)
    private String detalleErrores;

    @Column(name = "fecha_validacion", nullable = false)
    private LocalDateTime fechaValidacion;

    public ResultadoValidacionEntity() {}

    public ResultadoValidacionEntity(Long preguntaId, boolean esValido, int totalErrores, String detalleErrores) {
        this.preguntaId = preguntaId;
        this.esValido = esValido;
        this.totalErrores = totalErrores;
        this.detalleErrores = detalleErrores;
        this.fechaValidacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getPreguntaId() { return preguntaId; }
    public boolean isEsValido() { return esValido; }
    public int getTotalErrores() { return totalErrores; }
    public String getDetalleErrores() { return detalleErrores; }
    public LocalDateTime getFechaValidacion() { return fechaValidacion; }
}

