package co.edu.unicauca.bancopreguntas.review.domain.policy;

import co.edu.unicauca.bancopreguntas.review.domain.model.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;

import java.util.List;

/**
 * Política y reglas de negocio del dominio para el proceso de Revisión por Pares (HU8).
 * Clase Java PURA sin dependencias de Spring ni JPA.
 */
public class PoliticaConsensoRevision {

    public static final int MIN_REVISORES = 1;
    public static final int MAX_REVISORES = 3;

    /**
     * Valida que la cantidad de revisores a asignar esté entre 1 y 3 (HU4).
     */
    public void validarCantidadRevisores(List<Long> revisorIds) {
        if (revisorIds == null || revisorIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un revisor (entre 1 y 3 revisores permitidos)");
        }
        if (revisorIds.size() > MAX_REVISORES) {
            throw new IllegalArgumentException("Solo se permite asignar un máximo de 3 revisores por pregunta");
        }
    }

    /**
     * Valida que el autor de la pregunta no sea asignado como revisor (HU4).
     */
    public void validarExclusionAutor(Long autorId, List<Long> revisorIds) {
        if (autorId != null && revisorIds != null && revisorIds.contains(autorId)) {
            throw new IllegalArgumentException("El autor de la pregunta no puede ser asignado como revisor de su propia pregunta");
        }
    }

    /**
     * Valida que al rechazar exista al menos una observación no vacía (HU5).
     */
    public void validarRechazoConObservaciones(List<String> observacionesNuevas, boolean tieneObservacionesPrevias) {
        boolean tieneEnRequest = observacionesNuevas != null && observacionesNuevas.stream()
                .anyMatch(obs -> obs != null && !obs.trim().isEmpty());
        if (!tieneEnRequest && !tieneObservacionesPrevias) {
            throw new IllegalArgumentException("Para rechazar la pregunta es obligatorio registrar al menos una observación justificando el motivo");
        }
    }

    /**
     * Valida que la revisión pertenezca al revisor autenticado (HU5).
     */
    public void validarAsignacionRevisor(Revision revision, Long revisorIdAutenticado) {
        if (revision == null || !revision.getRevisorId().equals(revisorIdAutenticado)) {
            throw new SecurityException("No tiene permisos para decidir sobre una pregunta que no le fue asignada");
        }
    }

    /**
     * Valida que no se pueda decidir nuevamente sobre una revisión finalizada (HU5).
     */
    public void validarNoReDecision(Revision revision) {
        if (revision.getEstado().estaFinalizada()) {
            throw new IllegalStateException("Esta revisión ya se encuentra finalizada y no puede ser decidida nuevamente");
        }
    }

    /**
     * Determina si se alcanzó consenso total para la aprobación de la pregunta (HU5).
     * Consenso: todas las revisiones de la pregunta deben tener estado APROBADA.
     */
    public boolean determinarConsensoAprobacion(List<Revision> todasRevisiones) {
        if (todasRevisiones == null || todasRevisiones.isEmpty()) {
            return false;
        }
        return todasRevisiones.stream().allMatch(r -> r.getEstado() == EstadoRevision.APROBADA);
    }
}

