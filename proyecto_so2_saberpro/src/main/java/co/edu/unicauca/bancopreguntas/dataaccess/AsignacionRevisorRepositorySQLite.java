package co.edu.unicauca.bancopreguntas.dataaccess;

import co.edu.unicauca.bancopreguntas.domain.repositories.AsignacionRevisorRepository;
import com.unicauca.taller2.usuarios.access.ConexionSQLite;
import com.unicauca.taller2.usuarios.model.EstadoUsuario;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AsignacionRevisorRepositorySQLite implements AsignacionRevisorRepository {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final ConexionSQLite conexion;

    public AsignacionRevisorRepositorySQLite(ConexionSQLite conexion) {
        this.conexion = conexion;
    }

    @Override
    public void asignar(int preguntaId, int revisorId) {
        String sql = "INSERT INTO pregunta_revisores (pregunta_id, revisor_id, fecha_asignacion) VALUES (?, ?, ?)";
        try (Connection conn = conexion.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, preguntaId);
            ps.setInt(2, revisorId);
            ps.setString(3, LocalDateTime.now().format(FORMATTER));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al asignar revisor: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> obtenerRevisores(int preguntaId) {
        String sql = "SELECT u.* FROM usuarios u JOIN pregunta_revisores pr ON u.id = pr.revisor_id WHERE pr.pregunta_id = ?";
        List<Usuario> revisores = new ArrayList<>();
        try (Connection conn = conexion.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, preguntaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    revisores.add(new Usuario(
                        rs.getInt("id"),
                        rs.getString("nombre_usuario"),
                        rs.getString("nombre_completo"),
                        Rol.valueOf(rs.getString("rol")),
                        EstadoUsuario.valueOf(rs.getString("estado")),
                        rs.getString("password_hash"),
                        LocalDateTime.parse(rs.getString("fecha_creacion"), FORMATTER)
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener revisores: " + e.getMessage(), e);
        }
        return revisores;
    }

    @Override
    public boolean existeAsignacion(int preguntaId, int revisorId) {
        String sql = "SELECT 1 FROM pregunta_revisores WHERE pregunta_id = ? AND revisor_id = ?";
        try (Connection conn = conexion.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, preguntaId);
            ps.setInt(2, revisorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar asignación: " + e.getMessage(), e);
        }
    }
}
