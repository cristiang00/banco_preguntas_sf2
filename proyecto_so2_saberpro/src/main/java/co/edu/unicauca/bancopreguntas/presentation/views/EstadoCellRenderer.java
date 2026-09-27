package co.edu.unicauca.bancopreguntas.presentation.views;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class EstadoCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof EstadoPregunta) {
            EstadoPregunta estado = (EstadoPregunta) value;
            setText(estado.getLabel());
            if (!isSelected) {
                c.setBackground(estado.getColor());
                c.setForeground(estado.getTextColor());
            }
        }

        setHorizontalAlignment(SwingConstants.CENTER);
        return c;
    }
}
