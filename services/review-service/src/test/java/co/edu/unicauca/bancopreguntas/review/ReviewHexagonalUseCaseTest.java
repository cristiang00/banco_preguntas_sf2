package co.edu.unicauca.bancopreguntas.review;

import co.edu.unicauca.bancopreguntas.contracts.dto.DecisionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.EvaluacionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaAprobadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaRechazadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.RabbitMQConstants;
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
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.EvaluacionRevisorEntity;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.ObservacionEntity;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.RevisionEntity;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.memory.InMemoryRevisionRepositoryAdapter;
import co.edu.unicauca.bancopreguntas.review.messaging.ReviewEventPublisher;
import co.edu.unicauca.bancopreguntas.review.repository.EvaluacionRevisorRepository;
import co.edu.unicauca.bancopreguntas.review.repository.ObservacionRepository;
import co.edu.unicauca.bancopreguntas.review.repository.RevisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

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
        RevisionRepository stubJpaRev = new StubRevisionRepository();
        EvaluacionRevisorRepository stubJpaEval = new StubEvaluacionRepository();
        ObservacionRepository stubJpaObs = new StubObservacionRepository();
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

    @Test
    @DisplayName("HU4: AsignarRevisoresUseCase rechaza 0 revisores, >3 revisores y al autor como revisor; permite 3")
    void testAsignarRevisoresValidacionesLimitesYAutor() {
        Long preguntaId = 950L;
        PreguntaDTO p = new PreguntaDTO();
        p.setId(preguntaId);
        p.setAutorId(3L);
        p.setAutorNombre("Autor");
        p.setCompetencia("Lectura Crítica");
        p.setNivelDificultad("Medio");
        lenient().when(preguntasClientPort.obtenerPregunta(preguntaId)).thenReturn(Optional.of(p));
        lenient().when(lifecycleClientPort.obtenerEstado(preguntaId)).thenReturn(EstadoPreguntaDTO.PENDIENTE_REVISION);
        lenient().when(usuariosClientPort.buscarPorId(4L)).thenReturn(Optional.of(new UserSummaryDTO(4L, "r1", "Rev 1", "REVISOR", "ACTIVO", "r1@unicauca.edu.co")));
        lenient().when(usuariosClientPort.buscarPorId(5L)).thenReturn(Optional.of(new UserSummaryDTO(5L, "r2", "Rev 2", "REVISOR", "ACTIVO", "r2@unicauca.edu.co")));
        lenient().when(usuariosClientPort.buscarPorId(6L)).thenReturn(Optional.of(new UserSummaryDTO(6L, "r3", "Rev 3", "REVISOR", "ACTIVO", "r3@unicauca.edu.co")));

        assertThrows(IllegalArgumentException.class, () -> asignarRevisoresUseCase.execute(preguntaId, List.of(), 1L, "c0"));
        assertThrows(IllegalArgumentException.class, () -> asignarRevisoresUseCase.execute(preguntaId, List.of(4L, 5L, 6L, 7L), 1L, "c4"));
        assertThrows(IllegalArgumentException.class, () -> asignarRevisoresUseCase.execute(preguntaId, List.of(3L), 1L, "c-autor"));

        List<Revision> tres = asignarRevisoresUseCase.execute(preguntaId, List.of(4L, 5L, 6L), 1L, "c3");
        assertEquals(3, tres.size());
    }

    @Test
    @DisplayName("HU5: RegistrarDecisionUseCase exige observación al rechazar, bloquea revisor no asignado (403) y doble decisión (409)")
    void testRegistrarDecisionValidacionesSeguridadYEstado() {
        Long preguntaId = 960L;
        Revision rev = repositoryPort.save(Revision.crearNueva(preguntaId, 4L, "Rev 1", "r1@unicauca.edu.co", "Lectura", "Medio"));

        PreguntaDTO p = new PreguntaDTO();
        p.setId(preguntaId);
        p.setAutorId(2L);
        p.setAutorNombre("Autor");
        lenient().when(preguntasClientPort.obtenerPregunta(preguntaId)).thenReturn(Optional.of(p));
        lenient().when(usuariosClientPort.buscarPorId(2L)).thenReturn(Optional.of(new UserSummaryDTO(2L, "autor1", "Autor", "AUTOR", "ACTIVO", "autor.real@unicauca.edu.co")));

        assertThrows(SecurityException.class, () ->
                registrarDecisionUseCase.execute(rev.getId(), new DecisionRequestDTO("APROBAR", null, "OK"), 999L, "c-403"));
        assertThrows(IllegalArgumentException.class, () ->
                registrarDecisionUseCase.execute(rev.getId(), new DecisionRequestDTO("RECHAZAR", List.of(), ""), 4L, "c-400"));

        registrarDecisionUseCase.execute(rev.getId(), new DecisionRequestDTO("APROBAR", null, "OK"), 4L, "c-ok");
        assertThrows(IllegalStateException.class, () ->
                registrarDecisionUseCase.execute(rev.getId(), new DecisionRequestDTO("APROBAR", null, "Dup"), 4L, "c-409"));
    }

    @Test
    @DisplayName("HU12.S2: ListarAsignadasUseCase aplica filtros de estado/competencia/dificultad antes de paginar")
    void testListarAsignadasFiltrosCombinadosYPaginacionExacta() {
        Long revisorId = 44L;
        Revision r1 = Revision.crearNueva(971L, revisorId, "Rev", "r@u.co", "Matemáticas", "Medio");
        Revision r2 = Revision.crearNueva(972L, revisorId, "Rev", "r@u.co", "Matemáticas", "Alto");
        r2.evaluar();
        Revision r3 = Revision.crearNueva(973L, revisorId, "Rev", "r@u.co", "Lectura", "Medio");
        repositoryPort.save(r1);
        repositoryPort.save(r2);
        repositoryPort.save(r3);

        PreguntaDTO sample = new PreguntaDTO();
        sample.setContexto("Contexto demo");
        lenient().when(preguntasClientPort.obtenerPregunta(anyLong())).thenReturn(Optional.of(sample));

        Map<String, Object> porEstado = listarAsignadasUseCase.execute(revisorId, 0, 10, "ASIGNADA", null, null);
        assertEquals(2L, ((Number) porEstado.get("totalElements")).longValue());

        Map<String, Object> paginado = listarAsignadasUseCase.execute(revisorId, 0, 1, "ASIGNADA", null, null);
        assertEquals(1, ((List<?>) paginado.get("content")).size());
        assertEquals(2L, ((Number) paginado.get("totalElements")).longValue());
        assertEquals(2, ((Number) paginado.get("totalPages")).intValue());
    }

    @Test
    @DisplayName("HU12.S3: ReviewEventPublisher publica en exchange saberpro.events vía RabbitTemplate")
    void testPublicacionRabbitMQEnExchangeSaberproEvents() {
        RabbitTemplate mockRabbitTemplate = mock(RabbitTemplate.class);
        ReviewEventPublisher publisherWithRabbit = new ReviewEventPublisher(
                null, mockRabbitTemplate, null, "http://localhost:8086", "http://localhost:8087");

        PreguntaAprobadaEvent event = new PreguntaAprobadaEvent(
                999L, 3L, "Autor", "autor.real@unicauca.edu.co", "Contexto", "Aprobada", "corr-rmq");

        publisherWithRabbit.publish(event);

        verify(mockRabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConstants.EXCHANGE_NAME),
                eq("saberpro.PreguntaAprobada"),
                anyString());
    }

    // Stubs en memoria de Spring Data JPA para probar JpaRevisionRepositoryAdapter sin BD
    static class StubRevisionRepository implements RevisionRepository {
        Map<Long, RevisionEntity> map = new HashMap<>();
        long seq = 1;

        @Override
        public <S extends RevisionEntity> S save(S entity) {
            if (entity.getId() == null) entity.setId(seq++);
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override public Optional<RevisionEntity> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public List<RevisionEntity> findByPreguntaId(Long pId) {
            return map.values().stream().filter(r -> r.getPreguntaId().equals(pId)).collect(Collectors.toList());
        }
        @Override public Optional<RevisionEntity> findByPreguntaIdAndRevisorId(Long pId, Long rId) {
            return map.values().stream().filter(r -> r.getPreguntaId().equals(pId) && r.getRevisorId().equals(rId)).findFirst();
        }
        @Override public Optional<RevisionEntity> findByPreguntaIdAndRevisorIdAndRonda(Long pId, Long rId, Integer ronda) {
            int target = (ronda != null && ronda >= 1) ? ronda : 1;
            return map.values().stream()
                    .filter(r -> r.getPreguntaId().equals(pId) && r.getRevisorId().equals(rId) && r.getRonda() == target)
                    .findFirst();
        }
        @Override public Page<RevisionEntity> findByRevisorId(Long rId, Pageable pageable) { return Page.empty(pageable); }
        @Override
        public Page<RevisionEntity> findByRevisorIdWithFilters(Long rId,
                co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.EstadoRevision estado,
                String competencia, String dificultad, Pageable pageable) {
            List<RevisionEntity> filtered = map.values().stream()
                    .filter(r -> r.getRevisorId().equals(rId))
                    .filter(r -> estado == null || r.getEstado() == estado)
                    .filter(r -> competencia == null || (r.getCompetencia() != null && r.getCompetencia().equalsIgnoreCase(competencia)))
                    .filter(r -> dificultad == null || (r.getNivelDificultad() != null && r.getNivelDificultad().equalsIgnoreCase(dificultad)))
                    .collect(Collectors.toList());
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            List<RevisionEntity> sub = (start <= end && start < filtered.size()) ? filtered.subList(start, end) : Collections.emptyList();
            return new PageImpl<>(sub, pageable, filtered.size());
        }
        @Override public List<RevisionEntity> findByRevisorId(Long rId) {
            return map.values().stream().filter(r -> r.getRevisorId().equals(rId)).collect(Collectors.toList());
        }
        @Override public boolean existsByPreguntaIdAndRevisorId(Long pId, Long rId) {
            return map.values().stream().anyMatch(r -> r.getPreguntaId().equals(pId) && r.getRevisorId().equals(rId));
        }
        @Override public <S extends RevisionEntity> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public boolean existsById(Long aLong) { return map.containsKey(aLong); }
        @Override public List<RevisionEntity> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<RevisionEntity> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) { map.remove(aLong); }
        @Override public void delete(RevisionEntity entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends RevisionEntity> entities) {}
        @Override public void deleteAll() {}
        @Override public void flush() {}
        @Override public <S extends RevisionEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends RevisionEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<RevisionEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public RevisionEntity getOne(Long aLong) { return null; }
        @Override public RevisionEntity getById(Long aLong) { return null; }
        @Override public RevisionEntity getReferenceById(Long aLong) { return null; }
        @Override public <S extends RevisionEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends RevisionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends RevisionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends RevisionEntity> Page<S> findAll(org.springframework.data.domain.Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends RevisionEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends RevisionEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends RevisionEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<RevisionEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public Page<RevisionEntity> findAll(Pageable pageable) { return null; }
    }

    static class StubEvaluacionRepository implements EvaluacionRevisorRepository {
        List<EvaluacionRevisorEntity> list = new ArrayList<>();
        @Override public <S extends EvaluacionRevisorEntity> S save(S entity) { list.add(entity); return entity; }
        @Override public List<EvaluacionRevisorEntity> findByRevisionId(Long revisionId) {
            return list.stream().filter(e -> e.getRevisionId().equals(revisionId)).collect(Collectors.toList());
        }
        @Override public <S extends EvaluacionRevisorEntity> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<EvaluacionRevisorEntity> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<EvaluacionRevisorEntity> findAll() { return list; }
        @Override public List<EvaluacionRevisorEntity> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return list.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(EvaluacionRevisorEntity entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends EvaluacionRevisorEntity> entities) {}
        @Override public void deleteAll() {}
        @Override public void flush() {}
        @Override public <S extends EvaluacionRevisorEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends EvaluacionRevisorEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<EvaluacionRevisorEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public EvaluacionRevisorEntity getOne(Long aLong) { return null; }
        @Override public EvaluacionRevisorEntity getById(Long aLong) { return null; }
        @Override public EvaluacionRevisorEntity getReferenceById(Long aLong) { return null; }
        @Override public <S extends EvaluacionRevisorEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends EvaluacionRevisorEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends EvaluacionRevisorEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends EvaluacionRevisorEntity> Page<S> findAll(org.springframework.data.domain.Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends EvaluacionRevisorEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends EvaluacionRevisorEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends EvaluacionRevisorEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<EvaluacionRevisorEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public Page<EvaluacionRevisorEntity> findAll(Pageable pageable) { return null; }
    }

    static class StubObservacionRepository implements ObservacionRepository {
        List<ObservacionEntity> list = new ArrayList<>();
        @Override public <S extends ObservacionEntity> S save(S entity) { list.add(entity); return entity; }
        @Override public List<ObservacionEntity> findByRevisionId(Long rId) {
            return list.stream().filter(o -> o.getRevisionId().equals(rId)).collect(Collectors.toList());
        }
        @Override public List<ObservacionEntity> findByPreguntaIdAndVisibleAutorTrue(Long pId) {
            return list.stream().filter(o -> o.getPreguntaId().equals(pId)).collect(Collectors.toList());
        }
        @Override public <S extends ObservacionEntity> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<ObservacionEntity> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<ObservacionEntity> findAll() { return list; }
        @Override public List<ObservacionEntity> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return list.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(ObservacionEntity entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends ObservacionEntity> entities) {}
        @Override public void deleteAll() {}
        @Override public void flush() {}
        @Override public <S extends ObservacionEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ObservacionEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<ObservacionEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public ObservacionEntity getOne(Long aLong) { return null; }
        @Override public ObservacionEntity getById(Long aLong) { return null; }
        @Override public ObservacionEntity getReferenceById(Long aLong) { return null; }
        @Override public <S extends ObservacionEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends ObservacionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends ObservacionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends ObservacionEntity> Page<S> findAll(org.springframework.data.domain.Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends ObservacionEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends ObservacionEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends ObservacionEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<ObservacionEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public Page<ObservacionEntity> findAll(Pageable pageable) { return null; }
    }
}
