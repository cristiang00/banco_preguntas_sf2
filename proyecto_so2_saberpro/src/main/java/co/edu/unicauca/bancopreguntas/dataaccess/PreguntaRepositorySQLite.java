package co.edu.unicauca.bancopreguntas.dataaccess;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
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
        String sql = "INSERT INTO preguntas (contexto, pregunta_directa, distractor1, distractor2, distractor3, distractor4, respuesta_correcta, justificacion, bibliografia, competencia, tema, subtema, nivel_dificultad, estado, autor_id, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexion.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setPreparedStatementParameters(ps, pregunta);
            ps.setString(14, pregunta.getEstado().name());
            ps.setInt(15, pregunta.getAutorId());
            ps.setString(16, pregunta.getFechaCreacion() != null ? pregunta.getFechaCreacion().format(FORMATTER) : LocalDateTime.now().format(FORMATTER));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pregunta.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la pregunta: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Pregunta pregunta) {
        String sql = "UPDATE preguntas SET contexto=?, pregunta_directa=?, distractor1=?, distractor2=?, distractor3=?, distractor4=?, respuesta_correcta=?, justificacion=?, bibliografia=?, competencia=?, tema=?, subtema=?, nivel_dificultad=?, estado=?, autor_id=? WHERE id=?";
        try (Connection conn = conexion.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setPreparedStatementParameters(ps, pregunta);
            ps.setString(14, pregunta.getEstado().name());
            ps.setInt(15, pregunta.getAutorId());
            ps.setInt(16, pregunta.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la pregunta: " + e.getMessage(), e);
        }
    }

    private void setPreparedStatementParameters(PreparedStatement ps, Pregunta pregunta) throws SQLException {
        ps.setString(1, pregunta.getContexto());
        ps.setString(2, pregunta.getPreguntaDirecta());
        ps.setString(3, pregunta.getDistractor1());
        ps.setString(4, pregunta.getDistractor2());
        ps.setString(5, pregunta.getDistractor3());
        ps.setString(6, pregunta.getDistractor4());
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
                    return Optional.of(mapearPregunta(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la pregunta: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Pregunta> listarPorAutor(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT p.*, u.nombre_completo AS autor_nombre FROM preguntas p JOIN usuarios u ON p.autor_id = u.id WHERE 1=1");
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
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar preguntas: " + e.getMessage(), e);
        }
        return 0;
    }

    private void appendFiltros(StringBuilder sql, List<Object> params, List<EstadoPregunta> estadosFiltro, String nivelDificultad) {
        if (estadosFiltro != null && !estadosFiltro.isEmpty()) {
            sql.append(" AND p.estado IN (");
            for (int i = 0; i < estadosFiltro.size(); i++) {
                sql.append("?");
                if (i < estadosFiltro.size() - 1) sql.append(",");
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
                    lista.add(mapearPregunta(rs));
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
        p.setEstado(EstadoPregunta.valueOf(rs.getString("estado")));
        p.setAutorId(rs.getInt("autor_id"));
        p.setFechaCreacion(LocalDateTime.parse(rs.getString("fecha_creacion"), FORMATTER));
        
        // El JOIN nos da el nombre del autor
        try {
            p.setAutorNombre(rs.getString("autor_nombre"));
        } catch (SQLException e) {
            // Ignorar si la columna no está (por ejemplo, en un count)
        }
        return p;
    }
}
