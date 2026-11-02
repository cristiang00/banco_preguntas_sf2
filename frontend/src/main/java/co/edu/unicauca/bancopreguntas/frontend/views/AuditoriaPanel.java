package co.edu.unicauca.bancopreguntas.frontend.views;

import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.client.AuditoriaItemDTO;
import co.edu.unicauca.bancopreguntas.frontend.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AuditoriaPanel extends JPanel {

    private final JTable tablaAuditoria;
    private final DefaultTableModel modelAuditoria;
    private final JComboBox<String> cmbTipoEvento;

    public AuditoriaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("Bitácora de Auditoría y Trazabilidad Orientada a Eventos", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARY);
        topPanel.add(lblTitulo, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);

        cmbTipoEvento = new JComboBox<>(new String[]{
                "Todos", "PreguntaEnviadaARevision", "RevisoresAsignados", "PreguntaAprobada", "PreguntaRechazada"
        });
        UIUtils.stylizeTextField(cmbTipoEvento);

        JButton btnFiltrar = new JButton("Filtrar Eventos");
        UIUtils.stylizePrimaryButton(btnFiltrar);
        btnFiltrar.addActionListener(e -> cargarDatos());

        filterPanel.add(new JLabel("Tipo de Evento:"));
        filterPanel.add(cmbTipoEvento);
        filterPanel.add(btnFiltrar);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        modelAuditoria = new DefaultTableModel(new Object[]{
                "ID", "Tipo Evento", "ID Pregunta", "Acción", "Usuario", "Fecha Evento", "Detalles"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaAuditoria = new JTable(modelAuditoria);
        tablaAuditoria.setRowHeight(28);
        tablaAuditoria.setFont(UIUtils.FONT_REGULAR);
        add(new JScrollPane(tablaAuditoria), BorderLayout.CENTER);

        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        botPanel.setOpaque(false);
        JButton btnRefrescar = new JButton("Refrescar");
        UIUtils.stylizeSecondaryButton(btnRefrescar);
        btnRefrescar.addActionListener(e -> cargarDatos());
        botPanel.add(btnRefrescar);
        add(botPanel, BorderLayout.SOUTH);
    }

    public void cargarDatos() {
        String sel = (String) cmbTipoEvento.getSelectedItem();
        String filtro = "Todos".equalsIgnoreCase(sel) ? null : sel;

        SwingUtilities.invokeLater(() -> {
            try {
                List<AuditoriaItemDTO> eventos = ApiClient.getInstance().listarAuditoria(filtro);
                modelAuditoria.setRowCount(0);
                for (AuditoriaItemDTO a : eventos) {
                    modelAuditoria.addRow(new Object[]{
                            a.getId(),
                            a.getEventType(),
                            a.getAggregateId(),
                            a.getAccion(),
                            a.getUsuarioNombre() != null ? a.getUsuarioNombre() : a.getUsuarioId(),
                            a.getFechaEvento(),
                            a.getDetalles()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cargando auditoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
