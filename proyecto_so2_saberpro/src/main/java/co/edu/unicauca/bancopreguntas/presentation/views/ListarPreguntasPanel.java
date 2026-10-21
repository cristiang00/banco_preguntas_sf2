package co.edu.unicauca.bancopreguntas.presentation.views;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.presentation.controllers.PreguntaController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import co.edu.unicauca.bancopreguntas.presentation.utils.UIUtils;

public class ListarPreguntasPanel extends JPanel {
    private final PreguntaController preguntaController;
    private JTable tabla;
    private DefaultTableModel tableModel;
    private JList<EstadoPregunta> listaEstados;
    private JComboBox<String> cmbDificultad;
    private JButton btnAnterior, btnSiguiente;
    private JLabel lblPagina;

    private int paginaActual = 1;
    private final int TAMANO_PAGINA = 10;
    private int totalPaginas = 1;

    public ListarPreguntasPanel(PreguntaController preguntaController, List<EstadoPregunta> estadosIniciales) {
        this.preguntaController = preguntaController;
        inicializarUI();
        if (estadosIniciales != null && !estadosIniciales.isEmpty()) {
            List<Integer> indices = new ArrayList<>();
            EstadoPregunta[] all = EstadoPregunta.values();
            for (int i = 0; i < all.length; i++) {
                if (estadosIniciales.contains(all[i])) {
                    indices.add(i);
                }
            }
            int[] indicesArr = indices.stream().mapToInt(Integer::intValue).toArray();
            listaEstados.setSelectedIndices(indicesArr);
        }
    }

    public ListarPreguntasPanel(PreguntaController preguntaController) {
        this(preguntaController, null);
    }

