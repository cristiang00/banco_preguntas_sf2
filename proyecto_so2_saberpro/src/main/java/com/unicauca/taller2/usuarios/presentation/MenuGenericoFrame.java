package com.unicauca.taller2.usuarios.presentation;

import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import co.edu.unicauca.bancopreguntas.presentation.utils.UIUtils;

/**
 * Menú genérico reutilizable para roles distintos al Administrador.
 */
public class MenuGenericoFrame extends JFrame {

    private static final Map<Rol, List<String>> MENU_OPTIONS = new LinkedHashMap<>();

    static {
        MENU_OPTIONS.put(Rol.AUTOR_PREGUNTAS, Arrays.asList(
                "📝  Crear Pregunta",
                "📋  Mis Preguntas",
                "📊  Estado de Revisión de Preguntas"
        ));

        MENU_OPTIONS.put(Rol.REVISOR, Arrays.asList(
                "🔍  Revisar Preguntas Pendientes",
                "✅  Preguntas Aprobadas",
                "❌  Preguntas Rechazadas",
                "📊  Estadísticas de Revisión"
        ));

        MENU_OPTIONS.put(Rol.DOCENTE, Arrays.asList(
                "📝  Crear Simulacro",
                "📋  Mis Simulacros",
                "📊  Resultados de Estudiantes",
                "🔍  Consultar Banco de Preguntas"
        ));

        MENU_OPTIONS.put(Rol.ESTUDIANTE, Arrays.asList(
                "🎯  Realizar Simulacro",
                "📊  Mis Resultados",
                "📈  Historial de Simulacros",
                "📚  Material de Estudio"
        ));
    }

    private final Usuario usuario;
    private final LoginFrame loginFrame;
    private final co.edu.unicauca.bancopreguntas.presentation.controllers.PreguntaController preguntaController;

    /**
     * Constructor del menú genérico.
     *
     * @param usuario usuario autenticado
     * @param loginFrame ventana de inicio de sesión
     */
    public MenuGenericoFrame(Usuario usuario, LoginFrame loginFrame, co.edu.unicauca.bancopreguntas.presentation.controllers.PreguntaController preguntaController) {
        this.usuario = usuario;
        this.loginFrame = loginFrame;
        this.preguntaController = preguntaController;
        inicializarUI();
    }

    private void inicializarUI() {
        setTitle("Saber Pro - " + usuario.getRol().getDisplayName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cerrarSesion();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(new Color(245, 245, 250));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblBienvenida = new JLabel("Bienvenido, " + usuario.getNombreCompleto());
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBienvenida.setForeground(new Color(33, 37, 41));

        JLabel lblRol = new JLabel("Rol: " + usuario.getRol().getDisplayName());
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRol.setForeground(new Color(108, 117, 125));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        infoPanel.add(lblBienvenida);
        infoPanel.add(lblRol);

        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        UIUtils.stylizeSecondaryButton(btnCerrarSesion);
        btnCerrarSesion.setPreferredSize(new Dimension(130, 34));
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        headerPanel.add(infoPanel, BorderLayout.WEST);
        headerPanel.add(btnCerrarSesion, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel opcionesPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        opcionesPanel.setOpaque(false);
        opcionesPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        List<String> opciones = MENU_OPTIONS.getOrDefault(usuario.getRol(), Collections.emptyList());

        // Si es autor de preguntas y tenemos el controlador, asignamos las acciones reales
        if (usuario.getRol() == Rol.AUTOR_PREGUNTAS && preguntaController != null) {
            for (String opcion : opciones) {
                JButton btn = new JButton(opcion);
                UIUtils.stylizeSidebarButton(btn);

                if (opcion.equals("📝  Crear Pregunta")) {
                    btn.addActionListener(e -> {
                        JDialog dialog = new JDialog(MenuGenericoFrame.this, "Crear Pregunta", true);
                        co.edu.unicauca.bancopreguntas.presentation.views.CrearPreguntaPanel panel = new co.edu.unicauca.bancopreguntas.presentation.views.CrearPreguntaPanel(preguntaController);
                        dialog.setContentPane(panel);
                        dialog.pack();
                        dialog.setLocationRelativeTo(MenuGenericoFrame.this);
                        dialog.setVisible(true);
                    });
                    opcionesPanel.add(btn);
                } else if (opcion.equals("📋  Mis Preguntas")) {
                    btn.addActionListener(e -> {
                        JDialog dialog = new JDialog(MenuGenericoFrame.this, "Mis Preguntas", true);
                        co.edu.unicauca.bancopreguntas.presentation.views.ListarPreguntasPanel panel = new co.edu.unicauca.bancopreguntas.presentation.views.ListarPreguntasPanel(preguntaController);
                        panel.actualizarDatos();
                        dialog.setContentPane(panel);
                        dialog.pack();
                        dialog.setMinimumSize(new Dimension(800, 500));
                        dialog.setLocationRelativeTo(MenuGenericoFrame.this);
                        dialog.setVisible(true);
                    });
                    opcionesPanel.add(btn);
                } else {
                    opcionesPanel.add(crearBotonMenuPlaceholder(opcion + " (próximamente)"));
                }
            }
        } else {
            for (String opcion : opciones) {
                JButton btn = crearBotonMenuPlaceholder(opcion + " (próximamente)");
                opcionesPanel.add(btn);
            }
        }

        if (opciones.isEmpty()) {
            JLabel lblSinOpciones = new JLabel("No hay opciones disponibles para su rol.", SwingConstants.CENTER);
            lblSinOpciones.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblSinOpciones.setForeground(new Color(108, 117, 125));
            opcionesPanel.add(lblSinOpciones);
        }

        mainPanel.add(opcionesPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        pack();
        setMinimumSize(new Dimension(500, 380));
        setLocationRelativeTo(null);
    }

    private void cerrarSesion() {
        dispose();
        loginFrame.mostrarDeNuevo();
    }

    private JButton crearBotonMenuPlaceholder(String texto) {
        JButton btn = new JButton(texto);
        UIUtils.stylizeSidebarButton(btn);
        btn.setBackground(new Color(241, 245, 249)); // Gris claro disabled
        btn.setForeground(new Color(148, 163, 184)); // Texto gris
        btn.setEnabled(false);
        btn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return btn;
    }
}
