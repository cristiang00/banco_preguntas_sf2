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
        service = new AsignacionRevisorService(asignacionRepository, preguntaRepository, usuarioRepository);
    }

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
    void asignar_exitoso_guardaYNotifica() {
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
}
