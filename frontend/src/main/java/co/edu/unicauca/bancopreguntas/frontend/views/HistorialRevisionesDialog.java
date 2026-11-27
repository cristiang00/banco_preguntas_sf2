package co.edu.unicauca.bancopreguntas.frontend.views;

import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiException;
import co.edu.unicauca.bancopreguntas.frontend.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Diálogo de Historial de Revisiones por Pregunta (HU6.S1.3 y HU6.S2.3).
 * Muestra las rondas de revisión con revisor, estado coloreado, fechas y detalle de observaciones.
 * Valida autoría y maneja amigablemente el error 403 Forbidden.
 */
public class HistorialRevisionesDialog extends JDialog {

    private final Long preguntaId;
    private int paginaActual = 0;
    private final int tamanoPagina = 10;
    private int totalPaginas = 1;

    private DefaultTableModel tableModel;
    private JTable tablaHistorial;
    private JTextArea txtDetalleObservaciones;
    private JLabel lblPaginacion;
    private JButton btnAnterior;
    private JButton btnSiguiente;
    private List<Map<String, Object>> rondasActuales;

    public HistorialRevisionesDialog(Window owner, Long preguntaId) {
        super(owner, "Historial de Revisiones — Pregunta #" + preguntaId, ModalityType.APPLICATION_MODAL);
        this.preguntaId = preguntaId;
        initComponents();
        cargarHistorial();
    }

    private void initComponents() {
        setSize(750, 520);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIUtils.COLOR_BACKGROUND);

