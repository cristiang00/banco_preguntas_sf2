package co.edu.unicauca.bancopreguntas.contracts.events;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Despacha eventos de dominio de forma asíncrona hacia microservicios suscriptores (Notification y Audit).
 */
public class RemoteEventDispatcher {

    private static final Logger log = System.getLogger(RemoteEventDispatcher.class.getName());

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(1000))
            .build();

    public static void dispatchAsync(String json, String notificationServiceUrl, String auditServiceUrl) {
        if (json == null || json.isBlank()) return;

        CompletableFuture.runAsync(() -> {
            if (notificationServiceUrl != null && !notificationServiceUrl.isBlank()) {
                sendPost(notificationServiceUrl + "/notificaciones/eventos", json, "NotificationService");
            }
            if (auditServiceUrl != null && !auditServiceUrl.isBlank()) {
                sendPost(auditServiceUrl + "/auditoria/eventos", json, "AuditService");
            }
        });
    }

    private static void sendPost(String url, String json, String targetName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(2))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(res -> {
                        if (res.statusCode() >= 200 && res.statusCode() < 300) {
                            log.log(Level.INFO, "[RemoteEventDispatcher] Evento entregado exitosamente a " + targetName + " (" + url + ")");
                        } else {
                            log.log(Level.WARNING, "[RemoteEventDispatcher] " + targetName + " respondió con código " + res.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        log.log(Level.DEBUG, "[RemoteEventDispatcher] No se pudo entregar evento a " + targetName + ": " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.log(Level.DEBUG, "[RemoteEventDispatcher] Error preparando envío a " + targetName + ": " + e.getMessage());
        }
    }
}
