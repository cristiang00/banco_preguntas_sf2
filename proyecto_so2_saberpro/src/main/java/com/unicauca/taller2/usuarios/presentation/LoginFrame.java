package com.unicauca.taller2.usuarios.presentation;

import com.unicauca.taller2.usuarios.services.AutenticacionException;
import com.unicauca.taller2.usuarios.services.AutenticacionService;
import com.unicauca.taller2.usuarios.services.UsuarioService;
import com.unicauca.taller2.usuarios.model.Usuario;

import javax.swing.*;
import java.awt.*;
import co.edu.unicauca.bancopreguntas.presentation.utils.UIUtils;

/**
 * Ventana de inicio de sesión.
 */
public class LoginFrame extends JFrame {

    private final AutenticacionService autenticacionService;
    private final UsuarioService usuarioService;
    private java.util.function.Consumer<Usuario> onLoginSuccess;

    private JTextField txtUsuario;
    private JPasswordField txtPassword;

    /**
     * Constructor de la ventana de inicio de sesión.
     *
     * @param autenticacionService servicio de autenticación
     * @param usuarioService servicio de usuarios
     */
    public LoginFrame(AutenticacionService autenticacionService, UsuarioService usuarioService) {
        this.autenticacionService = autenticacionService;
        this.usuarioService = usuarioService;
        inicializarUI();
    }

    /**
     * Constructor con callback de éxito.
     */
    public LoginFrame(AutenticacionService autenticacionService, UsuarioService usuarioService, java.util.function.Consumer<Usuario> onLoginSuccess) {
        this.autenticacionService = autenticacionService;
        this.usuarioService = usuarioService;
        this.onLoginSuccess = onLoginSuccess;
        inicializarUI();
    }

    private void inicializarUI() {
        setTitle("Saber Pro - Inicio de Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(new Color(245, 245, 250));

        JLabel lblTitulo = new JLabel("Sistema de Gestión Saber Pro", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(33, 37, 41));
        mainPanel.add(lblTitulo, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(lblUsuario, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtUsuario = new JTextField(20);
        UIUtils.stylizeTextField(txtUsuario);
        formPanel.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(lblPassword, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtPassword = new JPasswordField(20);
        UIUtils.stylizeTextField(txtPassword);
        formPanel.add(txtPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton btnLogin = new JButton("Ingresar");
        UIUtils.stylizePrimaryButton(btnLogin);
        btnLogin.setPreferredSize(new Dimension(140, 38));
        btnLogin.addActionListener(e -> realizarLogin());

        JButton btnRegistro = new JButton("Registrarse");
        UIUtils.stylizeSecondaryButton(btnRegistro);
        btnRegistro.setPreferredSize(new Dimension(140, 38));
        btnRegistro.addActionListener(e -> abrirRegistro());

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegistro);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnLogin);

        setContentPane(mainPanel);
        pack();
        setMinimumSize(new Dimension(450, 300));
        setLocationRelativeTo(null);
    }

    private void realizarLogin() {
        String nombreUsuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (nombreUsuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, ingrese usuario y contraseña.",
                    "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario usuario = autenticacionService.autenticar(nombreUsuario, password);
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(usuario);
            }
            dispose();
        } catch (AutenticacionException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error de autenticación", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
        }
    }

    private void abrirRegistro() {
        new RegistroFrame(usuarioService, this).setVisible(true);
    }

    /**
     * Vuelve a mostrar la ventana de inicio de sesión.
     */
    public void mostrarDeNuevo() {
        txtUsuario.setText("");
        txtPassword.setText("");
        setVisible(true);
    }

    // Remove redundant custom button methods
}
