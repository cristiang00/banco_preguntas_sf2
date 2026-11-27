package co.edu.unicauca.bancopreguntas.frontend;

import co.edu.unicauca.bancopreguntas.contracts.dto.TokenResponseDTO;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiClient;
import co.edu.unicauca.bancopreguntas.frontend.client.ApiException;
import co.edu.unicauca.bancopreguntas.frontend.utils.EstadoCellRenderer;
import co.edu.unicauca.bancopreguntas.frontend.views.*;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
    @DisplayName("HU7.S2.4: Login exitoso mediante Keycloak simulado (Direct Access Grants)")
    void testLoginWithKeycloakSimulado() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();

        // Crear token JWT dummy con payload Keycloak (preferred_username: autor1, roles: [AUTOR])
        String payloadJson = "{\"preferred_username\":\"autor1\",\"name\":\"Autor Prueba\",\"realm_access\":{\"roles\":[\"AUTOR\"]}}";
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String dummyJwt = "eyJhbGciOiJSUzI1NiJ9." + encodedPayload + ".dummySignature123";

        server.createContext("/realms/saberpro/protocol/openid-connect/token", exchange -> {
            String resp = "{\"access_token\":\"" + dummyJwt + "\",\"expires_in\":3600,\"token_type\":\"Bearer\"}";
            byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        try {
            String kcUrl = "http://localhost:" + port + "/realms/saberpro/protocol/openid-connect/token";
            ApiClient client = new ApiClient("http://localhost:8080/api/v1", kcUrl);

            TokenResponseDTO token = client.login("autor1", "password123");

            assertNotNull(token);
            assertEquals("autor1", token.getNombreUsuario());
            assertEquals("AUTOR", token.getRol());
            assertEquals(2L, token.getUsuarioId());
            assertTrue(client.isAuthenticated());
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("HU7.S2.4: Credenciales inválidas en Keycloak simulado responden 401 Unauthorized")
    void testLoginWithKeycloakCredencialesInvalidas_responde401() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();

        server.createContext("/realms/saberpro/protocol/openid-connect/token", exchange -> {
            byte[] bytes = "{\"error\":\"invalid_grant\",\"error_description\":\"Invalid user credentials\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        try {
            String kcUrl = "http://localhost:" + port + "/realms/saberpro/protocol/openid-connect/token";
            ApiClient client = new ApiClient("http://localhost:8080/api/v1", kcUrl);

            ApiException ex = assertThrows(ApiException.class, () -> client.login("autor1", "claveIncorrecta"));
            assertEquals(401, ex.getStatusCode());
            assertFalse(client.isAuthenticated());
        } finally {
            server.stop(0);
        }
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

            try {
                HistorialRevisionesDialog historial = new HistorialRevisionesDialog(null, 999L);
                assertNotNull(historial);
            } catch (HeadlessException ignored) {
                // En entorno CI/headless, JDialog lanza HeadlessException al crear ventanas nativas
            }
        });
    }
}
