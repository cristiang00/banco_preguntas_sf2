package co.edu.unicauca.bancopreguntas.review;

import co.edu.unicauca.bancopreguntas.contracts.dto.DecisionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.EvaluacionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaAprobadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaRechazadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.RevisoresAsignadosEvent;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.*;
import co.edu.unicauca.bancopreguntas.review.application.usecases.AsignarRevisoresUseCase;
import co.edu.unicauca.bancopreguntas.review.application.usecases.ConsultarHistorialUseCase;
import co.edu.unicauca.bancopreguntas.review.application.usecases.ListarAsignadasUseCase;
import co.edu.unicauca.bancopreguntas.review.application.usecases.RegistrarDecisionUseCase;
import co.edu.unicauca.bancopreguntas.review.application.usecases.RegistrarEvaluacionUseCase;
import co.edu.unicauca.bancopreguntas.review.domain.model.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.model.Evaluacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Observacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.JpaRevisionRepositoryAdapter;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.memory.InMemoryRevisionRepositoryAdapter;
import co.edu.unicauca.bancopreguntas.review.repository.EvaluacionRevisorRepository;
import co.edu.unicauca.bancopreguntas.review.repository.ObservacionRepository;
import co.edu.unicauca.bancopreguntas.review.repository.RevisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas de casos de uso de arquitectura limpia (HU8.S4.2)
 * ejecutadas con el adaptador en memoria {@link InMemoryRevisionRepositoryAdapter}
 * y puertos secundarios simulados con Mockito, SIN levantar Spring Boot ni base de datos JPA.
 */
@ExtendWith(MockitoExtension.class)
class ReviewHexagonalUseCaseTest {

    private InMemoryRevisionRepositoryAdapter repositoryPort;

    @Mock
    private PreguntasClientPort preguntasClientPort;

    @Mock
    private LifecycleClientPort lifecycleClientPort;

    @Mock
    private UsuariosClientPort usuariosClientPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    private AsignarRevisoresUseCase asignarRevisoresUseCase;
    private RegistrarEvaluacionUseCase registrarEvaluacionUseCase;
    private RegistrarDecisionUseCase registrarDecisionUseCase;
    private ListarAsignadasUseCase listarAsignadasUseCase;
    private ConsultarHistorialUseCase consultarHistorialUseCase;

    @BeforeEach
    void setUp() {
        repositoryPort = new InMemoryRevisionRepositoryAdapter();
        asignarRevisoresUseCase = new AsignarRevisoresUseCase(
                repositoryPort, preguntasClientPort, lifecycleClientPort, usuariosClientPort, eventPublisherPort
        );
        registrarEvaluacionUseCase = new RegistrarEvaluacionUseCase(repositoryPort);
        registrarDecisionUseCase = new RegistrarDecisionUseCase(
                repositoryPort, preguntasClientPort, lifecycleClientPort, usuariosClientPort, eventPublisherPort
        );
        listarAsignadasUseCase = new ListarAsignadasUseCase(
                repositoryPort, preguntasClientPort
        );
        consultarHistorialUseCase = new ConsultarHistorialUseCase(
                repositoryPort, preguntasClientPort
        );
    }

