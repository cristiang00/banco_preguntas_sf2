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
        String estUpper = pregunta.getEstado() != null ? pregunta.getEstado().toUpperCase() : "";
        if ("APROBADA".equals(estUpper)) {
            lblEstado.setBackground(UIUtils.COLOR_SUCCESS);
        } else if ("RECHAZADA".equals(estUpper)) {
            lblEstado.setBackground(UIUtils.COLOR_ERROR);
        } else if ("PENDIENTE_REVISION".equals(estUpper)) {
            lblEstado.setBackground(new Color(217, 119, 6));
        } else {
            lblEstado.setBackground(UIUtils.COLOR_SECONDARY);
        }
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
            new HistorialRevisionesDialog(DetallePreguntaDialog.this, pregunta.getId()).setVisible(true);
        });
        btnPanel.add(btnHistorial);

        if ("BORRADOR".equalsIgnoreCase(pregunta.getEstado())) {
            JButton btnEditar = new JButton("Editar Borrador");
            UIUtils.stylizeSecondaryButton(btnEditar);
            btnEditar.addActionListener(e -> abrirEdicionBorrador());
            btnPanel.add(btnEditar);

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

    public void abrirEdicionBorrador() {
        JDialog editDialog = new JDialog(this, "Editar Borrador — Pregunta #" + pregunta.getId() + " (v" + pregunta.getVersion() + ")", true);
        editDialog.setSize(640, 620);
        editDialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        form.setBackground(UIUtils.COLOR_SURFACE);

        JTextArea txtCtx = new JTextArea(pregunta.getContexto(), 3, 25);
        txtCtx.setLineWrap(true);
        txtCtx.setWrapStyleWord(true);
        JTextArea txtDir = new JTextArea(pregunta.getPreguntaDirecta(), 2, 25);
        txtDir.setLineWrap(true);
        txtDir.setWrapStyleWord(true);

        String opA = "", opB = "", opC = "", opD = "";
        if (pregunta.getOpciones() != null) {
            for (OpcionDTO op : pregunta.getOpciones()) {
                if ("A".equalsIgnoreCase(op.getClave())) opA = op.getTexto();
                else if ("B".equalsIgnoreCase(op.getClave())) opB = op.getTexto();
                else if ("C".equalsIgnoreCase(op.getClave())) opC = op.getTexto();
                else if ("D".equalsIgnoreCase(op.getClave())) opD = op.getTexto();
            }
        }
        JTextField txtA = new JTextField(opA);
        JTextField txtB = new JTextField(opB);
        JTextField txtC = new JTextField(opC);
        JTextField txtD = new JTextField(opD);
        JComboBox<String> cmbResp = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        if (pregunta.getRespuestaCorrecta() != null) {
            cmbResp.setSelectedItem(pregunta.getRespuestaCorrecta().toUpperCase());
        }
        JTextField txtJust = new JTextField(pregunta.getJustificacion());
        JTextField txtBib = new JTextField(pregunta.getBibliografia());
        JComboBox<String> cmbDif = new JComboBox<>(new String[]{"Bajo", "Medio", "Alto"});
        if (pregunta.getNivelDificultad() != null) {
            cmbDif.setSelectedItem(pregunta.getNivelDificultad());
        }

        form.add(new JLabel("Contexto / Enunciado:"));
        form.add(new JScrollPane(txtCtx));
        form.add(new JLabel("Pregunta Directa:"));
        form.add(new JScrollPane(txtDir));
        form.add(new JLabel("Opción A:"));
        form.add(txtA);
        form.add(new JLabel("Opción B:"));
        form.add(txtB);
        form.add(new JLabel("Opción C:"));
        form.add(txtC);
        form.add(new JLabel("Opción D:"));
        form.add(txtD);
        form.add(new JLabel("Respuesta Correcta:"));
        form.add(cmbResp);
        form.add(new JLabel("Justificación:"));
        form.add(txtJust);
        form.add(new JLabel("Bibliografía:"));
        form.add(txtBib);
        form.add(new JLabel("Nivel de Dificultad:"));
        form.add(cmbDif);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnGuardar = new JButton("Guardar Cambios");
        UIUtils.stylizePrimaryButton(btnGuardar);
        btnGuardar.addActionListener(e -> {
            PreguntaDTO dto = new PreguntaDTO();
            dto.setContexto(txtCtx.getText().trim());
            dto.setPreguntaDirecta(txtDir.getText().trim());
            dto.setOpciones(List.of(
                    new OpcionDTO("A", txtA.getText().trim()),
                    new OpcionDTO("B", txtB.getText().trim()),
                    new OpcionDTO("C", txtC.getText().trim()),
                    new OpcionDTO("D", txtD.getText().trim())
            ));
            dto.setRespuestaCorrecta((String) cmbResp.getSelectedItem());
            dto.setJustificacion(txtJust.getText().trim());
            dto.setBibliografia(txtBib.getText().trim());
            dto.setCompetencia(pregunta.getCompetencia());
            dto.setTema(pregunta.getTema());
            dto.setSubtema(pregunta.getSubtema());
            dto.setNivelDificultad((String) cmbDif.getSelectedItem());
            try {
                ApiClient.getInstance().actualizarPregunta(pregunta.getId(), dto);
                JOptionPane.showMessageDialog(editDialog, "Borrador actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                editDialog.dispose();
                if (onActualizado != null) onActualizado.run();
                if (isVisible()) dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(editDialog, "Error al actualizar borrador: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton btnCancelar = new JButton("Cancelar");
        UIUtils.stylizeSecondaryButton(btnCancelar);
        btnCancelar.addActionListener(e -> editDialog.dispose());
        south.add(btnGuardar);
        south.add(btnCancelar);

        JPanel root = new JPanel(new BorderLayout());
        root.add(new JScrollPane(form), BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        editDialog.setContentPane(root);
        editDialog.setVisible(true);
    }
}
