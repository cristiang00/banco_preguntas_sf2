package co.edu.unicauca.bancopreguntas.domain.evaluation;

import co.edu.unicauca.bancopreguntas.domain.entities.OpcionRespuesta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Evaluador de respuestas del Banco de Preguntas (patrón Template Method).
 * <p>
 * El método plantilla {@link #evaluate(Pregunta, List)} es {@code final} y fija
 * el algoritmo general del proceso de evaluación BPMN:
 * <ol>
 *   <li>1. Validar respuesta (si no es válida → mostrar mensaje de error)</li>
 *   <li>2. Obtener respuestas seleccionadas</li>
 *   <li>3. Comparar con respuestas correctas</li>
 *   <li>4. Determinar resultado</li>
 *   <li>5. Generar retroalimentación</li>
 * </ol>
 */
public abstract class QuestionEvaluator {

    /** Puntaje base obtenido por una respuesta correcta. */
    public static final double FULL_SCORE = 1.0;

    /**
     * Método plantilla final — fija el flujo invariable de evaluación.
     */
    public final EvaluationResult evaluate(Pregunta question, List<String> answers) {
        Objects.requireNonNull(question, "La pregunta a evaluar es obligatoria");
        if (question.getTipo() != getSupportedType()) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " no evalúa preguntas de tipo '"
                    + question.getTipo().getEtiqueta() + "'");
        }
        try {
            validateCommon(question, answers);                                   // 1. Validar respuesta
            validate(question, answers);
        } catch (InvalidAnswerException e) {
            return handleInvalidAnswer(e);                                       // Rama No BPMN
        }

        List<String> selected = getSelectedAnswers(answers);                     // 2. Obtener seleccionadas
        boolean correct = compareWithCorrectAnswers(question, selected);         // 3. Comparar con correctas
        double score = determineResult(correct);                                 // 4. Determinar resultado
        String feedback = generateFeedback(question, selected, correct);         // 5. Generar retroalimentación
        return EvaluationResult.evaluated(correct, score, selected, question.getRespuestasCorrectas(), feedback);
    }

    /** @return tipo de pregunta que evalúa este evaluador concreto. */
    public abstract TipoPregunta getSupportedType();

    /**
     * Paso 1 (variable): reglas de validación específicas del tipo de pregunta.
     *
     * @throws InvalidAnswerException si la respuesta no es válida
     */
    protected abstract void validate(Pregunta question, List<String> answers);

    /** Paso 3 (variable): compara las respuestas seleccionadas con las correctas. */
    protected abstract boolean compareWithCorrectAnswers(Pregunta question, List<String> selected);

    /** Método sugerido por la guía de laboratorio: alias para compareWithCorrectAnswers. */
    protected boolean compareAnswers(Pregunta question, List<String> answers) {
        return compareWithCorrectAnswers(question, getSelectedAnswers(answers));
    }

    /** Paso 2 (hook): normaliza las respuestas del usuario y elimina duplicadas. */
    protected List<String> getSelectedAnswers(List<String> answers) {
        return answers.stream().map(this::normalize).distinct().toList();
    }

    /** Paso 4 (hook): calcula el puntaje según el resultado. */
    protected double determineResult(boolean correct) {
        return correct ? FULL_SCORE : 0.0;
    }

    /** Paso 5 (hook): genera la retroalimentación final. */
    protected String generateFeedback(Pregunta question, List<String> selected, boolean correct) {
        StringBuilder feedback = new StringBuilder();
        if (correct) {
            feedback.append("¡Correcto! Seleccionó: ").append(describe(question, selected)).append('.');
        } else {
            feedback.append("Incorrecto. Seleccionó: ").append(describe(question, selected))
                    .append("; la respuesta correcta es: ")
                    .append(describe(question, List.copyOf(question.getRespuestasCorrectas()))).append('.');
        }
        if (question.getJustificacion() != null && !question.getJustificacion().isBlank()) {
            feedback.append(" Justificación: ").append(question.getJustificacion());
        }
        return feedback.toString();
    }

    /** Rama No del BPMN: convierte la excepción de validación en resultado inválido. */
    protected EvaluationResult handleInvalidAnswer(InvalidAnswerException error) {
        return EvaluationResult.invalid("Respuesta no válida: " + error.getMessage());
    }

    /** Hook de normalización de una respuesta individual. */
    protected String normalize(String answer) {
        return answer.trim().toUpperCase(Locale.ROOT);
    }

    /** Describe un conjunto de claves con el texto de sus opciones. */
    protected final String describe(Pregunta question, List<String> keys) {
        return keys.stream()
                .map(key -> question.buscarOpcion(key).map(OpcionRespuesta::toString).orElse(key))
                .collect(Collectors.joining(", "));
    }

    /** Validaciones comunes a todos los tipos: hay respuestas, no vacías y existentes. */
    private void validateCommon(Pregunta question, List<String> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new InvalidAnswerException("debe seleccionar al menos una respuesta.");
        }
        for (String answer : answers) {
            if (answer == null || answer.isBlank()) {
                throw new InvalidAnswerException("hay respuestas vacías.");
            }
            if (question.buscarOpcion(normalize(answer)).isEmpty()) {
                throw new InvalidAnswerException("la opción '" + answer.trim() + "' no existe en la pregunta.");
            }
        }
    }
}
