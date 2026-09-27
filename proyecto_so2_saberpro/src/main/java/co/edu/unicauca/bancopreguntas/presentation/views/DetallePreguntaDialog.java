package co.edu.unicauca.bancopreguntas.presentation.views;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import co.edu.unicauca.bancopreguntas.presentation.utils.UIUtils;

public class DetallePreguntaDialog extends JDialog {
    public DetallePreguntaDialog(java.awt.Window parent, Pregunta pregunta) {
        super(parent, "Detalle de la Pregunta", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        inicializarUI(pregunta);
    }

    private void inicializarUI(Pregunta pregunta) {
        setSize(500, 600);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("ID: " + pregunta.getId() + " - " + pregunta.getEstado().getLabel(), JLabel.CENTER);
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARY);
        mainPanel.add(lblTitulo, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;
        addCampoTexto(infoPanel, gbc, "Contexto:", pregunta.getContexto(), y++);
        addCampoTexto(infoPanel, gbc, "Pregunta:", pregunta.getPreguntaDirecta(), y++);
        addCampoTexto(infoPanel, gbc, "Distractor A:", pregunta.getDistractor1(), y++);
        addCampoTexto(infoPanel, gbc, "Distractor B:", pregunta.getDistractor2(), y++);
        addCampoTexto(infoPanel, gbc, "Distractor C:", pregunta.getDistractor3(), y++);
        addCampoTexto(infoPanel, gbc, "Distractor D:", pregunta.getDistractor4(), y++);
        addCampoTexto(infoPanel, gbc, "Respuesta:", pregunta.getRespuestaCorrecta(), y++);
        addCampoTexto(infoPanel, gbc, "Justificación:", pregunta.getJustificacion(), y++);
        addCampoTexto(infoPanel, gbc, "Bibliografía:", pregunta.getBibliografia(), y++);
        addCampoTexto(infoPanel, gbc, "Competencia:", pregunta.getCompetencia(), y++);
        addCampoTexto(infoPanel, gbc, "Tema:", pregunta.getTema(), y++);
        addCampoTexto(infoPanel, gbc, "Subtema:", pregunta.getSubtema(), y++);
        addCampoTexto(infoPanel, gbc, "Dificultad:", pregunta.getNivelDificultad(), y++);

        JScrollPane scrollPane = new JScrollPane(infoPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void addCampoTexto(JPanel panel, GridBagConstraints gbc, String label, String text, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_SUBTITLE);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextArea area = new JTextArea(text);
        area.setFont(UIUtils.FONT_REGULAR);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        panel.add(area, gbc);
    }
}
