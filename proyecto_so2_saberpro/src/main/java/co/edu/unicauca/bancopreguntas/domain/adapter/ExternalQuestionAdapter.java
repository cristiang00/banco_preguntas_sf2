package co.edu.unicauca.bancopreguntas.domain.adapter;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adaptador que transforma la representación externa en JSON al modelo interno {@link Pregunta}.
 * Los distractores se generan de forma configurable a través del constructor.
 */
public class ExternalQuestionAdapter implements QuestionProvider {

    /** Distractores por defecto para complementar preguntas importadas. */
    public static final List<String> DEFAULT_DISTRACTORS = List.of("Medellín", "Cali", "Barranquilla");

    private static final Pattern STATEMENT_PATTERN = Pattern.compile("\"statement\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern ANSWER_PATTERN = Pattern.compile("\"answer\"\\s*:\\s*\"([^\"]*)\"");

    private final ExternalQuestionService service;
    private final List<String> distractors;

    public ExternalQuestionAdapter(ExternalQuestionService service) {
        this(service, DEFAULT_DISTRACTORS);
    }

    public ExternalQuestionAdapter(ExternalQuestionService service, List<String> distractors) {
        this.service = Objects.requireNonNull(service, "El servicio externo es obligatorio");
        Objects.requireNonNull(distractors, "La lista de distractores no puede ser nula");
        if (distractors.isEmpty()) {
            throw new IllegalArgumentException("Debe suministrar al menos un distractor para la pregunta");
        }
        this.distractors = List.copyOf(distractors);
    }

    @Override
    public Pregunta getQuestion() {
        String data = service.getQuestion();
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("El servicio externo no retornó datos");
        }

        String statement = extractField(STATEMENT_PATTERN, data, "statement");
        String answer = extractField(ANSWER_PATTERN, data, "answer");

        Pregunta pregunta = new Pregunta(statement, TipoPregunta.SELECCION_UNICA);
        pregunta.setContexto("Pregunta importada desde servicio externo institucional");
        pregunta.setJustificacion("Respuesta correcta importada: " + answer);
        pregunta.setBibliografia("Banco institucional externo");
        pregunta.setCompetencia("Lectura Crítica");
        pregunta.setTema("Ciencias");
        pregunta.setSubtema("General");
        pregunta.setNivelDificultad("Medio");
        pregunta.setEstado(EstadoPregunta.BORRADOR);

        // La opción A corresponde a la respuesta correcta provista externamente
        pregunta.addOpcion("A", answer);

        // Opciones distractoras generadas a partir de la lista configurable
        char keyChar = 'B';
        for (String distractor : distractors) {
            if (distractor.trim().equalsIgnoreCase(answer.trim())) {
                continue; // Evitar duplicar
            }
            pregunta.addOpcion(String.valueOf(keyChar), distractor.trim());
            keyChar++;
        }

        pregunta.definirRespuestasCorrectas("A");
        return pregunta;
    }

    private String extractField(Pattern pattern, String json, String fieldName) {
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("El JSON no contiene el campo obligatorio '" + fieldName + "'");
        }
        String value = matcher.group(1).trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("El campo '" + fieldName + "' no puede estar vacío");
        }
        return value;
    }
}
