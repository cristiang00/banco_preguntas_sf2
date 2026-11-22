package co.edu.unicauca.bancopreguntas.review;

import co.edu.unicauca.bancopreguntas.review.domain.model.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.model.Evaluacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Observacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;
import co.edu.unicauca.bancopreguntas.review.domain.policy.PoliticaConsensoRevision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de dominio puro (sin levantar Spring ni JPA) para HU8.S4.1.
 * Cubre reglas de negocio, ciclo de vida de Revision y PoliticaConsensoRevision.
 */
class PoliticaConsensoRevisionTest {

    private PoliticaConsensoRevision politica;

    @BeforeEach
    void setUp() {
        politica = new PoliticaConsensoRevision();
    }

    @Test
    @DisplayName("Regla 1: Debe exigir entre 1 y 3 revisores")
    void testValidarCantidadRevisores() {
        assertThrows(IllegalArgumentException.class, () -> politica.validarCantidadRevisores(null));
        assertThrows(IllegalArgumentException.class, () -> politica.validarCantidadRevisores(Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> politica.validarCantidadRevisores(List.of(1L, 2L, 3L, 4L)));

        assertDoesNotThrow(() -> politica.validarCantidadRevisores(List.of(1L)));
        assertDoesNotThrow(() -> politica.validarCantidadRevisores(List.of(1L, 2L)));
        assertDoesNotThrow(() -> politica.validarCantidadRevisores(List.of(1L, 2L, 3L)));
    }

    @Test
    @DisplayName("Regla 2: El autor no puede ser revisor de su propia pregunta")
    void testValidarExclusionAutor() {
        Long autorId = 10L;
        assertThrows(IllegalArgumentException.class, () -> politica.validarExclusionAutor(autorId, List.of(5L, 10L)));
        assertDoesNotThrow(() -> politica.validarExclusionAutor(autorId, List.of(5L, 6L)));
    }

    @Test
    @DisplayName("Regla 3: El rechazo exige al menos una observación no vacía")
    void testValidarRechazoConObservaciones() {
        assertThrows(IllegalArgumentException.class,
                () -> politica.validarRechazoConObservaciones(null, false));
        assertThrows(IllegalArgumentException.class,
                () -> politica.validarRechazoConObservaciones(List.of("   "), false));

        assertDoesNotThrow(() -> politica.validarRechazoConObservaciones(List.of("Falta bibliografía"), false));
        assertDoesNotThrow(() -> politica.validarRechazoConObservaciones(Collections.emptyList(), true));
    }

    @Test
    @DisplayName("Regla 4: Aprobación por consenso unánime de todos los revisores asignados")
    void testDeterminarConsensoAprobacion() {
        Revision rev1 = Revision.crearNueva(100L, 1L, "Revisor 1", "r1@test.com", "Lectura", "Medio");
        Revision rev2 = Revision.crearNueva(100L, 2L, "Revisor 2", "r2@test.com", "Lectura", "Medio");

        rev1.aprobar();
        assertFalse(politica.determinarConsensoAprobacion(List.of(rev1, rev2)));

        rev2.aprobar();
        assertTrue(politica.determinarConsensoAprobacion(List.of(rev1, rev2)));
    }

    @Test
    @DisplayName("Regla 5: Transiciones de estado de la entidad pura Revision y bloqueo de re-decisión")
    void testTransicionesRevision() {
        Revision rev = Revision.crearNueva(200L, 5L, "Revisor Juan", "juan@test.com", "Matemáticas", "Difícil");
        assertEquals(EstadoRevision.ASIGNADA, rev.getEstado());

        rev.evaluar();
        assertEquals(EstadoRevision.EVALUADA, rev.getEstado());

        rev.aprobar();
        assertEquals(EstadoRevision.APROBADA, rev.getEstado());
        assertNotNull(rev.getFechaDecision());

        // Intentar decidir nuevamente debe fallar con IllegalStateException
        assertThrows(IllegalStateException.class, rev::aprobar);
        assertThrows(IllegalStateException.class, rev::rechazar);
        assertThrows(IllegalStateException.class, () -> politica.validarNoReDecision(rev));
    }

    @Test
    @DisplayName("Entidad Observación y Evaluación puras")
    void testObservacionYEvaluacionPuras() {
        Observacion obs = Observacion.crear(1L, 50L, "Revisar ortografía");
        assertEquals(1L, obs.getRevisionId());
        assertEquals(50L, obs.getPreguntaId());
        assertEquals("Revisar ortografía", obs.getTexto());
        assertTrue(obs.isVisibleAutor());

        Evaluacion eval = new Evaluacion(null, 1L, "Claridad", 4.5, "Buena redacción", null);
        assertEquals("Claridad", eval.getCriterio());
        assertEquals(4.5, eval.getCalificacion());
    }
}

