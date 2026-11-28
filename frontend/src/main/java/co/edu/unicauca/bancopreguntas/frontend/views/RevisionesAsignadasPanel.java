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

    private final JComboBox<String> cmbEstado;
    private final JComboBox<String> cmbDificultad;
    private final JTextField txtCompetencia;
    private final JLabel lblPagina;
    private final JButton btnAnterior;
    private final JButton btnSiguiente;
    private int paginaActual = 0;
    private final int tamanoPagina = 10;
    private int totalPaginas = 1;

    public RevisionesAsignadasPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Cabecera y Filtros (HU12)
        JPanel pnlNorte = new JPanel(new BorderLayout(5, 5));
        pnlNorte.setOpaque(false);

        JLabel lblTitulo = new JLabel("Mis Revisiones Asignadas (Rol Revisor)", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARY);
        pnlNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlFiltros.setBackground(UIUtils.COLOR_SURFACE);
        pnlFiltros.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        cmbEstado = new JComboBox<>(
                new String[] { "Todos", "ASIGNADA", "EVALUADA", "APROBADA", "RECHAZADA", "CERRADA_POR_RECHAZO" });
        UIUtils.stylizeTextField(cmbEstado);

        cmbDificultad = new JComboBox<>(new String[] { "Todos", "Bajo", "Medio", "Alto" });
        UIUtils.stylizeTextField(cmbDificultad);

        txtCompetencia = new JTextField(12);
        UIUtils.stylizeTextField(txtCompetencia);

        JButton btnFiltrar = new JButton("Filtrar");
        UIUtils.stylizePrimaryButton(btnFiltrar);
        btnFiltrar.addActionListener(e -> {
            paginaActual = 0;
            cargarDatos();
        });

        JButton btnLimpiar = new JButton("Limpiar");
        UIUtils.stylizeSecondaryButton(btnLimpiar);
        btnLimpiar.addActionListener(e -> {
            cmbEstado.setSelectedIndex(0);
            cmbDificultad.setSelectedIndex(0);
            txtCompetencia.setText("");
            paginaActual = 0;
            cargarDatos();
        });

        pnlFiltros.add(new JLabel("Estado:"));
        pnlFiltros.add(cmbEstado);
        pnlFiltros.add(new JLabel("Dificultad:"));
        pnlFiltros.add(cmbDificultad);
        pnlFiltros.add(new JLabel("Competencia:"));
        pnlFiltros.add(txtCompetencia);
        pnlFiltros.add(btnFiltrar);
        pnlFiltros.add(btnLimpiar);

        pnlNorte.add(pnlFiltros, BorderLayout.SOUTH);
        add(pnlNorte, BorderLayout.NORTH);

        modelRevisiones = new DefaultTableModel(new Object[] {
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

        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        panelInferior.setOpaque(false);

        // Controles de Paginación
        JPanel pnlPaginacion = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlPaginacion.setOpaque(false);

        btnAnterior = new JButton("< Anterior");
        UIUtils.stylizeSecondaryButton(btnAnterior);
        btnAnterior.addActionListener(e -> {
            if (paginaActual > 0) {
                paginaActual--;
                cargarDatos();
            }
        });

        lblPagina = new JLabel("Página 1 de 1");
        lblPagina.setFont(UIUtils.FONT_SUBTITLE);

        btnSiguiente = new JButton("Siguiente >");
        UIUtils.stylizeSecondaryButton(btnSiguiente);
        btnSiguiente.addActionListener(e -> {
            if (paginaActual < totalPaginas - 1) {
                paginaActual++;
                cargarDatos();
            }
        });

        pnlPaginacion.add(btnAnterior);
        pnlPaginacion.add(lblPagina);
        pnlPaginacion.add(btnSiguiente);
        panelInferior.add(pnlPaginacion, BorderLayout.WEST);

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
        panelInferior.add(panelAcciones, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);
    }

    @SuppressWarnings("unchecked")
    public void cargarDatos() {
        SwingUtilities.invokeLater(() -> {
            try {
                String estado = (String) cmbEstado.getSelectedItem();
                String dificultad = (String) cmbDificultad.getSelectedItem();
                String competencia = txtCompetencia.getText().trim();

                java.util.Map<String, Object> pageData = ApiClient.getInstance().listarRevisionesAsignadasPaginado(
                        paginaActual, tamanoPagina, estado, dificultad, competencia);
                List<RevisionItemDTO> lista = (List<RevisionItemDTO>) pageData.getOrDefault("content", java.util.Collections.emptyList());
                Number totalPagesNum = (Number) pageData.getOrDefault("totalPages", 1);
                Number totalElemsNum = (Number) pageData.getOrDefault("totalElements", lista.size());

                revisionesActuales.clear();
                revisionesActuales.addAll(lista);

                totalPaginas = Math.max(1, totalPagesNum.intValue());
                lblPagina.setText("Página " + (paginaActual + 1) + " de " + totalPaginas
                        + " (" + totalElemsNum.longValue() + " total)");
                btnAnterior.setEnabled(paginaActual > 0);
                btnSiguiente.setEnabled(paginaActual < totalPaginas - 1);

                modelRevisiones.setRowCount(0);
                for (RevisionItemDTO r : lista) {
                    modelRevisiones.addRow(new Object[] {
                            r.getId(),
                            r.getPreguntaId(),
                            r.getContexto() != null ? r.getContexto() : r.getTituloPregunta(),
                            r.getEstadoRevision(),
                            r.getFechaAsignacion()
                    });
                }
            } catch (Exception ex) {
                UIUtils.showFriendlyError(this, ex, "cargar tus revisiones asignadas");
            }
        });
    }

    private void abrirDialogoEvaluacion() {
        int row = tablaRevisiones.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una revisión de la tabla.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        RevisionItemDTO rev = revisionesActuales.get(row);
        String estadoActual = rev.getEstadoRevision();
        if ("APROBADA".equalsIgnoreCase(estadoActual)
                || "RECHAZADA".equalsIgnoreCase(estadoActual)
                || "CERRADA_POR_RECHAZO".equalsIgnoreCase(estadoActual)) {
            JOptionPane.showMessageDialog(this,
                    "Esta revisión ya ha sido finalizada con estado: " + estadoActual + ".",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Evaluación de Pregunta #" + rev.getPreguntaId(), true);
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

        JButton btnAprobar = new JButton("Aprobar Pregunta");
        btnAprobar.setBackground(UIUtils.COLOR_SUCCESS);
        btnAprobar.setForeground(Color.WHITE);
        btnAprobar.setFont(UIUtils.FONT_SUBTITLE);
        btnAprobar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "¿Confirma la APROBACIÓN de la pregunta #" + rev.getPreguntaId() + "?", "Confirmar Aprobación",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String obs = txtObs.getText().trim();
                    int cumplidos = (chkPedagogico.isSelected() ? 1 : 0)
                            + (chkTecnico.isSelected() ? 1 : 0)
                            + (chkRedaccion.isSelected() ? 1 : 0);
                    double calif = Math.round((cumplidos / 3.0) * 50.0) / 10.0;
                    String comentarioEval = obs.isEmpty() ? "Cumple todos los criterios pedagógicos y técnicos." : obs;
                    ApiClient.getInstance().registrarEvaluacion(rev.getId(),
                            "Evaluación Integral (Pedagógico, Técnico, Redacción)", calif, comentarioEval,
                            obs.isEmpty() ? java.util.Collections.emptyList() : List.of(obs));
                    ApiClient.getInstance().emitirDecision(rev.getId(), "APROBADA", comentarioEval);
                    JOptionPane.showMessageDialog(dialog, "Pregunta aprobada exitosamente.", "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    cargarDatos();
                } catch (Exception ex) {
                    UIUtils.showFriendlyError(dialog, ex, "registrar la aprobación");
                }
            }
        });

        JButton btnRechazar = new JButton("Rechazar Pregunta");
        btnRechazar.setBackground(UIUtils.COLOR_ERROR);
        btnRechazar.setForeground(Color.WHITE);
        btnRechazar.setFont(UIUtils.FONT_SUBTITLE);
        btnRechazar.addActionListener(e -> {
            String obs = txtObs.getText().trim();
            if (obs.isEmpty()) {
                txtObs.setBorder(UIUtils.BORDER_ERROR);
                JOptionPane.showMessageDialog(dialog,
                        "Debe ingresar observaciones detalladas que justifiquen el rechazo de la pregunta.",
                        "Validación Requerida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "¿Confirma el RECHAZO de la pregunta #" + rev.getPreguntaId() + "?", "Confirmar Rechazo",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int cumplidos = (chkPedagogico.isSelected() ? 1 : 0)
                            + (chkTecnico.isSelected() ? 1 : 0)
                            + (chkRedaccion.isSelected() ? 1 : 0);
                    double calif = Math.round((cumplidos / 3.0) * 50.0) / 10.0;
                    ApiClient.getInstance().registrarEvaluacion(rev.getId(),
                            "Evaluación Integral (Pedagógico, Técnico, Redacción)", calif, obs, List.of());
                    ApiClient.getInstance().emitirDecision(rev.getId(), "RECHAZADA", obs);
                    JOptionPane.showMessageDialog(dialog, "Pregunta rechazada con observaciones registradas.", "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    cargarDatos();
                } catch (Exception ex) {
                    UIUtils.showFriendlyError(dialog, ex, "registrar el rechazo");
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
