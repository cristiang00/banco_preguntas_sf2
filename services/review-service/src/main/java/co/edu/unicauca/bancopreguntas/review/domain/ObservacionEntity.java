package co.edu.unicauca.bancopreguntas.review.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "observaciones_revision")
public class ObservacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "revision_id", nullable = false)
    private Long revisionId;

    @Column(name = "pregunta_id", nullable = false)
    private Long preguntaId;

    @Column(name = "texto", nullable = false, length = 2000)
    private String texto;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "visible_autor", nullable = false)
    private boolean visibleAutor = true;

    public ObservacionEntity() {}

    public ObservacionEntity(Long revisionId, Long preguntaId, String texto) {
        this.revisionId = revisionId;
        this.preguntaId = preguntaId;
        this.texto = texto;
        this.fecha = LocalDateTime.now();
        this.visibleAutor = true;
    }

    public Long getId() { return id; }
    public Long getRevisionId() { return revisionId; }
    public Long getPreguntaId() { return preguntaId; }
    public String getTexto() { return texto; }
    public LocalDateTime getFecha() { return fecha; }
    public boolean isVisibleAutor() { return visibleAutor; }
}