    public void actualizarDatos() {
        paginaActual = 1;
        cargarDatos();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior: Filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));

        listaEstados = new JList<>(EstadoPregunta.values());
        listaEstados.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaEstados.setVisibleRowCount(3);
        JScrollPane scrollEstados = new JScrollPane(listaEstados);
        panelFiltros.add(new JLabel("Estados:"));
        panelFiltros.add(scrollEstados);

        cmbDificultad = new JComboBox<>(new String[] { "Todos", "Bajo", "Medio", "Alto" });
        panelFiltros.add(new JLabel("Dificultad:"));
        panelFiltros.add(cmbDificultad);

        JButton btnFiltrar = new JButton("Filtrar");
        UIUtils.stylizePrimaryButton(btnFiltrar);
        btnFiltrar.addActionListener(e -> {
            paginaActual = 1;
            cargarDatos();
        });
        panelFiltros.add(btnFiltrar);

        add(panelFiltros, BorderLayout.NORTH);

        // Tabla
        tableModel = new DefaultTableModel(
                new Object[] { "ID", "Contexto", "Competencia", "Dificultad", "Estado", "Objeto" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(tableModel);
        tabla.removeColumn(tabla.getColumnModel().getColumn(5)); // Ocultar objeto Pregunta
        tabla.getColumnModel().getColumn(4).setCellRenderer(new EstadoCellRenderer());

        // Estilizar tabla
        tabla.setRowHeight(30);
        tabla.setFont(UIUtils.FONT_REGULAR);
        tabla.setGridColor(new Color(226, 232, 240));
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(new Color(219, 234, 254));
        tabla.setSelectionForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(UIUtils.FONT_SUBTITLE);
        header.setBackground(UIUtils.COLOR_TEXT_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 35));

        tabla.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table = (JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    mostrarDetalle(row);
                }
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(scrollTabla, BorderLayout.CENTER);

        // Panel inferior: Paginación y acciones
        JPanel panelInferior = new JPanel(new BorderLayout());

        JPanel panelPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAnterior = new JButton("< Anterior");
        UIUtils.stylizeSecondaryButton(btnAnterior);
        btnAnterior.addActionListener(e -> {
            if (paginaActual > 1) {
                paginaActual--;
                cargarDatos();
            }
        });

        lblPagina = new JLabel("Página 1 de 1");
        lblPagina.setFont(UIUtils.FONT_REGULAR);

        btnSiguiente = new JButton("Siguiente >");
        UIUtils.stylizeSecondaryButton(btnSiguiente);
        btnSiguiente.addActionListener(e -> {
            if (paginaActual < totalPaginas) {
                paginaActual++;
                cargarDatos();
            }
        });

        panelPaginacion.add(btnAnterior);
        panelPaginacion.add(lblPagina);
        panelPaginacion.add(btnSiguiente);
        panelInferior.add(panelPaginacion, BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));

        JButton btnImportar = new JButton("📥 Importar Externa");
        UIUtils.stylizeSecondaryButton(btnImportar);
        btnImportar.addActionListener(e -> importarExterna());

        JButton btnVerDetalle = new JButton("Ver Detalle");
        UIUtils.stylizeSecondaryButton(btnVerDetalle);
        btnVerDetalle.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row != -1) {
                mostrarDetalle(row);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una pregunta", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnCambiarEstado = new JButton("⚙️ Cambiar Estado");
        UIUtils.stylizeSecondaryButton(btnCambiarEstado);
        btnCambiarEstado.addActionListener(e -> cambiarEstado());

        JButton btnEvaluar = new JButton("🎯 Evaluar Respuesta");
        UIUtils.stylizeSecondaryButton(btnEvaluar);
        btnEvaluar.addActionListener(e -> evaluarRespuesta());

        JButton btnEnviarRevision = new JButton("Enviar a Revisión");
        UIUtils.stylizePrimaryButton(btnEnviarRevision);
        btnEnviarRevision.addActionListener(e -> enviarARevision());

        boolean esRevisor = preguntaController != null
                && preguntaController.getRol() == com.unicauca.taller2.usuarios.model.Rol.REVISOR;

        if (esRevisor) {
            JButton btnAprobar = new JButton("✅ Aprobar");
            UIUtils.stylizePrimaryButton(btnAprobar);
            btnAprobar.addActionListener(e -> aprobarSeleccionada());

            JButton btnRechazar = new JButton("❌ Rechazar");
            UIUtils.stylizeSecondaryButton(btnRechazar);
            btnRechazar.addActionListener(e -> rechazarSeleccionada());

            panelAcciones.add(btnVerDetalle);
            panelAcciones.add(btnAprobar);
            panelAcciones.add(btnRechazar);
            panelAcciones.add(btnEvaluar);
            panelAcciones.add(btnCambiarEstado);
        } else {
            panelAcciones.add(btnImportar);
            panelAcciones.add(btnVerDetalle);
            panelAcciones.add(btnCambiarEstado);
            panelAcciones.add(btnEvaluar);
            panelAcciones.add(btnEnviarRevision);
        }
        panelInferior.add(panelAcciones, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        List<EstadoPregunta> estadosSeleccionados = listaEstados.getSelectedValuesList();
        String dificultad = (String) cmbDificultad.getSelectedItem();

        int totalRegistros = preguntaController.contarMisPreguntas(estadosSeleccionados, dificultad);
        totalPaginas = (int) Math.ceil((double) totalRegistros / TAMANO_PAGINA);
        if (totalPaginas == 0)
            totalPaginas = 1;

        int offset = (paginaActual - 1) * TAMANO_PAGINA;
        List<Pregunta> preguntas = preguntaController.listarMisPreguntas(estadosSeleccionados, dificultad, offset,
                TAMANO_PAGINA);

        tableModel.setRowCount(0);

        if (preguntas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se ha encontrado ninguna pregunta", "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Pregunta p : preguntas) {
                String contextoCorto = p.getContexto().length() > 30 ? p.getContexto().substring(0, 30) + "..."
                        : p.getContexto();
                tableModel.addRow(new Object[] {
                        p.getId(),
                        contextoCorto,
                        p.getCompetencia(),
                        p.getNivelDificultad(),
                        p.getEstado(),
                        p // oculto
                });
            }
        }

        lblPagina.setText("Página " + paginaActual + " de " + totalPaginas);
        btnAnterior.setEnabled(paginaActual > 1);
        btnSiguiente.setEnabled(paginaActual < totalPaginas);
    }

    private void mostrarDetalle(int row) {
        try {
            Pregunta p = (Pregunta) tableModel.getValueAt(row, 5); // recuperar objeto oculto
            Pregunta detalleCompleto = preguntaController.obtenerDetalle(p.getId());
            if (detalleCompleto != null) {
                DetallePreguntaDialog dialog = new DetallePreguntaDialog(SwingUtilities.getWindowAncestor(this),
                        detalleCompleto, preguntaController);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el detalle de la pregunta.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al mostrar el detalle: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importarExterna() {
        try {
            Pregunta importada = preguntaController.importarPreguntaExterna();
            JOptionPane.showMessageDialog(this,
                    "Pregunta importada exitosamente desde servicio externo (Adapter):\nID: " + importada.getId()
                            + "\nEstado: " + importada.getEstado().getLabel()
                            + "\nTipo: " + (importada.getTipo() != null ? importada.getTipo().getNombre() : "N/A"),
                    "Importación Exitosa", JOptionPane.INFORMATION_MESSAGE);
            actualizarDatos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al importar pregunta: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstado() {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una pregunta para cambiar su estado.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Pregunta p = (Pregunta) tableModel.getValueAt(row, 5);

        String[] opciones = { "Enviar a Revisión", "Aprobar", "Rechazar", "Publicar", "Archivar",
                "Devolver a Borrador" };
        String seleccion = (String) JOptionPane.showInputDialog(
                this,
                "Estado actual: " + p.getEstado().getLabel() + "\nSeleccione la acción a realizar:",
                "Ciclo de Vida (Patrón State)",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (seleccion == null)
            return;

        try {
            switch (seleccion) {
                case "Enviar a Revisión":
                    preguntaController.enviarARevision(p.getId());
                    break;
                case "Aprobar":
                    preguntaController.aprobarPregunta(p.getId());
                    break;
                case "Rechazar":
                    String motivo = JOptionPane.showInputDialog(this, "Ingrese el motivo de rechazo:",
                            "Rechazar Pregunta", JOptionPane.PLAIN_MESSAGE);
                    if (motivo == null || motivo.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "El motivo de rechazo es obligatorio.", "Aviso",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    preguntaController.rechazarPregunta(p.getId(), motivo.trim());
                    break;
                case "Publicar":
                    preguntaController.publicarPregunta(p.getId());
                    break;
                case "Archivar":
                    preguntaController.archivarPregunta(p.getId());
                    break;
                case "Devolver a Borrador":
                    preguntaController.devolverABorrador(p.getId());
                    break;
            }
            JOptionPane.showMessageDialog(this, "Transición de estado ejecutada con éxito.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            actualizarDatos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en transición de estado:\n" + ex.getMessage(), "Error de Estado",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void evaluarRespuesta() {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una pregunta para evaluar.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Pregunta p = (Pregunta) tableModel.getValueAt(row, 5);
        Pregunta detalle = preguntaController.obtenerDetalle(p.getId());
        if (detalle == null) {
            detalle = p;
        }

        // Construir modal interactivo de evaluación
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Evaluar Respuesta (Template Method)",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(560, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS)); // antes GridLayout(0,1,4,4)
        info.add(new JLabel("<html><b>Pregunta:</b> " + detalle.getPreguntaDirecta() + "</html>"));
        info.add(new JLabel("<html><b>Estado:</b> " + detalle.getEstado().getLabel() + " (Debe ser Publicada)</html>"));
        info.add(new JLabel("<html><b>Tipo:</b> " + (detalle.getTipo() != null ? detalle.getTipo().getNombre() : "N/A")
                + "</html>"));
        for (Component c : info.getComponents()) {
            ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT); // evita que se centren
        }

        if (detalle.getOpciones() != null && !detalle.getOpciones().isEmpty()) {
            StringBuilder sb = new StringBuilder("<html><b>Opciones:</b><br/>");
            for (var op : detalle.getOpciones()) {
                sb.append(op.clave()).append(") ").append(op.texto()).append("<br/>");
            }
            sb.append("</html>");
            info.add(new JLabel(sb.toString()));
        }

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(new JLabel("Ingrese respuesta (ej. A, o A,B): "), BorderLayout.NORTH);
        JTextField txtRespuesta = new JTextField();
        UIUtils.stylizeTextField(txtRespuesta);
        inputPanel.add(txtRespuesta, BorderLayout.CENTER);

        JLabel lblResultado = new JLabel(" ", JLabel.CENTER);
        lblResultado.setFont(UIUtils.FONT_SUBTITLE);

        JButton btnCalificar = new JButton("Calificar");
        UIUtils.stylizePrimaryButton(btnCalificar);
        final Pregunta targetP = detalle;
        btnCalificar.addActionListener(e -> {
            String val = txtRespuesta.getText().trim();
            if (val.isEmpty()) {
                lblResultado.setText("<html><font color='red'>Ingrese una respuesta.</font></html>");
                return;
            }
            List<String> respuestas = java.util.Arrays.stream(val.split("[,; ]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            try {
                var res = preguntaController.evaluarRespuesta(targetP.getId(), respuestas);
                String color = res.correct() ? "green" : "red";
                lblResultado
                        .setText("<html><font color='" + color + "'><b>" + (res.correct() ? "¡CORRECTA!" : "INCORRECTA")
                                + "</b> | Score: " + res.score() + "<br/>" + res.feedback() + "</font></html>");
            } catch (Exception ex) {
                lblResultado.setText("<html><font color='red'><b>Error:</b> " + ex.getMessage() + "</font></html>");
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.add(info, BorderLayout.NORTH);
        centerPanel.add(inputPanel, BorderLayout.CENTER);
        centerPanel.add(lblResultado, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnCalificar);
        JButton btnCerrar = new JButton("Cerrar");
        UIUtils.stylizeSecondaryButton(btnCerrar);
        btnCerrar.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnCerrar);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void enviarARevision() {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una pregunta para enviar a revisión.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Pregunta p = (Pregunta) tableModel.getValueAt(row, 5);

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro que desea enviar la pregunta a revisión?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                preguntaController.enviarARevision(p.getId());
                JOptionPane.showMessageDialog(this, "La pregunta ha sido enviada a revisión exitosamente.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                cargarDatos(); // Refrescar la tabla
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void aprobarSeleccionada() {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una pregunta para aprobar.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Pregunta p = (Pregunta) tableModel.getValueAt(row, 5);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de aprobar la pregunta " + p.getId() + "?",
                "Confirmar Aprobación",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        try {
            preguntaController.aprobarPregunta(p.getId());
            JOptionPane.showMessageDialog(this, "Pregunta " + p.getId() + " aprobada con éxito.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            actualizarDatos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al aprobar la pregunta:\n" + ex.getMessage(), "Error de Estado",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rechazarSeleccionada() {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una pregunta para rechazar.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Pregunta p = (Pregunta) tableModel.getValueAt(row, 5);
        String motivo = JOptionPane.showInputDialog(this, "Ingrese el motivo del rechazo:", "Rechazar Pregunta",
                JOptionPane.PLAIN_MESSAGE);
        if (motivo == null || motivo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El motivo de rechazo es obligatorio.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            preguntaController.rechazarPregunta(p.getId(), motivo.trim());
            JOptionPane.showMessageDialog(this, "Pregunta " + p.getId() + " rechazada con éxito.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            actualizarDatos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al rechazar la pregunta:\n" + ex.getMessage(), "Error de Estado",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
