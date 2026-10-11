package app;

import controladores.LoginController;
import JdbcDAO.JdbcUsuarioDAO;
import servicios.AuthService;
import UI.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Se conserva el tema Swing predeterminado si el tema del sistema no está disponible.
            }
            var authService = new AuthService(new JdbcUsuarioDAO(config.DataBaseConnection.getInstance()));
            var view = new LoginFrame();
            new LoginController(view, authService);
            view.setVisible(true);
        });
    }
}