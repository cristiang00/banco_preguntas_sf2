package co.edu.unicauca.bancopreguntas.frontend.views;

import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.client.NotificacionItemDTO;
import co.edu.unicauca.bancopreguntas.frontend.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NotificacionesPanel extends JPanel {

    private final JTable tablaNotificaciones;
    private final DefaultTableModel modelNotificaciones;

    public NotificacionesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblTitulo = new JLabel("Bandeja de Notificaciones y Correos Electrónicos (Email Adapter)", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARY);
        add(lblTitulo, BorderLayout.NORTH);

        modelNotificaciones = new DefaultTableModel(new Object[]{
                "ID", "Destinatario", "Asunto", "Tipo", "Estado", "Intentos", "Fecha Envío"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaNotificaciones = new JTable(modelNotificaciones);
        tablaNotificaciones.setRowHeight(30);
        tablaNotificaciones.setFont(UIUtils.FONT_REGULAR);
        add(new JScrollPane(tablaNotificaciones), BorderLayout.CENTER);

        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        botPanel.setOpaque(false);

        JButton btnRefrescar = new JButton("Actualizar Bandeja");
        UIUtils.stylizePrimaryButton(btnRefrescar);
        btnRefrescar.addActionListener(e -> cargarDatos());
        botPanel.add(btnRefrescar);

        add(botPanel, BorderLayout.SOUTH);
    }

    public void cargarDatos() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<NotificacionItemDTO> notificaciones = ApiClient.getInstance().listarNotificaciones(null);
                modelNotificaciones.setRowCount(0);
                for (NotificacionItemDTO n : notificaciones) {
                    modelNotificaciones.addRow(new Object[]{
                            n.getId(),
                            n.getDestinatario(),
                            n.getAsunto(),
                            n.getTipo(),
                            n.getEstado(),
                            n.getIntentos(),
                            n.getFechaEnvio() != null ? n.getFechaEnvio() : n.getFechaCreacion()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cargando notificaciones: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
