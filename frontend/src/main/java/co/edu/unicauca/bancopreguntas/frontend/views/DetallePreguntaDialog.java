package co.edu.unicauca.bancopreguntas.frontend.views;

import co.edu.unicauca.bancopreguntas.contracts.dto.OpcionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DetallePreguntaDialog extends JDialog {

    private final PreguntaDTO pregunta;
    private final Runnable onActualizado;

    public DetallePreguntaDialog(Window parent, PreguntaDTO pregunta, Runnable onActualizado) {
        super(parent, "Detalle de Pregunta #" + pregunta.getId(), ModalityType.APPLICATION_MODAL);
        this.pregunta = pregunta;
        this.onActualizado = onActualizado;
        inicializarUI();
    }

    private void inicializarUI() {
        setSize(600, 720);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(UIUtils.COLOR_SURFACE);

        String estadoLabel = pregunta.getEstado() != null ? pregunta.getEstado() : "BORRADOR";
        JLabel lblTitulo = new JLabel("Pregunta #" + pregunta.getId() + " - [" + estadoLabel + "]",
                SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        lblTitulo.setForeground(UIUtils.COLOR_PRIMARY);
        mainPanel.add(lblTitulo, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(UIUtils.COLOR_SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int y = 0;
        addCampo(infoPanel, gbc, y++, "Contexto:", pregunta.getContexto());
        addCampo(infoPanel, gbc, y++, "Pregunta Directa:", pregunta.getPreguntaDirecta());

        // Opciones
        StringBuilder sbOpciones = new StringBuilder();
        if (pregunta.getOpciones() != null && !pregunta.getOpciones().isEmpty()) {
            for (OpcionDTO op : pregunta.getOpciones()) {
                boolean esCorrecta = op.getClave() != null
                        && op.getClave().equalsIgnoreCase(pregunta.getRespuestaCorrecta());
                sbOpciones.append(op.getClave()).append(") ").append(op.getTexto())
                        .append(esCorrecta ? " [CORRECTA]" : "").append("\n");
            }
        }
        addCampo(infoPanel, gbc, y++, "Opciones de Respuesta:", sbOpciones.toString().trim());
        addCampo(infoPanel, gbc, y++, "Respuesta Correcta:", pregunta.getRespuestaCorrecta());
        addCampo(infoPanel, gbc, y++, "Justificación:", pregunta.getJustificacion());
        addCampo(infoPanel, gbc, y++, "Bibliografía:", pregunta.getBibliografia());
        addCampo(infoPanel, gbc, y++, "Competencia:", pregunta.getCompetencia());
        addCampo(infoPanel, gbc, y++, "Tema / Subtema:", pregunta.getTema() + " / " + pregunta.getSubtema());
        addCampo(infoPanel, gbc, y++, "Nivel Dificultad:", pregunta.getNivelDificultad());
        addCampo(infoPanel, gbc, y++, "Autor:",
                pregunta.getAutorNombre() != null ? pregunta.getAutorNombre() : ("ID: " + pregunta.getAutorId()));

        // Observaciones si fue rechazada
        if ("RECHAZADA".equalsIgnoreCase(pregunta.getEstado())) {
            List<String> obs = List.of();
            try {
                obs = ApiClient.getInstance().obtenerObservaciones(pregunta.getId());
            } catch (Exception ignored) {
            }
            String obsTexto = obs.isEmpty() ? "No se registraron notas adicionales." : String.join("\n- ", obs);
            addCampo(infoPanel, gbc, y++, "Observaciones de Revisores:", obsTexto);
        }

        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Botones inferiores
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setOpaque(false);

        if ("BORRADOR".equalsIgnoreCase(pregunta.getEstado())) {
            JButton btnEnviar = new JButton("Enviar a Revisión");
            UIUtils.stylizePrimaryButton(btnEnviar);
            btnEnviar.addActionListener(e -> enviarARevision());
            btnPanel.add(btnEnviar);
        }

        JButton btnCerrar = new JButton("Cerrar");
        UIUtils.stylizeSecondaryButton(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());
        btnPanel.add(btnCerrar);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void addCampo(JPanel panel, GridBagConstraints gbc, int row, String label, String text) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_SUBTITLE);
        lbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextArea area = new JTextArea(text != null ? text : "");
        area.setFont(UIUtils.FONT_REGULAR);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 0));
        panel.add(area, gbc);
    }

    private void enviarARevision() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de enviar la pregunta #" + pregunta.getId() + " a revisión?\n"
                        + "Una vez enviada, no podrá modificarla hasta que concluya el proceso de evaluación.",
                "Confirmar Envío",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ApiClient.getInstance().enviarARevision(pregunta.getId());
                JOptionPane.showMessageDialog(this, "Pregunta enviada a revisión exitosamente.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                if (onActualizado != null)
                    onActualizado.run();
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error enviando a revisión: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
