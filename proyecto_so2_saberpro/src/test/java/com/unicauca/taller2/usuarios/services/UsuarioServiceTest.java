package com.unicauca.taller2.usuarios.services;

import com.unicauca.taller2.usuarios.model.EstadoUsuario;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;
import com.unicauca.taller2.usuarios.access.PasswordHasher;
import com.unicauca.taller2.usuarios.access.PasswordPolicy;
import com.unicauca.taller2.usuarios.access.PasswordPolicyException;
import com.unicauca.taller2.usuarios.access.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UsuarioService.
 */
class UsuarioServiceTest {

    private UsuarioRepository repository;
    private PasswordHasher hasher;
    private PasswordPolicy policy;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        hasher = mock(PasswordHasher.class);
        policy = mock(PasswordPolicy.class);
        service = new UsuarioService(repository, hasher, policy);
    }

    @Test
    @DisplayName("Registrar usuario válido: valida política, hashea contraseña y guarda")
    void registrarUsuarioValido_guardaConHash() {
        String password = "Abc123!@";
        String hashEsperado = "$argon2id$v=19$m=65536,t=3,p=1$hash_simulado";

        when(repository.buscarPorNombreUsuario("jperez")).thenReturn(Optional.empty());
        when(hasher.hash(password)).thenReturn(hashEsperado);

        service.registrarUsuario("jperez", "Juan Pérez", Rol.ESTUDIANTE, password);

        verify(policy).validar(password);

        verify(hasher).hash(password);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).guardar(captor.capture());

        Usuario guardado = captor.getValue();
        assertEquals("jperez", guardado.getNombreUsuario());
        assertEquals("Juan Pérez", guardado.getNombreCompleto());
        assertEquals(Rol.ESTUDIANTE, guardado.getRol());
        assertEquals(EstadoUsuario.ACTIVO, guardado.getEstado());
        assertEquals(hashEsperado, guardado.getPasswordHash());
    }

    @Test
    @DisplayName("Rechazar registro: contraseña sin mayúscula")
    void registrar_sinMayuscula_lanzaExcepcion() {
        doThrow(new PasswordPolicyException("Debe contener al menos 1 mayúscula"))
                .when(policy).validar("abc123!@");

        assertThrows(PasswordPolicyException.class, () ->
                service.registrarUsuario("user1", "Nombre", Rol.ESTUDIANTE, "abc123!@"));

        verify(repository, never()).guardar(any());
    }

    @Test
    @DisplayName("Rechazar registro: contraseña sin dígito")
    void registrar_sinDigito_lanzaExcepcion() {
        doThrow(new PasswordPolicyException("Debe contener al menos 1 dígito"))
                .when(policy).validar("Abcdef!@");

        assertThrows(PasswordPolicyException.class, () ->
                service.registrarUsuario("user1", "Nombre", Rol.DOCENTE, "Abcdef!@"));

        verify(repository, never()).guardar(any());
    }

    @Test
    @DisplayName("Rechazar registro: contraseña sin carácter especial")
    void registrar_sinCaracterEspecial_lanzaExcepcion() {
        doThrow(new PasswordPolicyException("Debe contener al menos 1 carácter especial"))
                .when(policy).validar("Abc12345");

        assertThrows(PasswordPolicyException.class, () ->
                service.registrarUsuario("user1", "Nombre", Rol.REVISOR, "Abc12345"));

        verify(repository, never()).guardar(any());
    }

    @Test
    @DisplayName("Rechazar registro: contraseña menor a 6 caracteres")
    void registrar_contraseñaCorta_lanzaExcepcion() {
        doThrow(new PasswordPolicyException("Mínimo 6 caracteres"))
                .when(policy).validar("A1!b");

        assertThrows(PasswordPolicyException.class, () ->
                service.registrarUsuario("user1", "Nombre", Rol.ESTUDIANTE, "A1!b"));

        verify(repository, never()).guardar(any());
    }

    @Test
    @DisplayName("Rechazar registro: nombre de usuario ya existente")
    void registrar_usuarioDuplicado_lanzaExcepcion() {
        Usuario existente = new Usuario("jperez", "Juan", Rol.ESTUDIANTE,
                EstadoUsuario.ACTIVO, "hash_existente");
        when(repository.buscarPorNombreUsuario("jperez")).thenReturn(Optional.of(existente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.registrarUsuario("jperez", "Juan Pérez", Rol.ESTUDIANTE, "Abc123!@"));

        assertTrue(ex.getMessage().contains("ya está registrado"));

        verify(hasher, never()).hash(any());
        verify(repository, never()).guardar(any());
    }

    @Test
    @DisplayName("Cambiar estado de usuario existente")
    void cambiarEstado_usuarioExistente_actualiza() {
        Usuario existente = new Usuario("jperez", "Juan", Rol.ESTUDIANTE,
                EstadoUsuario.ACTIVO, "hash");
        when(repository.buscarPorNombreUsuario("jperez")).thenReturn(Optional.of(existente));

        service.cambiarEstado("jperez", EstadoUsuario.INACTIVO);

        verify(repository).actualizarEstado("jperez", EstadoUsuario.INACTIVO);
    }

    @Test
    @DisplayName("Cambiar estado de usuario inexistente lanza excepción")
    void cambiarEstado_usuarioInexistente_lanzaExcepcion() {
        when(repository.buscarPorNombreUsuario("noexiste")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.cambiarEstado("noexiste", EstadoUsuario.INACTIVO));
    }
}
