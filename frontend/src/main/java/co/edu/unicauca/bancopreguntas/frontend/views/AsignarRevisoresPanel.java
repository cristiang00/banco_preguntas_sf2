package co.edu.unicauca.bancopreguntas.frontend.views;

import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AsignarRevisoresPanel extends JPanel {

    private final JTable tablaPreguntas;
    private final DefaultTableModel modelPreguntas;
    private final List<PreguntaDTO> preguntasPendientes = new ArrayList<>();

    private final JList<UserSummaryDTO> listaRevisores;
    private final DefaultListModel<UserSummaryDTO> modelRevisores;

    public AsignarRevisoresPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblTitulo = new JLabel("Asignación de Revisores a Preguntas (Rol Administrador)", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARY);
        add(lblTitulo, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);

        // Panel Izquierdo: Preguntas en estado PENDIENTE DE REVISIÓN
        JPanel panelPreguntas = new JPanel(new BorderLayout(5, 5));
        panelPreguntas.setBackground(UIUtils.COLOR_SURFACE);
        panelPreguntas.setBorder(BorderFactory.createTitledBorder("Preguntas en Estado: PENDIENTE DE REVISIÓN"));

        modelPreguntas = new DefaultTableModel(new Object[]{"ID", "Contexto / Enunciado", "Autor", "Dificultad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaPreguntas = new JTable(modelPreguntas);
        tablaPreguntas.setRowHeight(30);
        tablaPreguntas.setFont(UIUtils.FONT_REGULAR);
        panelPreguntas.add(new JScrollPane(tablaPreguntas), BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Actualizar Preguntas");
        UIUtils.stylizeSecondaryButton(btnRefrescar);
        btnRefrescar.addActionListener(e -> cargarDatos());
        panelPreguntas.add(btnRefrescar, BorderLayout.SOUTH);

        splitPane.setLeftComponent(panelPreguntas);

        // Panel Derecho: Lista de Revisores Elegibles
        JPanel panelRevisores = new JPanel(new BorderLayout(5, 5));
        panelRevisores.setBackground(UIUtils.COLOR_SURFACE);
        panelRevisores.setBorder(BorderFactory.createTitledBorder("Revisores Elegibles (Seleccione 1 a 3)"));

        modelRevisores = new DefaultListModel<>();
        listaRevisores = new JList<>(modelRevisores);
        listaRevisores.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaRevisores.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof UserSummaryDTO u) {
                    setText(u.getNombreCompleto() + " (" + u.getNombreUsuario() + ") - " + u.getEmail());
                }
                return this;
            }
        });
        panelRevisores.add(new JScrollPane(listaRevisores), BorderLayout.CENTER);

        JButton btnAsignar = new JButton("Asignar Revisor(es) Seleccionados");
        UIUtils.stylizePrimaryButton(btnAsignar);
        btnAsignar.addActionListener(e -> ejecutarAsignacion());
        panelRevisores.add(btnAsignar, BorderLayout.SOUTH);

        splitPane.setRightComponent(panelRevisores);
        add(splitPane, BorderLayout.CENTER);
    }

    public void cargarDatos() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Cargar preguntas pendientes
                List<PreguntaDTO> preguntas = ApiClient.getInstance().listarPreguntasPendientesRevision();
                preguntasPendientes.clear();
                preguntasPendientes.addAll(preguntas);

                modelPreguntas.setRowCount(0);
                for (PreguntaDTO p : preguntas) {
                    String ctx = p.getContexto() != null && p.getContexto().length() > 50
                            ? p.getContexto().substring(0, 50) + "..."
                            : p.getContexto();
                    modelPreguntas.addRow(new Object[]{
                            p.getId(), ctx, p.getAutorNombre(), p.getNivelDificultad()
                    });
                }

                // Cargar revisores elegibles
                List<UserSummaryDTO> revisores = ApiClient.getInstance().listarRevisoresElegibles();
                modelRevisores.clear();
                for (UserSummaryDTO r : revisores) {
                    modelRevisores.addElement(r);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cargando datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void ejecutarAsignacion() {
        int row = tablaPreguntas.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una pregunta de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<UserSummaryDTO> seleccionados = listaRevisores.getSelectedValuesList();
        if (seleccionados.isEmpty()) {
            listaRevisores.setBorder(UIUtils.BORDER_ERROR);
            JOptionPane.showMessageDialog(this, "Debe seleccionar al menos un revisor de la lista.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (seleccionados.size() > 3) {
            listaRevisores.setBorder(UIUtils.BORDER_ERROR);
            JOptionPane.showMessageDialog(this, "Puede asignar un máximo de 3 revisores por pregunta.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        listaRevisores.setBorder(UIUtils.BORDER_DEFAULT);
        PreguntaDTO p = preguntasPendientes.get(row);

        // Validar no asignación al autor de la pregunta
        for (UserSummaryDTO u : seleccionados) {
            if (u.getId().equals(p.getAutorId())) {
                JOptionPane.showMessageDialog(this, "Conflicto de interés: El revisor '" + u.getNombreCompleto() + "' es el autor de la pregunta.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Confirma la asignación de " + seleccionados.size() + " revisor(es) para la pregunta #" + p.getId() + "?",
                "Confirmar Asignación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            List<Long> ids = seleccionados.stream().map(UserSummaryDTO::getId).toList();
            try {
                ApiClient.getInstance().asignarRevisores(p.getId(), ids);
                JOptionPane.showMessageDialog(this, "Revisores asignados exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error asignando revisores: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
