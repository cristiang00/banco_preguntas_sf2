package co.edu.unicauca.bancopreguntas;

import co.edu.unicauca.bancopreguntas.dataaccess.AsignacionRevisorRepositorySQLite;
import co.edu.unicauca.bancopreguntas.dataaccess.PreguntaRepositorySQLite;
import co.edu.unicauca.bancopreguntas.domain.repositories.AsignacionRevisorRepository;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;
import co.edu.unicauca.bancopreguntas.domain.services.AsignacionRevisorService;
import co.edu.unicauca.bancopreguntas.domain.services.PreguntaService;
import co.edu.unicauca.bancopreguntas.presentation.controllers.AsignacionRevisorController;
import co.edu.unicauca.bancopreguntas.presentation.controllers.PreguntaController;
import com.unicauca.taller2.usuarios.access.*;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;
import com.unicauca.taller2.usuarios.presentation.LoginFrame;
import com.unicauca.taller2.usuarios.presentation.MenuAdministradorFrame;
import com.unicauca.taller2.usuarios.presentation.MenuGenericoFrame;
import com.unicauca.taller2.usuarios.services.AutenticacionService;
import com.unicauca.taller2.usuarios.services.UsuarioService;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Clase principal que inicializa la aplicación Saber Pro.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Configuración global básica de UI
                java.awt.Font globalFont = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
                java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
                while (keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    Object value = UIManager.get(key);
                    if (value instanceof javax.swing.plaf.FontUIResource) {
                        UIManager.put(key, new javax.swing.plaf.FontUIResource(globalFont));
                    }
                }
                
                UIManager.put("Panel.background", new java.awt.Color(248, 250, 252));
                UIManager.put("OptionPane.background", new java.awt.Color(255, 255, 255));
                UIManager.put("OptionPane.messageFont", globalFont);
                
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 1. Inicializar persistencia y base de datos (creará todas las tablas)
            ConexionSQLite conexion = new ConexionSQLite();
            UsuarioRepository usuarioRepository = new UsuarioRepositorySQLite(conexion);
            PreguntaRepository preguntaRepository = new PreguntaRepositorySQLite(conexion);
            AsignacionRevisorRepository asignacionRepository = new AsignacionRevisorRepositorySQLite(conexion);

            // 2. Inicializar servicios de usuarios
            PasswordHasher hasher = new Argon2PasswordHasher();
            PasswordPolicy policy = new PasswordPolicyDefault();
            UsuarioService usuarioService = new UsuarioService(usuarioRepository, hasher, policy);
            AutenticacionService autenticacionService = new AutenticacionService(usuarioRepository, hasher);

            // 3. Inicializar servicios del dominio de preguntas
            PreguntaService preguntaService = new PreguntaService(preguntaRepository);
            AsignacionRevisorService asignacionService = new AsignacionRevisorService(asignacionRepository, preguntaRepository, usuarioRepository);

            // 4. Configurar el callback de Login para crear los Controladores y abrir el Menú correcto
            LoginFrame[] loginFrameWrapper = new LoginFrame[1];
            
            Consumer<Usuario> onLoginSuccess = (Usuario usuario) -> {
                if (usuario.getRol() == Rol.ADMINISTRADOR) {
                    AsignacionRevisorController asignacionController = new AsignacionRevisorController(asignacionService, preguntaRepository, usuarioRepository);
                    new MenuAdministradorFrame(usuario, usuarioService, loginFrameWrapper[0], asignacionController).setVisible(true);
                } else {
                    PreguntaController preguntaController = new PreguntaController(preguntaService, usuario.getId());
                    new MenuGenericoFrame(usuario, loginFrameWrapper[0], preguntaController).setVisible(true);
                }
            };

            // 5. Iniciar la ventana de Login
            LoginFrame loginFrame = new LoginFrame(autenticacionService, usuarioService, onLoginSuccess);
            loginFrameWrapper[0] = loginFrame; // Guardamos la referencia para el cierre de sesión
            loginFrame.setVisible(true);
        });
    }
}
