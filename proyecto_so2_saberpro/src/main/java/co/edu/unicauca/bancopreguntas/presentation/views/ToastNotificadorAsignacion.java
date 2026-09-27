package co.edu.unicauca.bancopreguntas.presentation.views;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.services.NotificadorAsignacion;
import co.edu.unicauca.bancopreguntas.presentation.utils.ToastNotificacion;
import com.unicauca.taller2.usuarios.model.Usuario;

import javax.swing.SwingUtilities;
import java.awt.Window;

/**
 * Observer 2 — muestra un toast emergente en la UI cada vez que se asigna un revisor.
 * <p>
 * Vive en {@code presentation} e implementa la interfaz {@code NotificadorAsignacion}
 * definida en {@code domain} — válido por DIP: el dominio define el contrato,
 * la presentación lo implementa.
 * </p>
 * Toda actualización de UI se delega a {@link SwingUtilities#invokeLater} para
 * garantizar la seguridad de hilos en Swing.
 */
public class ToastNotificadorAsignacion implements NotificadorAsignacion {

    private final Window ventanaPadre;

    public ToastNotificadorAsignacion(Window ventanaPadre) {
        this.ventanaPadre = ventanaPadre;
    }

    @Override
    public void notificarAsignacion(Pregunta pregunta, Usuario revisor) {
        SwingUtilities.invokeLater(() ->
            ToastNotificacion.mostrar(ventanaPadre,
                "Notificación enviada a " + revisor.getNombreCompleto())
        );
    }
}
