package co.edu.unicauca.bancopreguntas.users.service;

import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.users.domain.EstadoUsuario;
import co.edu.unicauca.bancopreguntas.users.domain.Rol;
import co.edu.unicauca.bancopreguntas.users.domain.UsuarioEntity;
import co.edu.unicauca.bancopreguntas.users.repository.UsuarioRepository;
import co.edu.unicauca.bancopreguntas.users.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de usuarios y roles (HU7: la autenticación y emisión de JWT
 * se delega por completo a Keycloak como proveedor de identidad autoritativo).
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDTO> listarRevisores() {
        return usuarioRepository.findByRolAndEstado(Rol.REVISOR, EstadoUsuario.ACTIVO)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserSummaryDTO> buscarPorId(Long id) {
        return usuarioRepository.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<UserSummaryDTO> buscarPorUsername(String username) {
        return usuarioRepository.findByNombreUsuarioIgnoreCase(username).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDTO> listarTodos() {
        return usuarioRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public UserSummaryDTO registrar(String username, String fullName, String email, String password, Rol rol) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El rol de usuario es obligatorio");
        }
        if (usuarioRepository.findByNombreUsuarioIgnoreCase(username.trim()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado");
        }
        String hash = passwordHasher.hash(password != null ? password : "");
        UsuarioEntity entity = new UsuarioEntity(null, username.trim(), fullName, email, rol, EstadoUsuario.ACTIVO, hash, LocalDateTime.now());
        UsuarioEntity saved = usuarioRepository.save(entity);
        return toDTO(saved);
    }

    public UserSummaryDTO toDTO(UsuarioEntity u) {
        return new UserSummaryDTO(u.getId(), u.getNombreUsuario(), u.getNombreCompleto(), u.getRol().name(), u.getEstado().name(), u.getEmail());
    }
}
