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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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

        log.info("Inicializando migración y seeding de usuarios...");
        boolean migradoDesdeSQLite = intentarMigrarDesdeSQLite();

        if (!migradoDesdeSQLite || usuarioRepository.count() == 0) {
            crearUsuariosPorDefecto();
        }

        log.info("Carga inicial de usuarios finalizada con éxito. Total usuarios: {}", usuarioRepository.count());
    }

    private boolean intentarMigrarDesdeSQLite() {
        File[] posiblesRutas = new File[] {
            new File("saberpro.db"),
            new File("proyecto_so2_saberpro/saberpro.db"),
            new File("../proyecto_so2_saberpro/saberpro.db"),
            new File("../../proyecto_so2_saberpro/saberpro.db")
        };

        for (File dbFile : posiblesRutas) {
            if (dbFile.exists()) {
                try {
                    Class.forName("org.sqlite.JDBC");
                    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
                         Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT id, nombre_usuario, nombre_completo, rol, estado, password_hash FROM usuarios")) {

                        int count = 0;
                        while (rs.next()) {
                            Long id = rs.getLong("id");
                            String username = rs.getString("nombre_usuario");
                            String fullName = rs.getString("nombre_completo");
                            String rolStr = rs.getString("rol");
                            String estadoStr = rs.getString("estado");
                            String passHash = rs.getString("password_hash");

                            Rol rol = parseRol(rolStr);
                            EstadoUsuario estado = "ACTIVO".equalsIgnoreCase(estadoStr) ? EstadoUsuario.ACTIVO : EstadoUsuario.INACTIVO;
                            String email = username.toLowerCase() + "@unicauca.edu.co";

                            UsuarioEntity u = new UsuarioEntity(id, username, fullName, email, rol, estado, passHash, LocalDateTime.now());
                            usuarioRepository.save(u);
                            count++;
                        }
                        log.info("Migrados exitosamente {} usuarios desde SQLite ({})", count, dbFile.getPath());
                        return count > 0;
                    }
                } catch (Throwable t) {
                    log.warn("No se pudo migrar directamente desde SQLite {}: {}", dbFile.getPath(), t.getMessage());
                }
            }
        }
        return false;
    }

    private void crearUsuariosPorDefecto() {
        String defaultHash = passwordHasher.hash("Password123!@");

        crearSiNoExiste("Juan1", "Juan Pérez", "admin@unicauca.edu.co", Rol.ADMINISTRADOR, defaultHash);
        crearSiNoExiste("admin", "Administrador Principal", "admin.principal@unicauca.edu.co", Rol.ADMINISTRADOR, defaultHash);
        crearSiNoExiste("juan12", "Juan Pérez (Autor)", "juan12@unicauca.edu.co", Rol.AUTOR, defaultHash);
        crearSiNoExiste("juan2", "Juanito Gómez (Autor)", "juan2@unicauca.edu.co", Rol.AUTOR, defaultHash);
        crearSiNoExiste("autor", "Autor de Preguntas", "autor@unicauca.edu.co", Rol.AUTOR, defaultHash);
        crearSiNoExiste("Juanita", "Juana Pérez (Revisora)", "juanita@unicauca.edu.co", Rol.REVISOR, defaultHash);
        crearSiNoExiste("Juan6", "Juan García (Revisor)", "juan6@unicauca.edu.co", Rol.REVISOR, defaultHash);
        crearSiNoExiste("Juan13", "Tercer Revisor", "juan13@unicauca.edu.co", Rol.REVISOR, defaultHash);
        crearSiNoExiste("revisor1", "Revisor Docente 1", "revisor1@unicauca.edu.co", Rol.REVISOR, defaultHash);
    }

    private void crearSiNoExiste(String username, String fullName, String email, Rol rol, String hash) {
        if (usuarioRepository.findByNombreUsuarioIgnoreCase(username).isEmpty()) {
            UsuarioEntity u = new UsuarioEntity(null, username, fullName, email, rol, EstadoUsuario.ACTIVO, hash, LocalDateTime.now());
            usuarioRepository.save(u);
        }
    }

    private Rol parseRol(String rolStr) {
        if (rolStr == null) return Rol.DOCENTE;
        switch (rolStr.toUpperCase()) {
            case "ADMINISTRADOR": return Rol.ADMINISTRADOR;
            case "AUTOR":
            case "AUTOR_PREGUNTAS": return Rol.AUTOR;
            case "REVISOR": return Rol.REVISOR;
            case "ESTUDIANTE": return Rol.ESTUDIANTE;
            default: return Rol.DOCENTE;
        }
    }
}

