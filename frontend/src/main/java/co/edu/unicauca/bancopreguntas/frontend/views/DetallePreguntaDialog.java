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

    public DetallePreguntaDialog(Window owner, PreguntaDTO pregunta, Runnable onActualizado) {
        super(owner, "Detalle de la Pregunta #" + (pregunta != null ? pregunta.getId() : ""), ModalityType.APPLICATION_MODAL);
        this.pregunta = pregunta;
        this.onActualizado = onActualizado;

        if (pregunta == null) {
            dispose();
            return;
        }

        setSize(700, 680);
        setLocationRelativeTo(owner);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Cabecera con estado y versión
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitulo = new JLabel("Pregunta #" + pregunta.getId() + " (Versión " + pregunta.getVersion() + ")");
        lblTitulo.setFont(UIUtils.FONT_TITLE);
        lblTitulo.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel lblEstado = new JLabel(" " + pregunta.getEstado() + " ");
        lblEstado.setFont(UIUtils.FONT_SUBTITLE);
        lblEstado.setOpaque(true);
        lblEstado.setBackground(UIUtils.COLOR_SECONDARY);
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        headerPanel.add(lblTitulo, BorderLayout.WEST);
        headerPanel.add(lblEstado, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Formulario / Datos
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(UIUtils.COLOR_SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int y = 0;
        addCampo(infoPanel, gbc, y++, "Versión Actual:", "v" + pregunta.getVersion());
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

        JButton btnHistorial = new JButton("Ver Historial");
        UIUtils.stylizeSecondaryButton(btnHistorial);
        btnHistorial.addActionListener(e -> {
            new HistorialRevisionesDialog((Frame) SwingUtilities.getWindowAncestor(this), pregunta.getId()).setVisible(true);
        });
        btnPanel.add(btnHistorial);

        if ("BORRADOR".equalsIgnoreCase(pregunta.getEstado())) {
            JButton btnEnviar = new JButton("Enviar a Revisión");
            UIUtils.stylizePrimaryButton(btnEnviar);
            btnEnviar.addActionListener(e -> enviarARevision());
            btnPanel.add(btnEnviar);
        }

        // HU6.S3.4: Botón "Crear versión mejorada" solo visible/habilitado cuando está Rechazada
        if ("RECHAZADA".equalsIgnoreCase(pregunta.getEstado())) {
            JButton btnNuevaVersion = new JButton("Crear versión mejorada");
            UIUtils.stylizePrimaryButton(btnNuevaVersion);
            btnNuevaVersion.addActionListener(e -> crearNuevaVersion());
            btnPanel.add(btnNuevaVersion);
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
                "¿Está seguro de enviar la pregunta #" + pregunta.getId() + " (v" + pregunta.getVersion() + ") a revisión?\n"
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

    private void crearNuevaVersion() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Desea crear una nueva versión mejorada de la pregunta #" + pregunta.getId() + "?\n"
                        + "La pregunta pasará al estado BORRADOR (Versión " + (pregunta.getVersion() + 1) + ") y podrá ser editada y reenviada.",
                "Confirmar Nueva Versión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreguntaDTO versionada = ApiClient.getInstance().crearNuevaVersion(pregunta.getId(), null);
                JOptionPane.showMessageDialog(this,
                        "Nueva versión " + versionada.getVersion() + " creada exitosamente en estado " + versionada.getEstado() + ".",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                if (onActualizado != null)
                    onActualizado.run();
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al crear nueva versión: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
