package com.unicauca.taller2.usuarios.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la entidad {@link Usuario}.
 */
class UsuarioTest {

    @Test
    void constructor_completo_setCamposCorrectamente() {
        LocalDateTime ahora = LocalDateTime.now();
        Usuario u = new Usuario(1, "jdoe", "John Doe", Rol.ADMINISTRADOR,
                                EstadoUsuario.ACTIVO, "hash123", ahora);

        assertEquals(1, u.getId());
        assertEquals("jdoe", u.getNombreUsuario());
        assertEquals("John Doe", u.getNombreCompleto());
        assertEquals(Rol.ADMINISTRADOR, u.getRol());
        assertEquals(EstadoUsuario.ACTIVO, u.getEstado());
        assertEquals("hash123", u.getPasswordHash());
        assertEquals(ahora, u.getFechaCreacion());
    }

    @Test
    void constructor_sinId_setIdACero() {
        Usuario u = new Usuario("user", "Nombre Completo", Rol.REVISOR, EstadoUsuario.ACTIVO, "hash");
        assertEquals(0, u.getId());
    }

    @Test
    void constructor_sinId_setFechaCreacionNoNull() {
        Usuario u = new Usuario("user", "Nombre", Rol.AUTOR_PREGUNTAS, EstadoUsuario.ACTIVO, "hash");
        assertNotNull(u.getFechaCreacion());
    }

    @Test
    void setEstado_actualizaEstadoCorrectamente() {
        Usuario u = new Usuario("user", "Nombre", Rol.REVISOR, EstadoUsuario.ACTIVO, "hash");
        assertEquals(EstadoUsuario.ACTIVO, u.getEstado());
        u.setEstado(EstadoUsuario.INACTIVO);
        assertEquals(EstadoUsuario.INACTIVO, u.getEstado());
    }

    @Test
    void setEstado_desdeInactivo_aActivo() {
        Usuario u = new Usuario("user2", "Nombre2", Rol.REVISOR, EstadoUsuario.INACTIVO, "hash");
        u.setEstado(EstadoUsuario.ACTIVO);
        assertEquals(EstadoUsuario.ACTIVO, u.getEstado());
    }

    @Test
    void toString_contieneDatosEsenciales() {
        Usuario u = new Usuario(1, "jdoe", "John Doe", Rol.ADMINISTRADOR,
                                EstadoUsuario.ACTIVO, "hash", LocalDateTime.now());
        String str = u.toString();
        assertTrue(str.contains("jdoe"), "toString debe contener el nombreUsuario");
        assertTrue(str.contains("John Doe"), "toString debe contener el nombreCompleto");
    }

    @Test
    void getRol_retornaRolCorrecto() {
        Usuario admin = new Usuario("a", "Admin", Rol.ADMINISTRADOR, EstadoUsuario.ACTIVO, "h");
        Usuario revisor = new Usuario("r", "Rev", Rol.REVISOR, EstadoUsuario.ACTIVO, "h");
        Usuario autor = new Usuario("u", "Autor", Rol.AUTOR_PREGUNTAS, EstadoUsuario.ACTIVO, "h");

        assertEquals(Rol.ADMINISTRADOR, admin.getRol());
        assertEquals(Rol.REVISOR, revisor.getRol());
        assertEquals(Rol.AUTOR_PREGUNTAS, autor.getRol());
    }
}
