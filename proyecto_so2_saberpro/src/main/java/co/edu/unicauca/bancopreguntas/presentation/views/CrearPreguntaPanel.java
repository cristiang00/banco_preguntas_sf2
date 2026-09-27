package co.edu.unicauca.bancopreguntas.presentation.views;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.PreguntaBuilder;
import co.edu.unicauca.bancopreguntas.presentation.controllers.PreguntaController;
import co.edu.unicauca.bancopreguntas.presentation.utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel para crear una nueva pregunta.
 * Muestra un banner inline verde (éxito) o rojo (error) en lugar de
 * JOptionPane,
 * siguiendo el estilo del prototipo de interfaz.
 */
public class CrearPreguntaPanel extends JPanel {

    private final PreguntaController preguntaController;

    // Campos del formulario
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

    // Banner de retroalimentación inline (éxito / error)
    private JPanel bannerPanel;
    private JLabel bannerLabel;

    public CrearPreguntaPanel(PreguntaController preguntaController) {
        this.preguntaController = preguntaController;
        inicializarUI();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(UIUtils.COLOR_BACKGROUND);

        // ── Título ──────────────────────────────────────────────────────────
        JLabel lblTitulo = new JLabel("Crear Nueva Pregunta", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        lblTitulo.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // ── Banner de retroalimentación (oculto por defecto) ─────────────────
        bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        bannerPanel.setOpaque(true);
        bannerLabel = new JLabel("", SwingConstants.LEFT);
        bannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bannerPanel.add(bannerLabel, BorderLayout.CENTER);
        bannerPanel.setVisible(false);

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setOpaque(false);
        topPanel.add(lblTitulo, BorderLayout.NORTH);
        topPanel.add(bannerPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // ── Formulario ───────────────────────────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int y = 0;

        // Contexto *
        y = agregarCampoArea(formPanel, gbc, y, "Contexto: *", txtContexto = new JTextArea(3, 40), true);

        // Pregunta Directa *
        y = agregarCampoArea(formPanel, gbc, y, "Pregunta Directa: *", txtPreguntaDirecta = new JTextArea(2, 40), true);

        // Distractor A *
        txtDistractorA = new JTextField();
        y = agregarCampoField(formPanel, gbc, y, "Distractor A: *", txtDistractorA, true);

        // Distractor B *
        txtDistractorB = new JTextField();
        y = agregarCampoField(formPanel, gbc, y, "Distractor B: *", txtDistractorB, true);

        // Distractor C *
        txtDistractorC = new JTextField();
        y = agregarCampoField(formPanel, gbc, y, "Distractor C: *", txtDistractorC, true);

        // Distractor D *
        txtDistractorD = new JTextField();
        y = agregarCampoField(formPanel, gbc, y, "Distractor D: *", txtDistractorD, true);

        // Respuesta Correcta *
        cmbRespuestaCorrecta = new JComboBox<>(new String[] { "", "A", "B", "C", "D" });
        UIUtils.stylizeTextField(cmbRespuestaCorrecta);
        y = agregarCampoCombo(formPanel, gbc, y, "Respuesta Correcta: *", cmbRespuestaCorrecta, true);

        // Justificación * (HU01: campo obligatorio)
        y = agregarCampoArea(formPanel, gbc, y, "Justificación: *", txtJustificacion = new JTextArea(2, 40), true);

        // Bibliografía *
        y = agregarCampoArea(formPanel, gbc, y, "Bibliografía: *", txtBibliografia = new JTextArea(2, 40), true);

        // Competencia *
        cmbCompetencia = new JComboBox<>(
                new String[] { "", "Lectura Crítica", "Razonamiento Cuantitativo", "Competencias Ciudadanas" });
        cmbCompetencia.setEditable(true);
        UIUtils.stylizeTextField(cmbCompetencia);
        y = agregarCampoCombo(formPanel, gbc, y, "Competencia: *", cmbCompetencia, true);

        // Tema *
        cmbTema = new JComboBox<>(new String[] { "", "Matemáticas", "Ciencias", "Humanidades" });
        cmbTema.setEditable(true);
        UIUtils.stylizeTextField(cmbTema);
        y = agregarCampoCombo(formPanel, gbc, y, "Tema: *", cmbTema, true);

        // Subtema *
        cmbSubtema = new JComboBox<>(new String[] { "", "Álgebra", "Biología", "Historia" });
        cmbSubtema.setEditable(true);
        UIUtils.stylizeTextField(cmbSubtema);
        y = agregarCampoCombo(formPanel, gbc, y, "Subtema: *", cmbSubtema, true);

        // Dificultad *
        cmbDificultad = new JComboBox<>(new String[] { "", "Bajo", "Medio", "Alto" });
        UIUtils.stylizeTextField(cmbDificultad);
        agregarCampoCombo(formPanel, gbc, y, "Dificultad: *", cmbDificultad, true);

        JScrollPane mainScrollPane = new JScrollPane(formPanel);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScrollPane, BorderLayout.CENTER);

        // ── Botones ──────────────────────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton btnLimpiar = new JButton("Limpiar");
        UIUtils.stylizeSecondaryButton(btnLimpiar);
        btnLimpiar.addActionListener(e -> limpiarCampos());
        buttonPanel.add(btnLimpiar);

        JButton btnGuardar = new JButton("Guardar");
        UIUtils.stylizePrimaryButton(btnGuardar);
        btnGuardar.addActionListener(e -> guardarPregunta());
        buttonPanel.add(btnGuardar);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // ── Helpers de layout ─────────────────────────────────────────────────────

    private int agregarCampoArea(JPanel panel, GridBagConstraints gbc, int y,
            String labelText, JTextArea area, boolean obligatorio) {
        UIUtils.stylizeTextField(area);
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(crearLabel(labelText, obligatorio), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(new JScrollPane(area), gbc);
        return y + 1;
    }

    private int agregarCampoField(JPanel panel, GridBagConstraints gbc, int y,
            String labelText, JTextField field, boolean obligatorio) {
        UIUtils.stylizeTextField(field);
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(crearLabel(labelText, obligatorio), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
        return y + 1;
    }

    private int agregarCampoCombo(JPanel panel, GridBagConstraints gbc, int y,
            String labelText, JComboBox<?> combo, boolean obligatorio) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(crearLabel(labelText, obligatorio), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(combo, gbc);
        return y + 1;
    }

    /**
     * Crea un label con texto negro para obligatorios, gris para opcionales.
     */
    private JLabel crearLabel(String texto, boolean obligatorio) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(UIUtils.FONT_REGULAR);
        lbl.setForeground(obligatorio
                ? UIUtils.COLOR_TEXT_PRIMARY // negro: campo obligatorio
                : new Color(108, 117, 125)); // gris: campo opcional
        return lbl;
    }

    // ── Lógica principal ──────────────────────────────────────────────────────

    private void guardarPregunta() {
        ocultarBanner();

        Pregunta p = new PreguntaBuilder()
                .conContexto(txtContexto.getText())
                .conPreguntaDirecta(txtPreguntaDirecta.getText())
                .conDistractor1(txtDistractorA.getText())
                .conDistractor2(txtDistractorB.getText())
                .conDistractor3(txtDistractorC.getText())
                .conDistractor4(txtDistractorD.getText())
                .conRespuestaCorrecta((String) cmbRespuestaCorrecta.getSelectedItem())
                .conJustificacion(txtJustificacion.getText())
                .conBibliografia(txtBibliografia.getText())
                .conCompetencia(cmbCompetencia.getSelectedItem() != null
                        ? cmbCompetencia.getSelectedItem().toString()
                        : "")
                .conTema(cmbTema.getSelectedItem() != null
                        ? cmbTema.getSelectedItem().toString()
                        : "")
                .conSubtema(cmbSubtema.getSelectedItem() != null
                        ? cmbSubtema.getSelectedItem().toString()
                        : "")
                .conNivelDificultad((String) cmbDificultad.getSelectedItem())
                .build();

        try {
            preguntaController.crearPregunta(p);
            mostrarBannerExito(" Pregunta guardada correctamente en estado \"En borrador\"");
            limpiarCampos();
        } catch (IllegalArgumentException ex) {
            mostrarBannerError("✕  " + ex.getMessage());
        }
    }

    private void mostrarBannerExito(String mensaje) {
        bannerLabel.setText(mensaje);
        bannerLabel.setForeground(new Color(21, 87, 36)); // verde oscuro
        bannerPanel.setBackground(new Color(212, 237, 218)); // verde claro
        bannerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(195, 230, 203), 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        bannerPanel.setVisible(true);
        revalidate();
        repaint();
        // Auto-ocultar a los 5 segundos
        Timer t = new Timer(5000, e -> ocultarBanner());
        t.setRepeats(false);
        t.start();
    }

    private void mostrarBannerError(String mensaje) {
        bannerLabel.setText(mensaje);
        bannerLabel.setForeground(new Color(114, 28, 36)); // rojo oscuro
        bannerPanel.setBackground(new Color(248, 215, 218)); // rojo claro
        bannerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(245, 198, 203), 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        bannerPanel.setVisible(true);
        revalidate();
        repaint();
    }

    private void ocultarBanner() {
        bannerPanel.setVisible(false);
        revalidate();
        repaint();
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