        // Encabezado
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(UIUtils.COLOR_SURFACE);
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTitulo = new JLabel("Historial de Revisiones y Observaciones por Ronda");
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        lblTitulo.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Pregunta ID: #" + preguntaId + " — Rondas cronológicas de evaluación");
        lblSub.setFont(UIUtils.FONT_REGULAR);
        lblSub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        pnlHeader.add(lblTitulo, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // Tabla central
        String[] columnas = {"ID Revisión", "Ronda", "Revisor", "Decisión / Estado", "Fecha Asignación", "Observaciones"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaHistorial = new JTable(tableModel);
        tablaHistorial.setRowHeight(30);
        tablaHistorial.setFont(UIUtils.FONT_REGULAR);
        tablaHistorial.getTableHeader().setFont(UIUtils.FONT_SUBTITLE);
        tablaHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Renderizador con colores de estado (HU6.S1.3)
        tablaHistorial.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(UIUtils.FONT_SUBTITLE);
                String estado = value != null ? value.toString().toUpperCase() : "";

                if (!isSelected) {
                    if (estado.contains("APROBADA")) {
                        lbl.setForeground(new Color(22, 101, 52)); // Verde oscuro
                        lbl.setBackground(new Color(220, 252, 231)); // Fondo verde suave
                    } else if (estado.contains("RECHAZADA")) {
                        lbl.setForeground(new Color(153, 27, 27)); // Rojo oscuro
                        lbl.setBackground(new Color(254, 226, 226)); // Fondo rojo suave
                    } else if (estado.contains("EVALUADA")) {
                        lbl.setForeground(new Color(30, 64, 175)); // Azul oscuro
                        lbl.setBackground(new Color(219, 234, 254)); // Fondo azul suave
                    } else {
                        lbl.setForeground(new Color(133, 77, 14)); // Ámbar oscuro
                        lbl.setBackground(new Color(254, 243, 199)); // Fondo ámbar
                    }
                    lbl.setOpaque(true);
                }
                return lbl;
            }
        });

        tablaHistorial.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablaHistorial.getSelectedRow();
                if (row >= 0 && rondasActuales != null && row < rondasActuales.size()) {
                    Map<String, Object> ronda = rondasActuales.get(row);
                    StringBuilder sb = new StringBuilder();

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> evals = (List<Map<String, Object>>) ronda.get("evaluaciones");
                    if (evals != null && !evals.isEmpty()) {
                        sb.append("Evaluaciones registradas:\n");
                        for (int i = 0; i < evals.size(); i++) {
                            Map<String, Object> ev = evals.get(i);
                            sb.append("• Criterio: ").append(ev.getOrDefault("criterio", "General"))
                              .append(" | Calificación: ").append(ev.getOrDefault("calificacion", "N/A"));
                            Object com = ev.get("comentario");
                            if (com != null && !com.toString().isBlank()) {
                                sb.append(" — ").append(com);
                            }
                            sb.append("\n");
                        }
                        sb.append("\n");
                    }

                    @SuppressWarnings("unchecked")
                    List<String> obs = (List<String>) ronda.get("observaciones");
                    if (obs != null && !obs.isEmpty()) {
                        sb.append("Observaciones del revisor:\n");
                        for (int i = 0; i < obs.size(); i++) {
                            sb.append("• [Observación ").append(i + 1).append("]: ").append(obs.get(i)).append("\n");
                        }
                    }

                    String texto = sb.toString().trim();
                    txtDetalleObservaciones.setText(texto.isEmpty()
                            ? "Sin evaluaciones ni observaciones registradas en esta ronda."
                            : texto);
                }
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaHistorial);
        scrollTabla.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));

        // Panel de detalle de observaciones
        JPanel pnlDetalle = new JPanel(new BorderLayout());
        pnlDetalle.setBackground(UIUtils.COLOR_SURFACE);
        pnlDetalle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER), "Detalle de Observaciones de la Ronda Seleccionada"),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        txtDetalleObservaciones = new JTextArea(4, 30);
        txtDetalleObservaciones.setEditable(false);
        txtDetalleObservaciones.setLineWrap(true);
        txtDetalleObservaciones.setWrapStyleWord(true);
        txtDetalleObservaciones.setFont(UIUtils.FONT_REGULAR);
        txtDetalleObservaciones.setBackground(UIUtils.COLOR_BACKGROUND);
        txtDetalleObservaciones.setText("Seleccione una revisión de la tabla para ver el detalle de sus observaciones.");
        pnlDetalle.add(new JScrollPane(txtDetalleObservaciones), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTabla, pnlDetalle);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerSize(4);
        add(splitPane, BorderLayout.CENTER);

        // Pie con controles de paginación y cerrar
        JPanel pnlSur = new JPanel(new BorderLayout(8, 8));
        pnlSur.setBackground(UIUtils.COLOR_SURFACE);
        pnlSur.setBorder(BorderFactory.createEmptyBorder(8, 16, 12, 16));

        JPanel pnlPaging = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlPaging.setBackground(UIUtils.COLOR_SURFACE);

        btnAnterior = new JButton("◀ Anterior");
        btnSiguiente = new JButton("Siguiente ▶");
        lblPaginacion = new JLabel("Página 1 de 1");
        lblPaginacion.setFont(UIUtils.FONT_REGULAR);

        btnAnterior.addActionListener(e -> {
            if (paginaActual > 0) {
                paginaActual--;
                cargarHistorial();
            }
        });

        btnSiguiente.addActionListener(e -> {
            if (paginaActual < totalPaginas - 1) {
                paginaActual++;
                cargarHistorial();
            }
        });

        pnlPaging.add(btnAnterior);
        pnlPaging.add(lblPaginacion);
        pnlPaging.add(btnSiguiente);

        JButton btnCerrar = new JButton("Cerrar");
        UIUtils.stylizeSecondaryButton(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());

        pnlSur.add(pnlPaging, BorderLayout.WEST);
        pnlSur.add(btnCerrar, BorderLayout.EAST);
        add(pnlSur, BorderLayout.SOUTH);
    }

    private void cargarHistorial() {
        SwingUtilities.invokeLater(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().consultarHistorialRevisiones(
                        preguntaId, paginaActual, tamanoPagina
                );

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> content = (List<Map<String, Object>>) resp.get("content");
                this.rondasActuales = content;

                Number totalPagesNum = (Number) resp.getOrDefault("totalPages", 1);
                this.totalPaginas = Math.max(1, totalPagesNum.intValue());

                lblPaginacion.setText("Página " + (paginaActual + 1) + " de " + totalPaginas);
                btnAnterior.setEnabled(paginaActual > 0);
                btnSiguiente.setEnabled(paginaActual < totalPaginas - 1);

                tableModel.setRowCount(0);
                if (content != null && !content.isEmpty()) {
                    for (Map<String, Object> r : content) {
                        @SuppressWarnings("unchecked")
                        List<String> obs = (List<String>) r.get("observaciones");
                        String obsResumen = (obs != null && !obs.isEmpty())
                                ? obs.size() + " observación(es)"
                                : "Sin observaciones";

                        Object numRonda = r.getOrDefault("ronda", 1);
                        tableModel.addRow(new Object[]{
                                r.get("revisionId"),
                                "Ronda " + numRonda,
                                r.get("revisorNombre"),
                                r.get("estado"),
                                r.get("fechaAsignacion"),
                                obsResumen
                        });
                    }
                    tablaHistorial.setRowSelectionInterval(0, 0);
                } else {
                    txtDetalleObservaciones.setText("No hay rondas de revisión registradas para esta pregunta.");
                }

            } catch (ApiException ex) {
                if (ex.getStatusCode() == 403) {
                    JOptionPane.showMessageDialog(this,
                            "<html><b>Acceso Denegado (403):</b><br>" + ex.getMessage() + "</html>",
                            "Permisos Insuficientes", JOptionPane.WARNING_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al consultar historial: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error de comunicación con el gateway: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
