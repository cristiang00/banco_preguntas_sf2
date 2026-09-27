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

    public ListarPreguntasPanel(PreguntaController preguntaController) {
        this.preguntaController = preguntaController;
        inicializarUI();
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

        cmbDificultad = new JComboBox<>(new String[]{"Todos", "Bajo", "Medio", "Alto"});
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
        tableModel = new DefaultTableModel(new Object[]{"ID", "Contexto", "Competencia", "Dificultad", "Estado", "Objeto"}, 0) {
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

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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

        JButton btnEnviarRevision = new JButton("Enviar a Revisión");
        UIUtils.stylizePrimaryButton(btnEnviarRevision);
        btnEnviarRevision.addActionListener(e -> enviarARevision());

        panelAcciones.add(btnVerDetalle);
        panelAcciones.add(btnEnviarRevision);
        panelInferior.add(panelAcciones, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        List<EstadoPregunta> estadosSeleccionados = listaEstados.getSelectedValuesList();
        String dificultad = (String) cmbDificultad.getSelectedItem();
        
        int totalRegistros = preguntaController.contarMisPreguntas(estadosSeleccionados, dificultad);
        totalPaginas = (int) Math.ceil((double) totalRegistros / TAMANO_PAGINA);
        if (totalPaginas == 0) totalPaginas = 1;

        int offset = (paginaActual - 1) * TAMANO_PAGINA;
        List<Pregunta> preguntas = preguntaController.listarMisPreguntas(estadosSeleccionados, dificultad, offset, TAMANO_PAGINA);

        tableModel.setRowCount(0);

        if (preguntas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se ha encontrado ninguna pregunta", "Información", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Pregunta p : preguntas) {
                String contextoCorto = p.getContexto().length() > 30 ? p.getContexto().substring(0, 30) + "..." : p.getContexto();
                tableModel.addRow(new Object[]{
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
            if(detalleCompleto != null){
                DetallePreguntaDialog dialog = new DetallePreguntaDialog(SwingUtilities.getWindowAncestor(this), detalleCompleto);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el detalle de la pregunta.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al mostrar el detalle: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enviarARevision() {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una pregunta para enviar a revisión.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Pregunta p = (Pregunta) tableModel.getValueAt(row, 5);

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro que desea enviar la pregunta a revisión?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                preguntaController.enviarARevision(p.getId());
                JOptionPane.showMessageDialog(this, "La pregunta ha sido enviada a revisión exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatos(); // Refrescar la tabla
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