    @Test
    @DisplayName("AsignarRevisoresUseCase: asigna correctamente 2 revisores y emite evento")
    void testAsignarRevisoresExitoso() {
        Long preguntaId = 100L;
        PreguntaDTO pregunta = new PreguntaDTO();
        pregunta.setId(preguntaId);
        pregunta.setAutorId(1L);
        pregunta.setAutorNombre("Autor Pro");
        pregunta.setCompetencia("Razonamiento Cuantitativo");
        pregunta.setNivelDificultad("Medio");

        when(preguntasClientPort.obtenerPregunta(preguntaId)).thenReturn(Optional.of(pregunta));
        when(lifecycleClientPort.obtenerEstado(preguntaId)).thenReturn(EstadoPreguntaDTO.PENDIENTE_REVISION);

        UserSummaryDTO r1 = new UserSummaryDTO(2L, "rev1", "Revisor Uno", "REVISOR", "ACTIVO", "r1@unicauca.edu.co");
        UserSummaryDTO r2 = new UserSummaryDTO(3L, "rev2", "Revisor Dos", "REVISOR", "ACTIVO", "r2@unicauca.edu.co");
        when(usuariosClientPort.buscarPorId(2L)).thenReturn(Optional.of(r1));
        when(usuariosClientPort.buscarPorId(3L)).thenReturn(Optional.of(r2));

        List<Revision> asignaciones = asignarRevisoresUseCase.execute(preguntaId, List.of(2L, 3L), 99L, "corr-test-hex");

        assertEquals(2, asignaciones.size());
        assertEquals(2, repositoryPort.findByPreguntaId(preguntaId).size());
        verify(eventPublisherPort, times(1)).publish(any(RevisoresAsignadosEvent.class));
    }

    @Test
    @DisplayName("RegistrarDecisionUseCase: rechazo con observaciones actualiza estado y notifica")
    void testRegistrarDecisionRechazo() {
        Long revId = 5L;
        Long preguntaId = 200L;
        Revision rev = Revision.crearNueva(preguntaId, revId, "Revisor Juan", "juan@test.com", "Lectura", "Fácil");
        rev = repositoryPort.save(rev);

        PreguntaDTO pregunta = new PreguntaDTO();
        pregunta.setId(preguntaId);
        pregunta.setAutorId(10L);
        pregunta.setAutorNombre("Autor Ana");
        pregunta.setContexto("Texto de lectura");

        when(preguntasClientPort.obtenerPregunta(preguntaId)).thenReturn(Optional.of(pregunta));
        UserSummaryDTO autor = new UserSummaryDTO(10L, "ana", "Autor Ana", "AUTOR", "ACTIVO", "ana@unicauca.edu.co");
        when(usuariosClientPort.buscarPorId(10L)).thenReturn(Optional.of(autor));

        DecisionRequestDTO request = new DecisionRequestDTO();
        request.setDecision("RECHAZAR");
        request.setObservaciones(List.of("La opción B es ambigua", "Falta bibliografía primaria"));

        Revision resultado = registrarDecisionUseCase.execute(rev.getId(), request, revId, "corr-rechazo");

        assertEquals(EstadoRevision.RECHAZADA, resultado.getEstado());
        verify(lifecycleClientPort, times(1)).aplicarDecision(
                eq(preguntaId), eq("RECHAZADA"), eq(revId), eq("ana@unicauca.edu.co"), any(), any(), eq("corr-rechazo")
        );
        verify(eventPublisherPort, times(1)).publish(any(PreguntaRechazadaEvent.class));
    }

    @Test
    @DisplayName("RegistrarDecisionUseCase: consenso unánime de aprobación emite PreguntaAprobadaEvent")
    void testRegistrarDecisionAprobacionConsenso() {
        Long preguntaId = 300L;
        Revision rev1 = repositoryPort.save(Revision.crearNueva(preguntaId, 11L, "Revisor 1", "r1@test.com", "Inglés", "Medio"));
        Revision rev2 = repositoryPort.save(Revision.crearNueva(preguntaId, 12L, "Revisor 2", "r2@test.com", "Inglés", "Medio"));

        PreguntaDTO pregunta = new PreguntaDTO();
        pregunta.setId(preguntaId);
        pregunta.setAutorId(7L);
        pregunta.setAutorNombre("Autor Carlos");
        when(preguntasClientPort.obtenerPregunta(preguntaId)).thenReturn(Optional.of(pregunta));
        when(usuariosClientPort.buscarPorId(7L)).thenReturn(Optional.of(
                new UserSummaryDTO(7L, "carlos", "Carlos", "AUTOR", "ACTIVO", "carlos@unicauca.edu.co")
        ));

        // Revisor 1 aprueba (aún falta revisor 2)
        DecisionRequestDTO req1 = new DecisionRequestDTO();
        req1.setDecision("APROBAR");
        registrarDecisionUseCase.execute(rev1.getId(), req1, 11L, "corr-aprob-1");

        verify(eventPublisherPort, never()).publish(any(PreguntaAprobadaEvent.class));

        // Revisor 2 aprueba -> Consenso alcanzado
        DecisionRequestDTO req2 = new DecisionRequestDTO();
        req2.setDecision("APROBAR");
        registrarDecisionUseCase.execute(rev2.getId(), req2, 12L, "corr-aprob-2");

        verify(lifecycleClientPort, times(1)).aplicarDecision(
                eq(preguntaId), eq("APROBADA"), eq(12L), eq("carlos@unicauca.edu.co"), any(), any(), eq("corr-aprob-2")
        );
        verify(eventPublisherPort, times(1)).publish(any(PreguntaAprobadaEvent.class));
    }

