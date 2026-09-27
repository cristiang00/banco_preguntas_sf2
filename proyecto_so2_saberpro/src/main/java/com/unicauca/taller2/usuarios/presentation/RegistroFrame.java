package com.unicauca.taller2.usuarios.presentation;

import com.unicauca.taller2.usuarios.services.UsuarioService;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.access.PasswordPolicyException;

import javax.swing.*;
import java.awt.*;
import co.edu.unicauca.bancopreguntas.presentation.utils.UIUtils;

/**
 * Ventana de registro de nuevos usuarios.
 */
public class RegistroFrame extends JDialog {

    private final UsuarioService usuarioService;

    private JTextField txtNombreUsuario;
    private JTextField txtNombreCompleto;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JComboBox<Rol> cmbRol;

    /**
     * Constructor de la ventana de registro.
     *
     * @param usuarioService servicio de usuarios
     * @param parent         ventana principal
     */
    public RegistroFrame(UsuarioService usuarioService, JFrame parent) {
        super(parent, "Registro de Usuario", true);
        this.usuarioService = usuarioService;
        inicializarUI();
    }

    private void inicializarUI() {
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        mainPanel.setBackground(new Color(245, 245, 250));

        JLabel lblTitulo = new JLabel("Nuevo Usuario", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(33, 37, 41));
        mainPanel.add(lblTitulo, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblUsuario = new JLabel("Nombre de usuario:");
        lblUsuario.setFont(labelFont);
        formPanel.add(lblUsuario, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNombreUsuario = new JTextField(20);
        txtNombreUsuario.setFont(fieldFont);
        txtNombreUsuario.setMargin(new Insets(5, 6, 5, 6));
        formPanel.add(txtNombreUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblNombreCompleto = new JLabel("Nombre completo:");
        lblNombreCompleto.setFont(labelFont);
        formPanel.add(lblNombreCompleto, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNombreCompleto = new JTextField(20);
        txtNombreCompleto.setFont(fieldFont);
        txtNombreCompleto.setMargin(new Insets(5, 6, 5, 6));
        formPanel.add(txtNombreCompleto, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblRol = new JLabel("Rol:");
        lblRol.setFont(labelFont);
        formPanel.add(lblRol, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbRol = new JComboBox<>(Rol.values());
        cmbRol.setFont(fieldFont);
        cmbRol.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Rol) {
                    setText(((Rol) value).getDisplayName());
                }
                return this;
            }
        });
        formPanel.add(cmbRol, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(labelFont);
        formPanel.add(lblPassword, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(fieldFont);
        txtPassword.setMargin(new Insets(5, 6, 5, 6));
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        JLabel lblConfirm = new JLabel("Confirmar contraseña:");
        lblConfirm.setFont(labelFont);
        formPanel.add(lblConfirm, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtConfirmPassword = new JPasswordField(20);
        txtConfirmPassword.setFont(fieldFont);
        txtConfirmPassword.setMargin(new Insets(5, 6, 5, 6));
        formPanel.add(txtConfirmPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JLabel lblNota = new JLabel("<html><i>Mín. 6 caracteres, 1 mayúscula, 1 dígito, 1 carácter especial</i></html>");
        lblNota.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNota.setForeground(new Color(108, 117, 125));
        formPanel.add(lblNota, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton btnRegistrar = new JButton("Registrar");
        UIUtils.stylizePrimaryButton(btnRegistrar);
        btnRegistrar.addActionListener(e -> realizarRegistro());

        JButton btnCancelar = new JButton("Cancelar");
        UIUtils.stylizeSecondaryButton(btnCancelar);
        btnCancelar.addActionListener(e -> dispose());

        buttonPanel.add(btnRegistrar);
        buttonPanel.add(btnCancelar);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnRegistrar);

        setContentPane(mainPanel);
        pack();
        setMinimumSize(new Dimension(480, 400));
        setLocationRelativeTo(getParent());
    }

    private void realizarRegistro() {
        String nombreUsuario = txtNombreUsuario.getText().trim();
        String nombreCompleto = txtNombreCompleto.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        Rol rol = (Rol) cmbRol.getSelectedItem();

        if (nombreUsuario.isEmpty() || nombreCompleto.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Todos los campos son obligatorios.",
                    "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Las contraseñas no coinciden.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            txtConfirmPassword.setText("");
            return;
        }

        try {
            usuarioService.registrarUsuario(nombreUsuario, nombreCompleto, rol, password);

            JOptionPane.showMessageDialog(this,
                    "Usuario '" + nombreUsuario + "' registrado exitosamente.",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (PasswordPolicyException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Contraseña no válida", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error de registro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
