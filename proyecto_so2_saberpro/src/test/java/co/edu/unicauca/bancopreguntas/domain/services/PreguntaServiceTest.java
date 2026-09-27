package co.edu.unicauca.bancopreguntas.domain.services;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PreguntaServiceTest {

    private PreguntaRepository preguntaRepository;
    private PreguntaService preguntaService;

    @BeforeEach
    void setUp() {
        preguntaRepository = mock(PreguntaRepository.class);
        preguntaService = new PreguntaService(preguntaRepository);
    }

    private Pregunta crearPreguntaValida() {
        Pregunta p = new Pregunta();
        p.setContexto("Contexto de prueba");
        p.setPreguntaDirecta("Pregunta directa de prueba");
        p.setDistractor1("Distractor A");
        p.setDistractor2("Distractor B");
        p.setDistractor3("Distractor C");
        p.setDistractor4("Distractor D");
        p.setRespuestaCorrecta("A");
        p.setNivelDificultad("Bajo");
        p.setBibliografia("Biblio");
        p.setCompetencia("Comp");
        p.setTema("Tema");
        p.setSubtema("Sub");
        return p;
    }

    @Test
    void crearPregunta_sinContextoNiPregunta_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setContexto("");
        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.crearPregunta(p, 1));
        assertEquals("Debe completar el contexto y la pregunta directa", e.getMessage());
    }

    @Test
    void crearPregunta_sinDistractores_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setDistractor1("");
        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.crearPregunta(p, 1));
        assertEquals("Debe registrar los cuatro distractores", e.getMessage());
    }

    @Test
    void crearPregunta_sinRespuestaCorrecta_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setRespuestaCorrecta(null);
        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.crearPregunta(p, 1));
        assertEquals("Debe seleccionar una respuesta correcta", e.getMessage());
    }

    @Test
    void crearPregunta_sinDificultad_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setNivelDificultad("   ");
        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.crearPregunta(p, 1));
        assertEquals("Debe seleccionar el nivel de dificultad", e.getMessage());
    }

    @Test
    void crearPregunta_sinBibliografia_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setBibliografia("");
        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.crearPregunta(p, 1));
        assertEquals("Debe registrar la bibliografía de referencia", e.getMessage());
    }

    @Test
    void crearPregunta_sinCompetenciaTemSubtema_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setTema("");
        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.crearPregunta(p, 1));
        assertEquals("Debe seleccionar competencia, tema y subtema", e.getMessage());
    }

    @Test
    void crearPregunta_todosLosCampos_guardaEnBorrador() {
        Pregunta p = crearPreguntaValida();
        preguntaService.crearPregunta(p, 1);
        assertEquals(EstadoPregunta.BORRADOR, p.getEstado());
        assertEquals(1, p.getAutorId());
        verify(preguntaRepository, times(1)).guardar(p);
    }

    @Test
    void enviarARevision_autorNoPropietario_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setId(100);
        p.setAutorId(1);
        p.setEstado(EstadoPregunta.BORRADOR);
        when(preguntaRepository.buscarPorId(100)).thenReturn(Optional.of(p));

        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.enviarARevision(100, 2));
        assertEquals("No tiene permisos para modificar el estado de esta pregunta", e.getMessage());
    }

    @Test
    void enviarARevision_estadoNoBorrador_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setId(100);
        p.setAutorId(1);
        p.setEstado(EstadoPregunta.PENDIENTE_REVISION);
        when(preguntaRepository.buscarPorId(100)).thenReturn(Optional.of(p));

        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.enviarARevision(100, 1));
        assertEquals("Solo las preguntas en estado Borrador pueden enviarse a revisión", e.getMessage());
    }

    @Test
    void enviarARevision_desdeBorrador_cambiaAPendiente() {
        Pregunta p = crearPreguntaValida();
        p.setId(100);
        p.setAutorId(1);
        p.setEstado(EstadoPregunta.BORRADOR);
        when(preguntaRepository.buscarPorId(100)).thenReturn(Optional.of(p));

        preguntaService.enviarARevision(100, 1);
        verify(preguntaRepository, times(1)).actualizarEstado(100, EstadoPregunta.PENDIENTE_REVISION);
    }

    @Test
    void editarPregunta_enRevision_lanzaExcepcion() {
        Pregunta existente = crearPreguntaValida();
        existente.setId(100);
        existente.setAutorId(1);
        existente.setEstado(EstadoPregunta.EN_REVISION);
        when(preguntaRepository.buscarPorId(100)).thenReturn(Optional.of(existente));

        Pregunta modificada = crearPreguntaValida();
        modificada.setId(100);

        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.editarPregunta(modificada, 1));
        assertEquals("La pregunta no puede modificarse mientras esté en revisión", e.getMessage());
    }

    @Test
    void listarMisPreguntas_sinResultados_retornaListaVacia() {
        when(preguntaRepository.listarPorAutor(anyInt(), any(), any(), anyInt(), anyInt())).thenReturn(Arrays.asList());
        List<Pregunta> resultados = preguntaService.listarMisPreguntas(1, null, "Todos", 0, 10);
        assertTrue(resultados.isEmpty());
    }

    @Test
    void listarMisPreguntas_conResultados() {
        when(preguntaRepository.listarPorAutor(anyInt(), any(), any(), anyInt(), anyInt())).thenReturn(Arrays.asList(crearPreguntaValida()));
        List<Pregunta> resultados = preguntaService.listarMisPreguntas(1, null, "Todos", 0, 10);
        assertEquals(1, resultados.size());
    }

    // --- Tests para RF-12 (Expresiones prohibidas) ---
    @Test
    void crearPregunta_conTodasLasAnterioresEnPregunta_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setPreguntaDirecta("¿Cuál es correcta de todas las anteriores?");
        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.crearPregunta(p, 1));
        assertEquals("No se permiten expresiones como 'Todas las anteriores' o 'Ninguna de las anteriores'", e.getMessage());
    }

    @Test
    void crearPregunta_conNingunaDeLasAnterioresEnDistractor_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        // Prueba con tildes y mayúsculas
        p.setDistractor1("NíngunA dE lAs antEriores");
        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.crearPregunta(p, 1));
        assertEquals("No se permiten expresiones como 'Todas las anteriores' o 'Ninguna de las anteriores'", e.getMessage());
    }

    @Test
    void crearPregunta_conExpresionProhibidaEnDistractor3_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setDistractor3("todas las anteriores son correctas");
        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.crearPregunta(p, 1));
        assertEquals("No se permiten expresiones como 'Todas las anteriores' o 'Ninguna de las anteriores'", e.getMessage());
    }

    // --- Tests para RF-13 (Longitud y estructura mínima de distractores) ---
    @Test
    void crearPregunta_distractorVacioOMuyCorto_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setDistractor2("   A   "); // Solo 1 caracter util
        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.crearPregunta(p, 1));
        assertEquals("Los distractores deben cumplir con una longitud y estructura mínimas", e.getMessage());
    }

    @Test
    void crearPregunta_distractorInvalidoEnOtraOpcion_lanzaExcepcion() {
        Pregunta p = crearPreguntaValida();
        p.setDistractor4("NO"); // 2 caracteres, el mínimo es 3
        Exception e = assertThrows(IllegalArgumentException.class, () -> preguntaService.crearPregunta(p, 1));
        assertEquals("Los distractores deben cumplir con una longitud y estructura mínimas", e.getMessage());
    }
}
