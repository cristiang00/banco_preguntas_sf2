package com.unicauca.taller2.usuarios.access;

import com.unicauca.taller2.usuarios.model.EstadoUsuario;
import com.unicauca.taller2.usuarios.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz para la persistencia de usuarios.
 */
public interface UsuarioRepository {

    /**
     * Guarda un nuevo usuario en el almacenamiento.
     *
     * @param usuario el usuario a guardar
     */
    void guardar(Usuario usuario);

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param nombreUsuario el nombre de usuario a buscar
     * @return un Optional con el usuario si existe, o vacío si no
     */
    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);

    /**
     * Lista todos los usuarios registrados.
     *
     * @return la lista de todos los usuarios
     */
    List<Usuario> listarTodos();

    /**
     * Actualiza el estado de un usuario.
     *
     * @param nombreUsuario el nombre de usuario
     * @param estado el nuevo estado
     */
    void actualizarEstado(String nombreUsuario, EstadoUsuario estado);

    /**
     * Lista usuarios por un rol específico.
     *
     * @param rol el rol a filtrar
     * @return lista de usuarios que tienen el rol especificado
     */
    List<Usuario> listarPorRol(com.unicauca.taller2.usuarios.model.Rol rol);
}
