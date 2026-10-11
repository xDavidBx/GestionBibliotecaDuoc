package servicios;

import DAO.UsuarioDAO;
import modelo.Usuario;
import java.sql.SQLException;
import java.util.Optional;

public final class AuthService {
    private final UsuarioDAO usuarios;
    public AuthService(UsuarioDAO usuarios) { this.usuarios = usuarios; }

    public Optional<Usuario> autenticar(String correo, String contrasena) throws SQLException {
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) return Optional.empty();
        return usuarios.autenticar(correo, contrasena);
    }
}