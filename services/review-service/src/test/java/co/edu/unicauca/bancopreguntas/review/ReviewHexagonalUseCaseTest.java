package co.edu.unicauca.bancopreguntas.review;

import co.edu.unicauca.bancopreguntas.contracts.dto.DecisionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaAprobadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaRechazadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.RevisoresAsignadosEvent;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.*;
import co.edu.unicauca.bancopreguntas.review.application.usecases.AsignarRevisoresUseCase;
import co.edu.unicauca.bancopreguntas.review.application.usecases.ListarAsignadasUseCase;
import co.edu.unicauca.bancopreguntas.review.application.usecases.RegistrarDecisionUseCase;
import co.edu.unicauca.bancopreguntas.review.domain.model.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.memory.InMemoryRevisionRepositoryAdapter;
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
    private RegistrarDecisionUseCase registrarDecisionUseCase;
    private ListarAsignadasUseCase listarAsignadasUseCase;

    @BeforeEach
    void setUp() {
        repositoryPort = new InMemoryRevisionRepositoryAdapter();
        asignarRevisoresUseCase = new AsignarRevisoresUseCase(
                repositoryPort, preguntasClientPort, lifecycleClientPort, usuariosClientPort, eventPublisherPort
        );
        registrarDecisionUseCase = new RegistrarDecisionUseCase(
                repositoryPort, preguntasClientPort, lifecycleClientPort, usuariosClientPort, eventPublisherPort
        );
        listarAsignadasUseCase = new ListarAsignadasUseCase(
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
}
