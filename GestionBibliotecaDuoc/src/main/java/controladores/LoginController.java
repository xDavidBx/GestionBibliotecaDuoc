package controladores;

import JdbcDAO.JdbcEstudianteDAO;
import JdbcDAO.JdbcCategoriaDAO;
import JdbcDAO.JdbcLibroDAO;
import JdbcDAO.JdbcPrestamoDAO;
import modelo.Usuario;
import servicios.AuthService;
import UI.BibliotecaFrame;
import UI.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.sql.SQLException;

public final class LoginController {
    private final LoginFrame view;
    private final AuthService authService;

    public LoginController(LoginFrame view, AuthService authService) {
        this.view = view; this.authService = authService;
        view.setLoginAction(this::iniciarSesion);
    }

    private void iniciarSesion(String correo, String contrasena) {
        view.setBusy(true);
        new SwingWorker<Usuario, Void>() {
            @Override protected Usuario doInBackground() throws Exception {
                return authService.autenticar(correo, contrasena).orElse(null);
            }
            @Override protected void done() {
                view.setBusy(false);
                try {
                    Usuario usuario = get();
                    if (usuario == null) {
                        JOptionPane.showMessageDialog(view, "Correo o contraseña incorrectos.", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (!usuario.esBibliotecario() && usuario.idEstudiante() == null) {
                        JOptionPane.showMessageDialog(view, "La cuenta de estudiante no está asociada a un registro de estudiante.",
                                "Cuenta incompleta", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    view.dispose();
                    var db = config.DataBaseConnection.getInstance();
                    var frame = new BibliotecaFrame(usuario);
                    var controller = new BibliotecaController(usuario, frame,
                            new JdbcLibroDAO(db), new JdbcEstudianteDAO(db), new JdbcCategoriaDAO(db),
                            new JdbcPrestamoDAO(db));
                    controller.iniciar();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    String message = cause instanceof SQLException
                            ? "No se pudo conectar a MySQL. Revisa config.properties y que el script SQL esté ejecutado.\n\n" + cause.getMessage()
                            : "No se pudo iniciar sesión: " + cause.getMessage();
                    JOptionPane.showMessageDialog(view, message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
