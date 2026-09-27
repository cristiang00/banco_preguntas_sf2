package co.edu.unicauca.bancopreguntas.presentation.views;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.presentation.controllers.AsignacionRevisorController;
import com.unicauca.taller2.usuarios.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AsignarRevisorPanel extends JPanel {
    private final AsignacionRevisorController controller;
    private JTable tablaPreguntas;
    private DefaultTableModel modelPreguntas;
    private JList<Usuario> listaRevisores;
    private DefaultListModel<Usuario> modelRevisores;

    public AsignarRevisorPanel(AsignacionRevisorController controller) {
        this.controller = controller;
        inicializarUI();
    }

    public void actualizarDatos() {
        cargarPreguntas();
        cargarRevisores();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Asignación de Revisores", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(lblTitulo, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7);

        // Panel izquierdo: Tabla de preguntas pendientes
        JPanel panelPreguntas = new JPanel(new BorderLayout());
        panelPreguntas.setBorder(BorderFactory.createTitledBorder("Preguntas en Estado: PENDIENTE DE REVISIÓN"));
        
        modelPreguntas = new DefaultTableModel(new Object[]{"ID", "Contexto", "Autor", "Objeto"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPreguntas = new JTable(modelPreguntas);
        tablaPreguntas.removeColumn(tablaPreguntas.getColumnModel().getColumn(3)); // Ocultar objeto
        panelPreguntas.add(new JScrollPane(tablaPreguntas), BorderLayout.CENTER);
        splitPane.setLeftComponent(panelPreguntas);

        // Panel derecho: Lista de revisores
        JPanel panelRevisores = new JPanel(new BorderLayout());
        panelRevisores.setBorder(BorderFactory.createTitledBorder("Revisores Disponibles"));
        
        modelRevisores = new DefaultListModel<>();
        listaRevisores = new JList<>(modelRevisores);
        listaRevisores.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // Renderer para mostrar nombre completo del revisor
        listaRevisores.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Usuario) {
                    setText(((Usuario) value).getNombreCompleto() + " (" + ((Usuario) value).getNombreUsuario() + ")");
                }
                return this;
            }
        });
        panelRevisores.add(new JScrollPane(listaRevisores), BorderLayout.CENTER);

        JButton btnAsignar = new JButton("Asignar Revisor(es)");
        btnAsignar.addActionListener(e -> asignarRevisores());
        panelRevisores.add(btnAsignar, BorderLayout.SOUTH);

        splitPane.setRightComponent(panelRevisores);

        add(splitPane, BorderLayout.CENTER);
    }

    private void cargarPreguntas() {
        List<Pregunta> preguntas = controller.listarPreguntasPendientes();
        modelPreguntas.setRowCount(0);
        for (Pregunta p : preguntas) {
            String contexto = p.getContexto().length() > 30 ? p.getContexto().substring(0, 30) + "..." : p.getContexto();
            modelPreguntas.addRow(new Object[]{p.getId(), contexto, p.getAutorNombre(), p});
        }
    }

    private void cargarRevisores() {
        List<Usuario> revisores = controller.listarRevisores();
        modelRevisores.clear();
        for (Usuario u : revisores) {
            modelRevisores.addElement(u);
        }
    }

    private void asignarRevisores() {
        int row = tablaPreguntas.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una pregunta de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Usuario> seleccionados = listaRevisores.getSelectedValuesList();
        if (seleccionados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar al menos un revisor.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Pregunta p = (Pregunta) modelPreguntas.getValueAt(row, 3);
        List<Integer> revisorIds = new ArrayList<>();
        for (Usuario u : seleccionados) {
            revisorIds.add(u.getId());
        }

        try {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de asignar los revisores seleccionados a la pregunta " + p.getId() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.asignarRevisores(p.getId(), revisorIds);
                JOptionPane.showMessageDialog(this, "Revisores asignados correctamente. Se ha notificado por correo (simulación).", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                // Si la pregunta cambia de estado, se debería recargar
                // En este caso, el backlog no menciona que cambie de estado inmediatamente, pero si lo hiciera:
                // cargarPreguntas();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
