package co.edu.unicauca.bancopreguntas.domain.entities;

import java.util.List;

/**
 * Tipos de pregunta soportados por el Banco de Preguntas Saber PRO.
 * Cada tipo encapsula sus propias reglas de validación de opciones y respuestas correctas.
 */
public enum TipoPregunta {

    /** Selección múltiple con única respuesta. */
    SELECCION_UNICA("Selección múltiple con única respuesta", "exactamente una respuesta correcta") {
        @Override
        public boolean aceptaCantidadRespuestasCorrectas(int count) {
            return count == 1;
        }
    },

    /** Selección múltiple con múltiples respuestas. */
    SELECCION_MULTIPLE("Selección múltiple con múltiples respuestas", "al menos una respuesta correcta") {
        @Override
        public boolean aceptaCantidadRespuestasCorrectas(int count) {
            return count >= 1;
        }
    },

    /** Verdadero / Falso: opciones fijas V y F. */
    VERDADERO_FALSO("Verdadero/Falso", "exactamente una respuesta correcta (V o F)") {
        @Override
        public boolean aceptaCantidadRespuestasCorrectas(int count) {
            return count == 1;
        }

        @Override
        public boolean permiteOpcionesPersonalizadas() {
            return false;
        }

        @Override
        public List<OpcionRespuesta> opcionesPorDefecto() {
            return List.of(
                    new OpcionRespuesta(OpcionRespuesta.TRUE_KEY, "Verdadero"),
                    new OpcionRespuesta(OpcionRespuesta.FALSE_KEY, "Falso")
            );
        }
    };

    /** Número mínimo de opciones que debe tener cualquier pregunta. */
    public static final int MIN_OPCIONES = 2;

    private final String etiqueta;
    private final String reglaRespuestasCorrectas;

    TipoPregunta(String etiqueta, String reglaRespuestasCorrectas) {
        this.etiqueta = etiqueta;
        this.reglaRespuestasCorrectas = reglaRespuestasCorrectas;
    }

    public abstract boolean aceptaCantidadRespuestasCorrectas(int count);

    public boolean permiteOpcionesPersonalizadas() {
        return true;
    }

    public List<OpcionRespuesta> opcionesPorDefecto() {
        return List.of();
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getNombre() {
        return etiqueta;
    }

    public String getReglaRespuestasCorrectas() {
        return reglaRespuestasCorrectas;
    }

    // Alias para compatibilidad con el código anterior de taller08
    public boolean acceptsCorrectAnswerCount(int count) {
        return aceptaCantidadRespuestasCorrectas(count);
    }

    public boolean allowsCustomOptions() {
        return permiteOpcionesPersonalizadas();
    }

    public List<OpcionRespuesta> defaultOptions() {
        return opcionesPorDefecto();
    }

    public String getLabel() {
        return etiqueta;
    }

    public String getCorrectAnswersRule() {
        return reglaRespuestasCorrectas;
    }

    /**
     * Resuelve un TipoPregunta admitiendo nombres antiguos (SINGLE_CHOICE, etc.)
     */
    public static TipoPregunta resolver(String valor) {
        if (valor == null || valor.isBlank()) {
            return SELECCION_UNICA;
        }
        String v = valor.trim().toUpperCase();
        return switch (v) {
            case "SINGLE_CHOICE", "SELECCION_UNICA" -> SELECCION_UNICA;
            case "MULTIPLE_CHOICE", "SELECCION_MULTIPLE" -> SELECCION_MULTIPLE;
            case "TRUE_FALSE", "VERDADERO_FALSO" -> VERDADERO_FALSO;
            default -> {
                try {
                    yield TipoPregunta.valueOf(v);
                } catch (IllegalArgumentException e) {
                    yield SELECCION_UNICA;
                }
            }
        };
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
