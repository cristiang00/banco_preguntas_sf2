package co.edu.unicauca.bancopreguntas.frontend.views;

import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.utils.EstadoCellRenderer;
import co.edu.unicauca.bancopreguntas.frontend.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ListarPreguntasPanel extends JPanel {

    private final JComboBox<String> cmbEstado;
    private final JComboBox<String> cmbDificultad;
    private final JTable tabla;
    private final DefaultTableModel tableModel;
    private final List<PreguntaDTO> preguntasActuales = new ArrayList<>();

    private final JButton btnAnterior;
    private final JButton btnSiguiente;
    private final JLabel lblPagina;
    private final JLabel lblEstadoAccion;
    private final JButton btnEditarBorrador;
    private final JButton btnNuevaVersion;

    private int paginaActual = 0;
    private final int TAMANO_PAGINA = 10;
    private int totalPaginas = 1;

    public ListarPreguntasPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de Filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFiltros.setBackground(UIUtils.COLOR_SURFACE);
        panelFiltros.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        cmbEstado = new JComboBox<>(new String[]{"Todos", "BORRADOR", "PENDIENTE_REVISION", "APROBADA", "RECHAZADA"});
        UIUtils.stylizeTextField(cmbEstado);

        cmbDificultad = new JComboBox<>(new String[]{"Todos", "Bajo", "Medio", "Alto"});
        UIUtils.stylizeTextField(cmbDificultad);

        JButton btnFiltrar = new JButton("Filtrar");
        UIUtils.stylizePrimaryButton(btnFiltrar);
        btnFiltrar.addActionListener(e -> {
            paginaActual = 0;
            cargarDatos();
        });

        panelFiltros.add(new JLabel("Estado:"));
        panelFiltros.add(cmbEstado);
        panelFiltros.add(new JLabel("Dificultad:"));
        panelFiltros.add(cmbDificultad);
        panelFiltros.add(btnFiltrar);

        add(panelFiltros, BorderLayout.NORTH);

        // Tabla
        tableModel = new DefaultTableModel(new Object[]{"ID", "Contexto / Enunciado", "Competencia", "Dificultad", "Estado", "Versión"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(tableModel);
        tabla.setRowHeight(32);
        tabla.setFont(UIUtils.FONT_REGULAR);
        tabla.setSelectionBackground(new Color(219, 234, 254));
        tabla.setSelectionForeground(UIUtils.COLOR_TEXT_PRIMARY);
        tabla.getColumnModel().getColumn(4).setCellRenderer(new EstadoCellRenderer());

        JTableHeader header = tabla.getTableHeader();
        header.setFont(UIUtils.FONT_SUBTITLE);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 35));

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() != -1) {
                    mostrarDetalle(tabla.getSelectedRow());
                }
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        add(scrollTabla, BorderLayout.CENTER);

        // Panel Inferior: Paginación y Acciones
        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        panelInferior.setOpaque(false);

        JPanel panelPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        panelPaginacion.setOpaque(false);

        btnAnterior = new JButton("< Anterior");
        UIUtils.stylizeSecondaryButton(btnAnterior);
        btnAnterior.addActionListener(e -> {
            if (paginaActual > 0) {
                paginaActual--;
                cargarDatos();
            }
        });

        lblPagina = new JLabel("Página 1 de 1");
        lblPagina.setFont(UIUtils.FONT_REGULAR);

        btnSiguiente = new JButton("Siguiente >");
        UIUtils.stylizeSecondaryButton(btnSiguiente);
        btnSiguiente.addActionListener(e -> {
            if (paginaActual < totalPaginas - 1) {
                paginaActual++;
                cargarDatos();
            }
        });

        panelPaginacion.add(btnAnterior);
        panelPaginacion.add(lblPagina);
        panelPaginacion.add(btnSiguiente);
        panelInferior.add(panelPaginacion, BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        panelAcciones.setOpaque(false);

        JButton btnRefrescar = new JButton("Refrescar");
        UIUtils.stylizeSecondaryButton(btnRefrescar);
        btnRefrescar.addActionListener(e -> cargarDatos());

        JButton btnDetalle = new JButton("Ver Detalle");
        UIUtils.stylizeSecondaryButton(btnDetalle);
        btnDetalle.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row != -1) {
                mostrarDetalle(row);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una pregunta para ver el detalle.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnEnviarRevision = new JButton("Enviar a Revisión");
        UIUtils.stylizePrimaryButton(btnEnviarRevision);
        btnEnviarRevision.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row != -1) {
                PreguntaDTO p = preguntasActuales.get(row);
                if (!"BORRADOR".equalsIgnoreCase(p.getEstado())) {
                    JOptionPane.showMessageDialog(this, "Solo las preguntas en estado BORRADOR pueden ser enviadas a revisión.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this, "¿Desea enviar la pregunta #" + p.getId() + " (v" + p.getVersion() + ") a revisión?", "Confirmar Envío", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        ApiClient.getInstance().enviarARevision(p.getId());
                        JOptionPane.showMessageDialog(this, "Pregunta enviada a revisión correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        cargarDatos();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error enviando a revisión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una pregunta.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        panelAcciones.add(btnRefrescar);
        panelAcciones.add(btnDetalle);

        JButton btnHistorial = new JButton("Ver Historial");
        UIUtils.stylizeSecondaryButton(btnHistorial);
        btnHistorial.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row != -1) {
                PreguntaDTO p = preguntasActuales.get(row);
                new HistorialRevisionesDialog((Frame) SwingUtilities.getWindowAncestor(this), p.getId()).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una pregunta para consultar su historial de revisiones.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });
        panelAcciones.add(btnHistorial);

        btnEditarBorrador = new JButton("Editar Borrador");
        UIUtils.stylizeSecondaryButton(btnEditarBorrador);
        btnEditarBorrador.setEnabled(false);
        btnEditarBorrador.setToolTipText("Solo disponible para preguntas en estado BORRADOR");
        btnEditarBorrador.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row != -1 && row < preguntasActuales.size()) {
                PreguntaDTO p = preguntasActuales.get(row);
                if (!"BORRADOR".equalsIgnoreCase(p.getEstado())) {
                    JOptionPane.showMessageDialog(this, "Solo se pueden editar preguntas en estado BORRADOR.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                DetallePreguntaDialog dialog = new DetallePreguntaDialog(
                        SwingUtilities.getWindowAncestor(this), p, this::cargarDatos
                );
                dialog.abrirEdicionBorrador();
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una pregunta en BORRADOR.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });
        panelAcciones.add(btnEditarBorrador);

        // HU6.S3.4 / HU6.S4.3: Botón Crear versión mejorada (deshabilitado si estado != RECHAZADA)
        btnNuevaVersion = new JButton("Crear versión mejorada");
        UIUtils.stylizePrimaryButton(btnNuevaVersion);
        btnNuevaVersion.setEnabled(false);
        btnNuevaVersion.setToolTipText("Solo disponible para preguntas en estado RECHAZADA");
        btnNuevaVersion.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row != -1) {
                PreguntaDTO p = preguntasActuales.get(row);
                if (!"RECHAZADA".equalsIgnoreCase(p.getEstado())) {
                    JOptionPane.showMessageDialog(this,
                            "La acción 'Crear versión mejorada' solo está permitida para preguntas en estado RECHAZADA. El estado actual es: " + p.getEstado(),
                            "Acción no permitida", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this,
                        "¿Desea crear una nueva versión mejorada de la pregunta #" + p.getId() + "?\nLa pregunta pasará al estado BORRADOR (v" + (p.getVersion() + 1) + ") para su posterior edición y reenvío.",
                        "Confirmar Creación de Versión Mejorada", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        PreguntaDTO versionada = ApiClient.getInstance().crearNuevaVersion(p.getId(), null);
                        JOptionPane.showMessageDialog(this,
                                "Nueva versión (v" + versionada.getVersion() + ") creada exitosamente en estado " + versionada.getEstado() + ".",
                                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        cargarDatos();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error al crear nueva versión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una pregunta rechazada de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });
        panelAcciones.add(btnNuevaVersion);

        panelAcciones.add(btnEnviarRevision);
        panelInferior.add(panelAcciones, BorderLayout.EAST);

        lblEstadoAccion = new JLabel("Seleccione una pregunta. 'Crear versión mejorada' solo se habilita en estado RECHAZADA.");
        lblEstadoAccion.setFont(UIUtils.FONT_SMALL);
        lblEstadoAccion.setForeground(UIUtils.COLOR_TEXT_MUTED);
        panelInferior.add(lblEstadoAccion, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                actualizarEstadoBotonNuevaVersion();
            }
        });

        add(panelInferior, BorderLayout.SOUTH);
    }

    private void actualizarEstadoBotonNuevaVersion() {
        int row = tabla.getSelectedRow();
        if (row >= 0 && row < preguntasActuales.size()) {
            PreguntaDTO p = preguntasActuales.get(row);
            boolean esRechazada = "RECHAZADA".equalsIgnoreCase(p.getEstado());
            boolean esBorrador = "BORRADOR".equalsIgnoreCase(p.getEstado());
            btnNuevaVersion.setEnabled(esRechazada);
            btnEditarBorrador.setEnabled(esBorrador);
            if (esRechazada) {
                btnNuevaVersion.setToolTipText("Crear versión mejorada v" + (p.getVersion() + 1) + " en estado BORRADOR");
                lblEstadoAccion.setText("Pregunta #" + p.getId() + " en estado RECHAZADA: puede crear una versión mejorada.");
            } else if (esBorrador) {
                String msg = "Pregunta #" + p.getId() + " (v" + p.getVersion() + ") en BORRADOR: puede editarla o enviarla a revisión.";
                btnNuevaVersion.setToolTipText("Solo disponible para preguntas en estado RECHAZADA");
                lblEstadoAccion.setText(msg);
            } else {
                String msg = "No se puede crear una nueva versión en estado " + p.getEstado() + " (solo permitido desde RECHAZADA).";
                btnNuevaVersion.setToolTipText(msg);
                lblEstadoAccion.setText(msg);
            }
        } else {
            btnNuevaVersion.setEnabled(false);
            btnEditarBorrador.setEnabled(false);
            btnNuevaVersion.setToolTipText("Solo disponible para preguntas en estado RECHAZADA");
            if (!preguntasActuales.isEmpty()) {
                lblEstadoAccion.setText("Seleccione una pregunta. 'Crear versión mejorada' solo se habilita en estado RECHAZADA.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void cargarDatos() {
        String estadoSel = (String) cmbEstado.getSelectedItem();
        String difSel = (String) cmbDificultad.getSelectedItem();

        SwingUtilities.invokeLater(() -> {
            try {
                java.util.Map<String, Object> pageData = ApiClient.getInstance().listarPreguntasPaginado(
                        estadoSel, difSel, paginaActual, TAMANO_PAGINA
                );
                List<PreguntaDTO> lista = (List<PreguntaDTO>) pageData.getOrDefault("content", java.util.Collections.emptyList());
                Number totalPagesNum = (Number) pageData.getOrDefault("totalPages", 1);
                Number totalElemsNum = (Number) pageData.getOrDefault("totalElements", lista.size());

                preguntasActuales.clear();
                preguntasActuales.addAll(lista);

                tableModel.setRowCount(0);
                if (lista.isEmpty()) {
                    lblEstadoAccion.setText("No se encontraron preguntas con los filtros seleccionados.");
                } else {
                    for (PreguntaDTO p : lista) {
                        String contextoCorto = p.getContexto() != null && p.getContexto().length() > 60
                                ? p.getContexto().substring(0, 60) + "..."
                                : p.getContexto();
                        tableModel.addRow(new Object[]{
                                p.getId(),
                                contextoCorto,
                                p.getCompetencia(),
                                p.getNivelDificultad(),
                                p.getEstado(),
                                "v" + p.getVersion()
                        });
                    }
                }

                // Paginación exacta desde el backend
                totalPaginas = Math.max(1, totalPagesNum.intValue());
                lblPagina.setText("Página " + (paginaActual + 1) + " de " + totalPaginas
                        + " (" + totalElemsNum.longValue() + " total)");
                btnAnterior.setEnabled(paginaActual > 0);
                btnSiguiente.setEnabled(paginaActual < totalPaginas - 1);
                actualizarEstadoBotonNuevaVersion();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cargando preguntas desde API Gateway: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void mostrarDetalle(int row) {
        if (row >= 0 && row < preguntasActuales.size()) {
            PreguntaDTO p = preguntasActuales.get(row);
            DetallePreguntaDialog dialog = new DetallePreguntaDialog(
                    SwingUtilities.getWindowAncestor(this),
                    p,
                    this::cargarDatos
            );
            dialog.setVisible(true);
        }
    }
}
