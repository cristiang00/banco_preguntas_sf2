package co.edu.unicauca.bancopreguntas.domain.entities;

import java.time.LocalDateTime;

/**
 * Builder.
 *  construir objetos {@code Pregunta} con una API fluida.
 * Las <strong>validaciones de negocio</strong> siguen siendo responsabilidad
 * exclusiva de
 * {@code PreguntaService.validarCampos()} (SRP); el Builder solo crea el
 * objeto.
 */
public class PreguntaBuilder {

    private String contexto;
    private String preguntaDirecta;
    private String distractor1;
    private String distractor2;
    private String distractor3;
    private String distractor4;
    private String respuestaCorrecta;
    private String justificacion;
    private String bibliografia;
    private String competencia;
    private String tema;
    private String subtema;
    private String nivelDificultad;
    private int autorId;
    private String autorNombre;
    private TipoPregunta tipo = TipoPregunta.SELECCION_UNICA;
    private final java.util.List<OpcionRespuesta> opciones = new java.util.ArrayList<>();
    private final java.util.Set<String> respuestasCorrectas = new java.util.LinkedHashSet<>();

    public PreguntaBuilder conTipo(TipoPregunta tipo) {
        if (tipo != null) {
            this.tipo = tipo;
        }
        return this;
    }

    public PreguntaBuilder conOpcion(String clave, String texto) {
        if (clave != null && texto != null) {
            this.opciones.add(new OpcionRespuesta(clave, texto));
        }
        return this;
    }

    public PreguntaBuilder conOpciones(java.util.List<OpcionRespuesta> opciones) {
        if (opciones != null) {
            this.opciones.addAll(opciones);
        }
        return this;
    }

    public PreguntaBuilder conRespuestasCorrectas(String... respuestas) {
        if (respuestas != null) {
            for (String r : respuestas) {
                if (r != null && !r.isBlank()) {
                    this.respuestasCorrectas.add(r.trim().toUpperCase());
                }
            }
        }
        return this;
    }

    public PreguntaBuilder conRespuestasCorrectas(java.util.Collection<String> respuestas) {
        if (respuestas != null) {
            for (String r : respuestas) {
                if (r != null && !r.isBlank()) {
                    this.respuestasCorrectas.add(r.trim().toUpperCase());
                }
            }
        }
        return this;
    }

    public PreguntaBuilder conContexto(String contexto) {
        this.contexto = contexto;
        return this;
    }

    public PreguntaBuilder conPreguntaDirecta(String preguntaDirecta) {
        this.preguntaDirecta = preguntaDirecta;
        return this;
    }

    public PreguntaBuilder conDistractor1(String distractor1) {
        this.distractor1 = distractor1;
        return this;
    }

    public PreguntaBuilder conDistractor2(String distractor2) {
        this.distractor2 = distractor2;
        return this;
    }

    public PreguntaBuilder conDistractor3(String distractor3) {
        this.distractor3 = distractor3;
        return this;
    }

    public PreguntaBuilder conDistractor4(String distractor4) {
        this.distractor4 = distractor4;
        return this;
    }

    public PreguntaBuilder conRespuestaCorrecta(String respuestaCorrecta) {
        this.respuestaCorrecta = respuestaCorrecta;
        return this;
    }

    public PreguntaBuilder conJustificacion(String justificacion) {
        this.justificacion = justificacion;
        return this;
    }

    public PreguntaBuilder conBibliografia(String bibliografia) {
        this.bibliografia = bibliografia;
        return this;
    }

    public PreguntaBuilder conCompetencia(String competencia) {
        this.competencia = competencia;
        return this;
    }

    public PreguntaBuilder conTema(String tema) {
        this.tema = tema;
        return this;
    }

    public PreguntaBuilder conSubtema(String subtema) {
        this.subtema = subtema;
        return this;
    }

    public PreguntaBuilder conNivelDificultad(String nivelDificultad) {
        this.nivelDificultad = nivelDificultad;
        return this;
    }

    public PreguntaBuilder conAutorId(int autorId) {
        this.autorId = autorId;
        return this;
    }

    public PreguntaBuilder conAutorNombre(String autorNombre) {
        this.autorNombre = autorNombre;
        return this;
    }

    /**
     * Construye la instancia de {@link Pregunta}. El estado inicial siempre es
     * {@link EstadoPregunta#BORRADOR} y la fecha de creación se fija en el momento
     * del build.
     *
     * @return una nueva instancia de {@code Pregunta}
     */
    public Pregunta build() {
        Pregunta p = new Pregunta();
        p.setContexto(contexto);
        p.setPreguntaDirecta(preguntaDirecta);
        p.setDistractor1(distractor1);
        p.setDistractor2(distractor2);
        p.setDistractor3(distractor3);
        p.setDistractor4(distractor4);
        p.setRespuestaCorrecta(respuestaCorrecta);
        p.setJustificacion(justificacion);
        p.setBibliografia(bibliografia);
        p.setCompetencia(competencia);
        p.setTema(tema);
        p.setSubtema(subtema);
        p.setNivelDificultad(nivelDificultad);
        p.setAutorId(autorId);
        p.setAutorNombre(autorNombre);
        p.setEstado(EstadoPregunta.BORRADOR); // estado inicial fijo
        p.setFechaCreacion(LocalDateTime.now()); // timestamp del momento del build
        p.setTipo(tipo);
        if (!opciones.isEmpty()) {
            p.setOpciones(opciones);
        }
        if (!respuestasCorrectas.isEmpty()) {
            p.definirRespuestasCorrectas(respuestasCorrectas);
        }
        return p;
    }
}
