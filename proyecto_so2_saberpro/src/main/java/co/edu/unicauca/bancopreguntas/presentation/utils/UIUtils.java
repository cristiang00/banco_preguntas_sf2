package co.edu.unicauca.bancopreguntas.presentation.utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIUtils {
    public static final Color COLOR_PRIMARY = new Color(37, 99, 235); // Azul vibrante
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color COLOR_SECONDARY = new Color(100, 116, 139); // Gris azulado
    public static final Color COLOR_SECONDARY_HOVER = new Color(71, 85, 105);
    public static final Color COLOR_BACKGROUND = new Color(248, 250, 252); // Gris muy claro
    public static final Color COLOR_SURFACE = Color.WHITE;
    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);
    
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);

    public static void stylizePrimaryButton(JButton button) {
        stylizeButton(button, COLOR_PRIMARY, COLOR_PRIMARY_HOVER, Color.WHITE);
    }

    public static void stylizeSecondaryButton(JButton button) {
        stylizeButton(button, COLOR_SECONDARY, COLOR_SECONDARY_HOVER, Color.WHITE);
    }

    private static void stylizeButton(JButton button, Color bgNormal, Color bgHover, Color fg) {
        button.setBackground(bgNormal);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgNormal);
            }
        });
    }

    public static void stylizeSidebarButton(JButton button) {
        button.setBackground(COLOR_TEXT_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(30, 41, 59));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(COLOR_TEXT_PRIMARY);
            }
        });
    }

    public static void stylizeTextField(JComponent textField) {
        textField.setFont(FONT_REGULAR);
        Border line = new LineBorder(new Color(203, 213, 225), 1, true);
        Border empty = new EmptyBorder(8, 10, 8, 10);
        textField.setBorder(new CompoundBorder(line, empty));
        
        if (textField instanceof JTextField || textField instanceof JTextArea) {
            textField.setBackground(Color.WHITE);
        }
    }
}
