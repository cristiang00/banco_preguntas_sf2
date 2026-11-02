package co.edu.unicauca.bancopreguntas.questions.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "opciones_pregunta")
public class OpcionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clave", nullable = false, length = 10)
    private String clave;

    @Column(name = "texto", nullable = false, length = 1000)
    private String texto;

    @Column(name = "orden", nullable = false)
    private int orden;

    public OpcionEntity() {}

    public OpcionEntity(String clave, String texto, int orden) {
        this.clave = clave;
        this.texto = texto;
        this.orden = orden;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
}
