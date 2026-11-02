package co.edu.unicauca.bancopreguntas.frontend.views;

import co.edu.unicauca.bancopreguntas.contracts.dto.TokenResponseDTO;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiException;
import co.edu.unicauca.bancopreguntas.frontend.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JLabel lblError;
    private final JButton btnLogin;

    public LoginFrame() {
        super("Banco de Preguntas Saber Pro - Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));

        // Cabecera
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Saber Pro 2.0", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel lblSub = new JLabel("Arquitectura de Microservicios Distribuidos", SwingConstants.CENTER);
        lblSub.setFont(UIUtils.FONT_REGULAR);
        lblSub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        headerPanel.add(lblTitle);
        headerPanel.add(lblSub);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Formulario
        JPanel formPanel = new JPanel(new GridLayout(5, 1, 8, 8));
        formPanel.setOpaque(false);

        JLabel lblUser = new JLabel("Nombre de Usuario:");
        lblUser.setFont(UIUtils.FONT_SUBTITLE);
        txtUsername = new JTextField("juan12"); // Usuario autor por defecto para facilitar pruebas
        UIUtils.stylizeTextField(txtUsername);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(UIUtils.FONT_SUBTITLE);
        txtPassword = new JPasswordField("Password123!");
        UIUtils.stylizeTextField(txtPassword);

        lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setFont(UIUtils.FONT_SMALL);
        lblError.setForeground(UIUtils.COLOR_ERROR);

        formPanel.add(lblUser);
        formPanel.add(txtUsername);
        formPanel.add(lblPass);
        formPanel.add(txtPassword);
        formPanel.add(lblError);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Botones
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        bottomPanel.setOpaque(false);

        btnLogin = new JButton("Ingresar al Sistema");
        UIUtils.stylizePrimaryButton(btnLogin);
        btnLogin.addActionListener(e -> ejecutarLogin());

        JLabel lblInfo = new JLabel("Conectando a API Gateway (http://localhost:8080)", SwingConstants.CENTER);
        lblInfo.setFont(UIUtils.FONT_SMALL);
        lblInfo.setForeground(UIUtils.COLOR_TEXT_MUTED);

        bottomPanel.add(btnLogin);
        bottomPanel.add(lblInfo);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);

        // Enter para login
        getRootPane().setDefaultButton(btnLogin);
    }

    private void ejecutarLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Debe ingresar usuario y contraseña");
            return;
        }

        btnLogin.setEnabled(false);
        lblError.setText("Autenticando...");

        SwingUtilities.invokeLater(() -> {
            try {
                TokenResponseDTO tokenResponse = ApiClient.getInstance().login(username, password);
                lblError.setText("");
                dispose(); // Cerrar login
                new MainDashboardFrame(tokenResponse).setVisible(true);
            } catch (ApiException ex) {
                lblError.setText("Error (" + ex.getStatusCode() + "): " + ex.getMessage());
            } catch (Exception ex) {
                lblError.setText("Falla de conexión con API Gateway: " + ex.getMessage());
            } finally {
                btnLogin.setEnabled(true);
            }
        });
    }
}
