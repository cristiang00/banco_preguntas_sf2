package co.edu.unicauca.bancopreguntas.domain.services;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.AsignacionRevisorRepository;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;
import com.unicauca.taller2.usuarios.access.UsuarioRepository;
import com.unicauca.taller2.usuarios.model.EstadoUsuario;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AsignacionRevisorServiceTest {

    private AsignacionRevisorRepository asignacionRepository;
    private PreguntaRepository preguntaRepository;
    private UsuarioRepository usuarioRepository;
    private AsignacionRevisorService service;

    @BeforeEach
    void setUp() {
        asignacionRepository = mock(AsignacionRevisorRepository.class);
        preguntaRepository = mock(PreguntaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        // Constructor original — compatible con tests existentes
        service = new AsignacionRevisorService(asignacionRepository, preguntaRepository, usuarioRepository);
    }

    // -------------------------------------------------------------------------
    // Tests existentes (deben seguir pasando)
    // -------------------------------------------------------------------------

    @Test
    void asignar_preguntaNoPendiente_lanzaExcepcion() {
        Pregunta p = new Pregunta();
        p.setId(1);
        p.setEstado(EstadoPregunta.BORRADOR);
        when(preguntaRepository.buscarPorId(1)).thenReturn(Optional.of(p));

        Exception e = assertThrows(IllegalArgumentException.class, () -> service.asignarRevisores(1, Arrays.asList(2)));
        assertEquals("Solo las preguntas en estado Pendiente de revisión pueden tener revisores asignados", e.getMessage());
    }

    @Test
    void asignar_sinRevisores_lanzaExcepcion() {
        Pregunta p = new Pregunta();
        p.setId(1);
        p.setEstado(EstadoPregunta.PENDIENTE_REVISION);
        when(preguntaRepository.buscarPorId(1)).thenReturn(Optional.of(p));

        Exception e = assertThrows(IllegalArgumentException.class, () -> service.asignarRevisores(1, Collections.emptyList()));
        assertEquals("Debe seleccionar al menos un revisor", e.getMessage());
    }

    @Test
    void asignar_usuarioNoRevisor_lanzaExcepcion() {
        Pregunta p = new Pregunta();
        p.setId(1);
        p.setEstado(EstadoPregunta.PENDIENTE_REVISION);
        when(preguntaRepository.buscarPorId(1)).thenReturn(Optional.of(p));

        Usuario autor = new Usuario(2, "autor", "Autor", Rol.AUTOR_PREGUNTAS, EstadoUsuario.ACTIVO, "hash", LocalDateTime.now());
        when(usuarioRepository.listarTodos()).thenReturn(Arrays.asList(autor));

        Exception e = assertThrows(IllegalArgumentException.class, () -> service.asignarRevisores(1, Arrays.asList(2)));
        assertEquals("El usuario autor no puede ser asignado porque no tiene rol de Revisor", e.getMessage());
    }

    @Test
    void asignar_exitoso_guardaAsignacion() {
        Pregunta p = new Pregunta();
        p.setId(1);
        p.setEstado(EstadoPregunta.PENDIENTE_REVISION);
        when(preguntaRepository.buscarPorId(1)).thenReturn(Optional.of(p));

        Usuario revisor = new Usuario(3, "revisor", "Revisor", Rol.REVISOR, EstadoUsuario.ACTIVO, "hash", LocalDateTime.now());
        when(usuarioRepository.listarTodos()).thenReturn(Arrays.asList(revisor));
        when(asignacionRepository.existeAsignacion(1, 3)).thenReturn(false);

        service.asignarRevisores(1, Arrays.asList(3));

        verify(asignacionRepository, times(1)).asignar(1, 3);
    }

    // -------------------------------------------------------------------------
    // Tests nuevos — Observer + transición de estado
    // -------------------------------------------------------------------------

    @Test
    void asignar_exitoso_notificaObservadorUnaVez() {
        Pregunta p = new Pregunta();
        p.setId(1);
        p.setEstado(EstadoPregunta.PENDIENTE_REVISION);
        when(preguntaRepository.buscarPorId(1)).thenReturn(Optional.of(p));

        Usuario revisor = new Usuario(3, "rev", "Revisor Uno", Rol.REVISOR, EstadoUsuario.ACTIVO, "h", LocalDateTime.now());
        when(usuarioRepository.listarTodos()).thenReturn(Arrays.asList(revisor));
        when(asignacionRepository.existeAsignacion(1, 3)).thenReturn(false);

        NotificadorAsignacion mockObs = mock(NotificadorAsignacion.class);
        service.agregarObservador(mockObs);

        service.asignarRevisores(1, Arrays.asList(3));

        verify(mockObs, times(1)).notificarAsignacion(eq(p), eq(revisor));
    }

    @Test
    void asignar_dosRevisores_notificaDosVeces() {
        Pregunta p = new Pregunta();
        p.setId(1);
        p.setEstado(EstadoPregunta.PENDIENTE_REVISION);
        when(preguntaRepository.buscarPorId(1)).thenReturn(Optional.of(p));

        Usuario revisor1 = new Usuario(3, "rev1", "Revisor Uno", Rol.REVISOR, EstadoUsuario.ACTIVO, "h", LocalDateTime.now());
        Usuario revisor2 = new Usuario(4, "rev2", "Revisor Dos", Rol.REVISOR, EstadoUsuario.ACTIVO, "h", LocalDateTime.now());
        when(usuarioRepository.listarTodos()).thenReturn(Arrays.asList(revisor1, revisor2));
        when(asignacionRepository.existeAsignacion(1, 3)).thenReturn(false);
        when(asignacionRepository.existeAsignacion(1, 4)).thenReturn(false);

        NotificadorAsignacion mockObs = mock(NotificadorAsignacion.class);
        service.agregarObservador(mockObs);

        service.asignarRevisores(1, Arrays.asList(3, 4));

        verify(mockObs, times(2)).notificarAsignacion(eq(p), any(Usuario.class));
    }

    @Test
    void asignar_falla_noNotificaObservador() {
        Pregunta p = new Pregunta();
        p.setId(1);
        p.setEstado(EstadoPregunta.BORRADOR); // falla: no está en PENDIENTE_REVISION
        when(preguntaRepository.buscarPorId(1)).thenReturn(Optional.of(p));

        NotificadorAsignacion mockObs = mock(NotificadorAsignacion.class);
        service.agregarObservador(mockObs);

        assertThrows(IllegalArgumentException.class,
            () -> service.asignarRevisores(1, Arrays.asList(3)));
        verify(mockObs, never()).notificarAsignacion(any(), any());
    }

    @Test
    void asignar_exitoso_transicionaAEnRevision() {
        Pregunta p = new Pregunta();
        p.setId(1);
        p.setEstado(EstadoPregunta.PENDIENTE_REVISION);
        when(preguntaRepository.buscarPorId(1)).thenReturn(Optional.of(p));

        Usuario revisor = new Usuario(3, "rev", "Revisor", Rol.REVISOR, EstadoUsuario.ACTIVO, "h", LocalDateTime.now());
        when(usuarioRepository.listarTodos()).thenReturn(Arrays.asList(revisor));
        when(asignacionRepository.existeAsignacion(1, 3)).thenReturn(false);

        service.asignarRevisores(1, Arrays.asList(3));

        // Verificar transición de estado (Punto 9)
        verify(preguntaRepository, times(1)).actualizarEstado(1, EstadoPregunta.EN_REVISION);
    }

    @Test
    void asignar_revisorYaAsignado_noNotificaNiTransiciona() {
        Pregunta p = new Pregunta();
        p.setId(1);
        p.setEstado(EstadoPregunta.PENDIENTE_REVISION);
        when(preguntaRepository.buscarPorId(1)).thenReturn(Optional.of(p));

        Usuario revisor = new Usuario(3, "rev", "Revisor", Rol.REVISOR, EstadoUsuario.ACTIVO, "h", LocalDateTime.now());
        when(usuarioRepository.listarTodos()).thenReturn(Arrays.asList(revisor));
        when(asignacionRepository.existeAsignacion(1, 3)).thenReturn(true); // ya asignado

        NotificadorAsignacion mockObs = mock(NotificadorAsignacion.class);
        service.agregarObservador(mockObs);

        service.asignarRevisores(1, Arrays.asList(3));

        // No se notifica ni se transiciona porque ya estaba asignado
        verify(mockObs, never()).notificarAsignacion(any(), any());
        verify(preguntaRepository, never()).actualizarEstado(anyInt(), any());
    }

    @Test
    void removerObservador_noRecibeMasNotificaciones() {
        Pregunta p = new Pregunta();
        p.setId(1);
        p.setEstado(EstadoPregunta.PENDIENTE_REVISION);
        when(preguntaRepository.buscarPorId(1)).thenReturn(Optional.of(p));

        Usuario revisor = new Usuario(3, "rev", "Revisor", Rol.REVISOR, EstadoUsuario.ACTIVO, "h", LocalDateTime.now());
        when(usuarioRepository.listarTodos()).thenReturn(Arrays.asList(revisor));
        when(asignacionRepository.existeAsignacion(1, 3)).thenReturn(false);

        NotificadorAsignacion mockObs = mock(NotificadorAsignacion.class);
        service.agregarObservador(mockObs);
        service.removerObservador(mockObs);

        service.asignarRevisores(1, Arrays.asList(3));

        verify(mockObs, never()).notificarAsignacion(any(), any());
    }
}
