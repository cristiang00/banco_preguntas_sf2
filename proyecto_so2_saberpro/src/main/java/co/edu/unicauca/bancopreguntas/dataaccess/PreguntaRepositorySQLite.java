package co.edu.unicauca.bancopreguntas.dataaccess;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.OpcionRespuesta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;
import com.unicauca.taller2.usuarios.access.ConexionSQLite;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PreguntaRepositorySQLite implements PreguntaRepository {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final ConexionSQLite conexion;

    public PreguntaRepositorySQLite(ConexionSQLite conexion) {
        this.conexion = conexion;
    }

    @Override
    public void guardar(Pregunta pregunta) {
        String sql = "INSERT INTO preguntas (contexto, pregunta_directa, distractor1, distractor2, distractor3, distractor4, "
                + "respuesta_correcta, justificacion, bibliografia, competencia, tema, subtema, nivel_dificultad, estado, autor_id, fecha_creacion, tipo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setPreparedStatementParameters(ps, pregunta);
            ps.setString(14, pregunta.getEstado().name());
            ps.setInt(15, pregunta.getAutorId());
            ps.setString(16, pregunta.getFechaCreacion() != null ? pregunta.getFechaCreacion().format(FORMATTER)
                    : LocalDateTime.now().format(FORMATTER));
            ps.setString(17, pregunta.getTipo() != null ? pregunta.getTipo().name() : TipoPregunta.SELECCION_UNICA.name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pregunta.setId(rs.getInt(1));
                }
            }
            guardarOpciones(conn, pregunta.getId(), pregunta.getOpciones());
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la pregunta: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Pregunta pregunta) {
        String sql = "UPDATE preguntas SET contexto=?, pregunta_directa=?, distractor1=?, distractor2=?, distractor3=?, distractor4=?, "
                + "respuesta_correcta=?, justificacion=?, bibliografia=?, competencia=?, tema=?, subtema=?, nivel_dificultad=?, estado=?, autor_id=?, tipo=? "
                + "WHERE id=?";
        try (Connection conn = conexion.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setPreparedStatementParameters(ps, pregunta);
            ps.setString(14, pregunta.getEstado().name());
            ps.setInt(15, pregunta.getAutorId());
            ps.setString(16, pregunta.getTipo() != null ? pregunta.getTipo().name() : TipoPregunta.SELECCION_UNICA.name());
            ps.setInt(17, pregunta.getId());
            ps.executeUpdate();

            guardarOpciones(conn, pregunta.getId(), pregunta.getOpciones());
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la pregunta: " + e.getMessage(), e);
        }
    }

    private void guardarOpciones(Connection conn, int preguntaId, List<OpcionRespuesta> opciones) throws SQLException {
        if (opciones == null || opciones.isEmpty()) {
            return;
        }
        try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM pregunta_opciones WHERE pregunta_id = ?");
             PreparedStatement psIns = conn.prepareStatement("INSERT INTO pregunta_opciones (pregunta_id, clave, texto, orden) VALUES (?, ?, ?, ?)")) {
            psDel.setInt(1, preguntaId);
            psDel.executeUpdate();

            int orden = 1;
            for (OpcionRespuesta op : opciones) {
                psIns.setInt(1, preguntaId);
                psIns.setString(2, op.clave());
                psIns.setString(3, op.texto());
                psIns.setInt(4, orden++);
                psIns.executeUpdate();
            }
        }
    }

    private void setPreparedStatementParameters(PreparedStatement ps, Pregunta pregunta) throws SQLException {
        ps.setString(1, pregunta.getContexto());
        ps.setString(2, pregunta.getPreguntaDirecta());
        ps.setString(3, pregunta.getDistractor1() != null ? pregunta.getDistractor1() : "");
        ps.setString(4, pregunta.getDistractor2() != null ? pregunta.getDistractor2() : "");
        ps.setString(5, pregunta.getDistractor3() != null ? pregunta.getDistractor3() : "");
        ps.setString(6, pregunta.getDistractor4() != null ? pregunta.getDistractor4() : "");
        ps.setString(7, pregunta.getRespuestaCorrecta());
        ps.setString(8, pregunta.getJustificacion() != null ? pregunta.getJustificacion() : "");
        ps.setString(9, pregunta.getBibliografia());
        ps.setString(10, pregunta.getCompetencia());
        ps.setString(11, pregunta.getTema());
        ps.setString(12, pregunta.getSubtema());
        ps.setString(13, pregunta.getNivelDificultad());
    }

    @Override
    public void actualizarEstado(int preguntaId, EstadoPregunta nuevoEstado) {
        String sql = "UPDATE preguntas SET estado = ? WHERE id = ?";
        try (Connection conn = conexion.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, preguntaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el estado: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Pregunta> buscarPorId(int id) {
        String sql = "SELECT p.*, u.nombre_completo AS autor_nombre FROM preguntas p JOIN usuarios u ON p.autor_id = u.id WHERE p.id = ?";
        try (Connection conn = conexion.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Pregunta p = mapearPregunta(rs);
                    cargarOpciones(conn, p);
                    return Optional.of(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la pregunta: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Pregunta> listarPorAutor(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad,
                                         int offset, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, u.nombre_completo AS autor_nombre FROM preguntas p JOIN usuarios u ON p.autor_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (autorId != -1) {
            sql.append(" AND p.autor_id = ?");
            params.add(autorId);
        }

        appendFiltros(sql, params, estadosFiltro, nivelDificultad);

        sql.append(" ORDER BY p.fecha_creacion DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return ejecutarQueryLista(sql.toString(), params);
    }

    @Override
    public int contarPorAutor(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM preguntas p WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (autorId != -1) {
            sql.append(" AND p.autor_id = ?");
            params.add(autorId);
        }

        appendFiltros(sql, params, estadosFiltro, nivelDificultad);

        try (Connection conn = conexion.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar preguntas: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public List<Pregunta> listarPorRevisor(int revisorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad,
                                           int offset, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, u.nombre_completo AS autor_nombre FROM preguntas p " +
                "JOIN usuarios u ON p.autor_id = u.id " +
                "JOIN pregunta_revisores pr ON p.id = pr.pregunta_id " +
                "WHERE pr.revisor_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(revisorId);

        appendFiltros(sql, params, estadosFiltro, nivelDificultad);

        sql.append(" ORDER BY p.fecha_creacion DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return ejecutarQueryLista(sql.toString(), params);
    }

    @Override
    public int contarPorRevisor(int revisorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM preguntas p " +
                "JOIN pregunta_revisores pr ON p.id = pr.pregunta_id " +
                "WHERE pr.revisor_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(revisorId);

        appendFiltros(sql, params, estadosFiltro, nivelDificultad);

        try (Connection conn = conexion.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar preguntas por revisor: " + e.getMessage(), e);
        }
        return 0;
    }


    private void appendFiltros(StringBuilder sql, List<Object> params, List<EstadoPregunta> estadosFiltro,
                               String nivelDificultad) {
        if (estadosFiltro != null && !estadosFiltro.isEmpty()) {
            sql.append(" AND p.estado IN (");
            for (int i = 0; i < estadosFiltro.size(); i++) {
                sql.append("?");
                if (i < estadosFiltro.size() - 1)
                    sql.append(",");
                params.add(estadosFiltro.get(i).name());
            }
            sql.append(")");
        }
        if (nivelDificultad != null && !nivelDificultad.isEmpty() && !nivelDificultad.equals("Todos")) {
            sql.append(" AND p.nivel_dificultad = ?");
            params.add(nivelDificultad);
        }
    }

    private List<Pregunta> ejecutarQueryLista(String sql, List<Object> params) {
        List<Pregunta> lista = new ArrayList<>();
        try (Connection conn = conexion.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pregunta p = mapearPregunta(rs);
                    cargarOpciones(conn, p);
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando query: " + e.getMessage(), e);
        }
        return lista;
    }

    private Pregunta mapearPregunta(ResultSet rs) throws SQLException {
        Pregunta p = new Pregunta();
        p.setId(rs.getInt("id"));
        p.setContexto(rs.getString("contexto"));
        p.setPreguntaDirecta(rs.getString("pregunta_directa"));
        p.setDistractor1(rs.getString("distractor1"));
        p.setDistractor2(rs.getString("distractor2"));
        p.setDistractor3(rs.getString("distractor3"));
        p.setDistractor4(rs.getString("distractor4"));
        p.setRespuestaCorrecta(rs.getString("respuesta_correcta"));
        p.setJustificacion(rs.getString("justificacion"));
        p.setBibliografia(rs.getString("bibliografia"));
        p.setCompetencia(rs.getString("competencia"));
        p.setTema(rs.getString("tema"));
        p.setSubtema(rs.getString("subtema"));
        p.setNivelDificultad(rs.getString("nivel_dificultad"));
        try {
            String tipoStr = rs.getString("tipo");
            p.hidratarTipo(TipoPregunta.resolver(tipoStr));
        } catch (SQLException e) {
            p.hidratarTipo(TipoPregunta.SELECCION_UNICA);
        }

        p.hidratarEstado(EstadoPregunta.valueOf(rs.getString("estado")));
        p.setAutorId(rs.getInt("autor_id"));
        p.setFechaCreacion(LocalDateTime.parse(rs.getString("fecha_creacion"), FORMATTER));

        try {
            p.setAutorNombre(rs.getString("autor_nombre"));
        } catch (SQLException e) {
            // Ignorar si la columna no está
        }
        return p;
    }

    private void cargarOpciones(Connection conn, Pregunta p) {
        String sql = "SELECT clave, texto FROM pregunta_opciones WHERE pregunta_id = ? ORDER BY orden, id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<OpcionRespuesta> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new OpcionRespuesta(rs.getString("clave"), rs.getString("texto")));
                }
                if (!lista.isEmpty()) {
                    p.hidratarOpciones(lista);
                }
            }
        } catch (SQLException e) {
            // Silencioso para fallback a distractores en memoria
        }
    }

    @Override
    public List<Pregunta> listarPorEstado(EstadoPregunta estado, int offset, int limit) {
        String sql = "SELECT p.*, u.nombre_completo AS autor_nombre FROM preguntas p "
                + "JOIN usuarios u ON p.autor_id = u.id "
                + "WHERE p.estado = ? ORDER BY p.fecha_creacion DESC LIMIT ? OFFSET ?";
        return ejecutarQueryLista(sql, List.of(estado.name(), limit, offset));
    }

    @Override
    public int contarPorEstado(EstadoPregunta estado) {
        String sql = "SELECT COUNT(*) FROM preguntas WHERE estado = ?";
        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar preguntas por estado: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public List<Pregunta> listarTodas() {
        String sql = "SELECT p.*, u.nombre_completo AS autor_nombre FROM preguntas p "
                + "JOIN usuarios u ON p.autor_id = u.id ORDER BY p.fecha_creacion DESC";
        return ejecutarQueryLista(sql, List.of());
    }
}
