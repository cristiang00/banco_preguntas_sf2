package com.unicauca.taller2.usuarios.services;

import com.unicauca.taller2.usuarios.model.EstadoUsuario;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;
import com.unicauca.taller2.usuarios.access.PasswordHasher;
import com.unicauca.taller2.usuarios.access.PasswordPolicy;
import com.unicauca.taller2.usuarios.access.UsuarioRepository;
import com.unicauca.taller2.usuarios.access.PasswordPolicyException;

import java.util.List;

/**
 * Servicio de aplicación para la gestión de usuarios.
 */
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordHasher hasher;
    private final PasswordPolicy policy;

    /**
     * Constructor del servicio de usuarios.
     *
     * @param repository repositorio de usuarios
     * @param hasher     hasher de contraseñas
     * @param policy     política de contraseñas
     */
    public UsuarioService(UsuarioRepository repository, PasswordHasher hasher, PasswordPolicy policy) {
        this.repository = repository;
        this.hasher = hasher;
        this.policy = policy;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param nombreUsuario  nombre de usuario
     * @param nombreCompleto nombre completo del usuario
     * @param rol            rol asignado al usuario
     * @param passwordPlano  contraseña en texto plano
     * @throws PasswordPolicyException si la contraseña no cumple la política
     * @throws IllegalArgumentException si el nombre de usuario ya está registrado
     */
    public void registrarUsuario(String nombreUsuario, String nombreCompleto,
                                  Rol rol, String passwordPlano) {
        policy.validar(passwordPlano);

        if (repository.buscarPorNombreUsuario(nombreUsuario).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario '" + nombreUsuario + "' ya está registrado");
        }

        String hash = hasher.hash(passwordPlano);

        Usuario usuario = new Usuario(nombreUsuario, nombreCompleto, rol, EstadoUsuario.ACTIVO, hash);
        repository.guardar(usuario);
    }

    /**
     * Lista todos los usuarios registrados.
     *
     * @return lista de usuarios
     */
    public List<Usuario> listarUsuarios() {
        return repository.listarTodos();
    }

    /**
     * Cambia el estado de un usuario.
     *
     * @param nombreUsuario nombre de usuario
     * @param nuevoEstado   nuevo estado
     * @throws IllegalArgumentException si el usuario no existe
     */
    public void cambiarEstado(String nombreUsuario, EstadoUsuario nuevoEstado) {
        if (repository.buscarPorNombreUsuario(nombreUsuario).isEmpty()) {
            throw new IllegalArgumentException("El usuario '" + nombreUsuario + "' no existe");
        }
        repository.actualizarEstado(nombreUsuario, nuevoEstado);
    }
}
