package DAO;

import modelo.Categoria;
import java.sql.SQLException;
import java.util.List;

public interface CategoriaDAO {
    List<Categoria> listar() throws SQLException;
    void crear(String nombre) throws SQLException;
    void actualizar(int id, String nombre) throws SQLException;
    void eliminar(int id) throws SQLException;
}