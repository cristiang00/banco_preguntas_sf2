package co.edu.unicauca.bancopreguntas.domain.entities;

import java.util.Locale;

/**
 * Opción de respuesta de una pregunta, identificada por una clave (A, B, C... o V/F).
 *
 * @param clave clave normalizada en mayúsculas
 * @param texto texto visible de la opción
 */
public record OpcionRespuesta(String clave, String texto) {

    /** Clave de la opción "Verdadero". */
    public static final String TRUE_KEY = "V";
    /** Clave de la opción "Falso". */
    public static final String FALSE_KEY = "F";

    public OpcionRespuesta {
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("La clave de la opción es obligatoria");
        }
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto de la opción '" + clave.trim() + "' es obligatorio");
        }
        clave = normalizarClave(clave);
        texto = texto.trim();
    }

    /** Normaliza una clave de opción (sin espacios y en mayúsculas). */
    public static String normalizarClave(String key) {
        return key.trim().toUpperCase(Locale.ROOT);
    }

    // Métodos alias para compatibilidad con taller08 (AnswerOption)
    public String key() {
        return clave;
    }

    public String text() {
        return texto;
    }

    @Override
    public String toString() {
        return clave + ") " + texto;
    }
}
