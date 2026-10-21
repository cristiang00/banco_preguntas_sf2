package co.edu.unicauca.bancopreguntas.presentation.controllers;

import co.edu.unicauca.bancopreguntas.domain.decorator.DecoratedQuestionView;
import co.edu.unicauca.bancopreguntas.domain.decorator.Feature;
import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.evaluation.EvaluationResult;
import co.edu.unicauca.bancopreguntas.domain.facade.QuestionBankFacade;
import co.edu.unicauca.bancopreguntas.domain.facade.QuestionPublisher;
import co.edu.unicauca.bancopreguntas.domain.facade.QuestionValidator;
import co.edu.unicauca.bancopreguntas.domain.services.PreguntaService;

import com.unicauca.taller2.usuarios.model.Rol;

import java.util.List;

/**
 * Controlador de preguntas para la capa de presentación Swing.
 * Utiliza {@link QuestionBankFacade} para ejecutar todas las operaciones principales del banco.
 */
public class PreguntaController {

    private final QuestionBankFacade facade;
    private final int autorId;
    private final Rol rol;

    public PreguntaController(QuestionBankFacade facade, int autorId, Rol rol) {
        this.facade = facade;
        this.autorId = autorId;
        this.rol = rol != null ? rol : Rol.AUTOR_PREGUNTAS;
    }

    public PreguntaController(QuestionBankFacade facade, int autorId) {
        this(facade, autorId, Rol.AUTOR_PREGUNTAS);
    }

    public PreguntaController(PreguntaService preguntaService, int autorId, Rol rol) {
        this(new QuestionBankFacade(preguntaService, new QuestionValidator(), new QuestionPublisher()), autorId, rol);
    }

    public PreguntaController(PreguntaService preguntaService, int autorId) {
        this(new QuestionBankFacade(preguntaService, new QuestionValidator(), new QuestionPublisher()), autorId, Rol.AUTOR_PREGUNTAS);
    }

    public void crearPregunta(Pregunta pregunta) {
        pregunta.setAutorId(autorId);
        facade.createQuestion(pregunta);
    }

    public void enviarARevision(int preguntaId) {
        facade.submitForReview(preguntaId);
    }

    public void aprobarPregunta(int preguntaId) {
        facade.approveQuestion(preguntaId);
    }

    public void rechazarPregunta(int preguntaId, String motivo) {
        facade.rejectQuestion(preguntaId, motivo);
    }

    public void publicarPregunta(int preguntaId) {
        facade.publishQuestion(preguntaId);
    }

    public void archivarPregunta(int preguntaId) {
        facade.archiveQuestion(preguntaId);
    }

    public void devolverABorrador(int preguntaId) {
        facade.returnToDraft(preguntaId);
    }

    public EvaluationResult evaluarRespuesta(int preguntaId, List<String> respuestas) {
        return facade.evaluateAnswer(preguntaId, respuestas);
    }

    public DecoratedQuestionView describirPregunta(int preguntaId) {
        return facade.describeQuestion(preguntaId);
    }

    public void decorarPregunta(int preguntaId, Feature feature, String detalle) {
        facade.decorateQuestion(preguntaId, feature, detalle);
    }

    public Pregunta importarPreguntaExterna() {
        return facade.importDefaultExternalQuestion(autorId);
    }

    public List<Pregunta> listarMisPreguntas(List<EstadoPregunta> estadosFiltro, String nivelDificultad, int offset, int limit) {
        if (facade.getQuestionService() != null) {
            if (rol == Rol.REVISOR) {
                return facade.getQuestionService().listarPorRevisor(autorId, estadosFiltro, nivelDificultad, offset, limit);
            } else if (rol == Rol.DOCENTE) {
                return facade.getQuestionService().listarMisPreguntas(-1, estadosFiltro, nivelDificultad, offset, limit);
            }
            return facade.getQuestionService().listarMisPreguntas(autorId, estadosFiltro, nivelDificultad, offset, limit);
        }
        return facade.listAllQuestions();
    }

    public int contarMisPreguntas(List<EstadoPregunta> estadosFiltro, String nivelDificultad) {
        if (facade.getQuestionService() != null) {
            if (rol == Rol.REVISOR) {
                return facade.getQuestionService().contarPorRevisor(autorId, estadosFiltro, nivelDificultad);
            } else if (rol == Rol.DOCENTE) {
                return facade.getQuestionService().contarMisPreguntas(-1, estadosFiltro, nivelDificultad);
            }
            return facade.getQuestionService().contarMisPreguntas(autorId, estadosFiltro, nivelDificultad);
        }
        return facade.listAllQuestions().size();
    }

    public Pregunta obtenerDetalle(int preguntaId) {
        return facade.findQuestionById(preguntaId).orElse(null);
    }

    public QuestionBankFacade getFacade() {
        return facade;
    }

    public int getAutorId() {
        return autorId;
    }

    public Rol getRol() {
        return rol;
    }
}
