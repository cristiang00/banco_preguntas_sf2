package co.edu.unicauca.bancopreguntas.domain.facade;

import co.edu.unicauca.bancopreguntas.domain.adapter.ExternalQuestionAdapter;
import co.edu.unicauca.bancopreguntas.domain.adapter.ExternalQuestionService;
import co.edu.unicauca.bancopreguntas.domain.adapter.QuestionProvider;
import co.edu.unicauca.bancopreguntas.domain.decorator.*;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;
import co.edu.unicauca.bancopreguntas.domain.evaluation.EvaluationResult;
import co.edu.unicauca.bancopreguntas.domain.evaluation.QuestionEvaluator;
import co.edu.unicauca.bancopreguntas.domain.evaluation.QuestionEvaluatorRegistry;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;
import co.edu.unicauca.bancopreguntas.domain.services.PreguntaService;
import co.edu.unicauca.bancopreguntas.dataaccess.PreguntaRepositorySQLite;
import com.unicauca.taller2.usuarios.access.ConexionSQLite;

import java.util.*;

/**
 * Fachada principal del Banco de Preguntas Saber PRO (patrón Facade).
 * Simplifica la interacción con todos los subsistemas del banco:
 * validación, persistencia, publicación, ciclo de vida, decoradores,
 * evaluadores de respuestas e importación de fuentes externas.
 */
public class QuestionBankFacade {

    private final PreguntaService questionService;
    private final QuestionValidator validator;
    private final QuestionPublisher publisher;
    private final PreguntaRepository repository;
    private final QuestionEvaluatorRegistry evaluatorRegistry;
    private final QuestionImporter importer;

    private final Map<Integer, QuestionComponent> decoratedQuestions = new HashMap<>();

    /**
     * Constructor con inyección de subsistemas por constructor (recomendado).
     */
    public QuestionBankFacade(PreguntaService questionService,
                              QuestionValidator validator,
                              QuestionPublisher publisher,
                              PreguntaRepository repository,
                              QuestionEvaluatorRegistry evaluatorRegistry,
                              QuestionImporter importer) {
        this.questionService = Objects.requireNonNull(questionService, "PreguntaService es obligatorio");
        this.validator = Objects.requireNonNull(validator, "QuestionValidator es obligatorio");
        this.publisher = Objects.requireNonNull(publisher, "QuestionPublisher es obligatorio");
        this.repository = repository;
        this.evaluatorRegistry = evaluatorRegistry != null ? evaluatorRegistry : QuestionEvaluatorRegistry.withDefaults();
        this.importer = importer != null ? importer : new QuestionImporter();
    }

    /**
     * Constructor alineado con la rúbrica y tests unitarios básicos.
     */
    public QuestionBankFacade(PreguntaService questionService,
                              QuestionValidator validator,
                              QuestionPublisher publisher) {
        this(questionService, validator, publisher, null, QuestionEvaluatorRegistry.withDefaults(), new QuestionImporter());
    }

    /**
     * Ensamblaje en memoria para pruebas y desacoplamiento.
     */
    public static QuestionBankFacade createDefault() {
        co.edu.unicauca.bancopreguntas.dataaccess.InMemoryQuestionRepository repo =
                new co.edu.unicauca.bancopreguntas.dataaccess.InMemoryQuestionRepository();
        PreguntaService srv = new PreguntaService(repo);
        QuestionValidator val = new QuestionValidator();
        QuestionPublisher pub = new QuestionPublisher();
        QuestionEvaluatorRegistry evalReg = QuestionEvaluatorRegistry.withDefaults();
        QuestionImporter imp = new QuestionImporter();

        return new QuestionBankFacade(srv, val, pub, repo, evalReg, imp);
    }

