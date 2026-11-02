package co.edu.unicauca.bancopreguntas.frontend.views;

import co.edu.unicauca.bancopreguntas.contracts.dto.TokenResponseDTO;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

public class MainDashboardFrame extends JFrame {

    private final TokenResponseDTO usuario;
    private final JTabbedPane tabbedPane;

    public MainDashboardFrame(TokenResponseDTO usuario) {
        super("Banco de Preguntas Saber Pro - Sistema Distribuido 2.0");
        this.usuario = usuario;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // Cabecera superior con información del usuario y logout
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(UIUtils.COLOR_SURFACE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        userPanel.setOpaque(false);

        JLabel lblLogo = new JLabel("🎓 Saber Pro");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel lblUserInfo = new JLabel("Usuario: " + usuario.getNombreCompleto() + " (" + usuario.getNombreUsuario() + ")");
        lblUserInfo.setFont(UIUtils.FONT_REGULAR);
        lblUserInfo.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel lblRoleBadge = new JLabel(" [" + usuario.getRol() + "] ");
        lblRoleBadge.setFont(UIUtils.FONT_SUBTITLE);
        lblRoleBadge.setOpaque(true);
        lblRoleBadge.setBackground(new Color(224, 231, 255));
        lblRoleBadge.setForeground(new Color(67, 56, 202));
        lblRoleBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        userPanel.add(lblLogo);
        userPanel.add(Box.createHorizontalStrut(15));
        userPanel.add(lblUserInfo);
        userPanel.add(lblRoleBadge);
        headerPanel.add(userPanel, BorderLayout.WEST);

        JButton btnLogout = new JButton("Cerrar Sesión");
        UIUtils.stylizeSecondaryButton(btnLogout);
        btnLogout.addActionListener(e -> {
            ApiClient.getInstance().logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
        headerPanel.add(btnLogout, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Pestañas dinámicas según el Rol
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_SUBTITLE);

        configurarPestanasPorRol();

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private void configurarPestanasPorRol() {
        String role = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";

        ListarPreguntasPanel listarPreguntasPanel = new ListarPreguntasPanel();
        NotificacionesPanel notificacionesPanel = new NotificacionesPanel();

        if ("AUTOR".equals(role)) {
            CrearPreguntaPanel crearPreguntaPanel = new CrearPreguntaPanel();
            tabbedPane.addTab("📋 Mis Preguntas", listarPreguntasPanel);
            tabbedPane.addTab("➕ Crear Pregunta", crearPreguntaPanel);
            tabbedPane.addTab("🔔 Notificaciones", notificacionesPanel);
            listarPreguntasPanel.cargarDatos();

        } else if ("ADMINISTRADOR".equals(role)) {
            AsignarRevisoresPanel asignarRevisoresPanel = new AsignarRevisoresPanel();
            AuditoriaPanel auditoriaPanel = new AuditoriaPanel();
            tabbedPane.addTab("👥 Asignar Revisores", asignarRevisoresPanel);
            tabbedPane.addTab("📋 Banco de Preguntas", listarPreguntasPanel);
            tabbedPane.addTab("📜 Bitácora de Auditoría", auditoriaPanel);
            tabbedPane.addTab("🔔 Notificaciones", notificacionesPanel);
            asignarRevisoresPanel.cargarDatos();

        } else if ("REVISOR".equals(role)) {
            RevisionesAsignadasPanel revisionesPanel = new RevisionesAsignadasPanel();
            tabbedPane.addTab("🔍 Mis Revisiones Asignadas", revisionesPanel);
            tabbedPane.addTab("📋 Banco de Preguntas", listarPreguntasPanel);
            tabbedPane.addTab("🔔 Notificaciones", notificacionesPanel);
            revisionesPanel.cargarDatos();

        } else {
            tabbedPane.addTab("📋 Preguntas", listarPreguntasPanel);
            tabbedPane.addTab("🔔 Notificaciones", notificacionesPanel);
            listarPreguntasPanel.cargarDatos();
        }

        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected instanceof ListarPreguntasPanel lp) {
                lp.cargarDatos();
            } else if (selected instanceof AsignarRevisoresPanel ap) {
                ap.cargarDatos();
            } else if (selected instanceof RevisionesAsignadasPanel rp) {
                rp.cargarDatos();
            } else if (selected instanceof AuditoriaPanel aup) {
                aup.cargarDatos();
            } else if (selected instanceof NotificacionesPanel np) {
                np.cargarDatos();
            }
        });
    }
}
