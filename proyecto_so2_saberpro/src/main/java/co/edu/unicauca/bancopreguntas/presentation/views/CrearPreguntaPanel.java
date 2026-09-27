package co.edu.unicauca.bancopreguntas.presentation.views;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.presentation.controllers.PreguntaController;
import javax.swing.*;
import java.awt.*;
import co.edu.unicauca.bancopreguntas.presentation.utils.UIUtils;

public class CrearPreguntaPanel extends JPanel {
    private final PreguntaController preguntaController;
    private JTextArea txtContexto;
    private JTextArea txtPreguntaDirecta;
    private JTextField txtDistractorA;
    private JTextField txtDistractorB;
    private JTextField txtDistractorC;
    private JTextField txtDistractorD;
    private JComboBox<String> cmbRespuestaCorrecta;
    private JTextArea txtJustificacion;
    private JTextArea txtBibliografia;
    private JComboBox<String> cmbCompetencia;
    private JComboBox<String> cmbTema;
    private JComboBox<String> cmbSubtema;
    private JComboBox<String> cmbDificultad;

    public CrearPreguntaPanel(PreguntaController preguntaController) {
        this.preguntaController = preguntaController;
        inicializarUI();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("Crear Nueva Pregunta", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        add(lblTitulo, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;

        // Contexto
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Contexto:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtContexto = new JTextArea(3, 40);
        UIUtils.stylizeTextField(txtContexto);
        formPanel.add(new JScrollPane(txtContexto), gbc);
        y++;

        // Pregunta Directa
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Pregunta Directa:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPreguntaDirecta = new JTextArea(2, 40);
        UIUtils.stylizeTextField(txtPreguntaDirecta);
        formPanel.add(new JScrollPane(txtPreguntaDirecta), gbc);
        y++;

        // Distractor A
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Distractor A:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDistractorA = new JTextField();
        UIUtils.stylizeTextField(txtDistractorA);
        formPanel.add(txtDistractorA, gbc);
        y++;

        // Distractor B
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Distractor B:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDistractorB = new JTextField();
        UIUtils.stylizeTextField(txtDistractorB);
        formPanel.add(txtDistractorB, gbc);
        y++;

        // Distractor C
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Distractor C:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDistractorC = new JTextField();
        UIUtils.stylizeTextField(txtDistractorC);
        formPanel.add(txtDistractorC, gbc);
        y++;

        // Distractor D
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Distractor D:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDistractorD = new JTextField();
        UIUtils.stylizeTextField(txtDistractorD);
        formPanel.add(txtDistractorD, gbc);
        y++;

        // Respuesta Correcta
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Respuesta Correcta:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbRespuestaCorrecta = new JComboBox<>(new String[]{"", "A", "B", "C", "D"});
        UIUtils.stylizeTextField(cmbRespuestaCorrecta);
        formPanel.add(cmbRespuestaCorrecta, gbc);
        y++;

        // Justificación
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Justificación:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtJustificacion = new JTextArea(2, 40);
        UIUtils.stylizeTextField(txtJustificacion);
        formPanel.add(new JScrollPane(txtJustificacion), gbc);
        y++;

        // Bibliografía
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Bibliografía:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtBibliografia = new JTextArea(2, 40);
        UIUtils.stylizeTextField(txtBibliografia);
        formPanel.add(new JScrollPane(txtBibliografia), gbc);
        y++;

        // Competencia
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Competencia:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbCompetencia = new JComboBox<>(new String[]{"", "Lectura Crítica", "Razonamiento Cuantitativo", "Competencias Ciudadanas"});
        cmbCompetencia.setEditable(true);
        UIUtils.stylizeTextField(cmbCompetencia);
        formPanel.add(cmbCompetencia, gbc);
        y++;

        // Tema
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Tema:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbTema = new JComboBox<>(new String[]{"", "Matemáticas", "Ciencias", "Humanidades"});
        cmbTema.setEditable(true);
        UIUtils.stylizeTextField(cmbTema);
        formPanel.add(cmbTema, gbc);
        y++;

        // Subtema
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Subtema:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbSubtema = new JComboBox<>(new String[]{"", "Álgebra", "Biología", "Historia"});
        cmbSubtema.setEditable(true);
        UIUtils.stylizeTextField(cmbSubtema);
        formPanel.add(cmbSubtema, gbc);
        y++;

        // Dificultad
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(new JLabel("Dificultad:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbDificultad = new JComboBox<>(new String[]{"", "Bajo", "Medio", "Alto"});
        UIUtils.stylizeTextField(cmbDificultad);
        formPanel.add(cmbDificultad, gbc);

        JScrollPane mainScrollPane = new JScrollPane(formPanel);
        add(mainScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnGuardar = new JButton("Guardar");
        UIUtils.stylizePrimaryButton(btnGuardar);
        btnGuardar.addActionListener(e -> guardarPregunta());
        buttonPanel.add(btnGuardar);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void guardarPregunta() {
        Pregunta p = new Pregunta();
        p.setContexto(txtContexto.getText());
        p.setPreguntaDirecta(txtPreguntaDirecta.getText());
        p.setDistractor1(txtDistractorA.getText());
        p.setDistractor2(txtDistractorB.getText());
        p.setDistractor3(txtDistractorC.getText());
        p.setDistractor4(txtDistractorD.getText());
        p.setRespuestaCorrecta((String) cmbRespuestaCorrecta.getSelectedItem());
        p.setJustificacion(txtJustificacion.getText());
        p.setBibliografia(txtBibliografia.getText());
        p.setCompetencia(cmbCompetencia.getSelectedItem() != null ? cmbCompetencia.getSelectedItem().toString() : "");
        p.setTema(cmbTema.getSelectedItem() != null ? cmbTema.getSelectedItem().toString() : "");
        p.setSubtema(cmbSubtema.getSelectedItem() != null ? cmbSubtema.getSelectedItem().toString() : "");
        p.setNivelDificultad((String) cmbDificultad.getSelectedItem());

        try {
            preguntaController.crearPregunta(p);
            JOptionPane.showMessageDialog(this, "Pregunta guardada en estado Borrador exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtContexto.setText("");
        txtPreguntaDirecta.setText("");
        txtDistractorA.setText("");
        txtDistractorB.setText("");
        txtDistractorC.setText("");
        txtDistractorD.setText("");
        cmbRespuestaCorrecta.setSelectedIndex(0);
        txtJustificacion.setText("");
        txtBibliografia.setText("");
        cmbCompetencia.setSelectedIndex(0);
        cmbTema.setSelectedIndex(0);
        cmbSubtema.setSelectedIndex(0);
        cmbDificultad.setSelectedIndex(0);
    }
}
