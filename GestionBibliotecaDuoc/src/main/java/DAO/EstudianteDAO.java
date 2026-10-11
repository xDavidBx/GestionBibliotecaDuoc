package DAO;

import modelo.Estudiante;
import java.sql.SQLException;
import java.util.List;

public interface EstudianteDAO {
    List<Estudiante> listar() throws SQLException;
    void crear(Estudiante estudiante, String contrasenaInicial) throws SQLException;
    void actualizar(Estudiante estudiante, String rutAnterior) throws SQLException;
    void eliminar(int id, String rut) throws SQLException;
}