    @Test
    @DisplayName("ListarAsignadasUseCase: filtra y pagina en memoria sin alterar el dominio")
    void testListarAsignadasEnMemoria() {
        Long revisorId = 88L;
        Revision r1 = Revision.crearNueva(401L, revisorId, "Revisor", "r@test.com", "Matemáticas", "Difícil");
        Revision r2 = Revision.crearNueva(402L, revisorId, "Revisor", "r@test.com", "Inglés", "Fácil");
        r2.aprobar();
        repositoryPort.save(r1);
        repositoryPort.save(r2);

        PreguntaDTO p1 = new PreguntaDTO();
        p1.setId(401L);
        p1.setCompetencia("Matemáticas");
        p1.setNivelDificultad("Difícil");
        when(preguntasClientPort.obtenerPregunta(401L)).thenReturn(Optional.of(p1));

        Map<String, Object> result = listarAsignadasUseCase.execute(
                revisorId, 0, 10, "ASIGNADA", "Difícil", "Matemáticas"
        );

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
        assertEquals(1, content.size());
        assertEquals(401L, content.get(0).get("preguntaId"));
        assertEquals("ASIGNADA", content.get(0).get("estadoRevision"));
    }

    @Test
    @DisplayName("ConsultarHistorialUseCase: el autor consulta su historial exitosamente con rondas y observaciones (HU6.S1)")
    void testConsultarHistorialAutorExitoso() {
        Long preguntaId = 500L;
        Long autorId = 15L;
        PreguntaDTO pregunta = new PreguntaDTO();
        pregunta.setId(preguntaId);
        pregunta.setAutorId(autorId);
        pregunta.setAutorNombre("Autor Juan");
        when(preguntasClientPort.obtenerPregunta(preguntaId)).thenReturn(Optional.of(pregunta));

        Revision rev = Revision.crearNueva(preguntaId, 8L, "Revisor Carlos", "carlos@test.com", "Lectura", "Medio");
        rev.rechazar();
        Revision guardada = repositoryPort.save(rev);
        repositoryPort.saveObservacion(Observacion.crear(guardada.getId(), preguntaId, "La justificación es incompleta"));
        repositoryPort.saveEvaluacion(Evaluacion.crear(guardada.getId(), "Pedagógico", 3.5, "Mejorar redacción"));

        Map<String, Object> historial = consultarHistorialUseCase.execute(preguntaId, autorId, 0, 10);

        assertNotNull(historial);
        assertEquals(preguntaId, historial.get("preguntaId"));
        assertEquals(autorId, historial.get("autorId"));
        assertEquals(1, historial.get("totalElements"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) historial.get("content");
        assertEquals(1, content.size());
        assertEquals("RECHAZADA", content.get(0).get("estado"));

        @SuppressWarnings("unchecked")
        List<String> obs = (List<String>) content.get(0).get("observaciones");
        assertEquals(1, obs.size());
        assertEquals("La justificación es incompleta", obs.get(0));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> evals = (List<Map<String, Object>>) content.get(0).get("evaluaciones");
        assertEquals(1, evals.size());
        assertEquals("Pedagógico", evals.get(0).get("criterio"));
        assertEquals(3.5, evals.get(0).get("calificacion"));
    }

    @Test
    @DisplayName("HU8.S2.4: RegistrarEvaluacionUseCase guarda evaluación, observaciones y cambia estado a EVALUADA")
    void testRegistrarEvaluacionUseCase() {
        Revision rev = repositoryPort.save(Revision.crearNueva(700L, 4L, "Revisor 1", "r1@unicauca.edu.co", "Lectura", "Medio"));
        EvaluacionRequestDTO req = new EvaluacionRequestDTO();
        req.setCriterio("Técnico");
        req.setCalificacion(4.5);
        req.setComentario("Buena estructura");
        req.setObservaciones(List.of("Ajustar ortografía en opción C"));

        Evaluacion eval = registrarEvaluacionUseCase.execute(rev.getId(), req, 4L);

        assertNotNull(eval.getId());
        assertEquals("Técnico", eval.getCriterio());
        assertEquals(EstadoRevision.EVALUADA, repositoryPort.findById(rev.getId()).orElseThrow().getEstado());
        assertEquals(1, repositoryPort.findObservacionesByRevisionId(rev.getId()).size());
    }

    @Test
    @DisplayName("HU8.S3.2: Los mismos casos de uso funcionan idénticamente con JpaRevisionRepositoryAdapter e InMemoryRevisionRepositoryAdapter sin tocar el dominio")
    void testIntercambiabilidadAdaptadoresSinModificarDominio() {
        // Ejecutar el mismo flujo contra JpaRevisionRepositoryAdapter (segundo adaptador del puerto)
        RevisionRepository stubJpaRev = new ReviewServiceTest.StubRevisionRepository();
        EvaluacionRevisorRepository stubJpaEval = new ReviewServiceTest.StubEvaluacionRepository();
        ObservacionRepository stubJpaObs = new ReviewServiceTest.StubObservacionRepository();
        RevisionRepositoryPort jpaAdapter = new JpaRevisionRepositoryAdapter(stubJpaRev, stubJpaEval, stubJpaObs);

        AsignarRevisoresUseCase asignarConJpa = new AsignarRevisoresUseCase(
                jpaAdapter, preguntasClientPort, lifecycleClientPort, usuariosClientPort, eventPublisherPort
        );
        ConsultarHistorialUseCase historialConJpa = new ConsultarHistorialUseCase(jpaAdapter, preguntasClientPort);

        Long preguntaId = 800L;
        PreguntaDTO p = new PreguntaDTO();
        p.setId(preguntaId);
        p.setAutorId(2L);
        p.setAutorNombre("Autor Uno");
        p.setCompetencia("Diseño de Software");
        p.setNivelDificultad("Alto");
        when(preguntasClientPort.obtenerPregunta(preguntaId)).thenReturn(Optional.of(p));
        when(lifecycleClientPort.obtenerEstado(preguntaId)).thenReturn(EstadoPreguntaDTO.PENDIENTE_REVISION);
        when(usuariosClientPort.buscarPorId(4L)).thenReturn(Optional.of(
                new UserSummaryDTO(4L, "revisor1", "Revisor Uno", "REVISOR", "ACTIVO", "revisor1@unicauca.edu.co")
        ));

        List<Revision> creadasJpa = asignarConJpa.execute(preguntaId, List.of(4L), 1L, "corr-jpa");
        List<Revision> creadasMem = asignarRevisoresUseCase.execute(preguntaId, List.of(4L), 1L, "corr-mem");

        assertEquals(creadasMem.size(), creadasJpa.size());
        assertEquals(creadasMem.get(0).getEstado(), creadasJpa.get(0).getEstado());
        assertEquals(1, historialConJpa.execute(preguntaId, 2L, 0, 10).get("totalElements"));
        assertEquals(1, consultarHistorialUseCase.execute(preguntaId, 2L, 0, 10).get("totalElements"));
    }

    @Test
    @DisplayName("ConsultarHistorialUseCase: un usuario que no es autor recibe SecurityException (403 Forbidden) (HU6.S2)")
    void testConsultarHistorialNoAutorLanza403() {
        Long preguntaId = 600L;
        Long autorId = 20L;
        Long otroUsuarioId = 99L;

        PreguntaDTO pregunta = new PreguntaDTO();
        pregunta.setId(preguntaId);
        pregunta.setAutorId(autorId);
        when(preguntasClientPort.obtenerPregunta(preguntaId)).thenReturn(Optional.of(pregunta));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> consultarHistorialUseCase.execute(preguntaId, otroUsuarioId, 0, 10));
        assertTrue(ex.getMessage().contains("Acceso denegado: solo el autor"));
    }

