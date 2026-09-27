package co.edu.unicauca.bancopreguntas.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el patrón Builder de {@link Pregunta}.
 */
class PreguntaBuilderTest {

    @Test
    void build_todosLosCampos_retornaValoresCorrectos() {
        Pregunta p = new PreguntaBuilder()
            .conContexto("Contexto de prueba extenso")
            .conPreguntaDirecta("¿Cuál es la capital de Colombia?")
            .conDistractor1("Ciudad de México")
            .conDistractor2("Buenos Aires provincia")
            .conDistractor3("Lima departamento")
            .conDistractor4("Quito ciudad capital")
            .conRespuestaCorrecta("A")
            .conJustificacion("Bogotá es la capital oficial de Colombia según la Constitución de 1991")
            .conBibliografia("Constitución Política de Colombia, 1991")
            .conCompetencia("Lectura Crítica")
            .conTema("Geografía Política")
            .conSubtema("Capitales")
            .conNivelDificultad("Bajo")
            .build();

        assertEquals("Contexto de prueba extenso", p.getContexto());
        assertEquals("¿Cuál es la capital de Colombia?", p.getPreguntaDirecta());
        assertEquals("Ciudad de México", p.getDistractor1());
        assertEquals("Buenos Aires provincia", p.getDistractor2());
        assertEquals("Lima departamento", p.getDistractor3());
        assertEquals("Quito ciudad capital", p.getDistractor4());
        assertEquals("A", p.getRespuestaCorrecta());
        assertEquals("Bogotá es la capital oficial de Colombia según la Constitución de 1991", p.getJustificacion());
        assertEquals("Constitución Política de Colombia, 1991", p.getBibliografia());
        assertEquals("Lectura Crítica", p.getCompetencia());
        assertEquals("Geografía Política", p.getTema());
        assertEquals("Capitales", p.getSubtema());
        assertEquals("Bajo", p.getNivelDificultad());
    }

    @Test
    void build_estadoInicialSiempreEsBorrador() {
        Pregunta p = new PreguntaBuilder().build();
        assertEquals(EstadoPregunta.BORRADOR, p.getEstado());
    }

    @Test
    void build_fechaCreacionNoEsNull() {
        Pregunta p = new PreguntaBuilder().build();
        assertNotNull(p.getFechaCreacion());
    }

    @Test
    void build_sinCampos_estadoEsBorrador() {
        Pregunta p = new PreguntaBuilder().build();
        // El builder no lanza excepciones — eso es responsabilidad de PreguntaService
        assertEquals(EstadoPregunta.BORRADOR, p.getEstado());
        assertNull(p.getContexto());
        assertNull(p.getPreguntaDirecta());
    }

    @Test
    void build_autorId_seAsignaCorrectamente() {
        Pregunta p = new PreguntaBuilder()
            .conAutorId(42)
            .conAutorNombre("Juan Pérez")
            .build();
        assertEquals(42, p.getAutorId());
        assertEquals("Juan Pérez", p.getAutorNombre());
    }

    @Test
    void build_encadenamiento_retornaElMismoBuilder() {
        // Verifica que el Builder es fluido (cada con* devuelve this)
        PreguntaBuilder builder = new PreguntaBuilder();
        PreguntaBuilder resultado = builder.conContexto("ctx");
        // El builder fluido devuelve la misma instancia
        assertSame(builder, resultado);
    }

    @Test
    void build_dosConstrucciones_retornaInstanciasDiferentes() {
        PreguntaBuilder builder = new PreguntaBuilder().conContexto("contexto");
        Pregunta p1 = builder.build();
        Pregunta p2 = builder.build();
        // Deben ser instancias distintas
        assertNotSame(p1, p2);
        // Con los mismos datos
        assertEquals(p1.getContexto(), p2.getContexto());
    }
}
