package com.unicauca.taller2.usuarios.services;

import com.unicauca.taller2.usuarios.model.EstadoUsuario;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;
import com.unicauca.taller2.usuarios.access.PasswordHasher;
import com.unicauca.taller2.usuarios.access.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AutenticacionService.
 */
class AutenticacionServiceTest {

    private UsuarioRepository repository;
    private PasswordHasher hasher;
    private AutenticacionService service;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        hasher = mock(PasswordHasher.class);
        service = new AutenticacionService(repository, hasher);
    }

    @Test
    @DisplayName("Login exitoso con credenciales válidas y usuario activo")
    void autenticar_credencialesValidas_retornaUsuario() {
        Usuario usuario = new Usuario(1, "jperez", "Juan Pérez", Rol.ESTUDIANTE,
                EstadoUsuario.ACTIVO, "$argon2id$hash", null);
        when(repository.buscarPorNombreUsuario("jperez")).thenReturn(Optional.of(usuario));
        when(hasher.verify("Abc123!@", "$argon2id$hash")).thenReturn(true);

        Usuario resultado = service.autenticar("jperez", "Abc123!@");

        assertNotNull(resultado);
        assertEquals("jperez", resultado.getNombreUsuario());
        assertEquals("Juan Pérez", resultado.getNombreCompleto());
        assertEquals(Rol.ESTUDIANTE, resultado.getRol());
    }

    @Test
    @DisplayName("Login fallido: usuario no existe (mensaje genérico)")
    void autenticar_usuarioNoExiste_lanzaExcepcionGenerica() {
        when(repository.buscarPorNombreUsuario("noexiste")).thenReturn(Optional.empty());

        AutenticacionException ex = assertThrows(AutenticacionException.class, () ->
                service.autenticar("noexiste", "Abc123!@"));

        assertTrue(ex.getMessage().contains("Credenciales inválidas"));
        assertFalse(ex.getMessage().toLowerCase().contains("no existe"));
    }

    @Test
    @DisplayName("Login fallido: contraseña incorrecta (mensaje genérico)")
    void autenticar_contraseñaIncorrecta_lanzaExcepcionGenerica() {
        Usuario usuario = new Usuario(1, "jperez", "Juan Pérez", Rol.ESTUDIANTE,
                EstadoUsuario.ACTIVO, "$argon2id$hash", null);
        when(repository.buscarPorNombreUsuario("jperez")).thenReturn(Optional.of(usuario));
        when(hasher.verify("contraseñaMala", "$argon2id$hash")).thenReturn(false);

        AutenticacionException ex = assertThrows(AutenticacionException.class, () ->
                service.autenticar("jperez", "contraseñaMala"));

        assertTrue(ex.getMessage().contains("Credenciales inválidas"));
        assertEquals("Credenciales inválidas. Verifique su usuario y contraseña.",
                ex.getMessage());
    }

    @Test
    @DisplayName("Login fallido: usuario inactivo (mensaje claro)")
    void autenticar_usuarioInactivo_lanzaExcepcionConMensajeClaro() {
        Usuario usuario = new Usuario(1, "jperez", "Juan Pérez", Rol.ESTUDIANTE,
                EstadoUsuario.INACTIVO, "$argon2id$hash", null);
        when(repository.buscarPorNombreUsuario("jperez")).thenReturn(Optional.of(usuario));

        AutenticacionException ex = assertThrows(AutenticacionException.class, () ->
                service.autenticar("jperez", "Abc123!@"));

        assertTrue(ex.getMessage().toLowerCase().contains("inactiva"));

        verify(hasher, never()).verify(any(), any());
    }

    @Test
    @DisplayName("Login exitoso con diferentes roles")
    void autenticar_diferentesRoles_retornaUsuarioCorrecto() {
        Usuario admin = new Usuario(1, "admin1", "Admin User", Rol.ADMINISTRADOR,
                EstadoUsuario.ACTIVO, "$hash_admin", null);
        when(repository.buscarPorNombreUsuario("admin1")).thenReturn(Optional.of(admin));
        when(hasher.verify("Pass123!", "$hash_admin")).thenReturn(true);

        Usuario resultado = service.autenticar("admin1", "Pass123!");
        assertEquals(Rol.ADMINISTRADOR, resultado.getRol());

        Usuario docente = new Usuario(2, "docente1", "Docente User", Rol.DOCENTE,
                EstadoUsuario.ACTIVO, "$hash_docente", null);
        when(repository.buscarPorNombreUsuario("docente1")).thenReturn(Optional.of(docente));
        when(hasher.verify("Pass456!", "$hash_docente")).thenReturn(true);

        resultado = service.autenticar("docente1", "Pass456!");
        assertEquals(Rol.DOCENTE, resultado.getRol());
    }
}
