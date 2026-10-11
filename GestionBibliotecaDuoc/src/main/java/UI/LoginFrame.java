package UI;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.BiConsumer;

public final class LoginFrame extends JFrame {
    private final JTextField correo = new JTextField();
    private final JPasswordField contrasena = new JPasswordField();
    private final JButton ingresar = new JButton("Ingresar");
    private BiConsumer<String, String> loginAction;

    public LoginFrame() {
        super("Biblioteca Escolar | Ingreso");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(430, 270);
        setMinimumSize(new Dimension(400, 250));
        setLocationRelativeTo(null);
        JPanel card = new JPanel(new GridLayout(0, 1, 8, 8));
        card.setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));
        JLabel title = new JLabel("Sistema de Gestión de Biblioteca", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        card.add(title);
        card.add(new JLabel("Correo electrónico"));
        card.add(correo);
        card.add(new JLabel("Contraseña"));
        card.add(contrasena);
        ingresar.addActionListener(e -> {
            if (loginAction == null) return;
            loginAction.accept(correo.getText(), new String(contrasena.getPassword()));
        });
        contrasena.addActionListener(e -> ingresar.doClick());
        card.add(ingresar);
        add(card, BorderLayout.CENTER);
        getRootPane().setDefaultButton(ingresar);
    }

    public void setLoginAction(BiConsumer<String, String> action) { this.loginAction = action; }
    public void setBusy(boolean busy) { ingresar.setEnabled(!busy); setCursor(busy ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR) : java.awt.Cursor.getDefaultCursor()); }
}