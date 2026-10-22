package co.edu.unicauca.bancopreguntas.presentation.views;

import co.edu.unicauca.bancopreguntas.domain.decorator.DecoratedQuestionView;
import co.edu.unicauca.bancopreguntas.domain.decorator.QuestionDecoratorFactory;
import co.edu.unicauca.bancopreguntas.domain.entities.OpcionRespuesta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.presentation.controllers.PreguntaController;
import co.edu.unicauca.bancopreguntas.presentation.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

public class DetallePreguntaDialog extends JDialog {

    private final PreguntaController preguntaController;

    public DetallePreguntaDialog(java.awt.Window parent, Pregunta pregunta) {
        this(parent, pregunta, null);
    }

    public DetallePreguntaDialog(java.awt.Window parent, Pregunta pregunta, PreguntaController preguntaController) {
        super(parent, "Detalle de la Pregunta", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        this.preguntaController = preguntaController;
        inicializarUI(pregunta);
    }

    private void inicializarUI(Pregunta pregunta) {
        setSize(580, 680);
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

        // HU 3 - CA 6: Dado que visualizo una pregunta creada por mí, el sistema muestra su información completa y las acciones disponibles según su estado
        int y = 0;
        String tipoStr = pregunta.getTipo() != null ? pregunta.getTipo().getNombre() : "Selección Única";
        addCampoTexto(infoPanel, gbc, "Tipo de Pregunta:", tipoStr, y++);
        addCampoTexto(infoPanel, gbc, "Contexto:", pregunta.getContexto(), y++);
        addCampoTexto(infoPanel, gbc, "Pregunta Directa:", pregunta.getPreguntaDirecta(), y++);

        // Opciones
        if (pregunta.getOpciones() != null && !pregunta.getOpciones().isEmpty()) {
            StringBuilder sbOpciones = new StringBuilder();
            for (OpcionRespuesta op : pregunta.getOpciones()) {
                sbOpciones.append(op.clave()).append(") ").append(op.texto()).append("\n");
            }
            addCampoTexto(infoPanel, gbc, "Opciones:", sbOpciones.toString().trim(), y++);
        } else {
            addCampoTexto(infoPanel, gbc, "Distractor A:", pregunta.getDistractor1(), y++);
            addCampoTexto(infoPanel, gbc, "Distractor B:", pregunta.getDistractor2(), y++);
            addCampoTexto(infoPanel, gbc, "Distractor C:", pregunta.getDistractor3(), y++);
            addCampoTexto(infoPanel, gbc, "Distractor D:", pregunta.getDistractor4(), y++);
        }

        // Respuestas correctas
        String respCorrectasStr = (pregunta.getRespuestasCorrectas() != null && !pregunta.getRespuestasCorrectas().isEmpty())
                ? String.join(", ", pregunta.getRespuestasCorrectas())
                : pregunta.getRespuestaCorrecta();
        addCampoTexto(infoPanel, gbc, "Respuesta(s) Correcta(s):", respCorrectasStr, y++);

        addCampoTexto(infoPanel, gbc, "Justificación:", pregunta.getJustificacion(), y++);
        addCampoTexto(infoPanel, gbc, "Bibliografía:", pregunta.getBibliografia(), y++);
        addCampoTexto(infoPanel, gbc, "Competencia:", pregunta.getCompetencia(), y++);
        addCampoTexto(infoPanel, gbc, "Tema:", pregunta.getTema(), y++);
        addCampoTexto(infoPanel, gbc, "Subtema:", pregunta.getSubtema(), y++);
        addCampoTexto(infoPanel, gbc, "Dificultad:", pregunta.getNivelDificultad(), y++);

        // ── Decorator: Características enriquecidas ──────────────────────
        DecoratedQuestionView view;
        if (preguntaController != null) {
            view = preguntaController.describirPregunta(pregunta.getId());
        } else {
            co.edu.unicauca.bancopreguntas.domain.decorator.QuestionComponent comp =
                    QuestionDecoratorFactory.decorateFromEntity(pregunta);
            view = new DecoratedQuestionView(comp.getDescription(), comp.getScore());
        }

        addCampoTexto(infoPanel, gbc, "Descripción Decorada:", view.description(), y++);
        addCampoTexto(infoPanel, gbc, "Puntaje / Score:", String.format("%.2f", view.score()), y++);

        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        UIUtils.stylizeSecondaryButton(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnCerrar);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void addCampoTexto(JPanel panel, GridBagConstraints gbc, String label, String text, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_SUBTITLE);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextArea area = new JTextArea(text != null ? text : "");
        area.setFont(UIUtils.FONT_REGULAR);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        panel.add(area, gbc);
    }
}
