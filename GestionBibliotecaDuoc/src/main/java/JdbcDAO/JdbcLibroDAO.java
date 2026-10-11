package JdbcDAO;

import config.DataBaseConnection;
import DAO.LibroDAO;
import modelo.Libro;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class JdbcLibroDAO implements LibroDAO {
    private final DataBaseConnection database;
    public JdbcLibroDAO(DataBaseConnection database) { this.database = database; }

    private static final String SELECT = "SELECT l.id,l.titulo,l.autor,l.isbn,l.editorial,l.stock,l.id_categoria,c.nombre AS categoria " +
            "FROM libros l JOIN categorias c ON c.id=l.id_categoria ";

    @Override public List<Libro> listar() throws SQLException {
        List<Libro> result = new ArrayList<>();
        try (var c = database.getConnection(); var s = c.prepareStatement(SELECT + "ORDER BY l.titulo"); var rs = s.executeQuery()) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    @Override public void crear(Libro l) throws SQLException {
        String sql = "INSERT INTO libros(titulo,autor,isbn,editorial,stock,id_categoria) VALUES (?,?,?,?,?,?)";
        try (var c = database.getConnection(); var s = c.prepareStatement(sql)) {
            fill(s, l); s.executeUpdate();
        }
    }

    @Override public void actualizar(Libro l) throws SQLException {
        String sql = "UPDATE libros SET titulo=?,autor=?,isbn=?,editorial=?,stock=?,id_categoria=? WHERE id=?";
        try (var c = database.getConnection(); var s = c.prepareStatement(sql)) {
            fill(s, l); s.setInt(7, l.id()); s.executeUpdate();
        }
    }

    private static void fill(java.sql.PreparedStatement s, Libro l) throws SQLException {
        s.setString(1, l.titulo().trim()); s.setString(2, l.autor().trim()); s.setString(3, l.isbn().trim());
        s.setString(4, l.editorial().trim()); s.setInt(5, l.stock()); s.setInt(6, l.idCategoria());
    }

    private static Libro map(java.sql.ResultSet rs) throws SQLException {
        return new Libro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"), rs.getString("isbn"),
                rs.getString("editorial"), rs.getInt("stock"), rs.getInt("id_categoria"), rs.getString("categoria"));
    }

    @Override public void eliminar(int id) throws SQLException {
        try (var c = database.getConnection(); var s = c.prepareStatement("DELETE FROM libros WHERE id=?")) {
            s.setInt(1, id); s.executeUpdate();
        }
    }
}