    /**
     * Ensamblaje de producción conectado a la base de datos real SQLite.
     */
    public static QuestionBankFacade createDefault(ConexionSQLite conexion) {
        PreguntaRepository repo = new PreguntaRepositorySQLite(conexion);
        PreguntaService srv = new PreguntaService(repo);
        QuestionValidator val = new QuestionValidator();
        QuestionPublisher pub = new QuestionPublisher();
        QuestionEvaluatorRegistry evalReg = QuestionEvaluatorRegistry.withDefaults();
        QuestionImporter imp = new QuestionImporter();

        return new QuestionBankFacade(srv, val, pub, repo, evalReg, imp);
    }

    // -------------------------------------------------------------------------
    // RF1, RF2, RF3, RF4: Creación, Tipo, Opciones y Respuestas Correctas
    // -------------------------------------------------------------------------

    public void createQuestion(Pregunta question) {
        validator.validate(question);
        questionService.save(question);
    }

    public int createQuestion(String statement, TipoPregunta type) {
        Pregunta question = new Pregunta(statement, type);
        createQuestion(question);
        return question.getId();
    }

    public void addOption(int questionId, String key, String text) {
        Pregunta question = getExisting(questionId);
        question.addOpcion(key, text);
        questionService.save(question);
    }

    public void addOption(Long questionId, String key, String text) {
        addOption(questionId.intValue(), key, text);
    }

    public void defineCorrectAnswers(int questionId, String... keys) {
        defineCorrectAnswers(questionId, Arrays.asList(keys));
    }

    public void defineCorrectAnswers(Long questionId, String... keys) {
        defineCorrectAnswers(questionId.intValue(), keys);
    }

    public void defineCorrectAnswers(int questionId, Collection<String> keys) {
        Pregunta question = getExisting(questionId);
        question.definirRespuestasCorrectas(keys);
        questionService.save(question);
    }

    public void defineCorrectAnswers(Long questionId, Collection<String> keys) {
        defineCorrectAnswers(questionId.intValue(), keys);
    }

    // -------------------------------------------------------------------------
    // RF5: Consultar preguntas
    // -------------------------------------------------------------------------

    public Optional<Pregunta> findQuestionById(int id) {
        if (repository != null) {
            return repository.buscarPorId(id);
        }
        Pregunta p = questionService.obtenerPregunta(id);
        return Optional.ofNullable(p);
    }

    public Optional<Pregunta> findQuestionById(Long id) {
        return findQuestionById(id.intValue());
    }

    public List<Pregunta> listAllQuestions() {
        if (repository != null) {
            return repository.listarTodas();
        }
        return Collections.emptyList();
    }

    public List<Pregunta> listQuestionsByState(String stateName) {
        if (repository != null) {
            co.edu.unicauca.bancopreguntas.domain.state.QuestionState s =
                    co.edu.unicauca.bancopreguntas.domain.state.EstadosPregunta.desdeNombre(stateName);
            return repository.listarPorEstado(s.getEstado(), 0, 1000);
        }
        return Collections.emptyList();
    }

    // -------------------------------------------------------------------------
    // RF6: Cambiar el estado de una pregunta (State)
    // -------------------------------------------------------------------------

    public void submitForReview(int id) {
        Pregunta q = getExisting(id);
        q.submitForReview();
        questionService.save(q);
    }

    public void submitForReview(Long id) {
        submitForReview(id.intValue());
    }

    public void assignReviewer(int id, String reviewer) {
        Pregunta q = getExisting(id);
        q.assignReviewer(reviewer);
        questionService.save(q);
    }

    public void assignReviewer(Long id, String reviewer) {
        assignReviewer(id.intValue(), reviewer);
    }

    public void approveQuestion(Pregunta question) {
        validator.validate(question);
        questionService.approve(question);
    }

    public void approveQuestion(int id) {
        Pregunta q = getExisting(id);
        approveQuestion(q);
    }

    public void approveQuestion(Long id) {
        approveQuestion(id.intValue());
    }

    public void rejectQuestion(int id, String reason) {
        Pregunta q = getExisting(id);
        q.reject(reason);
        questionService.save(q);
    }

