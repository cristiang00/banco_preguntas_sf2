package com.unicauca.taller2.usuarios.services;

import com.unicauca.taller2.usuarios.model.EstadoUsuario;
import com.unicauca.taller2.usuarios.model.Usuario;
import com.unicauca.taller2.usuarios.access.PasswordHasher;
import com.unicauca.taller2.usuarios.access.UsuarioRepository;

import java.util.Optional;

/**
 * Servicio de aplicación para autenticación.
 */
public class AutenticacionService {

    private final UsuarioRepository repository;
    private final PasswordHasher hasher;

    /**
     * Constructor del servicio de autenticación.
     *
     * @param repository repositorio de usuarios
     * @param hasher     hasher de contraseñas
     */
    public AutenticacionService(UsuarioRepository repository, PasswordHasher hasher) {
        this.repository = repository;
        this.hasher = hasher;
    }

    /**
     * Autentica un usuario con nombre de usuario y contraseña.
     *
     * @param nombreUsuario nombre de usuario
     * @param password      contraseña en texto plano
     * @return el Usuario autenticado
     * @throws AutenticacionException si las credenciales son inválidas o el usuario está inactivo
     */
    public Usuario autenticar(String nombreUsuario, String password) {
        Optional<Usuario> optUsuario = repository.buscarPorNombreUsuario(nombreUsuario);

        if (optUsuario.isEmpty()) {
            throw new AutenticacionException("Credenciales inválidas. Verifique su usuario y contraseña.");
        }

        Usuario usuario = optUsuario.get();

        if (usuario.getEstado() == EstadoUsuario.INACTIVO) {
            throw new AutenticacionException("Su cuenta se encuentra inactiva. Contacte al administrador.");
        }

        if (!hasher.verify(password, usuario.getPasswordHash())) {
            throw new AutenticacionException("Credenciales inválidas. Verifique su usuario y contraseña.");
        }

        return usuario;
    }
}
