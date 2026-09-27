package co.edu.unicauca.bancopreguntas.dataaccess;

import co.edu.unicauca.bancopreguntas.domain.entities.Notificacion;
import co.edu.unicauca.bancopreguntas.domain.repositories.NotificacionRepository;
import com.unicauca.taller2.usuarios.access.ConexionSQLite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para {@link NotificacionRepositorySQLite}.
 * Usa una base de datos SQLite temporal para no afectar {@code saberpro.db}.
 */
class NotificacionRepositorySQLiteTest {

    @TempDir
    Path tempDir;

    private NotificacionRepository repo;

    @BeforeEach
    void setUp() {
        // Crear una ConexionSQLite anónima que apunte a un archivo temporal único
        // Se usa una subclase anónima para redirigir la URL al archivo temporal
        File dbFile = tempDir.resolve("test_notificaciones.db").toFile();
        final String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        ConexionSQLite conexion = new ConexionSQLite() {
            @Override
            public java.sql.Connection getConnection() throws java.sql.SQLException {
                return java.sql.DriverManager.getConnection(url);
            }
        };
        repo = new NotificacionRepositorySQLite(conexion);
    }

    @Test
    void listarTodas_sinDatos_retornaListaVacia() {
        List<Notificacion> lista = repo.listarTodas();
        assertTrue(lista.isEmpty());
    }

    @Test
    void guardar_yListar_retornaNotificacionGuardada() {
        Notificacion n = new Notificacion();
        n.setPreguntaId(1);
        n.setRevisorId(2);
        n.setAsunto("Test asunto de notificación");
        n.setCuerpo("Cuerpo de la notificación de prueba");
        n.setFecha(LocalDateTime.now());

        repo.guardar(n);

        List<Notificacion> lista = repo.listarTodas();
        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
        assertEquals("Test asunto de notificación", lista.get(0).getAsunto());
        assertEquals(1, lista.get(0).getPreguntaId());
        assertEquals(2, lista.get(0).getRevisorId());
    }

    @Test
    void guardar_variosRegistros_retornaOrdenadosFechaDesc() {
        LocalDateTime base = LocalDateTime.of(2026, 9, 1, 10, 0, 0);

        Notificacion n1 = new Notificacion();
        n1.setPreguntaId(1); n1.setRevisorId(1);
        n1.setAsunto("Primera"); n1.setFecha(base);
        repo.guardar(n1);

        Notificacion n2 = new Notificacion();
        n2.setPreguntaId(2); n2.setRevisorId(2);
        n2.setAsunto("Segunda"); n2.setFecha(base.plusHours(1));
        repo.guardar(n2);

        List<Notificacion> lista = repo.listarTodas();
        assertEquals(2, lista.size());
        // El más reciente debe aparecer primero (ORDER BY fecha DESC)
        assertEquals("Segunda", lista.get(0).getAsunto());
        assertEquals("Primera", lista.get(1).getAsunto());
    }

    @Test
    void guardar_asignaIdGenerado() {
        Notificacion n = new Notificacion();
        n.setPreguntaId(5); n.setRevisorId(3);
        n.setAsunto("Con ID"); n.setFecha(LocalDateTime.now());

        assertEquals(0, n.getId()); // antes de guardar
        repo.guardar(n);
        assertTrue(n.getId() > 0, "El ID debe ser asignado por AUTOINCREMENT");
    }
}
