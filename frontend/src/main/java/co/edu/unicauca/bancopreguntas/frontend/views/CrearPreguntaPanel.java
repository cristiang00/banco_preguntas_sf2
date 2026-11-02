package co.edu.unicauca.bancopreguntas.frontend.views;

import co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.OpcionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiException;
import co.edu.unicauca.bancopreguntas.frontend.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CrearPreguntaPanel extends JPanel {

    private final JTextArea txtContexto;
    private final JTextArea txtPreguntaDirecta;
    private final JTextField txtOpcionA;
    private final JTextField txtOpcionB;
    private final JTextField txtOpcionC;
    private final JTextField txtOpcionD;
    private final JComboBox<String> cmbRespuestaCorrecta;
    private final JTextArea txtJustificacion;
    private final JTextArea txtBibliografia;
    private final JComboBox<String> cmbCompetencia;
    private final JComboBox<String> cmbTema;
    private final JComboBox<String> cmbSubtema;
    private final JComboBox<String> cmbDificultad;

    private final JPanel bannerPanel;
    private final JLabel bannerLabel;
    private final JButton btnGuardar;
    private final JButton btnLimpiar;

    public CrearPreguntaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Banner de retroalimentación
        bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        bannerLabel = new JLabel("", SwingConstants.LEFT);
        bannerLabel.setFont(UIUtils.FONT_SUBTITLE);
        bannerPanel.add(bannerLabel, BorderLayout.CENTER);
        bannerPanel.setVisible(false);

        JLabel lblTitulo = new JLabel("Crear Nueva Pregunta (Borrador)", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARY);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setOpaque(false);
        topPanel.add(lblTitulo, BorderLayout.NORTH);
        topPanel.add(bannerPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIUtils.COLOR_SURFACE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int y = 0;

        txtContexto = new JTextArea(3, 30);
        txtContexto.setLineWrap(true);
        txtContexto.setWrapStyleWord(true);
        UIUtils.stylizeTextField(txtContexto);
        y = addInputRow(formPanel, gbc, y, "Contexto / Enunciado: *", new JScrollPane(txtContexto));

        txtPreguntaDirecta = new JTextArea(2, 30);
        txtPreguntaDirecta.setLineWrap(true);
        txtPreguntaDirecta.setWrapStyleWord(true);
        UIUtils.stylizeTextField(txtPreguntaDirecta);
        y = addInputRow(formPanel, gbc, y, "Pregunta Directa: *", new JScrollPane(txtPreguntaDirecta));

        txtOpcionA = new JTextField();
        UIUtils.stylizeTextField(txtOpcionA);
        y = addInputRow(formPanel, gbc, y, "Opción A: *", txtOpcionA);

        txtOpcionB = new JTextField();
        UIUtils.stylizeTextField(txtOpcionB);
        y = addInputRow(formPanel, gbc, y, "Opción B: *", txtOpcionB);

        txtOpcionC = new JTextField();
        UIUtils.stylizeTextField(txtOpcionC);
        y = addInputRow(formPanel, gbc, y, "Opción C: *", txtOpcionC);

        txtOpcionD = new JTextField();
        UIUtils.stylizeTextField(txtOpcionD);
        y = addInputRow(formPanel, gbc, y, "Opción D: *", txtOpcionD);

        cmbRespuestaCorrecta = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        UIUtils.stylizeTextField(cmbRespuestaCorrecta);
        y = addInputRow(formPanel, gbc, y, "Respuesta Correcta: *", cmbRespuestaCorrecta);

        txtJustificacion = new JTextArea(2, 30);
        txtJustificacion.setLineWrap(true);
        txtJustificacion.setWrapStyleWord(true);
        UIUtils.stylizeTextField(txtJustificacion);
        y = addInputRow(formPanel, gbc, y, "Justificación Pedagógica: *", new JScrollPane(txtJustificacion));

        txtBibliografia = new JTextArea(2, 30);
        txtBibliografia.setLineWrap(true);
        txtBibliografia.setWrapStyleWord(true);
        UIUtils.stylizeTextField(txtBibliografia);
        y = addInputRow(formPanel, gbc, y, "Bibliografía / Referencias: *", new JScrollPane(txtBibliografia));

        cmbCompetencia = new JComboBox<>(new String[]{
                "Comunicación Escrita", "Razonamiento Cuantitativo", "Lectura Crítica",
                "Competencias Ciudadanas", "Inglés", "Diseño de Software"
        });
        UIUtils.stylizeTextField(cmbCompetencia);
        y = addInputRow(formPanel, gbc, y, "Competencia: *", cmbCompetencia);

        cmbTema = new JComboBox<>(new String[]{
                "Arquitectura y Patrones", "Algoritmos y Estructuras", "Bases de Datos",
                "Pruebas y Calidad", "Estadística y Probabilidad"
        });
        UIUtils.stylizeTextField(cmbTema);
        y = addInputRow(formPanel, gbc, y, "Tema: *", cmbTema);

        cmbSubtema = new JComboBox<>(new String[]{
                "Patrones GoF", "Microservicios", "Eventos y Mensajería", "Refactorización", "Modelado Relacional"
        });
        UIUtils.stylizeTextField(cmbSubtema);
        y = addInputRow(formPanel, gbc, y, "Subtema: *", cmbSubtema);

        cmbDificultad = new JComboBox<>(new String[]{"Bajo", "Medio", "Alto"});
        UIUtils.stylizeTextField(cmbDificultad);
        y = addInputRow(formPanel, gbc, y, "Nivel de Dificultad: *", cmbDificultad);

        JScrollPane scrollCenter = new JScrollPane(formPanel);
        scrollCenter.setBorder(BorderFactory.createEmptyBorder());
        add(scrollCenter, BorderLayout.CENTER);

        // Botones inferiores
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        btnLimpiar = new JButton("Limpiar Formulario");
        UIUtils.stylizeSecondaryButton(btnLimpiar);
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        btnGuardar = new JButton("Guardar Pregunta");
        UIUtils.stylizePrimaryButton(btnGuardar);
        btnGuardar.addActionListener(e -> guardarPregunta());

        btnPanel.add(btnLimpiar);
        btnPanel.add(btnGuardar);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private int addInputRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.25;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_SUBTITLE);
        lbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        panel.add(comp, gbc);
        return row + 1;
    }

    private void limpiarFormulario() {
        txtContexto.setText("");
        txtPreguntaDirecta.setText("");
        txtOpcionA.setText("");
        txtOpcionB.setText("");
        txtOpcionC.setText("");
        txtOpcionD.setText("");
        txtJustificacion.setText("");
        txtBibliografia.setText("");
        cmbRespuestaCorrecta.setSelectedIndex(0);
        cmbDificultad.setSelectedIndex(0);
        resetBorders();
        bannerPanel.setVisible(false);
    }

    private void resetBorders() {
        txtContexto.setBorder(UIUtils.BORDER_DEFAULT);
        txtPreguntaDirecta.setBorder(UIUtils.BORDER_DEFAULT);
        txtOpcionA.setBorder(UIUtils.BORDER_DEFAULT);
        txtOpcionB.setBorder(UIUtils.BORDER_DEFAULT);
        txtOpcionC.setBorder(UIUtils.BORDER_DEFAULT);
        txtOpcionD.setBorder(UIUtils.BORDER_DEFAULT);
        txtJustificacion.setBorder(UIUtils.BORDER_DEFAULT);
        txtBibliografia.setBorder(UIUtils.BORDER_DEFAULT);
    }

    private void guardarPregunta() {
        resetBorders();
        bannerPanel.setVisible(false);

        // Construcción del DTO
        List<OpcionDTO> opciones = new ArrayList<>();
        String respCorrecta = (String) cmbRespuestaCorrecta.getSelectedItem();

        opciones.add(new OpcionDTO("A", txtOpcionA.getText().trim()));
        opciones.add(new OpcionDTO("B", txtOpcionB.getText().trim()));
        opciones.add(new OpcionDTO("C", txtOpcionC.getText().trim()));
        opciones.add(new OpcionDTO("D", txtOpcionD.getText().trim()));

        PreguntaDTO dto = new PreguntaDTO();
        dto.setContexto(txtContexto.getText().trim());
        dto.setPreguntaDirecta(txtPreguntaDirecta.getText().trim());
        dto.setOpciones(opciones);
        dto.setRespuestaCorrecta(respCorrecta);
        dto.setJustificacion(txtJustificacion.getText().trim());
        dto.setBibliografia(txtBibliografia.getText().trim());
        dto.setCompetencia((String) cmbCompetencia.getSelectedItem());
        dto.setTema((String) cmbTema.getSelectedItem());
        dto.setSubtema((String) cmbSubtema.getSelectedItem());
        dto.setNivelDificultad((String) cmbDificultad.getSelectedItem());
        dto.setEstado("BORRADOR");

        btnGuardar.setEnabled(false);

        SwingUtilities.invokeLater(() -> {
            try {
                PreguntaDTO guardada = ApiClient.getInstance().crearPregunta(dto);
                mostrarBannerExito("Pregunta #" + guardada.getId() + " creada exitosamente en estado BORRADOR.");
                limpiarFormulario();
            } catch (ApiException ex) {
                mostrarErroresValidacion(ex);
            } catch (Exception ex) {
                mostrarBannerError("Error comunicándose con el API Gateway: " + ex.getMessage());
            } finally {
                btnGuardar.setEnabled(true);
            }
        });
    }

    private void mostrarBannerExito(String mensaje) {
        bannerPanel.setBackground(new Color(220, 252, 231));
        bannerLabel.setForeground(new Color(22, 101, 52));
        bannerLabel.setText("✓ " + mensaje);
        bannerPanel.setVisible(true);
    }

    private void mostrarBannerError(String mensaje) {
        bannerPanel.setBackground(new Color(254, 226, 226));
        bannerLabel.setForeground(new Color(153, 27, 27));
        bannerLabel.setText("✗ " + mensaje);
        bannerPanel.setVisible(true);
    }

    private void mostrarErroresValidacion(ApiException ex) {
        StringBuilder sb = new StringBuilder("Errores de validación detectados:\n");
        List<ErrorValidacionDTO> errores = ex.getErroresValidacion();

        if (errores.isEmpty()) {
            sb.append(ex.getMessage());
        } else {
            for (ErrorValidacionDTO err : errores) {
                sb.append("• ").append(err.getMensaje()).append("\n");
                highlightField(err.getCodigo(), err.getCampo());
            }
        }
        mostrarBannerError(sb.toString().trim());
    }

    private void highlightField(String codigo, String campo) {
        if (campo == null) return;
        switch (campo.toLowerCase()) {
            case "contexto": txtContexto.setBorder(UIUtils.BORDER_ERROR); break;
            case "preguntadirecta": txtPreguntaDirecta.setBorder(UIUtils.BORDER_ERROR); break;
            case "bibliografia": txtBibliografia.setBorder(UIUtils.BORDER_ERROR); break;
            case "justificacion": txtJustificacion.setBorder(UIUtils.BORDER_ERROR); break;
            case "opciona": txtOpcionA.setBorder(UIUtils.BORDER_ERROR); break;
            case "opcionb": txtOpcionB.setBorder(UIUtils.BORDER_ERROR); break;
            case "opcionc": txtOpcionC.setBorder(UIUtils.BORDER_ERROR); break;
            case "opciond": txtOpcionD.setBorder(UIUtils.BORDER_ERROR); break;
        }
    }
}
