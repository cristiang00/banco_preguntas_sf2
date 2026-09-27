package co.edu.unicauca.bancopreguntas.domain.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PreguntaTest {

    @Test
    void testCreacionYGetters() {
        LocalDateTime ahora = LocalDateTime.now();
        Pregunta p = new Pregunta(1, "Contexto", "Directa", "A", "B", "C", "D", "A", "Just", "Biblio", "Comp", "Tema", "Subtema", "Bajo", EstadoPregunta.BORRADOR, 10, "Juan", ahora);

        assertEquals(1, p.getId());
        assertEquals("Contexto", p.getContexto());
        assertEquals("Directa", p.getPreguntaDirecta());
        assertEquals("A", p.getDistractor1());
        assertEquals("B", p.getDistractor2());
        assertEquals("C", p.getDistractor3());
        assertEquals("D", p.getDistractor4());
        assertEquals("A", p.getRespuestaCorrecta());
        assertEquals("Just", p.getJustificacion());
        assertEquals("Biblio", p.getBibliografia());
        assertEquals("Comp", p.getCompetencia());
        assertEquals("Tema", p.getTema());
        assertEquals("Subtema", p.getSubtema());
        assertEquals("Bajo", p.getNivelDificultad());
        assertEquals(EstadoPregunta.BORRADOR, p.getEstado());
        assertEquals(10, p.getAutorId());
        assertEquals("Juan", p.getAutorNombre());
        assertEquals(ahora, p.getFechaCreacion());
    }

    @Test
    void testSetters() {
        Pregunta p = new Pregunta();
        p.setId(2);
        p.setContexto("Contexto modificado");

        assertEquals(2, p.getId());
        assertEquals("Contexto modificado", p.getContexto());
    }
}