    public void rejectQuestion(Long id, String reason) {
        rejectQuestion(id.intValue(), reason);
    }

    public void returnToDraft(int id) {
        Pregunta q = getExisting(id);
        q.returnToDraft();
        questionService.save(q);
    }

    public void returnToDraft(Long id) {
        returnToDraft(id.intValue());
    }

    public void publishQuestion(Pregunta question) {
        validator.validate(question);
        if (!(question.getState() instanceof co.edu.unicauca.bancopreguntas.domain.state.ApprovedState)) {
            questionService.approve(question);
        }
        publisher.publish(question);
        questionService.save(question);
    }

    public void publishQuestion(int id) {
        Pregunta question = getExisting(id);
        publishQuestion(question);
    }

    public void publishQuestion(Long id) {
        publishQuestion(id.intValue());
    }

    public void archiveQuestion(int id) {
        Pregunta q = getExisting(id);
        q.archive();
        questionService.save(q);
    }

    public void archiveQuestion(Long id) {
        archiveQuestion(id.intValue());
    }

    // -------------------------------------------------------------------------
    // RF7: Evaluar una respuesta (Template Method)
    // -------------------------------------------------------------------------

    public EvaluationResult evaluateAnswer(int questionId, List<String> answers) {
        Pregunta question = getExisting(questionId);
        // Exigencia del enunciado: sólo una pregunta Publicada puede usarse en una prueba
        question.ensureUsableInTest();

        QuestionEvaluator evaluator = evaluatorRegistry.forType(question.getTipo());
        return evaluator.evaluate(question, answers);
    }

    public EvaluationResult evaluateAnswer(Long questionId, List<String> answers) {
        return evaluateAnswer(questionId.intValue(), answers);
    }

    // -------------------------------------------------------------------------
    // RF8: Decorator
    // -------------------------------------------------------------------------

    public void decorateQuestion(int questionId, Feature feature) {
        decorateQuestion(questionId, feature, null);
    }

    public void decorateQuestion(Long questionId, Feature feature) {
        decorateQuestion(questionId.intValue(), feature, null);
    }

    public void decorateQuestion(int questionId, Feature feature, String detail) {
        Pregunta q = getExisting(questionId);
        QuestionComponent current = decoratedQuestions.computeIfAbsent(questionId, k -> new BasicQuestion(q));
        decoratedQuestions.put(questionId, QuestionDecoratorFactory.applyFeature(current, feature, detail));
    }

    public void decorateQuestion(Long questionId, Feature feature, String detail) {
        decorateQuestion(questionId.intValue(), feature, detail);
    }

    public DecoratedQuestionView describeQuestion(int questionId) {
        Pregunta q = getExisting(questionId);
        QuestionComponent comp = decoratedQuestions.get(questionId);
        if (comp == null) {
            comp = QuestionDecoratorFactory.decorateFromEntity(q);
        }
        return new DecoratedQuestionView(comp.getDescription(), comp.getScore());
    }

    public DecoratedQuestionView describeQuestion(Long questionId) {
        return describeQuestion(questionId.intValue());
    }

    // -------------------------------------------------------------------------
    // RF9: Adapter
    // -------------------------------------------------------------------------

    public Pregunta importQuestion(QuestionProvider provider) {
        Pregunta question = importer.importQuestion(provider);
        createQuestion(question);
        return question;
    }

    public Pregunta importDefaultExternalQuestion() {
        return importQuestion(new ExternalQuestionAdapter(new ExternalQuestionService()));
    }

    // -------------------------------------------------------------------------
    // Getters de subsistemas para integración con vistas existentes
    // -------------------------------------------------------------------------

    public PreguntaService getQuestionService() {
        return questionService;
    }

    public PreguntaRepository getRepository() {
        return repository;
    }

    private Pregunta getExisting(int id) {
        return findQuestionById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la pregunta con ID " + id));
    }
}
