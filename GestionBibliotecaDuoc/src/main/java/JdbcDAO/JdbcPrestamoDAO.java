package JdbcDAO;

import DAO.PrestamoDAO;
import config.DataBaseConnection;
import modelo.Prestamo;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class JdbcPrestamoDAO implements PrestamoDAO {
    private static final String LOAN_SELECT = "SELECT p.id,p.id_estudiante,e.nombre AS estudiante,p.id_libro,l.titulo AS libro, " +
            "p.fecha_prestamo,p.fecha_devolucion,p.fecha_retorno,p.devuelto " +
            "FROM prestamos p JOIN estudiantes e ON e.id=p.id_estudiante JOIN libros l ON l.id=p.id_libro ";

    private final DataBaseConnection database;

    public JdbcPrestamoDAO(DataBaseConnection database) {
        this.database = database;
    }

    @Override public int crearConDescuentoStock(int idEstudiante, int idLibro, LocalDate inicio,
                                                LocalDate vencimiento) throws SQLException {
        String reservar = "UPDATE libros SET stock=stock-1 WHERE id=? AND stock>0";
        String insertar = "INSERT INTO prestamos(id_estudiante,id_libro,fecha_prestamo,fecha_devolucion,devuelto) VALUES (?,?,?,?,0)";
        try (Connection c = database.getConnection()) {
            c.setAutoCommit(false);
            try (var stock = c.prepareStatement(reservar);
                 var loan = c.prepareStatement(insertar, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                stock.setInt(1, idLibro);
                if (stock.executeUpdate() != 1) throw new SQLException("El libro no existe o no tiene stock disponible.");
                loan.setInt(1, idEstudiante);
                loan.setInt(2, idLibro);
                loan.setDate(3, java.sql.Date.valueOf(inicio));
                loan.setDate(4, java.sql.Date.valueOf(vencimiento));
                loan.executeUpdate();
                try (var keys = loan.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No se pudo obtener el número del préstamo.");
                    int id = keys.getInt(1);
                    c.commit();
                    return id;
                }
            } catch (SQLException e) {
                rollback(c, e);
                throw e;
            }
        }
    }

    @Override public void devolverConAumentoStock(int idPrestamo, int idEstudiante) throws SQLException {
        String marcar = "UPDATE prestamos SET devuelto=1,fecha_retorno=CURRENT_DATE WHERE id=? AND id_estudiante=? AND devuelto=0";
        String devolver = "UPDATE libros l JOIN prestamos p ON p.id_libro=l.id SET l.stock=l.stock+1 WHERE p.id=?";
        try (Connection c = database.getConnection()) {
            c.setAutoCommit(false);
            try (var loan = c.prepareStatement(marcar); var stock = c.prepareStatement(devolver)) {
                loan.setInt(1, idPrestamo);
                loan.setInt(2, idEstudiante);
                if (loan.executeUpdate() != 1) throw new SQLException("El préstamo ya fue devuelto o no pertenece a este estudiante.");
                stock.setInt(1, idPrestamo);
                if (stock.executeUpdate() != 1) throw new SQLException("No se pudo actualizar el inventario del libro.");
                c.commit();
            } catch (SQLException e) {
                rollback(c, e);
                throw e;
            }
        }
    }

    @Override public List<Prestamo> listarActivos(Integer idEstudiante) throws SQLException {
        String sql = LOAN_SELECT + "WHERE p.devuelto=0" +
                (idEstudiante == null ? "" : " AND p.id_estudiante=?") + " ORDER BY p.fecha_devolucion,p.id";
        try (var c = database.getConnection(); var s = c.prepareStatement(sql)) {
            if (idEstudiante != null) s.setInt(1, idEstudiante);
            try (var rs = s.executeQuery()) {
                return readLoans(rs);
            }
        }
    }

    @Override public List<Prestamo> historialEstudiante(int idEstudiante) throws SQLException {
        String sql = LOAN_SELECT + "WHERE p.id_estudiante=? ORDER BY p.fecha_prestamo DESC,p.id DESC";
        try (var c = database.getConnection(); var s = c.prepareStatement(sql)) {
            s.setInt(1, idEstudiante);
            try (var rs = s.executeQuery()) {
                return readLoans(rs);
            }
        }
    }

    @Override public String reporteLibrosMasPrestados() throws SQLException {
        String sql = "SELECT l.titulo,COUNT(*) AS cantidad FROM prestamos p " +
                "JOIN libros l ON l.id=p.id_libro GROUP BY l.id,l.titulo ORDER BY cantidad DESC,l.titulo";
        StringBuilder report = new StringBuilder("LIBROS MÁS PRESTADOS\n\n");
        try (var c = database.getConnection(); var s = c.prepareStatement(sql); var rs = s.executeQuery()) {
            int count = 0;
            while (rs.next()) {
                report.append(rs.getString("titulo")).append(" — ")
                        .append(rs.getInt("cantidad")).append(" préstamo(s)\n");
                count++;
            }
            if (count == 0) report.append("Todavía no hay préstamos registrados.\n");
        }
        return report.toString();
    }

    @Override public String reporteHistorialEstudiante() throws SQLException {
        String sql = LOAN_SELECT + "ORDER BY e.nombre,p.fecha_prestamo DESC,p.id DESC";
        try (var c = database.getConnection(); var s = c.prepareStatement(sql); var rs = s.executeQuery()) {
            return formatLoans("HISTORIAL DE PRÉSTAMOS", readLoans(rs));
        }
    }

    @Override public String reportePrestamosActivos() throws SQLException {
        String sql = LOAN_SELECT + "WHERE p.devuelto=0 ORDER BY p.fecha_devolucion,e.nombre";
        try (var c = database.getConnection(); var s = c.prepareStatement(sql); var rs = s.executeQuery()) {
            return formatLoans("PRÉSTAMOS ACTIVOS", readLoans(rs));
        }
    }

    private static List<Prestamo> readLoans(java.sql.ResultSet rs) throws SQLException {
        List<Prestamo> loans = new ArrayList<>();
        while (rs.next()) {
            loans.add(new Prestamo(rs.getInt("id"), rs.getInt("id_estudiante"), rs.getString("estudiante"),
                    rs.getInt("id_libro"), rs.getString("libro"), localDate(rs.getDate("fecha_prestamo")),
                    localDate(rs.getDate("fecha_devolucion")), localDate(rs.getDate("fecha_retorno")),
                    rs.getBoolean("devuelto")));
        }
        return loans;
    }

    private static LocalDate localDate(java.sql.Date value) {
        return value == null ? null : value.toLocalDate();
    }

    private static String formatLoans(String title, List<Prestamo> loans) {
        StringBuilder report = new StringBuilder(title).append("\n\n");
        if (loans.isEmpty()) return report.append("No hay préstamos para mostrar.\n").toString();
        for (Prestamo loan : loans) {
            report.append("#").append(loan.id()).append(" | ").append(loan.estudiante())
                    .append(" | ").append(loan.libro()).append(" | préstamo: ").append(loan.fechaPrestamo())
                    .append(" | vence: ").append(loan.fechaDevolucion()).append(" | ")
                    .append(loan.devuelto() ? "devuelto " + loan.fechaRetorno() : "activo")
                    .append(loan.estaAtrasado() ? " (atrasado)" : "").append('\n');
        }
        return report.toString();
    }

    private static void rollback(Connection c, SQLException original) {
        try {
            c.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }
}
