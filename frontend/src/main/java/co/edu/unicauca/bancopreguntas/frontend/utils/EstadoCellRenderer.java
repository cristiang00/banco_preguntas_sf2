package co.edu.unicauca.bancopreguntas.frontend.utils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class EstadoCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String estado = value != null ? value.toString() : "";

        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(UIUtils.FONT_SUBTITLE);

        if (!isSelected) {
            switch (estado.toUpperCase()) {
                case "BORRADOR":
                    c.setBackground(new Color(254, 240, 138)); // Amarillo claro
                    c.setForeground(new Color(133, 77, 14));   // Amarillo texto oscuro
                    setText("Borrador");
                    break;
                case "PENDIENTE_REVISION":
                case "PENDIENTE DE REVISIÓN":
                case "EN_REVISION":
                    c.setBackground(new Color(219, 234, 254)); // Azul claro
                    c.setForeground(new Color(30, 64, 175));   // Azul texto
                    setText("Pendiente de Revisión");
                    break;
                case "APROBADA":
                    c.setBackground(new Color(220, 252, 231)); // Verde claro
                    c.setForeground(new Color(22, 101, 52));   // Verde texto
                    setText("Aprobada");
                    break;
                case "RECHAZADA":
                    c.setBackground(new Color(254, 226, 226)); // Rojo claro
                    c.setForeground(new Color(153, 27, 27));   // Rojo texto
                    setText("Rechazada");
                    break;
                default:
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    setText(estado);
                    break;
            }
        }
        return c;
    }
}
