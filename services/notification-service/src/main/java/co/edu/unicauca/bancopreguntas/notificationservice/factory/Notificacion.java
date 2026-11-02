package co.edu.unicauca.bancopreguntas.notificationservice.factory;

/**
 * Producto abstracto del patrón Factory Method (GoF).
 */
public interface Notificacion {
    String getDestinatario();
    String getAsunto();
    String getCuerpo();
    String getTipo();
}
