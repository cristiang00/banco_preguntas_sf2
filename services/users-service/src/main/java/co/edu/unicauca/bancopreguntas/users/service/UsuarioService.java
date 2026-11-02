package co.edu.unicauca.bancopreguntas.users.service;

import co.edu.unicauca.bancopreguntas.contracts.dto.TokenResponseDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.users.domain.EstadoUsuario;
import co.edu.unicauca.bancopreguntas.users.domain.Rol;
import co.edu.unicauca.bancopreguntas.users.domain.UsuarioEntity;
import co.edu.unicauca.bancopreguntas.users.repository.UsuarioRepository;
import co.edu.unicauca.bancopreguntas.users.security.JwtUtil;
import co.edu.unicauca.bancopreguntas.users.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final JwtUtil jwtUtil;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.jwtUtil = jwtUtil;
    }

    @Transactional(readOnly = true)
    public TokenResponseDTO autenticar(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Usuario y contraseña son requeridos");
        }

        UsuarioEntity usuario = usuarioRepository.findByNombreUsuarioIgnoreCase(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new IllegalStateException("El usuario está inactivo o bloqueado");
        }

        boolean passwordValido = passwordHasher.verify(password, usuario.getPasswordHash());
        if (!passwordValido) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String roleStr = usuario.getRol().name();
        String token = jwtUtil.generateToken(usuario.getId(), usuario.getNombreUsuario(), usuario.getNombreCompleto(), roleStr);

        return new TokenResponseDTO(token, usuario.getId(), usuario.getNombreUsuario(), usuario.getNombreCompleto(), roleStr);
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
        if (usuarioRepository.findByNombreUsuarioIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado");
        }
        String hash = passwordHasher.hash(password);
        UsuarioEntity entity = new UsuarioEntity(null, username, fullName, email, rol, EstadoUsuario.ACTIVO, hash, LocalDateTime.now());
        UsuarioEntity saved = usuarioRepository.save(entity);
        return toDTO(saved);
    }

    public UserSummaryDTO toDTO(UsuarioEntity u) {
        return new UserSummaryDTO(u.getId(), u.getNombreUsuario(), u.getNombreCompleto(), u.getRol().name(), u.getEstado().name(), u.getEmail());
    }
}