    @Test
    @DisplayName("ConsultarHistorialUseCase: si la pregunta no existe lanza IllegalArgumentException (404)")
    void testConsultarHistorialPreguntaInexistente() {
        Long preguntaId = 9999L;
        when(preguntasClientPort.obtenerPregunta(preguntaId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> consultarHistorialUseCase.execute(preguntaId, 1L, 0, 10));
    }

    @Test
    @DisplayName("HU6 + HU5: Ronda 2 con distinto revisor alcanza consenso APROBADA sin bloquearse por Ronda 1 RECHAZADA")
    void testRonda2DistintoRevisorApruebaSinBloqueoYConservaRonda1() {
        Long preguntaId = 910L;
        PreguntaDTO pV1 = new PreguntaDTO();
        pV1.setId(preguntaId);
        pV1.setAutorId(2L);
        pV1.setAutorNombre("Autor Uno");
        pV1.setContexto("Contexto v1");
        pV1.setCompetencia("Lectura Crítica");
        pV1.setNivelDificultad("Medio");
        pV1.setVersion(1);

        when(preguntasClientPort.obtenerPregunta(preguntaId)).thenReturn(Optional.of(pV1));
        when(lifecycleClientPort.obtenerEstado(preguntaId)).thenReturn(EstadoPreguntaDTO.PENDIENTE_REVISION);
        when(usuariosClientPort.buscarPorId(2L)).thenReturn(Optional.of(
                new UserSummaryDTO(2L, "autor1", "Autor Uno", "AUTOR", "ACTIVO", "autor1@unicauca.edu.co")));
        when(usuariosClientPort.buscarPorId(4L)).thenReturn(Optional.of(
                new UserSummaryDTO(4L, "revisor1", "Revisor Uno", "REVISOR", "ACTIVO", "revisor1@unicauca.edu.co")));
        when(usuariosClientPort.buscarPorId(5L)).thenReturn(Optional.of(
                new UserSummaryDTO(5L, "revisor2", "Revisor Dos", "REVISOR", "ACTIVO", "revisor2@unicauca.edu.co")));

        // Ronda 1: revisor1 rechaza
        List<Revision> ronda1 = asignarRevisoresUseCase.execute(preguntaId, List.of(4L), 1L, "corr-r1");
        assertEquals(1, ronda1.get(0).getRonda());
        DecisionRequestDTO rej = new DecisionRequestDTO("RECHAZAR", List.of("Mejorar justificación"), "Rechazo v1");
        registrarDecisionUseCase.execute(ronda1.get(0).getId(), rej, 4L, "corr-r1-dec");

        // El autor crea versión mejorada v2 y reenvía a revisión
        pV1.setVersion(2);
        pV1.setContexto("Contexto v2 mejorado");

        // Ronda 2: se asigna revisor2 y aprueba -> debe pasar a APROBADA
        List<Revision> ronda2 = asignarRevisoresUseCase.execute(preguntaId, List.of(5L), 1L, "corr-r2");
        assertEquals(2, ronda2.get(0).getRonda());

        DecisionRequestDTO apr = new DecisionRequestDTO("APROBAR", List.of(), "Versión 2 excelente");
        Revision decidida2 = registrarDecisionUseCase.execute(ronda2.get(0).getId(), apr, 5L, "corr-r2-dec");

        assertEquals(EstadoRevision.APROBADA, decidida2.getEstado());
        verify(lifecycleClientPort, times(1)).aplicarDecision(
                eq(preguntaId), eq("APROBADA"), eq(5L), eq("autor1@unicauca.edu.co"), any(), any(), eq("corr-r2-dec")
        );
        verify(preguntasClientPort, times(1)).actualizarProyeccionEstado(preguntaId, "APROBADA");
        verify(eventPublisherPort, times(1)).publish(any(PreguntaAprobadaEvent.class));

        // Historial debe conservar ambas rondas (Ronda 2 APROBADA y Ronda 1 RECHAZADA)
        Map<String, Object> hist = consultarHistorialUseCase.execute(preguntaId, 2L, 0, 10);
        assertEquals(2, hist.get("totalElements"));
    }

    @Test
    @DisplayName("HU6.S3: Ronda 2 con el MISMO revisor crea nueva revisión sin sobrescribir la Ronda 1 en el historial")
    void testRonda2MismoRevisorConservaRonda1RechazadaEnHistorial() {
        Long preguntaId = 920L;
        PreguntaDTO pregunta = new PreguntaDTO();
        pregunta.setId(preguntaId);
        pregunta.setAutorId(2L);
        pregunta.setAutorNombre("Autor Uno");
        pregunta.setContexto("Contexto v1");
        pregunta.setCompetencia("Lectura Crítica");
        pregunta.setNivelDificultad("Medio");
        pregunta.setVersion(1);

        when(preguntasClientPort.obtenerPregunta(preguntaId)).thenReturn(Optional.of(pregunta));
        when(lifecycleClientPort.obtenerEstado(preguntaId)).thenReturn(EstadoPreguntaDTO.PENDIENTE_REVISION);
        when(usuariosClientPort.buscarPorId(2L)).thenReturn(Optional.of(
                new UserSummaryDTO(2L, "autor1", "Autor Uno", "AUTOR", "ACTIVO", "autor1@unicauca.edu.co")));
        when(usuariosClientPort.buscarPorId(4L)).thenReturn(Optional.of(
                new UserSummaryDTO(4L, "revisor1", "Revisor Uno", "REVISOR", "ACTIVO", "revisor1@unicauca.edu.co")));

        // Ronda 1: revisor1 rechaza
        Revision r1 = asignarRevisoresUseCase.execute(preguntaId, List.of(4L), 1L, "corr-m1").get(0);
        registrarDecisionUseCase.execute(r1.getId(),
                new DecisionRequestDTO("RECHAZAR", List.of("Corregir opción D"), "Rechazo ronda 1"), 4L, "corr-m1-dec");

        // Nueva versión v2 -> se asigna nuevamente al MISMO revisor1 (ID 4)
        pregunta.setVersion(2);
        Revision r2 = asignarRevisoresUseCase.execute(preguntaId, List.of(4L), 1L, "corr-m2").get(0);

        assertNotEquals(r1.getId(), r2.getId(), "La ronda 2 debe ser un registro independiente");
        assertEquals(1, r1.getRonda());
        assertEquals(2, r2.getRonda());
        assertEquals(EstadoRevision.RECHAZADA, repositoryPort.findById(r1.getId()).orElseThrow().getEstado(),
                "La decisión RECHAZADA de la Ronda 1 no debe sobrescribirse");

        // Revisor1 aprueba Ronda 2 -> consenso alcanzado y pregunta APROBADA
        registrarDecisionUseCase.execute(r2.getId(),
                new DecisionRequestDTO("APROBAR", List.of(), "Aprobada en ronda 2"), 4L, "corr-m2-dec");

        verify(preguntasClientPort, times(1)).actualizarProyeccionEstado(preguntaId, "APROBADA");

        Map<String, Object> hist = consultarHistorialUseCase.execute(preguntaId, 2L, 0, 10);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) hist.get("content");
        assertEquals(2, content.size());
        assertEquals(2, content.get(0).get("ronda"));
        assertEquals("APROBADA", content.get(0).get("estado"));
        assertEquals(1, content.get(1).get("ronda"));
        assertEquals("RECHAZADA", content.get(1).get("estado"));
    }
}
