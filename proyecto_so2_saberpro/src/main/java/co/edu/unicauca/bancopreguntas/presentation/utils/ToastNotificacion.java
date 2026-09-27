package co.edu.unicauca.bancopreguntas.presentation.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Utilidad estática para mostrar notificaciones tipo toast/snackbar no bloqueantes.
 * <p>
 * Se muestra en la esquina inferior derecha de la ventana padre y se cierra
 * automáticamente después de 3 segundos usando un {@link javax.swing.Timer}.
 * Nunca bloquea el hilo de UI.
 * </p>
 */
public class ToastNotificacion {

    private static final int DURACION_MS = 3000;

    private ToastNotificacion() {
        // Clase utilitaria, no instanciable
    }

    /**
     * Muestra un toast con el mensaje dado.
     *
     * @param padre   ventana padre (para posicionamiento); puede ser {@code null}
     * @param mensaje texto a mostrar
     */
    public static void mostrar(Window padre, String mensaje) {
        JWindow toast = new JWindow(padre);

        JLabel lbl = new JLabel("  " + mensaje + "  ", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(new Color(15, 23, 42));   // COLOR_TEXT_PRIMARY de UIUtils
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        toast.setContentPane(lbl);
        toast.pack();

        // Posicionar en la esquina inferior derecha de la ventana padre (o de la pantalla)
        Rectangle bounds;
        if (padre != null && padre.isVisible()) {
            bounds = padre.getBounds();
        } else {
            bounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                         .getDefaultScreenDevice()
                         .getDefaultConfiguration()
                         .getBounds();
        }
        int x = bounds.x + bounds.width  - toast.getWidth()  - 20;
        int y = bounds.y + bounds.height - toast.getHeight() - 20;
        toast.setLocation(x, y);
        toast.setVisible(true);

        // Auto-cerrar sin bloquear el EDT
        Timer timer = new Timer(DURACION_MS, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }
}
