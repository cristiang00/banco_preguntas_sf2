package co.edu.unicauca.bancopreguntas.frontend.utils;

import co.edu.unicauca.bancopreguntas.frontend.client.ApiException;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

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

    /**
     * Convierte cualquier excepción técnica o error HTTP en un mensaje claro para el usuario final,
     * sin códigos numéricos técnicos (401, 403, 409, 500) ni símbolos de corchetes.
     */
    public static String formatUserFriendlyError(Throwable ex, String accion) {
        Throwable cause = ex;
        while (cause instanceof ExecutionException && cause.getCause() != null) {
            cause = cause.getCause();
        }

        if (cause instanceof ApiException apiEx) {
            int code = apiEx.getStatusCode();
            String cleaned = cleanTechnicalText(apiEx.getMessage());
            String baseMessage;

            if (code == 401) {
                if (cleaned != null && !cleaned.isBlank()
                        && !cleaned.toLowerCase().contains("jwt")
                        && !cleaned.toLowerCase().contains("token")) {
                    baseMessage = cleaned;
                } else {
                    baseMessage = "Tu sesión no es válida o las credenciales ingresadas son incorrectas. Verifica tus datos o inicia sesión nuevamente.";
                }
            } else if (code == 403) {
                if (cleaned != null && !cleaned.isBlank()
                        && !cleaned.toLowerCase().contains("forbidden")
                        && !cleaned.toLowerCase().contains("jwt")) {
                    baseMessage = cleaned;
                } else {
                    baseMessage = "No tienes permisos suficientes para " + (accion != null ? accion : "realizar esta acción") + ".";
                }
            } else if (code == 404) {
                if (cleaned != null && !cleaned.isBlank() && !cleaned.toLowerCase().contains("not found")) {
                    baseMessage = cleaned;
                } else {
                    baseMessage = "No se encontró la información solicitada. Es posible que ya no esté disponible.";
                }
            } else if (code == 409) {
                baseMessage = (cleaned != null && !cleaned.isBlank())
                        ? cleaned
                        : "Esta acción no está disponible en el estado actual de la pregunta.";
            } else if (code == 400) {
                baseMessage = (cleaned != null && !cleaned.isBlank())
                        ? cleaned
                        : "Algunos datos ingresados no son válidos. Revisa el formulario e intenta nuevamente.";
            } else if (code >= 500) {
                baseMessage = "El servicio no está disponible en este momento. Intenta nuevamente en unos segundos.";
            } else {
                baseMessage = (cleaned != null && !cleaned.isBlank())
                        ? cleaned
                        : "No se pudo " + (accion != null ? accion : "completar la solicitud") + ". Intenta nuevamente.";
            }

            return code > 0 ? (baseMessage + "\nCódigo: " + code) : baseMessage;
        }

        if (cause instanceof IOException) {
            return "No fue posible conectar con el sistema. Verifica que los servicios estén activos e intenta nuevamente.";
        }

        String cleaned = cause != null ? cleanTechnicalText(cause.getMessage()) : "";
        if (cleaned == null || cleaned.isBlank()) {
            return "Ocurrió un inconveniente al " + (accion != null ? accion : "realizar esta acción") + ". Intenta nuevamente.";
        }
        return cleaned;
    }

    public static String getAlertTitle(Throwable ex) {
        Throwable cause = ex;
        while (cause instanceof ExecutionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof ApiException apiEx) {
            return switch (apiEx.getStatusCode()) {
                case 400 -> "Revisa los datos ingresados";
                case 401 -> "Acceso no autorizado";
                case 403 -> "Permisos insuficientes";
                case 404 -> "Información no encontrada";
                case 409 -> "Acción no permitida";
                default -> "Servicio no disponible";
            };
        }
        if (cause instanceof IOException) {
            return "Sin conexión con el sistema";
        }
        return "No se pudo completar la acción";
    }

    public static int getAlertMessageType(Throwable ex) {
        Throwable cause = ex;
        while (cause instanceof ExecutionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof ApiException apiEx) {
            int code = apiEx.getStatusCode();
            if (code == 400 || code == 401 || code == 403 || code == 404 || code == 409) {
                return JOptionPane.WARNING_MESSAGE;
            }
        }
        return JOptionPane.ERROR_MESSAGE;
    }

    public static void showFriendlyError(Component parent, Throwable ex, String accion) {
        String message = formatUserFriendlyError(ex, accion);
        String title = getAlertTitle(ex);
        int type = getAlertMessageType(ex);
        JOptionPane.showMessageDialog(parent, message, title, type);
    }

    private static String cleanTechnicalText(String raw) {
        if (raw == null) return "";
        String text = raw.trim();
        // Si llegó un JSON crudo, no mostrar llaves ni campos técnicos al usuario
        if (text.startsWith("{") && text.endsWith("}")) {
            return "";
        }
        // Eliminar menciones a códigos HTTP numéricos (ej: "Error 403", "(403)", "HTTP 401", "Error (409):")
        text = text.replaceAll("(?i)\\b(error\\s*)?http\\s*\\d{3}\\b", "");
        text = text.replaceAll("(?i)\\(\\s*(http\\s*)?\\d{3}\\s*\\)", "");
        text = text.replaceAll("(?i)^error\\s*\\d{3}\\s*:?\\s*", "");
        // Eliminar referencias internas de infraestructura
        text = text.replaceAll("(?i)en\\s+Keycloak", "en el sistema");
        text = text.replaceAll("(?i)por\\s+Keycloak", "por el sistema");
        text = text.replaceAll("(?i)de\\s+Keycloak", "del sistema");
        text = text.replaceAll("(?i)Keycloak/OIDC", "el servicio de autenticación");
        text = text.replaceAll("(?i)API\\s+Gateway", "el sistema");
        // Eliminar corchetes literales
        text = text.replace("[", "").replace("]", "");
        // Limpiar espacios múltiples y dos puntos sobrantes al inicio
        text = text.replaceAll("\\s{2,}", " ").replaceAll("^[:\\-\\s]+", "").trim();
        return text;
    }
}
