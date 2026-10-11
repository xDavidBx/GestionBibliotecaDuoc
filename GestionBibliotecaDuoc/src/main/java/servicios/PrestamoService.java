package servicios;

import DAO.PrestamoDAO;
import java.sql.SQLException;
import java.time.LocalDate;

/** Regla de préstamo sincronizada para evitar carreras entre solicitudes en esta ejecución. */
public final class PrestamoService {
    private final PrestamoDAO prestamos;
    public PrestamoService(PrestamoDAO prestamos) { this.prestamos = prestamos; }

    public synchronized int prestar(int idEstudiante, int idLibro) throws SQLException {
        if (idEstudiante <= 0 || idLibro <= 0) throw new IllegalArgumentException("Selecciona un estudiante y un libro.");
        LocalDate fechaPrestamo = LocalDate.now();
        LocalDate vencimiento = fechaPrestamo.plusDays(14);
        return prestamos.crearConDescuentoStock(idEstudiante, idLibro, fechaPrestamo, vencimiento);
    }

    public synchronized void devolver(int idPrestamo, int idEstudiante) throws SQLException {
        prestamos.devolverConAumentoStock(idPrestamo, idEstudiante);
    }
}