package co.edu.unicauca.bancopreguntas.dataaccess;

import co.edu.unicauca.bancopreguntas.domain.entities.Notificacion;
import co.edu.unicauca.bancopreguntas.domain.repositories.NotificacionRepository;
import com.unicauca.taller2.usuarios.access.ConexionSQLite;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación SQLite del repositorio de notificaciones.
 * Crea la tabla {@code notificaciones} automáticamente si no existe.
 */
public class NotificacionRepositorySQLite implements NotificacionRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final ConexionSQLite conexion;

    public NotificacionRepositorySQLite(ConexionSQLite conexion) {
        this.conexion = conexion;
        crearTablaSimNecesaria();
    }

    private void crearTablaSimNecesaria() {
        String sql = "CREATE TABLE IF NOT EXISTS notificaciones ("
                   + "    id          INTEGER PRIMARY KEY AUTOINCREMENT,"
                   + "    pregunta_id INTEGER NOT NULL,"
                   + "    revisor_id  INTEGER NOT NULL,"
                   + "    asunto      TEXT NOT NULL,"
                   + "    cuerpo      TEXT,"
                   + "    fecha       TEXT NOT NULL"
                   + ")";
        try (Connection conn = conexion.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear tabla notificaciones: " + e.getMessage(), e);
        }
    }

    @Override
    public void guardar(Notificacion notificacion) {
        String sql = "INSERT INTO notificaciones (pregunta_id, revisor_id, asunto, cuerpo, fecha) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notificacion.getPreguntaId());
            ps.setInt(2, notificacion.getRevisorId());
            ps.setString(3, notificacion.getAsunto());
            ps.setString(4, notificacion.getCuerpo());
            ps.setString(5, notificacion.getFecha().format(FORMATTER));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) notificacion.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar notificación: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Notificacion> listarTodas() {
        String sql = "SELECT * FROM notificaciones ORDER BY fecha DESC";
        List<Notificacion> lista = new ArrayList<>();
        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Notificacion n = new Notificacion();
                n.setId(rs.getInt("id"));
                n.setPreguntaId(rs.getInt("pregunta_id"));
                n.setRevisorId(rs.getInt("revisor_id"));
                n.setAsunto(rs.getString("asunto"));
                n.setCuerpo(rs.getString("cuerpo"));
                n.setFecha(LocalDateTime.parse(rs.getString("fecha"), FORMATTER));
                lista.add(n);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar notificaciones: " + e.getMessage(), e);
        }
        return lista;
    }
}
