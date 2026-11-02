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
        tableModel = new DefaultTableModel(new Object[]{"ID", "Contexto / Enunciado", "Competencia", "Dificultad", "Estado"}, 0) {
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
                int confirm = JOptionPane.showConfirmDialog(this, "¿Desea enviar la pregunta #" + p.getId() + " a revisión?", "Confirmar Envío", JOptionPane.YES_NO_OPTION);
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
        panelAcciones.add(btnEnviarRevision);
        panelInferior.add(panelAcciones, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);
    }

    public void cargarDatos() {
        String estadoSel = (String) cmbEstado.getSelectedItem();
        String difSel = (String) cmbDificultad.getSelectedItem();

        SwingUtilities.invokeLater(() -> {
            try {
                List<PreguntaDTO> lista = ApiClient.getInstance().listarPreguntas(estadoSel, difSel, paginaActual, TAMANO_PAGINA);
                preguntasActuales.clear();
                preguntasActuales.addAll(lista);

                tableModel.setRowCount(0);
                if (lista.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No se ha encontrado ninguna pregunta con los filtros seleccionados.", "Información", JOptionPane.INFORMATION_MESSAGE);
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
                                p.getEstado()
                        });
                    }
                }

                // Cálculo de paginación
                totalPaginas = Math.max(1, (int) Math.ceil((double) (lista.size() + (paginaActual * TAMANO_PAGINA)) / TAMANO_PAGINA));
                lblPagina.setText("Página " + (paginaActual + 1) + " de " + Math.max(paginaActual + 1, totalPaginas));
                btnAnterior.setEnabled(paginaActual > 0);
                btnSiguiente.setEnabled(lista.size() == TAMANO_PAGINA);

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
