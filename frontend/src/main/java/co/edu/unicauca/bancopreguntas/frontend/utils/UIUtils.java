package co.edu.unicauca.bancopreguntas.frontend.utils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UIUtils {

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);    // Azul 600
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color COLOR_SECONDARY = new Color(100, 116, 139); // Slate 500
    public static final Color COLOR_BACKGROUND = new Color(248, 250, 252);
    public static final Color COLOR_SURFACE = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240);
    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    public static final Color COLOR_ERROR = new Color(220, 38, 38);
    public static final Color COLOR_SUCCESS = new Color(22, 163, 74);

    public static final Border BORDER_DEFAULT = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
    );

    public static final Border BORDER_ERROR = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ERROR, 2),
            BorderFactory.createEmptyBorder(5, 7, 5, 7)
    );

    public static void stylizePrimaryButton(JButton btn) {
        btn.setFont(FONT_SUBTITLE);
        btn.setBackground(COLOR_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    public static void stylizeSecondaryButton(JButton btn) {
        btn.setFont(FONT_REGULAR);
        btn.setBackground(Color.WHITE);
        btn.setForeground(COLOR_TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
    }

    public static void stylizeTextField(JComponent comp) {
        comp.setFont(FONT_REGULAR);
        comp.setBorder(BORDER_DEFAULT);
    }
}
