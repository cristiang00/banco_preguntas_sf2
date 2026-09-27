package com.unicauca.taller2.usuarios.access;

import com.unicauca.taller2.usuarios.model.EstadoUsuario;
import com.unicauca.taller2.usuarios.model.Rol;
import com.unicauca.taller2.usuarios.model.Usuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de UsuarioRepository usando SQLite y JDBC.
 */
public class UsuarioRepositorySQLite implements UsuarioRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ConexionSQLite conexion;

    /**
     * Constructor.
     *
     * @param conexion la fábrica de conexiones a utilizar
     */
    public UsuarioRepositorySQLite(ConexionSQLite conexion) {
        this.conexion = conexion;
    }

    @Override
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre_usuario, nombre_completo, rol, estado, password_hash, fecha_creacion) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getNombreCompleto());
            ps.setString(3, usuario.getRol().name());
            ps.setString(4, usuario.getEstado().name());
            ps.setString(5, usuario.getPasswordHash());
            ps.setString(6, usuario.getFechaCreacion().format(FORMATTER));

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        String sql = "SELECT id, nombre_usuario, nombre_completo, rol, estado, password_hash, fecha_creacion " +
                     "FROM usuarios WHERE nombre_usuario = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        String sql = "SELECT id, nombre_usuario, nombre_completo, rol, estado, password_hash, fecha_creacion " +
                     "FROM usuarios ORDER BY id";

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }

            return usuarios;

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los usuarios: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizarEstado(String nombreUsuario, EstadoUsuario estado) {
        String sql = "UPDATE usuarios SET estado = ? WHERE nombre_usuario = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, estado.name());
            ps.setString(2, nombreUsuario);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el estado del usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> listarPorRol(Rol rol) {
        String sql = "SELECT id, nombre_usuario, nombre_completo, rol, estado, password_hash, fecha_creacion " +
                     "FROM usuarios WHERE rol = ? ORDER BY nombre_completo";

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, rol.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapearUsuario(rs));
                }
            }

            return usuarios;

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los usuarios por rol: " + e.getMessage(), e);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre_usuario"),
                rs.getString("nombre_completo"),
                Rol.valueOf(rs.getString("rol")),
                EstadoUsuario.valueOf(rs.getString("estado")),
                rs.getString("password_hash"),
                LocalDateTime.parse(rs.getString("fecha_creacion"), FORMATTER)
        );
    }
}
