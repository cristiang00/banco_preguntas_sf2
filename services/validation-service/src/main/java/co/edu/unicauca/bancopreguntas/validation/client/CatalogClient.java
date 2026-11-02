package co.edu.unicauca.bancopreguntas.validation.client;

public interface CatalogClient {
    boolean esCompetenciaValida(String competencia);
    boolean esTemaValido(String tema);
    boolean esSubtemaValido(String subtema);
}

