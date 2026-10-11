package DAO;

import modelo.Usuario;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UsuarioDAO {
    Optional<Usuario> autenticar(String correo, String contrasena) throws SQLException;
    List<Usuario> listar() throws SQLException;
    void crear(Usuario usuario, String contrasena) throws SQLException;
    void actualizar(Usuario usuario, String contrasenaNueva) throws SQLException;
    void eliminar(int id) throws SQLException;
}