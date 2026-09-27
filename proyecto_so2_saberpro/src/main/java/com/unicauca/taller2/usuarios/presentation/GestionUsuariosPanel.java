package com.unicauca.taller2.usuarios.presentation;

import com.unicauca.taller2.usuarios.services.UsuarioService;
import com.unicauca.taller2.usuarios.model.EstadoUsuario;
import com.unicauca.taller2.usuarios.model.Usuario;
import co.edu.unicauca.bancopreguntas.presentation.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel para la gestión de usuarios.
 */
public class GestionUsuariosPanel extends JPanel {

    private final UsuarioService usuarioService;
    private JTable tablaUsuarios;
    private DefaultTableModel tableModel;

    private static final String[] COLUMNAS = {
            "Usuario", "Nombre Completo", "Rol", "Estado"
    };

    /**
     * Constructor del panel de gestión de usuarios.
     *
     * @param usuarioService servicio de usuarios
     */
    public GestionUsuariosPanel(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        inicializarUI();
        cargarUsuarios();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 250));

        JLabel lblTitulo = new JLabel("Gestión de Usuarios", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(33, 37, 41));
        add(lblTitulo, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaUsuarios = new JTable(tableModel);
        tablaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaUsuarios.setRowHeight(30);
        tablaUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaUsuarios.getTableHeader().setBackground(new Color(13, 110, 253));
        tablaUsuarios.getTableHeader().setForeground(Color.WHITE);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.setGridColor(new Color(222, 226, 230));

        DefaultTableCellRenderer centeredRenderer = new DefaultTableCellRenderer();
        centeredRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < COLUMNAS.length; i++) {
            tablaUsuarios.getColumnModel().getColumn(i).setCellRenderer(centeredRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton btnActivar = new JButton("Activar");
        UIUtils.stylizePrimaryButton(btnActivar);
        btnActivar.setBackground(new Color(25, 135, 84));
        btnActivar.addActionListener(e -> cambiarEstadoSeleccionado(EstadoUsuario.ACTIVO));

        JButton btnInactivar = new JButton("Inactivar");
        UIUtils.stylizePrimaryButton(btnInactivar);
        btnInactivar.setBackground(new Color(220, 53, 69));
        btnInactivar.addActionListener(e -> cambiarEstadoSeleccionado(EstadoUsuario.INACTIVO));

        JButton btnRefrescar = new JButton("Refrescar");
        UIUtils.stylizeSecondaryButton(btnRefrescar);
        btnRefrescar.addActionListener(e -> cargarUsuarios());

        buttonPanel.add(btnActivar);
        buttonPanel.add(btnInactivar);
        buttonPanel.add(btnRefrescar);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void cargarUsuarios() {
        tableModel.setRowCount(0);

        List<Usuario> usuarios = usuarioService.listarUsuarios();
        for (Usuario u : usuarios) {
            tableModel.addRow(new Object[]{
                    u.getNombreUsuario(),
                    u.getNombreCompleto(),
                    u.getRol().getDisplayName(),
                    u.getEstado().getDisplayName()
            });
        }
    }

    private void cambiarEstadoSeleccionado(EstadoUsuario nuevoEstado) {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un usuario de la tabla.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombreUsuario = (String) tableModel.getValueAt(filaSeleccionada, 0);
        String estadoActual = (String) tableModel.getValueAt(filaSeleccionada, 3);

        if (estadoActual.equals(nuevoEstado.getDisplayName())) {
            JOptionPane.showMessageDialog(this,
                    "El usuario '" + nombreUsuario + "' ya se encuentra " + nuevoEstado.getDisplayName() + ".",
                    "Sin cambios", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Desea cambiar el estado del usuario '" + nombreUsuario + "' a " + nuevoEstado.getDisplayName() + "?",
                "Confirmar cambio de estado",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                usuarioService.cambiarEstado(nombreUsuario, nuevoEstado);
                cargarUsuarios();
                JOptionPane.showMessageDialog(this,
                        "Estado del usuario '" + nombreUsuario + "' actualizado a " + nuevoEstado.getDisplayName() + ".",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al cambiar el estado: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
