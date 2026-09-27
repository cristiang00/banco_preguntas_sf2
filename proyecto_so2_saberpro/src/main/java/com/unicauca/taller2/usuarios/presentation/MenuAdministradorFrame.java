package com.unicauca.taller2.usuarios.presentation;

import com.unicauca.taller2.usuarios.services.UsuarioService;
import com.unicauca.taller2.usuarios.model.Usuario;

import javax.swing.*;
import java.awt.*;
import co.edu.unicauca.bancopreguntas.presentation.utils.UIUtils;

/**
 * Menú principal para usuarios con rol ADMINISTRADOR.
 */
public class MenuAdministradorFrame extends JFrame {

    private final Usuario usuario;
    private final UsuarioService usuarioService;
    private final LoginFrame loginFrame;
    private final co.edu.unicauca.bancopreguntas.presentation.controllers.AsignacionRevisorController asignacionController;

    /**
     * Constructor del menú de administrador.
     *
     * @param usuario        usuario administrador autenticado
     * @param usuarioService servicio de usuarios
     * @param loginFrame     ventana de inicio de sesión
     */
    public MenuAdministradorFrame(Usuario usuario, UsuarioService usuarioService, LoginFrame loginFrame, co.edu.unicauca.bancopreguntas.presentation.controllers.AsignacionRevisorController asignacionController) {
        this.usuario = usuario;
        this.usuarioService = usuarioService;
        this.loginFrame = loginFrame;
        this.asignacionController = asignacionController;
        inicializarUI();
    }

    private void inicializarUI() {
        setTitle("Saber Pro - Panel de Administración");
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

        JButton btnGestionUsuarios = crearBotonMenu("👥  Gestión de Usuarios", true);
        btnGestionUsuarios.addActionListener(e -> abrirGestionUsuarios());
        opcionesPanel.add(btnGestionUsuarios);

        JButton btnBancoPreguntas = crearBotonMenu("📝  Gestión de Preguntas", asignacionController != null);
        if (asignacionController != null) {
            btnBancoPreguntas.addActionListener(e -> {
                try {
                    JDialog dialog = new JDialog(MenuAdministradorFrame.this, "Gestión de Preguntas", true);
                    co.edu.unicauca.bancopreguntas.presentation.views.AsignarRevisorPanel panel = new co.edu.unicauca.bancopreguntas.presentation.views.AsignarRevisorPanel(asignacionController);
                    panel.actualizarDatos();
                    dialog.setContentPane(panel);
                    dialog.pack();
                    dialog.setMinimumSize(new Dimension(800, 500));
                    dialog.setLocationRelativeTo(MenuAdministradorFrame.this);
                    dialog.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MenuAdministradorFrame.this, "Error al abrir Gestión de Preguntas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        opcionesPanel.add(btnBancoPreguntas);

        JButton btnReportes = crearBotonMenu("📊  Reportes y Estadísticas (próximamente)", false);
        opcionesPanel.add(btnReportes);

        JButton btnConfiguracion = crearBotonMenu("⚙️  Configuración del Sistema (próximamente)", false);
        opcionesPanel.add(btnConfiguracion);

        mainPanel.add(opcionesPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        pack();
        setMinimumSize(new Dimension(550, 400));
        setLocationRelativeTo(null);
    }

    private void abrirGestionUsuarios() {
        JDialog dialog = new JDialog(this, "Gestión de Usuarios", true);
        dialog.setContentPane(new GestionUsuariosPanel(usuarioService));
        dialog.pack();
        dialog.setMinimumSize(new Dimension(700, 450));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void cerrarSesion() {
        dispose();
        loginFrame.mostrarDeNuevo();
    }

    private JButton crearBotonMenu(String texto, boolean habilitado) {
        JButton btn = new JButton(texto);
        UIUtils.stylizeSidebarButton(btn);

        if (!habilitado) {
            btn.setEnabled(false);
            btn.setBackground(new Color(241, 245, 249)); // Gris claro disabled
            btn.setForeground(new Color(148, 163, 184)); // Texto gris
            btn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        return btn;
    }
}
