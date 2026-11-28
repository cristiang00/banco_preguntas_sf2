package co.edu.unicauca.bancopreguntas.users.service;

import co.edu.unicauca.bancopreguntas.users.domain.EstadoUsuario;
import co.edu.unicauca.bancopreguntas.users.domain.Rol;
import co.edu.unicauca.bancopreguntas.users.domain.UsuarioEntity;
import co.edu.unicauca.bancopreguntas.users.repository.UsuarioRepository;
import co.edu.unicauca.bancopreguntas.users.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserDataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public UserDataInitializer(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("Base de datos de usuarios ya contiene registros ({} usuarios)", usuarioRepository.count());
            return;
        }

        log.info("Inicializando usuarios por defecto...");
        crearUsuariosPorDefecto();
        log.info("Carga inicial de usuarios finalizada con éxito. Total usuarios: {}", usuarioRepository.count());
    }

    private void crearUsuariosPorDefecto() {
        String defaultHash = passwordHasher.hash("Password123!@");

        // Orden alineado con el mapeo de IDs de Keycloak en KeycloakJwtExtractor / ApiClient:
        // 1=admin, 2=autor1, 3=juan12 (preguntas iniciales), 4=revisor1, 5=revisor2
        crearSiNoExiste("admin", "Administrador Principal", "admin@unicauca.edu.co", Rol.ADMINISTRADOR, defaultHash);
        crearSiNoExiste("autor1", "Autor Prueba", "autor1@unicauca.edu.co", Rol.AUTOR, defaultHash);
        crearSiNoExiste("juan12", "Juan Pérez (Autor)", "juan12@unicauca.edu.co", Rol.AUTOR, defaultHash);
        crearSiNoExiste("revisor1", "Revisor Uno", "revisor1@unicauca.edu.co", Rol.REVISOR, defaultHash);
        crearSiNoExiste("revisor2", "Revisor Dos", "revisor2@unicauca.edu.co", Rol.REVISOR, defaultHash);
        crearSiNoExiste("Juan1", "Juan Pérez", "juan1@unicauca.edu.co", Rol.ADMINISTRADOR, defaultHash);
        crearSiNoExiste("juan2", "Juanito Gómez (Autor)", "juan2@unicauca.edu.co", Rol.AUTOR, defaultHash);
        crearSiNoExiste("autor", "Autor de Preguntas", "autor@unicauca.edu.co", Rol.AUTOR, defaultHash);
        crearSiNoExiste("Juanita", "Juana Pérez (Revisora)", "juanita@unicauca.edu.co", Rol.REVISOR, defaultHash);
        crearSiNoExiste("Juan6", "Juan García (Revisor)", "juan6@unicauca.edu.co", Rol.REVISOR, defaultHash);
        crearSiNoExiste("Juan13", "Tercer Revisor", "juan13@unicauca.edu.co", Rol.REVISOR, defaultHash);
    }

    private void crearSiNoExiste(String username, String fullName, String email, Rol rol, String hash) {
        if (usuarioRepository.findByNombreUsuarioIgnoreCase(username).isEmpty()) {
            UsuarioEntity u = new UsuarioEntity(null, username, fullName, email, rol, EstadoUsuario.ACTIVO, hash, LocalDateTime.now());
            usuarioRepository.save(u);
        }
    }
}
