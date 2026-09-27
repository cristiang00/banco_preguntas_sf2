package co.edu.unicauca.bancopreguntas.presentation.views;

import co.edu.unicauca.bancopreguntas.domain.entities.Notificacion;
import co.edu.unicauca.bancopreguntas.domain.repositories.NotificacionRepository;
import co.edu.unicauca.bancopreguntas.presentation.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel que muestra el historial de notificaciones de asignación de revisores.
 * <p>
 * Los datos se obtienen de {@link NotificacionRepository} (SQLite), así el historial
 * persiste entre sesiones. Se puede acceder desde el menú del Administrador.
 * </p>
 */
public class NotificacionesPanel extends JPanel {

    private final NotificacionRepository notificacionRepository;
    private DefaultTableModel modelo;

    public NotificacionesPanel(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
        inicializarUI();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UIUtils.COLOR_BACKGROUND);

        // Título
        JLabel titulo = new JLabel("Historial de Notificaciones", SwingConstants.CENTER);
        titulo.setFont(UIUtils.FONT_TITLE);
        titulo.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        add(titulo, BorderLayout.NORTH);

        // Tabla
        modelo = new DefaultTableModel(
                new Object[]{"Fecha", "Revisor ID", "Asunto", "Pregunta ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        tabla.setFont(UIUtils.FONT_REGULAR);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.setRowHeight(28);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(new Color(229, 231, 235));
        tabla.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true));
        add(scroll, BorderLayout.CENTER);

        // Botón de refresco
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sur.setOpaque(false);
        JButton btnRefrescar = new JButton("🔄  Refrescar");
        UIUtils.stylizePrimaryButton(btnRefrescar);
        btnRefrescar.addActionListener(e -> cargarDatos());
        sur.add(btnRefrescar);
        add(sur, BorderLayout.SOUTH);
    }

    /**
     * Recarga los datos desde el repositorio. Llamar al abrir el panel y al pulsar "Refrescar".
     */
    public void cargarDatos() {
        modelo.setRowCount(0);
        List<Notificacion> notificaciones = notificacionRepository.listarTodas();
        for (Notificacion n : notificaciones) {
            modelo.addRow(new Object[]{
                n.getFecha().toString().replace("T", " ").substring(0, 19),
                n.getRevisorId(),
                n.getAsunto(),
                n.getPreguntaId()
            });
        }
        if (notificaciones.isEmpty()) {
            // Fila informativa cuando no hay historial
            modelo.addRow(new Object[]{"—", "—", "Sin notificaciones registradas", "—"});
        }
    }
}
