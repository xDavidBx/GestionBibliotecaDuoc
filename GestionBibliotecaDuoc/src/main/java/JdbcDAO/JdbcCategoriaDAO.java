package JdbcDAO;

import config.DataBaseConnection;
import DAO.CategoriaDAO;
import modelo.Categoria;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class JdbcCategoriaDAO implements CategoriaDAO {
    private final DataBaseConnection database;
    public JdbcCategoriaDAO(DataBaseConnection database) { this.database = database; }

    @Override public List<Categoria> listar() throws SQLException {
        List<Categoria> result = new ArrayList<>();
        try (var c = database.getConnection(); var s = c.prepareStatement("SELECT id,nombre FROM categorias ORDER BY nombre");
             var rs = s.executeQuery()) {
            while (rs.next()) result.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
        }
        return result;
    }

    @Override public void crear(String nombre) throws SQLException {
        try (var c = database.getConnection(); var s = c.prepareStatement("INSERT INTO categorias(nombre) VALUES (?)")) {
            s.setString(1, nombre.trim());
            s.executeUpdate();
        }
    }

    @Override public void actualizar(int id, String nombre) throws SQLException {
        try (var c = database.getConnection(); var s = c.prepareStatement("UPDATE categorias SET nombre=? WHERE id=?")) {
            s.setString(1, nombre.trim()); s.setInt(2, id); s.executeUpdate();
        }
    }

    @Override public void eliminar(int id) throws SQLException {
        try (var c = database.getConnection(); var s = c.prepareStatement("DELETE FROM categorias WHERE id=?")) {
            s.setInt(1, id); s.executeUpdate();
        }
    }
}
