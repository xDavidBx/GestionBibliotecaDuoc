package DAO;

import modelo.Prestamo;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface PrestamoDAO {
    int crearConDescuentoStock(int idEstudiante, int idLibro, LocalDate fechaPrestamo,
                               LocalDate fechaDevolucion) throws SQLException;
    void devolverConAumentoStock(int idPrestamo, int idEstudiante) throws SQLException;
    List<Prestamo> listarActivos(Integer idEstudiante) throws SQLException;
    List<Prestamo> historialEstudiante(int idEstudiante) throws SQLException;
    String reporteLibrosMasPrestados() throws SQLException;
    String reporteHistorialEstudiante() throws SQLException;
    String reportePrestamosActivos() throws SQLException;
}
