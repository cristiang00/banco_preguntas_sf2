package co.edu.unicauca.bancopreguntas.notificationservice.adapter;

/**
 * Interfaz Target del patrón Adapter (GoF).
 * Desacopla la lógica de negocio del proveedor concreto de correo.
 */
public interface EmailSender {

    /**
     * Envía un correo electrónico al destinatario indicado.
     *
     * @param to dirección de correo destinataria
     * @param subject asunto del mensaje
     * @param body contenido del mensaje
     * @return true si fue entregado con éxito
     * @throws EmailDeliveryException si ocurre una falla en el transporte
     */
    boolean sendEmail(String to, String subject, String body) throws EmailDeliveryException;

    String getProviderName();
}
