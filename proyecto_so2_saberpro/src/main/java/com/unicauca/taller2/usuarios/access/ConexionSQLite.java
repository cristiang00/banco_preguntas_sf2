package com.unicauca.taller2.usuarios.access;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Fábrica de conexiones JDBC para SQLite con soporte para esquema y migraciones.
 */
public class ConexionSQLite {

    private static final String URL = resolverUrl();

    private static String resolverUrl() {
        java.io.File enSubdirectorio = new java.io.File("proyecto_1corte_so2/proyecto_so2_saberpro/saberpro.db");
        if (enSubdirectorio.exists()) {
            return "jdbc:sqlite:" + enSubdirectorio.getAbsolutePath();
        }
        return "jdbc:sqlite:saberpro.db";
    }

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
            "    tipo TEXT NOT NULL DEFAULT 'SELECCION_UNICA'," +
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

    private static final String CREATE_TABLE_PREGUNTA_OPCIONES_SQL =
            "CREATE TABLE IF NOT EXISTS pregunta_opciones (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    pregunta_id INTEGER NOT NULL," +
            "    clave TEXT NOT NULL," +
            "    texto TEXT NOT NULL," +
            "    orden INTEGER NOT NULL DEFAULT 0," +
            "    FOREIGN KEY (pregunta_id) REFERENCES preguntas(id) ON DELETE CASCADE," +
            "    UNIQUE(pregunta_id, clave)" +
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
            stmt.execute(CREATE_TABLE_PREGUNTA_OPCIONES_SQL);
            migrarEsquema(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error al inicializar el esquema de la base de datos: " + e.getMessage(), e);
        }
    }

    /**
     * Migración conservativa: agrega columnas o tablas nuevas si la BD ya existía,
     * e inicializa las opciones para las preguntas existentes.
     */
    private void migrarEsquema(Connection conn) throws SQLException {
        // 1. Verificar si la columna tipo existe en preguntas
        boolean tieneTipo = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(preguntas)")) {
            while (rs.next()) {
                String colName = rs.getString("name");
                if ("tipo".equalsIgnoreCase(colName)) {
                    tieneTipo = true;
                    break;
                }
            }
        }

        if (!tieneTipo) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE preguntas ADD COLUMN tipo TEXT NOT NULL DEFAULT 'SELECCION_UNICA'");
            }
        }

        // 2. Poblar opciones desde distractores existentes si pregunta_opciones está vacía para esa pregunta
        String sqlCheck = "SELECT COUNT(*) FROM pregunta_opciones WHERE pregunta_id = ?";
        String sqlSelectPreguntas = "SELECT id, distractor1, distractor2, distractor3, distractor4 FROM preguntas";
        String sqlInsertOpcion = "INSERT OR IGNORE INTO pregunta_opciones (pregunta_id, clave, texto, orden) VALUES (?, ?, ?, ?)";

        try (Statement stmt = conn.createStatement();
             ResultSet rsPreguntas = stmt.executeQuery(sqlSelectPreguntas);
             PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
             PreparedStatement psInsert = conn.prepareStatement(sqlInsertOpcion)) {

            while (rsPreguntas.next()) {
                int preguntaId = rsPreguntas.getInt("id");
                psCheck.setInt(1, preguntaId);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                        insertarOpcionSiExiste(psInsert, preguntaId, "A", rsPreguntas.getString("distractor1"), 1);
                        insertarOpcionSiExiste(psInsert, preguntaId, "B", rsPreguntas.getString("distractor2"), 2);
                        insertarOpcionSiExiste(psInsert, preguntaId, "C", rsPreguntas.getString("distractor3"), 3);
                        insertarOpcionSiExiste(psInsert, preguntaId, "D", rsPreguntas.getString("distractor4"), 4);
                    }
                }
            }
        }
    }

    private void insertarOpcionSiExiste(PreparedStatement ps, int preguntaId, String clave, String texto, int orden)
            throws SQLException {
        if (texto != null && !texto.isBlank()) {
            ps.setInt(1, preguntaId);
            ps.setString(2, clave);
            ps.setString(3, texto.trim());
            ps.setInt(4, orden);
            ps.executeUpdate();
        }
    }
}
