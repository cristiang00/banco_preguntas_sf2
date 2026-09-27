package com.unicauca.taller2.usuarios.access;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Fábrica de conexiones JDBC para SQLite.
 */
public class ConexionSQLite {

    private static final String URL = "jdbc:sqlite:saberpro.db";

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS usuarios (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    nombre_usuario TEXT UNIQUE NOT NULL," +
            "    nombre_completo TEXT NOT NULL," +
            "    rol TEXT NOT NULL," +
            "    estado TEXT NOT NULL," +
            "    password_hash TEXT NOT NULL," +
            "    fecha_creacion TEXT NOT NULL" +
            ");";

    private static final String CREATE_TABLE_PREGUNTAS_SQL =
            "CREATE TABLE IF NOT EXISTS preguntas (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    contexto TEXT NOT NULL," +
            "    pregunta_directa TEXT NOT NULL," +
            "    distractor1 TEXT NOT NULL," +
            "    distractor2 TEXT NOT NULL," +
            "    distractor3 TEXT NOT NULL," +
            "    distractor4 TEXT NOT NULL," +
            "    respuesta_correcta TEXT NOT NULL," +
            "    justificacion TEXT," +
            "    bibliografia TEXT NOT NULL," +
            "    competencia TEXT NOT NULL," +
            "    tema TEXT NOT NULL," +
            "    subtema TEXT NOT NULL," +
            "    nivel_dificultad TEXT NOT NULL," +
            "    estado TEXT NOT NULL DEFAULT 'BORRADOR'," +
            "    autor_id INTEGER NOT NULL," +
            "    fecha_creacion TEXT NOT NULL," +
            "    FOREIGN KEY (autor_id) REFERENCES usuarios(id)" +
            ");";

    private static final String CREATE_TABLE_PREGUNTA_REVISORES_SQL =
            "CREATE TABLE IF NOT EXISTS pregunta_revisores (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    pregunta_id INTEGER NOT NULL," +
            "    revisor_id INTEGER NOT NULL," +
            "    fecha_asignacion TEXT NOT NULL," +
            "    FOREIGN KEY (pregunta_id) REFERENCES preguntas(id)," +
            "    FOREIGN KEY (revisor_id) REFERENCES usuarios(id)," +
            "    UNIQUE(pregunta_id, revisor_id)" +
            ");";

    /**
     * Constructor por defecto que inicializa el esquema de la base de datos.
     */
    public ConexionSQLite() {
        inicializarEsquema();
    }

    /**
     * Obtiene una nueva conexión a la base de datos SQLite.
     *
     * @return una conexión JDBC activa
     * @throws SQLException si ocurre un error de conexión
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private void inicializarEsquema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
            stmt.execute(CREATE_TABLE_PREGUNTAS_SQL);
            stmt.execute(CREATE_TABLE_PREGUNTA_REVISORES_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Error al inicializar el esquema de la base de datos: " + e.getMessage(), e);
        }
    }
}
