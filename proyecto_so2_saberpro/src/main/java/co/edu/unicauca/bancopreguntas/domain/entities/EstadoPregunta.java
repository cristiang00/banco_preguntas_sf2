package co.edu.unicauca.bancopreguntas.domain.entities;

import java.awt.Color;

/**
 * Enum que representa los posibles estados de una pregunta.
 */
public enum EstadoPregunta {
    BORRADOR("En borrador", new Color(108, 117, 125)),
    PENDIENTE_REVISION("Pendiente de revisión", new Color(255, 193, 7)),
    EN_REVISION("En revisión", new Color(13, 110, 253)),
    APROBADA("Aprobada", new Color(25, 135, 84)),
    RECHAZADA("Rechazada", new Color(220, 53, 69)),
    PUBLICADA("Publicada", new Color(111, 66, 193)),
    ARCHIVADA("Archivada", new Color(173, 181, 189));

    private final String label;
    private final Color color;

    EstadoPregunta(String label, Color color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Calcula el color de texto con contraste adecuado según la luminancia del fondo.
     * Texto oscuro sobre fondos claros, texto blanco sobre fondos oscuros
     * (equivalente a WCAG AA ~4.5:1 para los valores usados).
     *
     * @return {@code Color.WHITE} o gris oscuro {@code #212529}
     */
    public Color getTextColor() {
        double luminancia = (0.299 * color.getRed()
                           + 0.587 * color.getGreen()
                           + 0.114 * color.getBlue()) / 255.0;
        return luminancia > 0.6 ? new Color(33, 37, 41) : Color.WHITE;
    }

    @Override
    public String toString() {
        return label;
    }
}
