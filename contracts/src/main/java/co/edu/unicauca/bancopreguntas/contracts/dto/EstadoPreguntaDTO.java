package co.edu.unicauca.bancopreguntas.contracts.dto;

import java.io.Serializable;

/**
 * Enumeración canónica de estados y colores del ciclo de vida de una pregunta (HU2 - SSCRUM-42, SSCRUM-44).
 */
public enum EstadoPreguntaDTO implements Serializable {
    BORRADOR("Borrador", "#6C757D"),
    PENDIENTE_REVISION("Pendiente de revisión", "#FFC107"),
    APROBADA("Aprobada", "#198754"),
    RECHAZADA("Rechazada", "#DC3545");

    private final String label;
    private final String colorHex;

    EstadoPreguntaDTO(String label, String colorHex) {
        this.label = label;
        this.colorHex = colorHex;
    }

    public String getLabel() {
        return label;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static EstadoPreguntaDTO fromString(String str) {
        if (str == null) return BORRADOR;
        try {
            return EstadoPreguntaDTO.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            for (EstadoPreguntaDTO eDto : values()) {
                if (eDto.label.equalsIgnoreCase(str.trim())) {
                    return eDto;
                }
            }
            return BORRADOR;
        }
    }
}

