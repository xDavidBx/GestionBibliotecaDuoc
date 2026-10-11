package DAO;

import modelo.Libro;
import java.sql.SQLException;
import java.util.List;

public interface LibroDAO {
    List<Libro> listar() throws SQLException;
    void crear(Libro libro) throws SQLException;
    void actualizar(Libro libro) throws SQLException;
    void eliminar(int id) throws SQLException;
}