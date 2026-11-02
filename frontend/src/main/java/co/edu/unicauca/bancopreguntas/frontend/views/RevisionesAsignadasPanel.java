package co.edu.unicauca.bancopreguntas.frontend.views;

import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.client.RevisionItemDTO;
import co.edu.unicauca.bancopreguntas.frontend.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RevisionesAsignadasPanel extends JPanel {

    private final JTable tablaRevisiones;
    private final DefaultTableModel modelRevisiones;
    private final List<RevisionItemDTO> revisionesActuales = new ArrayList<>();

    public RevisionesAsignadasPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblTitulo = new JLabel("Mis Revisiones Asignadas (Rol Revisor)", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARY);
        add(lblTitulo, BorderLayout.NORTH);

        modelRevisiones = new DefaultTableModel(new Object[]{
                "ID Revisión", "ID Pregunta", "Título / Contexto", "Estado Revisión", "Fecha Asignación"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaRevisiones = new JTable(modelRevisiones);
        tablaRevisiones.setRowHeight(32);
        tablaRevisiones.setFont(UIUtils.FONT_REGULAR);
        tablaRevisiones.setSelectionBackground(new Color(219, 234, 254));

        add(new JScrollPane(tablaRevisiones), BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panelAcciones.setOpaque(false);

        JButton btnRefrescar = new JButton("Actualizar");
        UIUtils.stylizeSecondaryButton(btnRefrescar);
        btnRefrescar.addActionListener(e -> cargarDatos());

        JButton btnEvaluar = new JButton("Evaluar / Emitir Decisión");
        UIUtils.stylizePrimaryButton(btnEvaluar);
        btnEvaluar.addActionListener(e -> abrirDialogoEvaluacion());

        panelAcciones.add(btnRefrescar);
        panelAcciones.add(btnEvaluar);
        add(panelAcciones, BorderLayout.SOUTH);
    }

    public void cargarDatos() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<RevisionItemDTO> lista = ApiClient.getInstance().listarRevisionesAsignadas();
                revisionesActuales.clear();
                revisionesActuales.addAll(lista);

                modelRevisiones.setRowCount(0);
                for (RevisionItemDTO r : lista) {
                    modelRevisiones.addRow(new Object[]{
                            r.getId(),
                            r.getPreguntaId(),
                            r.getContexto() != null ? r.getContexto() : r.getTituloPregunta(),
                            r.getEstadoRevision(),
                            r.getFechaAsignacion()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cargando revisiones asignadas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void abrirDialogoEvaluacion() {
        int row = tablaRevisiones.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una revisión de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RevisionItemDTO rev = revisionesActuales.get(row);
        if ("FINALIZADA".equalsIgnoreCase(rev.getEstadoRevision())) {
            JOptionPane.showMessageDialog(this, "Esta revisión ya ha sido finalizada con veredicto.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Evaluación de Pregunta #" + rev.getPreguntaId(), true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        pnl.setBackground(UIUtils.COLOR_SURFACE);

        JLabel lblD = new JLabel("Formulario de Decisión y Criterios Evaluativos", SwingConstants.CENTER);
        lblD.setFont(UIUtils.FONT_TITLE);
        lblD.setForeground(UIUtils.COLOR_PRIMARY);
        pnl.add(lblD, BorderLayout.NORTH);

        JPanel pnlCenter = new JPanel(new GridLayout(6, 1, 6, 6));
        pnlCenter.setOpaque(false);

        JCheckBox chkPedagogico = new JCheckBox("Cumple Criterio Pedagógico y Curricular", true);
        JCheckBox chkTecnico = new JCheckBox("Cumple Criterio Técnico y Respuesta Correcta Única", true);
        JCheckBox chkRedaccion = new JCheckBox("Redacción Clara y Sin Ambigüedades", true);

        JLabel lblObs = new JLabel("Observaciones / Retroalimentación (Obligatorio en Rechazo):");
        lblObs.setFont(UIUtils.FONT_SUBTITLE);
        JTextArea txtObs = new JTextArea(4, 30);
        txtObs.setLineWrap(true);
        txtObs.setWrapStyleWord(true);
        UIUtils.stylizeTextField(txtObs);

        pnlCenter.add(chkPedagogico);
        pnlCenter.add(chkTecnico);
        pnlCenter.add(chkRedaccion);
        pnlCenter.add(lblObs);
        pnlCenter.add(new JScrollPane(txtObs));

        pnl.add(pnlCenter, BorderLayout.CENTER);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBtns.setOpaque(false);

        JButton btnAprobar = new JButton("✓ Aprobar Pregunta");
        btnAprobar.setBackground(UIUtils.COLOR_SUCCESS);
        btnAprobar.setForeground(Color.WHITE);
        btnAprobar.setFont(UIUtils.FONT_SUBTITLE);
        btnAprobar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog, "¿Confirma la APROBACIÓN de la pregunta #" + rev.getPreguntaId() + "?", "Confirmar Aprobación", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String obs = txtObs.getText().trim();
                    ApiClient.getInstance().emitirDecision(rev.getId(), "APROBADA", obs.isEmpty() ? "Cumple todos los criterios pedagógicos y técnicos." : obs);
                    JOptionPane.showMessageDialog(dialog, "Pregunta aprobada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    cargarDatos();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error emitiendo decisión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnRechazar = new JButton("✗ Rechazar Pregunta");
        btnRechazar.setBackground(UIUtils.COLOR_ERROR);
        btnRechazar.setForeground(Color.WHITE);
        btnRechazar.setFont(UIUtils.FONT_SUBTITLE);
        btnRechazar.addActionListener(e -> {
            String obs = txtObs.getText().trim();
            if (obs.isEmpty()) {
                txtObs.setBorder(UIUtils.BORDER_ERROR);
                JOptionPane.showMessageDialog(dialog, "Debe ingresar observaciones detalladas que justifiquen el rechazo de la pregunta.", "Validación Requerida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(dialog, "¿Confirma el RECHAZO de la pregunta #" + rev.getPreguntaId() + "?", "Confirmar Rechazo", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    ApiClient.getInstance().emitirDecision(rev.getId(), "RECHAZADA", obs);
                    JOptionPane.showMessageDialog(dialog, "Pregunta rechazada con observaciones registradas.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    cargarDatos();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error emitiendo decisión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnCancelar = new JButton("Cancelar");
        UIUtils.stylizeSecondaryButton(btnCancelar);
        btnCancelar.addActionListener(e -> dialog.dispose());

        pnlBtns.add(btnAprobar);
        pnlBtns.add(btnRechazar);
        pnlBtns.add(btnCancelar);

        pnl.add(pnlBtns, BorderLayout.SOUTH);
        dialog.setContentPane(pnl);
        dialog.setVisible(true);
    }
}
