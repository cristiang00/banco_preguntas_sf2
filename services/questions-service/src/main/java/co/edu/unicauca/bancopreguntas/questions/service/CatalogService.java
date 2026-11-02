package co.edu.unicauca.bancopreguntas.questions.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    private final List<String> competencias = List.of(
            "Lectura Crítica",
            "Razonamiento Cuantitativo",
            "Competencias Ciudadanas",
            "Comunicación Escrita",
            "Inglés"
    );

    private final List<String> temas = List.of(
            "Geografía Política",
            "Estadística Descriptiva",
            "Probabilidad",
            "Argumentación y Lógica",
            "Constitución y Ciudadanía",
            "Comprensión Lectora",
            "Gramática y Cohesión",
            "Vocabulario y Sintaxis"
    );

    private final List<String> subtemas = List.of(
            "Capitales Departamentales",
            "Medidas de Tendencia Central",
            "Eventos Independientes",
            "Identificación de Falacias",
            "Mecanismos de Participación Ciudadana",
            "Inferencia Textual",
            "Conectores Lógicos",
            "Tiempos Verbales"
    );

    public List<String> listarCompetencias() {
        return competencias;
    }

    public List<String> listarTemas() {
        return temas;
    }

    public List<String> listarSubtemas() {
        return subtemas;
    }
}

