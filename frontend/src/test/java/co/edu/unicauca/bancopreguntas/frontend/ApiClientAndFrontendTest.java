package co.edu.unicauca.bancopreguntas.frontend;

import co.edu.unicauca.bancopreguntas.contracts.dto.TokenResponseDTO;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.utils.EstadoCellRenderer;
import co.edu.unicauca.bancopreguntas.frontend.views.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class ApiClientAndFrontendTest {

    @BeforeAll
    static void initHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    @DisplayName("ApiClient: inicialización, almacenamiento de token y logout")
    void testApiClientTokenState() {
        ApiClient client = new ApiClient("http://localhost:8080/api/v1");
        assertFalse(client.isAuthenticated());

        client.setToken("dummy.jwt.token");
        TokenResponseDTO user = new TokenResponseDTO("dummy.jwt.token", 1L, "autor1", "Autor Uno", "AUTOR");
        client.setCurrentUser(user);

        assertTrue(client.isAuthenticated());
        assertEquals("AUTOR", client.getCurrentUser().getRol());

        client.logout();
        assertFalse(client.isAuthenticated());
        assertNull(client.getCurrentUser());
    }

    @Test
    @DisplayName("EstadoCellRenderer: renderizado de etiquetas y colores por estado")
    void testEstadoCellRenderer() {
        EstadoCellRenderer renderer = new EstadoCellRenderer();
        JTable table = new JTable();

        Component compBorrador = renderer.getTableCellRendererComponent(table, "BORRADOR", false, false, 0, 0);
        assertEquals("Borrador", ((JLabel) compBorrador).getText());
        assertEquals(new Color(254, 240, 138), compBorrador.getBackground());

        Component compPendiente = renderer.getTableCellRendererComponent(table, "PENDIENTE_REVISION", false, false, 0, 0);
        assertEquals("Pendiente de Revisión", ((JLabel) compPendiente).getText());
        assertEquals(new Color(219, 234, 254), compPendiente.getBackground());

        Component compAprobada = renderer.getTableCellRendererComponent(table, "APROBADA", false, false, 0, 0);
        assertEquals("Aprobada", ((JLabel) compAprobada).getText());
        assertEquals(new Color(220, 252, 231), compAprobada.getBackground());

        Component compRechazada = renderer.getTableCellRendererComponent(table, "RECHAZADA", false, false, 0, 0);
        assertEquals("Rechazada", ((JLabel) compRechazada).getText());
        assertEquals(new Color(254, 226, 226), compRechazada.getBackground());
    }

    @Test
    @DisplayName("Instanciación de Paneles Swing desacoplados")
    void testPanelsInstantiation() {
        assertDoesNotThrow(() -> {
            CrearPreguntaPanel crear = new CrearPreguntaPanel();
            assertNotNull(crear);

            ListarPreguntasPanel listar = new ListarPreguntasPanel();
            assertNotNull(listar);

            AsignarRevisoresPanel asignar = new AsignarRevisoresPanel();
            assertNotNull(asignar);

            RevisionesAsignadasPanel revisiones = new RevisionesAsignadasPanel();
            assertNotNull(revisiones);

            AuditoriaPanel audit = new AuditoriaPanel();
            assertNotNull(audit);

            NotificacionesPanel notif = new NotificacionesPanel();
            assertNotNull(notif);
        });
    }
}
